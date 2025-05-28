package org.example.service.impl;

import jakarta.annotation.PostConstruct;
import jakarta.persistence.EntityNotFoundException;
import org.example.datastructure.queue.PriorityQueue;
import org.example.dto.request.ScheduledTransactionRequest;
import org.example.dto.request.TransactionRequest;
import org.example.dto.response.ScheduledTransactionResponse;
import org.example.dto.response.WalletResponse;
import org.example.model.*;
import org.example.repository.ScheduledTransactionRepository;
import org.example.repository.WalletRepository;
import org.example.service.NotificationService;
import org.example.service.ScheduledTransactionService;
import org.example.service.TransactionService;
import org.example.service.WalletService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ScheduledTransactionServiceImpl implements ScheduledTransactionService {

    private static final Logger logger = LoggerFactory.getLogger(ScheduledTransactionServiceImpl.class);

    @Autowired
    private ScheduledTransactionRepository scheduledTransactionRepository;

    @Autowired
    private WalletRepository walletRepository;

    @Autowired
    private WalletService walletService;

    @Autowired
    private TransactionService transactionService;

    @Autowired
    private NotificationService notificationService;

    // Cola de prioridad para transacciones programadas
    private PriorityQueue<ScheduledTransaction> transactionQueue;

    @PostConstruct
    public void init() {
        // Inicializar la cola de prioridad con un comparador basado en la fecha programada
        transactionQueue = new PriorityQueue<>(
            Comparator.comparing(ScheduledTransaction::getScheduledDate)
        );

        // Cargar transacciones pendientes desde la base de datos
        loadPendingTransactions();
    }

    /**
     * Carga las transacciones pendientes desde la base de datos a la cola de prioridad
     */
    private void loadPendingTransactions() {
        List<ScheduledTransaction> pendingTransactions =
            scheduledTransactionRepository.findPendingScheduledTransactions(LocalDateTime.now().plusDays(7));

        logger.info("Cargando {} transacciones programadas pendientes en la cola de prioridad",
            pendingTransactions.size());

        for (ScheduledTransaction transaction : pendingTransactions) {
            // Calcular prioridad basada en la proximidad de la fecha de ejecución
            long minutesToExecution = ChronoUnit.MINUTES.between(
                LocalDateTime.now(), transaction.getScheduledDate());

            // Prioridad inversa: menor tiempo = mayor prioridad
            int priority = minutesToExecution > Integer.MAX_VALUE ?
                Integer.MAX_VALUE : (int) (Integer.MAX_VALUE - minutesToExecution);

            transactionQueue.enqueue(transaction, priority);
        }
    }

    @Override
    @Transactional
    public ScheduledTransactionResponse createScheduledTransaction(ScheduledTransactionRequest request, Long userId) {
        Wallet wallet = walletRepository.findById(request.getWalletId())
                .orElseThrow(() -> new EntityNotFoundException("Monedero no encontrado"));

        if (!wallet.getUser().getId().equals(userId)) {
            throw new AccessDeniedException("No tienes permiso para programar transacciones en este monedero");
        }

        // Si es una transferencia, verificar que el monedero destino existe
        if (request.getType() == TransactionType.TRANSFER && request.getTargetWalletId() == null) {
            throw new IllegalArgumentException("Para transferencias, debe especificar el monedero destino");
        }

        // Validar parámetros de recurrencia
        validateRecurrenceParameters(request);

        ScheduledTransaction scheduledTransaction = new ScheduledTransaction();
        scheduledTransaction.setType(request.getType());
        scheduledTransaction.setAmount(request.getAmount());
        scheduledTransaction.setScheduledDate(request.getScheduledDate());
        scheduledTransaction.setDescription(request.getDescription());
        scheduledTransaction.setWallet(wallet);
        scheduledTransaction.setTargetWalletId(request.getTargetWalletId());
        scheduledTransaction.setRecurrenceType(request.getRecurrenceType());

        // Configurar parámetros de recurrencia avanzados
        scheduledTransaction.setRecurrenceDay(request.getRecurrenceDay());
        scheduledTransaction.setRecurrenceWeekDay(request.getRecurrenceWeekDay());
        scheduledTransaction.setRecurrenceCount(request.getRecurrenceCount());
        scheduledTransaction.setRecurrenceEndDate(request.getRecurrenceEndDate());
        scheduledTransaction.setExecutionCount(0);

        ScheduledTransaction savedTransaction = scheduledTransactionRepository.save(scheduledTransaction);

        // Añadir a la cola de prioridad
        long minutesToExecution = ChronoUnit.MINUTES.between(
            LocalDateTime.now(), savedTransaction.getScheduledDate());

        // Prioridad inversa: menor tiempo = mayor prioridad
        int priority = minutesToExecution > Integer.MAX_VALUE ?
            Integer.MAX_VALUE : (int) (Integer.MAX_VALUE - minutesToExecution);

        transactionQueue.enqueue(savedTransaction, priority);

        // Crear notificación
        String notificationMessage = "Has programado una " + request.getType().getDescription().toLowerCase() +
                " por " + request.getAmount() + " para el " + request.getScheduledDate();

        if (request.getRecurrenceType() != null && request.getRecurrenceType() != RecurrenceType.ONCE) {
            notificationMessage += " con recurrencia " + request.getRecurrenceType().getDescription().toLowerCase();
        }

        notificationService.createNotification(
                userId,
                "Transacción Programada",
                notificationMessage,
                NotificationType.SCHEDULED_TRANSACTION_REMINDER,
                savedTransaction.getId(),
                Notification.RelatedEntityType.SCHEDULED_TRANSACTION);

        return convertToScheduledTransactionResponse(savedTransaction);
    }

    @Override
    public List<ScheduledTransactionResponse> getUserScheduledTransactions(Long userId) {
        return scheduledTransactionRepository.findPendingScheduledTransactionsByUser(userId).stream()
                .map(this::convertToScheduledTransactionResponse)
                .collect(Collectors.toList());
    }

    @Override
    public ScheduledTransactionResponse getScheduledTransactionById(Long id, Long userId) {
        ScheduledTransaction scheduledTransaction = scheduledTransactionRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Transacción programada no encontrada"));

        if (!scheduledTransaction.getWallet().getUser().getId().equals(userId)) {
            throw new AccessDeniedException("No tienes permiso para ver esta transacción programada");
        }

        return convertToScheduledTransactionResponse(scheduledTransaction);
    }

    @Override
    @Transactional
    public void cancelScheduledTransaction(Long id, Long userId) {
        ScheduledTransaction scheduledTransaction = scheduledTransactionRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Transacción programada no encontrada"));

        if (!scheduledTransaction.getWallet().getUser().getId().equals(userId)) {
            throw new AccessDeniedException("No tienes permiso para cancelar esta transacción programada");
        }

        if (scheduledTransaction.isExecuted()) {
            throw new IllegalStateException("No se puede cancelar una transacción programada ya ejecutada");
        }

        scheduledTransactionRepository.delete(scheduledTransaction);

        // Crear notificación
        notificationService.createNotification(
                userId,
                "Transacción Programada Cancelada",
                "Has cancelado la transacción programada: " + scheduledTransaction.getDescription(),
                NotificationType.SYSTEM,
                null,
                null);
    }

    @Override
    @Scheduled(fixedRate = 60000) // Ejecutar cada minuto
    @Transactional
    public void executePendingScheduledTransactions() {
        logger.info("Ejecutando transacciones programadas pendientes");

        // Obtener la hora actual
        LocalDateTime now = LocalDateTime.now();

        // Lista para almacenar transacciones a ejecutar
        List<ScheduledTransaction> transactionsToExecute = new ArrayList<>();

        // Extraer transacciones de la cola que deben ejecutarse ahora
        while (!transactionQueue.isEmpty()) {
            ScheduledTransaction nextTransaction = transactionQueue.peek();

            // Si la siguiente transacción debe ejecutarse ahora o ya pasó su tiempo
            if (nextTransaction != null &&
                (nextTransaction.getScheduledDate().isBefore(now) ||
                 nextTransaction.getScheduledDate().isEqual(now))) {

                // Extraer de la cola
                transactionQueue.dequeue();

                // Verificar que no esté ya ejecutada (doble verificación)
                if (!nextTransaction.isExecuted()) {
                    transactionsToExecute.add(nextTransaction);
                }
            } else {
                // Si la siguiente transacción es para el futuro, salir del bucle
                break;
            }
        }

        // También buscar en la base de datos por si hay transacciones que no estén en la cola
        List<ScheduledTransaction> dbPendingTransactions =
            scheduledTransactionRepository.findPendingScheduledTransactions(now);

        // Añadir transacciones de la base de datos que no estén ya en la lista
        for (ScheduledTransaction dbTransaction : dbPendingTransactions) {
            if (!transactionsToExecute.contains(dbTransaction)) {
                transactionsToExecute.add(dbTransaction);
            }
        }

        // Ejecutar las transacciones
        for (ScheduledTransaction scheduledTransaction : transactionsToExecute) {
            try {
                executeScheduledTransaction(scheduledTransaction);

                // Si es recurrente, crear la próxima transacción programada
                if (scheduledTransaction.getRecurrenceType() != null &&
                        scheduledTransaction.getRecurrenceType() != RecurrenceType.ONCE) {
                    createNextRecurringTransaction(scheduledTransaction);
                }
            } catch (Exception e) {
                logger.error("Error al ejecutar transacción programada ID {}: {}",
                        scheduledTransaction.getId(), e.getMessage());
            }
        }

        // Recargar la cola si está vacía o casi vacía
        if (transactionQueue.size() < 10) {
            loadPendingTransactions();
        }
    }

    private void executeScheduledTransaction(ScheduledTransaction scheduledTransaction) {
        TransactionRequest transactionRequest = new TransactionRequest();
        transactionRequest.setType(scheduledTransaction.getType());
        transactionRequest.setAmount(scheduledTransaction.getAmount());
        transactionRequest.setDescription(scheduledTransaction.getDescription() +
                " (Transacción programada #" + scheduledTransaction.getId() + ")");

        switch (scheduledTransaction.getType()) {
            case DEPOSIT:
                transactionRequest.setTargetWalletId(scheduledTransaction.getWallet().getId());
                break;
            case WITHDRAWAL:
                transactionRequest.setSourceWalletId(scheduledTransaction.getWallet().getId());
                break;
            case TRANSFER:
                transactionRequest.setSourceWalletId(scheduledTransaction.getWallet().getId());
                transactionRequest.setTargetWalletId(scheduledTransaction.getTargetWalletId());
                break;
            default:
                throw new IllegalArgumentException("Tipo de transacción no soportado para transacciones programadas");
        }

        // Ejecutar la transacción
        transactionService.processTransaction(transactionRequest, scheduledTransaction.getWallet().getUser().getId());

        // Marcar como ejecutada
        scheduledTransaction.markAsExecuted();
        scheduledTransactionRepository.save(scheduledTransaction);

        // Crear notificación
        notificationService.createNotification(
                scheduledTransaction.getWallet().getUser().getId(),
                "Transacción Programada Ejecutada",
                "Se ha ejecutado la transacción programada: " + scheduledTransaction.getDescription(),
                NotificationType.TRANSACTION_COMPLETED,
                scheduledTransaction.getId(),
                Notification.RelatedEntityType.SCHEDULED_TRANSACTION);
    }

    /**
     * Valida los parámetros de recurrencia de una solicitud de transacción programada
     */
    private void validateRecurrenceParameters(ScheduledTransactionRequest request) {
        // Si no es recurrente, no se necesitan validaciones adicionales
        if (request.getRecurrenceType() == null || request.getRecurrenceType() == RecurrenceType.ONCE) {
            return;
        }

        // Para recurrencia mensual con día específico
        if (request.getRecurrenceType() == RecurrenceType.MONTHLY &&
            request.getRecurrenceDay() != null &&
            (request.getRecurrenceDay() < 1 || request.getRecurrenceDay() > 31)) {
            throw new IllegalArgumentException("El día del mes debe estar entre 1 y 31");
        }

        // Para recurrencia mensual con día de la semana específico
        if (request.getRecurrenceType() == RecurrenceType.MONTHLY &&
            request.getRecurrenceWeekDay() != null &&
            (request.getRecurrenceWeekDay() < 1 || request.getRecurrenceWeekDay() > 7)) {
            throw new IllegalArgumentException("El día de la semana debe estar entre 1 y 7");
        }

        // Si se especifica un número máximo de ejecuciones
        if (request.getRecurrenceCount() != null && request.getRecurrenceCount() < 1) {
            throw new IllegalArgumentException("El número de ejecuciones debe ser al menos 1");
        }

        // Si se especifica una fecha de fin
        if (request.getRecurrenceEndDate() != null &&
            request.getRecurrenceEndDate().isBefore(request.getScheduledDate())) {
            throw new IllegalArgumentException("La fecha de fin debe ser posterior a la fecha programada");
        }

        // No se puede especificar tanto un número máximo de ejecuciones como una fecha de fin
        if (request.getRecurrenceCount() != null && request.getRecurrenceEndDate() != null) {
            throw new IllegalArgumentException("No se puede especificar tanto un número máximo de ejecuciones como una fecha de fin");
        }
    }

    private void createNextRecurringTransaction(ScheduledTransaction currentTransaction) {
        // Calcular la próxima fecha de ejecución
        LocalDateTime nextExecutionDate = currentTransaction.calculateNextExecutionDate();
        if (nextExecutionDate == null) {
            logger.info("No se creará una nueva transacción recurrente para la transacción {}: fin de recurrencia",
                currentTransaction.getId());
            return;
        }

        // Crear la nueva transacción programada
        ScheduledTransaction nextTransaction = new ScheduledTransaction();
        nextTransaction.setType(currentTransaction.getType());
        nextTransaction.setAmount(currentTransaction.getAmount());
        nextTransaction.setScheduledDate(nextExecutionDate);
        nextTransaction.setDescription(currentTransaction.getDescription());
        nextTransaction.setWallet(currentTransaction.getWallet());
        nextTransaction.setTargetWalletId(currentTransaction.getTargetWalletId());
        nextTransaction.setRecurrenceType(currentTransaction.getRecurrenceType());

        // Copiar parámetros de recurrencia avanzados
        nextTransaction.setRecurrenceDay(currentTransaction.getRecurrenceDay());
        nextTransaction.setRecurrenceWeekDay(currentTransaction.getRecurrenceWeekDay());
        nextTransaction.setRecurrenceCount(currentTransaction.getRecurrenceCount());
        nextTransaction.setRecurrenceEndDate(currentTransaction.getRecurrenceEndDate());

        // Copiar el contador de ejecuciones
        if (currentTransaction.getExecutionCount() != null) {
            nextTransaction.setExecutionCount(currentTransaction.getExecutionCount());
        }

        ScheduledTransaction savedTransaction = scheduledTransactionRepository.save(nextTransaction);

        // Añadir a la cola de prioridad
        long minutesToExecution = ChronoUnit.MINUTES.between(
            LocalDateTime.now(), savedTransaction.getScheduledDate());

        // Prioridad inversa: menor tiempo = mayor prioridad
        int priority = minutesToExecution > Integer.MAX_VALUE ?
            Integer.MAX_VALUE : (int) (Integer.MAX_VALUE - minutesToExecution);

        transactionQueue.enqueue(savedTransaction, priority);

        // Crear notificación
        notificationService.createNotification(
                currentTransaction.getWallet().getUser().getId(),
                "Nueva Transacción Programada",
                "Se ha programado la siguiente transacción recurrente para el " + nextExecutionDate,
                NotificationType.SCHEDULED_TRANSACTION_REMINDER,
                nextTransaction.getId(),
                Notification.RelatedEntityType.SCHEDULED_TRANSACTION);

        logger.info("Creada nueva transacción programada recurrente ID {} para el {}",
            savedTransaction.getId(), nextExecutionDate);
    }

    private ScheduledTransactionResponse convertToScheduledTransactionResponse(ScheduledTransaction transaction) {
        ScheduledTransactionResponse response = new ScheduledTransactionResponse();
        response.setId(transaction.getId());
        response.setType(transaction.getType());
        response.setAmount(transaction.getAmount());
        response.setScheduledDate(transaction.getScheduledDate());
        response.setCreationDate(transaction.getCreationDate());
        response.setDescription(transaction.getDescription());
        response.setTargetWalletId(transaction.getTargetWalletId());
        response.setRecurrenceType(transaction.getRecurrenceType());
        response.setRecurrenceDay(transaction.getRecurrenceDay());
        response.setRecurrenceWeekDay(transaction.getRecurrenceWeekDay());
        response.setRecurrenceCount(transaction.getRecurrenceCount());
        response.setRecurrenceEndDate(transaction.getRecurrenceEndDate());
        response.setExecuted(transaction.isExecuted());
        response.setExecutionDate(transaction.getExecutionDate());
        response.setExecutionCount(transaction.getExecutionCount());

        // Calcular la próxima fecha de ejecución si es recurrente
        if (!transaction.isExecuted() ||
            (transaction.getRecurrenceType() != null &&
             transaction.getRecurrenceType() != RecurrenceType.ONCE)) {
            response.setNextExecutionDate(transaction.calculateNextExecutionDate());
        }

        WalletResponse walletResponse = walletService.convertToWalletResponse(transaction.getWallet());
        response.setWallet(walletResponse);

        return response;
    }
}

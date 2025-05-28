package org.example.service.impl;

import jakarta.persistence.EntityNotFoundException;
import org.example.dto.request.TransactionRequest;
import org.example.dto.response.TransactionResponse;
import org.example.dto.response.WalletResponse;
import org.example.model.*;
import org.example.repository.TransactionRepository;
import org.example.repository.WalletRepository;
import org.example.service.NotificationService;
import org.example.service.PointsAccountService;
import org.example.service.TransactionService;
import org.example.service.WalletService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class TransactionServiceImpl implements TransactionService {

    private static final Logger logger = LoggerFactory.getLogger(TransactionServiceImpl.class);

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private WalletRepository walletRepository;

    @Autowired
    private WalletService walletService;

    @Autowired
    private PointsAccountService pointsAccountService;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private TransactionHistoryManager transactionHistoryManager;

    @Override
    @Transactional
    public TransactionResponse processTransaction(TransactionRequest request, Long userId) {
        Transaction transaction = new Transaction();
        transaction.setType(request.getType());
        transaction.setAmount(request.getAmount());
        transaction.setDescription(request.getDescription());
        transaction.setReferenceId(UUID.randomUUID().toString());
        transaction.setStatus(TransactionStatus.COMPLETED);

        // Asignar fecha actual si no es transacción programada y no viene especificada
        if (request.getType() != TransactionType.SCHEDULED_TRANSFER) {
            transaction.setTransactionDate(java.time.LocalDateTime.now());
        }

        switch (request.getType()) {
            case DEPOSIT:
                processDeposit(transaction, request.getTargetWalletId(), userId);
                break;
            case WITHDRAWAL:
                processWithdrawal(transaction, request.getSourceWalletId(), userId);
                break;
            case TRANSFER:
                processTransfer(transaction, request.getSourceWalletId(), request.getTargetWalletId(), userId);
                break;
            default:
                throw new IllegalArgumentException("Tipo de transacción no soportado");
        }

        Transaction savedTransaction = transactionRepository.save(transaction);

        // Añadir a la pila de historial para permitir reversiones
        transactionHistoryManager.pushTransaction(savedTransaction);

        // Actualizar las notificaciones con el ID de la transacción guardada
        updateNotificationsWithTransactionId(savedTransaction);

        // Actualizar puntos del usuario
        if (transaction.getPointsEarned() > 0) {
            User user = transaction.getSourceWallet() != null ?
                    transaction.getSourceWallet().getUser() :
                    transaction.getTargetWallet().getUser();

            pointsAccountService.addPoints(
                    user.getId(),
                    transaction.getPointsEarned(),
                    "Puntos ganados por " + transaction.getType().getDescription(),
                    savedTransaction.getId());

            // Crear notificación de puntos ganados
            notificationService.createNotification(
                    user.getId(),
                    "Puntos Ganados",
                    "Has ganado " + transaction.getPointsEarned() + " puntos por tu " +
                            transaction.getType().getDescription().toLowerCase(),
                    NotificationType.POINTS_EARNED,
                    savedTransaction.getId(),
                    Notification.RelatedEntityType.TRANSACTION);
        }

        return convertToTransactionResponse(savedTransaction);
    }

    private void processDeposit(Transaction transaction, Long targetWalletId, Long userId) {
        Wallet targetWallet = walletRepository.findById(targetWalletId)
                .orElseThrow(() -> new EntityNotFoundException("Monedero destino no encontrado"));

        // Validar que el usuario sea dueño del monedero destino SOLO para depósitos
        if (!targetWallet.getUser().getId().equals(userId)) {
            throw new AccessDeniedException("No tienes permiso para depositar en este monedero");
        }

        transaction.setTargetWallet(targetWallet);
        walletService.updateWalletBalance(targetWalletId, transaction.getAmount());

        // Marcar que se debe crear notificación de depósito
        transaction.setDescription(transaction.getDescription() + "|NOTIFY_DEPOSIT:" + userId);
    }

    private void processWithdrawal(Transaction transaction, Long sourceWalletId, Long userId) {
        Wallet sourceWallet = walletRepository.findById(sourceWalletId)
                .orElseThrow(() -> new EntityNotFoundException("Monedero origen no encontrado"));

        if (!sourceWallet.getUser().getId().equals(userId)) {
            throw new AccessDeniedException("No tienes permiso para retirar de este monedero");
        }

        if (!walletService.hasWalletSufficientBalance(sourceWalletId, transaction.getAmount())) {
            throw new IllegalStateException("Saldo insuficiente para realizar esta operación");
        }

        transaction.setSourceWallet(sourceWallet);
        walletService.updateWalletBalance(sourceWalletId, transaction.getAmount().negate());

        // Marcar que se debe crear notificación de retiro
        transaction.setDescription(transaction.getDescription() + "|NOTIFY_WITHDRAWAL:" + userId);
    }

    private void processTransfer(Transaction transaction, Long sourceWalletId, Long targetWalletId, Long userId) {
        Wallet sourceWallet = walletRepository.findById(sourceWalletId)
                .orElseThrow(() -> new EntityNotFoundException("Monedero origen no encontrado"));

        if (!sourceWallet.getUser().getId().equals(userId)) {
            throw new AccessDeniedException("No tienes permiso para transferir desde este monedero");
        }

        Wallet targetWallet = walletRepository.findById(targetWalletId)
                .orElseThrow(() -> new EntityNotFoundException("Monedero destino no encontrado"));

        // NO validar el usuario del monedero destino para transferencias

        if (!walletService.hasWalletSufficientBalance(sourceWalletId, transaction.getAmount())) {
            throw new IllegalStateException("Saldo insuficiente para realizar esta transferencia");
        }

        transaction.setSourceWallet(sourceWallet);
        transaction.setTargetWallet(targetWallet);

        walletService.updateWalletBalance(sourceWalletId, transaction.getAmount().negate());
        walletService.updateWalletBalance(targetWalletId, transaction.getAmount());

        // Marcar que se deben crear notificaciones de transferencia
        transaction.setDescription(transaction.getDescription() +
                "|NOTIFY_TRANSFER_SENDER:" + userId +
                "|NOTIFY_TRANSFER_RECEIVER:" + targetWallet.getUser().getId());
    }

    @Override
    public Page<TransactionResponse> getUserTransactions(Long userId, Pageable pageable) {
        // Usar la nueva consulta que evita duplicados
        Page<Transaction> transactions = transactionRepository.findAllUserTransactions(userId, pageable);

        // Convertir las transacciones a DTOs
        return transactions.map(this::convertToTransactionResponse);
    }

    @Override
    public TransactionResponse getTransactionById(Long transactionId, Long userId) {
        Transaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new EntityNotFoundException("Transacción no encontrada"));

        // Verificar que el usuario es propietario de alguno de los monederos involucrados
        boolean isOwner = false;
        if (transaction.getSourceWallet() != null && transaction.getSourceWallet().getUser().getId().equals(userId)) {
            isOwner = true;
        } else if (transaction.getTargetWallet() != null && transaction.getTargetWallet().getUser().getId().equals(userId)) {
            isOwner = true;
        }

        if (!isOwner) {
            throw new AccessDeniedException("No tienes permiso para ver esta transacción");
        }

        return convertToTransactionResponse(transaction);
    }

    @Override
    public Page<TransactionResponse> getWalletTransactions(Long walletId, Long userId, Pageable pageable) {
        Wallet wallet = walletRepository.findById(walletId)
                .orElseThrow(() -> new EntityNotFoundException("Monedero no encontrado"));

        if (!wallet.getUser().getId().equals(userId)) {
            throw new AccessDeniedException("No tienes permiso para ver las transacciones de este monedero");
        }

        return transactionRepository.findAllByWallet(wallet, pageable)
                .map(this::convertToTransactionResponse);
    }

    @Override
    @Transactional
    public TransactionResponse reverseTransaction(Long transactionId, Long userId) {
        // Usar el gestor de historial para verificar si la transacción es reversible
        Optional<Transaction> optionalTransaction = transactionHistoryManager.getReversibleTransaction(transactionId);

        if (!optionalTransaction.isPresent()) {
            throw new IllegalStateException("La transacción no existe o no es reversible");
        }

        Transaction transaction = optionalTransaction.get();

        // Verificar que el usuario es propietario de alguno de los monederos involucrados
        boolean isOwner = false;
        if (transaction.getSourceWallet() != null && transaction.getSourceWallet().getUser().getId().equals(userId)) {
            isOwner = true;
        } else if (transaction.getTargetWallet() != null && transaction.getTargetWallet().getUser().getId().equals(userId)) {
            isOwner = true;
        }

        if (!isOwner) {
            throw new AccessDeniedException("No tienes permiso para revertir esta transacción");
        }

        // Crear transacción de reversión
        Transaction reversalTransaction = new Transaction();
        reversalTransaction.setType(transaction.getType());
        reversalTransaction.setAmount(transaction.getAmount());
        reversalTransaction.setDescription("Reversión de transacción #" + transaction.getId());
        reversalTransaction.setReferenceId(UUID.randomUUID().toString());
        reversalTransaction.setStatus(TransactionStatus.COMPLETED);

        // Invertir origen y destino
        reversalTransaction.setSourceWallet(transaction.getTargetWallet());
        reversalTransaction.setTargetWallet(transaction.getSourceWallet());

        // Actualizar saldos
        if (transaction.getSourceWallet() != null) {
            walletService.updateWalletBalance(transaction.getSourceWallet().getId(), transaction.getAmount());
        }
        if (transaction.getTargetWallet() != null) {
            walletService.updateWalletBalance(transaction.getTargetWallet().getId(), transaction.getAmount().negate());
        }

        // Marcar transacción original como revertida
        transaction.setReversed(true);
        transaction.setStatus(TransactionStatus.REVERSED);
        transactionRepository.save(transaction);

        // Revertir puntos si es necesario
        if (transaction.getPointsEarned() > 0) {
            User user = transaction.getSourceWallet() != null ?
                    transaction.getSourceWallet().getUser() :
                    transaction.getTargetWallet().getUser();

            pointsAccountService.adjustPoints(
                    user.getId(),
                    -transaction.getPointsEarned(),
                    "Puntos revertidos por cancelación de transacción #" + transaction.getId());
        }

        // Crear notificación de reversión
        notificationService.createNotification(
                userId,
                "Transacción Revertida",
                "Se ha revertido la transacción #" + transaction.getId() + " por un monto de " + transaction.getAmount(),
                NotificationType.TRANSACTION_REVERSED,
                transaction.getId(),
                Notification.RelatedEntityType.TRANSACTION);

        Transaction savedReversal = transactionRepository.save(reversalTransaction);
        return convertToTransactionResponse(savedReversal);
    }

    @Override
    @Transactional
    public TransactionResponse reverseLastTransaction(Long userId) {
        // Obtener la última transacción del usuario desde la pila
        Transaction lastTransaction = transactionHistoryManager.peekLastTransaction(userId);

        if (lastTransaction == null) {
            throw new IllegalStateException("No hay transacciones recientes para revertir");
        }

        // Verificar si es reversible
        if (!transactionHistoryManager.isTransactionReversible(lastTransaction)) {
            throw new IllegalStateException("La última transacción no es reversible");
        }

        // Extraer de la pila
        transactionHistoryManager.popLastTransaction(userId);

        // Revertir la transacción
        return reverseTransaction(lastTransaction.getId(), userId);
    }

    @Override
    public List<TransactionResponse> getReversibleTransactions(Long userId, int limit) {
        // Obtener los monederos del usuario
        List<Wallet> userWallets = walletRepository.findByUserId(userId);

        if (userWallets.isEmpty()) {
            return new ArrayList<>();
        }

        // Obtener las últimas transacciones de los monederos del usuario
        List<Transaction> recentTransactions = new ArrayList<>();

        for (Wallet wallet : userWallets) {
            // Buscar transacciones como origen o destino
            List<Transaction> walletTransactions = transactionRepository.findRecentByWallet(
                wallet.getId(), limit);

            recentTransactions.addAll(walletTransactions);
        }

        // Filtrar solo las reversibles
        List<Transaction> reversibleTransactions = recentTransactions.stream()
            .filter(transactionHistoryManager::isTransactionReversible)
            .limit(limit)
            .collect(Collectors.toList());

        // Convertir a DTOs
        return reversibleTransactions.stream()
            .map(this::convertToTransactionResponse)
            .collect(Collectors.toList());
    }

    @Override
    public boolean isTransactionReversible(Long transactionId, Long userId) {
        Optional<Transaction> transaction = transactionRepository.findById(transactionId);

        if (!transaction.isPresent()) {
            return false;
        }

        // Verificar propiedad
        boolean isOwner = false;
        if (transaction.get().getSourceWallet() != null &&
            transaction.get().getSourceWallet().getUser().getId().equals(userId)) {
            isOwner = true;
        } else if (transaction.get().getTargetWallet() != null &&
                   transaction.get().getTargetWallet().getUser().getId().equals(userId)) {
            isOwner = true;
        }

        if (!isOwner) {
            return false;
        }

        // Verificar si es reversible
        return transactionHistoryManager.isTransactionReversible(transaction.get());
    }

    /**
     * Crea las notificaciones correspondientes después de guardar la transacción
     */
    private void updateNotificationsWithTransactionId(Transaction savedTransaction) {
        String description = savedTransaction.getDescription();

        if (description.contains("NOTIFY_DEPOSIT:")) {
            String[] parts = description.split("\\|NOTIFY_DEPOSIT:");
            if (parts.length > 1) {
                Long userId = Long.parseLong(parts[1].split("\\|")[0]);
                notificationService.createNotification(
                        userId,
                        "Depósito Realizado",
                        "Se ha realizado un depósito de $" + savedTransaction.getAmount() +
                        " en tu monedero " + savedTransaction.getTargetWallet().getName(),
                        NotificationType.TRANSACTION_COMPLETED,
                        savedTransaction.getId(),
                        Notification.RelatedEntityType.TRANSACTION);

                // Limpiar la descripción
                savedTransaction.setDescription(parts[0]);
            }
        }

        if (description.contains("NOTIFY_WITHDRAWAL:")) {
            String[] parts = description.split("\\|NOTIFY_WITHDRAWAL:");
            if (parts.length > 1) {
                Long userId = Long.parseLong(parts[1].split("\\|")[0]);
                notificationService.createNotification(
                        userId,
                        "Retiro Realizado",
                        "Se ha realizado un retiro de $" + savedTransaction.getAmount() +
                        " de tu monedero " + savedTransaction.getSourceWallet().getName(),
                        NotificationType.TRANSACTION_COMPLETED,
                        savedTransaction.getId(),
                        Notification.RelatedEntityType.TRANSACTION);

                // Limpiar la descripción
                savedTransaction.setDescription(parts[0]);
            }
        }

        if (description.contains("NOTIFY_TRANSFER_SENDER:")) {
            String[] senderParts = description.split("\\|NOTIFY_TRANSFER_SENDER:");
            if (senderParts.length > 1) {
                Long senderId = Long.parseLong(senderParts[1].split("\\|")[0]);
                notificationService.createNotification(
                        senderId,
                        "Transferencia Enviada",
                        "Has enviado $" + savedTransaction.getAmount() +
                        " desde tu monedero " + savedTransaction.getSourceWallet().getName() +
                        " al monedero " + savedTransaction.getTargetWallet().getName() +
                        " de " + savedTransaction.getTargetWallet().getUser().getUsername(),
                        NotificationType.TRANSACTION_COMPLETED,
                        savedTransaction.getId(),
                        Notification.RelatedEntityType.TRANSACTION);
            }
        }

        if (description.contains("NOTIFY_TRANSFER_RECEIVER:")) {
            String[] receiverParts = description.split("\\|NOTIFY_TRANSFER_RECEIVER:");
            if (receiverParts.length > 1) {
                Long receiverId = Long.parseLong(receiverParts[1].split("\\|")[0]);
                notificationService.createNotification(
                        receiverId,
                        "Transferencia Recibida",
                        "Has recibido $" + savedTransaction.getAmount() +
                        " en tu monedero " + savedTransaction.getTargetWallet().getName() +
                        " de " + savedTransaction.getSourceWallet().getUser().getUsername(),
                        NotificationType.TRANSACTION_COMPLETED,
                        savedTransaction.getId(),
                        Notification.RelatedEntityType.TRANSACTION);

                // Limpiar la descripción de las marcas de notificación
                String cleanDescription = savedTransaction.getDescription()
                        .replaceAll("\\|NOTIFY_TRANSFER_SENDER:[0-9]+", "")
                        .replaceAll("\\|NOTIFY_TRANSFER_RECEIVER:[0-9]+", "");
                savedTransaction.setDescription(cleanDescription);
            }
        }

        // Guardar la transacción con la descripción limpia si se modificó
        if (!description.equals(savedTransaction.getDescription())) {
            transactionRepository.save(savedTransaction);
        }
    }

    private TransactionResponse convertToTransactionResponse(Transaction transaction) {
        TransactionResponse response = new TransactionResponse();
        response.setId(transaction.getId());
        response.setType(transaction.getType());
        response.setAmount(transaction.getAmount());
        response.setTransactionDate(transaction.getTransactionDate());
        response.setDescription(transaction.getDescription());
        response.setPointsEarned(transaction.getPointsEarned());
        response.setReversed(transaction.isReversed());
        response.setReferenceId(transaction.getReferenceId());
        response.setStatus(transaction.getStatus());

        if (transaction.getSourceWallet() != null) {
            response.setSourceWallet(walletService.convertToWalletResponse(transaction.getSourceWallet()));
        }

        if (transaction.getTargetWallet() != null) {
            response.setTargetWallet(walletService.convertToWalletResponse(transaction.getTargetWallet()));
        }

        return response;
    }
}

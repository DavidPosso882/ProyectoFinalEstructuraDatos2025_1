package org.example.service.impl;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.map.IMap;
import jakarta.persistence.EntityNotFoundException;
import org.example.dto.request.WalletRequest;
import org.example.dto.response.CurrencyResponse;
import org.example.dto.response.WalletResponse;
import org.example.model.Currency;
import org.example.model.Notification;
import org.example.model.NotificationType;
import org.example.model.Transaction;
import org.example.model.TransactionStatus;
import org.example.model.TransactionType;
import org.example.model.User;
import org.example.model.Wallet;
import org.example.model.WalletType;
import org.example.repository.CurrencyRepository;
import org.example.repository.TransactionRepository;
import org.example.repository.UserRepository;
import org.example.repository.WalletRepository;
import org.example.service.CurrencyService;
import org.example.service.NotificationService;
import org.example.service.PointsAccountService;
import org.example.service.WalletService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class WalletServiceImpl implements WalletService {

    private static final Logger logger = LoggerFactory.getLogger(WalletServiceImpl.class);

    @Autowired
    private WalletRepository walletRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CurrencyRepository currencyRepository;

    @Autowired
    private CurrencyService currencyService;

    @Autowired
    private HazelcastInstance hazelcastInstance;

    @Autowired
    private HazelcastTransactionManager transactionManager;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private PointsAccountService pointsAccountService;

    @Override
    public List<WalletResponse> getUserWallets(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado"));

        return walletRepository.findByUser(user).stream()
                .map(this::convertToWalletResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<WalletResponse> getUserWalletsByCurrency(Long userId, String currencyCode) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado"));

        Currency currency = currencyRepository.findByCode(currencyCode)
                .orElseThrow(() -> new EntityNotFoundException("Moneda no encontrada con código: " + currencyCode));

        return walletRepository.findByUser(user).stream()
                .filter(wallet -> wallet.getCurrency() != null && wallet.getCurrency().getCode().equals(currencyCode))
                .map(this::convertToWalletResponse)
                .collect(Collectors.toList());
    }

    @Override
    public WalletResponse getWalletById(Long walletId, Long userId) {
        Wallet wallet = walletRepository.findById(walletId)
                .orElseThrow(() -> new EntityNotFoundException("Monedero no encontrado"));

        if (!wallet.getUser().getId().equals(userId)) {
            throw new AccessDeniedException("No tienes permiso para acceder a este monedero");
        }

        return convertToWalletResponse(wallet);
    }

    @Override
    @Transactional
    @CacheEvict(value = {"userWallets", "walletBalances"}, key = "#userId")
    public WalletResponse createWallet(WalletRequest walletRequest, Long userId) {
        // Usar la moneda base por defecto
        Currency baseCurrency = currencyService.getBaseCurrency();
        return createWalletWithCurrency(walletRequest, userId, baseCurrency);
    }

    @Override
    @Transactional
    @CacheEvict(value = {"userWallets", "walletBalances", "userWalletsByCurrency"}, key = "#userId")
    public WalletResponse createWallet(WalletRequest walletRequest, Long userId, String currencyCode) {
        Currency currency = currencyRepository.findByCode(currencyCode)
                .orElseThrow(() -> new EntityNotFoundException("Moneda no encontrada con código: " + currencyCode));

        return createWalletWithCurrency(walletRequest, userId, currency);
    }

    private WalletResponse createWalletWithCurrency(WalletRequest walletRequest, Long userId, Currency currency) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado"));

        Wallet wallet = new Wallet();
        wallet.setName(walletRequest.getName());
        wallet.setDescription(walletRequest.getDescription());
        wallet.setWalletType(walletRequest.getWalletType());
        BigDecimal initialBalance = walletRequest.getInitialBalance() != null ? walletRequest.getInitialBalance() : BigDecimal.ZERO;
        if (initialBalance.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("El balance inicial no puede ser negativo");
        }
        wallet.setBalance(initialBalance);
        wallet.setUser(user);
        wallet.setCurrency(currency);

        Wallet savedWallet = walletRepository.save(wallet);
        logger.info("Monedero creado: {} con moneda: {} y balance inicial: {}", savedWallet.getName(), currency.getCode(), initialBalance);
        return convertToWalletResponse(savedWallet);
    }

    @Override
    @Transactional
    @CacheEvict(value = {"userWallets", "wallets", "walletBalances", "userWalletsByCurrency"}, allEntries = true)
    public void deleteWallet(Long walletId, Long userId) {
        Wallet wallet = walletRepository.findById(walletId)
                .orElseThrow(() -> new EntityNotFoundException("Monedero no encontrado"));

        if (!wallet.getUser().getId().equals(userId)) {
            throw new AccessDeniedException("No tienes permiso para eliminar este monedero");
        }

        if (wallet.getBalance().compareTo(BigDecimal.ZERO) > 0) {
            throw new IllegalStateException("No puedes eliminar un monedero con saldo positivo");
        }

        walletRepository.delete(wallet);
        logger.info("Monedero eliminado: {}", wallet.getName());
    }

    @Override
    @Transactional
    @CacheEvict(value = {"wallets", "walletBalances"}, key = "#walletId")
    public Wallet updateWalletBalance(Long walletId, BigDecimal amount) {
        Wallet wallet = walletRepository.findById(walletId)
                .orElseThrow(() -> new EntityNotFoundException("Monedero no encontrado"));

        wallet.updateBalance(amount);

        // Actualizar el saldo en Hazelcast
        IMap<String, BigDecimal> walletBalances = hazelcastInstance.getMap("walletBalances");
        walletBalances.put("wallet:" + walletId, wallet.getBalance());

        return walletRepository.save(wallet);
    }

    @Override
    @Transactional
    public Wallet transferBetweenWallets(Long sourceWalletId, Long targetWalletId, BigDecimal amount, Long userId) {
        Wallet sourceWallet = walletRepository.findById(sourceWalletId)
                .orElseThrow(() -> new EntityNotFoundException("Monedero origen no encontrado"));
        Wallet targetWallet = walletRepository.findById(targetWalletId)
                .orElseThrow(() -> new EntityNotFoundException("Monedero destino no encontrado"));

        // Validar que el usuario autenticado sea dueño del monedero origen
        if (!sourceWallet.getUser().getId().equals(userId)) {
            throw new AccessDeniedException("No tienes permiso para transferir desde este monedero");
        }
        // No validar el usuario del monedero destino

        // Verificar que hay saldo suficiente
        if (!hasWalletSufficientBalance(sourceWalletId, amount)) {
            throw new IllegalStateException("Saldo insuficiente en el monedero origen");
        }

        // Si las monedas son diferentes, convertir el monto
        BigDecimal convertedAmount = amount;
        if (sourceWallet.getCurrency() != null && targetWallet.getCurrency() != null &&
                !sourceWallet.getCurrency().getCode().equals(targetWallet.getCurrency().getCode())) {
            convertedAmount = currencyService.convertCurrency(
                    amount,
                    sourceWallet.getCurrency().getCode(),
                    targetWallet.getCurrency().getCode());
            logger.info("Conversión de moneda: {} {} a {} {}",
                    amount, sourceWallet.getCurrency().getCode(),
                    convertedAmount, targetWallet.getCurrency().getCode());
        }

        // Crear la transacción para registrar la transferencia
        Transaction transaction = new Transaction();
        transaction.setType(TransactionType.TRANSFER);
        transaction.setAmount(amount);
        transaction.setDescription("Transferencia entre monederos");
        transaction.setReferenceId(UUID.randomUUID().toString());
        transaction.setStatus(TransactionStatus.COMPLETED);
        // NO establecer transactionDate para permitir que @PrePersist calcule los puntos
        transaction.setSourceWallet(sourceWallet);
        transaction.setTargetWallet(targetWallet);

        // Guardar la transacción (esto ejecutará @PrePersist y calculará los puntos)
        Transaction savedTransaction = transactionRepository.save(transaction);

        // Actualizar saldos
        sourceWallet.updateBalance(amount.negate());
        targetWallet.updateBalance(convertedAmount);

        // Actualizar caché
        IMap<String, BigDecimal> walletBalances = hazelcastInstance.getMap("walletBalances");
        walletBalances.put("wallet:" + sourceWalletId, sourceWallet.getBalance());
        walletBalances.put("wallet:" + targetWalletId, targetWallet.getBalance());

        // Guardar cambios
        walletRepository.save(sourceWallet);
        walletRepository.save(targetWallet);

        // Actualizar puntos del usuario que envía la transferencia
        if (savedTransaction.getPointsEarned() > 0) {
            pointsAccountService.addPoints(
                    userId,
                    savedTransaction.getPointsEarned(),
                    "Puntos ganados por transferencia entre monederos",
                    savedTransaction.getId());

            // Crear notificación de puntos ganados
            notificationService.createNotification(
                    userId,
                    "Puntos Ganados",
                    "Has ganado " + savedTransaction.getPointsEarned() + " puntos por tu transferencia",
                    NotificationType.POINTS_EARNED,
                    savedTransaction.getId(),
                    Notification.RelatedEntityType.TRANSACTION);
        }

        logger.info("Transferencia completada: {} de {} a {}",
                amount, sourceWallet.getName(), targetWallet.getName());

        return sourceWallet;
    }

    @Override
    public boolean hasWalletSufficientBalance(Long walletId, BigDecimal amount) {
        // Primero intentar verificar en Hazelcast
        boolean hasSufficientBalance = transactionManager.hasWalletSufficientBalance(walletId, amount);

        if (hasSufficientBalance) {
            return true;
        }

        // Si no está en caché o no hay saldo suficiente, verificar en la base de datos
        Wallet wallet = walletRepository.findById(walletId)
                .orElseThrow(() -> new EntityNotFoundException("Monedero no encontrado"));

        boolean result = wallet.hasSufficientBalance(amount);

        // Actualizar la caché
        if (result) {
            IMap<String, BigDecimal> walletBalances = hazelcastInstance.getMap("walletBalances");
            walletBalances.put("wallet:" + walletId, wallet.getBalance());
        }

        return result;
    }

    @Override
    @Transactional
    public Wallet saveWallet(Wallet wallet) {
        return walletRepository.save(wallet);
    }

    @Override
    public WalletResponse convertToWalletResponse(Wallet wallet) {
        WalletResponse response = new WalletResponse();
        response.setId(wallet.getId());
        response.setName(wallet.getName());
        response.setDescription(wallet.getDescription());
        response.setBalance(wallet.getBalance());
        response.setWalletType(wallet.getWalletType());
        response.setCreatedAt(wallet.getCreatedAt());
        response.setUpdatedAt(wallet.getUpdatedAt());

        // Incluir información básica del usuario
        if (wallet.getUser() != null) {
            WalletResponse.UserBasicInfo userInfo = new WalletResponse.UserBasicInfo();
            userInfo.setId(wallet.getUser().getId());
            userInfo.setUsername(wallet.getUser().getUsername());
            response.setUser(userInfo);
        }

        // Incluir información de la moneda si existe
        if (wallet.getCurrency() != null) {
            CurrencyResponse currencyResponse = currencyService.convertToCurrencyResponse(wallet.getCurrency());
            response.setCurrency(currencyResponse);
        }

        // Calcular flujos mensuales
        calculateMonthlyFlows(wallet, response);

        return response;
    }

    @Override
    @Transactional
    public WalletResponse updateWallet(Long walletId, WalletRequest walletRequest, Long userId) {
        Wallet wallet = walletRepository.findById(walletId)
                .orElseThrow(() -> new EntityNotFoundException("Monedero no encontrado"));
        if (!wallet.getUser().getId().equals(userId)) {
            throw new AccessDeniedException("No tienes permiso para editar este monedero");
        }
        wallet.setName(walletRequest.getName());
        wallet.setDescription(walletRequest.getDescription());
        wallet.setWalletType(walletRequest.getWalletType());
        Wallet updatedWallet = walletRepository.save(wallet);
        return convertToWalletResponse(updatedWallet);
    }

    @Override
    @Transactional
    public Wallet transferToUser(Long sourceWalletId, Long targetUserId, BigDecimal amount, Long userId) {
        Wallet sourceWallet = walletRepository.findById(sourceWalletId)
                .orElseThrow(() -> new EntityNotFoundException("Monedero origen no encontrado"));
        if (!sourceWallet.getUser().getId().equals(userId)) {
            throw new AccessDeniedException("No tienes permiso para transferir desde este monedero");
        }
        // Buscar el monedero PRIMARY del usuario destino
        List<Wallet> targetWallets = walletRepository.findByUserId(targetUserId);
        Wallet targetWallet = targetWallets.stream()
            .filter(w -> w.getWalletType() == WalletType.PRIMARY)
            .findFirst()
            .orElseThrow(() -> new EntityNotFoundException("El usuario destino no tiene un monedero principal (PRIMARY)"));
        // Verificar saldo suficiente
        if (!hasWalletSufficientBalance(sourceWalletId, amount)) {
            throw new IllegalStateException("Saldo insuficiente en el monedero origen");
        }
        // Si las monedas son diferentes, convertir el monto
        BigDecimal convertedAmount = amount;
        if (sourceWallet.getCurrency() != null && targetWallet.getCurrency() != null &&
                !sourceWallet.getCurrency().getCode().equals(targetWallet.getCurrency().getCode())) {
            convertedAmount = currencyService.convertCurrency(
                    amount,
                    sourceWallet.getCurrency().getCode(),
                    targetWallet.getCurrency().getCode());
            logger.info("Conversión de moneda: {} {} a {} {}",
                    amount, sourceWallet.getCurrency().getCode(),
                    convertedAmount, targetWallet.getCurrency().getCode());
        }

        // Crear la transacción para registrar la transferencia
        Transaction transaction = new Transaction();
        transaction.setType(TransactionType.TRANSFER);
        transaction.setAmount(amount);
        transaction.setDescription("Transferencia externa a usuario " + targetWallet.getUser().getUsername());
        transaction.setReferenceId(UUID.randomUUID().toString());
        transaction.setStatus(TransactionStatus.COMPLETED);
        // NO establecer transactionDate para permitir que @PrePersist calcule los puntos
        transaction.setSourceWallet(sourceWallet);
        transaction.setTargetWallet(targetWallet);

        // Guardar la transacción (esto ejecutará @PrePersist y calculará los puntos)
        Transaction savedTransaction = transactionRepository.save(transaction);

        // Actualizar saldos
        sourceWallet.updateBalance(amount.negate());
        targetWallet.updateBalance(convertedAmount);
        // Actualizar caché
        IMap<String, BigDecimal> walletBalances = hazelcastInstance.getMap("walletBalances");
        walletBalances.put("wallet:" + sourceWalletId, sourceWallet.getBalance());
        walletBalances.put("wallet:" + targetWallet.getId(), targetWallet.getBalance());
        // Guardar cambios
        walletRepository.save(sourceWallet);
        walletRepository.save(targetWallet);

        // Actualizar puntos del usuario que envía la transferencia
        if (savedTransaction.getPointsEarned() > 0) {
            pointsAccountService.addPoints(
                    userId,
                    savedTransaction.getPointsEarned(),
                    "Puntos ganados por transferencia externa",
                    savedTransaction.getId());

            // Crear notificación de puntos ganados
            notificationService.createNotification(
                    userId,
                    "Puntos Ganados",
                    "Has ganado " + savedTransaction.getPointsEarned() + " puntos por tu transferencia",
                    NotificationType.POINTS_EARNED,
                    savedTransaction.getId(),
                    Notification.RelatedEntityType.TRANSACTION);
        }

        // Crear notificaciones para ambos usuarios
        notificationService.createNotification(
                userId,
                "Transferencia Enviada",
                "Has enviado $" + amount + " desde tu monedero " + sourceWallet.getName() +
                " al monedero " + targetWallet.getName() + " de " + targetWallet.getUser().getUsername(),
                NotificationType.TRANSACTION_COMPLETED,
                savedTransaction.getId(),
                Notification.RelatedEntityType.TRANSACTION);

        notificationService.createNotification(
                targetUserId,
                "Transferencia Recibida",
                "Has recibido $" + amount + " en tu monedero " + targetWallet.getName() +
                " de " + sourceWallet.getUser().getUsername(),
                NotificationType.TRANSACTION_COMPLETED,
                savedTransaction.getId(),
                Notification.RelatedEntityType.TRANSACTION);

        logger.info("Transferencia a usuario completada: {} de {} a usuario {} (wallet {})",
                amount, sourceWallet.getName(), targetUserId, targetWallet.getId());
        return sourceWallet;
    }

    /**
     * Calcula los flujos mensuales (ingresos y gastos) de un monedero
     * basándose en las transacciones del mes actual
     */
    private void calculateMonthlyFlows(Wallet wallet, WalletResponse response) {
        // Obtener el primer día del mes actual
        LocalDateTime startOfMonth = LocalDateTime.now().withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0).withNano(0);
        LocalDateTime endOfMonth = startOfMonth.plusMonths(1).minusNanos(1);

        // Obtener todas las transacciones del monedero en el mes actual
        List<Transaction> monthlyTransactions = transactionRepository.findByWalletIdAndDateRange(
                wallet.getId(), startOfMonth, endOfMonth);

        BigDecimal monthlyInflow = BigDecimal.ZERO;
        BigDecimal monthlyOutflow = BigDecimal.ZERO;

        for (Transaction transaction : monthlyTransactions) {
            // Si el monedero es el destino de la transacción, es un ingreso
            if (transaction.getTargetWallet() != null &&
                transaction.getTargetWallet().getId().equals(wallet.getId())) {
                monthlyInflow = monthlyInflow.add(transaction.getAmount());
            }
            // Si el monedero es el origen de la transacción, es un gasto
            else if (transaction.getSourceWallet() != null &&
                     transaction.getSourceWallet().getId().equals(wallet.getId())) {
                monthlyOutflow = monthlyOutflow.add(transaction.getAmount());
            }
        }

        response.setMonthlyInflow(monthlyInflow);
        response.setMonthlyOutflow(monthlyOutflow);

        logger.debug("Flujos mensuales calculados para monedero {}: Ingresos={}, Gastos={}",
                wallet.getId(), monthlyInflow, monthlyOutflow);
    }
}

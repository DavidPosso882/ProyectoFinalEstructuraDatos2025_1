import React, { useState, useEffect } from 'react';
import {
  Container,
  Grid,
  Typography,
  Box,
  Button,
  useTheme,
  useMediaQuery,
  Paper,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  FormControl,
  InputLabel,
  Select,
  MenuItem,
  TextField,
  Alert,
  CircularProgress,
  Tabs,
  Tab
} from '@mui/material';
import {
  Add as AddIcon,
} from '@mui/icons-material';
import { useDispatch, useSelector } from 'react-redux';
import { AppDispatch, RootState } from '../store';
import { UserRank } from '../types/points.types';
import { TransactionType, TransactionStatus, Transaction } from '../types/transaction.types';
import WalletService from '../services/wallet.service';
import { fetchTransactions } from '../store/slices/transactionSlice';
import { fetchUnreadNotifications } from '../store/slices/notificationSlice';
import { fetchPointsAccount } from '../store/slices/pointsSlice';
import TransactionService from '../services/transaction.service';
import {
  filterDepositTransactions,
  filterWithdrawalTransactions,
  filterTransferTransactions
} from '../utils/transactionUtils';

// Importar componentes modulares
import FinancialStats from '../components/dashboard/FinancialStats';
import PointsSystem from '../components/dashboard/PointsSystem';
import TransactionsList from '../components/dashboard/TransactionsList';
import WalletsSummary from '../components/dashboard/WalletsSummary';
import UpcomingScheduledTransactions from '../components/dashboard/UpcomingScheduledTransactions';

// Tipos para los monederos
type WalletType = 'PRIMARY' | 'SAVINGS' | 'EXPENSES' | 'INVESTMENT' | 'CUSTOM';

interface Wallet {
  id: number;
  name: string;
  balance: number;
  type: WalletType;
  description: string;
  isDefault?: boolean;
  isFavorite?: boolean;
  monthlyInflow?: number;
  monthlyOutflow?: number;
}

const Dashboard: React.FC = () => {
  const theme = useTheme();
  const isMobile = useMediaQuery(theme.breakpoints.down('sm'));
  const isMedium = useMediaQuery(theme.breakpoints.down('md'));
  const { user } = useSelector((state: RootState) => state.auth);
  const dispatch = useDispatch<AppDispatch>();
  const { transactions } = useSelector((state: RootState) => state.transaction);
  const { pointsAccount } = useSelector((state: RootState) => state.points);

  // Estados para datos simulados
  const [wallets, setWallets] = useState<Wallet[]>([]);

  // Función para calcular puntos necesarios para el siguiente rango
  const getPointsToNextRank = (currentRank: UserRank, currentPoints: number): number => {
    const RANK_THRESHOLDS = {
      [UserRank.BRONZE]: 0,
      [UserRank.SILVER]: 500,
      [UserRank.GOLD]: 1000,
      [UserRank.PLATINUM]: 5000,
      [UserRank.DIAMOND]: 50000
    };

    const getNextRank = (rank: UserRank): UserRank => {
      switch (rank) {
        case UserRank.BRONZE: return UserRank.SILVER;
        case UserRank.SILVER: return UserRank.GOLD;
        case UserRank.GOLD: return UserRank.PLATINUM;
        case UserRank.PLATINUM: return UserRank.DIAMOND;
        default: return UserRank.DIAMOND;
      }
    };

    const nextRank = getNextRank(currentRank);
    if (nextRank === currentRank) return 0; // Ya está en el rango máximo

    const nextThreshold = RANK_THRESHOLDS[nextRank];
    return Math.max(0, nextThreshold - currentPoints);
  };

  // Calcular datos financieros reales basándose en los monederos
  const calculateFinancialSummary = () => {
    const totalBalance = wallets.reduce((sum, wallet) => sum + wallet.balance, 0);
    const totalIncome = wallets.reduce((sum, wallet) => sum + (wallet.monthlyInflow || 0), 0);
    const totalExpenses = wallets.reduce((sum, wallet) => sum + (wallet.monthlyOutflow || 0), 0);

    // Calcular ratio real: (gastos / ingresos) * 100
    const expenseRatio = totalIncome > 0 ? Math.round((totalExpenses / totalIncome) * 100) : 0;

    return {
      totalBalance,
      totalIncome,
      totalExpenses,
      expenseRatio
    };
  };

  const financialSummary = calculateFinancialSummary();

  // Estados para diálogo de transacción inmediata
  const [openTxDialog, setOpenTxDialog] = useState(false);
  const [txType, setTxType] = useState<'DEPOSIT' | 'WITHDRAWAL' | 'TRANSFER'>('DEPOSIT');
  const [txAmount, setTxAmount] = useState('');
  const [txSourceId, setTxSourceId] = useState('');
  const [txTargetId, setTxTargetId] = useState('');
  const [txError, setTxError] = useState<string | null>(null);
  const [isTxLoading, setIsTxLoading] = useState(false);
  const [txSuccess, setTxSuccess] = useState(false);

  const [tabValue, setTabValue] = useState(0);

  // Función para filtrar transacciones según el tab seleccionado
  const getFilteredTransactions = () => {
    if (!user) return [];

    switch (tabValue) {
      case 0: // Todas
        return transactions;
      case 1: // Depósitos (cualquier ingreso de dinero)
        return filterDepositTransactions(transactions, user.id);
      case 2: // Retiros (cualquier salida de dinero)
        return filterWithdrawalTransactions(transactions, user.id);
      case 3: // Transferencias (solo transferencias locales)
        return filterTransferTransactions(transactions, user.id);
      default:
        return transactions;
    }
  };

  const handleOpenTxDialog = () => {
    setTxType('DEPOSIT');
    setTxAmount('');
    setTxSourceId(wallets.length > 0 ? String(wallets[0].id) : '');
    setTxTargetId('');
    setTxError(null);
    setOpenTxDialog(true);
  };
  const handleCloseTxDialog = () => {
    setOpenTxDialog(false);
    setTxError(null);
  };
  const handleTxSubmit = async () => {
    const amount = parseFloat(txAmount);
    const sourceId = Number(txSourceId);
    const targetUserId = Number(txTargetId);
    if (!sourceId || isNaN(amount) || amount <= 0 || (txType === 'TRANSFER' && (!targetUserId || targetUserId === user?.id))) {
      setTxError('Completa todos los campos correctamente');
      return;
    }
    try {
      setIsTxLoading(true);
      setTxError(null);
      if (txType === 'TRANSFER') {
        // Usar el endpoint de WalletService para transferencias por usuario
        await WalletService.transferToUser(sourceId, targetUserId, amount);
      } else {
        let req: any = { type: txType, amount, description: `Transacción inmediata (${txType})` };
        if (txType === 'DEPOSIT') req.targetWalletId = sourceId;
        if (txType === 'WITHDRAWAL') req.sourceWalletId = sourceId;
        await TransactionService.processTransaction(req);
      }
      // Refresca monederos y transacciones dos veces para asegurar actualización
      const updatedWallets = await WalletService.getUserWallets();
      setWallets(updatedWallets);
      await new Promise(res => setTimeout(res, 500)); // Espera breve
      const refreshedWallets = await WalletService.getUserWallets();
      setWallets(refreshedWallets);
      dispatch(fetchTransactions({}));
      // Recargar notificaciones para actualizar el contador
      dispatch(fetchUnreadNotifications());
      // Recargar puntos para reflejar los puntos ganados por la transacción
      dispatch(fetchPointsAccount());
      setTxSuccess(true);
      setTimeout(() => setTxSuccess(false), 2000);
      setOpenTxDialog(false);
    } catch (e: any) {
      setTxError(e.message || 'Error al procesar la transacción');
    } finally {
      setIsTxLoading(false);
    }
  };

  useEffect(() => {
    WalletService.getUserWallets().then(setWallets);
    dispatch(fetchTransactions({}));
    dispatch(fetchPointsAccount());
  }, [dispatch]);

  return (
    <Container maxWidth="xl" sx={{ mt: 4, mb: 4 }}>
      {/* Encabezado */}
      <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 4 }}>
        <Box>
          <Typography variant="h4" component="h1" fontWeight="bold">
            Dashboard
          </Typography>
          {/* Mostrar el número de cuenta */}
          {user && (
            <Typography variant="subtitle2" color="textSecondary" sx={{ mt: 1 }}>
              Número de cuenta: <b>{user.id}</b>
            </Typography>
          )}
        </Box>
        <Button
          variant="contained"
          startIcon={<AddIcon />}
          sx={{
            borderRadius: 2,
            textTransform: 'none',
            px: 3
          }}
          onClick={handleOpenTxDialog}
        >
          Nueva Transacción
        </Button>
      </Box>

      {/* Estadísticas Financieras */}
      <Box sx={{ mb: 4 }}>
        <FinancialStats
          totalBalance={financialSummary.totalBalance}
          expenseRatio={financialSummary.expenseRatio}
        />
      </Box>

      {/* Monederos */}
      <Grid container spacing={4} sx={{ mb: 4 }}>
        <Grid item xs={12}>
          <Paper
            elevation={0}
            sx={{
              p: 3,
              borderRadius: 3,
              bgcolor: theme.palette.background.paper,
              boxShadow: theme.shadows[2]
            }}
          >
            <WalletsSummary wallets={wallets} />
          </Paper>
        </Grid>
      </Grid>

      {/* Sistema de Puntos, Transacciones y Transacciones Programadas */}
      <Grid container spacing={4} sx={{ mb: 4 }}>
        {/* Sistema de Puntos */}
        <Grid item xs={12} md={4}>
          <Paper
            elevation={0}
            sx={{
              p: 3,
              borderRadius: 3,
              bgcolor: theme.palette.background.paper,
              boxShadow: theme.shadows[2],
              height: '100%'
            }}
          >
            <PointsSystem
              currentPoints={pointsAccount?.availablePoints || 0}
              currentRank={pointsAccount?.currentRank || UserRank.BRONZE}
              pointsToNextRank={getPointsToNextRank(
                pointsAccount?.currentRank || UserRank.BRONZE,
                pointsAccount?.totalPoints || 0
              )}
            />
          </Paper>
        </Grid>

        {/* Transacciones */}
        <Grid item xs={12} md={4}>
          <Paper
            elevation={0}
            sx={{
              p: 3,
              borderRadius: 3,
              bgcolor: theme.palette.background.paper,
              boxShadow: theme.shadows[2],
              height: '100%'
            }}
          >
            {/* Tabs de clasificación de transacciones */}
            <Box sx={{ borderBottom: 1, borderColor: 'divider', mb: 2 }}>
              <Tabs
                value={tabValue}
                onChange={(_e, newValue) => setTabValue(newValue)}
                indicatorColor="primary"
                textColor="primary"
                variant="scrollable"
                scrollButtons="auto"
              >
                <Tab label="Todas" />
                <Tab label="Depósitos" />
                <Tab label="Retiros" />
                <Tab label="Transferencias" />
              </Tabs>
            </Box>
            <TransactionsList transactions={getFilteredTransactions()} />
          </Paper>
        </Grid>

        {/* Transacciones Programadas */}
        <Grid item xs={12} md={4}>
          <UpcomingScheduledTransactions />
        </Grid>
      </Grid>

      {/* Diálogo para transacción inmediata */}
      <Dialog open={openTxDialog} onClose={handleCloseTxDialog} fullWidth maxWidth="xs">
        <DialogTitle>Nueva Transacción</DialogTitle>
        <DialogContent>
          <Box sx={{ mt: 1 }}>
            {txSuccess && <Alert severity="success">¡Transacción realizada con éxito!</Alert>}
            <FormControl fullWidth sx={{ mb: 2 }}>
              <InputLabel>Monedero Origen</InputLabel>
              <Select
                value={txSourceId}
                label="Monedero Origen"
                onChange={e => setTxSourceId(e.target.value)}
              >
                {wallets.map(wallet => (
                  <MenuItem key={wallet.id} value={wallet.id}>{wallet.name} (ID: {wallet.id})</MenuItem>
                ))}
              </Select>
            </FormControl>
            <FormControl fullWidth sx={{ mb: 2 }}>
              <InputLabel>Tipo de Transacción</InputLabel>
              <Select
                value={txType}
                label="Tipo de Transacción"
                onChange={e => setTxType(e.target.value as any)}
              >
                <MenuItem value="DEPOSIT">Depósito</MenuItem>
                <MenuItem value="WITHDRAWAL">Retiro</MenuItem>
                <MenuItem value="TRANSFER">Transferencia</MenuItem>
              </Select>
            </FormControl>
            <TextField
              label="Monto"
              type="text"
              fullWidth
              value={txAmount}
              inputMode="decimal"
              onChange={e => {
                // Solo permitir números y punto decimal
                const value = e.target.value.replace(/[^\d.]/g, '');
                setTxAmount(value);
              }}
              sx={{ mb: 2 }}
            />
            {txType === 'TRANSFER' && (
              <TextField
                label="Número de cuenta destino (ID de usuario)"
                type="text"
                fullWidth
                value={txTargetId}
                inputMode="numeric"
                onChange={e => {
                  // Solo permitir números
                  const value = e.target.value.replace(/\D/g, '');
                  setTxTargetId(value);
                }}
                sx={{ mb: 2 }}
              />
            )}
            {txError && <Alert severity="error">{txError}</Alert>}
          </Box>
        </DialogContent>
        <DialogActions>
          <Button onClick={handleCloseTxDialog}>Cancelar</Button>
          <Button onClick={handleTxSubmit} variant="contained" disabled={isTxLoading}>
            {isTxLoading ? <CircularProgress size={24} /> : 'Realizar'}
          </Button>
        </DialogActions>
      </Dialog>
    </Container>
  );
};

export default Dashboard;

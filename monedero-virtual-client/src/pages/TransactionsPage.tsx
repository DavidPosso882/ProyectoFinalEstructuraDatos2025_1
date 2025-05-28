import React, { useEffect, useState } from 'react';
import {
  Box,
  Typography,
  Paper,
  Grid,
  Button,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  CircularProgress,
  useTheme,
  Tabs,
  Tab,
  IconButton,
  Alert
} from '@mui/material';
import {
  Refresh as RefreshIcon,
  ArrowBack as ArrowBackIcon,
  Receipt as ReceiptIcon
} from '@mui/icons-material';
import { useDispatch, useSelector } from 'react-redux';
import { AppDispatch, RootState } from '../store';
import { fetchTransactions } from '../store/slices/transactionSlice';
import TransactionsList from '../components/TransactionsList';
import { Transaction, TransactionType } from '../types/transaction.types';
import TransactionService from '../services/transaction.service';
import {
  filterDepositTransactions,
  filterWithdrawalTransactions,
  filterTransferTransactions
} from '../utils/transactionUtils';

const TransactionsPage: React.FC = () => {
  const theme = useTheme();
  const dispatch = useDispatch<AppDispatch>();
  const { transactions, loading, error } = useSelector((state: RootState) => state.transaction);
  const { user } = useSelector((state: RootState) => state.auth);

  // Estados locales
  const [tabValue, setTabValue] = useState(0);
  const [selectedTransaction, setSelectedTransaction] = useState<Transaction | null>(null);
  const [detailsOpen, setDetailsOpen] = useState(false);
  const [page, setPage] = useState(0);
  const [pageSize, setPageSize] = useState(10);

  // Cargar transacciones al montar el componente
  useEffect(() => {
    loadTransactions();
  }, [dispatch, page, pageSize]);

  // Función para cargar transacciones
  const loadTransactions = async () => {
    try {
      // Ahora despachamos el thunk con los parámetros de paginación
      dispatch(fetchTransactions({ page, size: pageSize }));
    } catch (error) {
      console.error('Error al cargar transacciones:', error);
    }
  };

  // Manejador para cambiar de pestaña
  const handleTabChange = (event: React.SyntheticEvent, newValue: number) => {
    setTabValue(newValue);
  };

  // Manejador para ver detalles de una transacción
  const handleViewDetails = (transaction: Transaction) => {
    setSelectedTransaction(transaction);
    setDetailsOpen(true);
  };

  // Manejador para cerrar el diálogo de detalles
  const handleCloseDetails = () => {
    setDetailsOpen(false);
  };

  // Filtrar transacciones según la pestaña seleccionada
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

  // Renderizar el diálogo de detalles de transacción
  const renderTransactionDetails = () => {
    if (!selectedTransaction) return null;

    return (
      <Dialog
        open={detailsOpen}
        onClose={handleCloseDetails}
        maxWidth="sm"
        fullWidth
      >
        <DialogTitle sx={{
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'space-between'
        }}>
          <Box sx={{ display: 'flex', alignItems: 'center' }}>
            <IconButton
              edge="start"
              color="inherit"
              onClick={handleCloseDetails}
              aria-label="close"
              sx={{ mr: 1 }}
            >
              <ArrowBackIcon />
            </IconButton>
            <Typography variant="h6">
              Detalles de la Transacción
            </Typography>
          </Box>
        </DialogTitle>
        <DialogContent dividers>
          <Grid container spacing={2}>
            <Grid item xs={12}>
              <Typography variant="subtitle1" fontWeight="bold">
                ID de Transacción
              </Typography>
              <Typography variant="body1">
                {selectedTransaction.id}
              </Typography>
            </Grid>

            <Grid item xs={12} sm={6}>
              <Typography variant="subtitle1" fontWeight="bold">
                Tipo
              </Typography>
              <Typography variant="body1">
                {selectedTransaction.transactionType}
              </Typography>
            </Grid>

            <Grid item xs={12} sm={6}>
              <Typography variant="subtitle1" fontWeight="bold">
                Estado
              </Typography>
              <Typography variant="body1">
                {selectedTransaction.status}
              </Typography>
            </Grid>

            <Grid item xs={12}>
              <Typography variant="subtitle1" fontWeight="bold">
                Monto
              </Typography>
              <Typography variant="body1" fontWeight="bold" color={
                selectedTransaction.transactionType === TransactionType.DEPOSIT ? 'success.main' :
                selectedTransaction.transactionType === TransactionType.WITHDRAWAL ? 'error.main' :
                'text.primary'
              }>
                ${selectedTransaction.amount.toFixed(2)}
              </Typography>
            </Grid>

            <Grid item xs={12}>
              <Typography variant="subtitle1" fontWeight="bold">
                Descripción
              </Typography>
              <Typography variant="body1">
                {selectedTransaction.description || 'Sin descripción'}
              </Typography>
            </Grid>

            <Grid item xs={12} sm={6}>
              <Typography variant="subtitle1" fontWeight="bold">
                Fecha
              </Typography>
              <Typography variant="body1">
                {new Date(selectedTransaction.createdAt).toLocaleString()}
              </Typography>
            </Grid>

            {selectedTransaction.sourceWalletId && (
              <Grid item xs={12} sm={6}>
                <Typography variant="subtitle1" fontWeight="bold">
                  Monedero Origen
                </Typography>
                <Typography variant="body1">
                  {selectedTransaction.sourceWalletId}
                </Typography>
              </Grid>
            )}

            {selectedTransaction.destinationWalletId && (
              <Grid item xs={12} sm={6}>
                <Typography variant="subtitle1" fontWeight="bold">
                  Monedero Destino
                </Typography>
                <Typography variant="body1">
                  {selectedTransaction.destinationWalletId}
                </Typography>
              </Grid>
            )}
          </Grid>
        </DialogContent>
        <DialogActions>
          <Button onClick={handleCloseDetails}>Cerrar</Button>
        </DialogActions>
      </Dialog>
    );
  };

  const handleUndoLast = async () => {
    try {
      const res = await TransactionService.undoLastTransaction();
      alert(res);
      loadTransactions(); // Recargar la lista tras deshacer
    } catch (e) {
      alert('No se pudo deshacer la última transacción');
    }
  };

  return (
    <Box sx={{ py: 3 }}>
      <Grid container spacing={3}>
        <Grid item xs={12}>
          <Box sx={{
            display: 'flex',
            justifyContent: 'space-between',
            alignItems: 'center',
            mb: 3
          }}>
            <Typography variant="h4" component="h1" gutterBottom>
              Transacciones
            </Typography>

            <Box>
              <Button
                variant="outlined"
                startIcon={<RefreshIcon />}
                onClick={loadTransactions}
                sx={{ mr: 1 }}
              >
                Actualizar
              </Button>
              <Button
                variant="contained"
                color="warning"
                onClick={handleUndoLast}
              >
                Deshacer última transacción
              </Button>
            </Box>
          </Box>

          {error && (
            <Alert severity="error" sx={{ mb: 3 }}>
              {error}
            </Alert>
          )}

          <Paper sx={{ mb: 3 }}>
            <Tabs
              value={tabValue}
              onChange={handleTabChange}
              indicatorColor="primary"
              textColor="primary"
              variant="scrollable"
              scrollButtons="auto"
              sx={{ borderBottom: 1, borderColor: 'divider' }}
            >
              <Tab label="Todas" />
              <Tab label="Depósitos" />
              <Tab label="Retiros" />
              <Tab label="Transferencias" />
            </Tabs>
            <Box sx={{ p: 0 }}>
              {loading ? (
                <Box sx={{ display: 'flex', justifyContent: 'center', p: 4 }}>
                  <CircularProgress />
                </Box>
              ) : getFilteredTransactions().length > 0 ? (
                <TransactionsList
                  transactions={getFilteredTransactions()}
                  title=""
                  maxHeight={600}
                  showFilters={false}
                  onViewDetails={handleViewDetails}
                  currentUserId={user?.id}
                />
              ) : (
                <Box sx={{ p: 4, textAlign: 'center' }}>
                  <ReceiptIcon sx={{ fontSize: 60, color: 'text.secondary', mb: 2 }} />
                  <Typography variant="h6" color="textSecondary">
                    No hay transacciones para mostrar
                  </Typography>
                  <Typography variant="body2" color="textSecondary">
                    Las transacciones que realices aparecerán aquí
                  </Typography>
                </Box>
              )}
            </Box>
          </Paper>
        </Grid>
      </Grid>

      {/* Diálogo de detalles de transacción */}
      {renderTransactionDetails()}
    </Box>
  );
};

export default TransactionsPage;

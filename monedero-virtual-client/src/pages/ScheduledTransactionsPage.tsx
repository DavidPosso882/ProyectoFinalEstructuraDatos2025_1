import React, { useState, useEffect } from 'react';
import {
  Container,
  Typography,
  Box,
  Paper,
  Tabs,
  Tab,
  Divider,
  Button,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  Chip,
  IconButton,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  CircularProgress,
  Alert,
  useTheme,
  alpha
} from '@mui/material';
import {
  Add as AddIcon,
  Delete as DeleteIcon,
  CalendarMonth as CalendarIcon,
  ArrowUpward as UpIcon,
  ArrowDownward as DownIcon,
  SwapHoriz as TransferIcon,
  Refresh as RefreshIcon
} from '@mui/icons-material';
import { format, parseISO, isAfter, isBefore, addDays } from 'date-fns';
import { es } from 'date-fns/locale';

// Servicios y componentes
import ScheduledTransactionService from '../services/scheduledTransaction.service';
import WalletService from '../services/wallet.service';
import ScheduledTransactionForm from '../components/forms/ScheduledTransactionForm';
import { 
  ScheduledTransactionResponse, 
  RecurrenceType, 
  RecurrenceTypeLabels 
} from '../types/scheduledTransaction.types';
import { TransactionType } from '../types/transaction.types';
import { Wallet } from '../types/wallet.types';

// Componente principal
const ScheduledTransactionsPage: React.FC = () => {
  const theme = useTheme();
  
  // Estados
  const [transactions, setTransactions] = useState<ScheduledTransactionResponse[]>([]);
  const [wallets, setWallets] = useState<Wallet[]>([]);
  const [loading, setLoading] = useState<boolean>(true);
  const [error, setError] = useState<string | null>(null);
  const [tabValue, setTabValue] = useState<number>(0);
  const [openCreateDialog, setOpenCreateDialog] = useState<boolean>(false);
  const [transactionToDelete, setTransactionToDelete] = useState<number | null>(null);
  const [refreshTrigger, setRefreshTrigger] = useState<number>(0);
  
  // Cargar datos
  useEffect(() => {
    const loadData = async () => {
      try {
        setLoading(true);
        
        // Cargar monederos
        const walletsData = await WalletService.getUserWallets();
        setWallets(walletsData);
        
        // Cargar transacciones programadas
        const transactionsData = await ScheduledTransactionService.getUserScheduledTransactions();
        setTransactions(transactionsData);
        
        setError(null);
      } catch (err) {
        console.error('Error al cargar datos:', err);
        setError('No se pudieron cargar los datos. Por favor, intente nuevamente.');
      } finally {
        setLoading(false);
      }
    };
    
    loadData();
  }, [refreshTrigger]);
  
  // Manejador de cambio de tab
  const handleTabChange = (_: React.SyntheticEvent, newValue: number) => {
    setTabValue(newValue);
  };
  
  // Manejador para abrir diálogo de creación
  const handleOpenCreateDialog = () => {
    setOpenCreateDialog(true);
  };
  
  // Manejador para cerrar diálogo de creación
  const handleCloseCreateDialog = () => {
    setOpenCreateDialog(false);
  };
  
  // Manejador de éxito al crear transacción programada
  const handleCreateSuccess = () => {
    setOpenCreateDialog(false);
    setRefreshTrigger(prev => prev + 1); // Refresca la lista
  };
  
  // Manejador para confirmar eliminación
  const handleConfirmDelete = async () => {
    if (transactionToDelete !== null) {
      try {
        await ScheduledTransactionService.cancelScheduledTransaction(transactionToDelete);
        setTransactions(transactions.filter(t => t.id !== transactionToDelete));
        setTransactionToDelete(null);
      } catch (err) {
        console.error('Error al cancelar transacción programada:', err);
        setError('No se pudo cancelar la transacción programada');
      }
    }
  };
  
  // Función para obtener el ícono según el tipo de transacción
  const getTransactionIcon = (type: TransactionType) => {
    switch (type) {
      case TransactionType.DEPOSIT:
        return <DownIcon sx={{ color: theme.palette.success.main }} />;
      case TransactionType.WITHDRAWAL:
        return <UpIcon sx={{ color: theme.palette.error.main }} />;
      case TransactionType.TRANSFER:
        return <TransferIcon sx={{ color: theme.palette.info.main }} />;
      default:
        return <CalendarIcon sx={{ color: theme.palette.primary.main }} />;
    }
  };
  
  // Función para obtener el texto según el tipo de transacción
  const getTransactionTypeText = (type: TransactionType) => {
    switch (type) {
      case TransactionType.DEPOSIT:
        return 'Depósito';
      case TransactionType.WITHDRAWAL:
        return 'Retiro';
      case TransactionType.TRANSFER:
        return 'Transferencia';
      default:
        return 'Transacción';
    }
  };
  
  // Función para obtener el color de fondo según la proximidad de la fecha
  const getDateColor = (dateStr: string) => {
    const date = parseISO(dateStr);
    const today = new Date();
    const tomorrow = addDays(today, 1);
    
    if (isBefore(date, today)) {
      return theme.palette.error.light;
    } else if (isBefore(date, tomorrow)) {
      return theme.palette.warning.light;
    } else {
      return theme.palette.success.light;
    }
  };
  
  // Filtrar transacciones según la pestaña seleccionada
  const filteredTransactions = transactions.filter(transaction => {
    if (tabValue === 0) {
      // Todas
      return true;
    } else if (tabValue === 1) {
      // Pendientes
      return !transaction.executed;
    } else if (tabValue === 2) {
      // Ejecutadas
      return transaction.executed;
    }
    return true;
  });
  
  return (
    <Container maxWidth="lg" sx={{ mt: 4, mb: 4 }}>
      <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 3 }}>
        <Typography variant="h4">
          Transacciones Programadas
        </Typography>
        <Box>
          <Button
            variant="outlined"
            startIcon={<RefreshIcon />}
            onClick={() => setRefreshTrigger(prev => prev + 1)}
            sx={{ mr: 1 }}
          >
            Actualizar
          </Button>
          <Button
            variant="contained"
            startIcon={<AddIcon />}
            onClick={handleOpenCreateDialog}
          >
            Programar Nueva
          </Button>
        </Box>
      </Box>
      
      <Paper sx={{ mb: 3 }}>
        <Tabs
          value={tabValue}
          onChange={handleTabChange}
          indicatorColor="primary"
          textColor="primary"
          variant="fullWidth"
        >
          <Tab label="Todas" />
          <Tab label="Pendientes" />
          <Tab label="Ejecutadas" />
        </Tabs>
      </Paper>
      
      {loading ? (
        <Box sx={{ display: 'flex', justifyContent: 'center', py: 4 }}>
          <CircularProgress />
        </Box>
      ) : error ? (
        <Alert severity="error" sx={{ mb: 3 }}>
          {error}
        </Alert>
      ) : filteredTransactions.length === 0 ? (
        <Paper sx={{ p: 4, textAlign: 'center' }}>
          <CalendarIcon sx={{ fontSize: 60, color: alpha(theme.palette.text.secondary, 0.5), mb: 2 }} />
          <Typography variant="h6" gutterBottom>
            No hay transacciones programadas
          </Typography>
          <Typography variant="body2" color="textSecondary" paragraph>
            Programa tus pagos y transferencias para que se ejecuten automáticamente.
          </Typography>
          <Button
            variant="contained"
            startIcon={<AddIcon />}
            onClick={handleOpenCreateDialog}
            sx={{ mt: 2 }}
          >
            Programar Nueva Transacción
          </Button>
        </Paper>
      ) : (
        <TableContainer component={Paper}>
          <Table>
            <TableHead>
              <TableRow>
                <TableCell>Tipo</TableCell>
                <TableCell>Descripción</TableCell>
                <TableCell>Monto</TableCell>
                <TableCell>Fecha Programada</TableCell>
                <TableCell>Recurrencia</TableCell>
                <TableCell>Estado</TableCell>
                <TableCell>Acciones</TableCell>
              </TableRow>
            </TableHead>
            <TableBody>
              {filteredTransactions.map((transaction) => (
                <TableRow key={transaction.id}>
                  <TableCell>
                    <Box sx={{ display: 'flex', alignItems: 'center' }}>
                      {getTransactionIcon(transaction.type)}
                      <Typography variant="body2" sx={{ ml: 1 }}>
                        {getTransactionTypeText(transaction.type)}
                      </Typography>
                    </Box>
                  </TableCell>
                  <TableCell>
                    {transaction.description || 
                      `${getTransactionTypeText(transaction.type)} desde ${transaction.wallet.name}`}
                  </TableCell>
                  <TableCell>
                    <Typography 
                      variant="body2" 
                      sx={{ 
                        fontWeight: 'bold',
                        color: transaction.type === TransactionType.DEPOSIT 
                          ? theme.palette.success.main 
                          : theme.palette.error.main
                      }}
                    >
                      ${transaction.amount.toFixed(2)}
                    </Typography>
                  </TableCell>
                  <TableCell>
                    <Chip
                      label={format(parseISO(transaction.scheduledDate), 'dd MMM yyyy HH:mm', { locale: es })}
                      size="small"
                      sx={{ 
                        bgcolor: transaction.executed 
                          ? alpha(theme.palette.info.main, 0.1)
                          : getDateColor(transaction.scheduledDate)
                      }}
                    />
                  </TableCell>
                  <TableCell>
                    {transaction.recurrenceType ? (
                      <Chip
                        label={RecurrenceTypeLabels[transaction.recurrenceType]}
                        size="small"
                        sx={{ 
                          bgcolor: alpha(theme.palette.primary.main, 0.1)
                        }}
                      />
                    ) : (
                      <Typography variant="body2" color="textSecondary">
                        Una vez
                      </Typography>
                    )}
                  </TableCell>
                  <TableCell>
                    <Chip
                      label={transaction.executed ? 'Ejecutada' : 'Pendiente'}
                      color={transaction.executed ? 'success' : 'warning'}
                      size="small"
                    />
                  </TableCell>
                  <TableCell>
                    {!transaction.executed && (
                      <IconButton
                        size="small"
                        color="error"
                        onClick={() => setTransactionToDelete(transaction.id)}
                      >
                        <DeleteIcon />
                      </IconButton>
                    )}
                  </TableCell>
                </TableRow>
              ))}
            </TableBody>
          </Table>
        </TableContainer>
      )}
      
      {/* Diálogo para crear transacción programada */}
      <Dialog
        open={openCreateDialog}
        onClose={handleCloseCreateDialog}
        maxWidth="md"
        fullWidth
      >
        <DialogContent>
          <ScheduledTransactionForm
            wallets={wallets}
            onSuccess={handleCreateSuccess}
            onCancel={handleCloseCreateDialog}
          />
        </DialogContent>
      </Dialog>
      
      {/* Diálogo para confirmar eliminación */}
      <Dialog
        open={transactionToDelete !== null}
        onClose={() => setTransactionToDelete(null)}
      >
        <DialogTitle>Confirmar Cancelación</DialogTitle>
        <DialogContent>
          <Typography>
            ¿Está seguro de que desea cancelar esta transacción programada?
          </Typography>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setTransactionToDelete(null)}>
            No, Mantener
          </Button>
          <Button onClick={handleConfirmDelete} color="error" variant="contained">
            Sí, Cancelar
          </Button>
        </DialogActions>
      </Dialog>
    </Container>
  );
};

export default ScheduledTransactionsPage;

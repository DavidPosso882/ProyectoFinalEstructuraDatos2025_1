import React, { useState, useEffect } from 'react';
import {
  Paper,
  Typography,
  Divider,
  Box,
  List,
  ListItem,
  ListItemText,
  ListItemIcon,
  ListItemSecondaryAction,
  IconButton,
  Chip,
  Tooltip,
  CircularProgress,
  Button,
  useTheme,
  alpha
} from '@mui/material';
import {
  CalendarMonth as CalendarIcon,
  ArrowUpward as UpIcon,
  ArrowDownward as DownIcon,
  SwapHoriz as TransferIcon,
  Delete as DeleteIcon,
  Add as AddIcon
} from '@mui/icons-material';
import { format, parseISO, isAfter, isBefore, addDays } from 'date-fns';
import { es } from 'date-fns/locale';
import { useNavigate } from 'react-router-dom';

// Servicios y tipos
import ScheduledTransactionService from '../../services/scheduledTransaction.service';
import { 
  ScheduledTransactionResponse, 
  RecurrenceType, 
  RecurrenceTypeLabels 
} from '../../types/scheduledTransaction.types';
import { TransactionType } from '../../types/transaction.types';

// Componente para mostrar las próximas transacciones programadas
const UpcomingScheduledTransactions: React.FC = () => {
  const theme = useTheme();
  const navigate = useNavigate();
  
  // Estados
  const [transactions, setTransactions] = useState<ScheduledTransactionResponse[]>([]);
  const [loading, setLoading] = useState<boolean>(true);
  const [error, setError] = useState<string | null>(null);
  
  // Cargar transacciones programadas
  useEffect(() => {
    const loadTransactions = async () => {
      try {
        setLoading(true);
        const data = await ScheduledTransactionService.getUpcomingScheduledTransactions(5);
        setTransactions(data);
        setError(null);
      } catch (err) {
        console.error('Error al cargar transacciones programadas:', err);
        setError('No se pudieron cargar las transacciones programadas');
      } finally {
        setLoading(false);
      }
    };
    
    loadTransactions();
  }, []);
  
  // Función para cancelar una transacción programada
  const handleCancelTransaction = async (id: number) => {
    try {
      await ScheduledTransactionService.cancelScheduledTransaction(id);
      // Actualizar la lista de transacciones
      setTransactions(transactions.filter(t => t.id !== id));
    } catch (err) {
      console.error('Error al cancelar transacción programada:', err);
      setError('No se pudo cancelar la transacción programada');
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
  
  // Función para navegar a la página de creación de transacciones programadas
  const handleCreateTransaction = () => {
    navigate('/scheduled-transactions/create');
  };
  
  // Función para navegar a la página de todas las transacciones programadas
  const handleViewAllTransactions = () => {
    navigate('/scheduled-transactions');
  };
  
  return (
    <Paper elevation={3} sx={{ p: 2, height: '100%', display: 'flex', flexDirection: 'column' }}>
      <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 1 }}>
        <Typography variant="h6">
          Próximos Pagos Programados
        </Typography>
        <Tooltip title="Programar nueva transacción">
          <IconButton 
            size="small" 
            color="primary" 
            onClick={handleCreateTransaction}
            sx={{ 
              bgcolor: alpha(theme.palette.primary.main, 0.1),
              '&:hover': {
                bgcolor: alpha(theme.palette.primary.main, 0.2),
              }
            }}
          >
            <AddIcon />
          </IconButton>
        </Tooltip>
      </Box>
      <Divider sx={{ mb: 2 }} />
      
      {loading ? (
        <Box sx={{ display: 'flex', justifyContent: 'center', alignItems: 'center', flexGrow: 1 }}>
          <CircularProgress size={40} />
        </Box>
      ) : error ? (
        <Box sx={{ textAlign: 'center', color: 'text.secondary', py: 2, flexGrow: 1 }}>
          <Typography variant="body2" color="error">
            {error}
          </Typography>
        </Box>
      ) : transactions.length === 0 ? (
        <Box sx={{ 
          textAlign: 'center', 
          color: 'text.secondary', 
          py: 4, 
          display: 'flex', 
          flexDirection: 'column',
          justifyContent: 'center',
          alignItems: 'center',
          flexGrow: 1
        }}>
          <CalendarIcon sx={{ fontSize: 40, color: alpha(theme.palette.text.secondary, 0.5), mb: 1 }} />
          <Typography variant="body2" gutterBottom>
            No tienes pagos programados próximos
          </Typography>
          <Button 
            variant="outlined" 
            size="small" 
            startIcon={<AddIcon />}
            onClick={handleCreateTransaction}
            sx={{ mt: 1 }}
          >
            Programar pago
          </Button>
        </Box>
      ) : (
        <>
          <List sx={{ flexGrow: 1, mb: 1 }}>
            {transactions.map((transaction) => (
              <ListItem
                key={transaction.id}
                sx={{ 
                  mb: 1, 
                  borderRadius: 1,
                  bgcolor: alpha(theme.palette.background.default, 0.5),
                  '&:hover': {
                    bgcolor: alpha(theme.palette.background.default, 0.8),
                  }
                }}
              >
                <ListItemIcon>
                  {getTransactionIcon(transaction.type)}
                </ListItemIcon>
                <ListItemText
                  primary={
                    <Typography variant="body2" sx={{ fontWeight: 'medium' }}>
                      {transaction.description || `${getTransactionTypeText(transaction.type)} programado`}
                    </Typography>
                  }
                  secondary={
                    <Box sx={{ display: 'flex', alignItems: 'center', flexWrap: 'wrap', gap: 0.5, mt: 0.5 }}>
                      <Chip
                        label={format(parseISO(transaction.scheduledDate), 'dd MMM yyyy', { locale: es })}
                        size="small"
                        sx={{ 
                          bgcolor: getDateColor(transaction.scheduledDate),
                          fontSize: '0.7rem',
                          height: 20
                        }}
                      />
                      {transaction.recurrenceType && (
                        <Chip
                          label={RecurrenceTypeLabels[transaction.recurrenceType]}
                          size="small"
                          sx={{ 
                            bgcolor: alpha(theme.palette.primary.main, 0.1),
                            fontSize: '0.7rem',
                            height: 20
                          }}
                        />
                      )}
                      <Typography variant="body2" sx={{ fontWeight: 'bold', ml: 'auto' }}>
                        ${transaction.amount.toFixed(2)}
                      </Typography>
                    </Box>
                  }
                />
                <ListItemSecondaryAction>
                  <Tooltip title="Cancelar transacción">
                    <IconButton 
                      edge="end" 
                      size="small"
                      onClick={() => handleCancelTransaction(transaction.id)}
                      sx={{ color: theme.palette.error.main }}
                    >
                      <DeleteIcon fontSize="small" />
                    </IconButton>
                  </Tooltip>
                </ListItemSecondaryAction>
              </ListItem>
            ))}
          </List>
          
          <Box sx={{ textAlign: 'center', mt: 'auto' }}>
            <Button 
              variant="text" 
              size="small" 
              onClick={handleViewAllTransactions}
              sx={{ fontSize: '0.8rem' }}
            >
              Ver todas
            </Button>
          </Box>
        </>
      )}
    </Paper>
  );
};

export default UpcomingScheduledTransactions;

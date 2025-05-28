import React from 'react';
import {
  Box,
  Typography,
  Divider,
  IconButton,
  useTheme,
  Link
} from '@mui/material';
import {
  MoreVert as MoreVertIcon,
  Schedule as ScheduleIcon,
  ShoppingCart as ShoppingCartIcon,
  SwapHoriz as SwapHorizIcon
} from '@mui/icons-material';
import { TransactionType, TransactionStatus, Transaction } from '../../types/transaction.types';
import { isIncomingTransaction, isOutgoingTransaction } from '../../utils/transactionUtils';
import { useSelector } from 'react-redux';
import { RootState } from '../../store';

interface TransactionsListProps {
  transactions: Transaction[];
  maxItems?: number;
  showViewAll?: boolean;
}

const TransactionsList: React.FC<TransactionsListProps> = ({
  transactions,
  maxItems = 3,
  showViewAll = true
}) => {
  const theme = useTheme();
  const { user } = useSelector((state: RootState) => state.auth);
  const displayedTransactions = transactions.slice(0, maxItems);

  // Función para obtener el icono según el tipo de transacción
  const getTransactionIcon = (type: TransactionType) => {
    switch (type) {
      case TransactionType.PAYMENT:
        return <ScheduleIcon />;
      case TransactionType.WITHDRAWAL:
        return <ShoppingCartIcon />;
      case TransactionType.TRANSFER:
      case TransactionType.DEPOSIT:
      default:
        return <SwapHorizIcon />;
    }
  };

  // Función para obtener el color según el tipo de transacción
  const getTransactionColor = (type: TransactionType) => {
    switch (type) {
      case TransactionType.DEPOSIT:
        return theme.palette.success.main;
      case TransactionType.WITHDRAWAL:
        return theme.palette.error.main;
      case TransactionType.TRANSFER:
        return theme.palette.info.main;
      case TransactionType.PAYMENT:
        return theme.palette.warning.main;
      default:
        return theme.palette.text.secondary;
    }
  };

  // Función para formatear el monto con signo
  const formatAmount = (transaction: Transaction): string => {
    if (!user) {
      return `$${transaction.amount.toFixed(2)}`;
    }

    const isOutgoing = isOutgoingTransaction(transaction, user.id);
    const isIncoming = isIncomingTransaction(transaction, user.id);

    if (isOutgoing) {
      return `-$${transaction.amount.toFixed(2)}`;
    } else if (isIncoming) {
      return `+$${transaction.amount.toFixed(2)}`;
    } else {
      return `$${transaction.amount.toFixed(2)}`;
    }
  };

  // Función para formatear la fecha
  const formatDate = (dateString: string): string => {
    const date = new Date(dateString);
    return `${date.toLocaleDateString()}, ${date.toLocaleTimeString()} - ${TransactionStatus[transactions[0].status]}`;
  };

  return (
    <Box sx={{ width: '100%' }}>
      <Typography variant="h6" sx={{ mb: 1, fontWeight: 600 }}>
        Resumen de Transferencias
      </Typography>

      <Box sx={{
        display: 'flex',
        flexDirection: 'column',
        divideY: 1,
        divideColor: theme.palette.divider
      }}>
        {displayedTransactions.map((transaction, index) => (
          <React.Fragment key={transaction.id}>
            <Box sx={{
              display: 'flex',
              alignItems: 'center',
              py: 1.5,
              '&:hover': {
                bgcolor: `${theme.palette.action.hover}`,
                borderRadius: 1
              },
              cursor: 'pointer',
              transition: 'background-color 0.2s'
            }}>
              <Box sx={{
                mr: 2,
                color: getTransactionColor(transaction.transactionType),
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center',
                width: 40,
                height: 40,
                borderRadius: '50%',
                bgcolor: `${getTransactionColor(transaction.transactionType)}20`,
                flexShrink: 0
              }}>
                {getTransactionIcon(transaction.transactionType)}
              </Box>

              <Box sx={{ flexGrow: 1, mr: 2 }}>
                <Typography variant="body1" sx={{ fontWeight: 500 }}>
                  {transaction.description}
                </Typography>
                <Typography variant="caption" color="textSecondary">
                  {formatDate(transaction.createdAt)}
                </Typography>
              </Box>

              <Typography
                variant="body2"
                sx={{
                  fontWeight: 600,
                  color: getTransactionColor(transaction.transactionType),
                  minWidth: 80,
                  textAlign: 'right',
                  mr: 1
                }}
              >
                {formatAmount(transaction)}
              </Typography>

              <IconButton size="small" sx={{ color: theme.palette.text.secondary }}>
                <MoreVertIcon fontSize="small" />
              </IconButton>
            </Box>
            {index < displayedTransactions.length - 1 && (
              <Divider />
            )}
          </React.Fragment>
        ))}
      </Box>

      {showViewAll && (
        <Box sx={{ textAlign: 'right', mt: 1 }}>
          <Link
            href="/transactions"
            underline="hover"
            sx={{
              fontSize: '0.875rem',
              fontWeight: 500,
              color: theme.palette.primary.main,
              '&:hover': {
                color: theme.palette.primary.dark
              }
            }}
          >
            Ver todas las transferencias →
          </Link>
        </Box>
      )}
    </Box>
  );
};

export default TransactionsList;

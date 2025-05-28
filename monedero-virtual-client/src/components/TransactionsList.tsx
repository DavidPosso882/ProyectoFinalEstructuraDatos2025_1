import React, { useState } from 'react';
import {
  Box,
  Typography,
  Paper,
  List,
  ListItem,
  ListItemText,
  ListItemIcon,
  Divider,
  Chip,
  IconButton,
  Menu,
  MenuItem,
  TextField,
  InputAdornment,
  Button,
  FormControl,
  InputLabel,
  Select,
  SelectChangeEvent,
  Grid,
  Tooltip,
  useTheme
} from '@mui/material';
import {
  ArrowUpward,
  ArrowDownward,
  SwapHoriz,
  Search as SearchIcon,
  FilterList as FilterListIcon,
  MoreVert as MoreVertIcon,
  Receipt as ReceiptIcon,
  Info as InfoIcon,
  Repeat as RepeatIcon,
  Schedule as ScheduleIcon,
  Star as StarIcon,
  Payment as PaymentIcon,
  CardGiftcard as PointsIcon,
  Settings as SystemIcon
} from '@mui/icons-material';
import { Transaction, TransactionType, TransactionStatus } from '../types/transaction.types';
import { isIncomingTransaction, isOutgoingTransaction } from '../utils/transactionUtils';

interface TransactionsListProps {
  transactions: Transaction[];
  title?: string;
  maxHeight?: number | string;
  showFilters?: boolean;
  onViewDetails?: (transaction: Transaction) => void;
  currentUserId?: number;
}

const TransactionsList: React.FC<TransactionsListProps> = ({
  transactions,
  title = 'Transacciones Recientes',
  maxHeight = 400,
  showFilters = true,
  onViewDetails,
  currentUserId
}) => {
  const theme = useTheme();

  // Estados para filtros y búsqueda
  const [searchTerm, setSearchTerm] = useState('');
  const [typeFilter, setTypeFilter] = useState<string>('ALL');
  const [statusFilter, setStatusFilter] = useState<string>('ALL');
  const [sortOrder, setSortOrder] = useState<'asc' | 'desc'>('desc');

  // Estado para menú de acciones
  const [anchorEl, setAnchorEl] = useState<null | HTMLElement>(null);
  const [selectedTransaction, setSelectedTransaction] = useState<Transaction | null>(null);

  // Manejadores de eventos
  const handleOpenMenu = (event: React.MouseEvent<HTMLElement>, transaction: Transaction) => {
    setAnchorEl(event.currentTarget);
    setSelectedTransaction(transaction);
  };

  const handleCloseMenu = () => {
    setAnchorEl(null);
    setSelectedTransaction(null);
  };

  const handleTypeFilterChange = (event: SelectChangeEvent) => {
    setTypeFilter(event.target.value);
  };

  const handleStatusFilterChange = (event: SelectChangeEvent) => {
    setStatusFilter(event.target.value);
  };

  const handleSortOrderChange = () => {
    setSortOrder(sortOrder === 'asc' ? 'desc' : 'asc');
  };

  // Función para obtener el icono según el tipo de transacción
  const getTransactionIcon = (type: TransactionType) => {
    switch (type) {
      case TransactionType.DEPOSIT:
        return <ArrowDownward sx={{ color: theme.palette.success.main }} />;
      case TransactionType.WITHDRAWAL:
        return <ArrowUpward sx={{ color: theme.palette.error.main }} />;
      case TransactionType.TRANSFER:
        return <SwapHoriz sx={{ color: theme.palette.info.main }} />;
      case TransactionType.PAYMENT:
        return <PaymentIcon sx={{ color: theme.palette.warning.main }} />;
      case TransactionType.POINTS_REDEMPTION:
        return <PointsIcon sx={{ color: theme.palette.secondary.main }} />;
      case TransactionType.SCHEDULED_TRANSFER:
        return <ScheduleIcon sx={{ color: theme.palette.info.main }} />;
      case TransactionType.SYSTEM:
        return <SystemIcon sx={{ color: theme.palette.text.secondary }} />;
      default:
        return <ReceiptIcon sx={{ color: theme.palette.text.secondary }} />;
    }
  };

  // Función para obtener el color según el estado de la transacción
  const getStatusColor = (status: TransactionStatus) => {
    switch (status) {
      case TransactionStatus.COMPLETED:
        return theme.palette.success.main;
      case TransactionStatus.PENDING:
        return theme.palette.warning.main;
      case TransactionStatus.FAILED:
        return theme.palette.error.main;
      case TransactionStatus.CANCELLED:
        return theme.palette.info.main;
      default:
        return theme.palette.text.secondary;
    }
  };

  // Función para formatear el monto de la transacción
  const formatAmount = (transaction: Transaction) => {
    if (!currentUserId) {
      // Si no hay usuario actual, mostrar sin prefijo
      return (
        <Typography variant="body2" sx={{ fontWeight: 'bold', color: theme.palette.text.primary }}>
          ${Math.abs(transaction.amount).toFixed(2)}
        </Typography>
      );
    }

    const isOutgoing = isOutgoingTransaction(transaction, currentUserId);
    const isIncoming = isIncomingTransaction(transaction, currentUserId);

    let prefix = '';
    let color = theme.palette.text.primary;

    if (isOutgoing) {
      prefix = '-';
      color = theme.palette.error.main;
    } else if (isIncoming) {
      prefix = '+';
      color = theme.palette.success.main;
    }

    return (
      <Typography variant="body2" sx={{ fontWeight: 'bold', color }}>
        {prefix}${Math.abs(transaction.amount).toFixed(2)}
      </Typography>
    );
  };

  // Filtrar y ordenar transacciones
  const filteredTransactions = transactions
    .filter(transaction => {
      // Filtro por término de búsqueda
      const matchesSearch =
        searchTerm === '' ||
        (transaction.description && transaction.description.toLowerCase().includes(searchTerm.toLowerCase())) ||
        transaction.id.toString().includes(searchTerm);

      // Filtro por tipo
      const matchesType =
        typeFilter === 'ALL' ||
        transaction.transactionType === typeFilter;

      // Filtro por estado
      const matchesStatus =
        statusFilter === 'ALL' ||
        transaction.status === statusFilter;

      return matchesSearch && matchesType && matchesStatus;
    })
    .sort((a, b) => {
      // Ordenar por fecha
      const dateA = new Date(a.createdAt).getTime();
      const dateB = new Date(b.createdAt).getTime();

      return sortOrder === 'asc' ? dateA - dateB : dateB - dateA;
    });

  return (
    <Paper elevation={3} sx={{ mb: 3 }}>
      <Box sx={{ p: 2, borderBottom: `1px solid ${theme.palette.divider}` }}>
        <Typography variant="h6">{title}</Typography>
      </Box>

      {showFilters && (
        <Box sx={{ p: 2, borderBottom: `1px solid ${theme.palette.divider}` }}>
          <Grid container spacing={2}>
            <Grid item xs={12} md={6}>
              <TextField
                fullWidth
                size="small"
                placeholder="Buscar por descripción o ID"
                value={searchTerm}
                onChange={(e) => setSearchTerm(e.target.value)}
                InputProps={{
                  startAdornment: (
                    <InputAdornment position="start">
                      <SearchIcon fontSize="small" />
                    </InputAdornment>
                  ),
                }}
              />
            </Grid>

            <Grid item xs={12} sm={6} md={2}>
              <FormControl fullWidth size="small">
                <InputLabel id="type-filter-label">Tipo</InputLabel>
                <Select
                  labelId="type-filter-label"
                  id="type-filter"
                  value={typeFilter}
                  label="Tipo"
                  onChange={handleTypeFilterChange}
                >
                  <MenuItem value="ALL">Todos</MenuItem>
                  <MenuItem value={TransactionType.DEPOSIT}>Depósitos</MenuItem>
                  <MenuItem value={TransactionType.WITHDRAWAL}>Retiros</MenuItem>
                  <MenuItem value={TransactionType.TRANSFER}>Transferencias</MenuItem>
                  <MenuItem value={TransactionType.PAYMENT}>Pagos</MenuItem>
                  <MenuItem value={TransactionType.POINTS_REDEMPTION}>Canje de Puntos</MenuItem>
                  <MenuItem value={TransactionType.SCHEDULED_TRANSFER}>Transferencias Programadas</MenuItem>
                  <MenuItem value={TransactionType.SYSTEM}>Sistema</MenuItem>
                </Select>
              </FormControl>
            </Grid>

            <Grid item xs={12} sm={6} md={2}>
              <FormControl fullWidth size="small">
                <InputLabel id="status-filter-label">Estado</InputLabel>
                <Select
                  labelId="status-filter-label"
                  id="status-filter"
                  value={statusFilter}
                  label="Estado"
                  onChange={handleStatusFilterChange}
                >
                  <MenuItem value="ALL">Todos</MenuItem>
                  <MenuItem value={TransactionStatus.COMPLETED}>Completadas</MenuItem>
                  <MenuItem value={TransactionStatus.PENDING}>Pendientes</MenuItem>
                  <MenuItem value={TransactionStatus.FAILED}>Fallidas</MenuItem>
                  <MenuItem value={TransactionStatus.CANCELLED}>Canceladas</MenuItem>
                </Select>
              </FormControl>
            </Grid>

            <Grid item xs={12} sm={6} md={2}>
              <Button
                fullWidth
                variant="outlined"
                startIcon={<FilterListIcon />}
                onClick={handleSortOrderChange}
                size="medium"
                sx={{ height: '40px' }}
              >
                {sortOrder === 'desc' ? 'Más recientes' : 'Más antiguas'}
              </Button>
            </Grid>
          </Grid>
        </Box>
      )}

      <List sx={{ maxHeight, overflow: 'auto', p: 0 }}>
        {filteredTransactions.length > 0 ? (
          filteredTransactions.map((transaction, index) => (
            <React.Fragment key={transaction.id}>
              {index > 0 && <Divider component="li" />}
              <ListItem
                alignItems="flex-start"
                sx={{
                  '&:hover': {
                    bgcolor: theme.palette.action.hover,
                  },
                }}
                secondaryAction={
                  <IconButton
                    edge="end"
                    aria-label="more"
                    onClick={(e) => handleOpenMenu(e, transaction)}
                  >
                    <MoreVertIcon />
                  </IconButton>
                }
              >
                <ListItemIcon>
                  {getTransactionIcon(transaction.transactionType)}
                </ListItemIcon>
                <ListItemText
                  primary={
                    <Box sx={{ display: 'flex', justifyContent: 'space-between', pr: 4 }}>
                      <Typography variant="body1" sx={{ fontWeight: 'medium' }}>
                        {transaction.description || 'Sin descripción'}
                      </Typography>
                      {formatAmount(transaction)}
                    </Box>
                  }
                  secondary={
                    <Box sx={{ display: 'flex', alignItems: 'center', mt: 0.5 }}>
                      <Typography
                        variant="caption"
                        color="text.secondary"
                        component="span"
                      >
                        {new Date(transaction.createdAt).toLocaleString()}
                      </Typography>
                      <Chip
                        label={transaction.status}
                        size="small"
                        sx={{
                          ml: 1,
                          height: 20,
                          fontSize: '0.7rem',
                          bgcolor: `${getStatusColor(transaction.status)}20`,
                          color: getStatusColor(transaction.status),
                          fontWeight: 'bold'
                        }}
                      />
                    </Box>
                  }
                />
              </ListItem>
            </React.Fragment>
          ))
        ) : (
          <Box sx={{ p: 3, textAlign: 'center' }}>
            <ReceiptIcon sx={{ fontSize: 40, color: theme.palette.text.secondary, mb: 1 }} />
            <Typography color="textSecondary">
              No hay transacciones para mostrar
            </Typography>
          </Box>
        )}
      </List>

      {/* Menú de acciones */}
      <Menu
        anchorEl={anchorEl}
        open={Boolean(anchorEl)}
        onClose={handleCloseMenu}
      >
        <MenuItem onClick={() => {
          if (onViewDetails && selectedTransaction) {
            onViewDetails(selectedTransaction);
          }
          handleCloseMenu();
        }}>
          <ListItemIcon>
            <InfoIcon fontSize="small" />
          </ListItemIcon>
          <ListItemText>Ver detalles</ListItemText>
        </MenuItem>
        {selectedTransaction?.status === TransactionStatus.COMPLETED && (
          <MenuItem onClick={handleCloseMenu}>
            <ListItemIcon>
              <RepeatIcon fontSize="small" />
            </ListItemIcon>
            <ListItemText>Repetir transacción</ListItemText>
          </MenuItem>
        )}
        {selectedTransaction?.transactionType === TransactionType.TRANSFER && (
          <MenuItem onClick={handleCloseMenu}>
            <ListItemIcon>
              <ScheduleIcon fontSize="small" />
            </ListItemIcon>
            <ListItemText>Programar similar</ListItemText>
          </MenuItem>
        )}
      </Menu>
    </Paper>
  );
};

export default TransactionsList;

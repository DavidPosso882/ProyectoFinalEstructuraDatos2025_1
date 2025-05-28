import React, { useState, useEffect } from 'react';
import {
  Box,
  Typography,
  Grid,
  Paper,
  Card,
  CardContent,
  CardActions,
  Button,
  Divider,
  IconButton,
  Tooltip,
  Chip,
  Stack,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  TextField,
  MenuItem,
  useTheme,
  useMediaQuery,
  CircularProgress,
  LinearProgress,
  Avatar,
  Alert
} from '@mui/material';
import {
  AccountBalanceWallet as WalletIcon,
  Add as AddIcon,
  Edit as EditIcon,
  Delete as DeleteIcon,
  ArrowUpward as ArrowUpwardIcon,
  ArrowDownward as ArrowDownwardIcon,
  SwapHoriz as SwapHorizIcon,
  MoreVert as MoreVertIcon,
  Visibility as VisibilityIcon,
  VisibilityOff as VisibilityOffIcon,
  Star as StarIcon,
  StarBorder as StarBorderIcon,
  History as HistoryIcon,
  TrendingUp as TrendingUpIcon,
  TrendingDown as TrendingDownIcon,
  Info as InfoIcon
} from '@mui/icons-material';
import WalletService from '../services/wallet.service';
import { useDispatch } from 'react-redux';
import { AppDispatch } from '../store';
import { fetchUnreadNotifications } from '../store/slices/notificationSlice';

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
  createdAt: string;
  updatedAt: string;
  color?: string;
  icon?: string;
  monthlyInflow?: number;
  monthlyOutflow?: number;
  transactions?: Transaction[];
}

interface Transaction {
  id: number;
  amount: number;
  description: string;
  date: string;
  type: 'DEPOSIT' | 'WITHDRAWAL' | 'TRANSFER';
}

const WalletsPage: React.FC = () => {
  const theme = useTheme();
  const isMobile = useMediaQuery(theme.breakpoints.down('sm'));
  const isTablet = useMediaQuery(theme.breakpoints.down('md'));
  const dispatch = useDispatch<AppDispatch>();

  // Estados
  const [wallets, setWallets] = useState<Wallet[]>([]);
  const [selectedWallet, setSelectedWallet] = useState<Wallet | null>(null);
  const [openNewWalletDialog, setOpenNewWalletDialog] = useState(false);
  const [openDeleteDialog, setOpenDeleteDialog] = useState(false);
  const [showBalances, setShowBalances] = useState(true);
  const [isLoading, setIsLoading] = useState(false);
  const [newWalletData, setNewWalletData] = useState({
    name: '',
    type: 'PRIMARY' as WalletType,
    description: '',
    initialBalance: ''
  });
  const [openEditDialog, setOpenEditDialog] = useState(false);
  const [editWalletData, setEditWalletData] = useState({
    id: 0,
    name: '',
    type: 'PRIMARY' as WalletType,
    description: ''
  });
  const [openTxDialog, setOpenTxDialog] = useState(false);
  const [txType, setTxType] = useState<'DEPOSIT' | 'WITHDRAWAL' | 'TRANSFER' | null>(null);
  const [txAmount, setTxAmount] = useState('');
  const [txTargetId, setTxTargetId] = useState('');
  const [txError, setTxError] = useState<string | null>(null);

  useEffect(() => {
    WalletService.getUserWallets().then(setWallets);
  }, []);

  // Manejadores de eventos
  const handleWalletSelect = (wallet: Wallet) => {
    setSelectedWallet(wallet);
  };

  const handleToggleFavorite = (id: number) => {
    setWallets(wallets.map(wallet =>
      wallet.id === id ? { ...wallet, isFavorite: !wallet.isFavorite } : wallet
    ));
  };

  const handleToggleBalanceVisibility = () => {
    setShowBalances(!showBalances);
  };

  const handleOpenNewWalletDialog = () => {
    setNewWalletData({
      name: '',
      type: 'PRIMARY',
      description: '',
      initialBalance: ''
    });
    setOpenNewWalletDialog(true);
  };

  const handleCloseNewWalletDialog = () => {
    setOpenNewWalletDialog(false);
  };

  const handleNewWalletChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const { name, value } = e.target;
    setNewWalletData({
      ...newWalletData,
      [name]: value
    });
  };

  const handleCreateWallet = async () => {
    setIsLoading(true);
    try {
      const newWallet = await WalletService.createWallet({
        name: newWalletData.name,
        walletType: newWalletData.type,
        description: newWalletData.description,
        initialBalance: parseFloat(newWalletData.initialBalance) || 0
      });
      const updatedWallets = await WalletService.getUserWallets();
      setWallets(updatedWallets);
      setOpenNewWalletDialog(false);
    } catch (error) {
      alert('Error al crear el monedero');
    } finally {
      setIsLoading(false);
    }
  };

  const handleOpenDeleteDialog = (wallet: Wallet) => {
    setSelectedWallet(wallet);
    setOpenDeleteDialog(true);
  };

  const handleCloseDeleteDialog = () => {
    setOpenDeleteDialog(false);
  };

  const handleDeleteWallet = async () => {
    if (selectedWallet) {
      setIsLoading(true);
      try {
        await WalletService.deleteWallet(selectedWallet.id);
        setWallets(wallets.filter(wallet => wallet.id !== selectedWallet.id));
        setOpenDeleteDialog(false);
        setSelectedWallet(null);
      } catch (error) {
        alert('Error al eliminar el monedero');
      } finally {
        setIsLoading(false);
      }
    }
  };

  const handleOpenEditDialog = (wallet: Wallet) => {
    setEditWalletData({
      id: wallet.id,
      name: wallet.name,
      type: wallet.type,
      description: wallet.description || ''
    });
    setOpenEditDialog(true);
  };

  const handleCloseEditDialog = () => {
    setOpenEditDialog(false);
  };

  const handleEditWalletChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const { name, value } = e.target;
    setEditWalletData({
      ...editWalletData,
      [name]: value
    });
  };

  const handleEditWallet = async () => {
    setIsLoading(true);
    try {
      // Adaptar el objeto para el backend
      const updatePayload = {
        name: editWalletData.name,
        walletType: editWalletData.type,
        description: editWalletData.description
      };
      await WalletService.updateWallet(editWalletData.id, updatePayload);
      const updatedWallets = await WalletService.getUserWallets();
      setWallets(updatedWallets);
      const updated = await WalletService.getWalletById(editWalletData.id);
      setSelectedWallet(updated);
      setOpenEditDialog(false);
    } catch (error) {
      alert('Error al editar el monedero');
    } finally {
      setIsLoading(false);
    }
  };

  const handleOpenTxDialog = (type: 'DEPOSIT' | 'WITHDRAWAL' | 'TRANSFER', wallet: Wallet) => {
    setSelectedWallet(wallet);
    setTxType(type);
    setTxAmount('');
    setTxTargetId('');
    setTxError(null);
    setOpenTxDialog(true);
  };

  const handleCloseTxDialog = () => {
    setOpenTxDialog(false);
    setTxType(null);
    setTxAmount('');
    setTxTargetId('');
    setTxError(null);
  };

  const handleTxSubmit = async () => {
    if (!selectedWallet || !txType) return;
    const amount = parseFloat(txAmount);
    if (isNaN(amount) || amount <= 0) {
      setTxError('Ingresa un monto válido');
      return;
    }
    try {
      setIsLoading(true);
      setTxError(null);
      let req: any = { type: txType, amount, description: `Transacción inmediata (${txType})` };
      if (txType === 'DEPOSIT') req.targetWalletId = selectedWallet.id;
      if (txType === 'WITHDRAWAL') req.sourceWalletId = selectedWallet.id;
      if (txType === 'TRANSFER') {
        req.sourceWalletId = selectedWallet.id;
        req.targetWalletId = Number(txTargetId);
        if (!req.targetWalletId || req.targetWalletId === selectedWallet.id) {
          setTxError('ID de monedero destino inválido');
          setIsLoading(false);
          return;
        }
      }
      // Llama al servicio de transacciones
      await (await import('../services/transaction.service')).default.processTransaction(req);
      // Refresca monederos
      const updatedWallets = await WalletService.getUserWallets();
      setWallets(updatedWallets);
      // Recargar notificaciones para actualizar el contador
      dispatch(fetchUnreadNotifications());
      setOpenTxDialog(false);
    } catch (e: any) {
      setTxError(e.message || 'Error al procesar la transacción');
    } finally {
      setIsLoading(false);
    }
  };

  // Función para obtener el color según el tipo de monedero
  const getColorForWalletType = (type: WalletType): string => {
    switch (type) {
      case 'PRIMARY':
        return theme.palette.primary.main;
      case 'SAVINGS':
        return theme.palette.success.main;
      case 'EXPENSES':
        return theme.palette.warning.main;
      case 'INVESTMENT':
        return theme.palette.secondary.main;
      case 'CUSTOM':
        return theme.palette.info.main;
      default:
        return theme.palette.grey[500];
    }
  };

  // Función para formatear montos con signo
  const formatAmount = (amount: number): string => {
    return amount >= 0
      ? `+$${amount.toFixed(2)}`
      : `-$${Math.abs(amount).toFixed(2)}`;
  };

  // Función para obtener el icono según el tipo de monedero
  const getIconForWalletType = (type: WalletType) => {
    switch (type) {
      case 'PRIMARY':
        return <WalletIcon />;
      case 'SAVINGS':
        return <TrendingUpIcon />;
      case 'EXPENSES':
        return <TrendingDownIcon />;
      case 'INVESTMENT':
        return <StarIcon />;
      case 'CUSTOM':
        return <WalletIcon />;
      default:
        return <WalletIcon />;
    }
  };

  // Renderizar tarjeta de monedero
  const renderWalletCard = (wallet: Wallet) => {
    const isSelected = selectedWallet?.id === wallet.id;

    return (
      <Card
        elevation={isSelected ? 8 : 2}
        sx={{
          height: '100%',
          display: 'flex',
          flexDirection: 'column',
          position: 'relative',
          overflow: 'visible',
          transition: 'all 0.3s ease',
          transform: isSelected ? 'scale(1.02)' : 'scale(1)',
          '&:hover': {
            transform: 'scale(1.02)',
            boxShadow: 6
          },
          '&::before': {
            content: '""',
            position: 'absolute',
            top: 0,
            left: 0,
            width: '100%',
            height: '5px',
            backgroundColor: wallet.color || getColorForWalletType(wallet.type),
            borderTopLeftRadius: theme.shape.borderRadius,
            borderTopRightRadius: theme.shape.borderRadius
          }
        }}
        onClick={() => handleWalletSelect(wallet)}
      >
        <CardContent sx={{ flexGrow: 1, pb: 1, px: 2.5, pt: 2 }}>
          <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 1 }}>
            <Box sx={{ display: 'flex', alignItems: 'center' }}>
              <Avatar
                sx={{
                  mr: 1,
                  bgcolor: wallet.color || getColorForWalletType(wallet.type),
                  width: 32,
                  height: 32
                }}
              >
                {getIconForWalletType(wallet.type)}
              </Avatar>
              <Box>
                <Typography variant="h6" component="h2" noWrap sx={{ maxWidth: 150 }}>
                  {wallet.name}
                </Typography>
                <Chip
                  label={`ID: ${wallet.id}`}
                  size="small"
                  variant="outlined"
                  sx={{
                    fontSize: '0.7rem',
                    height: '18px',
                    mt: 0.5,
                    '& .MuiChip-label': {
                      px: 0.5
                    }
                  }}
                />
              </Box>
            </Box>
            <Box>
              <IconButton
                size="small"
                onClick={(e) => {
                  e.stopPropagation();
                  handleToggleFavorite(wallet.id);
                }}
              >
                {wallet.isFavorite ? <StarIcon color="warning" /> : <StarBorderIcon />}
              </IconButton>
            </Box>
          </Box>

          {wallet.isDefault && (
            <Chip
              label="Principal"
              size="small"
              color="primary"
              sx={{ mb: 1 }}
            />
          )}

          <Typography variant="body2" color="textSecondary" sx={{ mb: 2, height: 40, overflow: 'hidden' }}>
            {wallet.description}
          </Typography>

          <Typography variant="h5" component="p" sx={{ fontWeight: 'bold', mb: 1 }}>
            {showBalances ? `$${wallet.balance.toFixed(2)}` : '••••••'}
          </Typography>

          <Box sx={{ display: 'flex', justifyContent: 'space-between', mb: 1.5, gap: 2 }}>
            <Box sx={{ flex: 1, minWidth: 0 }}>
              <Typography variant="caption" color="textSecondary" sx={{ fontSize: '0.7rem' }}>
                Ingresos
              </Typography>
              <Typography
                variant="body2"
                color="success.main"
                sx={{
                  fontWeight: 'medium',
                  fontSize: '0.85rem',
                  whiteSpace: 'nowrap',
                  overflow: 'hidden',
                  textOverflow: 'ellipsis'
                }}
              >
                {showBalances ? `+$${wallet.monthlyInflow?.toFixed(2) || '0.00'}` : '••••'}
              </Typography>
            </Box>
            <Box sx={{ flex: 1, minWidth: 0, textAlign: 'right' }}>
              <Typography variant="caption" color="textSecondary" sx={{ fontSize: '0.7rem' }}>
                Gastos
              </Typography>
              <Typography
                variant="body2"
                color="error.main"
                sx={{
                  fontWeight: 'medium',
                  fontSize: '0.85rem',
                  whiteSpace: 'nowrap',
                  overflow: 'hidden',
                  textOverflow: 'ellipsis'
                }}
              >
                {showBalances ? `-$${wallet.monthlyOutflow?.toFixed(2) || '0.00'}` : '••••'}
              </Typography>
            </Box>
          </Box>

          {/* Barra de progreso que muestra la relación entre ingresos y gastos */}
          <Box sx={{ width: '100%', mt: 1, mb: 1 }}>
            <LinearProgress
              variant="determinate"
              value={wallet.monthlyOutflow && wallet.monthlyInflow ? (wallet.monthlyOutflow / wallet.monthlyInflow) * 100 : 0}
              color={wallet.monthlyOutflow && wallet.monthlyInflow && wallet.monthlyOutflow > wallet.monthlyInflow ? "error" : "success"}
              sx={{ height: 6, borderRadius: 3 }}
            />
          </Box>
        </CardContent>

        <Divider />

        <CardActions sx={{
          display: 'flex',
          flexDirection: 'column',
          p: 1.5,
          pt: 0.5,
          gap: 0.5,
          alignItems: 'flex-start'
        }}>
          <Box sx={{ display: 'flex', flexDirection: 'column', gap: 0.5, width: '100%' }}>
            <Button
              size="small"
              startIcon={<ArrowUpwardIcon />}
              color="error"
              variant="text"
              sx={{ justifyContent: 'flex-start', padding: '4px 8px', minWidth: 'auto', textTransform: 'none' }}
              onClick={e => { e.stopPropagation(); handleOpenTxDialog('WITHDRAWAL', wallet); }}
            >
              Retirar
            </Button>
            <Button
              size="small"
              startIcon={<ArrowDownwardIcon />}
              color="success"
              variant="text"
              sx={{ justifyContent: 'flex-start', padding: '4px 8px', minWidth: 'auto', textTransform: 'none' }}
              onClick={e => { e.stopPropagation(); handleOpenTxDialog('DEPOSIT', wallet); }}
            >
              Depositar
            </Button>
            <Button
              size="small"
              startIcon={<SwapHorizIcon />}
              color="primary"
              variant="text"
              sx={{ justifyContent: 'flex-start', padding: '4px 8px', minWidth: 'auto', textTransform: 'none' }}
              onClick={e => { e.stopPropagation(); handleOpenTxDialog('TRANSFER', wallet); }}
            >
              Transferir
            </Button>
          </Box>
        </CardActions>
      </Card>
    );
  };

  // Renderizar sección de detalles del monedero seleccionado
  const renderWalletDetails = () => {
    if (!selectedWallet) {
      return (
        <Paper elevation={3} sx={{ p: 3, height: '100%', display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
          <Typography variant="h6" color="textSecondary">
            Selecciona un monedero para ver sus detalles
          </Typography>
        </Paper>
      );
    }

    return (
      <Paper elevation={3} sx={{ p: 3, height: '100%' }}>
        <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 2 }}>
          <Box sx={{ display: 'flex', alignItems: 'center' }}>
            <Avatar
              sx={{
                mr: 2,
                bgcolor: selectedWallet.color || getColorForWalletType(selectedWallet.type),
                width: 48,
                height: 48
              }}
            >
              {getIconForWalletType(selectedWallet.type)}
            </Avatar>
            <Box>
              <Box sx={{ display: 'flex', alignItems: 'center', gap: 1, mb: 0.5 }}>
                <Typography variant="h5" component="h2">
                  {selectedWallet.name}
                </Typography>
                <Chip
                  label={`ID: ${selectedWallet.id}`}
                  size="small"
                  variant="outlined"
                  sx={{
                    fontSize: '0.75rem',
                    height: '20px',
                    '& .MuiChip-label': {
                      px: 0.75
                    }
                  }}
                />
              </Box>
              <Typography variant="body2" color="textSecondary">
                {selectedWallet.description}
              </Typography>
            </Box>
          </Box>
          <Box>
            <Tooltip title="Editar monedero">
              <IconButton onClick={() => handleOpenEditDialog(selectedWallet)}>
                <EditIcon />
              </IconButton>
            </Tooltip>
            <Tooltip title="Eliminar monedero">
              <IconButton color="error" onClick={() => handleOpenDeleteDialog(selectedWallet)}>
                <DeleteIcon />
              </IconButton>
            </Tooltip>
          </Box>
        </Box>

        <Divider sx={{ mb: 3 }} />

        <Grid container spacing={3} sx={{ mb: 3 }}>
          <Grid item xs={12} sm={6}>
            <Paper elevation={2} sx={{ p: 2, bgcolor: theme.palette.background.default }}>
              <Typography variant="subtitle2" color="textSecondary" gutterBottom>
                Balance actual
              </Typography>
              <Typography variant="h4" component="p" sx={{ fontWeight: 'bold' }}>
                {showBalances ? `$${selectedWallet.balance.toFixed(2)}` : '••••••'}
              </Typography>
            </Paper>
          </Grid>
          <Grid item xs={12} sm={6}>
            <Paper elevation={2} sx={{ p: 2, bgcolor: theme.palette.background.default }}>
              <Typography variant="subtitle2" color="textSecondary" gutterBottom>
                Flujo mensual
              </Typography>
              <Box sx={{ display: 'flex', justifyContent: 'space-between' }}>
                <Box>
                  <Typography variant="body2" color="textSecondary">Ingresos</Typography>
                  <Typography variant="body1" color="success.main" sx={{ fontWeight: 'medium' }}>
                    {showBalances ? `+$${selectedWallet.monthlyInflow?.toFixed(2) || '0.00'}` : '••••'}
                  </Typography>
                </Box>
                <Box>
                  <Typography variant="body2" color="textSecondary">Gastos</Typography>
                  <Typography variant="body1" color="error.main" sx={{ fontWeight: 'medium' }}>
                    {showBalances ? `-$${selectedWallet.monthlyOutflow?.toFixed(2) || '0.00'}` : '••••'}
                  </Typography>
                </Box>
              </Box>
            </Paper>
          </Grid>
        </Grid>
      </Paper>
    );
  };

  return (
    <Box>
      {/* Encabezado */}
      <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 3 }}>
        <Typography variant="h4" component="h1">
          Mis Monederos
        </Typography>
        <Box>
          <Tooltip title={showBalances ? "Ocultar balances" : "Mostrar balances"}>
            <IconButton onClick={handleToggleBalanceVisibility} sx={{ mr: 1 }}>
              {showBalances ? <VisibilityOffIcon /> : <VisibilityIcon />}
            </IconButton>
          </Tooltip>
          <Button
            variant="contained"
            startIcon={<AddIcon />}
            onClick={handleOpenNewWalletDialog}
          >
            Nuevo Monedero
          </Button>
        </Box>
      </Box>
      {/* Contenido principal */}
      <Grid container spacing={3}>
        {/* Lista de monederos */}
        <Grid item xs={12} md={6} lg={5}>
          <Box sx={{ mb: 2 }}>
            <Typography variant="h6" gutterBottom>
              Monederos
            </Typography>
            <Typography variant="body2" color="textSecondary">
              Administra tus monederos y realiza operaciones
            </Typography>
          </Box>
          <Grid container spacing={2}>
            {wallets.map(wallet => (
              <Grid item xs={12} sm={6} md={6} lg={12} xl={6} key={wallet.id}>
                {renderWalletCard(wallet)}
              </Grid>
            ))}
          </Grid>
        </Grid>
        {/* Detalles del monedero seleccionado */}
        <Grid item xs={12} md={6} lg={7}>
          {renderWalletDetails()}
        </Grid>
      </Grid>
      {/* Diálogo para crear nuevo monedero */}
      <Dialog
        open={openNewWalletDialog}
        onClose={handleCloseNewWalletDialog}
        fullWidth
        maxWidth="sm"
      >
        <DialogTitle>Crear nuevo monedero</DialogTitle>
        <DialogContent>
          <Box sx={{ mt: 1 }}>
            <TextField
              margin="dense"
              label="Nombre del monedero"
              type="text"
              fullWidth
              variant="outlined"
              name="name"
              value={newWalletData.name}
              onChange={handleNewWalletChange}
              required
              sx={{ mb: 2 }}
              error={!!newWalletData.name && newWalletData.name.length < 3}
              helperText={!!newWalletData.name && newWalletData.name.length < 3 ? 'El nombre debe tener al menos 3 caracteres' : ''}
            />
            <TextField
              select
              margin="dense"
              label="Tipo de monedero"
              fullWidth
              variant="outlined"
              name="type"
              value={newWalletData.type}
              onChange={handleNewWalletChange}
              required
              sx={{ mb: 2 }}
            >
              <MenuItem value="PRIMARY">Principal</MenuItem>
              <MenuItem value="SAVINGS">Ahorros</MenuItem>
              <MenuItem value="EXPENSES">Gastos</MenuItem>
              <MenuItem value="INVESTMENT">Inversión</MenuItem>
            </TextField>
            <TextField
              margin="dense"
              label="Descripción"
              type="text"
              fullWidth
              variant="outlined"
              name="description"
              value={newWalletData.description}
              onChange={handleNewWalletChange}
              multiline
              rows={2}
              sx={{ mb: 2 }}
            />
            <TextField
              margin="dense"
              label="Balance inicial"
              type="text"
              fullWidth
              variant="outlined"
              name="initialBalance"
              value={newWalletData.initialBalance}
              onChange={e => {
                const value = e.target.value.replace(/[^\d.]/g, '');
                setNewWalletData({
                  ...newWalletData,
                  initialBalance: value
                });
              }}
              inputProps={{ inputMode: 'decimal', pattern: '[0-9.]*' }}
            />
          </Box>
        </DialogContent>
        <DialogActions>
          <Button onClick={handleCloseNewWalletDialog}>Cancelar</Button>
          <Button
            onClick={handleCreateWallet}
            variant="contained"
            disabled={!newWalletData.name || newWalletData.name.length < 3 || isLoading}
          >
            {isLoading ? <CircularProgress size={24} /> : 'Crear'}
          </Button>
        </DialogActions>
      </Dialog>
      {/* Diálogo para confirmar eliminación */}
      <Dialog
        open={openDeleteDialog}
        onClose={handleCloseDeleteDialog}
      >
        <DialogTitle>Eliminar monedero</DialogTitle>
        <DialogContent>
          <Typography variant="body1">
            ¿Estás seguro de que deseas eliminar el monedero "{selectedWallet?.name}"?
          </Typography>
          <Typography variant="body2" color="error" sx={{ mt: 2 }}>
            Esta acción no se puede deshacer.
          </Typography>
        </DialogContent>
        <DialogActions>
          <Button onClick={handleCloseDeleteDialog}>Cancelar</Button>
          <Button
            onClick={handleDeleteWallet}
            color="error"
            variant="contained"
            disabled={isLoading}
          >
            {isLoading ? <CircularProgress size={24} /> : 'Eliminar'}
          </Button>
        </DialogActions>
      </Dialog>
      {/* Diálogo de edición de monedero */}
      <Dialog
        open={openEditDialog}
        onClose={handleCloseEditDialog}
        fullWidth
        maxWidth="sm"
      >
        <DialogTitle>Editar monedero</DialogTitle>
        <DialogContent>
          <Box sx={{ mt: 1 }}>
            <TextField
              margin="dense"
              label="Nombre del monedero"
              type="text"
              fullWidth
              variant="outlined"
              name="name"
              value={editWalletData.name}
              onChange={handleEditWalletChange}
              required
              sx={{ mb: 2 }}
              error={!!editWalletData.name && editWalletData.name.length < 3}
              helperText={!!editWalletData.name && editWalletData.name.length < 3 ? 'El nombre debe tener al menos 3 caracteres' : ''}
            />
            <TextField
              select
              margin="dense"
              label="Tipo de monedero"
              fullWidth
              variant="outlined"
              name="type"
              value={editWalletData.type}
              onChange={handleEditWalletChange}
              required
              sx={{ mb: 2 }}
            >
              <MenuItem value="PRIMARY">Principal</MenuItem>
              <MenuItem value="SAVINGS">Ahorros</MenuItem>
              <MenuItem value="EXPENSES">Gastos</MenuItem>
              <MenuItem value="INVESTMENT">Inversión</MenuItem>
            </TextField>
            <TextField
              margin="dense"
              label="Descripción"
              type="text"
              fullWidth
              variant="outlined"
              name="description"
              value={editWalletData.description}
              onChange={handleEditWalletChange}
              multiline
              rows={2}
              sx={{ mb: 2 }}
            />
          </Box>
        </DialogContent>
        <DialogActions>
          <Button onClick={handleCloseEditDialog}>Cancelar</Button>
          <Button
            onClick={handleEditWallet}
            variant="contained"
            disabled={!editWalletData.name || editWalletData.name.length < 3 || isLoading}
          >
            {isLoading ? <CircularProgress size={24} /> : 'Guardar'}
          </Button>
        </DialogActions>
      </Dialog>
      {/* Diálogo para transacciones inmediatas */}
      <Dialog open={openTxDialog} onClose={handleCloseTxDialog} fullWidth maxWidth="xs">
        <DialogTitle>Transacción Inmediata</DialogTitle>
        <DialogContent>
          <Box sx={{ mt: 1 }}>
            <Typography variant="subtitle1" gutterBottom>
              {txType === 'DEPOSIT' && 'Depósito'}
              {txType === 'WITHDRAWAL' && 'Retiro'}
              {txType === 'TRANSFER' && 'Transferencia'}
            </Typography>
            <TextField
              label="Monto"
              type="number"
              fullWidth
              value={txAmount}
              onChange={e => setTxAmount(e.target.value)}
              sx={{ mb: 2 }}
            />
            {txType === 'TRANSFER' && (
              <TextField
                label="ID de monedero destino"
                type="number"
                fullWidth
                value={txTargetId}
                onChange={e => setTxTargetId(e.target.value)}
                sx={{ mb: 2 }}
              />
            )}
            {txError && <Alert severity="error">{txError}</Alert>}
          </Box>
        </DialogContent>
        <DialogActions>
          <Button onClick={handleCloseTxDialog}>Cancelar</Button>
          <Button onClick={handleTxSubmit} variant="contained" disabled={isLoading}>
            {isLoading ? <CircularProgress size={24} /> : 'Realizar'}
          </Button>
        </DialogActions>
      </Dialog>
    </Box>
  );
};

export default WalletsPage;
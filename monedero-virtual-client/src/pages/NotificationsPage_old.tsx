import React, { useState, useEffect } from 'react';
import {
  Box,
  Typography,
  Paper,
  List,
  ListItem,
  ListItemText,
  ListItemIcon,
  ListItemAvatar,
  Avatar,
  IconButton,
  Divider,
  Chip,
  Button,
  Menu,
  MenuItem,
  Tooltip,
  Badge,
  Tabs,
  Tab,
  CircularProgress,
  useTheme,
  Fade,
  Checkbox,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogContentText,
  DialogActions,
  Snackbar,
  Alert,
  Grid
} from '@mui/material';
import {
  Notifications as NotificationIcon,
  Delete as DeleteIcon,
  MoreVert as MoreVertIcon,
  CheckCircle as CheckCircleIcon,
  Error as ErrorIcon,
  Info as InfoIcon,
  Warning as WarningIcon,
  MonetizationOn as PointsIcon,
  AccountBalanceWallet as WalletIcon,
  Security as SecurityIcon,
  Settings as SettingsIcon,
  DoneAll as DoneAllIcon,
  FilterList as FilterListIcon,
  Search as SearchIcon,
  Refresh as RefreshIcon,
  NotificationsActive as NotificationsActiveIcon,
  NotificationsOff as NotificationsOffIcon,
  NotificationsNone as NotificationsNoneIcon
} from '@mui/icons-material';
import { format, formatDistanceToNow } from 'date-fns';
import { es } from 'date-fns/locale';
import { useSelector, useDispatch } from 'react-redux';
import { RootState, AppDispatch } from '../store';
import {
  fetchNotifications,
  markNotificationAsRead,
  markAllNotificationsAsRead,
  clearNotificationError
} from '../store/slices/notificationSlice';

// Tipos para las notificaciones
type NotificationType = 'TRANSACTION' | 'SECURITY' | 'POINTS' | 'SYSTEM';
type NotificationPriority = 'LOW' | 'MEDIUM' | 'HIGH';
type NotificationStatus = 'READ' | 'UNREAD';

interface Notification {
  id: number;
  title: string;
  message: string;
  type: NotificationType;
  priority: NotificationPriority;
  status: NotificationStatus;
  createdAt: string;
  relatedEntityId?: number;
  relatedEntityType?: string;
  actionUrl?: string;
  actionText?: string;
}

// Datos de ejemplo para desarrollo
const mockNotifications: Notification[] = [
  {
    id: 1,
    title: 'Puntos acreditados',
    message: 'Has recibido 50 puntos por tu depósito de $500',
    type: 'POINTS',
    priority: 'MEDIUM',
    status: 'UNREAD',
    createdAt: '2023-04-10T10:35:00',
    relatedEntityId: 1,
    relatedEntityType: 'TRANSACTION',
    actionUrl: '/points',
    actionText: 'Ver puntos'
  },
  {
    id: 2,
    title: 'Transferencia completada',
    message: 'Tu transferencia de $1000 a la cuenta de Ahorros se ha completado exitosamente',
    type: 'TRANSACTION',
    priority: 'LOW',
    status: 'READ',
    createdAt: '2023-04-15T09:25:00',
    relatedEntityId: 3,
    relatedEntityType: 'TRANSACTION',
    actionUrl: '/transactions/3',
    actionText: 'Ver detalles'
  },
  {
    id: 3,
    title: 'Alerta de seguridad',
    message: 'Se ha detectado un inicio de sesión desde un nuevo dispositivo. Si no fuiste tú, cambia tu contraseña inmediatamente.',
    type: 'SECURITY',
    priority: 'HIGH',
    status: 'UNREAD',
    createdAt: '2023-04-16T14:30:00',
    actionUrl: '/settings/security',
    actionText: 'Revisar seguridad'
  },
  {
    id: 4,
    title: 'Transferencia programada',
    message: 'Recordatorio: Tienes una transferencia programada para mañana por $200',
    type: 'TRANSACTION',
    priority: 'MEDIUM',
    status: 'UNREAD',
    createdAt: '2023-04-30T10:00:00',
    relatedEntityId: 5,
    relatedEntityType: 'SCHEDULED_TRANSACTION',
    actionUrl: '/transactions/scheduled/5',
    actionText: 'Ver detalles'
  },
  {
    id: 5,
    title: 'Actualización del sistema',
    message: 'Hemos actualizado nuestros términos y condiciones. Por favor, revísalos cuando tengas tiempo.',
    type: 'SYSTEM',
    priority: 'LOW',
    status: 'UNREAD',
    createdAt: '2023-04-25T08:15:00',
    actionUrl: '/settings/terms',
    actionText: 'Leer términos'
  },
  {
    id: 6,
    title: 'Nuevo nivel alcanzado',
    message: '¡Felicidades! Has alcanzado el nivel GOLD en nuestro programa de puntos. Disfruta de nuevos beneficios.',
    type: 'POINTS',
    priority: 'HIGH',
    status: 'READ',
    createdAt: '2023-04-20T16:45:00',
    actionUrl: '/points/benefits',
    actionText: 'Ver beneficios'
  },
  {
    id: 7,
    title: 'Saldo bajo',
    message: 'Tu monedero "Gastos" tiene un saldo bajo de $50. Considera hacer un depósito pronto.',
    type: 'TRANSACTION',
    priority: 'MEDIUM',
    status: 'READ',
    createdAt: '2023-04-28T11:20:00',
    relatedEntityId: 3,
    relatedEntityType: 'WALLET',
    actionUrl: '/wallets/3',
    actionText: 'Ver monedero'
  },
  {
    id: 8,
    title: 'Cambio de contraseña exitoso',
    message: 'Tu contraseña ha sido cambiada exitosamente. Si no realizaste este cambio, contacta a soporte inmediatamente.',
    type: 'SECURITY',
    priority: 'HIGH',
    status: 'READ',
    createdAt: '2023-04-22T09:10:00',
    actionUrl: '/support',
    actionText: 'Contactar soporte'
  }
];

const NotificationsPage: React.FC = () => {
  const theme = useTheme();

  // Estados
  const [notifications, setNotifications] = useState<Notification[]>(mockNotifications);
  const [selectedTab, setSelectedTab] = useState<number>(0);
  const [isLoading, setIsLoading] = useState<boolean>(false);
  const [selectedNotification, setSelectedNotification] = useState<Notification | null>(null);
  const [anchorEl, setAnchorEl] = useState<null | HTMLElement>(null);
  const [filterAnchorEl, setFilterAnchorEl] = useState<null | HTMLElement>(null);
  const [selectedNotifications, setSelectedNotifications] = useState<number[]>([]);
  const [openDeleteDialog, setOpenDeleteDialog] = useState<boolean>(false);
  const [snackbarOpen, setSnackbarOpen] = useState<boolean>(false);
  const [snackbarMessage, setSnackbarMessage] = useState<string>('');
  const [snackbarSeverity, setSnackbarSeverity] = useState<'success' | 'error' | 'info' | 'warning'>('success');

  // Filtros
  const [typeFilter, setTypeFilter] = useState<NotificationType | 'ALL'>('ALL');
  const [priorityFilter, setPriorityFilter] = useState<NotificationPriority | 'ALL'>('ALL');

  // Efectos
  useEffect(() => {
    // Simulación de carga de notificaciones
    setIsLoading(true);
    setTimeout(() => {
      setIsLoading(false);
    }, 1000);
  }, []);

  // Manejadores de eventos
  const handleTabChange = (_: React.SyntheticEvent, newValue: number) => {
    setSelectedTab(newValue);
  };

  const handleNotificationClick = (notification: Notification) => {
    // Si la notificación está sin leer, marcarla como leída
    if (notification.status === 'UNREAD') {
      handleMarkAsRead(notification.id);
    }

    setSelectedNotification(notification);

    // Aquí se podría implementar la navegación a la URL de acción si existe
    if (notification.actionUrl) {
      console.log(`Navegando a: ${notification.actionUrl}`);
      // navigate(notification.actionUrl);
    }
  };

  const handleMenuOpen = (event: React.MouseEvent<HTMLElement>, notification: Notification) => {
    event.stopPropagation();
    setAnchorEl(event.currentTarget);
    setSelectedNotification(notification);
  };

  const handleMenuClose = () => {
    setAnchorEl(null);
  };

  const handleFilterMenuOpen = (event: React.MouseEvent<HTMLElement>) => {
    setFilterAnchorEl(event.currentTarget);
  };

  const handleFilterMenuClose = () => {
    setFilterAnchorEl(null);
  };

  const handleMarkAsRead = (id: number) => {
    setNotifications(prev =>
      prev.map(notification =>
        notification.id === id ? { ...notification, status: 'READ' } : notification
      )
    );
    handleMenuClose();
    showSnackbar('Notificación marcada como leída', 'success');
  };

  const handleMarkAllAsRead = () => {
    setNotifications(prev =>
      prev.map(notification => ({ ...notification, status: 'READ' }))
    );
    showSnackbar('Todas las notificaciones marcadas como leídas', 'success');
  };

  const handleDeleteNotification = (id: number) => {
    setNotifications(prev =>
      prev.filter(notification => notification.id !== id)
    );
    handleMenuClose();
    showSnackbar('Notificación eliminada', 'success');
  };

  const handleDeleteSelected = () => {
    setNotifications(prev =>
      prev.filter(notification => !selectedNotifications.includes(notification.id))
    );
    setSelectedNotifications([]);
    setOpenDeleteDialog(false);
    showSnackbar('Notificaciones seleccionadas eliminadas', 'success');
  };

  const handleDeleteAllRead = () => {
    setNotifications(prev =>
      prev.filter(notification => notification.status === 'UNREAD')
    );
    setOpenDeleteDialog(false);
    showSnackbar('Notificaciones leídas eliminadas', 'success');
  };

  const handleSelectNotification = (event: React.ChangeEvent<HTMLInputElement>, id: number) => {
    event.stopPropagation();

    setSelectedNotifications(prev => {
      if (prev.includes(id)) {
        return prev.filter(notificationId => notificationId !== id);
      } else {
        return [...prev, id];
      }
    });
  };

  const handleSelectAll = (event: React.ChangeEvent<HTMLInputElement>) => {
    if (event.target.checked) {
      const allIds = filteredNotifications.map(notification => notification.id);
      setSelectedNotifications(allIds);
    } else {
      setSelectedNotifications([]);
    }
  };

  const handleOpenDeleteDialog = () => {
    setOpenDeleteDialog(true);
  };

  const handleCloseDeleteDialog = () => {
    setOpenDeleteDialog(false);
  };

  const handleRefresh = () => {
    setIsLoading(true);
    // Simulación de recarga de notificaciones
    setTimeout(() => {
      setIsLoading(false);
      showSnackbar('Notificaciones actualizadas', 'success');
    }, 1000);
  };

  const handleSetTypeFilter = (type: NotificationType | 'ALL') => {
    setTypeFilter(type);
    handleFilterMenuClose();
  };

  const handleSetPriorityFilter = (priority: NotificationPriority | 'ALL') => {
    setPriorityFilter(priority);
    handleFilterMenuClose();
  };

  const showSnackbar = (message: string, severity: 'success' | 'error' | 'info' | 'warning') => {
    setSnackbarMessage(message);
    setSnackbarSeverity(severity);
    setSnackbarOpen(true);
  };

  const handleCloseSnackbar = () => {
    setSnackbarOpen(false);
  };

  // Funciones auxiliares
  const getNotificationIcon = (type: NotificationType, priority: NotificationPriority) => {
    switch (type) {
      case 'TRANSACTION':
        return <WalletIcon sx={{ color: theme.palette.info.main }} />;
      case 'SECURITY':
        return <SecurityIcon sx={{ color: priority === 'HIGH' ? theme.palette.error.main : theme.palette.warning.main }} />;
      case 'POINTS':
        return <PointsIcon sx={{ color: theme.palette.success.main }} />;
      case 'SYSTEM':
        return <SettingsIcon sx={{ color: theme.palette.grey[700] }} />;
      default:
        return <InfoIcon />;
    }
  };

  const getNotificationColor = (priority: NotificationPriority) => {
    switch (priority) {
      case 'HIGH':
        return theme.palette.error.main;
      case 'MEDIUM':
        return theme.palette.warning.main;
      case 'LOW':
        return theme.palette.info.main;
      default:
        return theme.palette.grey[500];
    }
  };

  const getNotificationTypeLabel = (type: NotificationType) => {
    switch (type) {
      case 'TRANSACTION':
        return 'Transacción';
      case 'SECURITY':
        return 'Seguridad';
      case 'POINTS':
        return 'Puntos';
      case 'SYSTEM':
        return 'Sistema';
      default:
        return type;
    }
  };

  const getNotificationPriorityLabel = (priority: NotificationPriority) => {
    switch (priority) {
      case 'HIGH':
        return 'Alta';
      case 'MEDIUM':
        return 'Media';
      case 'LOW':
        return 'Baja';
      default:
        return priority;
    }
  };

  const formatDate = (dateString: string) => {
    const date = new Date(dateString);
    return format(date, 'dd/MM/yyyy HH:mm', { locale: es });
  };

  const getTimeAgo = (dateString: string) => {
    const date = new Date(dateString);
    return formatDistanceToNow(date, { addSuffix: true, locale: es });
  };

  // Filtrar notificaciones según la pestaña seleccionada y los filtros aplicados
  const getFilteredNotifications = () => {
    let filtered = [...notifications];

    // Filtrar por pestaña
    if (selectedTab === 1) {
      filtered = filtered.filter(notification => notification.status === 'UNREAD');
    } else if (selectedTab === 2) {
      filtered = filtered.filter(notification => notification.status === 'READ');
    }

    // Aplicar filtros adicionales
    if (typeFilter !== 'ALL') {
      filtered = filtered.filter(notification => notification.type === typeFilter);
    }

    if (priorityFilter !== 'ALL') {
      filtered = filtered.filter(notification => notification.priority === priorityFilter);
    }

    // Ordenar por fecha (más recientes primero)
    filtered.sort((a, b) => new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime());

    return filtered;
  };

  const filteredNotifications = getFilteredNotifications();
  const unreadCount = notifications.filter(notification => notification.status === 'UNREAD').length;

  return (
    <Box>
      {/* Encabezado */}
      <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 3 }}>
        <Box sx={{ display: 'flex', alignItems: 'center' }}>
          <Badge badgeContent={unreadCount} color="error" sx={{ mr: 2 }}>
            <NotificationIcon fontSize="large" />
          </Badge>
          <Typography variant="h4" component="h1">
            Notificaciones
          </Typography>
        </Box>

        <Box>
          <Tooltip title="Actualizar">
            <IconButton onClick={handleRefresh} disabled={isLoading}>
              {isLoading ? <CircularProgress size={24} /> : <RefreshIcon />}
            </IconButton>
          </Tooltip>
          <Tooltip title="Filtrar">
            <IconButton onClick={handleFilterMenuOpen}>
              <FilterListIcon />
            </IconButton>
          </Tooltip>
          <Menu
            anchorEl={filterAnchorEl}
            open={Boolean(filterAnchorEl)}
            onClose={handleFilterMenuClose}
          >
            <MenuItem disabled>
              <Typography variant="subtitle2">Filtrar por tipo</Typography>
            </MenuItem>
            <MenuItem
              onClick={() => handleSetTypeFilter('ALL')}
              selected={typeFilter === 'ALL'}
            >
              Todos
            </MenuItem>
            <MenuItem
              onClick={() => handleSetTypeFilter('TRANSACTION')}
              selected={typeFilter === 'TRANSACTION'}
            >
              Transacciones
            </MenuItem>
            <MenuItem
              onClick={() => handleSetTypeFilter('SECURITY')}
              selected={typeFilter === 'SECURITY'}
            >
              Seguridad
            </MenuItem>
            <MenuItem
              onClick={() => handleSetTypeFilter('POINTS')}
              selected={typeFilter === 'POINTS'}
            >
              Puntos
            </MenuItem>
            <MenuItem
              onClick={() => handleSetTypeFilter('SYSTEM')}
              selected={typeFilter === 'SYSTEM'}
            >
              Sistema
            </MenuItem>
            <Divider />
            <MenuItem disabled>
              <Typography variant="subtitle2">Filtrar por prioridad</Typography>
            </MenuItem>
            <MenuItem
              onClick={() => handleSetPriorityFilter('ALL')}
              selected={priorityFilter === 'ALL'}
            >
              Todas
            </MenuItem>
            <MenuItem
              onClick={() => handleSetPriorityFilter('HIGH')}
              selected={priorityFilter === 'HIGH'}
            >
              Alta
            </MenuItem>
            <MenuItem
              onClick={() => handleSetPriorityFilter('MEDIUM')}
              selected={priorityFilter === 'MEDIUM'}
            >
              Media
            </MenuItem>
            <MenuItem
              onClick={() => handleSetPriorityFilter('LOW')}
              selected={priorityFilter === 'LOW'}
            >
              Baja
            </MenuItem>
          </Menu>
        </Box>
      </Box>

      {/* Pestañas y acciones */}
      <Box sx={{ borderBottom: 1, borderColor: 'divider', mb: 2 }}>
        <Grid container alignItems="center">
          <Grid item xs>
            <Tabs
              value={selectedTab}
              onChange={handleTabChange}
              aria-label="notification tabs"
            >
              <Tab
                label="Todas"
                id="notification-tab-0"
                aria-controls="notification-tabpanel-0"
              />
              <Tab
                label={
                  <Box sx={{ display: 'flex', alignItems: 'center' }}>
                    No leídas
                    {unreadCount > 0 && (
                      <Badge
                        badgeContent={unreadCount}
                        color="error"
                        sx={{ ml: 1 }}
                      />
                    )}
                  </Box>
                }
                id="notification-tab-1"
                aria-controls="notification-tabpanel-1"
              />
              <Tab
                label="Leídas"
                id="notification-tab-2"
                aria-controls="notification-tabpanel-2"
              />
            </Tabs>
          </Grid>

          <Grid item>
            <Box sx={{ display: 'flex', gap: 1 }}>
              {selectedNotifications.length > 0 ? (
                <Button
                  variant="outlined"
                  color="error"
                  startIcon={<DeleteIcon />}
                  onClick={handleOpenDeleteDialog}
                  size="small"
                >
                  Eliminar seleccionadas ({selectedNotifications.length})
                </Button>
              ) : (
                <>
                  <Button
                    variant="outlined"
                    startIcon={<DoneAllIcon />}
                    onClick={handleMarkAllAsRead}
                    size="small"
                    disabled={!notifications.some(n => n.status === 'UNREAD')}
                  >
                    Marcar todas como leídas
                  </Button>
                  <Button
                    variant="outlined"
                    color="error"
                    startIcon={<DeleteIcon />}
                    onClick={handleOpenDeleteDialog}
                    size="small"
                    disabled={!notifications.some(n => n.status === 'READ')}
                  >
                    Eliminar leídas
                  </Button>
                </>
              )}
            </Box>
          </Grid>
        </Grid>
      </Box>

      {/* Lista de notificaciones */}
      <Paper elevation={3} sx={{ mb: 3 }}>
        {isLoading ? (
          <Box sx={{ display: 'flex', justifyContent: 'center', p: 4 }}>
            <CircularProgress />
          </Box>
        ) : filteredNotifications.length === 0 ? (
          <Box sx={{ display: 'flex', flexDirection: 'column', alignItems: 'center', p: 4 }}>
            <NotificationsNoneIcon sx={{ fontSize: 60, color: theme.palette.grey[400], mb: 2 }} />
            <Typography variant="h6" color="textSecondary">
              No hay notificaciones
            </Typography>
            <Typography variant="body2" color="textSecondary">
              {selectedTab === 0
                ? 'No tienes notificaciones en este momento.'
                : selectedTab === 1
                  ? 'No tienes notificaciones sin leer.'
                  : 'No tienes notificaciones leídas.'}
            </Typography>
          </Box>
        ) : (
          <List sx={{ width: '100%', bgcolor: 'background.paper' }}>
            <ListItem>
              <Checkbox
                edge="start"
                checked={selectedNotifications.length === filteredNotifications.length && filteredNotifications.length > 0}
                indeterminate={selectedNotifications.length > 0 && selectedNotifications.length < filteredNotifications.length}
                onChange={handleSelectAll}
                inputProps={{ 'aria-label': 'select all notifications' }}
              />
              <ListItemText
                primary={
                  <Typography variant="subtitle2" color="textSecondary">
                    {selectedNotifications.length > 0
                      ? `${selectedNotifications.length} seleccionadas`
                      : `${filteredNotifications.length} notificaciones`}
                  </Typography>
                }
              />
            </ListItem>
            <Divider />

            {filteredNotifications.map((notification, index) => (
              <React.Fragment key={notification.id}>
                <ListItem
                  alignItems="flex-start"
                  sx={{
                    bgcolor: notification.status === 'UNREAD' ? `${theme.palette.primary.main}08` : 'transparent',
                    '&:hover': {
                      bgcolor: `${theme.palette.primary.main}15`,
                    },
                    cursor: 'pointer',
                    transition: 'background-color 0.2s'
                  }}
                  onClick={() => handleNotificationClick(notification)}
                  secondaryAction={
                    <IconButton
                      edge="end"
                      aria-label="more"
                      onClick={(e) => handleMenuOpen(e, notification)}
                    >
                      <MoreVertIcon />
                    </IconButton>
                  }
                >
                  <Checkbox
                    edge="start"
                    checked={selectedNotifications.includes(notification.id)}
                    onChange={(e) => handleSelectNotification(e, notification.id)}
                    onClick={(e) => e.stopPropagation()}
                  />
                  <ListItemAvatar>
                    <Avatar sx={{ bgcolor: getNotificationColor(notification.priority) }}>
                      {getNotificationIcon(notification.type, notification.priority)}
                    </Avatar>
                  </ListItemAvatar>
                  <ListItemText
                    primary={
                      <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                        <Typography
                          variant="subtitle1"
                          component="span"
                          sx={{
                            fontWeight: notification.status === 'UNREAD' ? 'bold' : 'normal',
                          }}
                        >
                          {notification.title}
                        </Typography>
                        {notification.status === 'UNREAD' && (
                          <Box
                            component="span"
                            sx={{
                              width: 8,
                              height: 8,
                              borderRadius: '50%',
                              bgcolor: theme.palette.primary.main,
                              display: 'inline-block'
                            }}
                          />
                        )}
                        <Chip
                          label={getNotificationTypeLabel(notification.type)}
                          size="small"
                          sx={{
                            bgcolor: `${getNotificationColor(notification.priority)}20`,
                            color: getNotificationColor(notification.priority),
                            fontWeight: 'medium',
                            fontSize: '0.7rem'
                          }}
                        />
                      </Box>
                    }
                    secondary={
                      <React.Fragment>
                        <Typography
                          component="span"
                          variant="body2"
                          color="text.primary"
                          sx={{ display: 'block', mb: 0.5 }}
                        >
                          {notification.message}
                        </Typography>
                        <Typography
                          component="span"
                          variant="caption"
                          color="text.secondary"
                        >
                          {getTimeAgo(notification.createdAt)} • {formatDate(notification.createdAt)}
                        </Typography>
                        {notification.actionText && (
                          <Button
                            size="small"
                            sx={{ mt: 1 }}
                            onClick={(e) => {
                              e.stopPropagation();
                              console.log(`Acción: ${notification.actionUrl}`);
                              // navigate(notification.actionUrl);
                            }}
                          >
                            {notification.actionText}
                          </Button>
                        )}
                      </React.Fragment>
                    }
                  />
                </ListItem>
                {index < filteredNotifications.length - 1 && <Divider variant="inset" component="li" />}
              </React.Fragment>
            ))}
          </List>
        )}
      </Paper>

      {/* Menú contextual para notificaciones */}
      <Menu
        anchorEl={anchorEl}
        open={Boolean(anchorEl)}
        onClose={handleMenuClose}
      >
        {selectedNotification?.status === 'UNREAD' && (
          <MenuItem onClick={() => handleMarkAsRead(selectedNotification.id)}>
            <ListItemIcon>
              <DoneAllIcon fontSize="small" />
            </ListItemIcon>
            <ListItemText>Marcar como leída</ListItemText>
          </MenuItem>
        )}
        <MenuItem onClick={() => handleDeleteNotification(selectedNotification?.id || 0)}>
          <ListItemIcon>
            <DeleteIcon fontSize="small" />
          </ListItemIcon>
          <ListItemText>Eliminar</ListItemText>
        </MenuItem>
      </Menu>

      {/* Diálogo de confirmación para eliminar */}
      <Dialog
        open={openDeleteDialog}
        onClose={handleCloseDeleteDialog}
      >
        <DialogTitle>
          {selectedNotifications.length > 0
            ? "Eliminar notificaciones seleccionadas"
            : "Eliminar notificaciones leídas"}
        </DialogTitle>
        <DialogContent>
          <DialogContentText>
            {selectedNotifications.length > 0
              ? `¿Estás seguro de que deseas eliminar las ${selectedNotifications.length} notificaciones seleccionadas?`
              : "¿Estás seguro de que deseas eliminar todas las notificaciones leídas?"}
          </DialogContentText>
        </DialogContent>
        <DialogActions>
          <Button onClick={handleCloseDeleteDialog}>Cancelar</Button>
          <Button
            onClick={selectedNotifications.length > 0 ? handleDeleteSelected : handleDeleteAllRead}
            color="error"
            autoFocus
          >
            Eliminar
          </Button>
        </DialogActions>
      </Dialog>

      {/* Snackbar para mensajes */}
      <Snackbar
        open={snackbarOpen}
        autoHideDuration={4000}
        onClose={handleCloseSnackbar}
        anchorOrigin={{ vertical: 'bottom', horizontal: 'center' }}
      >
        <Alert
          onClose={handleCloseSnackbar}
          severity={snackbarSeverity}
          sx={{ width: '100%' }}
        >
          {snackbarMessage}
        </Alert>
      </Snackbar>
    </Box>
  );
};

export default NotificationsPage;

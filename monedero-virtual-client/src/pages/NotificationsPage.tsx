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
  deleteNotification,
  deleteReadNotifications,
  clearNotificationError
} from '../store/slices/notificationSlice';

// Tipos para las notificaciones (compatibles con el backend)
type NotificationType = 'TRANSACTION' | 'SECURITY' | 'POINTS' | 'SYSTEM';
type NotificationPriority = 'LOW' | 'MEDIUM' | 'HIGH';

// Interfaz compatible con el backend NotificationResponse
interface BackendNotification {
  id: number;
  title: string;
  message: string;
  type: NotificationType;
  createdAt: string;
  readAt?: string;
  read: boolean;
  relatedEntityId?: number;
  relatedEntityType?: string;
}

// Interfaz extendida para el frontend con campos adicionales
interface Notification extends BackendNotification {
  priority?: NotificationPriority;
  actionUrl?: string;
  actionText?: string;
}

// Función auxiliar para determinar la prioridad basada en el tipo de notificación
const getNotificationPriority = (type: NotificationType): NotificationPriority => {
  switch (type) {
    case 'SECURITY':
      return 'HIGH';
    case 'TRANSACTION':
    case 'POINTS':
      return 'MEDIUM';
    case 'SYSTEM':
    default:
      return 'LOW';
  }
};

// Función auxiliar para generar URLs de acción basadas en el tipo y entidad relacionada
const getActionUrl = (notification: BackendNotification): string | undefined => {
  switch (notification.type) {
    case 'POINTS':
      return '/points';
    case 'TRANSACTION':
      return notification.relatedEntityId ? `/transactions/${notification.relatedEntityId}` : '/transactions';
    case 'SECURITY':
      return '/settings/security';
    case 'SYSTEM':
      return '/settings';
    default:
      return undefined;
  }
};

// Función auxiliar para generar texto de acción
const getActionText = (type: NotificationType): string => {
  switch (type) {
    case 'POINTS':
      return 'Ver puntos';
    case 'TRANSACTION':
      return 'Ver detalles';
    case 'SECURITY':
      return 'Revisar seguridad';
    case 'SYSTEM':
      return 'Ver configuración';
    default:
      return 'Ver más';
  }
};

const NotificationsPage: React.FC = () => {
  const theme = useTheme();
  const dispatch = useDispatch<AppDispatch>();

  // Estados de Redux
  const { notifications: backendNotifications, loading, error, unreadCount } = useSelector(
    (state: RootState) => state.notification
  );

  // Convertir notificaciones del backend al formato del frontend
  const notifications: Notification[] = backendNotifications.map((notification: any) => ({
    ...notification,
    priority: getNotificationPriority(notification.type),
    actionUrl: getActionUrl(notification),
    actionText: getActionText(notification.type)
  }));

  // Estados locales
  const [selectedTab, setSelectedTab] = useState<number>(0);
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
    dispatch(fetchNotifications());
  }, [dispatch]);

  // Limpiar errores cuando se desmonta el componente
  useEffect(() => {
    return () => {
      if (error) {
        dispatch(clearNotificationError());
      }
    };
  }, [error, dispatch]);

  // Manejadores de eventos
  const handleTabChange = (_: React.SyntheticEvent, newValue: number) => {
    setSelectedTab(newValue);
  };

  const handleNotificationClick = (notification: Notification) => {
    // Si la notificación está sin leer, marcarla como leída
    if (!notification.read) {
      dispatch(markNotificationAsRead(notification.id));
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
    dispatch(markNotificationAsRead(id));
    handleMenuClose();
    showSnackbar('Notificación marcada como leída', 'success');
  };

  const handleMarkAllAsRead = () => {
    dispatch(markAllNotificationsAsRead());
    showSnackbar('Todas las notificaciones marcadas como leídas', 'success');
  };

  const handleDeleteNotification = (id: number) => {
    dispatch(deleteNotification(id));
    handleMenuClose();
    showSnackbar('Notificación eliminada', 'success');
  };

  const handleDeleteSelected = () => {
    // Eliminar cada notificación seleccionada individualmente
    selectedNotifications.forEach(id => {
      dispatch(deleteNotification(id));
    });
    setSelectedNotifications([]);
    setOpenDeleteDialog(false);
    showSnackbar(`${selectedNotifications.length} notificaciones eliminadas`, 'success');
  };

  const handleDeleteAllRead = () => {
    dispatch(deleteReadNotifications());
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
    dispatch(fetchNotifications());
    showSnackbar('Notificaciones actualizadas', 'success');
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
      filtered = filtered.filter(notification => !notification.read);
    } else if (selectedTab === 2) {
      filtered = filtered.filter(notification => notification.read);
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
  const displayUnreadCount = notifications.filter(notification => !notification.read).length;

  return (
    <Box>
      {/* Encabezado */}
      <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 3 }}>
        <Box sx={{ display: 'flex', alignItems: 'center' }}>
          <Badge badgeContent={displayUnreadCount} color="error" sx={{ mr: 2 }}>
            <NotificationIcon fontSize="large" />
          </Badge>
          <Typography variant="h4" component="h1">
            Notificaciones
          </Typography>
        </Box>

        <Box>
          <Tooltip title="Actualizar">
            <IconButton onClick={handleRefresh} disabled={loading}>
              {loading ? <CircularProgress size={24} /> : <RefreshIcon />}
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
                    {displayUnreadCount > 0 && (
                      <Badge
                        badgeContent={displayUnreadCount}
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
                    disabled={!notifications.some(n => !n.read)}
                  >
                    Marcar todas como leídas
                  </Button>
                  <Button
                    variant="outlined"
                    color="error"
                    startIcon={<DeleteIcon />}
                    onClick={handleOpenDeleteDialog}
                    size="small"
                    disabled={!notifications.some(n => n.read)}
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
        {loading ? (
          <Box sx={{ display: 'flex', justifyContent: 'center', p: 4 }}>
            <CircularProgress />
          </Box>
        ) : error ? (
          <Box sx={{ display: 'flex', flexDirection: 'column', alignItems: 'center', p: 4 }}>
            <ErrorIcon sx={{ fontSize: 48, color: theme.palette.error.main, mb: 2 }} />
            <Typography variant="body1" color="error" align="center">
              Error al cargar notificaciones
            </Typography>
            <Typography variant="body2" color="textSecondary" align="center" sx={{ mt: 1 }}>
              {error}
            </Typography>
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
                    bgcolor: !notification.read ? `${theme.palette.primary.main}08` : 'transparent',
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
                    <Avatar sx={{ bgcolor: getNotificationColor(notification.priority || 'LOW') }}>
                      {getNotificationIcon(notification.type, notification.priority || 'LOW')}
                    </Avatar>
                  </ListItemAvatar>
                  <ListItemText
                    primary={
                      <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                        <Typography
                          variant="subtitle1"
                          component="span"
                          sx={{
                            fontWeight: !notification.read ? 'bold' : 'normal',
                          }}
                        >
                          {notification.title}
                        </Typography>
                        {!notification.read && (
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
                            bgcolor: `${getNotificationColor(notification.priority || 'LOW')}20`,
                            color: getNotificationColor(notification.priority || 'LOW'),
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
        {selectedNotification && !selectedNotification.read && (
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
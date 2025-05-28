import React, { useState, useEffect } from 'react';
import {
  Popover,
  Box,
  Typography,
  List,
  ListItem,
  ListItemText,
  ListItemAvatar,
  Avatar,
  IconButton,
  Divider,
  Badge,
  Button,
  useTheme,
  Tooltip,
  CircularProgress
} from '@mui/material';
import {
  Notifications as NotificationIcon,
  Close as CloseIcon,
  CheckCircle as CheckCircleIcon,
  Error as ErrorIcon,
  Info as InfoIcon,
  Warning as WarningIcon,
  MonetizationOn as PointsIcon,
  AccountBalanceWallet as WalletIcon,
  Security as SecurityIcon,
  Settings as SettingsIcon,
  DoneAll as DoneAllIcon,
  NotificationsActive as NotificationsActiveIcon,
  NotificationsOff as NotificationsOffIcon
} from '@mui/icons-material';
import { useNavigate } from 'react-router-dom';
import { formatDistanceToNow } from 'date-fns';
import { es } from 'date-fns/locale';
import { useSelector, useDispatch } from 'react-redux';
import { RootState, AppDispatch } from '../store';
import {
  fetchUnreadNotifications,
  markNotificationAsRead,
  markAllNotificationsAsRead,
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

interface NotificationPopoverProps {
  anchorEl: HTMLElement | null;
  onClose: () => void;
}

const NotificationPopover: React.FC<NotificationPopoverProps> = ({ anchorEl, onClose }) => {
  const theme = useTheme();
  const navigate = useNavigate();
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

  // Efecto para cargar notificaciones cuando se abre el popover
  useEffect(() => {
    if (anchorEl) {
      dispatch(fetchUnreadNotifications());
    }
  }, [anchorEl, dispatch]);

  // Limpiar errores cuando se cierra el popover
  useEffect(() => {
    if (!anchorEl && error) {
      dispatch(clearNotificationError());
    }
  }, [anchorEl, error, dispatch]);

  // Manejadores de eventos
  const handleMarkAsRead = (id: number, e?: React.MouseEvent) => {
    if (e) {
      e.stopPropagation();
    }

    dispatch(markNotificationAsRead(id));
  };

  const handleMarkAllAsRead = () => {
    dispatch(markAllNotificationsAsRead());
  };

  const handleNotificationClick = (notification: Notification) => {
    // Marcar como leída
    handleMarkAsRead(notification.id);

    // Cerrar popover
    onClose();

    // Navegar a la URL de acción si existe
    if (notification.actionUrl) {
      navigate(notification.actionUrl);
    } else {
      // Si no hay URL específica, ir a la página de notificaciones
      navigate('/notifications');
    }
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

  const getTimeAgo = (dateString: string) => {
    const date = new Date(dateString);
    return formatDistanceToNow(date, { addSuffix: true, locale: es });
  };

  // Filtrar solo notificaciones no leídas
  const unreadNotifications = notifications.filter(notification => !notification.read);
  const displayUnreadCount = unreadNotifications.length;

  return (
    <Popover
      open={Boolean(anchorEl)}
      anchorEl={anchorEl}
      onClose={onClose}
      anchorOrigin={{
        vertical: 'bottom',
        horizontal: 'right',
      }}
      transformOrigin={{
        vertical: 'top',
        horizontal: 'right',
      }}
      PaperProps={{
        sx: {
          width: 360,
          maxHeight: 500,
          overflow: 'hidden',
          boxShadow: theme.shadows[8]
        }
      }}
    >
      <Box sx={{ p: 2, display: 'flex', justifyContent: 'space-between', alignItems: 'center', borderBottom: `1px solid ${theme.palette.divider}` }}>
        <Box sx={{ display: 'flex', alignItems: 'center' }}>
          <Badge badgeContent={displayUnreadCount} color="error" sx={{ mr: 1 }}>
            <NotificationIcon />
          </Badge>
          <Typography variant="h6">
            Notificaciones
          </Typography>
        </Box>
        <Box>
          {displayUnreadCount > 0 && (
            <Tooltip title="Marcar todas como leídas">
              <IconButton size="small" onClick={handleMarkAllAsRead} sx={{ mr: 1 }}>
                <DoneAllIcon fontSize="small" />
              </IconButton>
            </Tooltip>
          )}
          <Tooltip title="Cerrar">
            <IconButton size="small" onClick={onClose}>
              <CloseIcon fontSize="small" />
            </IconButton>
          </Tooltip>
        </Box>
      </Box>

      <Box sx={{ maxHeight: 400, overflow: 'auto' }}>
        {loading ? (
          <Box sx={{ display: 'flex', justifyContent: 'center', p: 4 }}>
            <CircularProgress size={32} />
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
        ) : unreadNotifications.length === 0 ? (
          <Box sx={{ display: 'flex', flexDirection: 'column', alignItems: 'center', p: 4 }}>
            <NotificationsOffIcon sx={{ fontSize: 48, color: theme.palette.grey[400], mb: 2 }} />
            <Typography variant="body1" color="textSecondary" align="center">
              No tienes notificaciones sin leer
            </Typography>
          </Box>
        ) : (
          <List sx={{ width: '100%', p: 0 }}>
            {unreadNotifications.map((notification, index) => (
              <React.Fragment key={notification.id}>
                <ListItem
                  alignItems="flex-start"
                  sx={{
                    py: 1.5,
                    px: 2,
                    '&:hover': {
                      bgcolor: `${theme.palette.primary.main}15`,
                    },
                    cursor: 'pointer',
                    transition: 'background-color 0.2s'
                  }}
                  onClick={() => handleNotificationClick(notification)}
                  secondaryAction={
                    <Tooltip title="Marcar como leída">
                      <IconButton
                        edge="end"
                        size="small"
                        onClick={(e) => handleMarkAsRead(notification.id, e)}
                      >
                        <CheckCircleIcon fontSize="small" />
                      </IconButton>
                    </Tooltip>
                  }
                >
                  <ListItemAvatar>
                    <Avatar sx={{ bgcolor: getNotificationColor(notification.priority || 'LOW') }}>
                      {getNotificationIcon(notification.type, notification.priority || 'LOW')}
                    </Avatar>
                  </ListItemAvatar>
                  <ListItemText
                    primary={
                      <Typography
                        variant="subtitle2"
                        component="span"
                        sx={{ fontWeight: 'bold' }}
                      >
                        {notification.title}
                      </Typography>
                    }
                    secondary={
                      <React.Fragment>
                        <Typography
                          component="span"
                          variant="body2"
                          color="text.primary"
                          sx={{
                            display: 'block',
                            mb: 0.5,
                            overflow: 'hidden',
                            textOverflow: 'ellipsis',
                            WebkitLineClamp: 2,
                            WebkitBoxOrient: 'vertical',
                            WebkitBoxDisplay: '-webkit-box'
                          }}
                        >
                          {notification.message}
                        </Typography>
                        <Typography
                          component="span"
                          variant="caption"
                          color="text.secondary"
                        >
                          {getTimeAgo(notification.createdAt)}
                        </Typography>
                      </React.Fragment>
                    }
                  />
                </ListItem>
                {index < unreadNotifications.length - 1 && <Divider variant="inset" component="li" />}
              </React.Fragment>
            ))}
          </List>
        )}
      </Box>

      <Box sx={{ p: 2, borderTop: `1px solid ${theme.palette.divider}`, textAlign: 'center' }}>
        <Button
          fullWidth
          onClick={() => {
            onClose();
            navigate('/notifications');
          }}
        >
          Ver todas las notificaciones
        </Button>
      </Box>
    </Popover>
  );
};

export default NotificationPopover;

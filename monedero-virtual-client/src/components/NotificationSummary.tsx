import React, { useEffect } from 'react';
import {
  Paper,
  Typography,
  Divider,
  Box,
  List,
  ListItem,
  ListItemText,
  ListItemIcon,
  Badge,
  Button,
  useTheme,
  alpha,
  CircularProgress
} from '@mui/material';
import {
  Notifications as NotificationsIcon,
  NotificationsActive as NotificationsActiveIcon,
  Security as SecurityIcon,
  Payment as PaymentIcon,
  EmojiEvents as PointsIcon,
  Info as InfoIcon
} from '@mui/icons-material';
import { useSelector, useDispatch } from 'react-redux';
import { RootState, AppDispatch } from '../store';
import { fetchNotifications } from '../store/slices/notificationSlice';

// Tipos para notificaciones (compatibles con el backend)
type NotificationType = 'TRANSACTION' | 'SECURITY' | 'POINTS' | 'SYSTEM';

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

interface NotificationSummaryProps {
  onViewAll: () => void;
}

const NotificationSummary: React.FC<NotificationSummaryProps> = ({
  onViewAll
}) => {
  const theme = useTheme();
  const dispatch = useDispatch<AppDispatch>();

  // Estados de Redux
  const { notifications: backendNotifications, loading, unreadCount } = useSelector(
    (state: RootState) => state.notification
  );

  // Cargar notificaciones al montar el componente
  useEffect(() => {
    dispatch(fetchNotifications());
  }, [dispatch]);

  // Convertir notificaciones del backend al formato compatible
  const notifications: BackendNotification[] = backendNotifications.map((notification: any) => ({
    ...notification
  }));

  // Mostrar solo las 3 notificaciones más recientes
  const recentNotifications = [...notifications]
    .sort((a, b) => new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime())
    .slice(0, 3);

  // Obtener el icono según el tipo de notificación
  const getNotificationIcon = (type: NotificationType) => {
    switch (type) {
      case 'TRANSACTION':
        return <PaymentIcon sx={{ color: theme.palette.primary.main }} />;
      case 'SECURITY':
        return <SecurityIcon sx={{ color: theme.palette.error.main }} />;
      case 'POINTS':
        return <PointsIcon sx={{ color: theme.palette.warning.main }} />;
      case 'SYSTEM':
        return <InfoIcon sx={{ color: theme.palette.info.main }} />;
      default:
        return <NotificationsIcon sx={{ color: theme.palette.text.secondary }} />;
    }
  };

  // Formatear fecha
  const formatDate = (dateString: string) => {
    const date = new Date(dateString);
    const now = new Date();
    const diffMs = now.getTime() - date.getTime();
    const diffMins = Math.floor(diffMs / 60000);
    const diffHours = Math.floor(diffMins / 60);
    const diffDays = Math.floor(diffHours / 24);

    if (diffMins < 60) {
      return `Hace ${diffMins} min`;
    } else if (diffHours < 24) {
      return `Hace ${diffHours} h`;
    } else if (diffDays < 7) {
      return `Hace ${diffDays} d`;
    } else {
      return date.toLocaleDateString();
    }
  };

  return (
    <Paper elevation={3} sx={{ p: 2 }}>
      <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 1 }}>
        <Typography variant="h6">
          Notificaciones
        </Typography>
        <Badge
          badgeContent={unreadCount}
          color="error"
          sx={{ '& .MuiBadge-badge': { fontSize: '0.7rem', height: 18, minWidth: 18 } }}
        >
          {unreadCount > 0 ?
            <NotificationsActiveIcon color="action" /> :
            <NotificationsIcon color="action" />
          }
        </Badge>
      </Box>
      <Divider sx={{ mb: 1 }} />

      {loading ? (
        <Box sx={{ display: 'flex', justifyContent: 'center', p: 2 }}>
          <CircularProgress size={24} />
        </Box>
      ) : recentNotifications.length > 0 ? (
        <List dense disablePadding>
          {recentNotifications.map((notification) => (
            <ListItem
              key={notification.id}
              alignItems="flex-start"
              sx={{
                py: 0.75,
                px: 0,
                borderLeft: notification.read ? 'none' : `3px solid ${theme.palette.primary.main}`,
                pl: notification.read ? 0 : 1,
                bgcolor: notification.read ? 'transparent' : alpha(theme.palette.primary.main, 0.05)
              }}
            >
              <ListItemIcon sx={{ minWidth: 36 }}>
                {getNotificationIcon(notification.type)}
              </ListItemIcon>
              <ListItemText
                primary={
                  <Typography
                    variant="body2"
                    sx={{
                      fontWeight: notification.read ? 'normal' : 'bold',
                      fontSize: '0.85rem',
                      lineHeight: 1.2,
                      mb: 0.5,
                      display: '-webkit-box',
                      overflow: 'hidden',
                      WebkitBoxOrient: 'vertical',
                      WebkitLineClamp: 2
                    }}
                  >
                    {notification.title}
                  </Typography>
                }
                secondary={
                  <Typography
                    variant="caption"
                    color="text.secondary"
                    sx={{ fontSize: '0.7rem' }}
                  >
                    {formatDate(notification.createdAt)}
                  </Typography>
                }
              />
            </ListItem>
          ))}
        </List>
      ) : (
        <Box sx={{ py: 2, textAlign: 'center' }}>
          <Typography variant="body2" color="text.secondary">
            No hay notificaciones recientes
          </Typography>
        </Box>
      )}

      <Box sx={{ mt: 1, textAlign: 'center' }}>
        <Button
          size="small"
          onClick={onViewAll}
          sx={{ fontSize: '0.8rem' }}
        >
          Ver todas ({notifications.length})
        </Button>
      </Box>
    </Paper>
  );
};

export default NotificationSummary;

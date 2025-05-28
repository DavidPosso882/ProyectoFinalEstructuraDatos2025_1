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
  Badge,
  IconButton,
  Tabs,
  Tab,
  Chip,
  Button,
  useTheme
} from '@mui/material';
import { 
  Notifications as NotificationsIcon,
  NotificationsActive as NotificationsActiveIcon,
  NotificationsOff as NotificationsOffIcon,
  CheckCircle as CheckCircleIcon,
  Delete as DeleteIcon,
  Warning as WarningIcon,
  Info as InfoIcon,
  MonetizationOn as MonetizationOnIcon,
  EmojiEvents as EmojiEventsIcon,
  Security as SecurityIcon
} from '@mui/icons-material';

interface Notification {
  id: number;
  message: string;
  read: boolean;
  createdAt: string;
  type: 'TRANSACTION' | 'SECURITY' | 'POINTS' | 'SYSTEM';
  relatedEntityId?: number;
}

interface NotificationCenterProps {
  notifications: Notification[];
  unreadCount: number;
  onMarkAsRead: (id: number) => void;
  onMarkAllAsRead: () => void;
  onDelete: (id: number) => void;
}

const NotificationCenter: React.FC<NotificationCenterProps> = ({
  notifications,
  unreadCount,
  onMarkAsRead,
  onMarkAllAsRead,
  onDelete
}) => {
  const theme = useTheme();
  const [tabValue, setTabValue] = useState(0);
  
  // Función para obtener el icono según el tipo de notificación
  const getNotificationIcon = (type: string, read: boolean) => {
    const iconColor = read ? 'action' : 'primary';
    
    switch (type) {
      case 'TRANSACTION':
        return <MonetizationOnIcon color={iconColor} />;
      case 'SECURITY':
        return <SecurityIcon color={read ? 'action' : 'error'} />;
      case 'POINTS':
        return <EmojiEventsIcon color={read ? 'action' : 'warning'} />;
      default:
        return <InfoIcon color={iconColor} />;
    }
  };
  
  // Filtrar notificaciones según la pestaña seleccionada
  const getFilteredNotifications = () => {
    switch (tabValue) {
      case 0: // Todas
        return notifications;
      case 1: // No leídas
        return notifications.filter(notification => !notification.read);
      case 2: // Transacciones
        return notifications.filter(notification => notification.type === 'TRANSACTION');
      case 3: // Seguridad
        return notifications.filter(notification => notification.type === 'SECURITY');
      case 4: // Puntos
        return notifications.filter(notification => notification.type === 'POINTS');
      default:
        return notifications;
    }
  };
  
  const filteredNotifications = getFilteredNotifications();
  
  // Formatear fecha
  const formatDate = (dateString: string) => {
    const date = new Date(dateString);
    const now = new Date();
    const diffMs = now.getTime() - date.getTime();
    const diffMins = Math.floor(diffMs / 60000);
    const diffHours = Math.floor(diffMins / 60);
    const diffDays = Math.floor(diffHours / 24);
    
    if (diffMins < 60) {
      return `Hace ${diffMins} ${diffMins === 1 ? 'minuto' : 'minutos'}`;
    } else if (diffHours < 24) {
      return `Hace ${diffHours} ${diffHours === 1 ? 'hora' : 'horas'}`;
    } else if (diffDays < 7) {
      return `Hace ${diffDays} ${diffDays === 1 ? 'día' : 'días'}`;
    } else {
      return date.toLocaleDateString();
    }
  };
  
  return (
    <Paper elevation={3} sx={{ mb: 3 }}>
      <Box sx={{ 
        p: 2, 
        display: 'flex', 
        justifyContent: 'space-between',
        alignItems: 'center',
        borderBottom: `1px solid ${theme.palette.divider}`
      }}>
        <Box sx={{ display: 'flex', alignItems: 'center' }}>
          <Badge badgeContent={unreadCount} color="error" sx={{ mr: 1 }}>
            <NotificationsIcon color="primary" />
          </Badge>
          <Typography variant="h6">
            Notificaciones
          </Typography>
        </Box>
        
        <Box>
          {unreadCount > 0 && (
            <Button 
              size="small" 
              startIcon={<CheckCircleIcon />}
              onClick={onMarkAllAsRead}
              sx={{ mr: 1 }}
            >
              Marcar todas como leídas
            </Button>
          )}
        </Box>
      </Box>
      
      <Tabs
        value={tabValue}
        onChange={(_, newValue) => setTabValue(newValue)}
        variant="scrollable"
        scrollButtons="auto"
        sx={{ px: 2, borderBottom: `1px solid ${theme.palette.divider}` }}
      >
        <Tab 
          label={
            <Box sx={{ display: 'flex', alignItems: 'center' }}>
              Todas
              <Chip 
                label={notifications.length} 
                size="small" 
                sx={{ ml: 1, height: 20, minWidth: 20 }} 
              />
            </Box>
          } 
        />
        <Tab 
          label={
            <Box sx={{ display: 'flex', alignItems: 'center' }}>
              No leídas
              {unreadCount > 0 && (
                <Chip 
                  label={unreadCount} 
                  size="small" 
                  color="error" 
                  sx={{ ml: 1, height: 20, minWidth: 20 }} 
                />
              )}
            </Box>
          } 
        />
        <Tab label="Transacciones" />
        <Tab label="Seguridad" />
        <Tab label="Puntos" />
      </Tabs>
      
      <List sx={{ maxHeight: 400, overflow: 'auto' }}>
        {filteredNotifications.length > 0 ? (
          filteredNotifications.map((notification, index) => (
            <React.Fragment key={notification.id}>
              {index > 0 && <Divider component="li" />}
              <ListItem
                alignItems="flex-start"
                sx={{
                  bgcolor: notification.read ? 'transparent' : `${theme.palette.primary.light}15`,
                  '&:hover': {
                    bgcolor: theme.palette.action.hover,
                  },
                }}
                secondaryAction={
                  <Box>
                    {!notification.read && (
                      <IconButton 
                        edge="end" 
                        aria-label="mark as read"
                        onClick={() => onMarkAsRead(notification.id)}
                        size="small"
                        sx={{ mr: 1 }}
                      >
                        <CheckCircleIcon fontSize="small" />
                      </IconButton>
                    )}
                    <IconButton 
                      edge="end" 
                      aria-label="delete"
                      onClick={() => onDelete(notification.id)}
                      size="small"
                    >
                      <DeleteIcon fontSize="small" />
                    </IconButton>
                  </Box>
                }
              >
                <ListItemIcon>
                  {getNotificationIcon(notification.type, notification.read)}
                </ListItemIcon>
                <ListItemText
                  primary={
                    <Box sx={{ pr: 6 }}>
                      {notification.message}
                    </Box>
                  }
                  secondary={
                    <Box sx={{ display: 'flex', alignItems: 'center', mt: 0.5 }}>
                      <Typography
                        variant="caption"
                        color="text.secondary"
                        component="span"
                      >
                        {formatDate(notification.createdAt)}
                      </Typography>
                      <Chip 
                        label={notification.type} 
                        size="small" 
                        sx={{ ml: 1, height: 20, fontSize: '0.7rem' }} 
                        color={
                          notification.type === 'SECURITY' 
                            ? 'error' 
                            : notification.type === 'POINTS' 
                              ? 'warning' 
                              : 'default'
                        }
                      />
                    </Box>
                  }
                />
              </ListItem>
            </React.Fragment>
          ))
        ) : (
          <Box sx={{ p: 3, textAlign: 'center' }}>
            <NotificationsOffIcon sx={{ fontSize: 40, color: theme.palette.text.secondary, mb: 1 }} />
            <Typography color="textSecondary">
              No hay notificaciones para mostrar
            </Typography>
          </Box>
        )}
      </List>
      
      {filteredNotifications.length > 0 && (
        <Box sx={{ p: 2, textAlign: 'center', borderTop: `1px solid ${theme.palette.divider}` }}>
          <Button 
            variant="outlined" 
            onClick={() => window.location.href = '/notifications'}
          >
            Ver todas las notificaciones
          </Button>
        </Box>
      )}
    </Paper>
  );
};

export default NotificationCenter;

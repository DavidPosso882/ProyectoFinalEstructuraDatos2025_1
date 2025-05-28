import { createSlice, createAsyncThunk, PayloadAction } from '@reduxjs/toolkit';
import ApiService from '../../services/api.service';
import { API_ENDPOINTS } from '../../utils/api-config';

interface Notification {
  id: number;
  userId: number;
  message: string;
  read: boolean;
  createdAt: string;
}

interface NotificationState {
  notifications: Notification[];
  unreadCount: number;
  loading: boolean;
  error: string | null;
}

const initialState: NotificationState = {
  notifications: [],
  unreadCount: 0,
  loading: false,
  error: null,
};

// Thunks
// Interfaz para la respuesta paginada del backend
interface PageResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
}

export const fetchNotifications = createAsyncThunk(
  'notification/fetchNotifications',
  async (_, { rejectWithValue }) => {
    try {
      const response = await ApiService.get<PageResponse<Notification>>(API_ENDPOINTS.NOTIFICATIONS.ALL);
      return response.content; // Extraer solo el contenido
    } catch (error: any) {
      return rejectWithValue(error.response?.data?.message || 'Error al obtener notificaciones');
    }
  }
);

export const fetchUnreadNotifications = createAsyncThunk(
  'notification/fetchUnreadNotifications',
  async (_, { rejectWithValue }) => {
    try {
      const response = await ApiService.get<PageResponse<Notification>>(API_ENDPOINTS.NOTIFICATIONS.UNREAD);
      return response.content; // Extraer solo el contenido
    } catch (error: any) {
      return rejectWithValue(error.response?.data?.message || 'Error al obtener notificaciones no leídas');
    }
  }
);

export const fetchUnreadNotificationsCount = createAsyncThunk(
  'notification/fetchUnreadNotificationsCount',
  async (_, { rejectWithValue }) => {
    try {
      const count = await ApiService.get<number>(API_ENDPOINTS.NOTIFICATIONS.UNREAD_COUNT);
      return count;
    } catch (error: any) {
      return rejectWithValue(error.response?.data?.message || 'Error al obtener contador de notificaciones no leídas');
    }
  }
);

export const markNotificationAsRead = createAsyncThunk(
  'notification/markNotificationAsRead',
  async (notificationId: number, { rejectWithValue }) => {
    try {
      await ApiService.put(API_ENDPOINTS.NOTIFICATIONS.MARK_READ(notificationId));
      return notificationId;
    } catch (error: any) {
      return rejectWithValue(error.response?.data?.message || 'Error al marcar notificación como leída');
    }
  }
);

export const markAllNotificationsAsRead = createAsyncThunk(
  'notification/markAllNotificationsAsRead',
  async (_, { rejectWithValue }) => {
    try {
      await ApiService.put(API_ENDPOINTS.NOTIFICATIONS.MARK_ALL_READ);
      return true;
    } catch (error: any) {
      return rejectWithValue(error.response?.data?.message || 'Error al marcar todas las notificaciones como leídas');
    }
  }
);

export const deleteNotification = createAsyncThunk(
  'notification/deleteNotification',
  async (notificationId: number, { rejectWithValue }) => {
    try {
      await ApiService.delete(API_ENDPOINTS.NOTIFICATIONS.DELETE(notificationId));
      return notificationId;
    } catch (error: any) {
      return rejectWithValue(error.response?.data?.message || 'Error al eliminar notificación');
    }
  }
);

export const deleteReadNotifications = createAsyncThunk(
  'notification/deleteReadNotifications',
  async (_, { getState, rejectWithValue, dispatch }) => {
    try {
      const state = getState() as { notification: NotificationState };
      const readNotifications = state.notification.notifications.filter(n => n.read);

      // Eliminar cada notificación leída individualmente
      const deletePromises = readNotifications.map(notification =>
        ApiService.delete(API_ENDPOINTS.NOTIFICATIONS.DELETE(notification.id))
      );

      await Promise.all(deletePromises);

      return readNotifications.map(n => n.id);
    } catch (error: any) {
      return rejectWithValue(error.response?.data?.message || 'Error al eliminar notificaciones leídas');
    }
  }
);

// Slice
const notificationSlice = createSlice({
  name: 'notification',
  initialState,
  reducers: {
    clearNotificationError: (state) => {
      state.error = null;
    },
  },
  extraReducers: (builder) => {
    // Fetch Notifications
    builder.addCase(fetchNotifications.pending, (state) => {
      state.loading = true;
      state.error = null;
    });
    builder.addCase(fetchNotifications.fulfilled, (state, action: PayloadAction<any>) => {
      state.loading = false;
      // Asegurarse de que notifications sea un array
      const notifications = Array.isArray(action.payload) ? action.payload : [];
      state.notifications = notifications;
      state.unreadCount = notifications.filter ?
        notifications.filter(notification => !notification.read).length : 0;
    });
    builder.addCase(fetchNotifications.rejected, (state, action) => {
      state.loading = false;
      state.error = action.payload as string;
    });

    // Fetch Unread Notifications
    builder.addCase(fetchUnreadNotifications.pending, (state) => {
      state.loading = true;
      state.error = null;
    });
    builder.addCase(fetchUnreadNotifications.fulfilled, (state, action: PayloadAction<any>) => {
      state.loading = false;
      // Asegurarse de que notifications sea un array
      const unreadNotifications = Array.isArray(action.payload) ? action.payload : [];
      state.unreadCount = unreadNotifications.length;

      // Para el popover, solo necesitamos las notificaciones no leídas
      state.notifications = unreadNotifications;
    });
    builder.addCase(fetchUnreadNotifications.rejected, (state, action) => {
      state.loading = false;
      state.error = action.payload as string;
    });

    // Fetch Unread Notifications Count (optimizado para polling)
    builder.addCase(fetchUnreadNotificationsCount.pending, (state) => {
      // No cambiar loading para que no interfiera con la UI
      state.error = null;
    });
    builder.addCase(fetchUnreadNotificationsCount.fulfilled, (state, action: PayloadAction<number>) => {
      // Solo actualizar el contador, no las notificaciones
      state.unreadCount = action.payload;
    });
    builder.addCase(fetchUnreadNotificationsCount.rejected, (state, action) => {
      // Solo registrar el error, no cambiar loading
      state.error = action.payload as string;
    });

    // Mark Notification as Read
    builder.addCase(markNotificationAsRead.pending, (state) => {
      state.loading = true;
      state.error = null;
    });
    builder.addCase(markNotificationAsRead.fulfilled, (state, action: PayloadAction<number>) => {
      state.loading = false;
      const notificationId = action.payload;
      if (Array.isArray(state.notifications)) {
        state.notifications = state.notifications.map(notification =>
          notification.id === notificationId
            ? { ...notification, read: true }
            : notification
        );
        state.unreadCount = state.notifications.filter(notification => !notification.read).length;
      }
    });
    builder.addCase(markNotificationAsRead.rejected, (state, action) => {
      state.loading = false;
      state.error = action.payload as string;
    });

    // Mark All Notifications as Read
    builder.addCase(markAllNotificationsAsRead.pending, (state) => {
      state.loading = true;
      state.error = null;
    });
    builder.addCase(markAllNotificationsAsRead.fulfilled, (state) => {
      state.loading = false;
      if (Array.isArray(state.notifications)) {
        state.notifications = state.notifications.map(notification => ({
          ...notification,
          read: true
        }));
        state.unreadCount = 0;
      }
    });
    builder.addCase(markAllNotificationsAsRead.rejected, (state, action) => {
      state.loading = false;
      state.error = action.payload as string;
    });

    // Delete Notification
    builder.addCase(deleteNotification.pending, (state) => {
      state.loading = true;
      state.error = null;
    });
    builder.addCase(deleteNotification.fulfilled, (state, action: PayloadAction<number>) => {
      state.loading = false;
      const notificationId = action.payload;
      if (Array.isArray(state.notifications)) {
        state.notifications = state.notifications.filter(notification => notification.id !== notificationId);
        state.unreadCount = state.notifications.filter(notification => !notification.read).length;
      }
    });
    builder.addCase(deleteNotification.rejected, (state, action) => {
      state.loading = false;
      state.error = action.payload as string;
    });

    // Delete Read Notifications
    builder.addCase(deleteReadNotifications.pending, (state) => {
      state.loading = true;
      state.error = null;
    });
    builder.addCase(deleteReadNotifications.fulfilled, (state, action: PayloadAction<number[]>) => {
      state.loading = false;
      const deletedIds = action.payload;
      if (Array.isArray(state.notifications)) {
        state.notifications = state.notifications.filter(notification => !deletedIds.includes(notification.id));
        state.unreadCount = state.notifications.filter(notification => !notification.read).length;
      }
    });
    builder.addCase(deleteReadNotifications.rejected, (state, action) => {
      state.loading = false;
      state.error = action.payload as string;
    });
  },
});

export const { clearNotificationError } = notificationSlice.actions;
export default notificationSlice.reducer;

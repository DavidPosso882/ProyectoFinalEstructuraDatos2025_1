// API Base URL con detección automática
export const API_BASE_URL = window.location.hostname === 'localhost'
  ? 'http://localhost:8080'
  : `http://${window.location.hostname}:8080`;

// API Endpoints
export const API_ENDPOINTS = {
  AUTH: {
    LOGIN: `${API_BASE_URL}/api/auth/signin`,  // Cambiado de 'login' a 'signin'
    REGISTER: `${API_BASE_URL}/api/auth/signup`,  // Cambiado de 'register' a 'signup'
    LOGOUT: `${API_BASE_URL}/api/auth/logout`,
    REFRESH_TOKEN: `${API_BASE_URL}/api/auth/refresh-token`,
    VERIFY_TOKEN: `${API_BASE_URL}/api/auth/verify-token`
  },

  USERS: {
    PROFILE: `${API_BASE_URL}/api/users/me`,  // Ajustado según el código del componente
    BY_ID: (id: number) => `${API_BASE_URL}/api/users/${id}`
  },

  WALLETS: {
    ALL: `${API_BASE_URL}/api/wallets`,
    BY_ID: (id: number) => `${API_BASE_URL}/api/wallets/${id}`,
    TRANSACTIONS: (id: number) => `${API_BASE_URL}/api/wallets/${id}/transactions`
  },

  TRANSACTIONS: {
    ALL: `${API_BASE_URL}/api/transactions`,
    BY_ID: (id: number) => `${API_BASE_URL}/api/transactions/${id}`,
    CREATE: `${API_BASE_URL}/api/transactions`
  },

  POINTS: {
    ACCOUNT: `${API_BASE_URL}/api/points/account`,
    HISTORY: `${API_BASE_URL}/api/points/transactions`,  // Actualizado para coincidir con el backend
    REDEEM: `${API_BASE_URL}/api/points/redeem`,
    BENEFITS: `${API_BASE_URL}/api/points/benefits`  // Añadido para obtener beneficios disponibles
  },

  NOTIFICATIONS: {
    ALL: `${API_BASE_URL}/api/notifications`,
    UNREAD: `${API_BASE_URL}/api/notifications/unread`,
    UNREAD_COUNT: `${API_BASE_URL}/api/notifications/unread/count`,
    BY_ID: (id: number) => `${API_BASE_URL}/api/notifications/${id}`,
    MARK_READ: (id: number) => `${API_BASE_URL}/api/notifications/${id}/read`,
    MARK_ALL_READ: `${API_BASE_URL}/api/notifications/read-all`,
    DELETE: (id: number) => `${API_BASE_URL}/api/notifications/${id}`
  }
};

/**
 * Utilidad para limpiar tokens antiguos y datos de autenticación
 * que podrían causar problemas al iniciar la aplicación
 */

// Lista de claves a verificar en localStorage
const AUTH_KEYS = ['token', 'user', 'auth'];

/**
 * Verifica si un token JWT es válido
 * @param token El token JWT a verificar
 * @returns true si el token parece válido, false en caso contrario
 */
const isValidJWT = (token: string): boolean => {
  // Un token JWT válido debe tener 3 partes separadas por puntos
  const parts = token.split('.');
  if (parts.length !== 3) {
    return false;
  }

  // Intentar decodificar la parte del payload
  try {
    const payload = JSON.parse(atob(parts[1]));
    
    // Verificar si el token ha expirado
    if (payload.exp && payload.exp * 1000 < Date.now()) {
      return false;
    }
    
    return true;
  } catch (e) {
    return false;
  }
};

/**
 * Limpia tokens antiguos o inválidos del localStorage
 */
export const cleanupAuthData = (): void => {
  // Verificar si hay un token almacenado
  const token = localStorage.getItem('token');
  
  // Si hay un token, verificar si es válido
  if (token) {
    if (!isValidJWT(token)) {
      console.log('Eliminando token JWT inválido del localStorage');
      localStorage.removeItem('token');
    }
  }
  
  // Buscar y eliminar otras claves de autenticación que podrían estar causando problemas
  Object.keys(localStorage).forEach(key => {
    // Si la clave contiene alguna de las palabras clave de autenticación
    if (AUTH_KEYS.some(authKey => key.toLowerCase().includes(authKey.toLowerCase()))) {
      // Y no es el token principal (que ya verificamos)
      if (key !== 'token') {
        try {
          const value = localStorage.getItem(key);
          if (value && value.includes('.') && !isValidJWT(value)) {
            console.log(`Eliminando clave de autenticación potencialmente problemática: ${key}`);
            localStorage.removeItem(key);
          }
        } catch (e) {
          // Si hay algún error al procesar, eliminar la clave por seguridad
          localStorage.removeItem(key);
        }
      }
    }
  });
};

export default cleanupAuthData;

import { createSlice, createAsyncThunk, PayloadAction } from '@reduxjs/toolkit';
import ApiService from '../../services/api.service';
import { API_ENDPOINTS } from '../../utils/api-config';
import { AuthResponse, LoginRequest, RegisterRequest, User, VerifyTokenRequest } from '../../types/auth.types';

interface AuthState {
  user: User | null;
  token: string | null;
  isAuthenticated: boolean;
  loading: boolean;
  error: string | null;
  registrationSuccess: boolean;
  twoFactorRequired: boolean;
  pendingUsername: string | null;
  maskedEmail: string | null;
}

const initialState: AuthState = {
  user: null,
  token: localStorage.getItem('token'),
  isAuthenticated: !!localStorage.getItem('token'),
  loading: false,
  error: null,
  registrationSuccess: false,
  twoFactorRequired: false,
  pendingUsername: null,
  maskedEmail: null,
};

// Thunks
export const login = createAsyncThunk(
  'auth/login',
  async (credentials: LoginRequest, { rejectWithValue }) => {
    try {
      const response = await ApiService.post<any>(API_ENDPOINTS.AUTH.LOGIN, credentials);
      
      // Check if 2FA is required
      if (response.message === 'Verification code required') {
        return { 
          twoFactorRequired: true, 
          username: credentials.username,
          maskedEmail: response.maskedEmail || null
        };
      }
      
      // If no 2FA, handle normal login
      localStorage.setItem('token', response.token);
      return response;
    } catch (error: any) {
      return rejectWithValue(error.response?.data?.message || 'Error al iniciar sesión');
    }
  }
);

export const verifyToken = createAsyncThunk(
  'auth/verifyToken',
  async (verifyData: VerifyTokenRequest, { rejectWithValue }) => {
    try {
      const response = await ApiService.post<AuthResponse>(API_ENDPOINTS.AUTH.VERIFY_TOKEN, verifyData);
      localStorage.setItem('token', response.token);
      return response;
    } catch (error: any) {
      return rejectWithValue(error.response?.data?.message || 'Error al verificar el código');
    }
  }
);

export const register = createAsyncThunk(
  'auth/register',
  async (userData: RegisterRequest, { rejectWithValue }) => {
    try {
      const response = await ApiService.post<{ message: string }>(API_ENDPOINTS.AUTH.REGISTER, userData);
      return response.message;
    } catch (error: any) {
      return rejectWithValue(error.response?.data?.message || 'Error al registrarse');
    }
  }
);

export const getUserProfile = createAsyncThunk(
  'auth/getUserProfile',
  async (_, { rejectWithValue }) => {
    try {
      const response = await ApiService.get<User>(API_ENDPOINTS.USERS.PROFILE);
      return response;
    } catch (error: any) {
      return rejectWithValue(error.response?.data?.message || 'Error al obtener perfil');
    }
  }
);

// Slice
const authSlice = createSlice({
  name: 'auth',
  initialState,
  reducers: {
    logout: (state) => {
      localStorage.removeItem('token');
      state.user = null;
      state.token = null;
      state.isAuthenticated = false;
      state.twoFactorRequired = false;
      state.pendingUsername = null;
      state.maskedEmail = null;
    },
    clearError: (state) => {
      state.error = null;
    },
    clearRegistrationSuccess: (state) => {
      state.registrationSuccess = false;
    },
    cancelTwoFactor: (state) => {
      state.twoFactorRequired = false;
      state.pendingUsername = null;
      state.maskedEmail = null;
      state.loading = false;
    },
  },
  extraReducers: (builder) => {
    // Login
    builder.addCase(login.pending, (state) => {
      state.loading = true;
      state.error = null;
    });
    builder.addCase(login.fulfilled, (state, action: PayloadAction<any>) => {
      state.loading = false;
      
      // Check if 2FA is required
      if (action.payload.twoFactorRequired) {
        state.twoFactorRequired = true;
        state.pendingUsername = action.payload.username;
        state.maskedEmail = action.payload.maskedEmail;
      } else {
        // Normal login flow
        state.isAuthenticated = true;
        state.token = action.payload.token;
        state.user = {
          id: action.payload.id,
          username: action.payload.username,
          email: action.payload.email,
          roles: action.payload.roles,
        };
        state.twoFactorRequired = false;
        state.pendingUsername = null;
        state.maskedEmail = null;
      }
    });
    builder.addCase(login.rejected, (state, action) => {
      state.loading = false;
      state.error = action.payload as string;
      state.twoFactorRequired = false;
      state.pendingUsername = null;
      state.maskedEmail = null;
    });
    
    // Verify Token
    builder.addCase(verifyToken.pending, (state) => {
      state.loading = true;
      state.error = null;
    });
    builder.addCase(verifyToken.fulfilled, (state, action: PayloadAction<AuthResponse>) => {
      state.loading = false;
      state.isAuthenticated = true;
      state.token = action.payload.token;
      state.user = {
        id: action.payload.id,
        username: action.payload.username,
        email: action.payload.email,
        roles: action.payload.roles,
      };
      state.twoFactorRequired = false;
      state.pendingUsername = null;
      state.maskedEmail = null;
    });
    builder.addCase(verifyToken.rejected, (state, action) => {
      state.loading = false;
      state.error = action.payload as string;
      // Don't reset twoFactorRequired here to allow retries
    });

    // Register
    builder.addCase(register.pending, (state) => {
      state.loading = true;
      state.error = null;
      state.registrationSuccess = false;
    });
    builder.addCase(register.fulfilled, (state) => {
      state.loading = false;
      state.registrationSuccess = true;
      // No establecemos isAuthenticated ni token aquí, ya que el usuario debe iniciar sesión después de registrarse
    });
    builder.addCase(register.rejected, (state, action) => {
      state.loading = false;
      state.error = action.payload as string;
      state.registrationSuccess = false;
    });

    // Get User Profile
    builder.addCase(getUserProfile.pending, (state) => {
      state.loading = true;
      state.error = null;
    });
    builder.addCase(getUserProfile.fulfilled, (state, action: PayloadAction<User>) => {
      state.loading = false;
      state.user = action.payload;
    });
    builder.addCase(getUserProfile.rejected, (state, action) => {
      state.loading = false;
      state.error = action.payload as string;
    });
  },
});

export const { logout, clearError, clearRegistrationSuccess, cancelTwoFactor } = authSlice.actions;
export default authSlice.reducer;

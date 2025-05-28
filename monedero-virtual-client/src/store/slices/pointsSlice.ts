import { createSlice, createAsyncThunk, PayloadAction } from '@reduxjs/toolkit';
import ApiService from '../../services/api.service';
import { API_ENDPOINTS } from '../../utils/api-config';
import { PointsAccount, PointsTransaction, RedeemPointsRequest, Benefit, AvailableBenefit, PointsRedemptionRequest } from '../../types/points.types';

interface PointsState {
  pointsAccount: PointsAccount | null;
  pointsHistory: PointsTransaction[];
  availableBenefits: AvailableBenefit[];
  loading: boolean;
  redeemLoading: boolean;
  error: string | null;
}

const initialState: PointsState = {
  pointsAccount: null,
  pointsHistory: [],
  availableBenefits: [],
  loading: false,
  redeemLoading: false,
  error: null,
};

// Thunks
export const fetchPointsAccount = createAsyncThunk(
  'points/fetchPointsAccount',
  async (_, { rejectWithValue }) => {
    try {
      const response = await ApiService.get<PointsAccount>(API_ENDPOINTS.POINTS.ACCOUNT);
      return response;
    } catch (error: any) {
      return rejectWithValue(error.response?.data?.message || 'Error al obtener cuenta de puntos');
    }
  }
);

export const fetchPointsHistory = createAsyncThunk(
  'points/fetchPointsHistory',
  async (_, { rejectWithValue }) => {
    try {
      const response = await ApiService.get<any>(API_ENDPOINTS.POINTS.HISTORY);
      // El backend devuelve un Page<PointsTransactionResponse>, extraer el array de 'content'
      return response.content || [];
    } catch (error: any) {
      return rejectWithValue(error.response?.data?.message || 'Error al obtener historial de puntos');
    }
  }
);

export const fetchAvailableBenefits = createAsyncThunk(
  'points/fetchAvailableBenefits',
  async (_, { rejectWithValue }) => {
    try {
      const response = await ApiService.get<any>(API_ENDPOINTS.POINTS.BENEFITS);
      return response;
    } catch (error: any) {
      return rejectWithValue(error.response?.data?.message || 'Error al obtener beneficios disponibles');
    }
  }
);

export const redeemPoints = createAsyncThunk(
  'points/redeemPoints',
  async (redeemData: PointsRedemptionRequest, { rejectWithValue }) => {
    try {
      const response = await ApiService.post<any>(API_ENDPOINTS.POINTS.REDEEM, redeemData);
      return response;
    } catch (error: any) {
      return rejectWithValue(error.response?.data?.message || 'Error al canjear puntos');
    }
  }
);

// Slice
const pointsSlice = createSlice({
  name: 'points',
  initialState,
  reducers: {
    clearPointsError: (state) => {
      state.error = null;
    },
  },
  extraReducers: (builder) => {
    // Fetch Points Account
    builder.addCase(fetchPointsAccount.pending, (state) => {
      state.loading = true;
      state.error = null;
    });
    builder.addCase(fetchPointsAccount.fulfilled, (state, action: PayloadAction<PointsAccount>) => {
      state.loading = false;
      state.pointsAccount = action.payload;
    });
    builder.addCase(fetchPointsAccount.rejected, (state, action) => {
      state.loading = false;
      state.error = action.payload as string;
    });

    // Fetch Points History
    builder.addCase(fetchPointsHistory.pending, (state) => {
      state.loading = true;
      state.error = null;
    });
    builder.addCase(fetchPointsHistory.fulfilled, (state, action: PayloadAction<PointsTransaction[]>) => {
      state.loading = false;
      state.pointsHistory = action.payload;
    });
    builder.addCase(fetchPointsHistory.rejected, (state, action) => {
      state.loading = false;
      state.error = action.payload as string;
    });

    // Fetch Available Benefits
    builder.addCase(fetchAvailableBenefits.pending, (state) => {
      state.loading = true;
      state.error = null;
    });
    builder.addCase(fetchAvailableBenefits.fulfilled, (state, action: PayloadAction<any>) => {
      state.loading = false;
      state.availableBenefits = action.payload.benefits || [];
    });
    builder.addCase(fetchAvailableBenefits.rejected, (state, action) => {
      state.loading = false;
      state.error = action.payload as string;
    });

    // Redeem Points
    builder.addCase(redeemPoints.pending, (state) => {
      state.redeemLoading = true;
      state.error = null;
    });
    builder.addCase(redeemPoints.fulfilled, (state, action: PayloadAction<any>) => {
      state.redeemLoading = false;
      // Después del canje exitoso, necesitamos recargar la cuenta de puntos
      // Esto se manejará en el componente
    });
    builder.addCase(redeemPoints.rejected, (state, action) => {
      state.redeemLoading = false;
      state.error = action.payload as string;
    });
  },
});

export const { clearPointsError } = pointsSlice.actions;
export default pointsSlice.reducer;

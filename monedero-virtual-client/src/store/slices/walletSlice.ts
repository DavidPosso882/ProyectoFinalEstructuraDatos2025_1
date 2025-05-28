import { createSlice, createAsyncThunk, PayloadAction } from '@reduxjs/toolkit';
import ApiService from '../../services/api.service';
import { API_ENDPOINTS } from '../../utils/api-config';
import { Wallet, CreateWalletRequest } from '../../types/wallet.types';

interface WalletState {
  wallets: Wallet[];
  currentWallet: Wallet | null;
  loading: boolean;
  error: string | null;
}

const initialState: WalletState = {
  wallets: [],
  currentWallet: null,
  loading: false,
  error: null,
};

// Thunks
export const fetchWallets = createAsyncThunk(
  'wallet/fetchWallets',
  async (_, { rejectWithValue }) => {
    try {
      const response = await ApiService.get<Wallet[]>(API_ENDPOINTS.WALLETS.ALL);
      return response;
    } catch (error: any) {
      return rejectWithValue(error.response?.data?.message || 'Error al obtener monederos');
    }
  }
);

export const fetchWalletById = createAsyncThunk(
  'wallet/fetchWalletById',
  async (walletId: number, { rejectWithValue }) => {
    try {
      const response = await ApiService.get<Wallet>(API_ENDPOINTS.WALLETS.BY_ID(walletId));
      return response;
    } catch (error: any) {
      return rejectWithValue(error.response?.data?.message || 'Error al obtener monedero');
    }
  }
);

export const createWallet = createAsyncThunk(
  'wallet/createWallet',
  async (walletData: CreateWalletRequest, { rejectWithValue }) => {
    try {
      const response = await ApiService.post<Wallet>(API_ENDPOINTS.WALLETS.ALL, walletData);
      return response;
    } catch (error: any) {
      return rejectWithValue(error.response?.data?.message || 'Error al crear monedero');
    }
  }
);

// Slice
const walletSlice = createSlice({
  name: 'wallet',
  initialState,
  reducers: {
    clearWalletError: (state) => {
      state.error = null;
    },
    setCurrentWallet: (state, action: PayloadAction<Wallet | null>) => {
      state.currentWallet = action.payload;
    },
  },
  extraReducers: (builder) => {
    // Fetch Wallets
    builder.addCase(fetchWallets.pending, (state) => {
      state.loading = true;
      state.error = null;
    });
    builder.addCase(fetchWallets.fulfilled, (state, action: PayloadAction<Wallet[]>) => {
      state.loading = false;
      state.wallets = action.payload;
    });
    builder.addCase(fetchWallets.rejected, (state, action) => {
      state.loading = false;
      state.error = action.payload as string;
    });

    // Fetch Wallet By Id
    builder.addCase(fetchWalletById.pending, (state) => {
      state.loading = true;
      state.error = null;
    });
    builder.addCase(fetchWalletById.fulfilled, (state, action: PayloadAction<Wallet>) => {
      state.loading = false;
      state.currentWallet = action.payload;
    });
    builder.addCase(fetchWalletById.rejected, (state, action) => {
      state.loading = false;
      state.error = action.payload as string;
    });

    // Create Wallet
    builder.addCase(createWallet.pending, (state) => {
      state.loading = true;
      state.error = null;
    });
    builder.addCase(createWallet.fulfilled, (state, action: PayloadAction<Wallet>) => {
      state.loading = false;
      state.wallets.push(action.payload);
    });
    builder.addCase(createWallet.rejected, (state, action) => {
      state.loading = false;
      state.error = action.payload as string;
    });
  },
});

export const { clearWalletError, setCurrentWallet } = walletSlice.actions;
export default walletSlice.reducer;

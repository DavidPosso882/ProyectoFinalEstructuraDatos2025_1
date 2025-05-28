import { createSlice, createAsyncThunk, PayloadAction } from '@reduxjs/toolkit';
import ApiService from '../../services/api.service';
import { API_ENDPOINTS } from '../../utils/api-config';
import { Transaction, TransactionRequest, TransactionResponse } from '../../types/transaction.types';

interface TransactionState {
  transactions: Transaction[];
  currentTransaction: Transaction | null;
  loading: boolean;
  error: string | null;
}

const initialState: TransactionState = {
  transactions: [],
  currentTransaction: null,
  loading: false,
  error: null,
};

// Función para mapear TransactionResponse del backend a Transaction del frontend
const mapBackendTransactionToFrontend = (backendTransaction: any): Transaction => {
  const mapped = {
    id: backendTransaction.id,
    amount: backendTransaction.amount,
    description: backendTransaction.description,
    transactionType: backendTransaction.type, // Backend usa 'type', frontend usa 'transactionType'
    status: backendTransaction.status,
    sourceWalletId: backendTransaction.sourceWallet?.id,
    destinationWalletId: backendTransaction.targetWallet?.id,
    createdAt: backendTransaction.transactionDate, // Backend usa 'transactionDate', frontend usa 'createdAt'
    updatedAt: backendTransaction.transactionDate, // Usar la misma fecha
    pointsEarned: backendTransaction.pointsEarned,
    reversed: backendTransaction.reversed,
    referenceId: backendTransaction.referenceId,
    sourceWallet: backendTransaction.sourceWallet ? {
      id: backendTransaction.sourceWallet.id,
      name: backendTransaction.sourceWallet.name,
      user: backendTransaction.sourceWallet.user ? {
        id: backendTransaction.sourceWallet.user.id,
        username: backendTransaction.sourceWallet.user.username,
      } : { id: 0, username: 'Unknown' }
    } : undefined,
    targetWallet: backendTransaction.targetWallet ? {
      id: backendTransaction.targetWallet.id,
      name: backendTransaction.targetWallet.name,
      user: backendTransaction.targetWallet.user ? {
        id: backendTransaction.targetWallet.user.id,
        username: backendTransaction.targetWallet.user.username,
      } : { id: 0, username: 'Unknown' }
    } : undefined,
  };

  return mapped;
};

// Thunks
export const fetchTransactions = createAsyncThunk(
  'transaction/fetchTransactions',
  async (params: { page?: number; size?: number } = {}, { rejectWithValue }) => {
    try {
      const { page = 0, size = 20 } = params;
      // El backend devuelve un Page<TransactionResponse>
      const response = await ApiService.get<any>(`${API_ENDPOINTS.TRANSACTIONS.ALL}?page=${page}&size=${size}`);
      // Extraer el array de transacciones desde 'content'
      return response.content || [];
    } catch (error: any) {
      return rejectWithValue(error.response?.data?.message || 'Error al obtener transacciones');
    }
  }
);

export const fetchWalletTransactions = createAsyncThunk(
  'transaction/fetchWalletTransactions',
  async (walletId: number, { rejectWithValue }) => {
    try {
      const response = await ApiService.get<Transaction[]>(API_ENDPOINTS.WALLETS.TRANSACTIONS(walletId));
      return response;
    } catch (error: any) {
      return rejectWithValue(error.response?.data?.message || 'Error al obtener transacciones del monedero');
    }
  }
);

export const createTransaction = createAsyncThunk(
  'transaction/createTransaction',
  async (transactionData: TransactionRequest, { rejectWithValue }) => {
    try {
      const response = await ApiService.post<TransactionResponse>(API_ENDPOINTS.TRANSACTIONS.CREATE, transactionData);
      return response;
    } catch (error: any) {
      return rejectWithValue(error.response?.data?.message || 'Error al crear transacción');
    }
  }
);

// Slice
const transactionSlice = createSlice({
  name: 'transaction',
  initialState,
  reducers: {
    clearTransactionError: (state) => {
      state.error = null;
    },
    setCurrentTransaction: (state, action: PayloadAction<Transaction | null>) => {
      state.currentTransaction = action.payload;
    },
  },
  extraReducers: (builder) => {
    // Fetch Transactions
    builder.addCase(fetchTransactions.pending, (state) => {
      state.loading = true;
      state.error = null;
    });
    builder.addCase(fetchTransactions.fulfilled, (state, action: PayloadAction<any>) => {
      state.loading = false;
      // Mapear los datos del backend al formato del frontend
      const backendTransactions = Array.isArray(action.payload) ? action.payload : [];
      state.transactions = backendTransactions.map(mapBackendTransactionToFrontend);
    });
    builder.addCase(fetchTransactions.rejected, (state, action) => {
      state.loading = false;
      state.error = action.payload as string;
    });

    // Fetch Wallet Transactions
    builder.addCase(fetchWalletTransactions.pending, (state) => {
      state.loading = true;
      state.error = null;
    });
    builder.addCase(fetchWalletTransactions.fulfilled, (state, action: PayloadAction<any>) => {
      state.loading = false;
      // Mapear los datos del backend al formato del frontend
      const backendTransactions = Array.isArray(action.payload) ? action.payload : [];
      state.transactions = backendTransactions.map(mapBackendTransactionToFrontend);
    });
    builder.addCase(fetchWalletTransactions.rejected, (state, action) => {
      state.loading = false;
      state.error = action.payload as string;
    });

    // Create Transaction
    builder.addCase(createTransaction.pending, (state) => {
      state.loading = true;
      state.error = null;
    });
    builder.addCase(createTransaction.fulfilled, (state, action: PayloadAction<any>) => {
      state.loading = false;
      const mappedTransaction = mapBackendTransactionToFrontend(action.payload);
      state.transactions.unshift(mappedTransaction);
      state.currentTransaction = mappedTransaction;
    });
    builder.addCase(createTransaction.rejected, (state, action) => {
      state.loading = false;
      state.error = action.payload as string;
    });
  },
});

export const { clearTransactionError, setCurrentTransaction } = transactionSlice.actions;
export default transactionSlice.reducer;

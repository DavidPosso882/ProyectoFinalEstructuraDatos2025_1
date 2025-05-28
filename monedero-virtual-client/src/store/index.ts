import { configureStore } from '@reduxjs/toolkit';
import authReducer from './slices/authSlice';
import walletReducer from './slices/walletSlice';
import transactionReducer from './slices/transactionSlice';
import pointsReducer from './slices/pointsSlice';
import notificationReducer from './slices/notificationSlice';

export const store = configureStore({
  reducer: {
    auth: authReducer,
    wallet: walletReducer,
    transaction: transactionReducer,
    points: pointsReducer,
    notification: notificationReducer,
  },
});

export type RootState = ReturnType<typeof store.getState>;
export type AppDispatch = typeof store.dispatch;

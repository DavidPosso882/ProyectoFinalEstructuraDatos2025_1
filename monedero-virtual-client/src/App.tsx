import React, { useEffect } from 'react';
import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import { useDispatch, useSelector } from 'react-redux';
import { AppDispatch, RootState } from './store';
import { getUserProfile } from './store/slices/authSlice';

// Tema personalizado
import { ThemeProvider } from './theme/ThemeContext';

// Páginas
import Login from './pages/Login';
import Register from './pages/Register';
import Dashboard from './pages/Dashboard';
import WalletsPage from './pages/WalletsPage';
import NotificationsPage from './pages/NotificationsPage';
import SpendingAnalysis from './pages/SpendingAnalysis';
import ScheduledTransactionsPage from './pages/ScheduledTransactionsPage';
import CreateScheduledTransactionPage from './pages/CreateScheduledTransactionPage';
import TransactionsPage from './pages/TransactionsPage';
import PointsPage from './pages/PointsPage';
import DataStructuresDemo from './pages/DataStructuresDemo';
// Importa otras páginas cuando las crees

// Componentes
import MainLayout from './components/layout/MainLayout';

// Componente para rutas protegidas
const ProtectedRoute: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const { isAuthenticated, loading } = useSelector((state: RootState) => state.auth);
  
  if (loading) {
    return <div>Cargando...</div>;
  }
  
  if (!isAuthenticated) {
    return <Navigate to="/login" />;
  }
  
  return <>{children}</>;
};

const App: React.FC = () => {
  const dispatch = useDispatch<AppDispatch>();
  const { isAuthenticated } = useSelector((state: RootState) => state.auth);
  
  useEffect(() => {
    // Si el usuario está autenticado, obtener su perfil
    if (isAuthenticated) {
      dispatch(getUserProfile());
    }
  }, [isAuthenticated, dispatch]);
  
  return (
    <ThemeProvider>
      <Router>
        <Routes>
          {/* Rutas públicas */}
          <Route path="/login" element={<Login />} />
          <Route path="/register" element={<Register />} />
          <Route path="/" element={<Navigate to="/login" />} />
          
          {/* Rutas protegidas */}
          <Route path="/dashboard" element={
            <ProtectedRoute>
              <MainLayout>
                <Dashboard />
              </MainLayout>
            </ProtectedRoute>
          } />
          
          <Route path="/wallets" element={
            <ProtectedRoute>
              <MainLayout>
                <WalletsPage />
              </MainLayout>
            </ProtectedRoute>
          } />
          
          <Route path="/wallets/:id" element={
            <ProtectedRoute>
              <MainLayout>
                <WalletsPage />
              </MainLayout>
            </ProtectedRoute>
          } />
          
          <Route path="/notifications" element={
            <ProtectedRoute>
              <MainLayout>
                <NotificationsPage />
              </MainLayout>
            </ProtectedRoute>
          } />
          
          <Route path="/transactions" element={
            <ProtectedRoute>
              <MainLayout>
                <TransactionsPage />
              </MainLayout>
            </ProtectedRoute>
          } />
          
          <Route path="/analysis" element={
            <ProtectedRoute>
              <MainLayout>
                <SpendingAnalysis />
              </MainLayout>
            </ProtectedRoute>
          } />
          
          <Route path="/scheduled-transactions" element={
            <ProtectedRoute>
              <MainLayout>
                <ScheduledTransactionsPage />
              </MainLayout>
            </ProtectedRoute>
          } />
          
          <Route path="/scheduled-transactions/create" element={
            <ProtectedRoute>
              <MainLayout>
                <CreateScheduledTransactionPage />
              </MainLayout>
            </ProtectedRoute>
          } />
          
          <Route path="/points" element={
            <ProtectedRoute>
              <MainLayout>
                <PointsPage />
              </MainLayout>
            </ProtectedRoute>
          } />
          
          <Route path="/profile" element={
            <ProtectedRoute>
              <MainLayout>
                <div>Perfil (Implementar)</div>
              </MainLayout>
            </ProtectedRoute>
          } />
          
          <Route path="/settings" element={
            <ProtectedRoute>
              <MainLayout>
                <div>Configuración (Implementar)</div>
              </MainLayout>
            </ProtectedRoute>
          } />
          
          <Route path="/datastructures-demo" element={<DataStructuresDemo />} />
          
          {/* Ruta para manejar rutas no encontradas */}
          <Route path="*" element={<Navigate to="/login" />} />
        </Routes>
      </Router>
    </ThemeProvider>
  );
};

export default App;

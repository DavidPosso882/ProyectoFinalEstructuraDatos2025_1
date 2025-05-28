import React, { useState, useEffect } from 'react';
import {
  Container,
  Typography,
  Box,
  Paper,
  Breadcrumbs,
  Link,
  CircularProgress,
  Alert
} from '@mui/material';
import { Link as RouterLink, useNavigate } from 'react-router-dom';

// Servicios y componentes
import WalletService from '../services/wallet.service';
import ScheduledTransactionForm from '../components/forms/ScheduledTransactionForm';
import { Wallet } from '../types/wallet.types';

// Componente principal
const CreateScheduledTransactionPage: React.FC = () => {
  const navigate = useNavigate();
  
  // Estados
  const [wallets, setWallets] = useState<Wallet[]>([]);
  const [loading, setLoading] = useState<boolean>(true);
  const [error, setError] = useState<string | null>(null);
  
  // Cargar monederos
  useEffect(() => {
    const loadWallets = async () => {
      try {
        setLoading(true);
        const data = await WalletService.getUserWallets();
        setWallets(data);
        setError(null);
      } catch (err) {
        console.error('Error al cargar monederos:', err);
        setError('No se pudieron cargar los monederos. Por favor, intente nuevamente.');
      } finally {
        setLoading(false);
      }
    };
    
    loadWallets();
  }, []);
  
  // Manejador de éxito
  const handleSuccess = () => {
    // Navegar a la página de transacciones programadas
    navigate('/scheduled-transactions');
  };
  
  // Manejador de cancelación
  const handleCancel = () => {
    // Navegar a la página de transacciones programadas
    navigate('/scheduled-transactions');
  };
  
  return (
    <Container maxWidth="lg" sx={{ mt: 4, mb: 4 }}>
      <Box sx={{ mb: 3 }}>
        <Breadcrumbs aria-label="breadcrumb">
          <Link component={RouterLink} to="/dashboard" color="inherit">
            Dashboard
          </Link>
          <Link component={RouterLink} to="/scheduled-transactions" color="inherit">
            Transacciones Programadas
          </Link>
          <Typography color="text.primary">Nueva Transacción</Typography>
        </Breadcrumbs>
      </Box>
      
      <Typography variant="h4" gutterBottom>
        Programar Nueva Transacción
      </Typography>
      
      {loading ? (
        <Box sx={{ display: 'flex', justifyContent: 'center', py: 4 }}>
          <CircularProgress />
        </Box>
      ) : error ? (
        <Alert severity="error" sx={{ mb: 3 }}>
          {error}
        </Alert>
      ) : (
        <ScheduledTransactionForm
          wallets={wallets}
          onSuccess={handleSuccess}
          onCancel={handleCancel}
        />
      )}
    </Container>
  );
};

export default CreateScheduledTransactionPage;

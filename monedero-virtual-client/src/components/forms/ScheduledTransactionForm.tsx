import React, { useState, useEffect } from 'react';
import {
  Box,
  TextField,
  Button,
  FormControl,
  InputLabel,
  Select,
  MenuItem,
  FormHelperText,
  Grid,
  Typography,
  Paper,
  Divider,
  Alert,
  CircularProgress,
  useTheme,
  SelectChangeEvent
} from '@mui/material';
import { addDays, format } from 'date-fns';

// Servicios y tipos
import ScheduledTransactionService from '../../services/scheduledTransaction.service';
import {
  ScheduledTransactionRequest,
  RecurrenceType,
  RecurrenceTypeLabels
} from '../../types/scheduledTransaction.types';
import { TransactionType } from '../../types/transaction.types';
import { Wallet } from '../../types/wallet.types';

// Props del componente
interface ScheduledTransactionFormProps {
  wallets: Wallet[];
  onSuccess?: () => void;
  onCancel?: () => void;
}

// Componente para crear transacciones programadas
const ScheduledTransactionForm: React.FC<ScheduledTransactionFormProps> = ({
  wallets,
  onSuccess,
  onCancel
}) => {
  const theme = useTheme();

  // Estado del formulario
  const [formData, setFormData] = useState<ScheduledTransactionRequest>({
    type: TransactionType.TRANSFER,
    amount: 0,
    scheduledDate: addDays(new Date(), 1).toISOString(),
    description: '',
    walletId: 0,
    targetWalletId: undefined,
    recurrenceType: RecurrenceType.ONCE
  });

  // Estados de UI
  const [loading, setLoading] = useState<boolean>(false);
  const [error, setError] = useState<string | null>(null);
  const [success, setSuccess] = useState<boolean>(false);
  const [formErrors, setFormErrors] = useState<Record<string, string>>({});

  // Nuevo estado para ID manual
  const [manualTargetId, setManualTargetId] = useState<string>('');

  // Establecer el monedero por defecto cuando se cargan los monederos
  useEffect(() => {
    if (wallets.length > 0 && formData.walletId === 0) {
      setFormData(prev => ({
        ...prev,
        walletId: wallets[0].id
      }));
    }
  }, [wallets, formData.walletId]);

  // Manejador de cambios en campos de texto
  const handleChange = (e: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement>) => {
    const { name, value } = e.target;
    setFormData(prev => ({
      ...prev,
      [name]: name === 'amount' ? parseFloat(value) || 0 : value
    }));

    // Limpiar error del campo
    if (formErrors[name]) {
      setFormErrors(prev => ({
        ...prev,
        [name]: ''
      }));
    }
  };

  // Manejador de cambios en selects
  const handleSelectChange = (e: SelectChangeEvent<any>) => {
    const { name, value } = e.target;
    setFormData(prev => ({
      ...prev,
      [name]: value
    }));

    // Limpiar error del campo
    if (formErrors[name]) {
      setFormErrors(prev => ({
        ...prev,
        [name]: ''
      }));
    }
  };

  // Manejador de cambios en fecha
  const handleDateChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const { value } = e.target;
    try {
      if (value) {
        // Crear fecha local sin conversión a UTC
        const localDate = new Date(value);
        if (!isNaN(localDate.getTime())) {
          // Formatear como ISO string pero manteniendo la hora local
          const year = localDate.getFullYear();
          const month = String(localDate.getMonth() + 1).padStart(2, '0');
          const day = String(localDate.getDate()).padStart(2, '0');
          const hours = String(localDate.getHours()).padStart(2, '0');
          const minutes = String(localDate.getMinutes()).padStart(2, '0');
          const seconds = String(localDate.getSeconds()).padStart(2, '0');

          const localISOString = `${year}-${month}-${day}T${hours}:${minutes}:${seconds}`;

          setFormData(prev => ({
            ...prev,
            scheduledDate: localISOString
          }));

          // Limpiar error del campo
          if (formErrors.scheduledDate) {
            setFormErrors(prev => ({
              ...prev,
              scheduledDate: ''
            }));
          }
        }
      }
    } catch (err) {
      console.error('Error parsing date:', err);
    }
  };

  // Validar formulario
  const validateForm = (): boolean => {
    const errors: Record<string, string> = {};

    if (formData.amount <= 0) {
      errors.amount = 'El monto debe ser mayor a 0';
    }

    if (!formData.walletId) {
      errors.walletId = 'Debe seleccionar un monedero';
    }

    if (formData.type === TransactionType.TRANSFER && !formData.targetWalletId) {
      errors.targetWalletId = 'Debe seleccionar un monedero destino';
    }

    if (formData.type === TransactionType.TRANSFER && formData.targetWalletId === formData.walletId) {
      errors.targetWalletId = 'El monedero destino debe ser diferente al origen';
    }

    const scheduledDate = new Date(formData.scheduledDate);
    if (scheduledDate <= new Date()) {
      errors.scheduledDate = 'La fecha debe ser futura';
    }

    setFormErrors(errors);
    return Object.keys(errors).length === 0;
  };

  // Enviar formulario
  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();

    if (!validateForm()) {
      return;
    }

    setLoading(true);
    setError(null);

    try {
      await ScheduledTransactionService.createScheduledTransaction(formData);
      setSuccess(true);

      // Llamar al callback de éxito si existe
      if (onSuccess) {
        onSuccess();
      }
    } catch (err: any) {
      console.error('Error al crear transacción programada:', err);
      setError(err.message || 'No se pudo crear la transacción programada');
    } finally {
      setLoading(false);
    }
  };

  // Obtener el saldo del monedero seleccionado
  const getSelectedWalletBalance = (): number => {
    const wallet = wallets.find(w => w.id === formData.walletId);
    return wallet ? wallet.balance : 0;
  };

  return (
    <Paper elevation={3} sx={{ p: 3 }}>
      <Typography variant="h6" gutterBottom>
        Programar Nueva Transacción
      </Typography>
      <Divider sx={{ mb: 3 }} />

      {success ? (
        <Box sx={{ textAlign: 'center', py: 2 }}>
          <Alert severity="success" sx={{ mb: 2 }}>
            Transacción programada creada exitosamente
          </Alert>
          <Button
            variant="contained"
            color="primary"
            onClick={() => {
              setSuccess(false);
              setFormData({
                type: TransactionType.TRANSFER,
                amount: 0,
                scheduledDate: addDays(new Date(), 1).toISOString(),
                description: '',
                walletId: wallets.length > 0 ? wallets[0].id : 0,
                targetWalletId: undefined,
                recurrenceType: RecurrenceType.ONCE
              });
            }}
            sx={{ mr: 1 }}
          >
            Crear otra
          </Button>
          {onCancel && (
            <Button
              variant="outlined"
              onClick={onCancel}
            >
              Volver
            </Button>
          )}
        </Box>
      ) : (
        <form onSubmit={handleSubmit}>
          {error && (
            <Alert severity="error" sx={{ mb: 2 }}>
              {error}
            </Alert>
          )}

          <Grid container spacing={2}>
            <Grid item xs={12} sm={6}>
              <FormControl fullWidth error={!!formErrors.type}>
                <InputLabel>Tipo de Transacción</InputLabel>
                <Select
                  name="type"
                  value={formData.type}
                  onChange={handleSelectChange}
                  label="Tipo de Transacción"
                >
                  <MenuItem value={TransactionType.DEPOSIT}>Depósito</MenuItem>
                  <MenuItem value={TransactionType.WITHDRAWAL}>Retiro</MenuItem>
                  <MenuItem value={TransactionType.TRANSFER}>Transferencia</MenuItem>
                </Select>
                {formErrors.type && (
                  <FormHelperText>{formErrors.type}</FormHelperText>
                )}
              </FormControl>
            </Grid>

            <Grid item xs={12} sm={6}>
              <FormControl fullWidth error={!!formErrors.recurrenceType}>
                <InputLabel>Recurrencia</InputLabel>
                <Select
                  name="recurrenceType"
                  value={formData.recurrenceType || RecurrenceType.ONCE}
                  onChange={handleSelectChange}
                  label="Recurrencia"
                >
                  {Object.entries(RecurrenceTypeLabels).map(([key, label]) => (
                    <MenuItem key={key} value={key}>
                      {label}
                    </MenuItem>
                  ))}
                </Select>
                {formErrors.recurrenceType && (
                  <FormHelperText>{formErrors.recurrenceType}</FormHelperText>
                )}
              </FormControl>
            </Grid>

            <Grid item xs={12} sm={6}>
              <FormControl fullWidth error={!!formErrors.walletId}>
                <InputLabel>Monedero Origen</InputLabel>
                <Select
                  name="walletId"
                  value={formData.walletId || ''}
                  onChange={handleSelectChange}
                  label="Monedero Origen"
                >
                  {wallets.map(wallet => (
                    <MenuItem key={wallet.id} value={wallet.id}>
                      {wallet.name} (${wallet.balance.toFixed(2)})
                    </MenuItem>
                  ))}
                </Select>
                {formErrors.walletId && (
                  <FormHelperText>{formErrors.walletId}</FormHelperText>
                )}
              </FormControl>
            </Grid>

            {formData.type === TransactionType.TRANSFER && (
              <Grid item xs={12} sm={6}>
                <FormControl fullWidth sx={{ mb: 2 }}>
                  <InputLabel id="target-wallet-label">Monedero destino</InputLabel>
                  <Select
                    labelId="target-wallet-label"
                    name="targetWalletId"
                    value={formData.targetWalletId || ''}
                    label="Monedero destino"
                    onChange={e => {
                      setFormData(prev => ({
                        ...prev,
                        targetWalletId: e.target.value ? Number(e.target.value) : undefined
                      }));
                      setManualTargetId('');
                    }}
                  >
                    <MenuItem value="">Seleccionar...</MenuItem>
                    {wallets.filter(w => w.id !== formData.walletId).map(wallet => (
                      <MenuItem key={wallet.id} value={wallet.id}>{wallet.name} (ID: {wallet.id})</MenuItem>
                    ))}
                  </Select>
                  <FormHelperText>O ingresa el ID manualmente:</FormHelperText>
                  <TextField
                    label="ID de monedero destino"
                    type="number"
                    value={manualTargetId}
                    onChange={e => {
                      setManualTargetId(e.target.value);
                      setFormData(prev => ({
                        ...prev,
                        targetWalletId: e.target.value ? Number(e.target.value) : undefined
                      }));
                    }}
                    sx={{ mt: 1 }}
                  />
                  {formErrors.targetWalletId && (
                    <FormHelperText error>{formErrors.targetWalletId}</FormHelperText>
                  )}
                </FormControl>
              </Grid>
            )}

            <Grid item xs={12} sm={formData.type === TransactionType.TRANSFER ? 12 : 6}>
              <TextField
                fullWidth
                label="Monto"
                name="amount"
                type="number"
                value={formData.amount || ''}
                onChange={handleChange}
                error={!!formErrors.amount}
                helperText={formErrors.amount || `Saldo disponible: $${getSelectedWalletBalance().toFixed(2)}`}
                InputProps={{
                  startAdornment: <Box component="span" sx={{ mr: 1 }}>$</Box>
                }}
              />
            </Grid>

            <Grid item xs={12}>
              <TextField
                fullWidth
                label="Fecha y hora programada"
                type="datetime-local"
                name="scheduledDate"
                value={format(new Date(formData.scheduledDate), "yyyy-MM-dd'T'HH:mm")}
                onChange={handleDateChange}
                error={!!formErrors.scheduledDate}
                helperText={formErrors.scheduledDate || 'Seleccione fecha y hora futura'}
                InputLabelProps={{ shrink: true }}
                inputProps={{ min: format(new Date(), "yyyy-MM-dd'T'HH:mm") }}
              />
            </Grid>

            <Grid item xs={12}>
              <TextField
                fullWidth
                label="Descripción (opcional)"
                name="description"
                value={formData.description || ''}
                onChange={handleChange}
                multiline
                rows={2}
              />
            </Grid>
          </Grid>

          <Box sx={{ mt: 3, display: 'flex', justifyContent: 'flex-end' }}>
            {onCancel && (
              <Button
                variant="outlined"
                onClick={onCancel}
                sx={{ mr: 1 }}
                disabled={loading}
              >
                Cancelar
              </Button>
            )}
            <Button
              type="submit"
              variant="contained"
              color="primary"
              disabled={loading}
              startIcon={loading ? <CircularProgress size={20} /> : undefined}
            >
              {loading ? 'Guardando...' : 'Programar Transacción'}
            </Button>
          </Box>
        </form>
      )}
    </Paper>
  );
};

export default ScheduledTransactionForm;

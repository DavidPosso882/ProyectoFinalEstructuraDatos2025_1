import React, { useState, useEffect } from 'react';
import { 
  Container, 
  Box, 
  Typography, 
  TextField, 
  Button, 
  Paper, 
  Alert,
  CircularProgress,
  InputAdornment,
  IconButton
} from '@mui/material';
import { Visibility, VisibilityOff } from '@mui/icons-material';
import { Link as RouterLink, useNavigate } from 'react-router-dom';
import { useDispatch, useSelector } from 'react-redux';
import { login, clearError, verifyToken, cancelTwoFactor } from '../store/slices/authSlice';
import { AppDispatch, RootState } from '../store';
import TwoFactorAuth from '../components/TwoFactorAuth';

const Login: React.FC = () => {
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [showPassword, setShowPassword] = useState(false);
  const [formErrors, setFormErrors] = useState({ username: '', password: '' });
  
  const navigate = useNavigate();
  const dispatch = useDispatch<AppDispatch>();
  const { 
    loading, 
    error, 
    isAuthenticated, 
    twoFactorRequired, 
    pendingUsername,
    maskedEmail
  } = useSelector((state: RootState) => state.auth);

  useEffect(() => {
    // Si el usuario ya está autenticado, redirigir al dashboard
    if (isAuthenticated) {
      navigate('/dashboard');
    }
    
    // Limpiar errores al montar el componente
    dispatch(clearError());
  }, [isAuthenticated, navigate, dispatch]);

  const validateForm = (): boolean => {
    let valid = true;
    const newErrors = { username: '', password: '' };
    
    // Validate identification (username) - must be numeric and between 5-20 digits
    if (!username.trim()) {
      newErrors.username = 'La identificación es requerida';
      valid = false;
    } else if (!/^[0-9]{5,20}$/.test(username)) {
      newErrors.username = 'La identificación debe ser numérica y tener entre 5 y 20 dígitos';
      valid = false;
    }
    
    if (!password) {
      newErrors.password = 'La contraseña es requerida';
      valid = false;
    }
    
    setFormErrors(newErrors);
    return valid;
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    
    if (validateForm()) {
      await dispatch(login({ username, password }));
    }
  };

  // Handle input change with immediate validation for better UX
  const handleInputChange = (field: string, value: string) => {
    switch (field) {
      case 'username':
        setUsername(value);
        // Only validate if there's already an error or the field is not empty
        if (formErrors.username || value.trim()) {
          if (!value.trim()) {
            setFormErrors({...formErrors, username: 'La identificación es requerida'});
          } else if (!/^[0-9]{5,20}$/.test(value)) {
            setFormErrors({...formErrors, username: 'La identificación debe ser numérica y tener entre 5 y 20 dígitos'});
          } else {
            setFormErrors({...formErrors, username: ''});
          }
        }
        break;
      case 'password':
        setPassword(value);
        if (formErrors.password && value) {
          setFormErrors({...formErrors, password: ''});
        }
        break;
      default:
        break;
    }
  };

  // Toggle password visibility
  const handleTogglePasswordVisibility = () => {
    setShowPassword((prevShowPassword) => !prevShowPassword);
  };

  // Handle 2FA verification
  const handleVerifyToken = (token: string) => {
    if (pendingUsername) {
      dispatch(verifyToken({ username: pendingUsername, token }));
    }
  };

  // Handle 2FA cancellation
  const handleCancelTwoFactor = () => {
    dispatch(cancelTwoFactor());
    setPassword(''); // Clear password for security
  };

  // If 2FA is required, show the 2FA component
  if (twoFactorRequired && pendingUsername) {
    return (
      <Container component="main" maxWidth="md">
        <TwoFactorAuth
          username={pendingUsername}
          maskedEmail={maskedEmail}
          onVerify={handleVerifyToken}
          onCancel={handleCancelTwoFactor}
          error={error || undefined}
          loading={loading}
        />
      </Container>
    );
  }

  return (
    <Container component="main" maxWidth="xs">
      <Box
        sx={{
          marginTop: 8,
          display: 'flex',
          flexDirection: 'column',
          alignItems: 'center',
        }}
      >
        <Paper
          elevation={3}
          sx={{
            padding: 4,
            display: 'flex',
            flexDirection: 'column',
            alignItems: 'center',
            width: '100%',
          }}
        >
          <Typography component="h1" variant="h5" sx={{ mb: 3 }}>
            Monedero Virtual
          </Typography>
          
          <Typography component="h2" variant="h6" sx={{ mb: 3 }}>
            Iniciar Sesión
          </Typography>
          
          {error && (
            <Alert severity="error" sx={{ width: '100%', mb: 2 }}>
              {error}
            </Alert>
          )}
          
          <Box component="form" onSubmit={handleSubmit} noValidate sx={{ mt: 1, width: '100%' }}>
            <TextField
              margin="normal"
              required
              fullWidth
              id="username"
              label="Identificación"
              name="username"
              autoComplete="username"
              autoFocus
              value={username}
              onChange={(e) => handleInputChange('username', e.target.value)}
              error={!!formErrors.username}
              helperText={formErrors.username || "Ingrese su número de identificación"}
              disabled={loading}
              inputProps={{
                inputMode: 'numeric',
                pattern: '[0-9]*'
              }}
            />
            <TextField
              margin="normal"
              required
              fullWidth
              name="password"
              label="Contraseña"
              type={showPassword ? "text" : "password"}
              id="password"
              autoComplete="current-password"
              value={password}
              onChange={(e) => handleInputChange('password', e.target.value)}
              error={!!formErrors.password}
              helperText={formErrors.password}
              disabled={loading}
              InputProps={{
                endAdornment: (
                  <InputAdornment position="end">
                    <IconButton
                      aria-label="toggle password visibility"
                      onClick={handleTogglePasswordVisibility}
                      edge="end"
                    >
                      {showPassword ? <VisibilityOff /> : <Visibility />}
                    </IconButton>
                  </InputAdornment>
                )
              }}
            />
            <Button
              type="submit"
              fullWidth
              variant="contained"
              sx={{ mt: 3, mb: 2 }}
              disabled={loading}
            >
              {loading ? <CircularProgress size={24} /> : 'Iniciar Sesión'}
            </Button>
            <Box sx={{ display: 'flex', justifyContent: 'center' }}>
              <RouterLink to="/register" style={{ textDecoration: 'none', color: '#1976d2' }}>
                ¿No tienes una cuenta? Regístrate
              </RouterLink>
            </Box>
          </Box>
        </Paper>
      </Box>
    </Container>
  );
};

export default Login;

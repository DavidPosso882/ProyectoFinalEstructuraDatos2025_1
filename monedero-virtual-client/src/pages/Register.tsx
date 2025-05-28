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
  Grid,
  InputAdornment,
  IconButton
} from '@mui/material';
import { 
  Visibility, 
  VisibilityOff 
} from '@mui/icons-material';
import { Link as RouterLink, useNavigate } from 'react-router-dom';
import { useDispatch, useSelector } from 'react-redux';
import { register, clearError, clearRegistrationSuccess } from '../store/slices/authSlice';
import { AppDispatch, RootState } from '../store';

const Register: React.FC = () => {
  const [username, setUsername] = useState('');
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');
  const [firstName, setFirstName] = useState('');
  const [lastName, setLastName] = useState('');
  const [showPassword, setShowPassword] = useState(false);
  const [showConfirmPassword, setShowConfirmPassword] = useState(false);
  const [formErrors, setFormErrors] = useState({
    username: '',
    email: '',
    password: '',
    confirmPassword: '',
    firstName: '',
    lastName: ''
  });
  
  const navigate = useNavigate();
  const dispatch = useDispatch<AppDispatch>();
  const { loading, error, isAuthenticated, registrationSuccess } = useSelector((state: RootState) => state.auth);

  useEffect(() => {
    // Si el usuario ya está autenticado, redirigir al dashboard
    if (isAuthenticated) {
      navigate('/dashboard');
    }
    
    // Si el registro fue exitoso, redirigir a la página de login después de un breve retraso
    if (registrationSuccess) {
      const timer = setTimeout(() => {
        dispatch(clearRegistrationSuccess());
        navigate('/login');
      }, 2000);
      
      return () => clearTimeout(timer);
    }
    
    // Limpiar errores al montar el componente
    dispatch(clearError());
  }, [isAuthenticated, registrationSuccess, navigate, dispatch]);

  const validateForm = (): boolean => {
    let valid = true;
    const newErrors = {
      username: '',
      email: '',
      password: '',
      confirmPassword: '',
      firstName: '',
      lastName: ''
    };
    
    // Validate identification (username) - must be numeric and between 5-20 digits
    if (!username.trim()) {
      newErrors.username = 'La identificación es requerida';
      valid = false;
    } else if (!/^[0-9]{5,20}$/.test(username)) {
      newErrors.username = 'La identificación debe ser numérica y tener entre 5 y 20 dígitos';
      valid = false;
    }
    
    // Validate email
    if (!email.trim()) {
      newErrors.email = 'El email es requerido';
      valid = false;
    } else if (!/\S+@\S+\.\S+/.test(email)) {
      newErrors.email = 'El email no es válido';
      valid = false;
    }
    
    // Validate password - stronger requirements
    if (!password) {
      newErrors.password = 'La contraseña es requerida';
      valid = false;
    } else if (password.length < 6) {
      newErrors.password = 'La contraseña debe tener al menos 6 caracteres';
      valid = false;
    } else if (!/(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[!@#$%^&*])/.test(password)) {
      newErrors.password = 'La contraseña debe contener al menos un número, una letra minúscula, una letra mayúscula y un carácter especial';
      valid = false;
    }
    
    // Validate password confirmation
    if (password !== confirmPassword) {
      newErrors.confirmPassword = 'Las contraseñas no coinciden';
      valid = false;
    }
    
    // Validate first name - only letters allowed
    if (!firstName.trim()) {
      newErrors.firstName = 'El nombre es requerido';
      valid = false;
    } else if (!/^[a-zA-ZáéíóúÁÉÍÓÚñÑ\s]+$/.test(firstName)) {
      newErrors.firstName = 'El nombre solo debe contener letras';
      valid = false;
    }
    
    // Validate last name - only letters allowed
    if (!lastName.trim()) {
      newErrors.lastName = 'El apellido es requerido';
      valid = false;
    } else if (!/^[a-zA-ZáéíóúÁÉÍÓÚñÑ\s]+$/.test(lastName)) {
      newErrors.lastName = 'El apellido solo debe contener letras';
      valid = false;
    }
    
    setFormErrors(newErrors);
    return valid;
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    
    if (validateForm()) {
      await dispatch(register({ 
        username, 
        email, 
        password,
        firstName,
        lastName,
        roles: ['user']
      }));
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
      case 'email':
        setEmail(value);
        break;
      case 'password':
        setPassword(value);
        break;
      case 'confirmPassword':
        setConfirmPassword(value);
        break;
      case 'firstName':
        setFirstName(value);
        break;
      case 'lastName':
        setLastName(value);
        break;
      default:
        break;
    }
  };

  // Toggle password visibility
  const handleTogglePasswordVisibility = () => {
    setShowPassword((prevShowPassword) => !prevShowPassword);
  };

  // Toggle confirm password visibility
  const handleToggleConfirmPasswordVisibility = () => {
    setShowConfirmPassword((prevShowConfirmPassword) => !prevShowConfirmPassword);
  };

  return (
    <Container component="main" maxWidth="md">
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
            Crear Cuenta
          </Typography>
          
          {error && (
            <Alert severity="error" sx={{ width: '100%', mb: 2 }}>
              {error}
            </Alert>
          )}
          
          {registrationSuccess && (
            <Alert severity="success" sx={{ width: '100%', mb: 2 }}>
              ¡Registro exitoso! Serás redirigido a la página de inicio de sesión en unos segundos...
            </Alert>
          )}
          
          <Box component="form" onSubmit={handleSubmit} noValidate sx={{ mt: 1, width: '100%' }}>
            <Grid container spacing={2}>
              <Grid item xs={12} sm={6}>
                <TextField
                  margin="normal"
                  required
                  fullWidth
                  id="firstName"
                  label="Nombre"
                  name="firstName"
                  autoComplete="given-name"
                  value={firstName}
                  onChange={(e) => handleInputChange('firstName', e.target.value)}
                  error={!!formErrors.firstName}
                  helperText={formErrors.firstName}
                  disabled={loading || registrationSuccess}
                />
              </Grid>
              <Grid item xs={12} sm={6}>
                <TextField
                  margin="normal"
                  required
                  fullWidth
                  id="lastName"
                  label="Apellido"
                  name="lastName"
                  autoComplete="family-name"
                  value={lastName}
                  onChange={(e) => handleInputChange('lastName', e.target.value)}
                  error={!!formErrors.lastName}
                  helperText={formErrors.lastName}
                  disabled={loading || registrationSuccess}
                />
              </Grid>
              <Grid item xs={12}>
                <TextField
                  margin="normal"
                  required
                  fullWidth
                  id="username"
                  label="Identificación"
                  name="username"
                  autoComplete="username"
                  value={username}
                  onChange={(e) => handleInputChange('username', e.target.value)}
                  error={!!formErrors.username}
                  helperText={formErrors.username || "Debe ser un número de 5 a 20 dígitos"}
                  disabled={loading || registrationSuccess}
                  inputProps={{
                    inputMode: 'numeric',
                    pattern: '[0-9]*'
                  }}
                />
              </Grid>
              <Grid item xs={12}>
                <TextField
                  margin="normal"
                  required
                  fullWidth
                  id="email"
                  label="Correo Electrónico"
                  name="email"
                  autoComplete="email"
                  value={email}
                  onChange={(e) => handleInputChange('email', e.target.value)}
                  error={!!formErrors.email}
                  helperText={formErrors.email}
                  disabled={loading || registrationSuccess}
                />
              </Grid>
              <Grid item xs={12} sm={6}>
                <TextField
                  margin="normal"
                  required
                  fullWidth
                  name="password"
                  label="Contraseña"
                  type={showPassword ? "text" : "password"}
                  id="password"
                  autoComplete="new-password"
                  value={password}
                  onChange={(e) => handleInputChange('password', e.target.value)}
                  error={!!formErrors.password}
                  helperText={formErrors.password || "Mínimo 6 caracteres, incluyendo número, mayúscula, minúscula y carácter especial"}
                  disabled={loading || registrationSuccess}
                  InputProps={{
                    endAdornment: (
                      <InputAdornment position="end">
                        <IconButton
                          aria-label="toggle password visibility"
                          onClick={handleTogglePasswordVisibility}
                          edge="end"
                          disabled={loading || registrationSuccess}
                        >
                          {showPassword ? <VisibilityOff /> : <Visibility />}
                        </IconButton>
                      </InputAdornment>
                    )
                  }}
                />
              </Grid>
              <Grid item xs={12} sm={6}>
                <TextField
                  margin="normal"
                  required
                  fullWidth
                  name="confirmPassword"
                  label="Confirmar Contraseña"
                  type={showConfirmPassword ? "text" : "password"}
                  id="confirmPassword"
                  autoComplete="new-password"
                  value={confirmPassword}
                  onChange={(e) => handleInputChange('confirmPassword', e.target.value)}
                  error={!!formErrors.confirmPassword}
                  helperText={formErrors.confirmPassword}
                  disabled={loading || registrationSuccess}
                  InputProps={{
                    endAdornment: (
                      <InputAdornment position="end">
                        <IconButton
                          aria-label="toggle confirm password visibility"
                          onClick={handleToggleConfirmPasswordVisibility}
                          edge="end"
                          disabled={loading || registrationSuccess}
                        >
                          {showConfirmPassword ? <VisibilityOff /> : <Visibility />}
                        </IconButton>
                      </InputAdornment>
                    )
                  }}
                />
              </Grid>
            </Grid>
            <Button
              type="submit"
              fullWidth
              variant="contained"
              sx={{ mt: 3, mb: 2 }}
              disabled={loading || registrationSuccess}
            >
              {loading ? <CircularProgress size={24} /> : 'Registrarse'}
            </Button>
            <Box sx={{ display: 'flex', justifyContent: 'center' }}>
              <RouterLink to="/login" style={{ textDecoration: 'none', color: '#1976d2' }}>
                ¿Ya tienes una cuenta? Inicia sesión
              </RouterLink>
            </Box>
          </Box>
        </Paper>
      </Box>
    </Container>
  );
};

export default Register;

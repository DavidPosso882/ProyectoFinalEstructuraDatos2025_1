import React, { useEffect, useState } from 'react';
import {
  Box,
  Typography,
  Paper,
  Grid,
  Button,
  CircularProgress,
  useTheme,
  Tabs,
  Tab,
  Divider,
  IconButton,
  Tooltip,
  Alert,
  List,
  ListItem,
  ListItemIcon,
  ListItemText,
  Avatar,
  Chip,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  Card,
  CardContent,
  CardHeader,
  LinearProgress
} from '@mui/material';
import {
  EmojiEvents as TrophyIcon,
  Star as StarIcon,
  History as HistoryIcon,
  CardGiftcard as GiftIcon,
  ArrowDownward as ArrowDownwardIcon,
  Info as InfoIcon,
  Percent as PercentIcon,
  People as UsersIcon,
  CheckCircle as CheckCircleIcon,
  CreditCard as CreditCardIcon
} from '@mui/icons-material';
import { useDispatch, useSelector } from 'react-redux';
import { AppDispatch, RootState } from '../store';
import { fetchPointsAccount, fetchPointsHistory, fetchAvailableBenefits, redeemPoints } from '../store/slices/pointsSlice';
import { UserRank, RANK_COLORS, RANK_BENEFITS, RANK_THRESHOLDS, AvailableBenefit, PointsRedemptionRequest } from '../types/points.types';
import ApiService from '../services/api.service';
import { API_ENDPOINTS } from '../utils/api-config';
import UserService from '../services/user.service';
import { User } from '../types/auth.types';

interface Benefit {
    title: string;
    description: string;
    icon: React.ReactNode;
}

const PointsPage: React.FC = () => {
  const theme = useTheme();
  const dispatch = useDispatch<AppDispatch>();
  const { pointsAccount, pointsHistory, availableBenefits, loading, redeemLoading, error } = useSelector((state: RootState) => state.points);

  // Estados locales
  const [tabValue, setTabValue] = useState(0);
  const [historyDialogOpen, setHistoryDialogOpen] = useState(false);
  const [redeemDialogOpen, setRedeemDialogOpen] = useState(false);
  const [selectedBenefit, setSelectedBenefit] = useState<AvailableBenefit | null>(null);
  const [ranking, setRanking] = useState<User[]>([]);
  const [redeemSuccess, setRedeemSuccess] = useState(false);

  // Datos simulados para la demostración
  const currentRank = pointsAccount?.currentRank || UserRank.BRONZE;
  const points = pointsAccount?.availablePoints || 0; // Usar availablePoints en lugar de totalPoints
  const totalPoints = pointsAccount?.totalPoints || 0; // Para calcular el rango
  const nextRank = getNextRank(currentRank);
  const pointsToNextRank = getPointsToNextRank(currentRank, totalPoints); // Usar totalPoints para el rango

  // Beneficios actuales y próximos
  const currentBenefits: Benefit[] = [
    {
        title: 'Cashback 2%',
        description: 'Recibe un 2% de cashback en todas tus compras',
        icon: <PercentIcon sx={{ color: theme.palette.warning.main }} />,
    },
    {
        title: 'Sin comisiones',
        description: 'Transferencias sin comisiones entre usuarios de la plataforma',
        icon: <UsersIcon sx={{ color: theme.palette.success.main }} />,
    },
    {
        title: 'Soporte prioritario',
        description: 'Acceso a soporte prioritario por chat',
        icon: <CheckCircleIcon sx={{ color: theme.palette.info.main }} />,
    },
  ];

  const nextBenefits: Benefit[] = [
    {
        title: 'Cashback 3%',
        description: 'Recibe un 3% de cashback en todas tus compras',
        icon: <PercentIcon sx={{ color: theme.palette.warning.main }} />,
    },
    {
        title: 'Sin comisiones',
        description: 'Transferencias sin comisiones entre usuarios de la plataforma',
        icon: <UsersIcon sx={{ color: theme.palette.success.main }} />,
    },
    {
        title: 'Soporte VIP',
        description: 'Acceso a soporte VIP 24/7',
        icon: <CheckCircleIcon sx={{ color: theme.palette.info.main }} />,
    },
    {
        title: 'Tarjeta física',
        description: 'Tarjeta física gratuita con diseño exclusivo',
        icon: <CreditCardIcon sx={{ color: theme.palette.secondary.main }} />,
    },
  ];

  // Cargar datos de puntos al montar el componente
  useEffect(() => {
    dispatch(fetchPointsAccount());
    dispatch(fetchPointsHistory());
    dispatch(fetchAvailableBenefits());
  }, [dispatch]);

  useEffect(() => {
    UserService.getRanking().then(setRanking);
  }, []);

  // Función para obtener el siguiente rango
  function getNextRank(currentRank: UserRank): UserRank {
    switch (currentRank) {
      case UserRank.BRONZE:
        return UserRank.SILVER;
      case UserRank.SILVER:
        return UserRank.GOLD;
      case UserRank.GOLD:
        return UserRank.PLATINUM;
      case UserRank.PLATINUM:
        return UserRank.DIAMOND;
      default:
        return UserRank.DIAMOND;
    }
  }

  // Función para calcular los puntos necesarios para el siguiente rango
  function getPointsToNextRank(currentRank: UserRank, currentPoints: number): number {
    const nextRank = getNextRank(currentRank);
    if (nextRank === currentRank) return 0; // Ya está en el rango máximo

    const nextThreshold = RANK_THRESHOLDS[nextRank];
    return Math.max(0, nextThreshold - currentPoints);
  }

  // Manejador para cambiar de pestaña
  const handleTabChange = (event: React.SyntheticEvent, newValue: number) => {
    setTabValue(newValue);
  };

  // Manejador para abrir el diálogo de historial
  const handleOpenHistoryDialog = () => {
    setHistoryDialogOpen(true);
    // Cargar el historial de puntos cuando se abre el diálogo
    dispatch(fetchPointsHistory());
  };

  // Manejador para cerrar el diálogo de historial
  const handleCloseHistoryDialog = () => {
    setHistoryDialogOpen(false);
  };

  // Manejador para abrir el diálogo de canje
  const handleOpenRedeemDialog = () => {
    setRedeemDialogOpen(true);
    setSelectedBenefit(null);
    setRedeemSuccess(false);
  };

  // Manejador para cerrar el diálogo de canje
  const handleCloseRedeemDialog = () => {
    setRedeemDialogOpen(false);
    setSelectedBenefit(null);
    setRedeemSuccess(false);
  };

  // Manejador para seleccionar un beneficio
  const handleSelectBenefit = (benefit: AvailableBenefit) => {
    setSelectedBenefit(benefit);
  };

  // Manejador para confirmar el canje
  const handleConfirmRedeem = async () => {
    if (!selectedBenefit) return;

    const redeemData: PointsRedemptionRequest = {
      points: selectedBenefit.points,
      benefitCode: selectedBenefit.code
    };

    try {
      await dispatch(redeemPoints(redeemData)).unwrap();
      setRedeemSuccess(true);
      setSelectedBenefit(null);

      // Pequeño delay para mejor UX y luego recargar datos
      setTimeout(async () => {
        await dispatch(fetchPointsAccount()).unwrap();
        await dispatch(fetchPointsHistory()).unwrap();
      }, 500);
    } catch (error) {
      console.error('Error al canjear puntos:', error);
    }
  };

  // Renderizar el diálogo de historial de puntos
  const renderHistoryDialog = () => {
    return (
      <Dialog
        open={historyDialogOpen}
        onClose={handleCloseHistoryDialog}
        maxWidth="md"
        fullWidth
      >
        <DialogTitle>
          <Box sx={{ display: 'flex', alignItems: 'center' }}>
            <HistoryIcon sx={{ mr: 1 }} />
            <Typography variant="h6">Historial de Canjes</Typography>
          </Box>
        </DialogTitle>
        <DialogContent dividers>
          {loading ? (
            <Box sx={{ display: 'flex', justifyContent: 'center', p: 4 }}>
              <CircularProgress />
            </Box>
          ) : pointsHistory && pointsHistory.length > 0 ? (
            <List>
              {pointsHistory.map((transaction, index) => (
                <React.Fragment key={transaction.id}>
                  {index > 0 && <Divider component="li" />}
                  <ListItem alignItems="flex-start">
                    <ListItemIcon>
                      <ArrowDownwardIcon sx={{ color: theme.palette.error.main }} />
                    </ListItemIcon>
                    <ListItemText
                      primary={
                        <Box sx={{ display: 'flex', justifyContent: 'space-between' }}>
                          <Typography variant="body1" sx={{ fontWeight: 'medium' }}>
                            {transaction.description}
                          </Typography>
                          <Typography
                            variant="body1"
                            sx={{
                              fontWeight: 'bold',
                              color: theme.palette.error.main
                            }}
                          >
                            -{transaction.amount} puntos
                          </Typography>
                        </Box>
                      }
                      secondary={
                        <Typography variant="caption" color="text.secondary">
                          {new Date(transaction.createdAt).toLocaleString()}
                        </Typography>
                      }
                    />
                  </ListItem>
                </React.Fragment>
              ))}
            </List>
          ) : (
            <Box sx={{ p: 4, textAlign: 'center' }}>
              <Typography variant="body1" color="textSecondary">
                No hay canjes de puntos para mostrar
              </Typography>
            </Box>
          )}
        </DialogContent>
        <DialogActions>
          <Button onClick={handleCloseHistoryDialog}>Cerrar</Button>
        </DialogActions>
      </Dialog>
    );
  };

  // Renderizar el diálogo de canje de puntos
  const renderRedeemDialog = () => {
    return (
      <Dialog
        open={redeemDialogOpen}
        onClose={handleCloseRedeemDialog}
        maxWidth="md"
        fullWidth
      >
        <DialogTitle>
          <Box sx={{ display: 'flex', alignItems: 'center' }}>
            <GiftIcon sx={{ mr: 1, color: RANK_COLORS[currentRank] }} />
            <Typography variant="h6">Canjear Puntos</Typography>
          </Box>
        </DialogTitle>
        <DialogContent dividers>
          {redeemSuccess ? (
            <Box sx={{ textAlign: 'center', p: 4 }}>
              <CheckCircleIcon sx={{ fontSize: 64, color: theme.palette.success.main, mb: 2 }} />
              <Typography variant="h6" gutterBottom>
                ¡Canje Exitoso!
              </Typography>
              <Typography variant="body1" color="textSecondary">
                Tus puntos han sido canjeados correctamente.
              </Typography>
            </Box>
          ) : selectedBenefit ? (
            <Box>
              <Typography variant="h6" gutterBottom>
                Confirmar Canje
              </Typography>
              <Paper sx={{ p: 3, mb: 3, bgcolor: theme.palette.background.default }}>
                <Typography variant="h6" gutterBottom>
                  {selectedBenefit.name}
                </Typography>
                <Typography variant="body1" color="textSecondary" paragraph>
                  {selectedBenefit.description}
                </Typography>
                <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                  <Typography variant="h6" sx={{ color: RANK_COLORS[currentRank] }}>
                    {selectedBenefit.points} puntos
                  </Typography>
                  <Typography variant="body2" color="textSecondary">
                    Puntos disponibles: {points}
                  </Typography>
                </Box>
              </Paper>
              {points < selectedBenefit.points && (
                <Alert severity="warning" sx={{ mb: 2 }}>
                  No tienes suficientes puntos para este beneficio.
                </Alert>
              )}
            </Box>
          ) : (
            <Box>
              <Typography variant="body1" gutterBottom>
                Selecciona un beneficio para canjear tus puntos:
              </Typography>
              <Typography variant="body2" color="textSecondary" sx={{ mb: 3 }}>
                Puntos disponibles: <strong>{points}</strong>
              </Typography>

              {loading ? (
                <Box sx={{ display: 'flex', justifyContent: 'center', p: 4 }}>
                  <CircularProgress />
                </Box>
              ) : availableBenefits && availableBenefits.length > 0 ? (
                <Grid container spacing={2}>
                  {availableBenefits.map((benefit, index) => (
                    <Grid item xs={12} sm={6} key={index}>
                      <Card
                        sx={{
                          cursor: 'pointer',
                          transition: 'all 0.3s',
                          border: `1px solid ${theme.palette.divider}`,
                          '&:hover': {
                            boxShadow: 3,
                            borderColor: RANK_COLORS[currentRank]
                          },
                          opacity: points < benefit.points ? 0.6 : 1
                        }}
                        onClick={() => points >= benefit.points && handleSelectBenefit(benefit)}
                      >
                        <CardContent>
                          <Typography variant="h6" gutterBottom>
                            {benefit.name}
                          </Typography>
                          <Typography variant="body2" color="textSecondary" paragraph>
                            {benefit.description}
                          </Typography>
                          <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                            <Typography
                              variant="h6"
                              sx={{
                                color: points >= benefit.points ? RANK_COLORS[currentRank] : theme.palette.text.disabled
                              }}
                            >
                              {benefit.points} puntos
                            </Typography>
                            {points < benefit.points && (
                              <Chip
                                label="Insuficientes"
                                size="small"
                                color="error"
                                variant="outlined"
                              />
                            )}
                          </Box>
                        </CardContent>
                      </Card>
                    </Grid>
                  ))}
                </Grid>
              ) : (
                <Box sx={{ p: 4, textAlign: 'center' }}>
                  <Typography variant="body1" color="textSecondary">
                    No hay beneficios disponibles en este momento
                  </Typography>
                </Box>
              )}
            </Box>
          )}
        </DialogContent>
        <DialogActions>
          {selectedBenefit && !redeemSuccess ? (
            <>
              <Button onClick={() => setSelectedBenefit(null)}>
                Volver
              </Button>
              <Button
                variant="contained"
                onClick={handleConfirmRedeem}
                disabled={redeemLoading || points < selectedBenefit.points}
                sx={{
                  bgcolor: RANK_COLORS[currentRank],
                  '&:hover': {
                    bgcolor: RANK_COLORS[currentRank],
                    opacity: 0.9
                  }
                }}
              >
                {redeemLoading ? <CircularProgress size={20} /> : 'Confirmar Canje'}
              </Button>
            </>
          ) : (
            <Button onClick={handleCloseRedeemDialog}>
              {redeemSuccess ? 'Cerrar' : 'Cancelar'}
            </Button>
          )}
        </DialogActions>
      </Dialog>
    );
  };

  return (
    <Box sx={{ py: 3 }}>
      <Grid container spacing={3}>
        <Grid item xs={12}>
          <Box sx={{
            display: 'flex',
            justifyContent: 'space-between',
            alignItems: 'center',
            mb: 3
          }}>
            <Typography variant="h4" component="h1" gutterBottom>
              Sistema de Puntos
            </Typography>
          </Box>

          {error && (
            <Alert severity="error" sx={{ mb: 3 }}>
              {error}
            </Alert>
          )}

          {/* Beneficios de tu Rango Actual */}
          <Card sx={{ mb: 3, bgcolor: theme.palette.background.paper }}>
            <CardHeader
              title={
                <Box sx={{ display: 'flex', alignItems: 'center' }}>
                  <TrophyIcon sx={{ mr: 1, color: RANK_COLORS[currentRank] }} />
                  <Typography variant="h5">Beneficios de tu Rango</Typography>
                </Box>
              }
              subheader={
                <Typography variant="body2" color="textSecondary">
                  {currentRank === UserRank.SILVER
                    ? 'Disfruta de estos beneficios exclusivos'
                    : 'Tus beneficios actuales'}
                </Typography>
              }
            />
            <CardContent sx={{ pt: 0 }}>
              <Box sx={{ mb: 4 }}>
                <Box sx={{
                  display: 'flex',
                  flexDirection: { xs: 'column', md: 'row' },
                  alignItems: 'center',
                  gap: 3,
                  mb: 3
                }}>
                  <Box sx={{ textAlign: 'center' }}>
                    <Typography variant="h3" sx={{ fontWeight: 'bold', color: RANK_COLORS[currentRank] }}>
                      {points}
                    </Typography>
                    <Typography variant="body2" color="textSecondary">
                      Puntos disponibles
                    </Typography>
                    <Typography variant="caption" color="textSecondary" sx={{ mt: 0.5, display: 'block' }}>
                      Total acumulado: {totalPoints} | Canjeados: {pointsAccount?.redeemedPoints || 0}
                    </Typography>
                  </Box>
                  <Box sx={{ flexGrow: 1, width: '100%' }}>
                    <Box sx={{
                      display: 'flex',
                      justifyContent: 'space-between',
                      mb: 0.5
                    }}>
                      <Typography variant="body2">
                        Rango Actual: <Typography component="span" fontWeight="bold">{UserRank[currentRank]}</Typography>
                      </Typography>
                      <Typography variant="body2" sx={{ color: RANK_COLORS[nextRank] }}>
                        Siguiente: <Typography component="span" fontWeight="bold">{UserRank[nextRank]}</Typography>
                      </Typography>
                    </Box>
                    <LinearProgress
                      variant="determinate"
                      value={(totalPoints / (totalPoints + pointsToNextRank)) * 100}
                      sx={{
                        height: 8,
                        borderRadius: 4,
                        mb: 0.5,
                        bgcolor: theme.palette.action.hover,
                        '& .MuiLinearProgress-bar': {
                          bgcolor: RANK_COLORS[nextRank]
                        }
                      }}
                    />
                    <Typography variant="body2" color="textSecondary" align="right">
                      {pointsToNextRank} puntos para {UserRank[nextRank]}
                    </Typography>
                  </Box>
                </Box>

                <Box sx={{
                  display: 'flex',
                  flexDirection: { xs: 'column', sm: 'row' },
                  gap: 2,
                  mb: 4
                }}>
                  <Button
                    variant="outlined"
                    startIcon={<HistoryIcon />}
                    onClick={handleOpenHistoryDialog}
                    fullWidth
                  >
                    Historial de Canjes
                  </Button>
                  <Button
                    variant="contained"
                    startIcon={<GiftIcon />}
                    onClick={handleOpenRedeemDialog}
                    fullWidth
                    sx={{
                      bgcolor: RANK_COLORS[currentRank],
                      '&:hover': {
                        bgcolor: RANK_COLORS[currentRank],
                        opacity: 0.9
                      }
                    }}
                  >
                    Canjear Puntos
                  </Button>
                </Box>

                <Grid container spacing={2}>
                  {currentBenefits.map((benefit, index) => (
                    <Grid item xs={12} sm={6} md={4} key={index}>
                      <Paper sx={{
                        p: 2,
                        height: '100%',
                        display: 'flex',
                        alignItems: 'center',
                        gap: 2,
                        bgcolor: theme.palette.background.default
                      }}>
                        {benefit.icon}
                        <Box>
                          <Typography variant="subtitle1" fontWeight="bold">
                            {benefit.title}
                          </Typography>
                          <Typography variant="body2" color="textSecondary">
                            {benefit.description}
                          </Typography>
                        </Box>
                      </Paper>
                    </Grid>
                  ))}
                </Grid>
              </Box>
            </CardContent>
          </Card>

          {/* Próximos Beneficios */}
          <Card sx={{ bgcolor: theme.palette.background.paper }}>
            <CardHeader
              title={<Typography variant="h5">Próximos Beneficios</Typography>}
              subheader={
                <Typography variant="body2" color="textSecondary">
                  Alcanza el siguiente rango para desbloquear estos beneficios adicionales
                </Typography>
              }
            />
            <CardContent sx={{ pt: 0 }}>
              <Grid container spacing={2}>
                {nextBenefits.map((benefit, index) => (
                  <Grid item xs={12} sm={6} md={3} key={index}>
                    <Paper sx={{
                      p: 2,
                      height: '100%',
                      display: 'flex',
                      flexDirection: 'column',
                      bgcolor: theme.palette.background.default,
                      border: `1px solid ${theme.palette.divider}`,
                      transition: 'all 0.3s',
                      '&:hover': {
                        boxShadow: 3,
                        borderColor: RANK_COLORS[nextRank]
                      }
                    }}>
                      <Typography
                        variant="subtitle2"
                        sx={{
                          color: RANK_COLORS[nextRank],
                          mb: 1,
                          fontWeight: 'bold'
                        }}
                      >
                        {UserRank[nextRank]}
                      </Typography>
                      <Box sx={{ mb: 2 }}>
                        {benefit.icon}
                      </Box>
                      <Typography variant="subtitle1" fontWeight="bold">
                        {benefit.title}
                      </Typography>
                      <Typography variant="body2" color="textSecondary">
                        {benefit.description}
                      </Typography>
                    </Paper>
                  </Grid>
                ))}
              </Grid>
            </CardContent>
          </Card>
        </Grid>
      </Grid>

      {/* Diálogo de historial de puntos */}
      {renderHistoryDialog()}

      {/* Diálogo de canje de puntos */}
      {renderRedeemDialog()}

      <Box sx={{ mt: 4 }}>
        <Typography variant="h6">Ranking de usuarios (Top 10)</Typography>
        <ol>
          {ranking.map((user, idx) => (
            <li key={user.id}>{user.username} ({user.email})</li>
          ))}
        </ol>
      </Box>
    </Box>
  );
};

export default PointsPage;

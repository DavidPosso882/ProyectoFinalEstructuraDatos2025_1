import React from 'react';
import {
  Box,
  Typography,
  Button,
  LinearProgress,
  useTheme,
  alpha,
  Stack
} from '@mui/material';
import { useNavigate } from 'react-router-dom';
import { UserRank } from '../../types/points.types';

interface PointsSystemProps {
  currentPoints: number;
  currentRank: UserRank;
  pointsToNextRank: number;
}

const PointsSystem: React.FC<PointsSystemProps> = ({
  currentPoints,
  currentRank,
  pointsToNextRank
}) => {
  const theme = useTheme();
  const navigate = useNavigate();

  // Calcular el progreso hacia el siguiente rango
  const totalPointsNeeded = currentPoints + pointsToNextRank;
  const progress = (currentPoints / totalPointsNeeded) * 100;

  // Obtener el siguiente rango
  const getNextRank = (currentRank: UserRank): UserRank => {
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
  };

  const nextRank = getNextRank(currentRank);

  // Obtener color según el rango
  const getRankColor = (rank: UserRank): string => {
    switch (rank) {
      case UserRank.BRONZE:
        return '#CD7F32';
      case UserRank.SILVER:
        return '#C0C0C0';
      case UserRank.GOLD:
        return '#FFD700';
      case UserRank.PLATINUM:
        return '#E5E4E2';
      case UserRank.DIAMOND:
        return '#B9F2FF';
      default:
        return theme.palette.primary.main;
    }
  };

  // Manejadores de navegación
  const handleHistoryClick = () => {
    navigate('/points');
  };

  const handleRedeemClick = () => {
    navigate('/points');
  };

  return (
    <Box sx={{ width: '100%' }}>
      <Typography variant="h6" sx={{ mb: 3, fontWeight: 600 }}>
        Sistema de Puntos
      </Typography>

      {/* Puntos disponibles (centrado) */}
      <Box sx={{
        display: 'flex',
        flexDirection: 'column',
        alignItems: 'center',
        mb: 3
      }}>
        <Typography
          variant="h3"
          component="p"
          sx={{
            fontWeight: 'bold',
            color: getRankColor(currentRank)
          }}
        >
          {currentPoints}
        </Typography>
        <Typography variant="body2" color="textSecondary">
          Puntos disponibles
        </Typography>
      </Box>

      {/* Barra de progreso */}
      <Box sx={{ mb: 3 }}>
        <Box sx={{
          display: 'flex',
          justifyContent: 'space-between',
          mb: 0.5
        }}>
          <Typography variant="caption" color="textSecondary">
            {UserRank[currentRank]}
          </Typography>
          <Typography
            variant="caption"
            sx={{ color: getRankColor(nextRank) }}
          >
            {UserRank[nextRank]}
          </Typography>
        </Box>

        <LinearProgress
          variant="determinate"
          value={progress}
          sx={{
            height: 8,
            borderRadius: 4,
            bgcolor: theme.palette.mode === 'dark' ? 'rgba(255, 255, 255, 0.12)' : 'rgba(0, 0, 0, 0.12)',
            '& .MuiLinearProgress-bar': {
              bgcolor: getRankColor(nextRank)
            }
          }}
        />

        <Typography
          variant="caption"
          color="textSecondary"
          sx={{
            display: 'block',
            textAlign: 'right',
            mt: 0.5
          }}
        >
          {pointsToNextRank} puntos para {UserRank[nextRank]}
        </Typography>
      </Box>

      {/* Botones (uno al lado del otro) */}
      <Stack
        direction="row"
        spacing={2}
        justifyContent="center"
      >
        <Button
          variant="outlined"
          size="small"
          onClick={handleHistoryClick}
          sx={{
            borderRadius: 2,
            textTransform: 'none',
            flex: 1
          }}
        >
          Historial
        </Button>
        <Button
          variant="contained"
          size="small"
          onClick={handleRedeemClick}
          sx={{
            bgcolor: getRankColor(currentRank),
            color: theme.palette.getContrastText(getRankColor(currentRank)),
            '&:hover': {
              bgcolor: alpha(getRankColor(currentRank), theme.palette.mode === 'dark' ? 0.8 : 0.9)
            },
            borderRadius: 2,
            textTransform: 'none',
            flex: 1
          }}
        >
          Canjear Puntos
        </Button>
      </Stack>
    </Box>
  );
};

export default PointsSystem;

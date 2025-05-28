import React from 'react';
import { Box, Typography, LinearProgress, Tooltip, styled } from '@mui/material';
import { UserRank, RANK_THRESHOLDS, RANK_COLORS, RANK_BENEFITS } from '../types/points.types';

interface RankProgressBarProps {
  currentRank: UserRank;
  currentPoints: number;
  pointsToNextRank: number;
}

// Componente estilizado para el indicador de rango actual
const RankIndicator = styled(Box)(({ theme }) => ({
  width: 24,
  height: 24,
  borderRadius: '50%',
  display: 'flex',
  alignItems: 'center',
  justifyContent: 'center',
  color: theme.palette.common.white,
  fontWeight: 'bold',
  fontSize: 14,
  boxShadow: theme.shadows[3]
}));

// Componente para mostrar los beneficios en el tooltip
const BenefitsList: React.FC<{ benefits: { title: string; description: string }[] }> = ({ benefits }) => (
  <Box>
    <Typography variant="subtitle2" gutterBottom>Beneficios:</Typography>
    <ul style={{ margin: 0, paddingLeft: 16 }}>
      {benefits.map((benefit, index) => (
        <li key={index}>
          <Typography variant="body2">{benefit.title}</Typography>
        </li>
      ))}
    </ul>
  </Box>
);

const RankProgressBar: React.FC<RankProgressBarProps> = ({
  currentRank,
  currentPoints,
  pointsToNextRank
}) => {
  // Determinar el siguiente rango
  const getNextRank = (rank: UserRank): UserRank | null => {
    switch (rank) {
      case UserRank.BRONZE: return UserRank.SILVER;
      case UserRank.SILVER: return UserRank.GOLD;
      case UserRank.GOLD: return UserRank.PLATINUM;
      case UserRank.PLATINUM: return UserRank.DIAMOND;
      case UserRank.DIAMOND: return null; // No hay rango superior
      default: return UserRank.SILVER;
    }
  };

  const nextRank = getNextRank(currentRank);
  
  // Calcular el progreso hacia el siguiente rango
  const calculateProgress = (): number => {
    if (!nextRank) return 100; // Si es DIAMOND, mostrar 100%
    
    const currentThreshold = RANK_THRESHOLDS[currentRank];
    const nextThreshold = RANK_THRESHOLDS[nextRank];
    const pointsNeeded = nextThreshold - currentThreshold;
    const pointsGained = currentPoints - currentThreshold;
    
    return Math.min(Math.floor((pointsGained / pointsNeeded) * 100), 100);
  };

  const progress = calculateProgress();

  return (
    <Box sx={{ width: '100%', mt: 2 }}>
      <Box sx={{ display: 'flex', justifyContent: 'space-between', mb: 1 }}>
        <Tooltip 
          title={<BenefitsList benefits={RANK_BENEFITS[currentRank]} />}
          arrow
          placement="top"
        >
          <Box sx={{ display: 'flex', alignItems: 'center' }}>
            <RankIndicator sx={{ bgcolor: RANK_COLORS[currentRank] }}>
              {currentRank.charAt(0)}
            </RankIndicator>
            <Typography variant="body2" sx={{ ml: 1, fontWeight: 'medium' }}>
              {currentRank}
            </Typography>
          </Box>
        </Tooltip>
        
        {nextRank && (
          <Tooltip 
            title={<BenefitsList benefits={RANK_BENEFITS[nextRank]} />}
            arrow
            placement="top"
          >
            <Box sx={{ display: 'flex', alignItems: 'center' }}>
              <Typography variant="body2" sx={{ mr: 1, fontWeight: 'medium' }}>
                {nextRank}
              </Typography>
              <RankIndicator sx={{ bgcolor: RANK_COLORS[nextRank] }}>
                {nextRank.charAt(0)}
              </RankIndicator>
            </Box>
          </Tooltip>
        )}
      </Box>
      
      <LinearProgress 
        variant="determinate" 
        value={progress} 
        sx={{ 
          height: 8, 
          borderRadius: 4,
          bgcolor: 'rgba(0,0,0,0.1)',
          '& .MuiLinearProgress-bar': {
            borderRadius: 4,
            background: `linear-gradient(90deg, ${RANK_COLORS[currentRank]} 0%, ${nextRank ? RANK_COLORS[nextRank] : RANK_COLORS[currentRank]} 100%)`
          }
        }}
      />
      
      <Box sx={{ display: 'flex', justifyContent: 'space-between', mt: 0.5 }}>
        <Typography variant="caption" color="text.secondary">
          {currentPoints} puntos
        </Typography>
        {nextRank && (
          <Typography variant="caption" color="text.secondary">
            {pointsToNextRank} puntos para {nextRank}
          </Typography>
        )}
      </Box>
    </Box>
  );
};

export default RankProgressBar;

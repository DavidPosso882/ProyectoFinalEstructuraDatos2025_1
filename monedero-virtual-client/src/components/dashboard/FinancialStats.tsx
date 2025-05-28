import React from 'react';
import {
  Box,
  Typography,
  Grid,
  LinearProgress,
  useTheme
} from '@mui/material';
import {
  AccountBalanceWallet as WalletIcon
} from '@mui/icons-material';
import SummaryCard from './SummaryCard';

interface FinancialStatsProps {
  totalBalance: number;
  expenseRatio: number;
}

const FinancialStats: React.FC<FinancialStatsProps> = ({
  totalBalance,
  expenseRatio
}) => {
  const theme = useTheme();

  // Determinar el color del ratio de gastos
  const getRatioColor = (ratio: number) => {
    if (ratio < 50) return theme.palette.success.main;
    if (ratio < 80) return theme.palette.warning.main;
    return theme.palette.error.main;
  };

  return (
    <Grid container spacing={2}>
      {/* Balance Total */}
      <Grid item xs={12} md={6}>
        <SummaryCard
          icon={<WalletIcon />}
          iconColor={theme.palette.primary.main}
          fullWidth
        >
          <Box sx={{ textAlign: 'center', mt: 1 }}>
            <Typography variant="h3" component="p" sx={{ fontWeight: 'bold', color: theme.palette.primary.main }}>
              ${totalBalance.toFixed(2)}
            </Typography>
            <Typography variant="body2" color="textSecondary">
              Balance Total
            </Typography>
          </Box>
        </SummaryCard>
      </Grid>

      {/* Ratio Gastos vs Ingresos */}
      <Grid item xs={12} sm={6} md={6}>
        <SummaryCard title="Ratio Gastos vs Ingresos">
          <Box sx={{ mt: 1 }}>
            <LinearProgress
              variant="determinate"
              value={expenseRatio}
              sx={{
                height: 8,
                borderRadius: 4,
                bgcolor: theme.palette.mode === 'dark' ? 'rgba(255, 255, 255, 0.12)' : 'rgba(0, 0, 0, 0.12)',
                '& .MuiLinearProgress-bar': {
                  bgcolor: getRatioColor(expenseRatio)
                },
                mb: 0.5
              }}
            />
            <Typography
              variant="body2"
              sx={{
                textAlign: 'right',
                fontWeight: 'medium',
                color: getRatioColor(expenseRatio)
              }}
            >
              {expenseRatio}%
            </Typography>
          </Box>
        </SummaryCard>
      </Grid>
    </Grid>
  );
};

export default FinancialStats;

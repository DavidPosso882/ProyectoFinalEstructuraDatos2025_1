import React from 'react';
import { 
  Grid, 
  Paper, 
  Typography, 
  Box, 
  Divider, 
  Chip,
  useTheme
} from '@mui/material';
import { 
  TrendingUp, 
  TrendingDown, 
  AccountBalance,
  CalendarToday,
  Category
} from '@mui/icons-material';

interface FinancialSummaryProps {
  totalBalance: number;
  totalIncome: number;
  totalExpenses: number;
  topCategory: string;
  topCategoryAmount: number;
  upcomingTransaction?: {
    date: string;
    description: string;
    amount: number;
  };
}

const FinancialSummary: React.FC<FinancialSummaryProps> = ({
  totalBalance,
  totalIncome,
  totalExpenses,
  topCategory,
  topCategoryAmount,
  upcomingTransaction
}) => {
  const theme = useTheme();
  
  // Calcular el porcentaje de gastos vs ingresos
  const expenseRatio = totalIncome > 0 ? (totalExpenses / totalIncome) * 100 : 0;
  
  // Determinar el color según el ratio de gastos
  const getRatioColor = (ratio: number) => {
    if (ratio < 50) return theme.palette.success.main;
    if (ratio < 80) return theme.palette.warning.main;
    return theme.palette.error.main;
  };
  
  return (
    <Paper elevation={3} sx={{ p: 2, mb: 3 }}>
      <Typography variant="h6" gutterBottom>
        Resumen Financiero
      </Typography>
      <Divider sx={{ mb: 2 }} />
      
      <Grid container spacing={3}>
        {/* Balance Total */}
        <Grid item xs={12} sm={6} md={4}>
          <Box sx={{ display: 'flex', alignItems: 'center' }}>
            <AccountBalance 
              sx={{ 
                fontSize: 40, 
                color: theme.palette.primary.main,
                mr: 2 
              }} 
            />
            <Box>
              <Typography variant="body2" color="textSecondary">
                Balance Total
              </Typography>
              <Typography variant="h5" sx={{ fontWeight: 'medium' }}>
                ${totalBalance.toFixed(2)}
              </Typography>
            </Box>
          </Box>
        </Grid>
        
        {/* Ingresos */}
        <Grid item xs={12} sm={6} md={4}>
          <Box sx={{ display: 'flex', alignItems: 'center' }}>
            <TrendingUp 
              sx={{ 
                fontSize: 40, 
                color: theme.palette.success.main,
                mr: 2 
              }} 
            />
            <Box>
              <Typography variant="body2" color="textSecondary">
                Ingresos (30 días)
              </Typography>
              <Typography variant="h5" sx={{ fontWeight: 'medium', color: theme.palette.success.main }}>
                +${totalIncome.toFixed(2)}
              </Typography>
            </Box>
          </Box>
        </Grid>
        
        {/* Gastos */}
        <Grid item xs={12} sm={6} md={4}>
          <Box sx={{ display: 'flex', alignItems: 'center' }}>
            <TrendingDown 
              sx={{ 
                fontSize: 40, 
                color: theme.palette.error.main,
                mr: 2 
              }} 
            />
            <Box>
              <Typography variant="body2" color="textSecondary">
                Gastos (30 días)
              </Typography>
              <Typography variant="h5" sx={{ fontWeight: 'medium', color: theme.palette.error.main }}>
                -${totalExpenses.toFixed(2)}
              </Typography>
            </Box>
          </Box>
        </Grid>
        
        {/* Ratio de Gastos */}
        <Grid item xs={12} sm={6} md={4}>
          <Box sx={{ mt: 2 }}>
            <Typography variant="body2" color="textSecondary" gutterBottom>
              Ratio de Gastos vs Ingresos
            </Typography>
            <Box sx={{ display: 'flex', alignItems: 'center' }}>
              <Box
                sx={{
                  width: '100%',
                  mr: 1,
                  height: 8,
                  borderRadius: 4,
                  bgcolor: 'rgba(0,0,0,0.1)',
                  position: 'relative',
                  overflow: 'hidden'
                }}
              >
                <Box
                  sx={{
                    position: 'absolute',
                    left: 0,
                    top: 0,
                    height: '100%',
                    width: `${Math.min(expenseRatio, 100)}%`,
                    bgcolor: getRatioColor(expenseRatio),
                    borderRadius: 4,
                  }}
                />
              </Box>
              <Typography variant="body2" sx={{ fontWeight: 'bold', color: getRatioColor(expenseRatio) }}>
                {expenseRatio.toFixed(0)}%
              </Typography>
            </Box>
          </Box>
        </Grid>
        
        {/* Categoría Principal */}
        <Grid item xs={12} sm={6} md={4}>
          <Box sx={{ mt: 2, display: 'flex', alignItems: 'center' }}>
            <Category 
              sx={{ 
                fontSize: 24, 
                color: theme.palette.info.main,
                mr: 1 
              }} 
            />
            <Box>
              <Typography variant="body2" color="textSecondary" gutterBottom>
                Categoría Principal de Gasto
              </Typography>
              <Box sx={{ display: 'flex', alignItems: 'center' }}>
                <Chip 
                  label={topCategory} 
                  size="small" 
                  sx={{ mr: 1, bgcolor: theme.palette.info.light, color: theme.palette.info.contrastText }} 
                />
                <Typography variant="body2" sx={{ fontWeight: 'medium' }}>
                  ${topCategoryAmount.toFixed(2)}
                </Typography>
              </Box>
            </Box>
          </Box>
        </Grid>
        
        {/* Próxima Transacción */}
        {upcomingTransaction && (
          <Grid item xs={12} sm={6} md={4}>
            <Box sx={{ mt: 2, display: 'flex', alignItems: 'center' }}>
              <CalendarToday 
                sx={{ 
                  fontSize: 24, 
                  color: theme.palette.warning.main,
                  mr: 1 
                }} 
              />
              <Box>
                <Typography variant="body2" color="textSecondary" gutterBottom>
                  Próxima Transacción Programada
                </Typography>
                <Box>
                  <Typography variant="body2" sx={{ fontWeight: 'medium' }}>
                    {upcomingTransaction.description}
                  </Typography>
                  <Box sx={{ display: 'flex', justifyContent: 'space-between' }}>
                    <Typography variant="caption" color="textSecondary">
                      {upcomingTransaction.date}
                    </Typography>
                    <Typography 
                      variant="caption" 
                      sx={{ 
                        fontWeight: 'bold', 
                        color: upcomingTransaction.amount > 0 ? theme.palette.success.main : theme.palette.error.main 
                      }}
                    >
                      {upcomingTransaction.amount > 0 ? '+' : '-'}${Math.abs(upcomingTransaction.amount).toFixed(2)}
                    </Typography>
                  </Box>
                </Box>
              </Box>
            </Box>
          </Grid>
        )}
      </Grid>
    </Paper>
  );
};

export default FinancialSummary;

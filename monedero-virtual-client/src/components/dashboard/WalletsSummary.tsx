import React from 'react';
import {
  Box,
  Typography,
  Grid,
  Paper,
  Link,
  useTheme
} from '@mui/material';

// Tipos para los monederos
type WalletType = 'PRIMARY' | 'SAVINGS' | 'EXPENSES' | 'INVESTMENT' | 'CUSTOM';

interface Wallet {
  id: number;
  name: string;
  balance: number;
  type: WalletType;
  description: string;
  isDefault?: boolean;
  isFavorite?: boolean;
}

interface WalletsSummaryProps {
  wallets: Wallet[];
  showBalances?: boolean;
}

const WalletsSummary: React.FC<WalletsSummaryProps> = ({
  wallets,
  showBalances = true
}) => {
  const theme = useTheme();

  // Función para obtener el color según el tipo de monedero
  const getWalletColor = (type: WalletType): string => {
    switch (type) {
      case 'PRIMARY':
        return theme.palette.primary.main;
      case 'SAVINGS':
        return theme.palette.success.main;
      case 'EXPENSES':
        return theme.palette.warning.main;
      case 'INVESTMENT':
        return theme.palette.secondary.main;
      case 'CUSTOM':
        return theme.palette.info.main;
      default:
        return theme.palette.grey[500];
    }
  };

  return (
    <Box sx={{ width: '100%' }}>
      <Typography variant="h6" sx={{ mb: 2, fontWeight: 600 }}>
        Mis Monederos
      </Typography>

      <Grid container spacing={2}>
        {wallets.map((wallet) => (
          <Grid item xs={12} sm={6} md={4} lg={3} key={wallet.id}>
            <Paper
              elevation={1}
              sx={{
                p: 2.5,
                textAlign: 'center',
                bgcolor: theme.palette.mode === 'dark' ? 'background.paper' : 'background.default',
                borderRadius: 2,
                position: 'relative',
                overflow: 'hidden',
                minHeight: '120px',
                display: 'flex',
                flexDirection: 'column',
                justifyContent: 'center',
                '&::before': {
                  content: '""',
                  position: 'absolute',
                  top: 0,
                  left: 0,
                  width: '100%',
                  height: '4px',
                  backgroundColor: getWalletColor(wallet.type)
                }
              }}
            >
              <Typography
                variant="body2"
                color="textSecondary"
                sx={{ mb: 0.5 }}
              >
                {wallet.name}
              </Typography>
              <Typography
                variant="caption"
                color="textSecondary"
                sx={{
                  mb: 1,
                  display: 'block',
                  fontSize: '0.65rem',
                  opacity: 0.7
                }}
              >
                ID: {wallet.id}
              </Typography>
              <Typography
                variant="h6"
                sx={{ fontWeight: 600 }}
              >
                {showBalances ? `$${wallet.balance.toFixed(2)}` : '••••••'}
              </Typography>
            </Paper>
          </Grid>
        ))}
      </Grid>

      <Box sx={{ textAlign: 'right', mt: 2 }}>
        <Link
          href="/wallets"
          underline="hover"
          sx={{
            fontSize: '0.875rem',
            fontWeight: 500,
            color: theme.palette.primary.main,
            '&:hover': {
              color: theme.palette.primary.dark
            }
          }}
        >
          Gestionar Monederos →
        </Link>
      </Box>
    </Box>
  );
};

export default WalletsSummary;

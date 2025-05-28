import React, { ReactNode } from 'react';
import { Paper, Typography, Box, useTheme } from '@mui/material';

interface SummaryCardProps {
  title?: string;
  icon?: ReactNode;
  iconColor?: string;
  children: ReactNode;
  fullWidth?: boolean;
}

const SummaryCard: React.FC<SummaryCardProps> = ({ 
  title, 
  icon, 
  iconColor, 
  children, 
  fullWidth = false 
}) => {
  const theme = useTheme();
  
  return (
    <Paper 
      elevation={2}
      sx={{ 
        p: 2, 
        height: '100%',
        display: 'flex',
        flexDirection: 'column',
        bgcolor: theme.palette.mode === 'dark' ? 'background.paper' : 'background.default',
        borderRadius: 3,
        boxShadow: theme.shadows[3],
        gridColumn: fullWidth ? 'span 2' : 'auto'
      }}
    >
      {(title || icon) && (
        <Box sx={{ display: 'flex', alignItems: 'center', mb: 1 }}>
          {icon && (
            <Box 
              sx={{ 
                mr: 1.5, 
                color: iconColor || 'primary.main',
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center',
                fontSize: '1.5rem'
              }}
            >
              {icon}
            </Box>
          )}
          {title && (
            <Typography 
              variant="subtitle2" 
              color="textSecondary"
              sx={{ fontSize: '0.875rem' }}
            >
              {title}
            </Typography>
          )}
        </Box>
      )}
      <Box sx={{ flexGrow: 1 }}>
        {children}
      </Box>
    </Paper>
  );
};

export default SummaryCard;

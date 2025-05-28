import React from 'react';
import { IconButton, Tooltip, useTheme } from '@mui/material';
import { Brightness4, Brightness7 } from '@mui/icons-material';
import { useThemeContext } from '../theme/ThemeContext';

interface ThemeToggleProps {
  size?: 'small' | 'medium' | 'large';
}

const ThemeToggle: React.FC<ThemeToggleProps> = ({ size = 'medium' }) => {
  const { mode, toggleColorMode } = useThemeContext();
  const theme = useTheme();

  return (
    <Tooltip title={mode === 'dark' ? 'Cambiar a modo claro' : 'Cambiar a modo oscuro'}>
      <IconButton
        onClick={toggleColorMode}
        color="inherit"
        size={size}
        sx={{
          transition: 'transform 0.3s',
          '&:hover': {
            transform: 'rotate(30deg)',
          },
        }}
        aria-label="toggle theme"
      >
        {mode === 'dark' ? (
          <Brightness7 sx={{ color: theme.palette.common.white }} />
        ) : (
          <Brightness4 />
        )}
      </IconButton>
    </Tooltip>
  );
};

export default ThemeToggle;

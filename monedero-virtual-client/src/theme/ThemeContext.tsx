import React, { createContext, useState, useEffect, ReactNode } from 'react';
import { ThemeProvider as MuiThemeProvider, createTheme, PaletteMode } from '@mui/material';
import CssBaseline from '@mui/material/CssBaseline';

type ThemeContextType = {
  mode: PaletteMode;
  toggleColorMode: () => void;
};

export const ThemeContext = createContext<ThemeContextType>({
  mode: 'light',
  toggleColorMode: () => {},
});

interface ThemeProviderProps {
  children: ReactNode;
}

export const ThemeProvider: React.FC<ThemeProviderProps> = ({ children }) => {
  // Check localStorage and system preference
  const getInitialMode = (): PaletteMode => {
    const savedMode = localStorage.getItem('theme-mode');
    if (savedMode && (savedMode === 'light' || savedMode === 'dark')) {
      return savedMode;
    }
    
    // Check system preference
    if (window.matchMedia && window.matchMedia('(prefers-color-scheme: dark)').matches) {
      return 'dark';
    }
    
    return 'light';
  };

  const [mode, setMode] = useState<PaletteMode>(getInitialMode);

  // Listen for changes in system preference
  useEffect(() => {
    const mediaQuery = window.matchMedia('(prefers-color-scheme: dark)');
    
    const handleChange = (e: MediaQueryListEvent) => {
      // Only change if user hasn't explicitly set a preference
      if (!localStorage.getItem('theme-mode')) {
        setMode(e.matches ? 'dark' : 'light');
      }
    };
    
    // Add listener for older browsers
    if (mediaQuery.addEventListener) {
      mediaQuery.addEventListener('change', handleChange);
    } else {
      // For older browsers
      mediaQuery.addListener(handleChange);
    }
    
    return () => {
      if (mediaQuery.removeEventListener) {
        mediaQuery.removeEventListener('change', handleChange);
      } else {
        // For older browsers
        mediaQuery.removeListener(handleChange);
      }
    };
  }, []);

  // Save mode to localStorage when it changes
  useEffect(() => {
    localStorage.setItem('theme-mode', mode);
  }, [mode]);

  // Toggle between light and dark mode
  const toggleColorMode = () => {
    setMode((prevMode) => (prevMode === 'light' ? 'dark' : 'light'));
  };

  // Create theme based on current mode
  const theme = createTheme({
    palette: {
      mode,
      ...(mode === 'light'
        ? {
            // Light mode palette
            primary: {
              main: '#1976d2',
            },
            secondary: {
              main: '#9c27b0',
            },
            background: {
              default: '#f5f5f5',
              paper: '#ffffff',
            },
          }
        : {
            // Dark mode palette
            primary: {
              main: '#90caf9',
            },
            secondary: {
              main: '#ce93d8',
            },
            background: {
              default: '#121212',
              paper: '#1e1e1e',
            },
          }),
    },
    components: {
      MuiCard: {
        styleOverrides: {
          root: {
            boxShadow: mode === 'dark' ? '0 4px 20px 0 rgba(0,0,0,0.5)' : '0 4px 20px 0 rgba(0,0,0,0.1)',
          },
        },
      },
      MuiPaper: {
        styleOverrides: {
          root: {
            transition: 'background-color 0.3s, color 0.3s, box-shadow 0.3s',
          },
        },
      },
    },
  });

  return (
    <ThemeContext.Provider value={{ mode, toggleColorMode }}>
      <MuiThemeProvider theme={theme}>
        <CssBaseline />
        {children}
      </MuiThemeProvider>
    </ThemeContext.Provider>
  );
};

// Custom hook to use the theme context
export const useThemeContext = () => React.useContext(ThemeContext);

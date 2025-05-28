import React from 'react';
import { 
  Box, 
  Typography, 
  Card, 
  CardContent, 
  List, 
  ListItem, 
  ListItemIcon, 
  ListItemText,
  Divider,
  Chip,
  useTheme
} from '@mui/material';
import { 
  CheckCircleOutline, 
  MonetizationOn, 
  Speed, 
  Support, 
  Insights,
  Star
} from '@mui/icons-material';
import { UserRank, RANK_COLORS } from '../types/points.types';

interface RankBenefitsProps {
  currentRank: UserRank;
}

// Definición de beneficios por rango con iconos
const RANK_BENEFITS_WITH_ICONS = {
  [UserRank.BRONZE]: [
    { 
      title: 'Cashback básico', 
      description: '0.5% de cashback en todas tus transacciones', 
      icon: <MonetizationOn color="action" />
    },
    { 
      title: 'Funcionalidades estándar', 
      description: 'Acceso a todas las funciones básicas del monedero', 
      icon: <CheckCircleOutline color="action" />
    }
  ],
  [UserRank.SILVER]: [
    { 
      title: 'Cashback mejorado', 
      description: '1% de cashback en todas tus transacciones', 
      icon: <MonetizationOn color="primary" />
    },
    { 
      title: 'Comisiones reducidas', 
      description: '50% de descuento en comisiones por transferencias', 
      icon: <Speed color="primary" />
    },
    { 
      title: 'Notificaciones personalizadas', 
      description: 'Configura alertas según tus preferencias', 
      icon: <CheckCircleOutline color="primary" />
    }
  ],
  [UserRank.GOLD]: [
    { 
      title: 'Cashback premium', 
      description: '2% de cashback en todas tus transacciones', 
      icon: <MonetizationOn color="warning" />
    },
    { 
      title: 'Sin comisiones', 
      description: 'Transferencias sin comisiones entre monederos', 
      icon: <Speed color="warning" />
    },
    { 
      title: 'Atención prioritaria', 
      description: 'Soporte preferencial para tus consultas', 
      icon: <Support color="warning" />
    },
    { 
      title: 'Análisis avanzado', 
      description: 'Acceso a herramientas de análisis de gastos detallado', 
      icon: <Insights color="warning" />
    }
  ],
  [UserRank.PLATINUM]: [
    { 
      title: 'Cashback élite', 
      description: '3% de cashback en todas tus transacciones', 
      icon: <MonetizationOn color="secondary" />
    },
    { 
      title: 'Beneficios exclusivos', 
      description: 'Acceso a ofertas y promociones especiales', 
      icon: <Star color="secondary" />
    },
    { 
      title: 'Asesoramiento personalizado', 
      description: 'Consejos financieros adaptados a tu perfil', 
      icon: <Support color="secondary" />
    },
    { 
      title: 'Eventos VIP', 
      description: 'Invitaciones a eventos exclusivos', 
      icon: <CheckCircleOutline color="secondary" />
    },
    { 
      title: 'Análisis predictivo', 
      description: 'Proyecciones y recomendaciones basadas en tus hábitos', 
      icon: <Insights color="secondary" />
    }
  ],
  [UserRank.DIAMOND]: [
    { 
      title: 'Cashback supremo', 
      description: '5% de cashback en todas tus transacciones', 
      icon: <MonetizationOn color="error" />
    },
    { 
      title: 'Concierge personal', 
      description: 'Servicio de concierge personal 24/7', 
      icon: <Support color="error" />
    },
    { 
      title: 'Beneficios personalizados', 
      description: 'Beneficios adaptados a tus necesidades específicas', 
      icon: <Star color="error" />
    },
    { 
      title: 'Acceso anticipado', 
      description: 'Acceso anticipado a nuevas funcionalidades', 
      icon: <CheckCircleOutline color="error" />
    },
    { 
      title: 'Estatus de por vida', 
      description: 'Mantén tu estatus Diamond de por vida', 
      icon: <Insights color="error" />
    }
  ]
};

const RankBenefits: React.FC<RankBenefitsProps> = ({ currentRank }) => {
  const theme = useTheme();
  
  // Obtener los beneficios del rango actual
  const benefits = RANK_BENEFITS_WITH_ICONS[currentRank];
  
  // Obtener el color del rango actual
  const rankColor = RANK_COLORS[currentRank];
  
  return (
    <Card elevation={3}>
      <Box 
        sx={{ 
          p: 2, 
          background: `linear-gradient(45deg, ${rankColor}88 0%, ${theme.palette.background.paper} 100%)`,
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'space-between'
        }}
      >
        <Typography variant="h6" component="h2">
          Beneficios de tu Rango
        </Typography>
        <Chip 
          label={currentRank} 
          sx={{ 
            bgcolor: rankColor,
            color: '#000',
            fontWeight: 'bold'
          }} 
        />
      </Box>
      <Divider />
      <CardContent>
        <List disablePadding>
          {benefits.map((benefit: { title: string; description: string; icon: React.ReactNode }, index: number) => (
            <React.Fragment key={index}>
              {index > 0 && <Divider variant="inset" component="li" />}
              <ListItem alignItems="flex-start">
                <ListItemIcon>
                  {benefit.icon}
                </ListItemIcon>
                <ListItemText
                  primary={benefit.title}
                  secondary={benefit.description}
                />
              </ListItem>
            </React.Fragment>
          ))}
        </List>
      </CardContent>
    </Card>
  );
};

export default RankBenefits;

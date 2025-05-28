import React, { useState, useEffect } from 'react';
import { 
  Container, 
  Typography, 
  Box, 
  Paper, 
  Grid, 
  CircularProgress, 
  Alert, 
  Tabs, 
  Tab, 
  Divider,
  ToggleButtonGroup,
  ToggleButton,
  useTheme,
  useMediaQuery,
  TextField
} from '@mui/material';
import { format, parseISO } from 'date-fns';

// Componentes de visualización
import EnhancedSpendingChart from '../visualizations/EnhancedSpendingChart';
import SpendingPatternChart from '../visualizations/SpendingPatternChart';

// Servicios y tipos
import AnalysisService from '../services/analysis.service';
import { 
  SpendingAnalysisResponse, 
  AnalysisPeriod, 
  TimeSeriesDataPoint, 
  CategorySpending, 
  SpendingTrend,
  SpendingPatternResponse
} from '../types/analysis';

// Componente para mostrar tendencias
const TrendItem: React.FC<{ trend: SpendingTrend }> = ({ trend }) => {
  const theme = useTheme();
  
  return (
    <Box sx={{ 
      display: 'flex', 
      flexDirection: 'column', 
      alignItems: 'center', 
      p: 2, 
      borderRadius: 1,
      bgcolor: theme.palette.background.paper,
      boxShadow: 1
    }}>
      <Typography variant="subtitle1" gutterBottom>
        {trend.description}
      </Typography>
      <Typography variant="h5" sx={{ fontWeight: 'bold' }}>
        ${trend.amount.toFixed(2)}
      </Typography>
      <Box sx={{ 
        display: 'flex', 
        alignItems: 'center', 
        color: trend.increase ? 
          (trend.description.includes('Gastos') ? theme.palette.error.main : theme.palette.success.main) : 
          (trend.description.includes('Gastos') ? theme.palette.success.main : theme.palette.error.main)
      }}>
        <Typography variant="body2">
          {trend.increase ? '+' : ''}{trend.percentageChange.toFixed(2)}%
        </Typography>
      </Box>
    </Box>
  );
};

// Componente principal
const SpendingAnalysis: React.FC = () => {
  const theme = useTheme();
  const isMobile = useMediaQuery(theme.breakpoints.down('sm'));
  
  // Estados
  const [loading, setLoading] = useState<boolean>(true);
  const [error, setError] = useState<string | null>(null);
  const [analysisData, setAnalysisData] = useState<SpendingAnalysisResponse | null>(null);
  const [period, setPeriod] = useState<AnalysisPeriod>(AnalysisPeriod.LAST_MONTH);
  const [tabValue, setTabValue] = useState<number>(0);
  const [startDate, setStartDate] = useState<Date | null>(null);
  const [endDate, setEndDate] = useState<Date | null>(null);
  const [compareMode, setCompareMode] = useState<boolean>(false);
  const [patternData, setPatternData] = useState<SpendingPatternResponse | null>(null);
  const [patternLoading, setPatternLoading] = useState<boolean>(false);
  const [patternError, setPatternError] = useState<string | null>(null);
  
  // Efecto para cargar datos iniciales
  useEffect(() => {
    loadAnalysisData();
  }, [period, startDate, endDate, compareMode]);
  
  // Función para obtener el rango de fechas según el período
  const getCurrentDateRange = () => {
    const now = new Date();
    let start: Date;
    switch (period) {
      case AnalysisPeriod.LAST_WEEK:
        start = new Date(now);
        start.setDate(now.getDate() - 6);
        break;
      case AnalysisPeriod.LAST_MONTH:
        start = new Date(now);
        start.setMonth(now.getMonth() - 1);
        break;
      case AnalysisPeriod.LAST_THREE_MONTHS:
        start = new Date(now);
        start.setMonth(now.getMonth() - 3);
        break;
      case AnalysisPeriod.LAST_SIX_MONTHS:
        start = new Date(now);
        start.setMonth(now.getMonth() - 6);
        break;
      case AnalysisPeriod.LAST_YEAR:
        start = new Date(now);
        start.setFullYear(now.getFullYear() - 1);
        break;
      case AnalysisPeriod.CUSTOM:
        if (startDate && endDate) {
          return { startDate, endDate };
        }
        start = new Date(now);
        break;
      default:
        start = new Date(now);
        start.setMonth(now.getMonth() - 1);
    }
    return { startDate: start, endDate: now };
  };

  // Efecto para cargar el patrón de gasto con el rango de fechas correcto
  useEffect(() => {
    const fetchPattern = async () => {
      const { startDate: s, endDate: e } = getCurrentDateRange();
      if (!s || !e) return;
      setPatternLoading(true);
      setPatternError(null);
      try {
        const response = await AnalysisService.getUserSpendingPattern(
          s.toISOString(),
          e.toISOString()
        );
        setPatternData(response);
      } catch (err) {
        setPatternError('No se pudo cargar el patrón de gasto.');
      } finally {
        setPatternLoading(false);
      }
    };
    fetchPattern();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [period, startDate, endDate]);
  
  // Función para cargar datos de análisis
  const loadAnalysisData = async () => {
    setLoading(true);
    setError(null);
    
    try {
      let response: SpendingAnalysisResponse;
      
      switch (period) {
        case AnalysisPeriod.LAST_WEEK:
          const weekRange = AnalysisService.getDateRangeFromDays(7);
          response = await AnalysisService.getUserSpendingAnalysis(
            weekRange.startDate, 
            weekRange.endDate
          );
          break;
        case AnalysisPeriod.LAST_MONTH:
          response = await AnalysisService.getUserSpendingLastMonth();
          break;
        case AnalysisPeriod.LAST_THREE_MONTHS:
          const threeMonthsRange = AnalysisService.getDateRangeFromMonths(3);
          response = await AnalysisService.getUserSpendingAnalysis(
            threeMonthsRange.startDate, 
            threeMonthsRange.endDate
          );
          break;
        case AnalysisPeriod.LAST_SIX_MONTHS:
          const sixMonthsRange = AnalysisService.getDateRangeFromMonths(6);
          response = await AnalysisService.getUserSpendingAnalysis(
            sixMonthsRange.startDate, 
            sixMonthsRange.endDate
          );
          break;
        case AnalysisPeriod.LAST_YEAR:
          response = await AnalysisService.getUserSpendingLastYear();
          break;
        case AnalysisPeriod.CUSTOM:
          if (startDate && endDate) {
            if (compareMode) {
              // Calcular período anterior de la misma duración
              const currentStartISO = startDate.toISOString();
              const currentEndISO = endDate.toISOString();
              
              const durationMs = endDate.getTime() - startDate.getTime();
              const previousEndDate = new Date(startDate.getTime());
              const previousStartDate = new Date(previousEndDate.getTime() - durationMs);
              
              response = await AnalysisService.compareUserSpending(
                { startDate: currentStartISO, endDate: currentEndISO },
                { startDate: previousStartDate.toISOString(), endDate: previousEndDate.toISOString() }
              );
            } else {
              response = await AnalysisService.getUserSpendingAnalysis(
                startDate.toISOString(), 
                endDate.toISOString()
              );
            }
          } else {
            throw new Error('Debe seleccionar fechas de inicio y fin para el período personalizado');
          }
          break;
        default:
          response = await AnalysisService.getUserSpendingLastMonth();
      }
      
      setAnalysisData(response);
    } catch (err) {
      console.error('Error al cargar datos de análisis:', err);
      setError('No se pudieron cargar los datos de análisis. Por favor, intente nuevamente.');
    } finally {
      setLoading(false);
    }
  };
  
  // Manejador de cambio de período
  const handlePeriodChange = (
    _: React.MouseEvent<HTMLElement>,
    newPeriod: AnalysisPeriod,
  ) => {
    if (newPeriod !== null) {
      setPeriod(newPeriod);
      
      // Si cambiamos a un período predefinido, limpiar fechas personalizadas
      if (newPeriod !== AnalysisPeriod.CUSTOM) {
        setStartDate(null);
        setEndDate(null);
      } else {
        // Si cambiamos a período personalizado, establecer fechas por defecto
        const today = new Date();
        const lastMonth = new Date();
        lastMonth.setMonth(today.getMonth() - 1);
        
        setStartDate(lastMonth);
        setEndDate(today);
      }
    }
  };
  
  // Manejador de cambio de tab
  const handleTabChange = (_: React.SyntheticEvent, newValue: number) => {
    setTabValue(newValue);
  };
  
  // Manejador de cambio de fecha de inicio
  const handleStartDateChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const { value } = e.target;
    try {
      const date = new Date(value);
      if (!isNaN(date.getTime())) {
        setStartDate(date);
      }
    } catch (err) {
      console.error('Error parsing date:', err);
    }
  };
  
  // Manejador de cambio de fecha de fin
  const handleEndDateChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const { value } = e.target;
    try {
      const date = new Date(value);
      if (!isNaN(date.getTime())) {
        setEndDate(date);
      }
    } catch (err) {
      console.error('Error parsing date:', err);
    }
  };
  
  // Preparar datos para los gráficos
  const prepareChartData = () => {
    if (patternData && patternData.timeSeriesData && patternData.timeSeriesData.length > 0) {
      // Agrupar por fecha y categoría ('Ingresos' y 'Egresos')
      return patternData.timeSeriesData.map(point => ({
        date: parseISO(point.date),
        amount: point.amount,
        category: point.categoryName // Solo 'Ingresos' o 'Egresos'
      }));
    }
    return [];
  };
  
  // Preparar datos para el gráfico de categorías
  const prepareCategoryData = () => {
    // Usar los datos de patrón si están disponibles
    if (patternData && patternData.timeSeriesData && patternData.timeSeriesData.length > 0) {
      return patternData.timeSeriesData.map(point => ({
        date: parseISO(point.date),
        amount: point.amount,
        category: point.categoryName // Solo 'Ingresos' o 'Egresos'
      }));
    }
    return [];
  };
  
  return (
    <Container maxWidth="lg" sx={{ mt: 4, mb: 4 }}>
      <Typography variant="h4" gutterBottom>
        Análisis de Patrones de Gasto
      </Typography>
      
      <Paper sx={{ p: 3, mb: 3 }}>
        <Box sx={{ mb: 3 }}>
          <Typography variant="h6" gutterBottom>
            Período de Análisis
          </Typography>
          
          <ToggleButtonGroup
            value={period}
            exclusive
            onChange={handlePeriodChange}
            aria-label="período de análisis"
            size={isMobile ? "small" : "medium"}
            fullWidth
            sx={{ mb: 2 }}
          >
            <ToggleButton value={AnalysisPeriod.LAST_WEEK}>7 días</ToggleButton>
            <ToggleButton value={AnalysisPeriod.LAST_MONTH}>1 mes</ToggleButton>
            <ToggleButton value={AnalysisPeriod.LAST_THREE_MONTHS}>3 meses</ToggleButton>
            <ToggleButton value={AnalysisPeriod.LAST_SIX_MONTHS}>6 meses</ToggleButton>
            <ToggleButton value={AnalysisPeriod.LAST_YEAR}>1 año</ToggleButton>
            <ToggleButton value={AnalysisPeriod.CUSTOM}>Personalizado</ToggleButton>
          </ToggleButtonGroup>
          
          {period === AnalysisPeriod.CUSTOM && (
            <Grid container spacing={2} alignItems="center">
              <Grid item xs={12} sm={5}>
                <TextField
                  fullWidth
                  label="Fecha de inicio"
                  type="date"
                  value={startDate ? format(startDate, "yyyy-MM-dd") : ''}
                  onChange={handleStartDateChange}
                  size="small"
                  InputLabelProps={{ shrink: true }}
                />
              </Grid>
              <Grid item xs={12} sm={5}>
                <TextField
                  fullWidth
                  label="Fecha de fin"
                  type="date"
                  value={endDate ? format(endDate, "yyyy-MM-dd") : ''}
                  onChange={handleEndDateChange}
                  size="small"
                  InputLabelProps={{ shrink: true }}
                />
              </Grid>
              <Grid item xs={12} sm={2}>
                <ToggleButtonGroup
                  value={compareMode ? 'compare' : 'single'}
                  exclusive
                  onChange={(_, newValue) => {
                    if (newValue !== null) {
                      setCompareMode(newValue === 'compare');
                    }
                  }}
                  aria-label="modo de comparación"
                  size="small"
                  fullWidth
                >
                  <ToggleButton value="single">Simple</ToggleButton>
                  <ToggleButton value="compare">Comparar</ToggleButton>
                </ToggleButtonGroup>
              </Grid>
            </Grid>
          )}
        </Box>
        
        {loading ? (
          <Box sx={{ display: 'flex', justifyContent: 'center', p: 4 }}>
            <CircularProgress />
          </Box>
        ) : error ? (
          <Alert severity="error">{error}</Alert>
        ) : analysisData ? (
          <>
            <Box sx={{ mb: 3 }}>
              <Typography variant="subtitle1" color="text.secondary" gutterBottom>
                Período: {format(parseISO(analysisData.startDate), 'dd/MM/yyyy')} - {format(parseISO(analysisData.endDate), 'dd/MM/yyyy')}
              </Typography>
              
              <Grid container spacing={3} sx={{ mb: 3 }}>
                <Grid item xs={12} sm={4}>
                  <Paper sx={{ p: 2, textAlign: 'center', height: '100%' }}>
                    <Typography variant="subtitle1" color="text.secondary">
                      Gastos Totales
                    </Typography>
                    <Typography variant="h4" sx={{ fontWeight: 'bold', color: theme.palette.error.main }}>
                      ${analysisData.totalExpenses.toFixed(2)}
                    </Typography>
                  </Paper>
                </Grid>
                <Grid item xs={12} sm={4}>
                  <Paper sx={{ p: 2, textAlign: 'center', height: '100%' }}>
                    <Typography variant="subtitle1" color="text.secondary">
                      Ingresos Totales
                    </Typography>
                    <Typography variant="h4" sx={{ fontWeight: 'bold', color: theme.palette.success.main }}>
                      ${analysisData.totalIncome.toFixed(2)}
                    </Typography>
                  </Paper>
                </Grid>
                <Grid item xs={12} sm={4}>
                  <Paper sx={{ p: 2, textAlign: 'center', height: '100%' }}>
                    <Typography variant="subtitle1" color="text.secondary">
                      Flujo de Caja Neto
                    </Typography>
                    <Typography 
                      variant="h4" 
                      sx={{ 
                        fontWeight: 'bold', 
                        color: analysisData.netCashflow >= 0 ? 
                          theme.palette.success.main : 
                          theme.palette.error.main 
                      }}
                    >
                      ${analysisData.netCashflow.toFixed(2)}
                    </Typography>
                  </Paper>
                </Grid>
              </Grid>
              
              {analysisData.trends && analysisData.trends.length > 0 && (
                <Box sx={{ mb: 3 }}>
                  <Typography variant="h6" gutterBottom>
                    Tendencias
                  </Typography>
                  <Grid container spacing={2}>
                    {analysisData.trends.map((trend, index) => (
                      <Grid item xs={12} sm={6} md={4} key={index}>
                        <TrendItem trend={trend} />
                      </Grid>
                    ))}
                  </Grid>
                </Box>
              )}
            </Box>
            
            <Divider sx={{ mb: 3 }} />
            
            <Box sx={{ width: '100%' }}>
              <Box sx={{ borderBottom: 1, borderColor: 'divider', mb: 2 }}>
                <Tabs 
                  value={tabValue} 
                  onChange={handleTabChange} 
                  aria-label="análisis tabs"
                  variant={isMobile ? "scrollable" : "fullWidth"}
                  scrollButtons={isMobile ? "auto" : undefined}
                >
                  <Tab label="Evolución Temporal" />
                  <Tab label="Desglose por Categorías" />
                </Tabs>
              </Box>
              
              {tabValue === 0 && (
                <EnhancedSpendingChart 
                  data={prepareChartData()} 
                  title="Evolución de Ingresos y Gastos"
                  width={isMobile ? window.innerWidth - 64 : 800}
                  height={400}
                />
              )}
              
              {tabValue === 1 && (
                <>
                  <SpendingPatternChart 
                    data={prepareCategoryData()} 
                    title="Desglose por Categorías"
                    width={isMobile ? window.innerWidth - 64 : 600}
                    height={400}
                  />
                  {patternLoading ? (
                    <Box sx={{ display: 'flex', justifyContent: 'center', p: 4 }}>
                      <CircularProgress />
                    </Box>
                  ) : patternError ? (
                    <Alert severity="error">{patternError}</Alert>
                  ) : patternData ? (
                    <>
                      <SpendingPatternChart
                        data={patternData.topCategories.map(cat => ({
                          date: new Date(),
                          amount: cat.amount,
                          category: cat.categoryName
                        }))}
                        title="Principales Categorías de Gasto (Patrón)"
                        width={isMobile ? window.innerWidth - 64 : 600}
                        height={400}
                      />
                      <Box sx={{ mt: 2 }}>
                        <Typography variant="h6">Recomendaciones</Typography>
                        <ul>
                          {patternData.recommendations.map((rec, idx) => (
                            <li key={idx}>{rec}</li>
                          ))}
                        </ul>
                      </Box>
                    </>
                  ) : null}
                </>
              )}
            </Box>
          </>
        ) : (
          <Alert severity="info">No hay datos disponibles para el período seleccionado.</Alert>
        )}
      </Paper>
    </Container>
  );
};

export default SpendingAnalysis;

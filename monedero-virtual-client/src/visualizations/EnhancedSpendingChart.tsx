import React, { useRef, useEffect, useState } from 'react';
import * as d3 from 'd3';
import { 
  Box, 
  Typography, 
  Paper, 
  ToggleButtonGroup, 
  ToggleButton,
  FormControl,
  InputLabel,
  Select,
  MenuItem,
  SelectChangeEvent,
  Grid,
  Tooltip,
  IconButton,
  useTheme,
  useMediaQuery,
  Chip,
  Stack
} from '@mui/material';
import { 
  InfoOutlined, 
  ZoomIn, 
  ZoomOut, 
  RestartAlt 
} from '@mui/icons-material';

interface DataPoint {
  date: Date;
  amount: number;
  category: string;
}

interface EnhancedSpendingChartProps {
  data: DataPoint[];
  title?: string;
  width?: number;
  height?: number;
}

type TimeRange = '7d' | '1m' | '3m' | '6m' | '1y' | 'all';
type ChartType = 'line' | 'bar' | 'area';

const EnhancedSpendingChart: React.FC<EnhancedSpendingChartProps> = ({
  data,
  title = 'Patrones de Gasto',
  width = 800,
  height = 400
}) => {
  const svgRef = useRef<SVGSVGElement>(null);
  const theme = useTheme();
  
  // Puntos de quiebre más específicos para una mejor adaptación
  const isXSmall = useMediaQuery('(max-width:600px)');
  const isSmall = useMediaQuery('(max-width:800px)');
  const isMedium = useMediaQuery('(max-width:1100px)');
  
  // Estados para controles interactivos
  const [timeRange, setTimeRange] = useState<TimeRange>('1m');
  const [chartType, setChartType] = useState<ChartType>('line');
  const [selectedCategory, setSelectedCategory] = useState<string>('all');
  const [zoomLevel, setZoomLevel] = useState<number>(1);
  
  // Obtener categorías únicas de los datos
  const categories = ['all', ...Array.from(new Set(data.map(d => d.category)))];
  
  // Filtrar datos según los controles
  const getFilteredData = () => {
    let filteredData = [...data];
    
    // Filtrar por rango de tiempo
    const now = new Date();
    let startDate = new Date();
    
    switch (timeRange) {
      case '7d':
        startDate.setDate(now.getDate() - 7);
        break;
      case '1m':
        startDate.setMonth(now.getMonth() - 1);
        break;
      case '3m':
        startDate.setMonth(now.getMonth() - 3);
        break;
      case '6m':
        startDate.setMonth(now.getMonth() - 6);
        break;
      case '1y':
        startDate.setFullYear(now.getFullYear() - 1);
        break;
      default:
        // 'all' - no filtering
        startDate = new Date(0); // epoch
    }
    
    filteredData = filteredData.filter(d => d.date >= startDate);
    
    // Filtrar por categoría
    if (selectedCategory !== 'all') {
      filteredData = filteredData.filter(d => d.category === selectedCategory);
    }
    
    return filteredData;
  };
  
  // Manejadores de eventos
  const handleTimeRangeChange = (
    _: React.MouseEvent<HTMLElement>,
    newTimeRange: TimeRange,
  ) => {
    if (newTimeRange !== null) {
      setTimeRange(newTimeRange);
    }
  };
  
  const handleChartTypeChange = (
    _: React.MouseEvent<HTMLElement>,
    newChartType: ChartType,
  ) => {
    if (newChartType !== null) {
      setChartType(newChartType);
    }
  };
  
  const handleCategoryChange = (event: SelectChangeEvent) => {
    setSelectedCategory(event.target.value);
  };
  
  const handleZoomIn = () => {
    setZoomLevel(prev => Math.min(prev + 0.5, 3));
  };
  
  const handleZoomOut = () => {
    setZoomLevel(prev => Math.max(prev - 0.5, 0.5));
  };
  
  const handleResetZoom = () => {
    setZoomLevel(1);
  };
  
  // Efecto para renderizar el gráfico
  useEffect(() => {
    const filteredData = getFilteredData();
    
    if (!filteredData.length || !svgRef.current) {
      // Si no hay datos, mostrar mensaje
      d3.select(svgRef.current).selectAll('*').remove();
      
      const svg = d3
        .select(svgRef.current)
        .attr('width', width)
        .attr('height', height);
        
      svg.append('text')
        .attr('x', width / 2)
        .attr('y', height / 2)
        .attr('text-anchor', 'middle')
        .style('font-size', '16px')
        .style('fill', theme.palette.text.secondary)
        .text('No hay datos suficientes para mostrar');
        
      return;
    }

    // Limpiar el SVG
    d3.select(svgRef.current).selectAll('*').remove();

    // Configuración de márgenes
    const margin = { top: 20, right: 30, bottom: 50, left: 60 };
    const innerWidth = width - margin.left - margin.right;
    const innerHeight = height - margin.top - margin.bottom;

    // Crear el SVG
    const svg = d3
      .select(svgRef.current)
      .attr('width', width)
      .attr('height', height)
      .append('g')
      .attr('transform', `translate(${margin.left},${margin.top})`);

    // Ordenar datos por fecha
    filteredData.sort((a, b) => a.date.getTime() - b.date.getTime());
    
    // Aplicar zoom
    const zoomedWidth = innerWidth * zoomLevel;
    
    // Escalas
    const xScale = d3
      .scaleTime()
      .domain(d3.extent(filteredData, d => d.date) as [Date, Date])
      .range([0, zoomedWidth]);

    const yScale = d3
      .scaleLinear()
      .domain([0, d3.max(filteredData, d => d.amount) as number * 1.1]) // 10% más alto para margen
      .nice()
      .range([innerHeight, 0]);

    const colorScale = d3
      .scaleOrdinal(d3.schemeCategory10)
      .domain(filteredData.map(d => d.category));

    // Ejes
    const xAxis = d3.axisBottom(xScale);
    const yAxis = d3.axisLeft(yScale);

    // Eje X
    svg
      .append('g')
      .attr('transform', `translate(0,${innerHeight})`)
      .call(xAxis)
      .append('text')
      .attr('fill', theme.palette.text.primary)
      .attr('x', innerWidth / 2)
      .attr('y', 40)
      .attr('text-anchor', 'middle')
      .text('Fecha');

    // Eje Y
    svg
      .append('g')
      .call(yAxis)
      .append('text')
      .attr('fill', theme.palette.text.primary)
      .attr('transform', 'rotate(-90)')
      .attr('y', -40)
      .attr('x', -innerHeight / 2)
      .attr('text-anchor', 'middle')
      .text('Monto');

    // Agrupar datos por categoría y día (ignorando la hora)
    const groupedByCategoryAndDay = d3.groups(filteredData, d => d.category, d => d.date.toISOString().split('T')[0]);
    // Convertir a formato plano para graficar: [{category, date, amount}]
    const dailyData: DataPoint[] = [];
    groupedByCategoryAndDay.forEach(([category, days]) => {
      days.forEach(([day, values]) => {
        const total = values.reduce((sum, v) => sum + v.amount, 0);
        dailyData.push({
          category,
          date: new Date(day),
          amount: total
        });
      });
    });
    // Agrupar dailyData por categoría para renderizar
    const nestedDailyData = d3.group(dailyData, (d: DataPoint) => d.category);

    // Renderizar según el tipo de gráfico seleccionado
    if (chartType === 'line' || chartType === 'area') {
      // Línea
      const line = d3
        .line<DataPoint>()
        .x(d => xScale(d.date))
        .y(d => yScale(d.amount))
        .curve(d3.curveMonotoneX);
        
      // Área (para gráfico de área)
      const area = d3
        .area<DataPoint>()
        .x(d => xScale(d.date))
        .y0(innerHeight)
        .y1(d => yScale(d.amount))
        .curve(d3.curveMonotoneX);

      // Dibujar líneas/áreas por categoría
      nestedDailyData.forEach((values: DataPoint[], key: string) => {
        // Ordenar por fecha
        values.sort((a: DataPoint, b: DataPoint) => a.date.getTime() - b.date.getTime());
        // Sanitizar el nombre de la categoría para usarlo como clase CSS
        const safeKey = String(key).replace(/[^a-zA-Z0-9_-]/g, '_');

        if (chartType === 'area') {
          // Dibujar área
          svg
            .append('path')
            .datum(values)
            .attr('fill', `${colorScale(key)}50`) // Color con transparencia
            .attr('stroke', 'none')
            .attr('d', area);
        }
        
        // Dibujar línea
        svg
          .append('path')
          .datum(values)
          .attr('fill', 'none')
          .attr('stroke', colorScale(key) as string)
          .attr('stroke-width', 2)
          .attr('d', line);

        // Añadir puntos
        svg
          .selectAll(`.dot-${safeKey}`)
          .data(values)
          .enter()
          .append('circle')
          .attr('class', `dot-${safeKey}`)
          .attr('cx', d => xScale(d.date))
          .attr('cy', d => yScale(d.amount))
          .attr('r', 4)
          .attr('fill', colorScale(key) as string)
          .append('title') // Tooltip
          .text(d => `${d.category}: $${d.amount.toFixed(2)} (${d.date.toLocaleDateString()})`);
      });
    } else if (chartType === 'bar') {
      // Gráfico de barras
      
      // Agrupar datos por fecha (para barras apiladas)
      const groupedByDate = d3.group(dailyData, d => d.date.toISOString().split('T')[0]);
      
      // Preparar datos para barras apiladas
      const stackData: any[] = [];
      
      groupedByDate.forEach((values, dateStr) => {
        const dateObj = new Date(dateStr);
        const entry: any = { date: dateObj };
        
        values.forEach(v => {
          entry[v.category] = v.amount;
        });
        
        stackData.push(entry);
      });
      
      // Ordenar por fecha
      stackData.sort((a, b) => a.date.getTime() - b.date.getTime());
      
      // Obtener todas las categorías
      const allCategories = Array.from(new Set(dailyData.map(d => d.category)));
      
      // Configurar el stack
      const stack = d3.stack()
        .keys(allCategories)
        .order(d3.stackOrderNone)
        .offset(d3.stackOffsetNone);
      
      // Calcular datos apilados
      const stackedData = stack(stackData);
      
      // Ancho de las barras
      const barWidth = Math.max(innerWidth / stackData.length - 2, 1);
      
      // Dibujar barras apiladas
      stackedData.forEach((layer, i) => {
        svg.selectAll(`.bar-${i}`)
          .data(layer)
          .enter()
          .append('rect')
          .attr('class', `bar-${i}`)
          .attr('x', d => xScale(d.data.date) - barWidth / 2)
          .attr('y', d => yScale(d[1]))
          .attr('height', d => yScale(d[0]) - yScale(d[1]))
          .attr('width', barWidth)
          .attr('fill', colorScale(allCategories[i]) as string)
          .append('title') // Tooltip
          .text(d => `${allCategories[i]}: $${(d[1] - d[0]).toFixed(2)} (${d.data.date.toLocaleString()})`);
      });
    }

  }, [data, width, height, timeRange, chartType, selectedCategory, zoomLevel, theme]);

  // Obtener colores para las categorías
  const getColorForCategory = (category: string) => {
    const colorScale = d3.scaleOrdinal(d3.schemeCategory10);
    return colorScale(category) as string;
  };

  // Filtrar datos para obtener categorías presentes en los datos filtrados
  const filteredData = getFilteredData();
  const categoriesInData = Array.from(new Set(filteredData.map(d => d.category)));

  // Componente de rango de tiempo
  const TimeRangeControl = () => (
    <ToggleButtonGroup
      value={timeRange}
      exclusive
      onChange={handleTimeRangeChange}
      aria-label="time range"
      size="small"
      fullWidth
    >
      <ToggleButton value="7d" aria-label="7 días">7D</ToggleButton>
      <ToggleButton value="1m" aria-label="1 mes">1M</ToggleButton>
      <ToggleButton value="3m" aria-label="3 meses">3M</ToggleButton>
      <ToggleButton value="6m" aria-label="6 meses">6M</ToggleButton>
      <ToggleButton value="1y" aria-label="1 año">1A</ToggleButton>
      <ToggleButton value="all" aria-label="todo">Todo</ToggleButton>
    </ToggleButtonGroup>
  );

  // Componente de tipo de gráfico
  const ChartTypeControl = () => (
    <ToggleButtonGroup
      value={chartType}
      exclusive
      onChange={handleChartTypeChange}
      aria-label="chart type"
      size="small"
      fullWidth
    >
      <ToggleButton value="line" aria-label="línea">Línea</ToggleButton>
      <ToggleButton value="bar" aria-label="barras">Barras</ToggleButton>
      <ToggleButton value="area" aria-label="área">Área</ToggleButton>
    </ToggleButtonGroup>
  );

  // Componente de selección de categoría
  const CategoryControl = () => (
    <FormControl fullWidth size="small">
      <InputLabel id="category-select-label">Categoría</InputLabel>
      <Select
        labelId="category-select-label"
        id="category-select"
        value={selectedCategory}
        label="Categoría"
        onChange={handleCategoryChange}
      >
        {categories.map((category) => (
          <MenuItem key={category} value={category}>
            {category === 'all' ? 'Todas las categorías' : category}
          </MenuItem>
        ))}
      </Select>
    </FormControl>
  );

  return (
    <Paper elevation={3} sx={{ p: 3, mb: 3 }}>
      <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 2 }}>
        <Typography variant="h6">
          {title}
          <Tooltip title="Este gráfico muestra tus patrones de gasto a lo largo del tiempo. Puedes filtrar por período, categoría y tipo de visualización.">
            <IconButton size="small" sx={{ ml: 1 }}>
              <InfoOutlined fontSize="small" />
            </IconButton>
          </Tooltip>
        </Typography>
        
        <Box>
          <IconButton onClick={handleZoomIn} size="small" sx={{ mr: 0.5 }}>
            <ZoomIn />
          </IconButton>
          <IconButton onClick={handleZoomOut} size="small" sx={{ mr: 0.5 }}>
            <ZoomOut />
          </IconButton>
          <IconButton onClick={handleResetZoom} size="small">
            <RestartAlt />
          </IconButton>
        </Box>
      </Box>
      
      {/* Controles responsivos que se reorganizan verticalmente cuando el espacio es limitado */}
      {isXSmall ? (
        // Móvil: siempre vertical
        <Stack spacing={2} sx={{ mb: 2 }}>
          <TimeRangeControl />
          <ChartTypeControl />
          <CategoryControl />
        </Stack>
      ) : isSmall ? (
        // Tablet pequeña: vertical
        <Stack spacing={2} sx={{ mb: 2 }}>
          <TimeRangeControl />
          <ChartTypeControl />
          <CategoryControl />
        </Stack>
      ) : isMedium ? (
        // Tablet/Desktop pequeño: tiempo en una fila, tipo y categoría en otra
        <Stack spacing={2} sx={{ mb: 2 }}>
          <Box>
            <TimeRangeControl />
          </Box>
          <Grid container spacing={2}>
            <Grid item xs={6}>
              <ChartTypeControl />
            </Grid>
            <Grid item xs={6}>
              <CategoryControl />
            </Grid>
          </Grid>
        </Stack>
      ) : (
        // Desktop grande: todos en una fila
        <Grid container spacing={2} sx={{ mb: 2 }}>
          <Grid item xs={4}>
            <TimeRangeControl />
          </Grid>
          <Grid item xs={4}>
            <ChartTypeControl />
          </Grid>
          <Grid item xs={4}>
            <CategoryControl />
          </Grid>
        </Grid>
      )}
      
      <Box sx={{ overflowX: 'auto', overflowY: 'hidden' }}>
        <svg ref={svgRef} />
      </Box>
      
      {/* Leyenda de categorías como chips horizontales */}
      <Box sx={{ display: 'flex', flexWrap: 'wrap', gap: 1, mt: 2, justifyContent: 'center' }}>
        {selectedCategory === 'all' ? (
          categoriesInData.map(category => (
            <Chip
              key={category}
              label={category}
              sx={{
                backgroundColor: `${getColorForCategory(category)}20`,
                color: getColorForCategory(category),
                borderColor: getColorForCategory(category),
                fontWeight: 'medium',
                border: '1px solid'
              }}
              size="small"
              onClick={() => setSelectedCategory(category)}
            />
          ))
        ) : (
          <Chip
            label={selectedCategory}
            sx={{
              backgroundColor: `${getColorForCategory(selectedCategory)}20`,
              color: getColorForCategory(selectedCategory),
              borderColor: getColorForCategory(selectedCategory),
              fontWeight: 'medium',
              border: '1px solid'
            }}
            size="small"
            onDelete={() => setSelectedCategory('all')}
          />
        )}
      </Box>
    </Paper>
  );
};

export default EnhancedSpendingChart;

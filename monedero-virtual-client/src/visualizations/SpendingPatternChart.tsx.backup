import React, { useRef, useEffect } from 'react';
import * as d3 from 'd3';
import { Box, Typography, Paper } from '@mui/material';

interface DataPoint {
  date: Date;
  amount: number;
  category: string;
}

interface SpendingPatternChartProps {
  data: DataPoint[];
  title?: string;
  width?: number;
  height?: number;
}

const SpendingPatternChart: React.FC<SpendingPatternChartProps> = ({
  data,
  title = 'Patrones de Gasto',
  width = 600,
  height = 400
}) => {
  const svgRef = useRef<SVGSVGElement>(null);

  useEffect(() => {
    if (!data.length || !svgRef.current) return;

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

    // Escalas
    const xScale = d3
      .scaleTime()
      .domain(d3.extent(data, d => d.date) as [Date, Date])
      .range([0, innerWidth]);

    const yScale = d3
      .scaleLinear()
      .domain([0, d3.max(data, d => d.amount) as number])
      .nice()
      .range([innerHeight, 0]);

    const colorScale = d3
      .scaleOrdinal(d3.schemeCategory10)
      .domain(data.map(d => d.category));

    // Ejes
    const xAxis = d3.axisBottom(xScale);
    const yAxis = d3.axisLeft(yScale);

    svg
      .append('g')
      .attr('transform', `translate(0,${innerHeight})`)
      .call(xAxis)
      .append('text')
      .attr('fill', 'black')
      .attr('x', innerWidth / 2)
      .attr('y', 40)
      .attr('text-anchor', 'middle')
      .text('Fecha');

    svg
      .append('g')
      .call(yAxis)
      .append('text')
      .attr('fill', 'black')
      .attr('transform', 'rotate(-90)')
      .attr('y', -40)
      .attr('x', -innerHeight / 2)
      .attr('text-anchor', 'middle')
      .text('Monto');

    // Agrupar datos por categoría
    const nestedData = d3.group(data, d => d.category);

    // Línea
    const line = d3
      .line<DataPoint>()
      .x(d => xScale(d.date))
      .y(d => yScale(d.amount))
      .curve(d3.curveMonotoneX);

    // Dibujar líneas por categoría
    nestedData.forEach((values, key) => {
      // Ordenar por fecha
      values.sort((a, b) => a.date.getTime() - b.date.getTime());

      svg
        .append('path')
        .datum(values)
        .attr('fill', 'none')
        .attr('stroke', colorScale(key) as string)
        .attr('stroke-width', 2)
        .attr('d', line);

      // Añadir puntos
      svg
        .selectAll(`.dot-${key.replace(/\s+/g, '-')}`)
        .data(values)
        .enter()
        .append('circle')
        .attr('class', `dot-${key.replace(/\s+/g, '-')}`)
        .attr('cx', d => xScale(d.date))
        .attr('cy', d => yScale(d.amount))
        .attr('r', 4)
        .attr('fill', colorScale(key) as string);
    });

    // Leyenda
    const legend = svg
      .append('g')
      .attr('font-family', 'sans-serif')
      .attr('font-size', 10)
      .attr('text-anchor', 'end')
      .selectAll('g')
      .data(Array.from(nestedData.keys()))
      .enter()
      .append('g')
      .attr('transform', (d, i) => `translate(0,${i * 20})`);

    legend
      .append('rect')
      .attr('x', innerWidth - 19)
      .attr('width', 19)
      .attr('height', 19)
      .attr('fill', d => colorScale(d) as string);

    legend
      .append('text')
      .attr('x', innerWidth - 24)
      .attr('y', 9.5)
      .attr('dy', '0.32em')
      .text(d => d);

  }, [data, width, height]);

  return (
    <Paper elevation={3} sx={{ p: 3, mb: 3 }}>
      <Typography variant="h6" gutterBottom>
        {title}
      </Typography>
      <Box sx={{ overflowX: 'auto' }}>
        <svg ref={svgRef} />
      </Box>
    </Paper>
  );
};

export default SpendingPatternChart;

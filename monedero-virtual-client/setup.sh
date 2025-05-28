#!/bin/bash

# Script para inicializar el proyecto monedero-virtual-client

echo "Inicializando proyecto monedero-virtual-client..."

# Instalar dependencias
echo "Instalando dependencias..."
npm install

# Crear archivo .env para configuración
echo "Creando archivo .env..."
cat > .env << EOL
REACT_APP_API_URL=http://localhost:8080
EOL

echo "Configuración completada."
echo "Para iniciar el proyecto, ejecuta: npm start"

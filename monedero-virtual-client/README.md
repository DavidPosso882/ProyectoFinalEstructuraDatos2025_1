# Monedero Virtual Client

Este proyecto es el frontend para la aplicación "Monedero Virtual con Sistema de Puntos", desarrollado con React, TypeScript, Material UI y Redux.

## Tecnologías utilizadas

- **React**: Biblioteca para construir interfaces de usuario
- **TypeScript**: Superset de JavaScript con tipado estático
- **Material UI**: Biblioteca de componentes de UI para React
- **Redux**: Biblioteca para gestión del estado de la aplicación
- **React Router**: Enrutamiento para aplicaciones React
- **Axios**: Cliente HTTP para realizar peticiones a la API
- **D3.js**: Biblioteca para visualización de datos

## Estructura del proyecto

```
monedero-virtual-client/
  ├── public/                # Archivos públicos
  ├── src/                   # Código fuente
  │   ├── components/        # Componentes reutilizables
  │   ├── pages/             # Páginas de la aplicación
  │   ├── services/          # Servicios para comunicación con la API
  │   ├── store/             # Configuración y slices de Redux
  │   ├── hooks/             # Custom hooks
  │   ├── utils/             # Utilidades
  │   ├── types/             # Definiciones de TypeScript
  │   ├── visualizations/    # Componentes de visualización con D3.js
  │   ├── App.tsx            # Componente principal
  │   └── index.tsx          # Punto de entrada
  ├── package.json           # Dependencias y scripts
  └── tsconfig.json          # Configuración de TypeScript
```

## Requisitos previos

- Node.js (v14 o superior)
- npm o yarn

## Instalación

1. Clona el repositorio
2. Instala las dependencias:

```bash
npm install
# o
yarn install
```

## Ejecución

Para iniciar la aplicación en modo desarrollo:

```bash
npm start
# o
yarn start
```

La aplicación estará disponible en [http://localhost:3000](http://localhost:3000).

## Construcción para producción

```bash
npm run build
# o
yarn build
```

## Comunicación con el backend

La aplicación está configurada para comunicarse con el backend en `http://localhost:8080`. Si el backend está en otra URL, modifica el archivo `src/utils/api-config.ts`.

## Características principales

- Autenticación de usuarios
- Gestión de monederos virtuales
- Realización de transacciones
- Sistema de puntos y beneficios
- Visualización de patrones de gasto
- Notificaciones

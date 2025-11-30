# Cupcake App

Esta app permite realizar pedidos de galletas (cupcakes) eligiendo **cantidad**, **sabor** y **fecha de retiro**.  
Los detalles del pedido se muestran en una **pantalla de resumen** y pueden compartirse con otra app.  

## Características principales
- Selección de cantidad: 1, 6 o 12 galletas.  
- Selección de sabor **localizado** según el idioma del dispositivo.  
- Elección de fecha de retiro (hoy + próximos 3 días).  
- Cálculo dinámico del precio según cantidad, sabor y fecha.  
- Compatibilidad con múltiples idiomas usando `strings.xml`.  

## Notas de implementación
- Los sabores se manejan mediante IDs de recursos de `strings.xml`, permitiendo que la app funcione en cualquier idioma.  
- `OrderViewModel` gestiona el estado del pedido y recalcula el precio automáticamente.  
- Solo se requiere actualizar `strings.xml` para añadir nuevos idiomas o sabores.  

## Pre-requisitos
- Conocimiento básico de Kotlin.  
- Saber crear y ejecutar proyectos en Android Studio.  
- Conocer funciones composables de Jetpack Compose.  

## Cómo empezar
1. Instalar Android Studio si aún no lo tienes.  
2. Descargar el proyecto.  
3. Importar el proyecto en Android Studio.  
4. Compilar y ejecutar la app.

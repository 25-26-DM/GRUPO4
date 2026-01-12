Mars Photos - Versión con Login y Monitoreo
==================================

Mars Photos app es una aplicación de demostración que muestra imágenes reales de la superficie de Marte capturadas por los rovers de la NASA. Los datos se almacenan en un servidor web como un servicio REST.

Esta versión ha sido modificada para incluir funcionalidades de autenticación, personalización de usuario y herramientas de depuración de red.

Modificaciones Recientes
-----------------------

### 1. Sistema de Autenticación (Login)
- **Pantalla de Inicio de Sesión**: Se implementó una `LoginScreen` que incluye el logo oficial del grupo (`logo.png`).
- **Usuarios Autorizados**: El acceso está restringido a una lista de usuarios predefinida en memoria.
- **Validación de Credenciales**: El sistema valida el nombre de usuario y utiliza el apellido como contraseña (con ocultamiento de caracteres).
- **Lista de usuarios admitidos**: Byron Condolo, Pamela Fernandez, Marielena Gonzalez, Angelo Lascano, Ruth Rosero, Joan Santamaria, Dennis Trujillo.

### 2. Personalización y Seguimiento de Sesión
- **Identificación en Pantalla**: En todas las pantallas de la app se muestra el nombre del usuario activo.
- **Registro de Inicio de Sesión**: Se visualiza la hora exacta en la que el usuario inició la sesión en la barra superior (`MarsTopAppBar`).

### 3. Monitoreo y Depuración (Logging)
- **Interceptor de Red**: Se integró `HttpLoggingInterceptor` de OkHttp para visualizar el tráfico JSON en el Logcat de Android Studio.
- **Conteo de Datos**: Se añadió una función de registro en el `MarsViewModel` que imprime el número total de fotos recibidas desde el servidor JSON.
- **Internacionalización**: El mensaje de estado de carga exitosa ha sido traducido al español, mostrando dinámicamente la cantidad de elementos recuperados.

Tecnologías Utilizadas
---------------------
- **Jetpack Compose**: Para la interfaz de usuario moderna y reactiva.
- **Retrofit**: Para realizar peticiones REST al servicio web.
- **OkHttp & Logging Interceptor**: Para la gestión y monitoreo de peticiones HTTP.
- **Kotlinx Serialization**: Para convertir el JSON recibido en objetos Kotlin.
- **Coil**: Para la carga asíncrona de imágenes desde URLs.

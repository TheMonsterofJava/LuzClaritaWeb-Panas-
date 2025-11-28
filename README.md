# Luz Clarita - Pastelería Sin TACC

¡Bienvenido a Luz Clarita! Este proyecto es una aplicación web full-stack para una pastelería artesanal especializada en productos sin TACC (libres de gluten). La plataforma no solo permite a los clientes explorar y comprar productos, sino que también ofrece un sistema de gestión completo para los administradores del negocio.

Este proyecto fue desarrollado como trabajo final de la carrera de programación, demostrando la aplicación de tecnologías modernas para crear una solución de comercio electrónico robusta y funcional.

## ✨ Características Principales

### Para Clientes:
- **Catálogo de Productos:** Explora una amplia variedad de pastelería sin TACC con descripciones detalladas y precios.
- **Carrito de Compras:** Añade productos fácilmente y gestiona tu pedido antes de finalizar la compra.
- **Proceso de Pago Seguro:** Integración con **Mercado Pago** para un proceso de pago fluido y seguro.
- **Registro y Autenticación:** Crea una cuenta o inicia sesión utilizando tu correo electrónico. También puedes registrarte e iniciar sesión rápidamente con tus cuentas de **Google**.
- **Consultas y Contacto:** Envía tus dudas o consultas directamente a través de un formulario en la web.

### Para Administradores (Dashboard):
- **Gestión de Inventario:** Control total sobre el stock de productos, con actualizaciones en tiempo real.
- **Gestión de Productos:** Añade, edita o elimina productos del catálogo.
- **Gestión de Caja:** Realiza un seguimiento de los movimientos de caja, incluyendo ventas y otros ingresos/egresos.
- **Gestión de Compras:** Administra las compras a proveedores y mantén un registro detallado.
- **Reportes de Ventas:** Genera reportes para analizar el rendimiento del negocio.

## 🚀 Tecnologías Utilizadas

Este proyecto fue construido utilizando un stack tecnológico moderno y robusto, dividido entre el backend y el frontend.

### Backend
- **Lenguaje:** **Java 17**
- **Framework:** **Spring Boot 3.5.4**
  - **Spring Web:** Para la creación de la API REST y el manejo de peticiones HTTP.
  - **Spring Data JPA:** Para la persistencia de datos y la comunicación con la base de datos a través del ORM **Hibernate**.
  - **Spring Security:** Para la autenticación y autorización de usuarios, incluyendo la integración con OAuth 2.0 (Google).
  - **Spring Boot Actuator:** Para el monitoreo de la aplicación.
- **Base de Datos:**
  - **MySQL:** Como base de datos principal en producción.
  - **H2 Database:** Para la ejecución de pruebas automatizadas.
- **Motor de Plantillas:** **Thymeleaf** para el renderizado de las vistas del lado del servidor.
- **Herramientas Adicionales:**
  - **Maven:** Para la gestión de dependencias y la construcción del proyecto.
  - **Lombok:** Para reducir el código repetitivo en los modelos de datos.
  - **OpenPDF:** Para la generación de reportes en formato PDF.
  - **Mercado Pago SDK:** Para la integración con la pasarela de pagos.
  - **Jakarta Bean Validation:** Para la validación de los datos de entrada.

### Frontend
- **Lenguajes:** **HTML5, CSS3, JavaScript**
- **Frameworks y Librerías:**
  - **Bootstrap 5.3.3:** Para un diseño responsive y moderno.
  - **jQuery:** Para la manipulación del DOM y la simplificación de scripts.
  - **DataTables:** Para la creación de tablas interactivas en el dashboard.
  - **SweetAlert2:** Para alertas y notificaciones personalizadas.
  - **Select2:** Para mejorar la usabilidad de los campos de selección.
  - **Bootstrap Icons & Boxicons:** Para la iconografía de la interfaz.

### Herramientas de Desarrollo
- **Control de Versiones:** **Git**
- **Testing:** **JUnit**

## ⚙️ Cómo Poner en Marcha el Proyecto Localmente

Para ejecutar este proyecto en tu entorno de desarrollo, sigue estos pasos:

### Prerrequisitos
- **Java JDK 17** o superior.
- **Maven** instalado y configurado en tu PATH.
- **MySQL** instalado y en ejecución.

### 1. Clona el Repositorio
```bash
git clone https://github.com/tu-usuario/LuzClaritaWeb.git
cd LuzClaritaWeb
```

### 2. Configura la Base de Datos
1. Abre tu cliente de MySQL y crea una nueva base de datos.
   ```sql
   CREATE DATABASE LuzClaritaWebOva;
   ```
2. Edita el archivo `src/main/resources/application.properties` y actualiza las siguientes propiedades con tus credenciales de MySQL:
   ```properties
   spring.datasource.url=jdbc:mysql://localhost:3306/LuzClaritaWebOva?useUnicode=true&characterEncoding=UTF-8&useSSL=false&serverTimezone=UTC&createDatabaseIfNotExist=true&allowPublicKeyRetrieval=true
   spring.datasource.username=TU_USUARIO_MYSQL
   spring.datasource.password=TU_CONTRASEÑA_MYSQL
   ```
   *Nota: La propiedad `spring.jpa.hibernate.ddl-auto=update` se encargará de crear y actualizar las tablas automáticamente al iniciar la aplicación.*

### 3. Configura las Variables de Entorno (Opcional)
Para que el inicio de sesión con Google funcione, necesitas configurar las credenciales de OAuth 2.0. Crea las siguientes variables de entorno en tu sistema o en tu IDE:

- `GOOGLE_CLIENT_ID`: Tu Client ID de Google.
- `GOOGLE_CLIENT_SECRET`: Tu Client Secret de Google.

Si no configuras estas variables, la aplicación funcionará, pero el inicio de sesión con Google no estará disponible.

### 4. Construye y Ejecuta el Proyecto
Puedes ejecutar la aplicación utilizando el wrapper de Maven incluido en el proyecto.

- En Windows:
  ```bash
  mvnw spring-boot:run
  ```
- En macOS/Linux:
  ```bash
  ./mvnw spring-boot:run
  ```

Una vez que la aplicación se haya iniciado, podrás acceder a ella en tu navegador en la siguiente URL:
[http://localhost:8081](http://localhost:8081)

¡Y eso es todo! Ahora tienes el proyecto Luz Clarita ejecutándose en tu máquina local.

## 👨‍💻 Autor

**Osvaldo Lovisolo Lopez Garcia** - *Desarrollador Full-Stack*

¡Gracias por visitar mi proyecto! Si tienes alguna pregunta, no dudes en contactarme.

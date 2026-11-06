# WillBook - Backend API

Este es el proyecto backend para el sistema de gestión de bibliotecas **WillBook**. Está desarrollado en **Java con Spring Boot** y expone una API RESTful asegurada mediante **JWT (JSON Web Tokens)**.

## 🛠️ Requisitos Previos

Para ejecutar este proyecto, necesitarás tener instalado:
* **Java 17** (o superior)
* **Maven** (o usar el wrapper `mvnw` incluido en el proyecto)
* **MySQL** (Versión 8 recomendada)

## ⚙️ Configuración del Entorno

El proyecto utiliza un archivo `.env` en la raíz del proyecto para gestionar las variables de entorno de forma segura (como se define en el `application.properties`).

Crea un archivo `.env` en la raíz del proyecto backend con las siguientes variables:

```env
# Configuración de Base de Datos
DB_HOST=localhost
DB_PORT=3306
DB_NAME=willbook_db
DB_USERNAME=tu_usuario_mysql
DB_PASSWORD=tu_password_mysql
JPA_DDL_AUTO=update
JPA_SHOW_SQL=true

# Seguridad (JWT)
# Utiliza una cadena alfanumérica larga (mínimo 32 caracteres / 256 bits)
JWT_SECRET=tu_super_secreto_para_firmar_los_tokens_jwt_aqui
JWT_EXPIRATION=86400000 # 24 horas en milisegundos

# Configuración de Correo (Para recuperación de contraseñas)
MAIL_HOST=smtp.gmail.com
MAIL_PORT=587
MAIL_USERNAME=tu_correo@gmail.com
MAIL_PASSWORD=tu_contraseña_de_aplicacion_gmail

# Frontend URL (CORS)
FRONTEND_URL=http://localhost:4200
CORS_ALLOWED_ORIGINS=http://localhost:4200
```

## 🚀 Cómo ejecutar el proyecto

1. Clona el repositorio.
2. Abre una terminal en la raíz del proyecto backend.
3. Asegúrate de que tu servidor MySQL esté corriendo y de haber creado la base de datos `willbook_db`.
4. Ejecuta la aplicación usando Maven:

```bash
# En Windows
mvnw.cmd spring-boot:run

# En Linux/Mac
./mvnw spring-boot:run
```
La API estará disponible en `http://localhost:8080`.

---

## 📡 Endpoints de la API

La API base es `/api/v1`. A continuación se detallan los controladores y sus rutas:

### 🔐 Autenticación y Seguridad (`/auth`)
* `POST /auth/login` - Inicia sesión y retorna el token JWT y datos de perfil.
* `POST /auth/register` - Registra un nuevo lector en el sistema.
* `POST /auth/forgot-password` - Solicita un enlace por correo para recuperar la contraseña.

### 📚 Catálogo y Libros (`/catalogo`)
* `GET /catalogo` - Lista todo el catálogo o busca por título/autor (Público).
* `GET /catalogo/{id}` - Obtiene los detalles de un libro específico (Público).
* `POST /catalogo/crear` - Registra un nuevo libro *(Requiere Bibliotecario/Admin)*.
* `PUT /catalogo/{id}` - Actualiza la información de un libro *(Requiere Bibliotecario/Admin)*.
* `POST /catalogo/{id}/ejemplares` - Añade un nuevo ejemplar físico al inventario *(Requiere Bibliotecario/Admin)*.

### ✍️ Autores (`/autores`) y 🏷️ Categorías (`/categorias`)
* `GET /autores` - Lista o filtra autores (Público).
* `POST /autores` - Crea un autor *(Requiere Bibliotecario/Admin)*.
* `GET /autores/{id}` - Detalle de un autor.
* `PUT /autores/{id}` - Actualiza un autor *(Requiere Bibliotecario/Admin)*.
* `DELETE /autores/{id}` - Elimina un autor sin dependencias *(Requiere Bibliotecario/Admin)*.
* `GET /categorias` - Lista todas las categorías (Público).
* `POST /categorias` - Crea una nueva categoría *(Requiere Bibliotecario/Admin)*.

### 📖 Préstamos (`/prestamos`)
* `POST /prestamos/solicitar` - El lector solicita el retiro de un libro *(Requiere Lector)*.
* `GET /prestamos/mis-prestamos` - Historial y préstamos activos del usuario actual *(Requiere Lector)*.
* `GET /prestamos/pendientes` - Lista solicitudes pendientes de retiro *(Requiere Bibliotecario/Admin)*.
* `PUT /prestamos/{id}/confirmar` - Confirma el retiro físico del libro *(Requiere Bibliotecario/Admin)*.
* `PUT /prestamos/{id}/cancelar` - Cancela una reserva que no fue retirada *(Requiere Bibliotecario/Admin)*.
* `PUT /prestamos/{id}/reportar-perdida` - Reporta pérdida/daño de un ejemplar.

### ⚠️ Multas (`/multas`)
* `GET /multas/mis-multas` - El lector consulta sus multas y estado de cuenta *(Requiere Lector)*.
* `POST /multas` - Carga una multa manual a un usuario *(Requiere Bibliotecario/Admin)*.
* `PUT /multas/{id}/pagar` - Registra el pago/cobro de una multa *(Requiere Bibliotecario/Admin)*.
* `PUT /multas/{id}/cancelar` - Anula una multa por error administrativo *(Requiere Bibliotecario/Admin)*.

### 👤 Usuarios y Perfil (`/usuarios`)
* `PUT /usuarios/perfil` - Actualiza los datos personales del usuario logueado.
* `PATCH /usuarios/cambiar-password` - Cambia la contraseña validando la anterior.
* `GET /usuarios/buscar` - Busca usuarios por DNI, Nombre o Apellido *(Requiere Bibliotecario/Admin)*.
* `GET /usuarios/{id}/perfil` - Obtiene el perfil completo de un lector (datos, préstamos, multas) *(Requiere Bibliotecario/Admin)*.

### 🔔 Otros
* **Notificaciones (`/notificaciones`):** Obtener bandeja (`GET`), marcar como leída (`PUT /{id}/leer`), marcar todas (`PUT /marcar-todas`).
* **Anuncios (`/anuncios`):** Listar vigentes (`GET`), crear (`POST`).
* **Menú Dinámico (`/menu`):** Obtener las opciones de navegación según el rol (`GET`).
* **Ejemplares (`/ejemplares/{codigo}`):** Dar de baja un ejemplar físico (`DELETE`).
* **Localidades (`/localidades`):** Listar localidades disponibles para el registro (`GET`).

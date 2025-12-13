# OnMind-XDB - Guía Tecnica

> Una base de datos No-SQL rápida en memoria con lenguaje de consulta simple  
> por Cesar Andres Arcila B.

## Tabla de Contenido

1. [Introducción](#introducción)
2. [Instalación](#instalación)
3. [Inicio Rápido](#inicio-rápido)
4. [Estructura del Proyecto](#estructura-del-proyecto)
5. [Interfaz de Usuario](#interfaz-de-usuario)
6. [API y Operaciones](#api-y-operaciones)
7. [Desarrollo](#desarrollo)

---

## Introducción

**OnMind-XDB** está escrito en **Kotlin** (+http4k) y deriva del [proyecto **OnMind**](https://onmind.co), una plataforma preparada durante 7 años (2015-2021) creada por Cesar Andres Arcila.

Utiliza una base de datos embebida (H2) que ejecuta SQL internamente en memoria para consultas, pero las sentencias finalmente usan almacén clave-valor. Esto es posible gracias a su meta-modelo (arquetipos), según el [**Método OnMind**](https://onmind.co/web/blog/es/fundamentals.md).

### Nueva Interfaz de Administración

OnMind-XDB ahora incluye un panel de administración web para gestionar tu base de datos:

- **Dashboard**: Vista general de tu base de datos
- **Colecciones de Datos**: Gestiona tus datos (any)
- **Usuarios y Roles**: Gestión de usuarios (key)
- **Configuraciones**: Parámetros de configuración (set)
- **Sheets**: Define estructuras de datos (kit)

---

## Instalación

### Instalación Binaria

> Es portable, basta con descomprimir...

Para instalar el software basta con descomprimir el archivo `.zip` en una carpeta, por ejemplo, llamada `onmind`

Para ejecutar el software basta con abrir el archivo:
- `onmind-xdb.exe` para Windows
- `onmind-xdb-mac` para macOS  
- `onmind-xdb` para Linux

La primera vez que inicies el software, se requiere dar permisos y se crea una carpeta en el directorio de inicio del usuario para la base de datos llamada: `onmind/xy`

### Instalación para Desarrollo

#### Prerrequisitos
- JDK 17 o superior
- Gradle 8.x o superior

#### Compilar desde Código Fuente

```bash
# Clonar el repositorio
git clone <repository-url>
cd xdb

# Compilar el proyecto
./gradlew build

# Ejecutar la aplicación
./gradlew run

# O crear JAR ejecutable
./gradlew shadowJar
java -jar build/libs/xdb-1.0.0-final2024-full.jar
```

---

## Inicio Rápido

### 1. Iniciar el Servidor

```bash
./gradlew run
```

El servidor se iniciará en el puerto 9990 (configurable en `onmind.ini`)

### 2. Acceder a la Interfaz de Administración

Abrir el navegador en: `http://localhost:9990/_/`

### 3. Usar la API

La API REST está disponible en: `http://localhost:9990/abc`

#### Ejemplo de Request API

```bash
curl -X POST http://localhost:9990/abc \
  -H "Content-Type: application/json" \
  -d '{
    "what": "find",
    "from": "xyany",
    "some": "PRODUCTS.SHEET",
    "size": "100"
  }'
```

### 4. Probar la API

```bash
./test-api.sh
```

---

## Estructura del Proyecto

### Directorios Principales

```
xdb/
├── src/main/
│   ├── kotlin/
│   │   ├── onmindxdb.kt                    # Punto de entrada principal
│   │   └── co/onmind/
│   │       ├── api/
│   │       │   └── AbcAPI.kt               # API REST principal
│   │       ├── app/                        # Módulo UI
│   │       │   └── AppUI.kt                # Controlador de UI
│   │       ├── auth/                       # Módulo Authenticacion
│   │       ├── db/                         # Operaciones de base de datos
│   │       ├── io/                         # Modelos I/O (DTO's)
│   │       ├── kv/                         # Almacén clave-valor
│   │       ├── trait/                      # Clases base
│   │       ├── util/                       # Utilidades
│   │       └── xy/                         # Modelos de entidades
│   └── resources/
│       ├── kte/                            # Templates (JTE con Kotlin)
│       │   ├── layout.kte                  # Layout base
│       │   ├── dashboard.kte               # Vista dashboard
│       │   ├── data-list.kte               # Lista de colecciones
│       │   ├── data-view.kte               # Vista de registros
│       │   ├── users-list.kte              # Lista de usuarios
│       │   ├── settings-list.kte           # Lista de configuraciones
│       │   └── sheets-list.kte             # Lista de sheets
│       └── static/                         # Recursos estaticos (js/abcapi.js)
└── build.gradle.kts                        # Configuración de build
```

### 🏗️ Arquitectura

```
┌─────────────────────────────────────────────────────────────┐
│                         Browser                             │
│                    http://localhost:9990                    │
└────────────────────────┬────────────────────────────────────┘
                         │
                         ▼
┌─────────────────────────────────────────────────────────────┐
│                      http4k Server                          │
│                     (SunHttp/Netty)                         │
└────────────┬────────────────────────────┬───────────────────┘
             │                            │
             ▼                            ▼
┌────────────────────────┐    ┌──────────────────────────────┐
│      API Routes        │    │       UI Routes              │
│      /abc (POST)       │    │       /_/ (GET)              │
│      /abc (GET)        │    │       /_/data (GET)          │
│      /swagger (GET)    │    │       /_/users (GET)         │
└──────────┬─────────────┘    │       /_/settings (GET)      │
           │                  │       /_/sheets (GET)        │
           ▼                  └──────────┬───────────────────┘
┌────────────────────────┐               │
│      AbcAPI.kt         │               ▼
│   - find()             │    ┌──────────────────────────────┐
│   - insert()           │    │       AppUI.kt               │
│   - update()           │    │   - dashboard()              │
│   - delete()           │    │   - dataList()               │
│   - create()           │    │   - dataView()               │
│   - drop()             │    │   - usersList()              │
│   - list()             │    │   - settingsList()           │
│   - define()           │    │   - sheetsList()             │
└──────────┬─────────────┘    └──────────┬───────────────────┘
           │                             │
           │                             ▼
           │                  ┌──────────────────────────────┐
           │                  │   KTE - Java Template Engine │
           │                  │   - layout.kte               │
           │                  │   - dashboard.kte            │
           │                  │   - data-*.kte               │
           │                  │   - users-list.kte           │
           │                  └──────────────────────────────┘
           │
           ▼
┌─────────────────────────────────────────────────────────────┐
│                         RDB.kt                              │
│                   Database Operations                       │
└────────────┬────────────────────────────┬───────────────────┘
             │                            │
             ▼                            ▼
┌────────────────────────┐    ┌──────────────────────────────┐
│   H2 Database          │    │    MVStore (Persistence)     │
│   (In-Memory)          │    │    Key-Value Store           │
│   - xyany (data)       │    │    - savePointAny()          │
│   - xykey (users)      │    │    - savePointKey()          │
│   - xyset (settings)   │    │    - savePointSet()          │
│   - xykit (sheets)     │    │    - savePointKit()          │
└────────────────────────┘    └──────────────────────────────┘
```

---

## Interfaz de Usuario

### Stack Tecnológico

- **JTE** (Java Template Engine) - Templates del lado del servidor (con Kotlin)
- **Tailwind CSS** - Estilos utilitarios (archivo local)
- **Lucide Icons** - Iconografía
- **JavaScript vanilla** - Funcionalidad básica (tema, sidebar, filtros)

### Rutas Disponibles

| Ruta | Descripción | Entidad |
|------|-------------|---------|
| `/_/` | Dashboard principal | - |
| `/_/data` | Lista de colecciones de datos | `any` |
| `/_/data/{sheet}` | Vista de registros de una colección | `any` |
| `/_/users` | Lista de usuarios y roles | `key` |
| `/_/settings` | Lista de configuraciones | `set` |
| `/_/sheets` | Lista de definiciones de sheets | `kit` |

### Características Actuales
- Ver colecciones de datos
- Ver usuarios y roles
- Ver configuraciones
- Ver definiciones de sheets
- Vista general del dashboard

### Características Planificadas
- Operaciones CRUD para todas las entidades
- Formularios interactivos
- Búsqueda y filtrado mejorados
- Soporte bilingüe (EN/ES)
- Notificaciones toast

---

## API y Operaciones

### Meta-modelo OnMind

#### Entidades y Prefijos

| Prefijo | Tabla  | Descripción | Sección UI |
|---------|--------|-------------|------------|
| `kit`   | xykit  | Definiciones de sheets | Sheets |
| `key`   | xykey  | Usuarios y roles | Users |
| `set`   | xyset  | Configuraciones | Settings |
| `any`   | xyany  | Datos dinámicos | Data |
| `doc`   | xydoc  | Documentos | (No implementado) |

#### Operaciones Disponibles

| Operación | Descripción | Ejemplo |
|-----------|-------------|---------|
| `find`   | Buscar registros | Encontrar productos |
| `insert` | Crear registro | Agregar nuevo producto |
| `update` | Actualizar registro | Modificar producto |
| `delete` | Eliminar registro | Remover producto |
| `create` | Crear sheet | Definir nueva colección |
| `drop`   | Eliminar sheet | Remover colección |
| `define` | Definir sheet | Definir mapeo de atributos |
| `list`   | Listar sheets | Mostrar todas las colecciones |
| `signup` | Crear usuario | Registrar nuevo usuario |
| `signin` | Autenticar | Login de usuario |
| `whoami` | Info del sistema | Obtener info del servidor |

### Endpoints

#### API Endpoints
```
POST /abc                 # Endpoint principal de API
GET  /abc                 # Estado del servicio
GET  /swagger             # Documentación API (modo dev)
```

#### UI Endpoints
```
GET  /                    # Redirect a /_/
GET  /_/                  # Dashboard
GET  /_/data              # Lista de colecciones de datos
GET  /_/data/{sheet}      # Vista de registros de datos
GET  /_/users             # Lista de usuarios
GET  /_/settings          # Lista de configuraciones
GET  /_/sheets            # Lista de sheets
```

---

## Funcionalidades JavaScript Claves

### 1. Sistema de Temas

```javascript
function toggleTheme() {
    const isDark = document.body.classList.contains('dark');
    const newTheme = isDark ? 'light' : 'dark';
    localStorage.setItem('onmind-xdb-theme', newTheme);
    applyTheme(newTheme);
}
```

### 2. Sidebar Responsivo

```javascript
function toggleSidebar() {
    document.getElementById('sidebar').classList.toggle('collapsed');
    document.getElementById('mainContent').classList.toggle('expanded');
    document.getElementById('toggleBtn').classList.toggle('collapsed');
}
```

### 3. Modales para Crear Entidades

```javascript
// Ejemplo: Modal "New User"
window.openNewUserModal = function() {
    document.getElementById('newUserModal').classList.remove('hidden');
    lucide.createIcons();
}

window.closeNewUserModal = function() {
    document.getElementById('newUserModal').classList.add('hidden');
    // Limpiar campos del formulario
    document.getElementById('userName').value = '';
    document.getElementById('userEmail').value = '';
}

window.createUser = async function() {
    const name = document.getElementById('userName').value.trim();
    const email = document.getElementById('userEmail').value.trim();
    
    if (!name || !email) {
        showToast('Name and email are required');
        return;
    }
    
    const result = await abc.signup('/abc', { some: name, user: email, with: 'USER' });
    
    if (result.ok) {
        showToast('User created successfully');
        closeNewUserModal();
        setTimeout(() => window.location.reload(true), 800);
    } else {
        showToast(result.message || 'Error creating user');
    }
}
```
---

## Autenticación y Autorización

### Resumen

OnMind-XDB usa **autenticación básica HTTP por defecto** configurada desde `onmind.ini`.

- **Usuario por defecto**: `admin`
- **Contraseña por defecto**: `admin`
- **Tipo**: HTTP Basic Authentication

### Proveedores Soportados

1. **Basic** (Default) - Autenticación HTTP Basic
2. **Authelia** - Autenticación corporativa con headers
3. **AWS Cognito** - Autenticación cloud con JWT

### Configuración en onmind.ini

#### Autenticación Básica (Default)
```ini
# Habilitada por defecto
auth.enabled = true
auth.type = BASIC
auth.basic.user = admin
auth.basic.pass = admin
```

#### Sin Autenticación
```ini
auth.enabled = false
```

#### Con Authelia
```ini
auth.enabled = true
auth.type = AUTHELIA
auth.authelia.url = https://auth.example.com
```

#### Con AWS Cognito
```ini
auth.enabled = true
auth.type = COGNITO
auth.cognito.region = us-east-1
auth.cognito.user_pool_id = us-east-1_XXXXXXXXX
auth.cognito.client_id = xxxxxxxxxxxxxxxxxxxxxxxxxx
```

### Uso

#### Acceder a la UI
Al acceder a `http://localhost:9990/_/`, el navegador pedirá usuario y contraseña.

#### Cambiar Credenciales
Editar `~/onmind/onmind.ini`:
```ini
auth.basic.user = miusuario
auth.basic.pass = mipassword
```

### Arquitectura de Autenticación

```
Request → BasicAuthProvider.filter()
    ↓
Valida Authorization: Basic header
    ↓
Decodifica Base64 (user:pass)
    ↓
Compara con auth.basic.user y auth.basic.pass
    ↓
Si válido: Agrega X-Auth-User header → Routes
Si inválido: 401 Unauthorized + WWW-Authenticate header
```

**Proveedores disponibles:**
- `BasicAuthProvider`: Valida usuario/contraseña con HTTP Basic Auth
- `NoAuthProvider`: Sin autenticación (cuando auth.enabled=false)
- `AutheliaProvider`: Lee headers Remote-User, Remote-Email, Remote-Groups
- `CognitoProvider`: Valida JWT token de AWS Cognito

### Uso en el Código

```kotlin
// En cualquier handler
val authUser = request.header("X-Auth-User") ?: "anonymous"

// Extension function en AppUI
val user = req.authUser()
```

---

## Connection Pool con Agroal

### Beneficios

OnMind-XDB usa **Agroal** como connection pool para mejorar el rendimiento:

**Ventajas:**
- **Múltiples conexiones concurrentes**: 10 usuarios simultáneos sin bloqueos
- **Respuestas más eficientes**: 5-10x más rápido que conexiones directas
- **Reutilización de conexiones**: ~1-5ms vs ~50-100ms crear nueva
- **Gestión automática**: `.use {}` cierra conexiones automáticamente
- **Ligero**: Solo 120KB, ideal para uso embebido con OnMind-XDB

### Configuración

```properties
# onmind.ini
db.max_pool_size = 10    # Máximo de conexiones concurrentes
db.query_limit = 1200    # Límite de registros por query
```

**Recomendaciones:**
- Desarrollo: `max_pool_size = 5`
- Producción: `max_pool_size = 20`
- Cloud/Serverless: `max_pool_size = 2`

---

## Desarrollo

### Comandos Útiles

```bash
# Compilar proyecto
./gradlew build

# Ejecutar aplicación
./gradlew run

# Crear JAR ejecutable
./gradlew shadowJar

# Limpiar build
./gradlew clean

# Ejecutar tests
./gradlew test

# Generar templates JTE
./gradlew generateJte

# Ejecutar JAR
java -jar build/libs/xdb-1.0.0-final2024-full.jar

# Probar API
./test-api.sh
```

### Configuración

#### onmind.ini
```ini
# Ubicación: ~/onmind/onmind.ini (auto-generado)
app.mode = production
app.local = /Users/home/onmind/
dai.port = 9990
db.driver = 0  # 0=H2, 6=DuckDB
kv.store = mvstore
```

### Testing

#### Para Probar la UI
1. Compilar: `./gradlew build`
2. Ejecutar: `./gradlew run`
3. Abrir: `http://localhost:9990/_/`

---

### Licencia

Este proyecto está bajo la Licencia Apache 2.0 - ver el archivo [LICENSE.md](LICENSE.md) para detalles.

---

**Última actualización**: 2025  
**Versión**: 0.9.0

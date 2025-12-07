# OnMind-XDB - Guía Tecnica

> Una base de datos No-SQL rápida en memoria con lenguaje de consulta simple  
> por Cesar Andres Arcila

## 📋 Tabla de Contenidos

1. [Introducción](#introducción)
2. [Instalación](#instalación)
3. [Inicio Rápido](#inicio-rápido)
4. [Estructura del Proyecto](#estructura-del-proyecto)
5. [Interfaz de Usuario](#interfaz-de-usuario)
6. [API y Operaciones](#api-y-operaciones)
7. [Ejemplos HTMX](#ejemplos-htmx)
8. [Próximos Pasos](#próximos-pasos)
9. [Desarrollo](#desarrollo)

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
- Gradle 7.x o superior

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
│   │       │   ├── AppUI.kt                # Controlador de UI
│   │       │   ├── README.md               # Documentación del módulo
│   │       │   └── HTMX_EXAMPLES.md        # Ejemplos de HTMX
│   │       ├── db/                         # Operaciones de base de datos
│   │       ├── io/                         # Modelos I/O
│   │       ├── kv/                         # Almacén clave-valor
│   │       ├── trait/                      # Clases base
│   │       ├── util/                       # Utilidades
│   │       └── xy/                         # Modelos de dominio
│   └── resources/
│       ├── jte/                            # Templates JTE
│       │   ├── layout.jte                  # Layout base
│       │   ├── dashboard.jte               # Vista dashboard
│       │   ├── data-list.jte               # Lista de colecciones
│       │   ├── data-view.jte               # Vista de registros
│       │   ├── users-list.jte              # Lista de usuarios
│       │   ├── settings-list.jte           # Lista de configuraciones
│       │   └── sheets-list.jte             # Lista de sheets
│       └── application.conf                # Configuración de app
├── build.gradle.kts                        # Configuración de build
└── test-api.sh                             # Script de prueba API
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
           │                  │    JTE Template Engine       │
           │                  │   - layout.jte               │
           │                  │   - dashboard.jte            │
           │                  │   - data-*.jte               │
           │                  │   - users-list.jte           │
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

- **JTE** (Java Template Engine) - Templates del lado del servidor
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
| `find` | Buscar registros | Encontrar productos |
| `insert` | Crear registro | Agregar nuevo producto |
| `update` | Actualizar registro | Modificar producto |
| `delete` | Eliminar registro | Remover producto |
| `create` | Crear sheet | Definir nueva colección |
| `drop` | Eliminar sheet | Remover colección |
| `list` | Listar sheets | Mostrar todas las colecciones |
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

## Funcionalidades JavaScript

### 1. Sistema de Temas

```javascript
function toggleTheme() {
    const isDark = document.body.classList.contains('dark');
    const newTheme = isDark ? 'light' : 'dark';
    localStorage.setItem('onmind-xdb-theme', newTheme);
    applyTheme(newTheme);
}
```

### 2. Filtrado de Tablas

```javascript
function filterTable(input, tableId) {
    const filter = input.value.toLowerCase();
    const table = document.getElementById(tableId);
    const rows = table.getElementsByTagName('tr');
    
    for (let i = 0; i < rows.length; i++) {
        const cells = rows[i].getElementsByTagName('td');
        let found = false;
        
        for (let j = 0; j < cells.length; j++) {
            const cell = cells[j];
            if (cell && cell.textContent.toLowerCase().indexOf(filter) > -1) {
                found = true;
                break;
            }
        }
        
        rows[i].style.display = found || cells.length === 0 ? '' : 'none';
    }
}
```

### 3. Notificaciones Toast

```javascript
function showToast(message) {
    const toast = document.createElement('div');
    toast.className = 'toast';
    toast.innerHTML = '<i data-lucide="info" class="w-5 h-5"></i><span>' + message + '</span>';
    document.body.appendChild(toast);
    lucide.createIcons();
    
    setTimeout(function() {
        toast.classList.add('hide');
        setTimeout(function() { toast.remove(); }, 300);
    }, 3000);
}
```

### 4. Sidebar Responsivo

```javascript
function toggleSidebar() {
    document.getElementById('sidebar').classList.toggle('collapsed');
    document.getElementById('mainContent').classList.toggle('expanded');
    document.getElementById('toggleBtn').classList.toggle('collapsed');
}
```

### 5. Modales para Crear Entidades

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

### Dependencias Principales

```kotlin
// HTTP & Routing
implementation("org.http4k:http4k-core:5.47.0.0")
implementation("org.http4k:http4k-format-jackson:5.47.0.0")

// Database
implementation("com.h2database:h2:2.3.232")

// Template Engine
implementation("gg.jte:jte:3.1.9")
implementation("gg.jte:jte-kotlin:3.1.9")

// JSON
implementation("com.fasterxml.jackson.core:jackson-databind:2.18.3")
implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.18.3")
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
**Versión**: 0.7.0

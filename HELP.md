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
7. [Coherencia y Monitoreo](#coherencia-y-monitoreo)
8. [Desarrollo](#desarrollo)

---

## Introducción

**OnMind-XDB** está escrito en **Kotlin** (+http4k) y deriva del [proyecto **OnMind**](https://onmind.co), una plataforma preparada durante 7 años (2015-2021) creada por Cesar Andres Arcila.

Utiliza una base de datos embebida (H2) que ejecuta SQL internamente en memoria para consultas, pero las sentencias finalmente usan almacén clave-valor. Esto es posible gracias a su meta-modelo (arquetipos), según el [**Método OnMind**](https://onmind.co/web/blog/es/fundamentals.md).

### Caracteristicas Técnicas

**Filosofía**: Simplicidad, portabilidad, rendimiento, zero configuración

**Arquitectura Core**
- Stack: Kotlin + http4k + H2 (in-memory) + MVStore/DynamoDB/CosmosDB + JTE templates
- Patrón: In-memory SQL + Key-value persistence (dual storage)
- Meta-modelo: OnMind Method con arquetipos (kit, key, set, any, doc)

**Estructura de Datos**
- xykit: Definiciones de sheets (tipo: SHEET/SETUP)
- xykey: Usuarios y roles
- xyset: Configuraciones
- xyany: Datos dinámicos
- xydoc: Documentos

**Componentes Principales Backend**
- AbcAPI.kt: Controlador principal (find, insert, update, delete, create, drop, define, list, signup, signin, whoami)
- RDB.kt: Gestión dual storage (H2 + MVStore), savePoint/movePoint
- AppUI.kt: Rutas UI (/app/*, /app/data, /app/users, /app/settings, /app/sheets)
- trait/AuthProvider.kt: Interfaz de autenticación (Strategy pattern)
- auth/*Plug.kt: BasicAuthPlug (default), AutheliaPlug, CognitoPlug, OIDCPlug, NoAuthPlug
- Agroal: Connection pool (max_pool_size=10, query_limit=1200)
- kv/*Plug.kt: MVStorePlug (default), DynamoPlug, CosmosPlug

**Componentes Principales Frontend**
- JTE templates (.kte): layout, dashboard, data-list, data-view, users-list, settings-list, sheets-list, error
- Tailwind CSS: Utilidades + tema dark/light
- onmind-cui-v2: Web Components (as-datagrid, as-confirm, as-button, as-input, as-select)
- Lucide Icons: Iconografía
- ACE Editor: Editor JSON en dashboard

**Configuración (onmind.ini)**
```ini
app.mode=production
app.ui=+
app.logger=-  # +: log to file, -: log to console
dai.port=9990
db.driver=0 (H2)
db.max_pool_size=10
db.query_limit=1200
auth.enabled=true
auth.type=BASIC
kv.store=mvstore
```

**Flujo de Datos**
```
Request → AuthProvider.filter() → Routes
AbcAPI → RDB.forQuery/forUpdate (H2)
RDB.savePoint → KVStore (MVStore/DynamoDB/CosmosDB)
```

**Operaciones API**
- find: SELECT con filtros
- insert/update/delete: CRUD en tablas
- create: Crear sheet (genera kit01=CODE.SCHEME)
- drop: Eliminar sheet (valida sin datos, elimina de H2 y KVStore por id)
- define: Actualizar spec de sheet (mapeo de datos)
- list: Listar sheets por esquema

**Rendimiento mínimo estimado**
- Startup: ~2s (JVM), ~10ms (GraalVM Native)
- Memory: ~150MB (JVM), ~20MB (Native)
- Queries: 10000 ops/s (con Agroal pool)

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
│   │       ├── auth/                       # Módulo Autenticación
│   │       │   ├── AuthConfig.kt           # Configuración de auth
│   │       │   ├── BasicAuthPlug.kt        # HTTP Basic Auth
│   │       │   ├── NoAuthPlug.kt           # Sin autenticación
│   │       │   ├── AutheliaPlug.kt         # Authelia provider
│   │       │   └── CognitoPlug.kt          # AWS Cognito provider
│   │       ├── db/                         # Operaciones de base de datos
│   │       ├── io/                         # Modelos I/O (DTO's)
│   │       ├── kv/                         # Almacén clave-valor
│   │       │   ├── KVStoreFactory.kt       # Factory para KV stores
│   │       │   ├── MVStorePlug.kt          # H2 MVStore (default)
│   │       │   ├── CosmosPlug.kt           # Azure CosmosDB provider
│   │       │   └── DynamoPlug.kt           # AWS DynamoDB provider
│   │       ├── trait/                      # Interfaces (Strategy pattern)
│   │       │   ├── AuthProvider.kt         # Interfaz de autenticación
│   │       │   └── KVStore.kt              # Interfaz de almacenamiento
│   │       ├── util/                       # Utilidades
│   │       │   ├── Rote.kt                 # Configuración y rutas
│   │       │   ├── Trace.kt                # Logging
│   │       │   └── Swagger.kt              # Swagger UI
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

#### Coherence & Monitoring Endpoints
```
GET  /api/store/coherence        # Estadísticas de coherencia
POST /api/store/coherence/verify # Verificación completa de coherencia
POST /api/store/coherence/sync   # Sincronización forzada desde disco
GET  /api/store/health           # Estado de salud del sistema
GET  /api/trace/stats            # Estadísticas de logging
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

## Almacenamiento y Persistencia

OnMind-XDB es agnóstico al almacenamiento de persistencia (KVStore). Soporta múltiples motores mediante un sistema de conectores.

### Motores Soportados

1. **MVStore (Default)**: Almacenamiento local en archivo (basado en H2).
2. **DynamoDB**: Almacenamiento en la nube de AWS.
3. **Cosmos DB**: Almacenamiento en la nube de Azure.
4. **RocksDB**: Almacenamiento en disco solido (Por ahora inactivo).

### Configuración en onmind.ini

#### MVStore (Local)
```ini
kv.store = mvstore
kv.mvstore.name = xybox
```

#### AWS DynamoDB
```ini
kv.store = dynamodb
kv.dynamodb.table = onmind-xdb
kv.dynamodb.region = us-east-1
```

#### Azure Cosmos DB
```ini
kv.store = cosmosdb
kv.cosmosdb.endpoint = https://your-account.documents.azure.com:443/
kv.cosmosdb.key = your-primary-key
kv.cosmosdb.database = onmindxdb
kv.cosmosdb.container = kvstore
```

---

## Coherencia y Monitoreo

### Sistema de Coherencia

OnMind-XDB implementa un **sistema de coherencia optimizado** entre la base de datos en memoria (H2) y el almacenamiento persistente (KVStore: MVStore/DynamoDB/CosmosDB). Este sistema garantiza que los datos estén sincronizados entre ambas capas de almacenamiento **sin impacto en rendimiento** cuando está desactivado.

#### Arquitectura del Sistema de Coherencia

El sistema utiliza **contadores incrementales en memoria (O(1))** para evitar full-scans del KVStore en cada verificación:

```
┌─────────────────┐     Incremental Counters        ┌──────────────────┐
│   H2 (Memory)   │ ◄─────────────────────────────► │   KVStore (Disk) │
│  - SQL queries  │   memoryCounts[entity]          │  - MVStore       │
│  - Fast reads   │   diskCounts[entity]            │  - DynamoDB      │
│                 │                                 │  - CosmosDB      │
└─────────────────┘                                 └──────────────────┘
         │                                                   │
         └────────── CoherenceStore (lock.read) ─────────────┘
                    Atomic O(1) verification
```

**Optimizaciones clave:**
- **Contadores incrementales**: `memoryCounts` y `diskCounts` actualizados en cada INSERT/UPDATE/DELETE/CREATE/DROP
- **Verificación O(1)**: `verifyCoherenceQuick()` lee solo contadores atómicos, sin full-scan
- **Zero-overhead**: Funciones `inline` con early-return cuando `db.check=-`
- **Concurrencia segura**: `ReentrantReadWriteLock` - múltiples lectores concurrentes, escritor exclusivo solo en `forceSyncFromDisk()`

#### Inicialización y Hidratación

Al arrancar, `readPoint()` carga datos desde KVStore a H2, luego `resyncCounters()` hidrata los contadores incrementales con un full-scan **único** (solo al inicio o en `forceSyncFromDisk()`).

### Endpoints de Coherencia

#### GET /api/store/coherence

Obtiene estadísticas detalladas de coherencia para todas las entidades del sistema (O(1) desde contadores).

**Respuesta:**
```json
{
  "overall_coherent": true,
  "entities": {
    "any": { "memory_count": 150, "disk_count": 150, "coherent": true },
    "key": { "memory_count": 5, "disk_count": 5, "coherent": true },
    "set": { "memory_count": 12, "disk_count": 12, "coherent": true },
    "kit": { "memory_count": 8, "disk_count": 8, "coherent": true },
    "doc": { "memory_count": 0, "disk_count": 0, "coherent": true }
  },
  "last_check": 1766193576332,
  "config": {
    "log_level": 2,
    "check_enabled": true,
    "log_requests": true,
    "log_debug": true,
    "check_coherence": true
  }
}
```

**Campos:**
- `overall_coherent`: Estado general de coherencia (true/false)
- `entities`: Estadísticas por entidad (any, key, set, kit, doc)
- `memory_count`: Número de registros en memoria (H2) - desde contador incremental
- `disk_count`: Número de registros en disco (KVStore) - desde contador incremental
- `coherent`: Estado de coherencia por entidad
- `last_check`: Timestamp de la última verificación
- `config`: Configuración actual del sistema

#### POST /api/store/coherence/verify

Ejecuta una verificación completa de coherencia de forma manual (bajo lock.read, métrica de latencia incluida).

**Respuesta:**
```json
{
  "coherent": true,
  "message": "Data coherence verified successfully",
  "timestamp": 1766193576370
}
```

#### POST /api/store/coherence/sync

Fuerza una sincronización completa desde el almacenamiento persistente hacia la memoria. **Resincroniza contadores incrementales** tras reload completo. Útil para recuperación de datos.

**Respuesta:**
```json
{
  "success": true,
  "message": "Force sync completed successfully",
  "timestamp": 1766193576400
}
```

**Operaciones internas:**
1. `rdb.readPoint()` - Recarga H2 desde KVStore
2. `resyncCounters()` - Full-scan único para hidratar contadores
3. `verifyCoherence()` - Validación final

#### GET /api/store/health

Proporciona un estado de salud completo del sistema.

#### GET /api/trace/stats

Obtiene estadísticas específicas del sistema de logging y trace.

### Ejemplos de Uso

#### Verificar Estado del Sistema
```bash
curl -s http://localhost:9990/api/store/health | jq .
```

#### Obtener Estadísticas de Coherencia
```bash
curl -s http://localhost:9990/api/store/coherence | jq .
```

#### Verificar Coherencia Manualmente
```bash
curl -s -X POST http://localhost:9990/api/store/coherence/verify | jq .
```

#### Forzar Sincronización (Recovery)
```bash
curl -s -X POST http://localhost:9990/api/store/coherence/sync | jq .
```

#### Monitorear Logging
```bash
curl -s http://localhost:9990/api/trace/stats | jq .
```

### Script de Pruebas Automatizadas

OnMind-XDB incluye un script completo para probar todas las funcionalidades de coherencia:

```bash
./scripts/test-coherence.sh
```

Este script:
1. Verifica el estado de salud del sistema
2. Obtiene estadísticas de coherencia
3. Ejecuta verificación manual
4. Prueba la creación de datos
5. Verifica que la coherencia se mantiene
6. Proporciona ejemplos de configuración

### Configuración de Rendimiento

El sistema de coherencia está optimizado para **cero overhead** cuando está desactivado:

```ini
# Máximo rendimiento (producción)
app.logger = 0    # Sin logging
db.check = -      # Sin verificaciones automáticas

# Monitoreo básico
app.logger = 1    # Solo peticiones HTTP
db.check = -      # Sin verificaciones automáticas

# Depuración completa (desarrollo)
app.logger = 2    # Logging completo
db.check = +      # Verificaciones automáticas activas
```

**Nota:** Los endpoints de monitoreo (`/api/store/coherence`, `/api/store/coherence/verify`, `/api/store/coherence/sync`, `/api/store/health`, `/api/trace/stats`) están **siempre disponibles** independientemente de la configuración, permitiendo verificaciones bajo demanda sin impacto en el rendimiento.

### Detalles de Implementación

#### Contadores Incrementales (O(1))

```kotlin
// En CoherenceStore.kt - Contadores atómicos thread-safe
private val memoryCounts = ConcurrentHashMap<String, AtomicInteger>()
private val diskCounts = ConcurrentHashMap<String, AtomicInteger>()

// Operaciones O(1)
fun incrementMemoryCount(entityType: String)  { memoryCounts[entityType]?.incrementAndGet() }
fun decrementMemoryCount(entityType: String)  { memoryCounts[entityType]?.decrementAndGet() }
fun incrementDiskCount(entityType: String)    { diskCounts[entityType]?.incrementAndGet() }
fun decrementDiskCount(entityType: String)    { diskCounts[entityType]?.decrementAndGet() }

fun getMemoryCount(entityType: String): Int = memoryCounts[entityType]?.get() ?: 0
fun getDiskCount(entityType: String): Int = diskCounts[entityType]?.get() ?: 0
```

#### Verificación Atómica con Shared Lock

```kotlin
@Suppress("NOTHING_TO_INLINE")
internal inline fun verifyCoherenceQuick(entityType: String) {
    if (!CoherenceConfig.shouldCheckCoherence()) return  // Zero overhead
    
    lock.read {  // Shared lock - NO bloquea otros readers
        val memoryCount = getMemoryCount(entityType)
        val diskCount = getDiskCount(entityType)  // O(1)
        
        if (memoryCount != diskCount) {
            Trace.logCoherenceCheck(entityType, memoryCount, diskCount, false)
        }
    }
}
```

#### Integración en Operaciones CRUD

El sistema de contadores incrementales se actualiza automáticamente en cada operación:

| Operación | H2 (memoria) | KVStore (disco) | Contadores |
|-----------|--------------|-----------------|------------|
| INSERT | `forUpdate()` | `savePoint*()` + `commit()` | `incrementMemoryCount` + `incrementDiskCount` |
| UPDATE | `forUpdate()` | `savePoint*()` + `commit()` | No cambia conteo |
| DELETE | `forUpdate(DELETE)` | `movePoint()` + `commit()` | `decrementMemoryCount` + `decrementDiskCount` |
| CREATE kit | `forUpdate()` | `savePointKit()` + `commit()` | `incrementMemoryCount("kit")` + `incrementDiskCount("kit")` |
| DROP kit | `forUpdate(DELETE)` | `movePoint(id, "kit")` + `commit()` | `decrementMemoryCount("kit")` + `decrementDiskCount("kit")` |

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
2. **OTP Mail** - Passwordless con código de un solo uso por email
3. **Authelia** - Autenticación corporativa con headers
4. **AWS Cognito** - Autenticación cloud con JWT
5. **OIDC / Keycloak / Entra ID** - Autenticación estándar con JWT (OIDC)

### Configuración en onmind.ini

#### Autenticación Básica (Default)
```ini
# Habilitada por defecto (credenciales en Base64)
auth.enabled = true
auth.type = BASIC
auth.basic.user = YWRtaW4=  # admin en Base64
auth.basic.pass = YWRtaW4=  # admin en Base64
```

#### OTP Mail (passwordless, recomendado sin IdP externo)
```ini
auth.enabled = true
auth.type = OTPMAIL
auth.otp.smtp_host = localhost
auth.otp.smtp_port = 1025
auth.otp.smtp_user =
auth.otp.smtp_pass =
auth.otp.from = xdb@localhost
auth.otp.session_key = change-me-otp-session-key
auth.otp.auto_register = true
```

Flujo:
1. Usuario visita `/app/` sin sesión → redirect a `/auth/otpmail/login`
2. Ingresa email → XDB genera código de 6 dígitos (TTL 5 min, máx. 3 intentos)
3. Código enviado por SMTP; en `localhost`/`127.0.0.1` se muestra también en pantalla (dev mode)
4. Verificación OK → cookie firmada `xdb-otp-session` (7 días) e inyección de `X-Auth-User`
5. Auto-registro en `xykey` (`key02` = email) si `auth.otp.auto_register = true`

Mailpit (desarrollo):
```bash
docker run -p 1025:1025 -p 8025:8025 axllent/mailpit
```

Gmail (relay SMTP):
```ini
auth.otp.smtp_host = smtp.gmail.com
auth.otp.smtp_port = 587
auth.otp.smtp_user = tu-cuenta@gmail.com
auth.otp.smtp_pass = contraseña-de-aplicacion
auth.otp.from = tu-cuenta@gmail.com
```

Rutas públicas OTP:
- `GET  /auth/otpmail/login`
- `POST /auth/otpmail/send`
- `POST /auth/otpmail/verify`
- `GET|POST /auth/otpmail/logout`

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
auth.cognito.client_id = xxxxxxxxxxxxxxxxxxxxxxxxxx
```

#### Con OIDC / Keycloak / Entra ID
```ini
auth.enabled = true
auth.type = KEYCLOAK  # o ENTRAID, o OIDC
auth.oidc.url = https://auth.example.com/realms/master
auth.oidc.realm = master
auth.oidc.client_id = onmind-xdb
# auth.oidc.user_claim = sub      # opcional
# auth.oidc.roles_claim = roles   # opcional
```

### Uso

#### Acceder a la UI
- Con **BASIC**: al acceder a `http://localhost:9990/app/`, el navegador pedirá usuario y contraseña.
- Con **OTPMAIL**: se redirige a `/auth/otpmail/login` para login passwordless por email.

#### Cambiar Credenciales (BASIC)
Editar `~/onmind/onmind.ini`:
```ini
auth.basic.user = miusuario
auth.basic.pass = mipassword
```

### Arquitectura de Autenticación

```
Request → AuthProvider.filter()
    ↓
Valida credenciales / sesión / JWT según auth.type
    ↓
Si válido: Agrega X-Auth-User header → Routes
Si inválido: 401 (API) o redirect a login (UI / OTP)
```

**Proveedores disponibles (Strategy pattern):**

- `NoAuthPlug`: Sin autenticación (cuando auth.enabled=false)
- `BasicAuthPlug`: Valida usuario/contraseña con HTTP Basic Auth
- `OTPMailPlug`: Passwordless OTP por email + cookie de sesión firmada
- `AutheliaPlug`: Lee headers Remote-User, Remote-Email, Remote-Groups
- `CognitoPlug`: Valida JWT token de AWS Cognito
- `OIDCPlug`: Valida JWT de proveedores OIDC (Keycloak, Entra ID, etc.)

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

## Telemetría y Logging

### Request Logging

OnMind-XDB puede registrar todas las peticiones HTTP en consola o en archivo.

#### Configuración

#### Configuración de Logging y Coherencia

### app.logger (Numérico)

Controla el nivel de logging del sistema:

- `0` o vacío: **Desactivado** (cero overhead, máximo rendimiento)
- `1`: **Peticiones HTTP** (logging básico de requests)
- `2`: **Depuración completa** (incluye coherencia y trace detallado)

### db.check (Símbolo)

Controla la verificación automática de coherencia:

- `-` o vacío: **Desactivado** (cero overhead)
- `+`: **Activado** (verificación en operaciones de escritura)

### Estadísticas de Coherencia

Las estadísticas están **siempre disponibles bajo demanda** via API:
- `GET /api/store/coherence` - Estadísticas actuales
- `POST /api/store/coherence/verify` - Verificación manual
- `POST /api/store/coherence/sync` - Sincronización forzada
- `GET /api/store/health` - Estado del sistema
- `GET /api/trace/stats` - Estadísticas de logging

**No requieren configuración** y no impactan el rendimiento de operaciones normales.

#### Ejemplo de Configuración

```ini
# Máximo rendimiento (producción)
app.logger = 0
db.check = -

# Monitoreo básico
app.logger = 1
db.check = -

# Depuración completa (desarrollo)
app.logger = 2
db.check = +
```

#### Ubicación del Log

Cuando `app.logger >= 1`, el archivo se crea en:
```
~/onmind/onmind-xdb.log
```

#### Formato del Log

```
2025-01-15 10:23:45.123 [REQUEST] [POST] /abc -> 200
2025-01-15 10:23:46.456 [DEBUG] Coherence OK for any: 15 items
2025-01-15 10:23:47.789 [WARN] Coherence mismatch for key: memory=5, disk=4
```

**Campos:**
- Timestamp: `yyyy-MM-dd HH:mm:ss.SSS`
- Nivel: `REQUEST`, `DEBUG`, `INFO`, `WARN`, `ERROR`
- Método HTTP: `GET`, `POST` (para REQUEST)
- URI: Ruta solicitada (para REQUEST)
- Status Code: Código de respuesta HTTP (para REQUEST)

#### Características Técnicas

**Implementación Optimizada con Inline:**
- **Cero overhead** cuando está desactivado (`app.logger=0`, `db.check=-`)
- Buffer en memoria: 100 mensajes o 5 segundos
- Escritura por lotes (batch): Reduce I/O 100x
- Thread-safe: Sincronización en buffer
- Shutdown hook: Flush automático al cerrar
- Fallback: Si falla escritura, imprime en consola

**Performance:**
- Overhead con `app.logger=0`: **0ms** (cero impacto)
- Overhead con `app.logger=1`: ~0.1ms por request
- Overhead con `app.logger=2, db.check=+`: ~0.15ms por request
- Throughput impact: <2% en el peor caso
- Memory: ~10KB buffer típico

#### Ventajas por Modo

| Modo | Configuración | Overhead | Uso |
|------|---------------|----------|-----|
| **Producción** | `app.logger=0, db.check=-` | 0ms | Máximo rendimiento |
| **Monitoreo** | `app.logger=1, db.check=-` | ~0.1ms | Logging básico |
| **Desarrollo** | `app.logger=2, db.check=+` | ~0.15ms | Depuración completa |
| **Consola** (`0` o vacío) | Cero overhead, máximo rendimiento | Producción |
| **Peticiones** (`1`) | Logging básico, mínimo overhead | Monitoreo |
| **Completo** (`2`) | Logging detallado con coherencia | Desarrollo, debugging |

#### Rotación de Logs

El archivo crece indefinidamente. Para rotación manual:

```bash
# Backup y limpiar
mv ~/onmind/onmind-xdb.log ~/onmind/onmind-xdb.log.bak
touch ~/onmind/onmind-xdb.log
```

#### APIs de Coherencia

OnMind-XDB incluye un sistema completo de verificación de coherencia entre memoria (H2) y persistencia (KVStore):

```bash
# Obtener estadísticas de coherencia
curl http://localhost:9990/api/store/coherence

# Verificar coherencia manualmente
curl -X POST http://localhost:9990/api/store/coherence/verify

# Forzar sincronización desde disco
curl -X POST http://localhost:9990/api/store/coherence/sync

# Estado de salud del sistema
curl http://localhost:9990/api/store/health

# Estadísticas de trace
curl http://localhost:9990/api/trace/stats
```

#### Script de Pruebas

Para probar las funcionalidades de coherencia:

```bash
# Ejecutar script de pruebas
./test-coherence.sh
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

### Testing

#### Para Probar la UI
1. Compilar: `./gradlew build`
2. Ejecutar: `./gradlew run`
3. Abrir: `http://localhost:9990/app/`

---

## MCP (Model Context Protocol) – Acceso de Agentes a XDB

OnMind-XDB incluye un servidor MCP **embebido** (sin dependencias pesadas) que expone el protocolo JSON-RPC sobre el contrato ABC existente.

### Cómo activarlo

En `onmind.ini`:

```ini
mcp.enabled = +     # expone /mcp y /mcp/chat
mcp.write = -       # ¡importante! + habilita herramientas de escritura (por defecto deshabilitado)
# mcp.stdio = +     # o arrancar con: java -jar ... --mcp  (solo stdio, sin servidor HTTP)
```

### Puntos de entrada

| Ruta | Método | Uso |
|------|--------|-----|
| `/mcp` | GET / POST | Servidor MCP JSON-RPC puro (`initialize`, `tools/list`, `tools/call`) |
| `/mcp/chat` | GET / POST | Sketch de chat para el Dashboard (comandos en lenguaje natural corto) |

Stdio (`--mcp`): transporte recomendado para Claude Desktop, Cursor, etc.

### Tools disponibles (prefijo `abc_`)

**Lectura (siempre):**
- `abc_status` — Estado del servicio + modo MCP actual.
- `abc_list` — Listado de sheets/arquetipos (xykit).
- `abc_describe` — Metadatos y spec (kit05) de un sheet.
- `abc_find` — Consultas con los mismos campos que el cuerpo ABC (`some`, `with`, `show`, `size`, etc.).

**Escritura de esquemas (solo con `mcp.write=+`):**
- `abc_create` — Crear un sheet/arquetipo (lo que hace `what=create`).
- `abc_define` — Definir/actualizar campos (lo que hace `what=define`, `puts` contiene la spec).
- `abc_schema` — Ruta recomendada (combinada): crea el sheet si falta + aplica el spec en un solo paso (idempotente).

**Nota importante**: Las operaciones **row-level** (`insert`, `update`, `delete`, `drop`) NO están expuestas por MCP en esta versión. Solo se permite definir la estructura.

### Permisos explícitos

- Por defecto `mcp.write = -` (modo solo lectura). Esto es intencional.
- `mcp.write = +` es una autorización explícita en el archivo de configuración local.
- Las tools de escritura fallan con mensaje claro si están deshabilitadas.
- Las requests por `/mcp` siguen pasando por el mismo filtro de autenticación que el resto de la aplicación.

### Ejemplos de uso desde el Dashboard

Una vez activado `mcp.enabled = +`, aparece un panel **"MCP Chat"** al final del Dashboard.

Comandos de ejemplo:

```
status
list sheets
describe PRODUCTS
find PRODUCTS where any03 = 'demo' size 10
create sheet demo title "Demo"
schema demo2 title "Demo 2" spec any02=code,any03=name
/tool abc_define {"some":"demo","spec":"any02=code,any03=name"}
```

`/mcp/chat` interpreta los mensajes con reglas locales (sin LLM externo) y llama a las tools `abc_*`.

### JSON-RPC directo

```bash
# Inicialización rápida
curl -u admin:admin -X POST http://127.0.0.1:9990/mcp \
  -H 'Content-Type: application/json' \
  -d '{"jsonrpc":"2.0","id":1,"method":"initialize"}'

# Llamar tool directamente
curl -u admin:admin -X POST http://127.0.0.1:9990/mcp \
  -H 'Content-Type: application/json' \
  -d '{"jsonrpc":"2.0","id":2,"method":"tools/call","params":{"name":"abc_find","arguments":{"some":"PRODUCTS.SHEET","size":"5"}}}'
```

---

## MCP (Model Context Protocol) – Acceso de Agentes a XDB

OnMind-XDB incluye un servidor MCP **embebido** (sin dependencias pesadas) que expone el protocolo JSON-RPC sobre el contrato ABC existente.

### Cómo activarlo

En `onmind.ini`:

```ini
mcp.enabled = +     # expone /mcp y /mcp/chat
mcp.write = -       # ¡importante! + habilita herramientas de escritura (por defecto deshabilitado)
# mcp.stdio = +     # o arrancar con: java -jar ... --mcp  (solo stdio, sin servidor HTTP)
```

### Puntos de entrada

| Ruta | Método | Uso |
|------|--------|-----|
| `/mcp` | GET / POST | Servidor MCP JSON-RPC puro (`initialize`, `tools/list`, `tools/call`) |
| `/mcp/chat` | GET / POST | Sketch de chat para el Dashboard (comandos en lenguaje natural corto) |

Stdio (`--mcp`): transporte recomendado para Claude Desktop, Cursor, etc.

### Tools disponibles (prefijo `abc_`)

**Lectura (siempre):**
- `abc_status` — Estado del servicio + modo MCP actual.
- `abc_list` — Listado de sheets/arquetipos (xykit).
- `abc_describe` — Metadatos y spec (kit05) de un sheet.
- `abc_find` — Consultas con los mismos campos que el cuerpo ABC (`some`, `with`, `show`, `size`, etc.).

**Escritura de esquemas (solo con `mcp.write=+`):**
- `abc_create` — Crear un sheet/arquetipo (lo que hace `what=create`).
- `abc_define` — Definir/actualizar campos (lo que hace `what=define`, `puts` contiene la spec).
- `abc_schema` — Ruta recomendada (combinada): crea el sheet si falta + aplica el spec en un solo paso (idempotente).

**Nota importante**: Las operaciones **row-level** (`insert`, `update`, `delete`, `drop`) NO están expuestas por MCP en esta versión. Solo se permite definir la estructura.

### Permisos explícitos

- Por defecto `mcp.write = -` (modo solo lectura). Esto es intencional.
- `mcp.write = +` es una autorización explícita en el archivo de configuración local.
- Las tools de escritura fallan con un mensaje claro si están deshabilitadas.
- Las requests por `/mcp` siguen pasando por el mismo filtro de autenticación que el resto de la aplicación.

### Ejemplos de uso desde el Dashboard

Una vez activado `mcp.enabled = +`, aparece un panel **"MCP Chat"** al final del Dashboard.

Comandos de ejemplo:

```
status
list sheets
describe PRODUCTS
find PRODUCTS where any03 = 'demo' size 10
create sheet demo title "Demo"
schema demo title "Demo" spec any02=code,any03=name
/tool abc_define {"some":"demo","spec":"any02=code,any03=name"}
```

`/mcp/chat` interpreta los mensajes con reglas locales (sin LLM externo) y llama a las tools `abc_*`.

### JSON-RPC directo

```bash
# Inicialización rápida
curl -u admin:admin -X POST http://127.0.0.1:9990/mcp \
  -H 'Content-Type: application/json' \
  -d '{"jsonrpc":"2.0","id":1,"method":"initialize"}'

# Llamar tool directamente
curl -u admin:admin -X POST http://127.0.0.1:9990/mcp \
  -H 'Content-Type: application/json' \
  -d '{"jsonrpc":"2.0","id":2,"method":"tools/call","params":{"name":"abc_find","arguments":{"some":"PRODUCTS.SHEET","size":"3"}}}'
```

### Implementación interna

- Código en `co.onmind.mcp`:
  - `AbcMcpServer` — JSON-RPC 2.0 manual sobre Jackson (cero SDK externo).
  - `AbcMcpTools` — Adaptador que convierte tools a `AbcBody` + llama a `AbcAPI`.
  - `AbcMcpChat` — Interfaz HTTP `/mcp/chat` para el Dashboard.
  - `AbcMcpLlm` — Cliente Ollama opcional (OmniCoder u otro modelo) para planner LLM.
- Reutiliza `AbcAPI`, Jackson (`JsonMapper.instance`) y el mismo filtro de autenticación.
- Impacto en el `.jar` fat: **~33 KB comprimidos** (prácticamente cero comparado con los ~94 MB totales). No hay reactor, no hay MCP Java SDK.

### Arquitectura MCP en XDB

```
┌─────────────────────────────────────────────────────────────┐
│  Dashboard Chat (UI)                                        │
│    ├─ modo "rules"  → regex → AbcMcpTools → ABC /abc        │
│    └─ modo "llm"    → AbcMcpLlm (Ollama) → AbcMcpTools      │
└─────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────┐
│  MCP Server (/mcp) — JSON-RPC 2.0                           │
│    tools/list  →  [abc_status, abc_list, abc_find, ...]     │
│    tools/call  →  ejecuta AbcMcpTools → AbcAPI              │
└─────────────────────────────────────────────────────────────┘
                              │
                              ▼
                    ┌─────────────────┐
                    │  Agentes MCP    │  ← Traen su propio LLM
                    │ (Claude, Cursor,│
                    │  Grok, etc.)    │
                    └─────────────────┘
```

**Puntos clave:**
- El **MCP server (`/mcp`) no tiene LLM** — es solo protocolo JSON-RPC.
- Los **agentes externos** (Claude Desktop, Cursor, Grok TUI) **traen su propio LLM** y hablan MCP nativo.
- El **Dashboard chat (`/mcp/chat`)** es un cliente HTTP opcional con dos planners:
  1. **Rules planner** (regex, sin LLM) — fallback, comandos básicos
  2. **LLM planner** (Ollama: OmniCoder, etc.) — lenguaje natural completo

<!--
### Planner "rules" — Tabla de patrones (fallback sin LLM)

El *rules planner* cubre comandos básicos mediante regex. Orden = prioridad (primera coincidencia gana).

| Intención | Patrones (regex resumido) | Tool invocada | Argumentos |
|-----------|---------------------------|---------------|------------|
| **status** | `^(status\|estado\|version\|ping\|que tal\|como estas\|hello\|hola)\b` | `abc_status` | `{}` |
| **list sheets** | `^(list\|listar\|sheets\|hojas\|collections\|dame\|muestra\|muestrame\|puedes)\b.*(list\|sheet\|hoj\|collection\|tabla\|coleccion)` | `abc_list` | `{scheme?}` |
| **list (palabra sola)** | `^(list\|sheets\|hojas\|collections\|listar)\b` | `abc_list` | `{}` |
| **describe X** | `^(describe\|describir\|desc\|que es\|que hay\|informacion de\|que contiene)\s+(\w+)` | `abc_describe` | `{some: X}` |
| **find X [where F] [size N]** | `^(find\|buscar\|query\|consultar\|buscar en\|dame los\|muestra los\|muestrame los)\s+(\w+)(?:\s+(where\|con\|with\|donde\|filtro)\s+(.+?))?(?:\s+size\s+(\d+))?` | `abc_find` | `{some: X, with?: F, size?: N}` |
| **create sheet X [title "T"]** | `^(create\|crear\|crea\|nueva?)\s+(sheet\|hoja\|tabla\|coleccion)?\s*(\w+)(?:\s+title\s+"(.+?)")?` | `abc_create` | `{some: X, title?: T}` |
| **define X spec S** | `^(define\|definir)\s+(\w+)\s+(?:spec\s+)?(.+)` | `abc_define` | `{some: X, spec: S}` |
| **schema X [title "T"] [spec S]** | `^(schema\|esquema)\s+(\w+)(?:\s+title\s+"(.+?)")?(?:\s+spec\s+(.+))?` | `abc_schema` | `{some: X, title?: T, spec?: S}` |
| **explain ACTION ...** | `^(explain\|explicar\|como\|how\s+to\|show\s+(?:query\|body))\s+(find\|list\|describe\|create\|define\|schema)\b(.*)$` | `abc_explain` | `{action, some?, with?, size?, title?, spec?, with?}` |
-->
> **Nota:** El comando `/tool abc_* {...}` salta el planner y llama a la tool directamente (uso power-user / agentes).

### LLM Planner (opcional)

Con `mcp.llm=ollama` en `onmind.ini`:

```ini
mcp.llm = ollama
mcp.llm.url = http://127.0.0.1:11434
mcp.llm.model = carstenuhlig/omnicoder-9b:latest
mcp.llm.timeout = 120
```

- Usa `AbcMcpLlm` (cliente Ollama nativo, sin SDK pesado).
- Soporta **tool calls nativas** de Ollama + protocolo JSON en contenido (`{"tool":"abc_find","arguments":{...}}`).
- System prompt incluye esquema de todas las tools `abc_*`.
- Multi-paso: el LLM puede encadenar tools (`abc_list` → `abc_describe` → `abc_find`).

### Ejemplos de uso desde el Dashboard

Una vez activado `mcp.enabled = +`, aparece un panel **"MCP Chat"** al final del Dashboard.

```
status
list sheets
describe PRODUCTS
find PRODUCTS where any03 = 'demo' size 10
create sheet demo title "Demo"
schema demo title "Demo" spec any02=code,any03=name
explain find PRODUCTS where any03 = 'x' size 10
explain create sheet PROYECTOS title "Proyectos" spec any02=code,any03=name
explain list sheets
/tool abc_define {"some":"demo","spec":"any02=code,any03=name"}
```

### JSON-RPC directo (para agentes / scripts)

```bash
# Inicialización
curl -u admin:admin -X POST http://127.0.0.1:9990/mcp \
  -H 'Content-Type: application/json' \
  -d '{"jsonrpc":"2.0","id":1,"method":"initialize"}'

# Llamar tool directamente
curl -u admin:admin -X POST http://127.0.0.1:9990/mcp \
  -H 'Content-Type: application/json' \
  -d '{"jsonrpc":"2.0","id":2,"method":"tools/call","params":{"name":"abc_find","arguments":{"some":"PRODUCTS.SHEET","size":"5"}}}'
```

---

### Licencia

Este proyecto está bajo la Licencia Apache 2.0 - ver el archivo [LICENSE.md](LICENSE.md) para detalles.

---

**Última actualización**: 2026  
**Versión**: 1.0.0-RC


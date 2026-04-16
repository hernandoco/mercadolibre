# Documento de Diseño y Documentación – Backend: API Development
Repositorio: `hernandoco/mercadolibre`  
Branch: `master`  
Fecha: 2026-04-15

## 1) Requerimientos – Backend: API Development
**Objetivo:** construir una API REST en Java (Spring Boot) que permita:
- Listar ítems de un catálogo de calzado deportivo con **paginación**.
- Comparar **múltiples ítems** del catálogo en un solo request a partir de IDs.
- Proveer un template de endpoints `ModelController` (requerimiento base del proyecto) funcionando en Java 21.
- Incluir documentación: diseño de API, endpoints principales, setup y decisiones arquitectónicas.

**Paquetes y clases relevantes:**
- Controllers:
  - `src/main/java/com/hackerrank/sample/controller/ItemController.java`
  - `src/main/java/com/hackerrank/sample/controller/ModelController.java`
- DTOs:
  - `src/main/java/com/hackerrank/sample/dto/PagedItemsResponse.java`
  - `src/main/java/com/hackerrank/sample/dto/CompareItemsResponse.java`
- Modelo:
  - `src/main/java/com/hackerrank/sample/model/Item.java`
- Persistencia:
  - `src/main/java/com/hackerrank/sample/repository/ItemRepository.java` (JPA)
  - `src/main/java/com/hackerrank/sample/DataInitializer.java` (precarga de datos)
- Errores:
  - `src/main/java/com/hackerrank/sample/exception/BadResourceRequestException.java` (400)
  - `src/main/java/com/hackerrank/sample/exception/NoSuchResourceFoundException.java` (404)

---

## 2) Descripción del API REST (Endpoints) – Comparación de múltiples ítems

### 2.1 `GET /items`
**Controller:** `ItemController#getItems(page, size)`  
**Ruta base:** `/items`

**Propósito:** listar ítems del catálogo con paginación.

**Query params:**
- `page` (int, default `0`)
- `size` (int, default `20`)
- Restricción real: `size` debe estar entre `1` y `50` (constante `SIZE_MAX = 50`).

**Validación y errores:**
- Si `page < 0` o `size < 1` o `size > 50` → lanza `BadResourceRequestException` → HTTP **400 Bad Request**.

**Response DTO real:** `PagedItemsResponse`
```json
{
  "items": [ { /* Item */ } ],
  "page": 0,
  "size": 20,
  "totalItems": 12,
  "totalPages": 1
}
```

---

### 2.2 `GET /items/compare?ids=ID1,ID2,ID3`
**Controller:** `ItemController#compareItems(ids)`  
**Ruta:** `/items/compare`

**Propósito:** comparar múltiples ítems usando IDs (separados por coma).

**Query params:**
- `ids` (string CSV), ejemplo:
  - `ids=SNEAK-001,SNEAK-010`
  - también se aceptan espacios, ya que se hace `trim()` por ID.

**Cómo funciona (lógica real del controller):**
1. `ids` se separa por comas: `ids.split(",")`
2. Se hace `trim()`, se filtran vacíos, se aplica `distinct()` para evitar duplicados.
3. Si la cantidad final de IDs distintos es menor que 2 → error 400.
4. Se delega a `ItemService#compareItems(idList)` para obtener:
   - lista de `items` encontrados
   - lista de `missingIds` (IDs solicitados que no existen)

**Validación y errores:**
- Si `idList.size() < 2` → `BadResourceRequestException` → HTTP **400** con el mensaje:
  - `"At least 2 distinct item ids are required for comparison"`

**Response DTO real:** `CompareItemsResponse`
```json
{
  "items": [
    {
      "id": "SNEAK-001",
      "name": "Air Walk Pro",
      "imageUrl": "https://example.com/images/sneak-001.jpg",
      "description": "Zapatilla cómoda para caminata diaria",
      "price": 89.99,
      "rating": 4.5,
      "specifications": {
        "subcategory": "caminar",
        "color": "blanco",
        "material": "malla"
      }
    }
  ],
  "missingIds": ["SNEAK-999"]
}
```

**Por qué `missingIds` es importante:**
- Permite respuesta parcial en comparación: el consumidor (frontend) puede mostrar resultados disponibles y, al mismo tiempo, indicar qué IDs faltaron sin fallar todo el request.
- Para escalabilidad, la comparación se beneficia de que los IDs son claves (en un futuro, índice en DB).
---

## 3) Campos expuestos por la API (catálogo)
El modelo real `Item` incluye estos campos, que se serializan en las respuestas JSON:

- `id` (String) – identificador único (ej. `SNEAK-001`)
- `name` (String) – **nombre del producto**
- `imageUrl` (String) – **URL de imagen**
- `description` (String) – **descripción**
- `price` (double) – **precio**
- `rating` (double) – **rating**
- `specifications` (Map<String,String>) – **especificaciones** en formato clave/valor

Ejemplo de claves usadas actualmente por el inicializador:
- `subcategory`
- `color`
- `material`

> Nota: `specifications` es intencionalmente extensible (se pueden agregar claves nuevas sin cambiar el contrato base del ítem).

---

## 4) Manejo básico de errores y comentarios inline (lógica)
### Manejo real de errores (simple y efectivo)
- **400 Bad Request**: `BadResourceRequestException` (anotada con `@ResponseStatus(HttpStatus.BAD_REQUEST)`)
  - Se usa para entradas inválidas, por ejemplo:
    - paginación fuera de rango (`page`, `size`)
    - menos de 2 IDs válidos en compare
- **404 Not Found**: `NoSuchResourceFoundException` (anotada con `@ResponseStatus(HttpStatus.NOT_FOUND)`)
  - Se utiliza cuando un recurso solicitado no existe (por ejemplo, en endpoints del template /model o futuras extensiones).

### Comentarios inline (qué se explica)
En el código, los puntos que típicamente se documentan con comentarios son:
- La validación de parámetros de paginación y el motivo del `SIZE_MAX`.
- El parsing de `ids` (trim, eliminar vacíos, distinct) y la regla “mínimo 2 IDs”.
- Por qué se retorna `missingIds` (degradación controlada / respuesta parcial). Con IDs inexistentes no rompen el request.

> En una versión “producción”, sería recomendable agregar un `@ControllerAdvice` + `@ExceptionHandler` para respuestas consistentes. 
- Respuesta estándar tipo Problem Details (RFC7807) o un DTO de error propio con:
  - `timestamp`, `status`, `error`, `message`, `path`, `details`
 Para la prueba técnica se resolvió de forma directa con `@ResponseStatus`.

> Importante: los comentarios inline se usan para explicar la lógica de paginación, parsing de IDs y armado de respuesta (items encontrados + missingIds).
---

## 5) Stack usado
Basado en el `pom.xml` y el código:

- **Java:** 21
- **Spring Boot:** 3.2.5
- **Web:** `spring-boot-starter-web` (REST controllers)
- **Persistencia:** `spring-boot-starter-data-jpa`
- **DB en memoria:** H2 (`com.h2database:h2` runtime)
- **Validación:** Jakarta Validation API (`jakarta.validation-api`) + `hibernate-validator`
- **Build:** Maven 3.x
- **Testing:** `spring-boot-starter-test` + `junit-vintage-engine` (para compatibilidad con tests estilo JUnit 4)
- **Pruebas HTTP:** `MockMvc` (en `HttpJsonDynamicUnitTest`)

---

## 6) Simulación de persistencia (real)
En este proyecto, la persistencia se simula con **H2 en memoria** + precarga inicial:

- `ItemRepository` extiende `JpaRepository<Item, String>`.
- `Item` está anotado como `@Entity`.
- `DataInitializer` implementa `ApplicationRunner` y en el arranque:
  1. Verifica si ya hay registros (`itemRepository.count() > 0`)
  2. Si está vacío, inserta una lista fija de items `SNEAK-001` a `SNEAK-012` mediante `itemRepository.saveAll(items)`

Esto provee:
- Datos disponibles desde el inicio sin archivos externos.
- Comportamiento reproducible para tests y validación local.

---

## 7) Requerimientos Non-functional (NFR)
- **Reproducibilidad:** correr localmente con `mvn test` / `mvn spring-boot:run` sin infraestructura adicional.
- **Performance razonable:** paginación para no retornar listas enormes por defecto.
- **Mantenibilidad:** separación por paquetes (`controller`, `dto`, `model`, `repository`, `exception`).
- **Extensibilidad:** `specifications` como `Map` para crecer en atributos.
- **Calidad:** suite de tests que valida respuestas HTTP.
- **Escalabilidad (plan):** posibilidad de migrar a DB y cache sin romper contrato.

---

## 8) Buenas prácticas aplicadas y recomendadas (NFR)
### Errores
- Validación temprana (paginación e IDs) para responder 400 en vez de 500.
- Excepciones con `@ResponseStatus` para mapear error→HTTP status sin sobre-ingeniería.

**Recomendación para siguiente iteración:**
- `@ControllerAdvice` + DTO de error uniforme o Problem Details (RFC7807) que se utiliza como estandar para representar errores en APIs HTTP de forma estructurada.

### Documentación
- README + este documento describen endpoints, setup y decisiones.

### Testing
- Existe un test de integración tipo “dynamic tests”:
  - `src/test/java/com/hackerrank/sample/HttpJsonDynamicUnitTest.java`
  - Se apoya en casos en `src/test/resources/testcases/` (`http00.json`, `http01.json`, etc.)
- Buen enfoque para validar contrato HTTP sin depender de ejecución manual.

### Otros
- Límites de paginación (`SIZE_MAX = 50`) para prevenir abuso accidental.
- `distinct()` en IDs para evitar comparaciones redundantes.

---

## 9) Resumen estratégico del proyecto
Se priorizó una solución:
- **Autocontenida y evaluable**: H2 + precarga con `DataInitializer`.
- **Fácil de probar**: endpoints simples + `curl` + tests.
- **Escalable a futuro**: `JpaRepository` permite migrar de H2 a una DB real sin cambiar la API.

**Nota:** No se implementó Circuit Breaker porque no hay dependencias remotas; sería relevante si el catálogo viniera de APIs externas o microservicios.

---

## 10) Diagrama del diseño de la API + endpoints + setup + decisiones
### 10.1 Diagrama (alto nivel)

```
+----------------------+
| Client (curl / UI)   |
+----------+-----------+
           |
           v
+----------------------+
| Spring MVC Controllers|
| - /items             |
| - /items/compare     |
| - /model (template)  |
+----------+-----------+
           |
           v
+----------------------+
| Service Layer         |
| - pagination           |
| - compare (missingIds) |
+----------+-----------+
           |
           v
+----------------------+
| Persistence (JPA)     |
| ItemRepository         |
|  + H2 in-memory DB     |
+----------+-----------+
           |
           v
+----------------------+
| DataInitializer        |
| loads SNEAK-001..012   |
+----------------------+
```

### 10.2 Setup y prueba rápida
Clonar el repositorio
Ejecutar tests y levantar aplicación:
```bash
mvn test
mvn spring-boot:run

curl "http://localhost:8080/items?page=0&size=20"
curl "http://localhost:8080/items/compare?ids=SNEAK-001,SNEAK-010"
```
La API corre en:
http://localhost:8080

### 10.3 Decisiones arquitectónicas clave
- **Fuente de datos simple (JSON local):** Se usa items.json para mantener la solución autocontenida y fácil de ejecutar sin dependencias externas
- **H2 + DataInitializer**: datos predecibles y sin dependencias externas.
- **Paginación en capa de servicio** Se aplica paginación por parámetros para evitar enviar listas completas y facilitar escalamiento, con límites (`size <= 50`) para robustez.
- **Compare tolerante**: `missingIds` para degradación controlada.
- **Errores simples**: `@ResponseStatus` en excepciones para 400/404 sin complejidad extra.


# Mercadolibre – Sports Footwear Catalog API (Spring Boot)

API REST en Spring Boot para:
- CRUD básico del recurso `model` (endpoints del template)
- Listado paginado de ítems de catálogo (zapatillas) y endpoint de comparación

## Requisitos
- Java 21
- Maven 3.x

## Setup (local)
1. Clonar el repositorio
2. Ejecutar tests:
   ```bash
   mvn test
   ```
3. Levantar la aplicación:
   ```bash
   mvn spring-boot:run
   ```
4. La API corre en:
   - `http://localhost:8080`

## Diseño de la API (resumen)
- Estilo REST, respuestas JSON
- Paginación por `page` y `size`
- Catálogo alimentado desde archivo local (`items.json`) incluido en el proyecto

### Endpoints principales

#### Template: ModelController
> Estos endpoints vienen del template y se ajustaron para compilar y funcionar correctamente con Java 21.

- `POST /model`  
  Crea/actualiza un modelo.

- `GET /model`  
  Lista modelos.

- `DELETE /erase`  
  Limpia datos (según template).

#### Items (catálogo)
- `GET /items?page={page}&size={size}`  
  Retorna un listado paginado de ítems.
  - **Query params**
    - `page` (int, default 0)
    - `size` (int, default 20)
  - **Response (ejemplo)**
    ```json
    {
      "items": [ { "id": "SNEAK-001", "name": "..." } ],
      "page": 0,
      "size": 20,
      "totalItems": 12,
      "totalPages": 1
    }
    ```

- `GET /items/compare?ids={id1},{id2},...`  
  Retorna los ítems encontrados y reporta IDs faltantes.
  - **Query params**
    - `ids` (string CSV o repetido según implementación; en este proyecto se usa CSV)
  - **Response (ejemplo)**
    ```json
    {
      "items": [ { "id": "SNEAK-001", "name": "..." } ],
      "missingIds": ["SNEAK-999"]
    }
    ```

## Ejemplos (curl)
```bash
curl "http://localhost:8080/items?page=0&size=20"

curl "http://localhost:8080/items/compare?ids=SNEAK-001,SNEAK-010"
```

## Decisiones arquitectónicas
- **Fuente de datos simple (JSON local):** Se usa `items.json` para mantener la solución autocontenida y fácil de ejecutar sin dependencias externas.
- **Paginación en capa de servicio:** Se aplica paginación por parámetros para evitar enviar listas completas y facilitar escalamiento.
- **Compatibilidad con Java 21:** Se restauró/ajustó configuración de compilación para asegurar CI y ejecución local con Java 21.

## Road to scalability (alto nivel)
Si esto creciera a producción:
- Persistencia en DB (PostgreSQL) + índices por `id`, `subcategory`, etc.
- Cache (Redis) para lecturas frecuentes del catálogo
- Observabilidad: logs estructurados + métricas (Micrometer/Prometheus)
- Validación y manejo de errores consistente (Problem Details / RFC7807)
- Versionado de API (`/v1/...`) y contratos (OpenAPI/Swagger)

- ### ¿Por qué no se usó un Circuit Breaker (Patron)?
En esta solución **no se implementó un patrón Circuit Breaker** (p. ej. Resilience4j/Hystrix) porque el servicio es **autocontenido** y no consume dependencias remotas (APIs externas, microservicios, colas, etc.).  
El catálogo se lee desde un **archivo local (`items.json`)** y las operaciones principales son in-process, por lo que el Circuit Breaker no aportaría valor real en términos de tolerancia a fallas (no hay latencia de red ni errores transitorios típicos de llamadas remotas).

**Cuándo sí lo agregaría:**
- Si `items` proviniera de una API externa o de otro microservicio.
- Si hubiera llamadas a servicios de pricing, stock o recomendaciones.
- Para degradación controlada (fallback), timeouts y protección ante cascadas de fallos.

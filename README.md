# ╰┈➤| Paarcial 1ARSW ┆⤿
### *Escuela Colombiana de Ingeniería – Arquitecturas de Software*
### *Autor: Daniel Palacios Moreno*

---

## ╰┈➤ |Requisitos|

- **Java 21**
- **Maven 3.9+**
- **Docker Desktop**

---

## ╰┈➤ |Ejecución del proyecto|

Para ejecutar el proyecto, asegúrate de tener **Docker Desktop instalado y en ejecución**. Luego usa:

```bash
mvn clean install
docker-compose up --build 
```
> Con el comando `docker-compose up --build` corre con el filtro predeterminado el cual es el Identity si quieres  cambiar el filtro agrega al compiezno de este comando $env:SPRING_PROFILES_ACTIVE="nombre del perfil"; por ejemplo `$env:SPRING_PROFILES_ACTIVE="redundancy"; docker compose up --build` lo que activara el filtro del perfil redundancy

> Si deseas activar filtros de puntos (reducción de redundancia, *undersampling*, etc.), implementa nuevas clases que extiendan `BlueprintsFilter` y agrega a la restriccion de perfil de IdentityFilter el nombre del filtro que vayas a usar  crea el perfil en el nuevo filtro

### Acceso en navegador:

*   **Swagger UI:** <http://localhost:8080/swagger-ui.html>
*   **OpenAPI JSON:** <http://localhost:8080/v3/api-docs>

***

## ╰┈➤ |Estructura de carpetas (arquitectura)|

    src/main/java/edu/eci/arsw/tickets
      ├── model/         # Entidades de dominio: Blueprint, Point
      ├── persistence/   # Interfaz + repositorios (InMemory, Postgres)
      ├── services/      # Lógica de negocio y orquestación
      ├── filters/       # Filtros de procesamiento (Identity, Redundancy, Undersampling)
      ├── controllers/   # REST Controllers (BlueprintsAPIController)
      └── config/        # Configuración (Swagger/OpenAPI, etc.)
      └── socket/        # socket de comunicacion

> La estructura sigue el patrón de **capas lógicas**, permitiendo extender el sistema hacia nuevas tecnologías o fuentes de datos.

***

## ╰┈➤ |Parcial|

### 3. Buenas prácticas de API REST
- Cambia el path base de los controladores a `/api/v1/blueprints`.
- Usa **códigos HTTP** correctos:
    - `200 OK` (consultas exitosas).
    - `201 Created` (creación).
    - `202 Accepted` (actualizaciones).
    - `400 Bad Request` (datos inválidos).
    - `404 Not Found` (recurso inexistente).
- Implementa una clase genérica de respuesta uniforme:
  ```java
  public record ApiResponse<T>(int code, String message, T data) {}
  ```
  Ejemplo JSON:
  ```json
  {
    "code": 200,
    "message": "execute ok",
    "data": { "author": "john", "name": "house", "points": [...] }
  }
  ```
Se realizó desde la anotación `@RequestMapping`, lo que permite que cualquier endpoint generado desde ese controller se pueda comunicar desde ese base path sin generar ningún tipo de conflicto con implementaciones futuras, y permitiendo al proyecto ser más extensible.

Para la implementación de los códigos y de la respuesta uniforme que se nos plantea, se realizó la implementación de una clase llamada `ApiResponseFormated`, que se encarga del manejo y creación de las respuestas con su código HTTP indicado (no se usó el nombre recomendado en la guía debido a que generaba conflictos con una anotación de la documentación que tiene Spring Boot). Se implementaron try-catch para realizar el manejo y control de los errores, para devolver la respuesta correcta según correspondiera, ya sea 400 o 404 para errores, y para cada uno de los endpoints se especificó qué tipo de mensaje de verificación correcta se debía enviar, ya fuera cualquiera de los siguientes: 200, 201, 202.

***

### 4. OpenAPI / Swagger

*   Configuración de `springdoc-openapi`.
*   Documentación accesible en `/swagger-ui.html`.
*   Anotación de endpoints con `@Operation` y `@ApiResponse`.

Para la documentación, se usaron las anotaciones `@Operation` y `@ApiResponse` que nos brinda Spring Boot para cada endpoint, especificando el path, el método, el código de respuesta y la respuesta esperada. De esta manera, al abrir Swagger o api-docs al momento de querer usar alguno de los endpoints que la API tiene, se cuenta con la documentación adecuada a la mano y el manejo de códigos, lo que permite una mayor facilidad para el uso de la API.


---
## ╰┈➤ |Pruebas de funcionamiento|


### Swagger-ui

[Video evidencia Swagger blueprint.mp4](Images/ReadMe/Video%20evidencia%20Swagger%20blueprint.mp4)

### Docker

![img_10.png](Images/ReadMe/img_10.png)

---

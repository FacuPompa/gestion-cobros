# Pellet API

Backend de gestión para pequeñas distribuidoras y forrajerías, en evolución
hacia productos, existencias y ventas. Actualmente permite registrar clientes,
deudas y cobros en pesos argentinos, consultar saldos y conservar su historial.
El catálogo, el inventario y las ventas todavía no están implementados.

La aplicación registra pagos que ya ocurrieron; no mueve dinero ni integra
pasarelas de pago o facturación fiscal.

## Funcionalidades actuales

- Registro de clientes y consulta por identificador.
- Registro de deudas asociadas a un cliente y consulta de sus deudas.
- Vencimiento opcional: si se omite, se asigna un mes calendario desde la fecha
  de registro en la zona America/Argentina/Buenos_Aires.
- Cobros parciales o totales con importe, medio de pago y momento de registro.
- Medios de pago: efectivo (CASH), transferencia (BANK_TRANSFER) y tarjeta (CARD).
- Rechazo de cobros superiores al saldo pendiente, incluyendo deudas saldadas.
- Consulta del importe original, total cobrado y saldo pendiente de una deuda.
- Consulta de totales de todas las deudas de un cliente; sin deudas devuelve ceros.
- Autoría de cobros obtenida del usuario autenticado: registeredBy al registrar
  y cancelledBy al anular.
- Anulación de cobros con motivo y fecha, conservando el importe y el registro original.
- Rechazo de una segunda anulación del mismo cobro.
- Historial por deuda con cobros vigentes y anulados, ordenado por fecha de
  registro e identificador; incluye autores, fecha y motivo de anulación.
- Usuarios persistidos con hashes BCrypt y roles OPERATOR y ADMIN.
- Login con JWT de 15 minutos y autenticación Bearer en el perfil dev;
  anulación reservada a ADMIN.
- Validación de importes positivos con hasta dos decimales.

El saldo se calcula desde la deuda y sus cobros vigentes; no se guarda como un saldo
mutable independiente.

## Tecnologías

Java 27 (entorno actual con JDK 27 EA), Spring Boot 4.1.2, Spring MVC,
Spring Data JPA, Bean Validation, Spring Security y Lombok. PostgreSQL 18 con
Docker Compose, migraciones Flyway y Gradle Wrapper 9.8.2.

El código se organiza en DTOs y servicios en domain, entidades y repositorios
JPA en persistence, y controladores, configuración HTTP y errores en web.

## Ejecución local

Requisitos: JDK 27 compatible con el proyecto y Docker con Compose.
Los comandos se ejecutan desde la raíz del repositorio.

1. Definir POSTGRES_PASSWORD para Docker Compose. Puede utilizarse un archivo
   local .env, excluido de Git.
2. Ejecutar `docker compose up -d postgres`.
3. Configurar la ejecución de la aplicación con SPRING_PROFILES_ACTIVE=dev y
   POSTGRES_PASSWORD con la contraseña de esa base. Spring no lee el archivo
   .env de Compose automáticamente.
4. Si se reutiliza una base existente con nombres personalizados, pasar también
   POSTGRES_DB y POSTGRES_USER al entorno de Spring (Compose lee .env; Spring no).
5. Ejecutar PelletApiApplication desde el IDE o `./gradlew bootRun` desde
   una terminal que tenga esas variables disponibles.

PostgreSQL se publica en 127.0.0.1:5433, con base pellet y usuario
pellet_app por defecto. POSTGRES_DB, POSTGRES_USER y POSTGRES_VOLUME permiten
conectar una instalación existente conservando sus datos. Sus datos persisten
en un volumen de Docker. Cambiar la variable
de contraseña después de inicializar el volumen no cambia la contraseña de la
base existente.

La API local escucha en http://127.0.0.1:8080. Flyway aplica las migraciones y
Hibernate valida el esquema mediante ddl-auto=validate.

## Endpoints

| Método | Ruta | Operación |
| --- | --- | --- |
| POST | /api/auth/login | Autenticar y obtener JWT en header Authorization |
| POST | /api/customers | Registrar cliente |
| GET | /api/customers/{customerId} | Consultar cliente |
| GET | /api/customers/{customerId}/balance | Consultar saldo total del cliente |
| POST | /api/debts | Registrar deuda |
| GET | /api/debts/customer/{customerId} | Consultar deudas paginadas de un cliente |
| GET | /api/debts/{debtId}/balance | Consultar saldo de deuda |
| POST | /api/payments | Registrar cobro |
| POST | /api/payments/{paymentId}/cancel | Anular cobro con motivo |
| GET | /api/payments/debt/{debtId}/history | Consultar historial de cobros de una deuda |

Las altas responden 201 y las consultas exitosas 200. Las entradas inválidas
responden 400, los clientes, deudas o cobros inexistentes 404, y los cobros
superiores al saldo o una segunda anulación del mismo cobro 409. Un cliente existente sin deudas devuelve una página con content vacío.

### Paginación de deudas

`GET /api/debts/customer/{customerId}?page=0&size=20` devuelve un objeto con
`content`, `page`, `size`, `totalElements`, `totalPages` y `last`. Esta respuesta
reemplaza la lista plana anterior. Las páginas empiezan en cero; el tamaño
predeterminado es 20 y admite valores de 1 a 100. El orden es vencimiento
ascendente y después identificador ascendente.

Una página fuera del rango devuelve 200 con contenido vacío y conserva los
totales. Parámetros numéricos fuera de los límites devuelven 400 con código
`invalid-pagination`; parámetros no numéricos también reciben 400. Un cliente
inexistente sigue recibiendo 404.

## Acceso local

Configurar JWT_SECRET con una clave aleatoria local (por ejemplo, generada con
`openssl rand -base64 32`) en el entorno que ejecuta la aplicación y los tests,
además de las variables de PostgreSQL y SPRING_PROFILES_ACTIVE=dev. La propiedad
security.jwt.secret referencia esa variable; no guardar la clave en Git.

Enviar POST /api/auth/login sin autenticación, con username y password en el
body JSON. Con credenciales válidas responde 200, sin body, y un header
Authorization con el valor Bearer seguido del JWT. En Postman, usar después
Authorization → Bearer Token en las rutas protegidas y pegar solo el token.

Los tokens duran 15 minutos y se verifican firma HMAC256, emisor pellet-api,
expiración y presencia de sujeto. Cada petición carga los roles actuales desde
PostgreSQL. El flujo es stateless; Basic Auth está deshabilitado.

Las cuentas deben existir en app_users. V5 solo crea la tabla, sin insertar
credenciales. Las contraseñas se guardan como hashes BCrypt; no existe registro
público ni carga automática de usuarios.

OPERATOR puede consultar y registrar datos; ADMIN puede además anular cobros.
Credenciales incorrectas en login o peticiones protegidas sin JWT válido reciben
401; un operador que intenta anular recibe 403. Fuera del entorno local se requiere
HTTPS. La renovación de tokens no está implementada.

Los autores se conservan como usernames históricos. Los cobros anteriores a V6
pueden tener autor desconocido (null); no se les atribuye un usuario retroactivamente.

## Verificación y CI

Con PostgreSQL local iniciado y las variables del perfil dev disponibles,
ejecutar `./gradlew test --rerun-tasks`.

La batería actual contiene veintidós pruebas:

- Una prueba de carga del contexto de Spring.
- Tres pruebas unitarias de PaymentEntity: cobro vigente, anulación que conserva
  los datos originales y rechazo de una segunda anulación sin sobrescribirla.
- Siete pruebas de integración con PostgreSQL: persistencia de la anulación,
  exclusión de anulados de saldos de deuda y cliente, registro de un nuevo cobro
  usando el saldo recuperado, historial con desempate por ID, cobro inexistente,
  conservación del autor ante una segunda anulación y autor desconocido en datos
  históricos. Verifican también la persistencia y respuesta de los autores.
- Cuatro pruebas unitarias de CurrentUserService: obtiene el username autenticado
  y rechaza contexto vacío, credenciales sin autenticar y autenticación anónima.

- Siete pruebas de paginación con PostgreSQL: filtro por cliente, orden y
  desempate, metadatos, página fuera del rango, cliente vacío o inexistente y
  límites de parámetros. Incluyen contrato MVC de parámetros predeterminados,
  respuesta paginada y errores 400/404 mediante MockMvc sin cadena de seguridad.

Las pruebas de integración crean sus propios datos y revierten sus transacciones
al terminar. No comprueban concurrencia ni representan cobertura completa de la API.
Los contratos HTTP y las validaciones de entrada también se comprobaron manualmente
con Postman y DataGrip.

Para ejecutar únicamente las pruebas unitarias de la entidad, sin Spring ni base:
`./gradlew test --tests '*PaymentEntityTest' --rerun-tasks`.

GitHub Actions compila y ejecuta las pruebas en PRs hacia main y en pushes a
main, utilizando PostgreSQL efímero independiente de la base local. El job se
llama Compilar. CI utiliza una clave JWT sintética exclusiva de pruebas,
independiente de la clave local. Todavía no hay despliegue automático (CD).

## Próximos pasos y límites actuales

- Implementar catálogo de productos y movimientos básicos de existencias.
- Incorporar ventas y su integración con la cuenta corriente de mayoristas.
- Ampliar las pruebas automatizadas de las reglas de negocio.
- Trabajar idempotencia y conflictos entre operaciones simultáneas.

El control de acceso actual está configurado para el perfil dev.
La implementación actual no incorpora protección frente a
cobros concurrentes o reenvíos duplicados. Los cobros anulados se excluyen del
cálculo de saldos y del saldo disponible al registrar nuevos cobros. No se contemplan saldos a favor ni reparto entre deudas.

Este README se actualizará con cada funcionalidad completada y verificada,
distinguiendo lo implementado de lo pendiente.

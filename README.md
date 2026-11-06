# Cobranzas API

API REST para registrar clientes, deudas y cobros en pesos argentinos, y consultar
el saldo pendiente de una deuda o el total de un cliente. Proyecto backend en desarrollo.

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
- Validación de importes positivos con hasta dos decimales.

El saldo se calcula desde la deuda y sus cobros; no se guarda como un saldo
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
4. Ejecutar CobranzasApiApplication desde el IDE o `./gradlew bootRun` desde
   una terminal que tenga esas variables disponibles.

PostgreSQL se publica en 127.0.0.1:5433, con base cobranzas y usuario
cobranzas_app. Sus datos persisten en un volumen de Docker. Cambiar la variable
de contraseña después de inicializar el volumen no cambia la contraseña de la
base existente.

La API local escucha en http://127.0.0.1:8080. Flyway aplica las migraciones y
Hibernate valida el esquema mediante ddl-auto=validate.

## Endpoints

| Método | Ruta | Operación |
| --- | --- | --- |
| POST | /api/customers | Registrar cliente |
| GET | /api/customers/{customerId} | Consultar cliente |
| GET | /api/customers/{customerId}/balance | Consultar saldo total del cliente |
| POST | /api/debts | Registrar deuda |
| GET | /api/debts/customer/{customerId} | Consultar deudas de un cliente |
| GET | /api/debts/{debtId}/balance | Consultar saldo de deuda |
| POST | /api/payments | Registrar cobro |

Las altas responden 201 y las consultas exitosas 200. Las entradas inválidas
responden 400, los clientes o deudas inexistentes 404, y los cobros superiores
al saldo 409. Un cliente existente sin deudas devuelve una lista vacía.

## Verificación y CI

Con PostgreSQL local iniciado y las variables del perfil dev disponibles,
ejecutar `./gradlew test --rerun-tasks`.

La prueba automatizada actual comprueba la carga del contexto de Spring.
Los recorridos de registro, consulta, saldo y rechazos se comprobaron manualmente
con Postman y DataGrip; todavía no hay tests automatizados específicos de negocio.

GitHub Actions compila y ejecuta las pruebas en PRs hacia main y en pushes a
main, utilizando PostgreSQL efímero independiente de la base local. El job se
llama Compilar. Todavía no hay despliegue automático (CD).

## Próximos pasos y límites actuales

- Ampliar las pruebas automatizadas de las reglas de negocio.
- Incorporar anulaciones e historial conservando los cobros originales.
- Incorporar usuarios, permisos y autoría de las operaciones.
- Trabajar idempotencia y conflictos entre operaciones simultáneas.

El perfil dev permite acceder a las rutas implementadas sin autenticación para
pruebas locales. La implementación actual no incorpora anulaciones ni protección
frente a cobros concurrentes o reenvíos duplicados. Por ahora se suman todos los
cobros registrados. No se contemplan saldos a favor ni reparto entre deudas.

Este README se actualizará con cada funcionalidad completada y verificada,
distinguiendo lo implementado de lo pendiente.

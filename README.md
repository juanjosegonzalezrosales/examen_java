# CREDIYA S.A.S. — SISTEMA INTEGRAL DE GESTIÓN DE CRÉDITOS Y CARTERA

> **Proyecto Académico Profesional de Software en Java 17, Maven, MySQL 8.0 y JDBC puro.**  
> Implementa Arquitectura por Capas **MVC (Modelo - Vista - Controlador)**, Patrón **DAO (Data Access Object)**, Principios **SOLID**, Patrones de Diseño Gang of Four (**Singleton, Strategy, Factory Method, Chain of Responsibility**), Criptografía Segura de Credenciales (**PBKDF2WithHmacSHA256**) y Persistencia Física en Archivos (`.txt` y `.dat`).

---

## 1. DESCRIPCIÓN Y FUNCIONAMIENTO DEL PROGRAMA

**CrediYA S.A.S.** es una aplicación empresarial de consola orientada a la originación, administración, amortización, recaudo y cobranza de créditos de libre inversión y comerciales. El sistema opera con segregación de accesos basada en la identidad y el rol de quien inicia sesión:

1. **Portal del Cliente:**  
   Permite a los usuarios deudores autenticarse de manera segura con su número de documento y contraseña. Ofrece acceso exclusivo de solo lectura a sus datos personales, lista de créditos vigentes, calendario de cuotas (discriminando cuotas pagadas, al día y en mora) y visualización de la tabla de amortización detallada de sus contratos. El cliente no puede ver información de terceros ni realizar alteraciones financieras directamente.

2. **Portal del Funcionario:**  
   Brinda a los empleados de la entidad herramientas administrativas y financieras organizadas por módulos según sus privilegios:
   - **Asesor Comercial:** Radica clientes y solicitudes de crédito, consulta solicitudes, registra pagos de cuotas y abonos extraordinarios.
   - **Supervisor de Cartera:** Evalúa solicitudes de crédito hasta por un valor de **\$200.000.000**, autoriza o revoca decisiones mediante la Cadena de Responsabilidad, desembolsa préstamos aprobados (generando los planes de cuotas en transacciones atómicas) y consulta métricas generales.
   - **Gerente General:** Ejerce la máxima autoridad en la Cadena de Responsabilidad (aprobación sin límite de monto), gestiona el personal (creación de funcionarios y asignación de credenciales), supervisa la cartera completa, accede a reportes analíticos con Stream API y genera backups binarios serializados (`.dat`).

3. **Ciclo de Vida Financiero:**  
   $$\text{Radicación} \longrightarrow \text{Evaluación en Cadena} \longrightarrow \text{Desembolso} \longrightarrow \text{Generación de Cuotas} \longrightarrow \text{Recaudo / Abonos} \longrightarrow \text{Liquidación}$$
   - **Sistemas de Amortización:** Soporta modalidad **Francesa** (cuota fija mediante fórmula de anualidad) y modalidad **Alemana** (amortización constante a capital con intereses decrecientes).
   - **Abonos Extraordinarios:** Permite prepagos a capital con recálculo dinámico de la deuda mediante dos modalidades: **Reducir Cuota** (manteniendo el plazo restante) o **Reducir Plazo** (manteniendo el valor de la cuota de referencia).
   - **Auditoría Estricta:** Cada cambio de estado, modificación de plazo/monto o resolución deja una traza inmutable en las observaciones de la solicitud con fecha, rol y nombre del funcionario.

---

## 2. ÁRBOL COMPLETO DE ARCHIVOS DEL PROYECTO

```text
proyectoJAVA/examen_java/
│
├── README.md                                  <-- Documentación maestra del sistema
├── .gitignore                                 <-- Reglas de exclusión de Git
├── reporte_prestamos.dat                      <-- Backup binario serializado generado
│
├── material_guia/                             <-- Proyectos y recursos académicos de referencia
│   └── clases/
│       ├── clases_descomprimidas/
│       │   ├── CineK4MySQL/                   <-- Proyecto modelo MVC + DAO de clase
│       │   └── K4ArchivosJava/                <-- Proyecto modelo de persistencia binaria .dat
│       └── *.pdf / *.zip                      <-- Diapositivas y material teórico
│
└── crediYA/                                   <-- Código fuente y configuración de la aplicación
    ├── pom.xml                                <-- Archivo de configuración Maven y dependencias
    ├── db.properties                          <-- Parámetros de conexión JDBC a MySQL
    │
    ├── database/                              <-- Scripts SQL idempotentes para MySQL 8.0
    │   ├── crediya_db_ddl.sql                 <-- Definición de tablas, llaves, checks e índices
    │   └── crediya_db_dml.sql                 <-- Carga inicial y 20 clientes de prueba
    │
    └── src/main/java/                         <-- Código fuente en Java 17
        │
        ├── Main.java                          <-- Único punto de entrada (main method)
        │
        ├── controller/                        <-- Capa Controladora (Coordinación y Permisos)
        │   ├── CrediYA.java                   <-- Application Controller / Orquestador inicial
        │   ├── ClienteController.java         <-- Flujo y operaciones de Clientes
        │   ├── EmpleadoController.java        <-- Flujo de Personal y Credenciales
        │   ├── SolicitudController.java       <-- Flujo de Solicitudes y Cadena
        │   ├── PrestamoController.java        <-- Desembolsos y Planes de Amortización
        │   ├── PagoController.java            <-- Pagos ordinarios, extraordinarios y cierre
        │   └── ReporteController.java         <-- Reportes, Ficha Integral y Archivos
        │
        ├── view/                              <-- Capa de Presentación de Consola
        │   ├── MenuConsola.java               <-- Enrutador y menús dinámicos por rol
        │   │
        │   ├── comun/                         <-- Componentes transversales de interfaz
        │   │   ├── EntradaConsola.java        <-- Scanner único compartido y validación I/O
        │   │   └── MensajesView.java          <-- Estandarización visual de mensajes y banners
        │   │
        │   ├── autenticacion/
        │   │   └── LoginView.java             <-- Captura interactiva de credenciales
        │   │
        │   ├── clientes/
        │   │   └── ClienteView.java           <-- Formularios y tablas formateadas de clientes
        │   │
        │   ├── empleados/
        │   │   └── EmpleadoView.java          <-- Formularios de empleados y directorio
        │   │
        │   ├── solicitudes/
        │   │   └── SolicitudView.java         <-- Formularios de radicación y edición
        │   │
        │   ├── prestamos/
        │   │   └── PrestamoView.java          <-- Tablas de contratos y cuotas
        │   │
        │   ├── pagos/
        │   │   └── PagoView.java              <-- Menús de cartera y recibos de pago
        │   │
        │   └── reportes/
        │       └── ReporteView.java           <-- Scorecard analítico y ficha integral
        │
        ├── model/                             <-- Capa de Dominio (Entidades y Lógica de Negocio)
        │   ├── Persona.java                   <-- Superclase abstracta serializable
        │   ├── Cliente.java                   <-- Entidad Cliente con validaciones
        │   ├── Empleado.java                  <-- Entidad Funcionario con roles
        │   ├── Credencial.java                <-- Entidad de autenticación (Hash y Salt)
        │   ├── Solicitud.java                 <-- Entidad Solicitud de crédito
        │   ├── Prestamo.java                  <-- Entidad Contrato de préstamo
        │   ├── Cuota.java                     <-- Entidad Cuota con estado visual dinámico
        │   ├── Pago.java                      <-- Entidad Transacción de pago
        │   │
        │   ├── enums/                         <-- Enumeraciones de Estado y Dominio
        │   │   ├── Rol.java                   <-- ASESOR, SUPERVISOR, GERENTE
        │   │   ├── EstadoSolicitud.java       <-- PENDIENTE, APROBADA, RECHAZADA, DESEMBOLSADO
        │   │   ├── EstadoPrestamo.java        <-- ACTIVO, PAGADO
        │   │   ├── EstadoCuota.java           <-- PENDIENTE, PAGADA
        │   │   ├── ModalidadAmortizacion.java <-- FRANCES, ALEMAN
        │   │   ├── TipoPago.java              <-- CUOTA_PROGRAMADA, ABONO_EXTRAORDINARIO, PAGO_TOTAL
        │   │   └── OpcionAbonoExtra.java      <-- REDUCIR_CUOTA, REDUCIR_PLAZO
        │   │
        │   ├── amortizacion/                  <-- Patrones Strategy y Factory Method
        │   │   ├── IEstrategiaAmortizacion.java <-- Interfaz del Strategy
        │   │   ├── AmortizacionFrancesa.java  <-- Implementación de Cuota Fija
        │   │   ├── AmortizacionAlemana.java   <-- Implementación de Capital Fijo
        │   │   ├── CreadorAmortizacion.java   <-- Creator abstracto del Factory Method
        │   │   ├── CreadorFrances.java        <-- Concrete Creator Sistema Francés
        │   │   └── CreadorAleman.java         <-- Concrete Creator Sistema Alemán
        │   │
        │   └── chain/                         <-- Patrón Chain of Responsibility
        │       ├── ManejadorSolicitud.java    <-- Handler abstracto de la cadena
        │       ├── AsesorHandler.java         <-- Eslabón 1: Asesor Comercial
        │       ├── SupervisorHandler.java     <-- Eslabón 2: Supervisor (tope $200M)
        │       ├── GerenteHandler.java        <-- Eslabón 3: Gerente General (sin límite)
        │       ├── CadenaAprobacion.java      <-- Fachada ensambladora de la cadena
        │       ├── AccionEvaluacion.java      <-- Enum de acciones evaluadas en la cadena
        │       └── ResultadoCadena.java       <-- DTO del resultado y dictamen
        │
        ├── dao/                               <-- Capa de Persistencia y Acceso a Datos (JDBC)
        │   ├── ClienteDAO.java                <-- Operaciones CRUD sobre tabla clientes
        │   ├── EmpleadoDAO.java               <-- Operaciones CRUD sobre tabla empleados
        │   ├── CredencialDAO.java             <-- Consultas e inserciones en tabla credenciales
        │   ├── SolicitudDAO.java              <-- Persistencia y estados de solicitudes
        │   ├── PrestamoDAO.java               <-- Transacciones ACID de desembolso y cuotas
        │   └── PagoDAO.java                   <-- Registro transaccional de pagos y abonos
        │
        ├── database/                          <-- Conectividad a Base de Datos
        │   └── Conexion.java                  <-- Patrón Singleton para Connection Pools JDBC
        │
        ├── security/                          <-- Seguridad, Criptografía y Control de Acceso
        │   ├── SeguridadUtil.java             <-- Algoritmo PBKDF2 y generación de salt
        │   ├── AutenticacionService.java      <-- Validación de contraseñas y bootstrap
        │   ├── AutorizacionService.java       <-- Control de permisos RBAC por rol
        │   └── SesionUsuario.java             <-- Singleton de contexto del usuario activo
        │
        ├── exception/                         <-- Jerarquía de Excepciones de Negocio
        │   ├── CrediYaException.java          <-- Excepción raíz unchecked de la aplicación
        │   ├── ValidacionException.java       <-- Errores de formato y tipos de datos
        │   ├── ReglaNegocioException.java     <-- Violaciones de políticas de crédito
        │   ├── EntidadNoEncontradaException.java <-- Registros inexistentes en BD
        │   └── AutenticacionException.java    <-- Credenciales incorrectas o inactivas
        │
        └── utils/                             <-- Utilidades y Persistencia en Archivos
            └── ReporteArchivoService.java     <-- E/S física de reportes (.txt) y backups (.dat)
```

---

## 3. OBJETIVO DE CADA PAQUETE Y CADA CLASE

### Paquete Raíz (`default`)
- **`Main`**: Punto de entrada de la aplicación. Contiene el método `main(String[] args)`, que instancia `CrediYA` e inicia el ciclo de vida del sistema.

### Paquete `controller` (Controladores del Sistema)
- **`CrediYA`**: Application Controller. Verifica la conectividad con MySQL mediante `Conexion`, inicializa las instancias de DAOs, servicios y vistas, realiza el bootstrap inicial del primer Gerente si no hay credenciales y entrega el control a `MenuConsola`.
- **`ClienteController`**: Orquesta el registro de clientes (incluyendo la opción de generar credenciales de inmediato), búsqueda por ID/documento, listado y eliminación segura validando integridad financiera.
- **`EmpleadoController`**: Gestiona la consulta de perfil propio del empleado, actualización de datos de contacto y, si el rol lo autoriza (Gerente), el registro de personal y cambio de contraseñas.
- **`SolicitudController`**: Coordina la radicación de créditos, consulta general/individual, edición de datos antes de resolución, y canaliza las evaluaciones hacia la Cadena de Responsabilidad.
- **`PrestamoController`**: Administra la formalización del desembolso a partir de solicitudes aprobadas, invocando la generación transaccional de cuotas vía Strategy y facilitando consultas de tablas de amortización.
- **`PagoController`**: Controla el recaudo de cuotas programadas, valida topes para abonos extraordinarios a capital, ejecuta liquidaciones totales y expone el histórico transaccional.
- **`ReporteController`**: Procesa analítica de cartera mediante Stream API (préstamos activos, deudores en mora, préstamos cancelados), consolida indicadores en `HashMap`, genera backups serializados `.dat` y exporta a texto plano `.txt`.

### Paquete `view` y Subpaquetes (Presentación)
- **`MenuConsola`**: Enrutador central de consola. Despliega de forma limpia el menú principal, el portal del cliente, el portal del funcionario y los submenús dinámicos acordes al rol autenticado.
- **`view.comun.EntradaConsola`**: Encapsula un único objeto `Scanner` para toda la aplicación. Proporciona lectura robusta con captura de excepciones para enteros dentro de un rango, decimales mayores a un mínimo, texto no vacío y pausas de pantalla.
- **`view.comun.MensajesView`**: Provee métodos estandarizados para desplegar avisos de éxito, error, información general y encabezados oficiales con caracteres legibles.
- **`view.autenticacion.LoginView`**: Captura las credenciales de acceso (documento y contraseña) tanto para funcionarios como para clientes.
- **`view.clientes.ClienteView`**: Renderiza formularios de captura de datos de clientes, actualización de contacto y tablas formateadas con alineación tabular.
- **`view.empleados.EmpleadoView`**: Muestra formularios de personal, selección de roles y el listado de clientes asignados o atendidos por un funcionario.
- **`view.solicitudes.SolicitudView`**: Captura los parámetros de una solicitud (monto, plazo, modalidad, observaciones), formularios de edición y tabla detallada con estados.
- **`view.prestamos.PrestamoView`**: Solicita tasas nominales anuales para desembolso y despliega el cronograma completo de cuotas y saldos restantes.
- **`view.pagos.PagoView`**: Despliega el menú de operaciones de cartera, formularios de confirmación para abonos extraordinarios y tablas de recibos de pago.
- **`view.reportes.ReporteView`**: Presenta reportes analíticos de cartera, lista de deudores morosos con cuotas vencidas, la tabla de indicadores en HashMap, la Ficha Integral del Cliente y los préstamos deserializados desde archivo binario.

### Paquete `model` (Dominio y Entidades)
- **`Persona`**: Clase base abstracta serializable que encapsula atributos comunes: `id`, `nombre`, `documento` y `correo`.
- **`Cliente`**: Extiende de `Persona`. Añade el atributo `telefono` e implementa validaciones de dominio mediante expresiones regulares para documento (8 a 10 dígitos) y teléfono.
- **`Empleado`**: Extiende de `Persona`. Añade `rol` (enum `Rol`) y `salario`, con validaciones de consistencia salarial y correo institucional.
- **`Credencial`**: Modela las credenciales criptográficas asociadas a un empleado (`empleadoId`) o a un cliente (`clienteId`), almacenando el hash PBKDF2, el salt y el número de iteraciones.
- **`Solicitud`**: Representa la petición de crédito con cliente titular, empleado radicador, monto solicitado, plazo en meses, modalidad de amortización, fecha, estado y traza de auditoría.
- **`Prestamo`**: Representa el contrato financiero perfeccionado tras el desembolso. Contiene el monto capital, tasa de interés anual, cuota fija de referencia, saldo pendiente, estado y su colección de `Cuota`.
- **`Cuota`**: Representa cada vencimiento del préstamo con su desglose de capital, interés, saldo restante y estado visual dinámico (`PAGADA`, `PENDIENTE` o `VENCIDA`).
- **`Pago`**: Modela la transacción financiera de recaudo, discriminando abonos a capital, intereses corrientes y mora, vinculado al tipo de pago (`TipoPago`) y opción de abono (`OpcionAbonoExtra`).

### Paquete `model.enums` (Enumeraciones)
- **`Rol`**: Roles jerárquicos del personal: `ASESOR`, `SUPERVISOR`, `GERENTE`.
- **`EstadoSolicitud`**: Estados posibles: `PENDIENTE`, `APROBADA`, `RECHAZADA`, `DESEMBOLSADO`.
- **`EstadoPrestamo`**: Ciclo de vida del contrato: `ACTIVO`, `PAGADO`.
- **`EstadoCuota`**: Estado registrado en base de datos: `PENDIENTE`, `PAGADA`.
- **`ModalidadAmortizacion`**: Métodos matemáticos: `FRANCES`, `ALEMAN`.
- **`TipoPago`**: Naturaleza del pago: `CUOTA_PROGRAMADA`, `ABONO_EXTRAORDINARIO`, `PAGO_TOTAL`.
- **`OpcionAbonoExtra`**: Destino del abono extraordinario: `REDUCIR_CUOTA`, `REDUCIR_PLAZO`.

### Paquete `model.amortizacion` (Patrones Strategy y Factory Method)
- **`IEstrategiaAmortizacion`**: Interfaz *Strategy* que define el contrato `generarTabla(BigDecimal capital, BigDecimal tasaAnual, int plazo, LocalDate fechaInicio, Integer prestamoId)`.
- **`AmortizacionFrancesa`**: Estrategia concreta que implementa la fórmula de cuota fija:  
  $$R = P \cdot \frac{i \cdot (1 + i)^n}{(1 + i)^n - 1}$$
- **`AmortizacionAlemana`**: Estrategia concreta que implementa la amortización constante a capital ($A = P/n$) con cuotas e intereses decrecientes.
- **`CreadorAmortizacion`**: Clase abstracta *Creator* del patrón Factory Method.
- **`CreadorFrances`**: *Concrete Creator* que fabrica instancias de `AmortizacionFrancesa`.
- **`CreadorAleman`**: *Concrete Creator* que fabrica instancias de `AmortizacionAlemana`.

### Paquete `model.chain` (Patrón Chain of Responsibility)
- **`ManejadorSolicitud`**: Clase base abstracta que define el eslabón de la cadena (`sucesor`) y el método abstracto `evaluar(Solicitud, Empleado, AccionEvaluacion, Object...)`.
- **`AsesorHandler`**: Primer eslabón. Valida que el asesor no intente aprobar o rechazar solicitudes, y permite modificar montos o datos dentro de sus competencias.
- **`SupervisorHandler`**: Segundo eslabón. Autorizado para aprobar y rechazar solicitudes cuyo monto sea menor o igual a **\$200.000.000**. Si el monto supera dicho límite, transfiere la decisión automáticamente al Gerente.
- **`GerenteHandler`**: Eslabón final de máxima jerarquía. Autorizado para resolver solicitudes por cualquier monto sin límite superior.
- **`CadenaAprobacion`**: Fachada que ensambla e inicializa la cadena (`AsesorHandler` $\rightarrow$ `SupervisorHandler` $\rightarrow$ `GerenteHandler`) y expone métodos estáticos de evaluación.
- **`AccionEvaluacion`**: Enum que tipifica la acción evaluada: `MODIFICAR_MONTO`, `MODIFICAR_DATOS`, `APROBAR`, `RECHAZAR`, `ELIMINAR`.
- **`ResultadoCadena`**: Objeto de transferencia (DTO) que encapsula si la acción fue autorizada (`isAutorizado()`) y el mensaje o dictamen institucional correspondiente.

### Paquete `dao` (Persistencia y Acceso a Datos)
- **`ClienteDAO`**: Ejecuta sentencias SQL preparadas (`PreparedStatement`) para guardar, actualizar, eliminar y consultar clientes por ID o documento en MySQL.
- **`EmpleadoDAO`**: Gestiona la persistencia de funcionarios, mapeando sus datos y resolviendo su relación con la tabla de roles.
- **`CredencialDAO`**: Administra la persistencia de credenciales criptográficas asociadas a empleados o clientes, gestionando inserciones, actualizaciones de hash y consultas.
- **`SolicitudDAO`**: Controla el ciclo de persistencia de solicitudes de crédito y sus transiciones de estado.
- **`PrestamoDAO`**: Administra los préstamos. Implementa **transacciones atómicas** (`con.setAutoCommit(false)`, `commit()`, `rollback()`) para garantizar que el contrato de crédito y todas sus cuotas amortizadas (generadas mediante el Factory Method de amortización) se persistan íntegramente.
- **`PagoDAO`**: Registra transaccionalmente los recaudos, actualiza los saldos de cuotas y capital en el préstamo, e invoca el recálculo del cronograma ante abonos extraordinarios a capital.

### Paquete `database` (Conectividad)
- **`Conexion`**: Implementa el patrón **Singleton**. Lee las credenciales de base de datos desde `db.properties` o variables de entorno, suministra conexiones JDBC mediante `conectar()` y provee diagnósticos con `probarConexion()`.

### Paquete `security` (Seguridad y Control de Acceso)
- **`SeguridadUtil`**: Provee utilidades criptográficas mediante el estándar industrial `PBKDF2WithHmacSHA256` con sales criptográficas seguras (`SecureRandom`) de 16 bytes y 65.536 iteraciones para evitar ataques de diccionario o rainbow tables.
- **`AutenticacionService`**: Servicio que autentica polimórficamente credenciales contra la base de datos (identificando si el usuario es `Empleado` o `Cliente`), registra el primer gerente en bases de datos vacías y asigna contraseñas a clientes y personal.
- **`AutorizacionService`**: Implementa control de acceso basado en roles (**RBAC**). Valida si la sesión activa cuenta con los permisos necesarios para ejecutar operaciones críticas.
- **`SesionUsuario`**: Implementa el patrón **Singleton** para mantener el estado de la sesión activa en memoria (`empleadoAutenticado` o `clienteAutenticado`), determinando el rol y permitiendo el cierre de sesión seguro.

### Paquete `exception` (Manejo de Errores)
- **`CrediYaException`**: Excepción base no comprobada (`RuntimeException`) de todo el dominio CrediYA.
- **`ValidacionException`**: Lanzada ante errores de entrada de datos (longitud de documentos, formatos de correo, montos no numéricos).
- **`ReglaNegocioException`**: Lanzada ante violaciones de directrices comerciales (solicitudes ya desembolsadas, abonos que exceden el saldo pendiente, clientes con créditos activos en mora).
- **`EntidadNoEncontradaException`**: Lanzada cuando un ID o documento consultado no existe en la base de datos.
- **`AutenticacionException`**: Lanzada cuando un documento no existe o la contraseña ingresada no coincide con su hash almacenado.

### Paquete `utils` (Servicios de Archivo)
- **`ReporteArchivoService`**: Centraliza el manejo de flujos físicos de entrada/salida (`PrintWriter`, `ObjectOutputStream`, `ObjectInputStream`). Exporta reportes tabulares en texto plano (`.txt`), serializa la lista de préstamos hacia archivos binarios (`.dat`) y deserializa backups recuperando los objetos en memoria sin alterar la estabilidad de la aplicación.

---

## 4. INSTRUCCIONES PARA CONFIGURACIÓN Y EJECUCIÓN

### Requisitos Previos
- **Java Development Kit (JDK):** Versión 17 o superior instalada y configurada en la variable de entorno `JAVA_HOME`.
- **Apache Maven:** Versión 3.8 o superior instalada (o utilizar el Maven embebido de NetBeans).
- **MySQL Server:** Versión 8.0 o superior en ejecución en el puerto local por defecto (`3306`).

---

### Paso 1: Configurar la Base de Datos MySQL
Abra su cliente preferido (MySQL Workbench, DBeaver o la consola de comandos de MySQL) y ejecute los scripts en el siguiente orden:

1. Ejecutar el script DDL:
   ```sql
   SOURCE C:/Users/juanj/Downloads/proyectoJAVA/examen_java/crediYA/database/crediya_db_ddl.sql;
   ```
2. Ejecutar el script DML con los datos de prueba y roles:
   ```sql
   SOURCE C:/Users/juanj/Downloads/proyectoJAVA/examen_java/crediYA/database/crediya_db_dml.sql;
   ```

---

### Paso 2: Verificar Parámetros de Conexión
Verifique el archivo de configuración localizado en `crediYA/db.properties`:
```properties
db.url=jdbc:mysql://localhost:3306/crediya_db
db.user=root
db.password=TU_PASSWORD_AQUI
```
> [!NOTE]
> Si su usuario o contraseña de MySQL son diferentes, actualícelos en este archivo antes de iniciar.

---

### Paso 3: Compilar el Proyecto con Maven
Abra una terminal en la carpeta del proyecto `crediYA` y ejecute:
```powershell
cd C:\Users\juanj\Downloads\proyectoJAVA\examen_java\crediYA
mvn clean compile
```
Para generar el archivo ejecutable `.jar`, compile con:
```powershell
mvn clean package
```

---

### Paso 4: Ejecutar la Aplicación

#### Opción A: Desde Línea de Comandos (Terminal)
```powershell
java -jar target/crediYA-1.0-SNAPSHOT.jar
```
O directamente con el plugin de Maven:
```powershell
mvn exec:java -Dexec.mainClass="Main"
```

#### Opción B: Desde Apache NetBeans o Antigravity IDE
1. Abrir NetBeans / Antigravity IDE.
2. Menú **File** $\rightarrow$ **Open Project...** $\rightarrow$ Seleccionar la carpeta `crediYA`.
3. Hacer clic derecho sobre el proyecto `crediYA` y seleccionar **Clean and Build**.
4. Presionar la tecla **F6** (o clic derecho $\rightarrow$ **Run**). La aplicación iniciará en la consola integrada.

---

## 6. TABLA DE CREDENCIALES DE PRUEBA PRECONFIGURADAS

El sistema cuenta con un ecosistema completo de cuentas de prueba precargadas en `database/crediya_db_dml.sql`:

### A. Funcionarios de la Entidad
| Rol | Documento | Contraseña | Funcionario | Capacidades Principales |
| :--- | :---: | :---: | :--- | :--- |
| **ASESOR** | `1001` | `Asesor123*` | Carlos Andrés Mendoza | Radicación de clientes y solicitudes; registro de pagos. No aprueba ni desembolsa. |
| **ASESOR** | `1002` | `Asesor123*` | Laura Sofía Restrepo | Radicación y atención de clientes. |
| **SUPERVISOR** | `2001` | `Super123*` | Andrés Felipe Vargas | Aprobación/Rechazo de solicitudes hasta **\$200M**; desembolsos; reportes de cartera. |
| **SUPERVISOR** | `2002` | `Super123*` | Diana Patricia Ospina | Aprobación hasta **\$200M**; desembolsos; consultas generales. |
| **GERENTE** | `3001` | `Gerente123*` | Juan José González | **Aprobación sin límite**; gestión de empleados y credenciales; backups `.dat`; métricas consolidadas. |
| **GERENTE** | `3002` | `Gerente123*` | Roberto Carlos Silva | Administración integral y resolución de montos mayores. |

### B. Clientes para Portal Web / Móvil de Clientes
Todos los clientes de prueba comparten la contraseña: `123456`

| Documento | Contraseña | Cliente | Escenario Financiero Asociado |
| :---: | :---: | :--- | :--- |
| `10000000` | `123456` | Alejandro Morales Vega | **Cliente nuevo:** Sin solicitudes ni contratos vigentes. |
| `10000001` | `123456` | Beatriz Helena Castro | **Cliente nuevo:** Sin historial previo. |
| `10000002` | `123456` | Camilo Andrés Pardo | **Solicitud PENDIENTE:** \$5.000.000 a 12 meses (Francés). |
| `10000003` | `123456` | Daniela María Ríos | **Solicitud PENDIENTE:** \$12.000.000 a 24 meses (Alemán). |
| `10000004` | `123456` | Esteban José Valencia | **Solicitud RECHAZADA:** \$50.000.000 (Capacidad de pago insuficiente). |
| `10000005` | `123456` | Fernanda Lucía Montoya | **Solicitud APROBADA:** \$8.000.000 a 18 meses (Lista para desembolso). |
| `10000006` | `123456` | Gabriel Antonio Ortiz | **Solicitud APROBADA:** \$15.000.000 a 24 meses (Lista para desembolso). |
| `10000007` | `123456` | Helena Patricia Muñoz | **Préstamo ACTIVO al día:** Sistema Francés, cuotas 1 y 2 pagadas, cuotas 3 a 12 al día. |
| `10000008` | `123456` | Ignacio David Salcedo | **Préstamo ACTIVO al día:** Sistema Alemán, cuotas 1 a 3 pagadas, cuotas 4 a 12 al día. |
| `10000009` | `123456` | Juliana Marcela Cárdenas | **Préstamo en MORA:** Sistema Francés con cuotas vencidas no pagadas. |
| `10000010` | `123456` | Kevin Mauricio Duarte | **Préstamo en MORA:** Sistema Alemán con cuotas vencidas en fechas pasadas. |
| `10000011` | `123456` | Lorena Isabel Bermúdez | **Cliente regular:** Con consultas de cartera. |
| `10000012` | `123456` | Manuel Fernando Quintero | **Abono Extraordinario:** \$3.000.000 prepagados con reducción de cuota. |
| `10000013` | `123456` | Natalia Sofía Herrera | **Cliente en seguimiento:** Historial financiero activo. |
| `10000014` | `123456` | Oscar Eduardo Salazar | **Préstamo PAGADO:** Microcrédito de 3 cuotas liquidado al 100% [PAGADO]. |
| `10000015` | `123456` | Paula Andrea Giraldo | **Cancelación Anticipada:** Liquidación total de saldo pendiente. |
| `10000016` | `123456` | Quirino Alberto Mejía | **Doble solicitud:** Una en estado PENDIENTE y otra en curso. |
| `10000017` | `123456` | Rosa María Villamizar | **Múltiples contratos:** Préstamo anterior cancelado y uno nuevo activo. |
| `10000018` | `123456` | Sergio Daniel Restrepo | **Crédito Empresarial:** \$80.000.000 autorizado por Supervisor de Cartera. |
| `10000019` | `123456` | Tatiana Giselle Guzmán | **Crédito Industrial:** \$150.000.000 autorizado por Gerencia General. |

---

## 7. CRÉDITOS Y LICENCIA
- **Institución:** Proyecto Académico de Ingeniería y Desarrollo de Software.
- **Autor / Estudiante:** Equipo de Desarrollo CrediYA S.A.S.
- **Licencia:** Material educativo de libre distribución académica bajo estándares de código limpio y buenas prácticas de ingeniería.


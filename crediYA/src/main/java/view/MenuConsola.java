package view;

import chain.AccionEvaluacion;
import chain.CadenaAprobacion;
import chain.ManejadorSolicitud;
import chain.ResultadoCadena;
import dao.ClienteDAO;
import dao.EmpleadoDAO;
import dao.PagoDAO;
import dao.PrestamoDAO;
import dao.SolicitudDAO;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;
import model.Cliente;
import model.Cuota;
import model.Empleado;
import model.Pago;
import model.Prestamo;
import model.Solicitud;
import model.enums.EstadoCuota;
import model.enums.EstadoPrestamo;
import model.enums.EstadoSolicitud;
import model.enums.ModalidadAmortizacion;
import model.enums.OpcionAbonoExtra;
import model.enums.Rol;
import model.enums.TipoPago;
import security.AutenticacionService;
import security.AutorizacionService;
import security.SesionUsuario;

public class MenuConsola {

    private final Scanner scanner;
    private final ClienteDAO clienteDAO;
    private final EmpleadoDAO empleadoDAO;
    private final SolicitudDAO solicitudDAO;
    private final PrestamoDAO prestamoDAO;
    private final PagoDAO pagoDAO;
    private final AutenticacionService autenticacionService;
    private final AutorizacionService autorizacionService;
    private final SesionUsuario sesion;
    private final ManejadorSolicitud cadenaAprobacion;

    public MenuConsola() {
        this.scanner = new Scanner(System.in);
        this.clienteDAO = new ClienteDAO();
        this.empleadoDAO = new EmpleadoDAO();
        this.solicitudDAO = new SolicitudDAO();
        this.prestamoDAO = new PrestamoDAO();
        this.pagoDAO = new PagoDAO();
        this.autenticacionService = new AutenticacionService();
        this.autorizacionService = new AutorizacionService();
        this.sesion = SesionUsuario.getInstancia();
        this.cadenaAprobacion = CadenaAprobacion.obtenerCadena();
    }

    public void iniciarMenuPrincipal() {

        if (!autenticacionService.tieneCredencialesConfiguradas()) {
            configurarPrimerGerente();
        }

        boolean salir = false;
        while (!salir) {
            System.out.println("\n==================================================");
            System.out.println("        SISTEMA DE GESTION CREDIYA S.A.S.         ");
            System.out.println("             ACCESO Y AUTENTICACION               ");
            System.out.println("==================================================");
            System.out.println("  1. Iniciar sesion");
            System.out.println("  2. Configuracion inicial de Gerente");
            System.out.println("  3. Salir de la aplicacion");
            System.out.println("==================================================");

            int opcion = leerEntero("Seleccione una opcion [1-3]: ", 1, 3);
            switch (opcion) {
                case 1 -> iniciarSesion();
                case 2 -> configurarPrimerGerente();
                case 3 -> {
                    salir = true;
                    System.out.println("\n>> Ha salido del sistema CrediYa S.A.S. Hasta pronto.");
                }
            }
        }
    }

    private void iniciarSesion() {
        System.out.println("\n--- INICIO DE SESION DE EMPLEADO ---");
        String documento = leerTexto("Documento de identidad: ");
        String password = leerTexto("Contraseña: ");

        Empleado emp = autenticacionService.autenticar(documento, password.toCharArray());
        if (emp != null) {
            sesion.iniciarSesion(emp);
            System.out.printf("\n>> OK: Autenticación exitosa! Bienvenido/a %s\n", emp.getNombre());
            System.out.printf(">> Rol asignado: %s\n", emp.getRol());

            desplegarMenuPorRol(emp);

            sesion.cerrarSesion();
            System.out.println(">> Sesión finalizada correctamente.");
        } else {
            System.out.println(">> Error: Documento o contraseña inválidos.");
            pausar();
        }
    }

    private void desplegarMenuPorRol(Empleado emp) {
        switch (emp.getRol()) {
            case ASESOR -> menuAsesor(emp);
            case SUPERVISOR -> menuSupervisor(emp);
            case GERENTE -> menuGerente(emp);
        }
    }

    private void configurarPrimerGerente() {
        System.out.println("\n==================================================");
        System.out.println("     CONFIGURACION INICIAL DE ACCESO: GERENTE     ");
        System.out.println("==================================================");
        System.out.println("Este proceso permite configurar las credenciales del");
        System.out.println("primer GERENTE en una base de datos recién instalada.");
        System.out.println("==================================================");

        String doc = leerTexto("Ingrese el documento del empleado GERENTE: ");
        Empleado emp = empleadoDAO.buscarPorDocumento(doc);

        if (emp == null) {
            System.out.println(">> No existe ningún empleado con ese documento.");
            String reg = leerTexto("¿Desea registrar al primer empleado GERENTE ahora? (S/N): ");
            if (reg.equalsIgnoreCase("S")) {
                String nombre = leerTexto("Nombre completo del Gerente: ");
                String correo = leerTexto("Correo electrónico: ");
                BigDecimal salario = leerBigDecimal("Salario: ", BigDecimal.ZERO);
                emp = new Empleado(nombre, doc, correo, Rol.GERENTE, salario);
                if (empleadoDAO.guardar(emp)) {
                    System.out.println(">> OK: Empleado GERENTE creado con ID: " + emp.getId());
                } else {
                    System.out.println(">> Error al crear empleado GERENTE.");
                    pausar();
                    return;
                }
            } else {
                pausar();
                return;
            }
        }

        if (emp.getRol() != Rol.GERENTE) {
            System.out.println(">> ERROR: El empleado con documento " + doc + " tiene rol " + emp.getRol() + ", NO es GERENTE.");
            pausar();
            return;
        }

        System.out.println("Asignando contraseña para: " + emp.getNombre() + " (GERENTE)");
        String pass1 = leerTexto("Ingrese la nueva contraseña: ");
        String pass2 = leerTexto("Confirme la contraseña: ");

        if (!pass1.equals(pass2)) {
            System.out.println(">> Error: Las contraseñas no coinciden.");
            pausar();
            return;
        }

        if (autenticacionService.registrarCredencialGerente(doc, pass1.toCharArray())) {
            System.out.println(">> OK: Credenciales del GERENTE configuradas exitosamente.");
            System.out.println(">> Ahora puede iniciar sesión con el documento " + doc);
        } else {
            System.out.println(">> Error: No se pudieron registrar las credenciales (posiblemente ya existían).");
        }
        pausar();
    }

    private void menuAsesor(Empleado emp) {
        int op;
        do {
            System.out.println("\n==================================================");
            System.out.printf("  CREDIYA S.A.S. - MENU DEL ASESOR COMERCIAL\n");
            System.out.printf("  Usuario: %s | Rol: ASESOR\n", emp.getNombre());
            System.out.println("==================================================");
            System.out.println("  1. Registrar nuevo cliente");
            System.out.println("  2. Consultar y listar clientes");
            System.out.println("  3. Actualizar datos de un cliente");
            System.out.println("  4. Radicar nueva solicitud de crédito");
            System.out.println("  5. Consultar MIS solicitudes asignadas");
            System.out.println("  6. Consultar MIS préstamos asignados");
            System.out.println("  7. Registrar pago o abono a préstamo");
            System.out.println("  8. Cerrar sesión");
            System.out.println("==================================================");

            op = leerEntero("Opcion [1-8]: ", 1, 8);
            switch (op) {
                case 1 -> registrarCliente();
                case 2 -> listarClientes();
                case 3 -> actualizarCliente();
                case 4 -> radicarSolicitud(emp);
                case 5 -> listarSolicitudesAsignadas(emp);
                case 6 -> listarPrestamosAsignados(emp);
                case 7 -> menuOperacionesPagos();
                case 8 -> System.out.println("Cerrando sesión de Asesor...");
            }
        } while (op != 8);
    }

    private void menuSupervisor(Empleado emp) {
        int op;
        do {
            System.out.println("\n==================================================");
            System.out.printf("  CREDIYA S.A.S. - MENU DEL SUPERVISOR DE CARTERA\n");
            System.out.printf("  Usuario: %s | Rol: SUPERVISOR\n", emp.getNombre());
            System.out.println("==================================================");
            System.out.println("  1. Gestion de Clientes (Consultar / Registrar)");
            System.out.println("  2. Radicar nueva solicitud de crédito");
            System.out.println("  3. Consultar TODAS las solicitudes de crédito");
            System.out.println("  4. Evaluar solicitud (Cadena de Responsabilidad)");
            System.out.println("  5. Desembolsar préstamo desde solicitud APROBADA");
            System.out.println("  6. Consultar todos los préstamos y tablas de cuotas");
            System.out.println("  7. Registrar pago o abono a préstamo");
            System.out.println("  8. Consultar reportes generales (Préstamos activos y Mora)");
            System.out.println("  9. Cerrar sesión");
            System.out.println("==================================================");

            op = leerEntero("Opcion [1-9]: ", 1, 9);
            switch (op) {
                case 1 -> subMenuClientesSupervisor();
                case 2 -> radicarSolicitud(emp);
                case 3 -> listarTodasSolicitudes();
                case 4 -> evaluarSolicitudCadena(emp);
                case 5 -> desembolsarPrestamo();
                case 6 -> subMenuConsultaPrestamos();
                case 7 -> menuOperacionesPagos();
                case 8 -> subMenuReportesGenerales();
                case 9 -> System.out.println("Cerrando sesión de Supervisor...");
            }
        } while (op != 9);
    }

    private void menuGerente(Empleado emp) {
        int op;
        do {
            System.out.println("\n==================================================");
            System.out.printf("  CREDIYA S.A.S. - MENU DE GERENCIA GENERAL\n");
            System.out.printf("  Usuario: %s | Rol: GERENTE\n", emp.getNombre());
            System.out.println("==================================================");
            System.out.println("  1. Gestion de Empleados y Credenciales");
            System.out.println("  2. Gestion de Clientes");
            System.out.println("  3. Radicar nueva solicitud de crédito");
            System.out.println("  4. Consultar TODAS las solicitudes");
            System.out.println("  5. Evaluar solicitud (Aprobación/Rechazo de Gerencia)");
            System.out.println("  6. Desembolsar préstamo desde solicitud APROBADA");
            System.out.println("  7. Consultar todos los préstamos y amortizaciones");
            System.out.println("  8. Registrar pago o abono a préstamo");
            System.out.println("  9. Reportes y Exportación (.txt y .dat)");
            System.out.println(" 10. Cerrar sesión");
            System.out.println("==================================================");

            op = leerEntero("Opcion [1-10]: ", 1, 10);
            switch (op) {
                case 1 -> subMenuGestionEmpleados();
                case 2 -> subMenuClientesSupervisor();
                case 3 -> radicarSolicitud(emp);
                case 4 -> listarTodasSolicitudes();
                case 5 -> evaluarSolicitudCadena(emp);
                case 6 -> desembolsarPrestamo();
                case 7 -> subMenuConsultaPrestamos();
                case 8 -> menuOperacionesPagos();
                case 9 -> subMenuReportesYExportacion();
                case 10 -> System.out.println("Cerrando sesión de Gerente...");
            }
        } while (op != 10);
    }

    private void registrarCliente() {
        if (!autorizacionService.validarPermiso(autorizacionService.puedeRegistrarClientes(), "Registrar clientes")) return;
        System.out.println("\n>> REGISTRAR NUEVO CLIENTE");
        String nombre = leerTexto("Nombre completo: ");
        String doc = leerTexto("Documento de identidad: ");
        String correo = leerTexto("Correo electrónico: ");
        String tel = leerTexto("Teléfono de contacto: ");

        Cliente c = new Cliente(nombre, doc, correo, tel);
        if (clienteDAO.guardar(c)) {
            System.out.println(">> OK: Cliente registrado exitosamente con ID: " + c.getId());
        } else {
            System.out.println(">> Error: No se pudo registrar el cliente.");
        }
        pausar();
    }

    private void listarClientes() {
        if (!autorizacionService.validarPermiso(autorizacionService.puedeConsultarClientes(), "Consultar clientes")) return;
        System.out.println("\n>> LISTADO DE CLIENTES REGISTRADOS");
        List<Cliente> lista = clienteDAO.listarTodos();
        if (lista.isEmpty()) {
            System.out.println("No hay clientes registrados en el sistema.");
        } else {
            System.out.printf("%-5s %-25s %-15s %-25s %-15s\n", "ID", "NOMBRE", "DOCUMENTO", "CORREO", "TELEFONO");
            System.out.println("-----------------------------------------------------------------------------------------");
            for (Cliente c : lista) {
                System.out.printf("%-5d %-25s %-15s %-25s %-15s\n",
                    c.getId(), c.getNombre(), c.getDocumento(), c.getCorreo(), c.getTelefono());
            }
        }
        pausar();
    }

    private void actualizarCliente() {
        if (!autorizacionService.validarPermiso(autorizacionService.puedeActualizarClientes(), "Actualizar clientes")) return;
        System.out.println("\n>> ACTUALIZAR DATOS DE CLIENTE");
        int id = leerEntero("ID del cliente a modificar: ", 1, Integer.MAX_VALUE);
        Cliente c = clienteDAO.buscarPorId(id);
        if (c == null) {
            System.out.println(">> Cliente no encontrado.");
            pausar();
            return;
        }

        System.out.println("Cliente actual: " + c);
        c.setNombre(leerTexto("Nuevo nombre completo [" + c.getNombre() + "]: "));
        c.setCorreo(leerTexto("Nuevo correo [" + c.getCorreo() + "]: "));
        c.setTelefono(leerTexto("Nuevo teléfono [" + c.getTelefono() + "]: "));

        if (clienteDAO.actualizar(c)) {
            System.out.println(">> OK: Datos del cliente actualizados exitosamente.");
        } else {
            System.out.println(">> Error al actualizar el cliente.");
        }
        pausar();
    }

    private void subMenuClientesSupervisor() {
        System.out.println("\n--- SUBMENU CLIENTES ---");
        System.out.println("1. Listar clientes");
        System.out.println("2. Registrar cliente");
        System.out.println("3. Actualizar cliente");
        System.out.println("4. Volver");
        int op = leerEntero("Opción: ", 1, 4);
        switch (op) {
            case 1 -> listarClientes();
            case 2 -> registrarCliente();
            case 3 -> actualizarCliente();
        }
    }

    private void radicarSolicitud(Empleado empActual) {
        if (!autorizacionService.validarPermiso(autorizacionService.puedeRadicarSolicitudes(), "Radicar solicitudes")) return;
        System.out.println("\n>> RADICAR NUEVA SOLICITUD DE CREDITO");
        int idCliente = leerEntero("ID del Cliente: ", 1, Integer.MAX_VALUE);
        Cliente cli = clienteDAO.buscarPorId(idCliente);
        if (cli == null) {
            System.out.println(">> Error: No se encontró el cliente.");
            pausar();
            return;
        }

        BigDecimal monto = leerBigDecimal("Monto solicitado ($): ", new BigDecimal("100000.00"));
        int plazo = leerEntero("Plazo en meses [1-72]: ", 1, 72);

        System.out.println("Modalidad: 1. FRANCES (Cuota fija) | 2. ALEMAN (Capital fijo)");
        int opMod = leerEntero("Seleccione modalidad [1-2]: ", 1, 2);
        ModalidadAmortizacion mod = (opMod == 1) ? ModalidadAmortizacion.FRANCES : ModalidadAmortizacion.ALEMAN;

        String obs = leerTexto("Destino del crédito / Observaciones: ");

        Solicitud sol = new Solicitud(cli, empActual, monto, plazo, mod, LocalDate.now(), obs);
        if (solicitudDAO.guardar(sol)) {
            System.out.println(">> OK: Solicitud radicada exitosamente con número #" + sol.getId() + " [PENDIENTE]");
            System.out.printf("   Responsable asignado: %s (%s)\n", empActual.getNombre(), empActual.getRol());
        } else {
            System.out.println(">> Error al radicar la solicitud.");
        }
        pausar();
    }

    private void listarSolicitudesAsignadas(Empleado asesor) {
        System.out.println("\n>> MIS SOLICITUDES ASIGNADAS (ASESOR: " + asesor.getNombre() + ")");
        List<Solicitud> lista = solicitudDAO.listarPorEmpleado(asesor.getId());
        mostrarTablaSolicitudes(lista);
        pausar();
    }

    private void listarTodasSolicitudes() {
        if (!autorizacionService.validarPermiso(autorizacionService.puedeConsultarTodasSolicitudes(), "Consultar todas las solicitudes")) return;
        System.out.println("\n>> TODAS LAS SOLICITUDES DE CREDITO");
        List<Solicitud> lista = solicitudDAO.listarTodos();
        mostrarTablaSolicitudes(lista);
        pausar();
    }

    private void mostrarTablaSolicitudes(List<Solicitud> lista) {
        if (lista.isEmpty()) {
            System.out.println("No se encontraron solicitudes.");
            return;
        }
        System.out.printf("%-6s %-20s %-16s %-12s %-6s %-10s %-12s\n",
            "NUM", "CLIENTE", "ASESOR/RESP", "MONTO", "PLAZO", "MODALIDAD", "ESTADO");
        System.out.println("-----------------------------------------------------------------------------------------");
        for (Solicitud s : lista) {
            System.out.printf("#%-5d %-20s %-16s $%,10.2f %-4d m %-10s %-12s\n",
                s.getId(),
                s.getCliente() != null ? s.getCliente().getNombre() : "N/A",
                s.getEmpleado() != null ? s.getEmpleado().getNombre() : "N/A",
                s.getMontoSolicitado(),
                s.getPlazoMeses(),
                s.getModalidad(),
                s.getEstado());
        }
    }

    private void evaluarSolicitudCadena(Empleado evaluador) {
        boolean puedeAprobar = autorizacionService.puedeAprobarSolicitudes();
        if (!autorizacionService.validarPermiso(puedeAprobar, "Evaluar solicitudes")) return;

        System.out.println("\n>> EVALUACION DE SOLICITUD — CADENA DE RESPONSABILIDAD");
        int idSol = leerEntero("Número de solicitud a evaluar: ", 1, Integer.MAX_VALUE);
        Solicitud sol = solicitudDAO.buscarPorId(idSol);

        if (sol == null) {
            System.out.println(">> Solicitud no encontrada.");
            pausar();
            return;
        }

        System.out.printf("Solicitud #%d | Cliente: %s | Monto: $%,.2f | Estado: %s\n",
            sol.getId(), sol.getCliente().getNombre(), sol.getMontoSolicitado(), sol.getEstado());
        System.out.println("Observaciones registradas: " + sol.getObservaciones());

        if (sol.getEstado() != EstadoSolicitud.PENDIENTE) {
            System.out.println(">> Aviso: La solicitud ya se encuentra en estado " + sol.getEstado());
        }

        System.out.println("\nAcciones disponibles en la cadena:");
        System.out.println("  1. APROBAR solicitud");
        System.out.println("  2. RECHAZAR solicitud");
        System.out.println("  3. DERIVAR al siguiente nivel");
        System.out.println("  4. Cancelar");

        int op = leerEntero("Acción [1-4]: ", 1, 4);
        if (op == 4) return;

        AccionEvaluacion accion = switch (op) {
            case 1 -> AccionEvaluacion.APROBAR;
            case 2 -> AccionEvaluacion.RECHAZAR;
            default -> AccionEvaluacion.DERIVAR;
        };

        String notas = leerTexto("Motivo / Dictamen técnico: ");

        ResultadoCadena resultado = cadenaAprobacion.procesar(sol, evaluador, accion, notas);

        if (resultado.isExitoso()) {
            solicitudDAO.actualizar(sol);
            System.out.println("\n>> RESULTADO DE LA CADENA:");
            System.out.println("   " + resultado.getMensaje());
            System.out.println("   Nuevo estado: " + sol.getEstado());
        } else {
            System.out.println("\n>> NO SE PUDO CONCLUIR LA EVALUACION EN LA CADENA:");
            System.out.println("   " + resultado.getMensaje());
        }
        pausar();
    }

    private void desembolsarPrestamo() {
        if (!autorizacionService.validarPermiso(autorizacionService.puedeDesembolsarPrestamos(), "Desembolsar préstamos")) return;
        System.out.println("\n>> CREAR Y DESEMBOLSAR PRESTAMO DESDE SOLICITUD APROBADA");
        int idSol = leerEntero("Número de Solicitud APROBADA: ", 1, Integer.MAX_VALUE);
        Solicitud sol = solicitudDAO.buscarPorId(idSol);

        if (sol == null) {
            System.out.println(">> Solicitud no encontrada.");
            pausar();
            return;
        }

        if (sol.getEstado() != EstadoSolicitud.APROBADA) {
            System.out.printf(">> REGLA DE NEGOCIO: No se puede originar un préstamo. "
                            + "La solicitud está en estado %s (debe estar APROBADA).\n", sol.getEstado());
            pausar();
            return;
        }

        BigDecimal tasaAnual = leerBigDecimal("Tasa nominal anual porcentual (%) [ej. 24.0]: ", BigDecimal.ZERO);

        Prestamo p = new Prestamo();
        p.setSolicitud(sol);
        p.setMontoCapital(sol.getMontoSolicitado());
        p.setTasaInteresAnual(tasaAnual);
        p.setPlazoCuotas(sol.getPlazoMeses());
        p.setModalidad(sol.getModalidad());
        p.setFechaInicio(LocalDate.now());

        if (prestamoDAO.crearPrestamo(p)) {
            System.out.println("\n>> OK: Préstamo #" + p.getId() + " desembolsado y calendario de cuotas generado!");
            System.out.printf("   Monto Capital:    $%,12.2f\n", p.getMontoCapital());
            System.out.printf("   Cuota Referencia: $%,12.2f\n", p.getValorCuotaReferencia());
            System.out.printf("   Monto Total:      $%,12.2f\n", p.getMontoTotal());
        } else {
            System.out.println(">> Error al desembolsar el préstamo.");
        }
        pausar();
    }

    private void listarPrestamosAsignados(Empleado asesor) {
        System.out.println("\n>> MIS PRESTAMOS ASIGNADOS (ASESOR: " + asesor.getNombre() + ")");
        List<Prestamo> lista = prestamoDAO.listarPorEmpleado(asesor.getId());
        mostrarTablaPrestamos(lista);
        pausar();
    }

    private void subMenuConsultaPrestamos() {
        if (!autorizacionService.validarPermiso(autorizacionService.puedeConsultarTodosPrestamos(), "Consultar préstamos")) return;
        System.out.println("\n--- CONSULTA DE PRESTAMOS ---");
        System.out.println("1. Listar todos los préstamos");
        System.out.println("2. Consultar tabla de cuotas y amortización de un préstamo");
        System.out.println("3. Volver");
        int op = leerEntero("Opción: ", 1, 3);
        switch (op) {
            case 1 -> {
                List<Prestamo> lista = prestamoDAO.listarTodos();
                mostrarTablaPrestamos(lista);
                pausar();
            }
            case 2 -> consultarDetallePrestamo();
        }
    }

    private void mostrarTablaPrestamos(List<Prestamo> lista) {
        if (lista.isEmpty()) {
            System.out.println("No se encontraron préstamos.");
            return;
        }
        System.out.printf("%-5s %-20s %-12s %-8s %-10s %-14s %-14s %-10s\n",
            "ID", "CLIENTE", "CAPITAL", "TASA", "MODALIDAD", "CUOTA_REF", "SALDO_PEND", "ESTADO");
        System.out.println("-------------------------------------------------------------------------------------------------");
        for (Prestamo p : lista) {
            System.out.printf("%-5d %-20s $%,10.2f %-6.2f%% %-10s $%,12.2f $%,12.2f %-10s\n",
                p.getId(),
                p.getCliente() != null ? p.getCliente().getNombre() : "N/A",
                p.getMontoCapital(),
                p.getTasaInteresAnual(),
                p.getModalidad(),
                p.getValorCuotaReferencia(),
                p.getSaldoPendiente(),
                p.getEstado());
        }
    }

    private void consultarDetallePrestamo() {
        int idP = leerEntero("ID del préstamo a consultar: ", 1, Integer.MAX_VALUE);
        Prestamo p = prestamoDAO.buscarPorId(idP);
        if (p == null) {
            System.out.println(">> Préstamo no encontrado.");
            pausar();
            return;
        }

        System.out.println("\n===============================================================================");
        System.out.printf("DETALLE DEL PRESTAMO #%d - CLIENTE: %s\n", p.getId(), p.getCliente().getNombre());
        System.out.printf("Capital: $%,.2f | Tasa: %.2f%% | Saldo Pendiente: $%,.2f | Estado: %s\n",
            p.getMontoCapital(), p.getTasaInteresAnual(), p.getSaldoPendiente(), p.getEstado());
        System.out.println("===============================================================================");
        System.out.printf("%-4s %-12s %-12s %-12s %-12s %-12s %-12s %-10s\n",
            "NRO", "VTO", "VALOR_CUOTA", "CAPITAL", "INTERES", "SALDO_CAP", "PAGADO", "ESTADO");
        System.out.println("-----------------------------------------------------------------------------------------------");

        for (Cuota c : p.getCuotas()) {
            System.out.printf("%-4d %-12s $%,10.2f $%,10.2f $%,10.2f $%,10.2f $%,10.2f %-10s\n",
                c.getNumeroCuota(), c.getFechaVencimiento(), c.getValorCuota(),
                c.getValorCapital(), c.getValorInteres(), c.getSaldoRestanteCapital(),
                c.getValorPagado(), c.getEstado());
        }
        pausar();
    }

    private void menuOperacionesPagos() {
        if (!autorizacionService.validarPermiso(autorizacionService.puedeRegistrarPagos(), "Registrar pagos")) return;
        System.out.println("\n--- REGISTRAR PAGO O ABONO ---");
        System.out.println("1. Pagar cuota programada del calendario");
        System.out.println("2. Registrar abono extraordinario a capital");
        System.out.println("3. Cancelación total del préstamo");
        System.out.println("4. Consultar histórico de pagos de un préstamo");
        System.out.println("5. Volver");

        int op = leerEntero("Opción [1-5]: ", 1, 5);
        switch (op) {
            case 1 -> pagarCuotaProgramada();
            case 2 -> registrarAbonoExtraordinario();
            case 3 -> cancelarTotalPrestamo();
            case 4 -> consultarHistoricoPagos();
        }
    }

    private void pagarCuotaProgramada() {
        int idP = leerEntero("ID del Préstamo: ", 1, Integer.MAX_VALUE);
        Prestamo p = prestamoDAO.buscarPorId(idP);
        if (p == null || p.getEstado() == EstadoPrestamo.PAGADO) {
            System.out.println(">> Préstamo no disponible o ya pagado.");
            pausar();
            return;
        }

        Cuota cuotaPendiente = p.getCuotas().stream()
                .filter(c -> c.getEstado() == EstadoCuota.PENDIENTE)
                .findFirst()
                .orElse(null);

        if (cuotaPendiente == null) {
            System.out.println(">> No hay cuotas pendientes en este préstamo.");
            pausar();
            return;
        }

        BigDecimal cap = cuotaPendiente.getCapitalPendiente();
        BigDecimal inte = cuotaPendiente.getInteresPendiente();
        BigDecimal total = cap.add(inte);

        System.out.printf("Cuota #%d | Vence: %s | Total a pagar: $%,.2f (Cap: $%,.2f, Int: $%,.2f)\n",
            cuotaPendiente.getNumeroCuota(), cuotaPendiente.getFechaVencimiento(), total, cap, inte);

        String conf = leerTexto("¿Confirmar pago de la cuota? (S/N): ");
        if (conf.equalsIgnoreCase("S")) {
            Pago pago = new Pago(p.getId(), cuotaPendiente.getId(), LocalDate.now(),
                                 total, BigDecimal.ZERO, inte, cap,
                                 TipoPago.CUOTA_PROGRAMADA, null, "Pago cuota #" + cuotaPendiente.getNumeroCuota());
            if (pagoDAO.registrarPago(pago)) {
                System.out.println(">> OK: Pago registrado exitosamente! ID Transacción: " + pago.getId());
            } else {
                System.out.println(">> Error al procesar el pago.");
            }
        }
        pausar();
    }

    private void registrarAbonoExtraordinario() {
        int idP = leerEntero("ID del Préstamo: ", 1, Integer.MAX_VALUE);
        Prestamo p = prestamoDAO.buscarPorId(idP);
        if (p == null || p.getEstado() == EstadoPrestamo.PAGADO) {
            System.out.println(">> Préstamo no disponible o ya pagado.");
            pausar();
            return;
        }

        System.out.printf("Saldo capital actual: $%,.2f\n", p.getSaldoPendiente());
        BigDecimal monto = leerBigDecimal("Monto del abono extraordinario ($): ", BigDecimal.ONE);
        if (monto.compareTo(p.getSaldoPendiente()) > 0) {
            System.out.println(">> El abono no puede superar el saldo pendiente.");
            pausar();
            return;
        }

        System.out.println("Opción: 1. REDUCIR_CUOTA | 2. REDUCIR_PLAZO");
        int op = leerEntero("Opción [1-2]: ", 1, 2);
        OpcionAbonoExtra opcion = (op == 1) ? OpcionAbonoExtra.REDUCIR_CUOTA : OpcionAbonoExtra.REDUCIR_PLAZO;

        Pago pago = new Pago(p.getId(), null, LocalDate.now(), monto,
                             BigDecimal.ZERO, BigDecimal.ZERO, monto,
                             TipoPago.ABONO_EXTRAORDINARIO, opcion, "Abono extraordinario (" + opcion + ")");

        if (pagoDAO.registrarPago(pago)) {
            System.out.println(">> OK: Abono extraordinario registrado exitosamente.");
        } else {
            System.out.println(">> Error al registrar el abono extraordinario.");
        }
        pausar();
    }

    private void cancelarTotalPrestamo() {
        int idP = leerEntero("ID del Préstamo: ", 1, Integer.MAX_VALUE);
        Prestamo p = prestamoDAO.buscarPorId(idP);
        if (p == null || p.getEstado() == EstadoPrestamo.PAGADO) {
            System.out.println(">> Préstamo no disponible o ya cancelado.");
            pausar();
            return;
        }

        BigDecimal saldo = p.getSaldoPendiente();
        System.out.printf("Monto de cancelación total: $%,.2f\n", saldo);
        String conf = leerTexto("¿Confirmar cancelación total? (S/N): ");
        if (conf.equalsIgnoreCase("S")) {
            Pago pago = new Pago(p.getId(), null, LocalDate.now(), saldo,
                                 BigDecimal.ZERO, BigDecimal.ZERO, saldo,
                                 TipoPago.PAGO_TOTAL, null, "Cancelación total anticipada");
            if (pagoDAO.registrarPago(pago)) {
                System.out.println(">> OK: Préstamo #" + p.getId() + " liquidado en su totalidad (PAGADO).");
            }
        }
        pausar();
    }

    private void consultarHistoricoPagos() {
        int idP = leerEntero("ID del Préstamo: ", 1, Integer.MAX_VALUE);
        List<Pago> pagos = pagoDAO.listarPorPrestamo(idP);
        if (pagos.isEmpty()) {
            System.out.println("No hay pagos registrados para este préstamo.");
        } else {
            System.out.printf("%-5s %-12s %-14s %-10s %-10s %-12s %-20s\n",
                "ID", "FECHA", "MONTO TOTAL", "MORA", "INTERES", "CAPITAL", "TIPO");
            System.out.println("-----------------------------------------------------------------------------------------");
            for (Pago p : pagos) {
                System.out.printf("%-5d %-12s $%,12.2f $%,8.2f $%,8.2f $%,10.2f %-20s\n",
                    p.getId(), p.getFechaPago(), p.getMonto(),
                    p.getMontoMora(), p.getMontoInteres(), p.getMontoCapital(), p.getTipoPago());
            }
        }
        pausar();
    }

    private void subMenuGestionEmpleados() {
        if (!autorizacionService.validarPermiso(autorizacionService.puedeGestionarEmpleados(), "Gestionar empleados")) return;
        System.out.println("\n--- GESTION DE EMPLEADOS Y SEGURIDAD (GERENCIA) ---");
        System.out.println("  1. Registrar nuevo empleado");
        System.out.println("  2. Listar empleados");
        System.out.println("  3. Asignar o cambiar contraseña a un empleado");
        System.out.println("  4. Volver");

        int op = leerEntero("Opción [1-4]: ", 1, 4);
        switch (op) {
            case 1 -> {
                String nombre = leerTexto("Nombre completo: ");
                String doc = leerTexto("Documento: ");
                String correo = leerTexto("Correo: ");
                BigDecimal sal = leerBigDecimal("Salario: ", BigDecimal.ZERO);
                System.out.println("Roles: 1. ASESOR | 2. SUPERVISOR | 3. GERENTE");
                int rOp = leerEntero("Rol [1-3]: ", 1, 3);
                Rol rol = (rOp == 1) ? Rol.ASESOR : (rOp == 2) ? Rol.SUPERVISOR : Rol.GERENTE;

                Empleado e = new Empleado(nombre, doc, correo, rol, sal);
                if (empleadoDAO.guardar(e)) {
                    System.out.println(">> OK: Empleado guardado con ID: " + e.getId());
                    String asig = leerTexto("¿Desea asignarle contraseña de acceso ahora? (S/N): ");
                    if (asig.equalsIgnoreCase("S")) {
                        String pass = leerTexto("Contraseña: ");
                        autenticacionService.asignarCredencialEmpleado(e.getId(), pass.toCharArray());
                        System.out.println(">> OK: Contraseña asignada con éxito.");
                    }
                }
                pausar();
            }
            case 2 -> {
                List<Empleado> lista = empleadoDAO.listarTodos();
                System.out.printf("%-5s %-25s %-15s %-15s %-15s\n", "ID", "NOMBRE", "DOCUMENTO", "ROL", "SALARIO");
                System.out.println("-------------------------------------------------------------------------------");
                for (Empleado e : lista) {
                    System.out.printf("%-5d %-25s %-15s %-15s $%,12.2f\n",
                        e.getId(), e.getNombre(), e.getDocumento(), e.getRol(), e.getSalario());
                }
                pausar();
            }
            case 3 -> {
                int idEmp = leerEntero("ID del empleado: ", 1, Integer.MAX_VALUE);
                Empleado e = empleadoDAO.buscarPorId(idEmp);
                if (e != null) {
                    String pass = leerTexto("Nueva contraseña para " + e.getNombre() + ": ");
                    if (autenticacionService.asignarCredencialEmpleado(e.getId(), pass.toCharArray())) {
                        System.out.println(">> OK: Credenciales actualizadas exitosamente.");
                    }
                } else {
                    System.out.println(">> Empleado no encontrado.");
                }
                pausar();
            }
        }
    }

    private void subMenuReportesGenerales() {
        if (!autorizacionService.validarPermiso(autorizacionService.puedeConsultarReportesGenerales(), "Consultar reportes generales")) return;
        System.out.println("\n--- REPORTES GENERALES DE CARTERA ---");
        System.out.println("1. Préstamos activos (Filtro con Stream API)");
        System.out.println("2. Consultar cuotas vencidas y clientes morosos");
        System.out.println("3. Volver");
        int op = leerEntero("Opción [1-3]: ", 1, 3);
        switch (op) {
            case 1 -> reportePrestamosActivosStream();
            case 2 -> reporteCuotasVencidas();
        }
    }

    private void subMenuReportesYExportacion() {
        if (!autorizacionService.validarPermiso(autorizacionService.puedeConsultarReportesAdministrativos(), "Consultar reportes administrativos")) return;
        System.out.println("\n--- REPORTES Y EXPORTACION DE GERENCIA ---");
        System.out.println("1. Préstamos activos (Filtro con Stream API)");
        System.out.println("2. Consultar cuotas vencidas y clientes morosos");
        System.out.println("3. Exportar reporte general a archivo de texto (.txt)");
        System.out.println("4. Exportar préstamos a archivo serializado (.dat)");
        System.out.println("5. Volver");
        int op = leerEntero("Opción [1-5]: ", 1, 5);
        switch (op) {
            case 1 -> reportePrestamosActivosStream();
            case 2 -> reporteCuotasVencidas();
            case 3 -> exportarReporteTxt();
            case 4 -> exportarReporteDat();
        }
    }

    private void reportePrestamosActivosStream() {
        System.out.println("\n>> REPORTE: PRESTAMOS ACTIVOS (STREAM API)");
        List<Prestamo> todos = prestamoDAO.listarTodos();

        List<Prestamo> activos = todos.stream()
                .filter(p -> p.getEstado() == EstadoPrestamo.ACTIVO)
                .sorted((p1, p2) -> p2.getMontoCapital().compareTo(p1.getMontoCapital()))
                .collect(Collectors.toList());

        if (activos.isEmpty()) {
            System.out.println("No hay préstamos activos.");
        } else {
            System.out.println("Total préstamos activos: " + activos.size());
            System.out.printf("%-5s %-22s %-14s %-14s %-10s\n", "ID", "CLIENTE", "CAPITAL", "SALDO PEND.", "MODALIDAD");
            System.out.println("-----------------------------------------------------------------------------");
            activos.forEach(p -> System.out.printf("%-5d %-22s $%,12.2f $%,12.2f %-10s\n",
                p.getId(),
                p.getCliente() != null ? p.getCliente().getNombre() : "N/A",
                p.getMontoCapital(),
                p.getSaldoPendiente(),
                p.getModalidad()));
        }
        pausar();
    }

    private void reporteCuotasVencidas() {
        System.out.println("\n>> REPORTE: CUOTAS VENCIDAS Y DEUDORES EN MORA");
        List<Prestamo> todos = prestamoDAO.listarTodos();
        LocalDate hoy = LocalDate.now();

        int totalMorosos = 0;
        for (Prestamo p : todos) {
            Prestamo completo = prestamoDAO.buscarPorId(p.getId());
            List<Cuota> vencidas = completo.getCuotas().stream()
                    .filter(c -> c.esVencida(hoy))
                    .collect(Collectors.toList());

            if (!vencidas.isEmpty()) {
                totalMorosos++;
                System.out.printf("\n[MORA] Préstamo #%d | Cliente: %s | Tel: %s\n",
                    completo.getId(), completo.getCliente().getNombre(), completo.getCliente().getTelefono());
                for (Cuota c : vencidas) {
                    System.out.printf("   -> Cuota #%d vencida el %s | Saldo cuota: $%,.2f\n",
                        c.getNumeroCuota(), c.getFechaVencimiento(), c.getSaldoPendienteCuota());
                }
            }
        }
        if (totalMorosos == 0) {
            System.out.println(">> No existen clientes con cuotas vencidas a la fecha (" + hoy + ").");
        }
        pausar();
    }

    private void exportarReporteTxt() {
        System.out.println("\n>> EXPORTAR REPORTE A ARCHIVO .TXT");
        List<Prestamo> todos = prestamoDAO.listarTodos();
        File archivo = new File("reporte_prestamos.txt");

        try (PrintWriter writer = new PrintWriter(new FileWriter(archivo))) {
            writer.println("================================================================================");
            writer.println("                 CREDIYA S.A.S. - REPORTE OFICIAL DE PRESTAMOS                  ");
            writer.println("Fecha de emisión: " + LocalDate.now());
            writer.println("================================================================================");
            writer.printf("%-5s %-25s %-15s %-15s %-10s %-10s\n", "ID", "CLIENTE", "CAPITAL", "SALDO PEND.", "MODALIDAD", "ESTADO");
            writer.println("--------------------------------------------------------------------------------");

            for (Prestamo p : todos) {
                writer.printf("%-5d %-25s $%,12.2f $%,12.2f %-10s %-10s\n",
                    p.getId(),
                    p.getCliente() != null ? p.getCliente().getNombre() : "N/A",
                    p.getMontoCapital(),
                    p.getSaldoPendiente(),
                    p.getModalidad(),
                    p.getEstado());
            }

            writer.println("================================================================================");
            writer.println("Total registros: " + todos.size());
            System.out.println(">> OK: Reporte generado en: " + archivo.getAbsolutePath());
        } catch (IOException e) {
            System.out.println(">> Error al escribir archivo .txt: " + e.getMessage());
        }
        pausar();
    }

    private void exportarReporteDat() {
        System.out.println("\n>> EXPORTAR PRESTAMOS A ARCHIVO SERIALIZADO (.DAT)");
        List<Prestamo> todos = prestamoDAO.listarTodos();
        File archivo = new File("reporte_prestamos.dat");

        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(archivo))) {
            oos.writeObject(todos);
            System.out.println(">> OK: Archivo binario serializado creado en: " + archivo.getAbsolutePath());
        } catch (IOException e) {
            System.out.println(">> Error al serializar a .dat: " + e.getMessage());
        }
        pausar();
    }

    private int leerEntero(String prompt, int min, int max) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();
            try {
                int val = Integer.parseInt(input);
                if (val >= min && val <= max) return val;
                System.out.printf(">> Ingrese un valor entre %d y %d.\n", min, max);
            } catch (NumberFormatException e) {
                System.out.println(">> Error: Debe ingresar un número entero.");
            }
        }
    }

    private BigDecimal leerBigDecimal(String prompt, BigDecimal min) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim().replace(",", ".");
            try {
                BigDecimal val = new BigDecimal(input).setScale(2, RoundingMode.HALF_UP);
                if (val.compareTo(min) >= 0) return val;
                System.out.printf(">> El monto no puede ser menor a $%,.2f\n", min);
            } catch (NumberFormatException e) {
                System.out.println(">> Error: Debe ingresar un valor numérico decimal válido.");
            }
        }
    }

    private String leerTexto(String prompt) {
        while (true) {
            System.out.print(prompt);
            String texto = scanner.nextLine().trim();
            if (!texto.isEmpty()) return texto;
            System.out.println(">> El campo no puede estar vacío.");
        }
    }

    private void pausar() {
        System.out.print("\nPresione [ENTER] para continuar...");
        scanner.nextLine();
    }
}

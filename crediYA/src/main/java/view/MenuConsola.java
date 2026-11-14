package view;

import controller.ClienteController;
import controller.EmpleadoController;
import controller.PagoController;
import controller.PrestamoController;
import controller.ReporteController;
import controller.SolicitudController;
import exception.AutenticacionException;
import exception.ValidacionException;
import model.Cliente;
import model.Empleado;
import model.Persona;
import model.enums.Rol;
import security.AutenticacionService;
import security.SesionUsuario;
import view.autenticacion.LoginView;
import view.clientes.ClienteView;
import view.comun.EntradaConsola;
import view.comun.MensajesView;
import view.reportes.ReporteView;

public class MenuConsola {

    private final EntradaConsola entrada;
    private final MensajesView mensajes;
    private final LoginView loginView;
    private final ClienteView clienteView;
    private final ReporteView reporteView;
    private final AutenticacionService autenticacionService;
    private final SesionUsuario sesion;

    private final ClienteController clienteController;
    private final EmpleadoController empleadoController;
    private final SolicitudController solicitudController;
    private final PrestamoController prestamoController;
    private final PagoController pagoController;
    private final ReporteController reporteController;

    public MenuConsola(EntradaConsola entrada, MensajesView mensajes, LoginView loginView,
                       ClienteView clienteView, ReporteView reporteView,
                       AutenticacionService autenticacionService, SesionUsuario sesion,
                       ClienteController clienteController, EmpleadoController empleadoController,
                       SolicitudController solicitudController, PrestamoController prestamoController,
                       PagoController pagoController, ReporteController reporteController) {
        this.entrada = entrada;
        this.mensajes = mensajes;
        this.loginView = loginView;
        this.clienteView = clienteView;
        this.reporteView = reporteView;
        this.autenticacionService = autenticacionService;
        this.sesion = sesion;
        this.clienteController = clienteController;
        this.empleadoController = empleadoController;
        this.solicitudController = solicitudController;
        this.prestamoController = prestamoController;
        this.pagoController = pagoController;
        this.reporteController = reporteController;
    }

    public void iniciarMenuPrincipal() {
        int op;
        do {
            System.out.println("\n====================================");
            System.out.println("          CREDIYA S.A.S.            ");
            System.out.println("====================================");
            System.out.println("1. Iniciar sesion");
            System.out.println("2. Reportes");
            System.out.println("3. examen");
            System.out.println("0. Salir");
            System.out.println("====================================");

            op = entrada.leerEntero("Seleccione una opcion: ", 0, 3);
            switch (op) {
                case 1 -> procesarInicioSesion();
                case 2 -> procesarAccesoReportes();
                case 3 -> reporteExamen();
                case 0 -> mensajes.informativo("Gracias por utilizar CrediYA S.A.S. Hasta pronto!");
            }
        } while (op != 0);
    }

    private void procesarInicioSesion() {
        String[] creds = loginView.pedirCredenciales("INICIO DE SESION");
        try {
            Persona p = autenticacionService.autenticar(creds[0], creds[1].toCharArray());
            if (p instanceof Empleado emp) {
                sesion.iniciarSesion(emp);
                mensajes.exito("Bienvenido/a " + emp.getNombre() + " (" + emp.getRol() + ")");
                portalFuncionario(emp);
                sesion.cerrarSesion();
            } else if (p instanceof Cliente cli) {
                sesion.iniciarSesionCliente(cli);
                mensajes.exito("Bienvenido/a " + cli.getNombre() + " (CLIENTE)");
                portalCliente(cli);
                sesion.cerrarSesion();
            }
        } catch (AutenticacionException | ValidacionException e) {
            mensajes.error(e.getMessage());
            entrada.pausar();
        }
    }

    private void procesarAccesoReportes() {
        String[] creds = loginView.pedirCredenciales("AUTENTICACION PARA REPORTES");
        try {
            Persona p = autenticacionService.autenticar(creds[0], creds[1].toCharArray());
            if (p instanceof Empleado emp) {
                sesion.iniciarSesion(emp);
                mensajes.exito("Acceso autorizado para " + emp.getNombre() + " (" + emp.getRol() + ")");
                menuReportesFuncionario(emp);
                sesion.cerrarSesion();
            } else if (p instanceof Cliente cli) {
                sesion.iniciarSesionCliente(cli);
                mensajes.exito("Acceso autorizado para " + cli.getNombre() + " (CLIENTE)");
                menuReportesCliente(cli);
                sesion.cerrarSesion();
            }
        } catch (AutenticacionException | ValidacionException e) {
            mensajes.error(e.getMessage());
            entrada.pausar();
        }
    }

    private void portalCliente(Cliente cli) {
        int op;
        do {
            System.out.println("\n====================================");
            System.out.println("          PORTAL DEL CLIENTE        ");
            System.out.println("====================================");
            System.out.println("1. Mi informacion personal");
            System.out.println("2. Mis prestamos");
            System.out.println("3. Mis cuotas");
            System.out.println("4. Reportes");
            System.out.println("0. Cerrar sesion");
            System.out.println("====================================");

            op = entrada.leerEntero("Seleccione una opcion [0-4]: ", 0, 4);
            switch (op) {
                case 1 -> {
                    clienteView.mostrarDetalleCliente(cli);
                    entrada.pausar();
                }
                case 2 -> reporteController.reporteCreditosPropiosCliente(cli);
                case 3 -> reporteController.reporteCuotasVencidasPropiasCliente(cli);
                case 4 -> menuReportesCliente(cli);
                case 0 -> mensajes.informativo("Sesion de cliente cerrada correctamente.");
            }
        } while (op != 0);
    }

    private void menuReportesCliente(Cliente cli) {
        int op;
        do {
            System.out.println("\n========== REPORTES DEL CLIENTE ==========");
            System.out.println("1. Creditos activos propios");
            System.out.println("2. Cuotas vencidas propias");
            System.out.println("3. Tabla de amortizacion de un credito");
            System.out.println("0. Volver");
            System.out.println("==========================================");

            op = entrada.leerEntero("Seleccione una opcion [0-3]: ", 0, 3);
            switch (op) {
                case 1 -> reporteController.reporteCreditosPropiosCliente(cli);
                case 2 -> reporteController.reporteCuotasVencidasPropiasCliente(cli);
                case 3 -> reporteController.reporteTablaAmortizacionPropiaCliente(cli);
            }
        } while (op != 0);
    }

    private void portalFuncionario(Empleado emp) {
        int op;
        do {
            System.out.println("\n====================================");
            System.out.printf("         MENU DE FUNCIONARIO\n");
            System.out.printf("  Usuario: %s | Rol: %s\n", emp.getNombre(), emp.getRol());
            System.out.println("====================================");
            System.out.println("1. Clientes");
            System.out.println("2. Personal");
            System.out.println("3. Solicitudes");
            System.out.println("4. Prestamos");
            System.out.println("5. Pagos");
            System.out.println("6. Reportes");
            System.out.println("0. Cerrar sesion");
            System.out.println("====================================");

            op = entrada.leerEntero("Seleccione una opcion [0-6]: ", 0, 6);
            switch (op) {
                case 1 -> subMenuClientes();
                case 2 -> subMenuPersonal(emp);
                case 3 -> subMenuSolicitudes(emp);
                case 4 -> subMenuPrestamos(emp);
                case 5 -> pagoController.gestionarMenuPagos();
                case 6 -> menuReportesFuncionario(emp);
                case 0 -> mensajes.informativo("Sesion de funcionario finalizada correctamente.");
            }
        } while (op != 0);
    }

    private void subMenuClientes() {
        int op;
        do {
            mensajes.encabezado("GESTION DE CLIENTES");
            System.out.println("  1. Registrar nuevo cliente");
            System.out.println("  2. Consultar cliente por ID o documento");
            System.out.println("  3. Listar todos los clientes");
            System.out.println("  4. Modificar datos de cliente");
            System.out.println("  5. Eliminar cliente");
            System.out.println("  0. Volver");

            op = entrada.leerEntero("Seleccione opcion [0-5]: ", 0, 5);
            switch (op) {
                case 1 -> clienteController.registrarCliente();
                case 2 -> clienteController.consultarCliente();
                case 3 -> clienteController.listarClientes();
                case 4 -> clienteController.modificarCliente();
                case 5 -> clienteController.eliminarCliente();
            }
        } while (op != 0);
    }

    private void subMenuPersonal(Empleado emp) {
        int op;
        do {
            mensajes.encabezado("GESTION DE PERSONAL Y FUNCIONARIOS");
            System.out.println("  1. Consultar mi informacion personal");
            System.out.println("  2. Modificar mis datos personales");
            if (emp.getRol() == Rol.SUPERVISOR || emp.getRol() == Rol.GERENTE) {
                System.out.println("  3. Consultar informacion de otros empleados");
            }
            if (emp.getRol() == Rol.GERENTE) {
                System.out.println("  4. Registrar nuevo empleado");
                System.out.println("  5. Asignar o cambiar contrasena a un empleado");
            }
            System.out.println("  0. Volver");

            int maxOp = (emp.getRol() == Rol.GERENTE) ? 5 : (emp.getRol() == Rol.SUPERVISOR ? 3 : 2);
            op = entrada.leerEntero("Seleccione opcion [0-" + maxOp + "]: ", 0, maxOp);
            switch (op) {
                case 1 -> empleadoController.consultarPerfilPropio(emp);
                case 2 -> empleadoController.modificarDatosPersonalesPropio(emp);
                case 3 -> {
                    if (emp.getRol() == Rol.SUPERVISOR || emp.getRol() == Rol.GERENTE) {
                        empleadoController.listarEmpleados();
                    }
                }
                case 4 -> {
                    if (emp.getRol() == Rol.GERENTE) {
                        empleadoController.registrarEmpleado();
                    }
                }
                case 5 -> {
                    if (emp.getRol() == Rol.GERENTE) {
                        empleadoController.asignarContrasenaEmpleado();
                    }
                }
            }
        } while (op != 0);
    }

    private void subMenuSolicitudes(Empleado emp) {
        int op;
        boolean esAprobador = (emp.getRol() == Rol.SUPERVISOR || emp.getRol() == Rol.GERENTE);
        do {
            mensajes.encabezado("GESTION DE SOLICITUDES DE CREDITO");
            System.out.println("  1. Radicar nueva solicitud de credito");
            System.out.println("  2. Consultar todas las solicitudes");
            System.out.println("  3. Consultar solicitudes por cliente");
            System.out.println("  4. Consultar detalle de una solicitud");
            System.out.println("  5. Modificar solicitud pendiente");
            System.out.println("  6. Eliminar solicitud pendiente");
            if (esAprobador) {
                System.out.println("  7. APROBAR solicitud (Cadena de Responsabilidad)");
                System.out.println("  8. RECHAZAR solicitud (Cadena de Responsabilidad)");
            }
            System.out.println("  0. Volver");

            int maxOp = esAprobador ? 8 : 6;
            op = entrada.leerEntero("Seleccione opcion [0-" + maxOp + "]: ", 0, maxOp);
            switch (op) {
                case 1 -> solicitudController.radicarSolicitud(emp);
                case 2 -> solicitudController.listarTodas();
                case 3 -> solicitudController.listarPorCliente();
                case 4 -> solicitudController.consultarDetalle();
                case 5 -> solicitudController.modificarSolicitud(emp);
                case 6 -> solicitudController.eliminarSolicitud(emp);
                case 7 -> {
                    if (esAprobador) solicitudController.evaluarSolicitud(emp, true);
                }
                case 8 -> {
                    if (esAprobador) solicitudController.evaluarSolicitud(emp, false);
                }
            }
        } while (op != 0);
    }

    private void subMenuPrestamos(Empleado emp) {
        int op;
        boolean puedeDesembolsar = (emp.getRol() == Rol.SUPERVISOR || emp.getRol() == Rol.GERENTE);
        do {
            mensajes.encabezado("GESTION DE CONTRATOS DE PRESTAMO");
            System.out.println("  1. Consultar prestamos de un cliente");
            System.out.println("  2. Consultar prestamos generales");
            System.out.println("  3. Consultar detalle de un prestamo");
            System.out.println("  4. Consultar tabla de amortizacion");
            if (puedeDesembolsar) {
                System.out.println("  5. Desembolsar prestamo desde solicitud APROBADA");
            }
            System.out.println("  0. Volver");

            int maxOp = puedeDesembolsar ? 5 : 4;
            op = entrada.leerEntero("Seleccione opcion [0-" + maxOp + "]: ", 0, maxOp);
            switch (op) {
                case 1 -> prestamoController.consultarPrestamosCliente();
                case 2 -> prestamoController.consultarPrestamosGenerales();
                case 3 -> prestamoController.consultarDetallePrestamo();
                case 4 -> prestamoController.consultarTablaAmortizacion();
                case 5 -> {
                    if (puedeDesembolsar) prestamoController.desembolsarPrestamo(emp);
                }
            }
        } while (op != 0);
    }

    private void menuReportesFuncionario(Empleado emp) {
        int op;
        do {
            System.out.println("\n========== REPORTES DEL FUNCIONARIO ==========");
            System.out.println("1. Prestamos activos");
            System.out.println("2. Cuotas vencidas");
            System.out.println("3. Clientes morosos");
            System.out.println("4. Detalles de un cliente (Ficha integral)");
            System.out.println("5. Prestamos pagados");
            System.out.println("6. Backup de informacion (.dat)");
            System.out.println("7. Mis reportes como empleado");
            if (emp.getRol() == Rol.GERENTE) {
                System.out.println("8. Indicadores consolidados de cartera (HashMap)");
                System.out.println("9. Exportar reporte general a texto (.txt)");
                System.out.println("10. Importar y consultar backup binario (.dat)");
            }
            System.out.println("0. Volver");
            System.out.println("==============================================");

            int maxOp = (emp.getRol() == Rol.GERENTE) ? 10 : 7;
            op = entrada.leerEntero("Seleccione una opcion [0-" + maxOp + "]: ", 0, maxOp);
            switch (op) {
                case 1 -> reporteController.reportePrestamosActivos();
                case 2 -> reporteController.reporteCuotasVencidas();
                case 3 -> reporteController.reporteClientesMorosos();
                case 4 -> reporteController.reporteDetallesCliente();
                case 5 -> reporteController.reportePrestamosPagados();
                case 6 -> reporteController.generarBackupDat();
                case 7 -> subMenuMisReportesEmpleado(emp);
                case 8 -> {
                    if (emp.getRol() == Rol.GERENTE) reporteController.reporteIndicadoresCartera();
                }
                case 9 -> {
                    if (emp.getRol() == Rol.GERENTE) reporteController.exportarReporteTxt();
                }
                case 10 -> {
                    if (emp.getRol() == Rol.GERENTE) reporteController.importarBackupDat();
                }
            }
        } while (op != 0);
    }

    private void subMenuMisReportesEmpleado(Empleado emp) {
        int op;
        do {
            mensajes.encabezado("MIS REPORTES COMO FUNCIONARIO");
            System.out.println("  1. Reporte de ingresos anuales proyectados");
            System.out.println("  2. Clientes atendidos con creditos asociados a mi gestion");
            System.out.println("  0. Volver");

            op = entrada.leerEntero("Seleccione opcion [0-2]: ", 0, 2);
            switch (op) {
                case 1 -> reporteController.reporteIngresosEmpleado(emp);
                case 2 -> reporteController.reporteClientesAtendidosPorEmpleado(emp);
            }
        } while (op != 0);
    }
    
    private void reporteExamen() {
        int op;
        do {
            System.out.println("\n========== REPORTES DEL FUNCIONARIO ==========");
            System.out.println("1. Prestamos activos");
            System.out.println("2. Prestamos vencidos");
            System.out.println("3. Mostrar Resumen");
            System.out.println("0. Volver");
            System.out.println("==============================================");

            
            op = entrada.leerEntero("Seleccione una opcion [0-3]",0,3);
            switch (op) {
                case 1 -> reporteController.reportePrestamosActivos();
                case 2 -> reporteController.reporteCuotasVencidasExamen();
                case 3 -> reporteController.reporteClientesMorosos();
                case 4 -> reporteController.reporteDetallesCliente();
                case 5 -> reporteController.reportePrestamosPagados();
                case 6 -> reporteController.generarBackupDat();
            }
        } while (op != 0);
    }

    
    
}

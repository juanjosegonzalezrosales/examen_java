package controller;

import dao.ClienteDAO;
import dao.CredencialDAO;
import dao.EmpleadoDAO;
import dao.PagoDAO;
import dao.PrestamoDAO;
import dao.SolicitudDAO;
import database.Conexion;
import exception.CrediYaException;
import exception.ReglaNegocioException;
import exception.ValidacionException;
import java.math.BigDecimal;
import model.Empleado;
import model.enums.Rol;
import security.AutenticacionService;
import security.AutorizacionService;
import security.SesionUsuario;
import utils.ReporteArchivoService;
import view.MenuConsola;
import view.autenticacion.LoginView;
import view.clientes.ClienteView;
import view.comun.EntradaConsola;
import view.comun.MensajesView;
import view.empleados.EmpleadoView;
import view.pagos.PagoView;
import view.prestamos.PrestamoView;
import view.reportes.ReporteView;
import view.solicitudes.SolicitudView;

public class CrediYA {

    public void iniciar() {
        System.out.println("==================================================");
        System.out.println("        SISTEMA DE GESTION CREDIYA S.A.S.         ");
        System.out.println("   Plataforma de Creditos Personales y Cobranza   ");
        System.out.println("==================================================");
        System.out.println("Iniciando componentes y verificando conexion...");
        System.out.println();

        Conexion conexion = Conexion.getInstancia();
        boolean conexionExitosa = conexion.probarConexion();

        System.out.println();
        if (conexionExitosa) {
            System.out.println(">> [ESTADO]: Conexion verificada. Cargando modulos funcionales...");

            EntradaConsola entrada = new EntradaConsola();
            MensajesView mensajes = new MensajesView();

            LoginView loginView = new LoginView(entrada, mensajes);
            ClienteView clienteView = new ClienteView(entrada, mensajes);
            EmpleadoView empleadoView = new EmpleadoView(entrada, mensajes);
            SolicitudView solicitudView = new SolicitudView(entrada, mensajes);
            PrestamoView prestamoView = new PrestamoView(entrada, mensajes);
            PagoView pagoView = new PagoView(entrada, mensajes);
            ReporteView reporteView = new ReporteView(entrada, mensajes);

            ClienteDAO clienteDAO = new ClienteDAO();
            EmpleadoDAO empleadoDAO = new EmpleadoDAO();
            SolicitudDAO solicitudDAO = new SolicitudDAO();
            PrestamoDAO prestamoDAO = new PrestamoDAO();
            PagoDAO pagoDAO = new PagoDAO();
            CredencialDAO credencialDAO = new CredencialDAO();

            AutenticacionService autenticacionService = new AutenticacionService();
            AutorizacionService autorizacionService = new AutorizacionService();
            SesionUsuario sesion = SesionUsuario.getInstancia();
            ReporteArchivoService reporteArchivoService = new ReporteArchivoService();

            if (!autenticacionService.tieneCredencialesConfiguradas()) {
                configurarPrimerGerente(entrada, mensajes, empleadoDAO, autenticacionService);
            }

            ClienteController clienteController = new ClienteController(
                clienteDAO, solicitudDAO, prestamoDAO, credencialDAO,
                autenticacionService, autorizacionService, clienteView, entrada, mensajes
            );

            EmpleadoController empleadoController = new EmpleadoController(
                empleadoDAO, autenticacionService, autorizacionService, empleadoView, entrada, mensajes
            );

            SolicitudController solicitudController = new SolicitudController(
                solicitudDAO, clienteDAO, prestamoDAO, autorizacionService, solicitudView, entrada, mensajes
            );

            PrestamoController prestamoController = new PrestamoController(
                prestamoDAO, solicitudDAO, clienteDAO, autorizacionService, prestamoView, entrada, mensajes
            );

            PagoController pagoController = new PagoController(
                pagoDAO, prestamoDAO, autorizacionService, pagoView, entrada, mensajes
            );

            ReporteController reporteController = new ReporteController(
                prestamoDAO, solicitudDAO, clienteDAO, pagoDAO, autorizacionService,
                reporteArchivoService, reporteView, entrada, mensajes
            );

            MenuConsola menu = new MenuConsola(
                entrada, mensajes, loginView, clienteView, reporteView,
                autenticacionService, sesion, clienteController, empleadoController,
                solicitudController, prestamoController, pagoController, reporteController
            );

            menu.iniciarMenuPrincipal();
        } else {
            System.out.println(">> [AVISO]: La aplicacion no pudo conectar a MySQL.");
            System.out.println("   Verifique que el servicio MySQL este activo y el archivo 'db.properties'");
            System.out.println("   contenga las credenciales correctas.");
        }
    }

    private void configurarPrimerGerente(EntradaConsola entrada, MensajesView mensajes,
                                        EmpleadoDAO empleadoDAO, AutenticacionService autenticacionService) {
        mensajes.encabezado("CONFIGURACION INICIAL DE ACCESO: PRIMER GERENTE");
        System.out.println("No se detectaron credenciales en la base de datos.");
        System.out.println("Configure las credenciales de un empleado con rol GERENTE.");
        try {
            String doc = entrada.leerTexto("Documento del empleado GERENTE: ");
            Empleado emp = empleadoDAO.buscarPorDocumento(doc);

            if (emp == null) {
                System.out.println(">> No existe ningun empleado con ese documento.");
                String reg = entrada.leerTexto("Desea registrar al primer empleado GERENTE ahora? (S/N): ");
                if (reg.equalsIgnoreCase("S")) {
                    String nombre = entrada.leerTexto("Nombre completo del Gerente: ");
                    String correo = entrada.leerTexto("Correo electronico: ");
                    BigDecimal salario = entrada.leerBigDecimal("Salario: ", BigDecimal.ZERO);
                    emp = new Empleado(nombre, doc, correo, Rol.GERENTE, salario);
                    emp.validar();
                    if (empleadoDAO.guardar(emp)) {
                        mensajes.exito("Empleado GERENTE creado con ID: " + emp.getId());
                    } else {
                        mensajes.error("Error al crear empleado GERENTE.");
                        entrada.pausar();
                        return;
                    }
                } else {
                    entrada.pausar();
                    return;
                }
            }

            if (emp.getRol() != Rol.GERENTE) {
                throw new ReglaNegocioException("El empleado con documento " + doc + " no es GERENTE (Rol: " + emp.getRol() + ")");
            }

            String pass1 = entrada.leerTexto("Nueva contrasena de acceso: ");
            String pass2 = entrada.leerTexto("Confirmar contrasena: ");
            if (!pass1.equals(pass2)) {
                throw new ValidacionException("Las contrasenas no coinciden.");
            }

            if (autenticacionService.registrarCredencialGerente(doc, pass1.toCharArray())) {
                mensajes.exito("Credenciales del primer GERENTE configuradas exitosamente.");
            } else {
                mensajes.error("No se pudieron registrar las credenciales.");
            }
        } catch (CrediYaException e) {
            mensajes.error(e.getMessage());
        }
        entrada.pausar();
    }
}

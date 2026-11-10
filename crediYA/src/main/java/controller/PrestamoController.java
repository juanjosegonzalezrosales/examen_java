package controller;

import dao.ClienteDAO;
import dao.PrestamoDAO;
import dao.SolicitudDAO;
import exception.EntidadNoEncontradaException;
import exception.ReglaNegocioException;
import exception.ValidacionException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import model.Cliente;
import model.Empleado;
import model.Prestamo;
import model.Solicitud;
import model.enums.EstadoSolicitud;
import security.AutorizacionService;
import view.comun.EntradaConsola;
import view.comun.MensajesView;
import view.prestamos.PrestamoView;

public class PrestamoController {

    private final PrestamoDAO prestamoDAO;
    private final SolicitudDAO solicitudDAO;
    private final ClienteDAO clienteDAO;
    private final AutorizacionService autorizacionService;
    private final PrestamoView prestamoView;
    private final EntradaConsola entrada;
    private final MensajesView mensajes;

    public PrestamoController(PrestamoDAO prestamoDAO, SolicitudDAO solicitudDAO, ClienteDAO clienteDAO,
                              AutorizacionService autorizacionService, PrestamoView prestamoView,
                              EntradaConsola entrada, MensajesView mensajes) {
        this.prestamoDAO = prestamoDAO;
        this.solicitudDAO = solicitudDAO;
        this.clienteDAO = clienteDAO;
        this.autorizacionService = autorizacionService;
        this.prestamoView = prestamoView;
        this.entrada = entrada;
        this.mensajes = mensajes;
    }

    public void desembolsarPrestamo(Empleado empActual) {
        if (!autorizacionService.validarPermiso(autorizacionService.puedeDesembolsarPrestamos(), "Desembolsar prestamos")) return;
        mensajes.encabezado("CREAR Y DESEMBOLSAR PRESTAMO DESDE SOLICITUD APROBADA");
        try {
            int idSol = entrada.leerEntero("Numero de Solicitud APROBADA a desembolsar: ", 1, Integer.MAX_VALUE);
            Solicitud sol = solicitudDAO.buscarPorId(idSol);
            if (sol == null) throw new EntidadNoEncontradaException("Solicitud #" + idSol + " no encontrada.");

            if (sol.getEstado() == EstadoSolicitud.DESEMBOLSADO || prestamoDAO.buscarPorSolicitudId(sol.getId()) != null) {
                throw new ReglaNegocioException("La solicitud #" + sol.getId() + " ya origino un prestamo desembolsado.");
            }

            if (sol.getEstado() != EstadoSolicitud.APROBADA) {
                throw new ReglaNegocioException("No se puede desembolsar: La solicitud esta en estado " + sol.getEstado() + " (debe estar APROBADA).");
            }

            BigDecimal tasaAnual = prestamoView.pedirTasaDesembolso();

            Prestamo p = new Prestamo();
            p.setSolicitud(sol);
            p.setMontoCapital(sol.getMontoSolicitado());
            p.setTasaInteresAnual(tasaAnual);
            p.setPlazoCuotas(sol.getPlazoMeses());
            p.setModalidad(sol.getModalidad());
            p.setFechaInicio(LocalDate.now());

            if (prestamoDAO.crearPrestamo(p, empActual)) {
                mensajes.exito("Prestamo #" + p.getId() + " desembolsado exitosamente y calendario de cuotas generado.");
                prestamoView.mostrarDetallePrestamo(p);
            } else {
                mensajes.error("No se pudo desembolsar el prestamo.");
            }
        } catch (EntidadNoEncontradaException | ReglaNegocioException | ValidacionException e) {
            mensajes.error(e.getMessage());
        }
        entrada.pausar();
    }

    public void consultarPrestamosGenerales() {
        if (!autorizacionService.validarPermiso(autorizacionService.puedeConsultarTodosPrestamos(), "Consultar todos los prestamos")) return;
        List<Prestamo> lista = prestamoDAO.listarTodos();
        prestamoView.mostrarTablaPrestamos(lista);
        entrada.pausar();
    }

    public void consultarPrestamosCliente() {
        if (!autorizacionService.validarPermiso(autorizacionService.puedeConsultarClientes(), "Consultar prestamos por cliente")) return;
        try {
            int idCli = entrada.leerEntero("ID del Cliente titular: ", 1, Integer.MAX_VALUE);
            Cliente cli = clienteDAO.buscarPorId(idCli);
            if (cli == null) throw new EntidadNoEncontradaException("Cliente con ID " + idCli + " no encontrado.");

            List<Prestamo> lista = prestamoDAO.listarPorCliente(idCli);
            prestamoView.mostrarTablaPrestamos(lista);
        } catch (EntidadNoEncontradaException e) {
            mensajes.error(e.getMessage());
        }
        entrada.pausar();
    }

    public void consultarTablaAmortizacion() {
        try {
            int idP = entrada.leerEntero("ID del Prestamo a consultar: ", 1, Integer.MAX_VALUE);
            Prestamo p = prestamoDAO.buscarPorId(idP);
            if (p == null) throw new EntidadNoEncontradaException("Prestamo con ID " + idP + " no encontrado.");
            prestamoView.mostrarTablaAmortizacion(p);
        } catch (EntidadNoEncontradaException e) {
            mensajes.error(e.getMessage());
        }
        entrada.pausar();
    }

    public void consultarDetallePrestamo() {
        try {
            int idP = entrada.leerEntero("ID del Prestamo a consultar: ", 1, Integer.MAX_VALUE);
            Prestamo p = prestamoDAO.buscarPorId(idP);
            if (p == null) throw new EntidadNoEncontradaException("Prestamo con ID " + idP + " no encontrado.");
            prestamoView.mostrarDetallePrestamo(p);
        } catch (EntidadNoEncontradaException e) {
            mensajes.error(e.getMessage());
        }
        entrada.pausar();
    }
}


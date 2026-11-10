package controller;

import dao.ClienteDAO;
import dao.PagoDAO;
import dao.PrestamoDAO;
import dao.SolicitudDAO;
import exception.EntidadNoEncontradaException;
import java.io.File;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import model.Cliente;
import model.Cuota;
import model.Empleado;
import model.Pago;
import model.Prestamo;
import model.Solicitud;
import model.enums.EstadoPrestamo;
import model.enums.EstadoSolicitud;
import model.enums.TipoPago;
import security.AutorizacionService;
import utils.ReporteArchivoService;
import view.comun.EntradaConsola;
import view.comun.MensajesView;
import view.reportes.ReporteView;

public class ReporteController {

    private final PrestamoDAO prestamoDAO;
    private final SolicitudDAO solicitudDAO;
    private final ClienteDAO clienteDAO;
    private final PagoDAO pagoDAO;
    private final AutorizacionService autorizacionService;
    private final ReporteArchivoService reporteArchivoService;
    private final ReporteView reporteView;
    private final EntradaConsola entrada;
    private final MensajesView mensajes;

    public ReporteController(PrestamoDAO prestamoDAO, SolicitudDAO solicitudDAO, ClienteDAO clienteDAO,
                             PagoDAO pagoDAO, AutorizacionService autorizacionService,
                             ReporteArchivoService reporteArchivoService, ReporteView reporteView,
                             EntradaConsola entrada, MensajesView mensajes) {
        this.prestamoDAO = prestamoDAO;
        this.solicitudDAO = solicitudDAO;
        this.clienteDAO = clienteDAO;
        this.pagoDAO = pagoDAO;
        this.autorizacionService = autorizacionService;
        this.reporteArchivoService = reporteArchivoService;
        this.reporteView = reporteView;
        this.entrada = entrada;
        this.mensajes = mensajes;
    }

    public void reportePrestamosActivos() {
        List<Prestamo> activos = prestamoDAO.listarTodos().stream()
                .filter(p -> p.getEstado() == EstadoPrestamo.ACTIVO)
                .sorted((p1, p2) -> p2.getMontoCapital().compareTo(p1.getMontoCapital()))
                .toList();
        reporteView.mostrarPrestamosActivos(activos);
        entrada.pausar();
    }

    public void reporteCuotasVencidas() {
        List<Prestamo> todos = prestamoDAO.listarTodos();
        LocalDate hoy = LocalDate.now();
        List<Prestamo> morosos = new ArrayList<>();
        for (Prestamo p : todos) {
            Prestamo comp = prestamoDAO.buscarPorId(p.getId());
            if (comp != null && comp.getCuotas().stream().anyMatch(c -> c.esVencida(hoy))) {
                morosos.add(comp);
            }
        }
        reporteView.mostrarCuotasVencidasYMorosos(morosos, hoy);
        entrada.pausar();
    }

    public void reporteClientesMorosos() {
        List<Prestamo> todos = prestamoDAO.listarTodos();
        LocalDate hoy = LocalDate.now();
        List<Prestamo> morosos = new ArrayList<>();
        for (Prestamo p : todos) {
            Prestamo comp = prestamoDAO.buscarPorId(p.getId());
            if (comp != null && comp.getCuotas().stream().anyMatch(c -> c.esVencida(hoy))) {
                morosos.add(comp);
            }
        }
        reporteView.mostrarClientesMorosos(morosos);
        entrada.pausar();
    }

    public void reportePrestamosPagados() {
        List<Prestamo> pagados = prestamoDAO.listarTodos().stream()
                .filter(p -> p.getEstado() == EstadoPrestamo.PAGADO)
                .toList();
        reporteView.mostrarPrestamosPagados(pagados);
        entrada.pausar();
    }

    public void reporteDetallesCliente() {
        try {
            System.out.println("Buscar cliente: 1. Por documento | 2. Por ID");
            int crit = entrada.leerEntero("Seleccione opcion [1-2]: ", 1, 2);
            Cliente cli;
            if (crit == 1) {
                String doc = entrada.leerTexto("Documento de identidad: ");
                cli = clienteDAO.buscarPorDocumento(doc);
                if (cli == null) throw new EntidadNoEncontradaException("Cliente con documento " + doc + " no encontrado.");
            } else {
                int id = entrada.leerEntero("ID del cliente: ", 1, Integer.MAX_VALUE);
                cli = clienteDAO.buscarPorId(id);
                if (cli == null) throw new EntidadNoEncontradaException("Cliente con ID " + id + " no encontrado.");
            }

            int cliId = cli.getId();
            List<Solicitud> sols = solicitudDAO.listarTodos().stream()
                    .filter(s -> s.getCliente() != null && cliId == s.getCliente().getId())
                    .toList();

            List<Prestamo> prestamos = prestamoDAO.listarPorCliente(cliId);
            List<Prestamo> completos = new ArrayList<>();
            Map<Integer, List<Pago>> pagosMap = new HashMap<>();

            for (Prestamo p : prestamos) {
                Prestamo c = prestamoDAO.buscarPorId(p.getId());
                completos.add(c != null ? c : p);
                pagosMap.put(p.getId(), pagoDAO.listarPorPrestamo(p.getId()));
            }

            reporteView.mostrarFichaIntegralCliente(cli, sols, completos, pagosMap);
        } catch (EntidadNoEncontradaException e) {
            mensajes.error(e.getMessage());
        }
        entrada.pausar();
    }

    public void reporteIndicadoresCartera() {
        List<Prestamo> prestamos = prestamoDAO.listarTodos();
        List<Solicitud> solicitudes = solicitudDAO.listarTodos();
        List<Pago> pagos = pagoDAO.listarTodos();

        Map<String, String> indicadores = new HashMap<>();

        long totalPrestamos = prestamos.stream().count();
        long activos = prestamos.stream().filter(p -> p.getEstado() == EstadoPrestamo.ACTIVO).count();
        long pagados = prestamos.stream().filter(p -> p.getEstado() == EstadoPrestamo.PAGADO).count();

        long solPendientes = solicitudes.stream().filter(s -> s.getEstado() == EstadoSolicitud.PENDIENTE).count();
        long solAprobadas = solicitudes.stream().filter(s -> s.getEstado() == EstadoSolicitud.APROBADA).count();
        long solRechazadas = solicitudes.stream().filter(s -> s.getEstado() == EstadoSolicitud.RECHAZADA).count();
        long solDesembolsadas = solicitudes.stream().filter(s -> s.getEstado() == EstadoSolicitud.DESEMBOLSADO).count();

        long pagosCuota = pagos.stream().filter(p -> p.getTipoPago() == TipoPago.CUOTA_PROGRAMADA).count();
        long pagosAbono = pagos.stream().filter(p -> p.getTipoPago() == TipoPago.ABONO_EXTRAORDINARIO).count();
        long pagosTotales = pagos.stream().filter(p -> p.getTipoPago() == TipoPago.PAGO_TOTAL).count();

        indicadores.put("1. Total contratos de prestamo", String.valueOf(totalPrestamos));
        indicadores.put("2. Prestamos activos", String.valueOf(activos));
        indicadores.put("3. Prestamos cancelados/pagados", String.valueOf(pagados));
        indicadores.put("4. Solicitudes PENDIENTE", String.valueOf(solPendientes));
        indicadores.put("5. Solicitudes APROBADA", String.valueOf(solAprobadas));
        indicadores.put("6. Solicitudes RECHAZADA", String.valueOf(solRechazadas));
        indicadores.put("7. Solicitudes DESEMBOLSADO", String.valueOf(solDesembolsadas));
        indicadores.put("8. Pagos de cuota programada", String.valueOf(pagosCuota));
        indicadores.put("9. Pagos de abono extraordinario", String.valueOf(pagosAbono));
        indicadores.put("10. Pagos de cancelacion total", String.valueOf(pagosTotales));

        reporteView.mostrarIndicadoresCartera(indicadores);
        entrada.pausar();
    }

    public void generarBackupDat() {
        List<Prestamo> todos = prestamoDAO.listarTodos();
        File archivo = new File("reporte_prestamos.dat");
        if (reporteArchivoService.exportarBinario(todos, archivo)) {
            mensajes.exito("Backup binario (.dat) generado exitosamente en: " + archivo.getAbsolutePath());
        } else {
            mensajes.error("No se pudo generar el archivo de backup .dat");
        }
        entrada.pausar();
    }

    public void importarBackupDat() {
        File archivo = new File("reporte_prestamos.dat");
        if (!archivo.exists()) {
            mensajes.error("No existe el archivo de backup 'reporte_prestamos.dat' en la raiz del proyecto.");
            entrada.pausar();
            return;
        }
        List<Prestamo> recuperados = reporteArchivoService.importarBinario(archivo);
        reporteView.mostrarPrestamosDeserializados(recuperados);
        entrada.pausar();
    }

    public void exportarReporteTxt() {
        List<Prestamo> todos = prestamoDAO.listarTodos();
        File archivo = new File("reporte_prestamos.txt");
        if (reporteArchivoService.exportarTexto(todos, archivo)) {
            mensajes.exito("Reporte exportado a texto plano (.txt) exitosamente: " + archivo.getAbsolutePath());
        } else {
            mensajes.error("No se pudo generar el archivo .txt");
        }
        entrada.pausar();
    }

    public void reporteIngresosEmpleado(Empleado emp) {
        if (emp == null) return;
        BigDecimal ingresosAnuales = emp.getSalario().multiply(new BigDecimal("12"));
        List<Prestamo> asignados = prestamoDAO.listarPorEmpleado(emp.getId());
        List<Integer> cliIds = asignados.stream()
                .filter(p -> p.getCliente() != null)
                .map(p -> p.getCliente().getId())
                .distinct()
                .toList();

        List<Cliente> clientesAtendidos = new ArrayList<>();
        for (Integer cid : cliIds) {
            Cliente c = clienteDAO.buscarPorId(cid);
            if (c != null) clientesAtendidos.add(c);
        }
        reporteView.mostrarReporteEmpleado(emp, ingresosAnuales, clientesAtendidos);
        entrada.pausar();
    }

    public void reporteClientesAtendidosPorEmpleado(Empleado emp) {
        if (emp == null) return;
        List<Prestamo> asignados = prestamoDAO.listarPorEmpleado(emp.getId());
        List<Integer> cliIds = asignados.stream()
                .filter(p -> p.getCliente() != null)
                .map(p -> p.getCliente().getId())
                .distinct()
                .toList();

        List<Cliente> clientesAtendidos = new ArrayList<>();
        for (Integer cid : cliIds) {
            Cliente c = clienteDAO.buscarPorId(cid);
            if (c != null) clientesAtendidos.add(c);
        }
        reporteView.mostrarReporteEmpleado(emp, emp.getSalario().multiply(new BigDecimal("12")), clientesAtendidos);
        entrada.pausar();
    }

    public void reporteCreditosPropiosCliente(Cliente c) {
        if (c == null) return;
        List<Prestamo> prestamos = prestamoDAO.listarPorCliente(c.getId());
        reporteView.mostrarCreditosPropiosCliente(prestamos);
        entrada.pausar();
    }

    public void reporteCuotasVencidasPropiasCliente(Cliente c) {
        if (c == null) return;
        List<Prestamo> prestamos = prestamoDAO.listarPorCliente(c.getId());
        LocalDate hoy = LocalDate.now();
        List<Prestamo> prestamosConCuotas = new ArrayList<>();
        for (Prestamo p : prestamos) {
            Prestamo completo = prestamoDAO.buscarPorId(p.getId());
            if (completo != null) {
                List<Cuota> vencidas = completo.getCuotas().stream().filter(cu -> cu.esVencida(hoy)).toList();
                if (!vencidas.isEmpty()) {
                    completo.setCuotas(vencidas);
                    prestamosConCuotas.add(completo);
                }
            }
        }
        if (prestamosConCuotas.isEmpty()) {
            mensajes.exito("Felicitaciones! No registra cuotas vencidas a la fecha (" + hoy + ").");
        } else {
            reporteView.mostrarCuotasPropiasCliente(prestamosConCuotas, hoy);
        }
        entrada.pausar();
    }

    public void reporteTablaAmortizacionPropiaCliente(Cliente c) {
        if (c == null) return;
        List<Prestamo> prestamos = prestamoDAO.listarPorCliente(c.getId());
        if (prestamos.isEmpty()) {
            mensajes.informativo("No registra creditos para consultar plan de amortizacion.");
            entrada.pausar();
            return;
        }

        reporteView.mostrarCreditosPropiosCliente(prestamos);
        int idP = entrada.leerEntero("Ingrese el ID de su credito para ver la amortizacion: ", 1, Integer.MAX_VALUE);
        boolean lePertenece = prestamos.stream().anyMatch(p -> p.getId() == idP);
        if (!lePertenece) {
            mensajes.error("Acceso denegado: El credito #" + idP + " no pertenece a su cuenta.");
            entrada.pausar();
            return;
        }

        Prestamo completo = prestamoDAO.buscarPorId(idP);
        if (completo != null) {
            reporteView.mostrarTablaAmortizacion(completo);
        }
        entrada.pausar();
    }
}

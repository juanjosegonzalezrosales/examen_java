package controller;

import dao.PagoDAO;
import dao.PrestamoDAO;
import exception.EntidadNoEncontradaException;
import exception.ReglaNegocioException;
import exception.ValidacionException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import model.Cuota;
import model.Pago;
import model.Prestamo;
import model.enums.EstadoCuota;
import model.enums.EstadoPrestamo;
import model.enums.OpcionAbonoExtra;
import model.enums.TipoPago;
import security.AutorizacionService;
import view.comun.EntradaConsola;
import view.comun.MensajesView;
import view.pagos.PagoView;

public class PagoController {

    private final PagoDAO pagoDAO;
    private final PrestamoDAO prestamoDAO;
    private final AutorizacionService autorizacionService;
    private final PagoView pagoView;
    private final EntradaConsola entrada;
    private final MensajesView mensajes;

    public PagoController(PagoDAO pagoDAO, PrestamoDAO prestamoDAO,
                          AutorizacionService autorizacionService, PagoView pagoView,
                          EntradaConsola entrada, MensajesView mensajes) {
        this.pagoDAO = pagoDAO;
        this.prestamoDAO = prestamoDAO;
        this.autorizacionService = autorizacionService;
        this.pagoView = pagoView;
        this.entrada = entrada;
        this.mensajes = mensajes;
    }

    public void gestionarMenuPagos() {
        if (!autorizacionService.validarPermiso(autorizacionService.puedeRegistrarPagos(), "Gestionar y registrar pagos")) return;
        int op;
        do {
            op = pagoView.mostrarMenuOperaciones();
            switch (op) {
                case 1 -> pagarCuotaProgramada();
                case 2 -> registrarAbonoExtraordinario();
                case 3 -> cancelarTotalPrestamo();
                case 4 -> consultarHistorialPorPrestamo();
                case 5 -> consultarPagosCuota();
                case 6 -> consultarAbonosExtraordinarios();
            }
        } while (op != 0);
    }

    public void pagarCuotaProgramada() {
        if (!autorizacionService.validarPermiso(autorizacionService.puedeRegistrarPagos(), "Registrar pago de cuota")) return;
        try {
            int idP = entrada.leerEntero("ID del Prestamo: ", 1, Integer.MAX_VALUE);
            Prestamo p = prestamoDAO.buscarPorId(idP);
            if (p == null) throw new EntidadNoEncontradaException("Prestamo con ID " + idP + " no encontrado.");
            if (p.getEstado() == EstadoPrestamo.PAGADO) {
                throw new ReglaNegocioException("El prestamo #" + idP + " ya se encuentra completamente PAGADO.");
            }

            Cuota cuotaPendiente = p.getCuotas().stream()
                    .filter(c -> c.getEstado() == EstadoCuota.PENDIENTE)
                    .findFirst()
                    .orElse(null);

            if (cuotaPendiente == null) {
                throw new ReglaNegocioException("No hay cuotas pendientes para liquidar en el prestamo #" + idP + ".");
            }

            BigDecimal cap = cuotaPendiente.getCapitalPendiente();
            BigDecimal inte = cuotaPendiente.getInteresPendiente();
            BigDecimal total = cap.add(inte);

            if (pagoView.confirmarPagoCuota(cuotaPendiente, total, cap, inte)) {
                Pago pago = new Pago(p.getId(), cuotaPendiente.getId(), LocalDate.now(),
                                     total, BigDecimal.ZERO, inte, cap,
                                     TipoPago.CUOTA_PROGRAMADA, null, "Pago cuota #" + cuotaPendiente.getNumeroCuota());
                if (pagoDAO.registrarPago(pago)) {
                    mensajes.exito("Pago registrado exitosamente. Recibo de transaccion ID #" + pago.getId());
                } else {
                    mensajes.error("No se pudo procesar el pago.");
                }
            } else {
                mensajes.informativo("Operacion cancelada.");
            }
        } catch (EntidadNoEncontradaException | ReglaNegocioException | ValidacionException e) {
            mensajes.error(e.getMessage());
        }
        entrada.pausar();
    }

    public void registrarAbonoExtraordinario() {
        if (!autorizacionService.validarPermiso(autorizacionService.puedeRegistrarPagos(), "Registrar abono extraordinario")) return;
        try {
            int idP = entrada.leerEntero("ID del Prestamo: ", 1, Integer.MAX_VALUE);
            Prestamo p = prestamoDAO.buscarPorId(idP);
            if (p == null) throw new EntidadNoEncontradaException("Prestamo con ID " + idP + " no encontrado.");
            if (p.getEstado() == EstadoPrestamo.PAGADO) {
                throw new ReglaNegocioException("El prestamo #" + idP + " ya se encuentra completamente PAGADO.");
            }

            Object[] datos = pagoView.pedirAbonoExtraordinario(p.getSaldoPendiente());
            BigDecimal monto = (BigDecimal) datos[0];
            OpcionAbonoExtra opcion = (OpcionAbonoExtra) datos[1];

            if (monto.compareTo(p.getSaldoPendiente()) > 0) {
                throw new ReglaNegocioException("El abono ($" + monto + ") no puede ser superior al saldo pendiente ($" + p.getSaldoPendiente() + ").");
            }

            Pago pago = new Pago(p.getId(), null, LocalDate.now(), monto,
                                 BigDecimal.ZERO, BigDecimal.ZERO, monto,
                                 TipoPago.ABONO_EXTRAORDINARIO, opcion, "Abono extraordinario (" + opcion + ")");

            if (pagoDAO.registrarPago(pago)) {
                mensajes.exito("Abono extraordinario procesado exitosamente. Calendario de cuotas recalculado.");
            } else {
                mensajes.error("Error al procesar el abono extraordinario.");
            }
        } catch (EntidadNoEncontradaException | ReglaNegocioException | ValidacionException e) {
            mensajes.error(e.getMessage());
        }
        entrada.pausar();
    }

    public void cancelarTotalPrestamo() {
        if (!autorizacionService.validarPermiso(autorizacionService.puedeRegistrarPagos(), "Cancelar prestamo total")) return;
        try {
            int idP = entrada.leerEntero("ID del Prestamo a liquidar: ", 1, Integer.MAX_VALUE);
            Prestamo p = prestamoDAO.buscarPorId(idP);
            if (p == null) throw new EntidadNoEncontradaException("Prestamo con ID " + idP + " no encontrado.");
            if (p.getEstado() == EstadoPrestamo.PAGADO) {
                throw new ReglaNegocioException("El prestamo #" + idP + " ya se encuentra cancelado (PAGADO).");
            }

            BigDecimal saldo = p.getSaldoPendiente();
            if (pagoView.confirmarCancelacionTotal(saldo)) {
                Pago pago = new Pago(p.getId(), null, LocalDate.now(), saldo,
                                     BigDecimal.ZERO, BigDecimal.ZERO, saldo,
                                     TipoPago.PAGO_TOTAL, null, "Cancelacion total anticipada del credito");
                if (pagoDAO.registrarPago(pago)) {
                    mensajes.exito("Prestamo #" + p.getId() + " liquidado y cerrado en su totalidad [PAGADO].");
                } else {
                    mensajes.error("Error al procesar la cancelacion.");
                }
            } else {
                mensajes.informativo("Operacion cancelada.");
            }
        } catch (EntidadNoEncontradaException | ReglaNegocioException | ValidacionException e) {
            mensajes.error(e.getMessage());
        }
        entrada.pausar();
    }

    public void consultarHistorialPorPrestamo() {
        try {
            int idP = entrada.leerEntero("ID del Prestamo: ", 1, Integer.MAX_VALUE);
            Prestamo p = prestamoDAO.buscarPorId(idP);
            if (p == null) throw new EntidadNoEncontradaException("Prestamo con ID " + idP + " no encontrado.");
            List<Pago> pagos = pagoDAO.listarPorPrestamo(idP);
            pagoView.mostrarTablaPagos(pagos, "HISTORIAL DE PAGOS DEL PRESTAMO #" + idP);
        } catch (EntidadNoEncontradaException e) {
            mensajes.error(e.getMessage());
        }
        entrada.pausar();
    }

    public void consultarPagosCuota() {
        List<Pago> lista = pagoDAO.listarTodos().stream()
                .filter(p -> p.getTipoPago() == TipoPago.CUOTA_PROGRAMADA)
                .toList();
        pagoView.mostrarTablaPagos(lista, "PAGOS REGISTRADOS DE CUOTAS PROGRAMADAS");
        entrada.pausar();
    }

    public void consultarAbonosExtraordinarios() {
        List<Pago> lista = pagoDAO.listarTodos().stream()
                .filter(p -> p.getTipoPago() == TipoPago.ABONO_EXTRAORDINARIO)
                .toList();
        pagoView.mostrarTablaPagos(lista, "PAGOS REGISTRADOS DE ABONOS EXTRAORDINARIOS");
        entrada.pausar();
    }
}


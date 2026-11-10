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
import model.Solicitud;
import model.chain.AccionEvaluacion;
import model.chain.CadenaAprobacion;
import model.chain.ResultadoCadena;
import model.enums.EstadoSolicitud;
import model.enums.ModalidadAmortizacion;
import security.AutorizacionService;
import view.comun.EntradaConsola;
import view.comun.MensajesView;
import view.solicitudes.SolicitudView;

public class SolicitudController {

    private final SolicitudDAO solicitudDAO;
    private final ClienteDAO clienteDAO;
    private final PrestamoDAO prestamoDAO;
    private final AutorizacionService autorizacionService;
    private final SolicitudView solicitudView;
    private final EntradaConsola entrada;
    private final MensajesView mensajes;

    public SolicitudController(SolicitudDAO solicitudDAO, ClienteDAO clienteDAO, PrestamoDAO prestamoDAO,
                               AutorizacionService autorizacionService, SolicitudView solicitudView,
                               EntradaConsola entrada, MensajesView mensajes) {
        this.solicitudDAO = solicitudDAO;
        this.clienteDAO = clienteDAO;
        this.prestamoDAO = prestamoDAO;
        this.autorizacionService = autorizacionService;
        this.solicitudView = solicitudView;
        this.entrada = entrada;
        this.mensajes = mensajes;
    }

    public void radicarSolicitud(Empleado empActual) {
        if (!autorizacionService.validarPermiso(autorizacionService.puedeRadicarSolicitudes(), "Radicar solicitudes")) return;
        try {
            int idCliente = entrada.leerEntero("ID del Cliente titular: ", 1, Integer.MAX_VALUE);
            Cliente cli = clienteDAO.buscarPorId(idCliente);
            if (cli == null) {
                throw new EntidadNoEncontradaException("Cliente con ID " + idCliente + " no encontrado.");
            }

            Solicitud sol = solicitudView.pedirNuevaSolicitud(cli, empActual);
            sol.validar();

            if (solicitudDAO.guardar(sol)) {
                mensajes.exito("Solicitud radicada exitosamente con numero #" + sol.getId() + " [PENDIENTE]");
            } else {
                mensajes.error("No se pudo radicar la solicitud.");
            }
        } catch (EntidadNoEncontradaException | ValidacionException e) {
            mensajes.error(e.getMessage());
        }
        entrada.pausar();
    }

    public void listarTodas() {
        if (!autorizacionService.validarPermiso(autorizacionService.puedeConsultarTodasSolicitudes(), "Consultar solicitudes")) return;
        List<Solicitud> lista = solicitudDAO.listarTodos();
        solicitudView.mostrarTablaSolicitudes(lista);
        entrada.pausar();
    }

    public void listarPorCliente() {
        if (!autorizacionService.validarPermiso(autorizacionService.puedeConsultarTodasSolicitudes(), "Consultar solicitudes por cliente")) return;
        try {
            int idCli = entrada.leerEntero("ID del Cliente a consultar: ", 1, Integer.MAX_VALUE);
            Cliente cli = clienteDAO.buscarPorId(idCli);
            if (cli == null) {
                throw new EntidadNoEncontradaException("Cliente con ID " + idCli + " no encontrado.");
            }
            List<Solicitud> lista = solicitudDAO.listarTodos().stream()
                    .filter(s -> s.getCliente() != null && idCli == s.getCliente().getId())
                    .toList();
            solicitudView.mostrarTablaSolicitudes(lista);
        } catch (EntidadNoEncontradaException e) {
            mensajes.error(e.getMessage());
        }
        entrada.pausar();
    }

    public void consultarDetalle() {
        try {
            int id = entrada.leerEntero("Numero de solicitud a consultar: ", 1, Integer.MAX_VALUE);
            Solicitud s = solicitudDAO.buscarPorId(id);
            if (s == null) throw new EntidadNoEncontradaException("Solicitud #" + id + " no encontrada.");
            solicitudView.mostrarDetalleSolicitud(s);
        } catch (EntidadNoEncontradaException e) {
            mensajes.error(e.getMessage());
        }
        entrada.pausar();
    }

    public void modificarSolicitud(Empleado empActual) {
        if (!autorizacionService.validarPermiso(autorizacionService.puedeActualizarSolicitudes(), "Actualizar solicitudes")) return;
        try {
            int idSol = entrada.leerEntero("Numero de solicitud a modificar: ", 1, Integer.MAX_VALUE);
            Solicitud sol = solicitudDAO.buscarPorId(idSol);
            if (sol == null) throw new EntidadNoEncontradaException("Solicitud #" + idSol + " no encontrada.");

            if (sol.getEstado() == EstadoSolicitud.DESEMBOLSADO || prestamoDAO.buscarPorSolicitudId(sol.getId()) != null) {
                throw new ReglaNegocioException("La solicitud #" + sol.getId() + " ya origino un prestamo desembolsado y es inmutable.");
            }

            if (sol.getEstado() != EstadoSolicitud.PENDIENTE) {
                throw new ReglaNegocioException("Solo se pueden modificar solicitudes en estado PENDIENTE. Estado actual: " + sol.getEstado());
            }

            solicitudView.mostrarDetalleSolicitud(sol);

            System.out.println("Campos modificables:");
            System.out.println("  1. Modificar monto solicitado");
            System.out.println("  2. Modificar plazo en meses");
            System.out.println("  3. Modificar modalidad (FRANCES / ALEMAN)");
            System.out.println("  4. Agregar observacion");
            System.out.println("  0. Cancelar");

            int op = entrada.leerEntero("Seleccione opcion [0-4]: ", 0, 4);
            if (op == 0) return;

            switch (op) {
                case 1 -> {
                    BigDecimal nuevoMonto = entrada.leerBigDecimal("Nuevo monto solicitado ($): ", new BigDecimal("100000.00"));
                    ResultadoCadena res = CadenaAprobacion.evaluar(sol, empActual, AccionEvaluacion.MODIFICAR_MONTO, nuevoMonto);
                    if (!res.isAutorizado()) {
                        mensajes.error("Acceso denegado por la Cadena de Responsabilidad: " + res.getMensaje());
                    } else {
                        BigDecimal ant = sol.getMontoSolicitado();
                        sol.setMontoSolicitado(nuevoMonto);
                        sol.validar();
                        String aud = " | [" + LocalDate.now() + "] Monto modificado de $" + ant + " a $" + nuevoMonto + " por " + empActual.getRol() + " " + empActual.getNombre();
                        sol.setObservaciones((sol.getObservaciones() != null ? sol.getObservaciones() : "") + aud);
                        if (solicitudDAO.actualizar(sol)) {
                            mensajes.exito("Monto actualizado exitosamente a $" + nuevoMonto);
                        } else {
                            mensajes.error("No se pudo actualizar la solicitud en base de datos.");
                        }
                    }
                }
                case 2 -> {
                    ResultadoCadena res = CadenaAprobacion.evaluar(sol, empActual, AccionEvaluacion.MODIFICAR_DATOS);
                    if (!res.isAutorizado()) {
                        mensajes.error("Acceso denegado: " + res.getMensaje());
                    } else {
                        int nuevoPlazo = entrada.leerEntero("Nuevo plazo en meses [1-72]: ", 1, 72);
                        int ant = sol.getPlazoMeses();
                        sol.setPlazoMeses(nuevoPlazo);
                        sol.validar();
                        String aud = " | [" + LocalDate.now() + "] Plazo modificado de " + ant + " a " + nuevoPlazo + " meses por " + empActual.getRol() + " " + empActual.getNombre();
                        sol.setObservaciones((sol.getObservaciones() != null ? sol.getObservaciones() : "") + aud);
                        if (solicitudDAO.actualizar(sol)) {
                            mensajes.exito("Plazo actualizado exitosamente.");
                        } else {
                            mensajes.error("Error al actualizar la solicitud.");
                        }
                    }
                }
                case 3 -> {
                    ResultadoCadena res = CadenaAprobacion.evaluar(sol, empActual, AccionEvaluacion.MODIFICAR_DATOS);
                    if (!res.isAutorizado()) {
                        mensajes.error("Acceso denegado: " + res.getMensaje());
                    } else {
                        System.out.println("Modalidad: 1. FRANCES | 2. ALEMAN");
                        int mOp = entrada.leerEntero("Opcion [1-2]: ", 1, 2);
                        ModalidadAmortizacion mod = (mOp == 1) ? ModalidadAmortizacion.FRANCES : ModalidadAmortizacion.ALEMAN;
                        sol.setModalidad(mod);
                        sol.validar();
                        String aud = " | [" + LocalDate.now() + "] Modalidad modificada a " + mod + " por " + empActual.getRol() + " " + empActual.getNombre();
                        sol.setObservaciones((sol.getObservaciones() != null ? sol.getObservaciones() : "") + aud);
                        if (solicitudDAO.actualizar(sol)) {
                            mensajes.exito("Modalidad actualizada exitosamente.");
                        } else {
                            mensajes.error("Error al actualizar.");
                        }
                    }
                }
                case 4 -> {
                    String nota = entrada.leerTexto("Observacion a agregar: ");
                    String aud = " | [" + LocalDate.now() + "] Nota de " + empActual.getRol() + " " + empActual.getNombre() + ": " + nota;
                    sol.setObservaciones((sol.getObservaciones() != null ? sol.getObservaciones() : "") + aud);
                    if (solicitudDAO.actualizar(sol)) {
                        mensajes.exito("Observacion agregada correctamente.");
                    } else {
                        mensajes.error("Error al actualizar.");
                    }
                }
            }
        } catch (EntidadNoEncontradaException | ReglaNegocioException | ValidacionException e) {
            mensajes.error(e.getMessage());
        }
        entrada.pausar();
    }

    public void evaluarSolicitud(Empleado evaluador, boolean aprobar) {
        boolean puedeEvaluar = aprobar ? autorizacionService.puedeAprobarSolicitudes() : autorizacionService.puedeRechazarSolicitudes();
        if (!autorizacionService.validarPermiso(puedeEvaluar, (aprobar ? "Aprobar" : "Rechazar") + " solicitudes")) return;

        try {
            int idSol = entrada.leerEntero("Numero de solicitud a " + (aprobar ? "APROBAR" : "RECHAZAR") + ": ", 1, Integer.MAX_VALUE);
            Solicitud sol = solicitudDAO.buscarPorId(idSol);
            if (sol == null) throw new EntidadNoEncontradaException("Solicitud #" + idSol + " no encontrada.");

            if (sol.getEstado() == EstadoSolicitud.DESEMBOLSADO || prestamoDAO.buscarPorSolicitudId(sol.getId()) != null) {
                throw new ReglaNegocioException("La solicitud #" + sol.getId() + " ya tiene un prestamo desembolsado y no puede alterarse.");
            }
            if (sol.getEstado() == EstadoSolicitud.RECHAZADA) {
                throw new ReglaNegocioException("La solicitud #" + sol.getId() + " esta RECHAZADA. Las decisiones de rechazo son irreversibles.");
            }

            AccionEvaluacion accion = aprobar ? AccionEvaluacion.APROBAR : AccionEvaluacion.RECHAZAR;
            ResultadoCadena res = CadenaAprobacion.evaluar(sol, evaluador, accion);
            if (!res.isAutorizado()) {
                mensajes.error("Acceso denegado por la Cadena de Responsabilidad: " + res.getMensaje());
                entrada.pausar();
                return;
            }

            String dictamen = entrada.leerTexto("Dictamen / Motivo de la decision: ");
            sol.setEstado(aprobar ? EstadoSolicitud.APROBADA : EstadoSolicitud.RECHAZADA);
            sol.setFechaResolucion(LocalDate.now());
            String aud = " | [" + LocalDate.now() + "] " + (aprobar ? "Aprobada" : "Rechazada") + " por " + evaluador.getRol() + " " + evaluador.getNombre() + ": " + dictamen;
            sol.setObservaciones((sol.getObservaciones() != null ? sol.getObservaciones() : "") + aud);

            if (solicitudDAO.actualizar(sol)) {
                mensajes.exito("Solicitud #" + sol.getId() + " " + (aprobar ? "APROBADA" : "RECHAZADA") + " exitosamente. " + res.getMensaje());
            } else {
                mensajes.error("No se pudo registrar la resolucion en base de datos.");
            }
        } catch (EntidadNoEncontradaException | ReglaNegocioException e) {
            mensajes.error(e.getMessage());
        }
        entrada.pausar();
    }

    public void eliminarSolicitud(Empleado empActual) {
        if (!autorizacionService.validarPermiso(autorizacionService.puedeEliminarSolicitudes(), "Eliminar solicitudes")) return;
        try {
            int idSol = entrada.leerEntero("Numero de solicitud a eliminar: ", 1, Integer.MAX_VALUE);
            Solicitud sol = solicitudDAO.buscarPorId(idSol);
            if (sol == null) throw new EntidadNoEncontradaException("Solicitud #" + idSol + " no encontrada.");

            if (sol.getEstado() == EstadoSolicitud.DESEMBOLSADO || prestamoDAO.buscarPorSolicitudId(sol.getId()) != null) {
                throw new ReglaNegocioException("La solicitud #" + sol.getId() + " ya tiene un prestamo desembolsado y no puede eliminarse.");
            }

            ResultadoCadena res = CadenaAprobacion.evaluar(sol, empActual, AccionEvaluacion.ELIMINAR);
            if (!res.isAutorizado()) {
                mensajes.error("Acceso denegado por la Cadena: " + res.getMensaje());
                entrada.pausar();
                return;
            }

            String conf = entrada.leerTexto("Confirmar eliminacion de la solicitud #" + idSol + "? (S/N): ");
            if (conf.equalsIgnoreCase("S")) {
                if (solicitudDAO.eliminar(idSol)) {
                    mensajes.exito("Solicitud #" + idSol + " eliminada exitosamente.");
                } else {
                    mensajes.error("Error al eliminar la solicitud.");
                }
            } else {
                mensajes.informativo("Operacion cancelada.");
            }
        } catch (EntidadNoEncontradaException | ReglaNegocioException e) {
            mensajes.error(e.getMessage());
        }
        entrada.pausar();
    }
}


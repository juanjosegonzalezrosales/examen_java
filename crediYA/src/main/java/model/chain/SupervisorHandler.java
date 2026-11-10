package model.chain;

import java.math.BigDecimal;
import model.Empleado;
import model.Solicitud;
import model.enums.EstadoSolicitud;
import model.enums.Rol;

public class SupervisorHandler extends ManejadorSolicitud {

    public static final BigDecimal LIMITE_SUPERVISOR = new BigDecimal("200000000.00");

    @Override
    public ResultadoCadena evaluar(Solicitud solicitud, Empleado empleado, AccionEvaluacion accion, BigDecimal nuevoMonto) {
        if (empleado == null) {
            return new ResultadoCadena(false, "No se especifico el empleado que realiza la operacion.");
        }

        if (empleado.getRol() != Rol.SUPERVISOR) {
            if (siguiente != null) {
                return siguiente.evaluar(solicitud, empleado, accion, nuevoMonto);
            }
            return new ResultadoCadena(false, "Fin de cadena sin manejador competente.");
        }

        if (solicitud == null) {
            return new ResultadoCadena(false, "La solicitud a evaluar no puede ser nula.");
        }

        if (solicitud.getEstado() == EstadoSolicitud.DESEMBOLSADO) {
            return new ResultadoCadena(false, "Operacion denegada: La solicitud ya fue desembolsada y su estado es definitivo e irreversible.");
        }

        switch (accion) {
            case APROBAR -> {
                if (solicitud.getEstado() != EstadoSolicitud.PENDIENTE) {
                    return new ResultadoCadena(false, "Operacion denegada: Solo se pueden aprobar solicitudes en estado PENDIENTE.");
                }
                if (solicitud.getMontoSolicitado().compareTo(LIMITE_SUPERVISOR) > 0) {
                    return new ResultadoCadena(false, "Acceso denegado: El Supervisor solo puede aprobar solicitudes de hasta $200.000.000. Monto actual ($" + solicitud.getMontoSolicitado() + ") requiere aprobacion de Gerencia.");
                }
                return new ResultadoCadena(true, "Aprobacion autorizada para Supervisor.");
            }
            case RECHAZAR -> {
                if (solicitud.getEstado() == EstadoSolicitud.RECHAZADA) {
                    return new ResultadoCadena(false, "La solicitud ya se encuentra RECHAZADA y cerrada definitivamente.");
                }
                if (solicitud.getEstado() != EstadoSolicitud.PENDIENTE && solicitud.getEstado() != EstadoSolicitud.APROBADA) {
                    return new ResultadoCadena(false, "Estado no valido para rechazar la solicitud.");
                }
                if (solicitud.getMontoSolicitado().compareTo(LIMITE_SUPERVISOR) > 0) {
                    return new ResultadoCadena(false, "Acceso denegado: El Supervisor solo puede rechazar solicitudes de hasta $200.000.000.");
                }
                return new ResultadoCadena(true, "Rechazo autorizado para Supervisor.");
            }
            case MODIFICAR_DATOS -> {
                if (solicitud.getEstado() != EstadoSolicitud.PENDIENTE) {
                    return new ResultadoCadena(false, "Operacion denegada: Solo se pueden modificar solicitudes en estado PENDIENTE.");
                }
                return new ResultadoCadena(true, "Modificacion de datos autorizada para Supervisor.");
            }
            case MODIFICAR_MONTO -> {
                if (solicitud.getEstado() != EstadoSolicitud.PENDIENTE) {
                    return new ResultadoCadena(false, "Operacion denegada: Solo se pueden modificar solicitudes en estado PENDIENTE.");
                }
                if (solicitud.getMontoSolicitado().compareTo(LIMITE_SUPERVISOR) > 0) {
                    return new ResultadoCadena(false, "Acceso denegado: El Supervisor no puede modificar el monto de una solicitud que supera $200.000.000.");
                }
                if (nuevoMonto != null && nuevoMonto.compareTo(LIMITE_SUPERVISOR) > 0) {
                    return new ResultadoCadena(false, "Acceso denegado: El Supervisor no puede aumentar el monto por encima de $200.000.000.");
                }
                return new ResultadoCadena(true, "Modificacion de monto autorizada para Supervisor.");
            }
            case ELIMINAR -> {
                if (solicitud.getEstado() != EstadoSolicitud.PENDIENTE) {
                    return new ResultadoCadena(false, "Operacion denegada: Solo se pueden eliminar solicitudes en estado PENDIENTE.");
                }
                return new ResultadoCadena(true, "Eliminacion de solicitud pendiente autorizada para Supervisor.");
            }
            default -> {
                return new ResultadoCadena(false, "Accion no reconocida para evaluacion.");
            }
        }
    }
}

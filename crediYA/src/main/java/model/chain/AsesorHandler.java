package model.chain;

import java.math.BigDecimal;
import model.Empleado;
import model.Solicitud;
import model.enums.EstadoSolicitud;
import model.enums.Rol;

public class AsesorHandler extends ManejadorSolicitud {

    public static final BigDecimal LIMITE_MONTO = new BigDecimal("200000000.00");

    @Override
    public ResultadoCadena evaluar(Solicitud solicitud, Empleado empleado, AccionEvaluacion accion, BigDecimal nuevoMonto) {
        if (empleado == null) {
            return new ResultadoCadena(false, "No se especifico el empleado que realiza la operacion.");
        }

        if (empleado.getRol() != Rol.ASESOR) {
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
                return new ResultadoCadena(false, "Acceso denegado: El rol ASESOR no tiene autorizacion para aprobar solicitudes.");
            }
            case RECHAZAR -> {
                return new ResultadoCadena(false, "Acceso denegado: El rol ASESOR no tiene autorizacion para rechazar solicitudes.");
            }
            case MODIFICAR_DATOS -> {
                if (solicitud.getEstado() != EstadoSolicitud.PENDIENTE) {
                    return new ResultadoCadena(false, "Operacion denegada: Solo se pueden modificar solicitudes en estado PENDIENTE.");
                }
                return new ResultadoCadena(true, "Modificacion de datos autorizada para Asesor.");
            }
            case MODIFICAR_MONTO -> {
                if (solicitud.getEstado() != EstadoSolicitud.PENDIENTE) {
                    return new ResultadoCadena(false, "Operacion denegada: Solo se pueden modificar solicitudes en estado PENDIENTE.");
                }
                if (solicitud.getMontoSolicitado().compareTo(LIMITE_MONTO) > 0) {
                    return new ResultadoCadena(false, "Acceso denegado: El Asesor no puede modificar el monto de solicitudes superiores a $200.000.000.");
                }
                if (nuevoMonto != null && nuevoMonto.compareTo(LIMITE_MONTO) > 0) {
                    return new ResultadoCadena(false, "Acceso denegado: El Asesor no puede aumentar el monto por encima de $200.000.000.");
                }
                return new ResultadoCadena(true, "Modificacion de monto autorizada para Asesor.");
            }
            case ELIMINAR -> {
                if (solicitud.getEstado() != EstadoSolicitud.PENDIENTE) {
                    return new ResultadoCadena(false, "Operacion denegada: Solo se pueden eliminar solicitudes en estado PENDIENTE.");
                }
                return new ResultadoCadena(true, "Eliminacion de solicitud pendiente autorizada para Asesor.");
            }
            default -> {
                return new ResultadoCadena(false, "Accion no reconocida para evaluacion.");
            }
        }
    }
}

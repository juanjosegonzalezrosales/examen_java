package model.chain;

import java.math.BigDecimal;
import model.Empleado;
import model.Solicitud;
import model.enums.EstadoSolicitud;
import model.enums.Rol;

public class GerenteHandler extends ManejadorSolicitud {

    @Override
    public ResultadoCadena evaluar(Solicitud solicitud, Empleado empleado, AccionEvaluacion accion, BigDecimal nuevoMonto) {
        if (empleado == null) {
            return new ResultadoCadena(false, "No se especifico el empleado que realiza la operacion.");
        }

        if (empleado.getRol() != Rol.GERENTE) {
            if (siguiente != null) {
                return siguiente.evaluar(solicitud, empleado, accion, nuevoMonto);
            }
            return new ResultadoCadena(false, "Rol no reconocido para autorizacion en la cadena.");
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
                return new ResultadoCadena(true, "Aprobacion autorizada para Gerente (sin limite de monto).");
            }
            case RECHAZAR -> {
                if (solicitud.getEstado() == EstadoSolicitud.RECHAZADA) {
                    return new ResultadoCadena(false, "La solicitud ya se encuentra RECHAZADA y cerrada definitivamente.");
                }
                if (solicitud.getEstado() != EstadoSolicitud.PENDIENTE && solicitud.getEstado() != EstadoSolicitud.APROBADA) {
                    return new ResultadoCadena(false, "Estado no valido para rechazar la solicitud.");
                }
                return new ResultadoCadena(true, "Rechazo autorizado para Gerente (sin limite de monto).");
            }
            case MODIFICAR_DATOS -> {
                if (solicitud.getEstado() != EstadoSolicitud.PENDIENTE) {
                    return new ResultadoCadena(false, "Operacion denegada: Solo se pueden modificar solicitudes en estado PENDIENTE.");
                }
                return new ResultadoCadena(true, "Modificacion de datos autorizada para Gerente.");
            }
            case MODIFICAR_MONTO -> {
                if (solicitud.getEstado() != EstadoSolicitud.PENDIENTE) {
                    return new ResultadoCadena(false, "Operacion denegada: Solo se pueden modificar solicitudes en estado PENDIENTE.");
                }
                return new ResultadoCadena(true, "Modificacion de monto autorizada para Gerente (sin limite de monto).");
            }
            case ELIMINAR -> {
                if (solicitud.getEstado() != EstadoSolicitud.PENDIENTE) {
                    return new ResultadoCadena(false, "Operacion denegada: Solo se pueden eliminar solicitudes en estado PENDIENTE.");
                }
                return new ResultadoCadena(true, "Eliminacion de solicitud pendiente autorizada para Gerente.");
            }
            default -> {
                return new ResultadoCadena(false, "Accion no reconocida para evaluacion.");
            }
        }
    }
}

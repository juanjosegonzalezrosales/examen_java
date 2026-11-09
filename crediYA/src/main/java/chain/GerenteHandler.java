package chain;

import java.time.LocalDate;
import model.Empleado;
import model.Solicitud;
import model.enums.EstadoSolicitud;
import model.enums.Rol;

public class GerenteHandler extends ManejadorSolicitud {

    @Override
    public ResultadoCadena procesar(Solicitud solicitud, Empleado evaluador, AccionEvaluacion accion, String notas) {
        String obsAnterior = solicitud.getObservaciones() != null ? solicitud.getObservaciones() : "";

        if (evaluador.getRol() == Rol.GERENTE) {
            if (accion == AccionEvaluacion.APROBAR) {
                solicitud.setEstado(EstadoSolicitud.APROBADA);
                solicitud.setFechaResolucion(LocalDate.now());
                solicitud.setObservaciones(obsAnterior + " | [Aprobado por Gerente " + evaluador.getNombre() + "]: " + notas);
                return new ResultadoCadena(true, "Solicitud APROBADA en última instancia por Gerente.",
                                           EstadoSolicitud.APROBADA, evaluador);
            } else if (accion == AccionEvaluacion.RECHAZAR) {
                solicitud.setEstado(EstadoSolicitud.RECHAZADA);
                solicitud.setFechaResolucion(LocalDate.now());
                solicitud.setObservaciones(obsAnterior + " | [Rechazado por Gerente " + evaluador.getNombre() + "]: " + notas);
                return new ResultadoCadena(true, "Solicitud RECHAZADA en última instancia por Gerente.",
                                           EstadoSolicitud.RECHAZADA, evaluador);
            } else {
                return new ResultadoCadena(false, "El Gerente es la máxima autoridad; no existe nivel superior para derivar.",
                                           solicitud.getEstado(), evaluador);
            }
        }

        if (accion == AccionEvaluacion.APROBAR) {
            solicitud.setEstado(EstadoSolicitud.APROBADA);
            solicitud.setFechaResolucion(LocalDate.now());
            solicitud.setObservaciones(obsAnterior + " | [Aprobado en Gerencia]: " + notas);
            return new ResultadoCadena(true, "Solicitud radicada y escalada APROBADA en Gerencia.",
                                       EstadoSolicitud.APROBADA, evaluador);
        } else if (accion == AccionEvaluacion.RECHAZAR) {
            solicitud.setEstado(EstadoSolicitud.RECHAZADA);
            solicitud.setFechaResolucion(LocalDate.now());
            solicitud.setObservaciones(obsAnterior + " | [Rechazado en Gerencia]: " + notas);
            return new ResultadoCadena(true, "Solicitud radicada y escalada RECHAZADA en Gerencia.",
                                       EstadoSolicitud.RECHAZADA, evaluador);
        }

        return new ResultadoCadena(false, "No se pudo procesar la solicitud en la cadena.", solicitud.getEstado(), evaluador);
    }
}

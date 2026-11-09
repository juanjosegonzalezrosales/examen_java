package chain;

import java.time.LocalDate;
import model.Empleado;
import model.Solicitud;
import model.enums.EstadoSolicitud;
import model.enums.Rol;

public class SupervisorHandler extends ManejadorSolicitud {

    @Override
    public ResultadoCadena procesar(Solicitud solicitud, Empleado evaluador, AccionEvaluacion accion, String notas) {
        if (evaluador.getRol() == Rol.SUPERVISOR) {
            String obsAnterior = solicitud.getObservaciones() != null ? solicitud.getObservaciones() : "";

            if (accion == AccionEvaluacion.APROBAR) {
                solicitud.setEstado(EstadoSolicitud.APROBADA);
                solicitud.setFechaResolucion(LocalDate.now());
                solicitud.setObservaciones(obsAnterior + " | [Aprobado por Supervisor " + evaluador.getNombre() + "]: " + notas);
                return new ResultadoCadena(true, "Solicitud APROBADA exitosamente por Supervisor.",
                                           EstadoSolicitud.APROBADA, evaluador);
            } else if (accion == AccionEvaluacion.RECHAZAR) {
                solicitud.setEstado(EstadoSolicitud.RECHAZADA);
                solicitud.setFechaResolucion(LocalDate.now());
                solicitud.setObservaciones(obsAnterior + " | [Rechazado por Supervisor " + evaluador.getNombre() + "]: " + notas);
                return new ResultadoCadena(true, "Solicitud RECHAZADA por Supervisor.",
                                           EstadoSolicitud.RECHAZADA, evaluador);
            } else {

                solicitud.setObservaciones(obsAnterior + " | [Escalado por Supervisor " + evaluador.getNombre() + " a Gerencia]: " + notas);
                System.out.println("[Cadena - Supervisor] Escalando solicitud a GERENCIA...");
                if (siguiente != null) {
                    return siguiente.procesar(solicitud, evaluador, accion, notas);
                } else {
                    return new ResultadoCadena(false, "No hay superior en la cadena para derivar.",
                                               solicitud.getEstado(), evaluador);
                }
            }
        }

        if (siguiente != null) {
            return siguiente.procesar(solicitud, evaluador, accion, notas);
        }
        return new ResultadoCadena(false, "Fin de cadena sin eslabón competente.", solicitud.getEstado(), evaluador);
    }
}

package chain;

import java.time.LocalDate;
import model.Empleado;
import model.Solicitud;
import model.enums.EstadoSolicitud;
import model.enums.Rol;

public class AsesorHandler extends ManejadorSolicitud {

    @Override
    public ResultadoCadena procesar(Solicitud solicitud, Empleado evaluador, AccionEvaluacion accion, String notas) {
        if (evaluador.getRol() == Rol.ASESOR) {
            String obsAnterior = solicitud.getObservaciones() != null ? solicitud.getObservaciones() : "";
            String nuevaObs = obsAnterior + " | [Revisión Asesor " + evaluador.getNombre() + "]: " + notas;
            solicitud.setObservaciones(nuevaObs);

            if (accion == AccionEvaluacion.APROBAR || accion == AccionEvaluacion.RECHAZAR) {
                System.out.println("[Cadena - Asesor] El Asesor no posee atribución de aprobación/rechazo.");
                System.out.println("[Cadena - Asesor] Derivando solicitud al siguiente nivel (SUPERVISOR)...");
            } else {
                System.out.println("[Cadena - Asesor] Derivando formalmente solicitud a SUPERVISOR...");
            }

            if (siguiente != null) {
                return siguiente.procesar(solicitud, evaluador, accion, notas);
            } else {
                return new ResultadoCadena(false, "No hay superior en la cadena para atender la derivación.",
                                           solicitud.getEstado(), evaluador);
            }
        }

        if (siguiente != null) {
            return siguiente.procesar(solicitud, evaluador, accion, notas);
        }
        return new ResultadoCadena(false, "Fin de cadena sin eslabón competente.", solicitud.getEstado(), evaluador);
    }
}

package model.chain;

import java.math.BigDecimal;
import model.Empleado;
import model.Solicitud;

public final class CadenaAprobacion {

    private CadenaAprobacion() {
    }

    public static ManejadorSolicitud obtenerCadena() {
        ManejadorSolicitud asesor = new AsesorHandler();
        ManejadorSolicitud supervisor = new SupervisorHandler();
        ManejadorSolicitud gerente = new GerenteHandler();

        asesor.setSiguiente(supervisor);
        supervisor.setSiguiente(gerente);

        return asesor;
    }

    public static ResultadoCadena evaluar(Solicitud solicitud, Empleado empleado, AccionEvaluacion accion, BigDecimal nuevoMonto) {
        return obtenerCadena().evaluar(solicitud, empleado, accion, nuevoMonto);
    }

    public static ResultadoCadena evaluar(Solicitud solicitud, Empleado empleado, AccionEvaluacion accion) {
        return evaluar(solicitud, empleado, accion, null);
    }
}

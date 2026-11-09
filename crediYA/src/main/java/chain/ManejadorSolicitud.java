package chain;

import model.Empleado;
import model.Solicitud;

public abstract class ManejadorSolicitud {

    protected ManejadorSolicitud siguiente;

    public void setSiguiente(ManejadorSolicitud siguiente) {
        this.siguiente = siguiente;
    }

    public abstract ResultadoCadena procesar(Solicitud solicitud, Empleado evaluador, AccionEvaluacion accion, String notas);
}

package model.chain;

import java.math.BigDecimal;
import model.Empleado;
import model.Solicitud;

public abstract class ManejadorSolicitud {

    protected ManejadorSolicitud siguiente;

    public void setSiguiente(ManejadorSolicitud siguiente) {
        this.siguiente = siguiente;
    }

    public abstract ResultadoCadena evaluar(Solicitud solicitud, Empleado empleado, AccionEvaluacion accion, BigDecimal nuevoMonto);
}

package chain;

import model.Empleado;
import model.enums.EstadoSolicitud;

public class ResultadoCadena {

    private final boolean exitoso;
    private final String mensaje;
    private final EstadoSolicitud nuevoEstado;
    private final Empleado responsableFinal;

    public ResultadoCadena(boolean exitoso, String mensaje, EstadoSolicitud nuevoEstado, Empleado responsableFinal) {
        this.exitoso = exitoso;
        this.mensaje = mensaje;
        this.nuevoEstado = nuevoEstado;
        this.responsableFinal = responsableFinal;
    }

    public boolean isExitoso() {
        return exitoso;
    }

    public String getMensaje() {
        return mensaje;
    }

    public EstadoSolicitud getNuevoEstado() {
        return nuevoEstado;
    }

    public Empleado getResponsableFinal() {
        return responsableFinal;
    }

    @Override
    public String toString() {
        return "ResultadoCadena{exitoso=" + exitoso + ", mensaje='" + mensaje + '\'' + '}';
    }
}

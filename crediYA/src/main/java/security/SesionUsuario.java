package security;

import model.Empleado;
import model.enums.Rol;

public class SesionUsuario {

    private static SesionUsuario instancia;
    private Empleado empleadoAutenticado;

    private SesionUsuario() {
    }

    public static synchronized SesionUsuario getInstancia() {
        if (instancia == null) {
            instancia = new SesionUsuario();
        }
        return instancia;
    }

    public void iniciarSesion(Empleado empleado) {
        this.empleadoAutenticado = empleado;
    }

    public void cerrarSesion() {
        this.empleadoAutenticado = null;
    }

    public boolean estaAutenticado() {
        return this.empleadoAutenticado != null;
    }

    public Empleado getEmpleadoAutenticado() {
        return this.empleadoAutenticado;
    }

    public Rol getRol() {
        return this.empleadoAutenticado != null ? this.empleadoAutenticado.getRol() : null;
    }

    public boolean tieneRol(Rol... rolesPermitidos) {
        if (!estaAutenticado() || rolesPermitidos == null) {
            return false;
        }
        Rol rolActual = getRol();
        for (Rol r : rolesPermitidos) {
            if (rolActual == r) {
                return true;
            }
        }
        return false;
    }
}

package security;

import model.Cliente;
import model.Empleado;
import model.enums.Rol;

public class SesionUsuario {

    private static SesionUsuario instancia;
    private Empleado empleadoAutenticado;
    private Cliente clienteAutenticado;

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
        this.clienteAutenticado = null;
    }

    public void iniciarSesionCliente(Cliente cliente) {
        this.clienteAutenticado = cliente;
        this.empleadoAutenticado = null;
    }

    public void cerrarSesion() {
        this.empleadoAutenticado = null;
        this.clienteAutenticado = null;
    }

    public boolean estaAutenticado() {
        return this.empleadoAutenticado != null || this.clienteAutenticado != null;
    }

    public boolean esFuncionario() {
        return this.empleadoAutenticado != null;
    }

    public boolean esCliente() {
        return this.clienteAutenticado != null;
    }

    public Empleado getEmpleadoAutenticado() {
        return this.empleadoAutenticado;
    }

    public Cliente getClienteAutenticado() {
        return this.clienteAutenticado;
    }

    public String getNombreUsuario() {
        if (empleadoAutenticado != null) {
            return empleadoAutenticado.getNombre();
        }
        if (clienteAutenticado != null) {
            return clienteAutenticado.getNombre();
        }
        return "Desconocido";
    }

    public Rol getRol() {
        return this.empleadoAutenticado != null ? this.empleadoAutenticado.getRol() : null;
    }

    public boolean tieneRol(Rol... rolesPermitidos) {
        if (!esFuncionario() || rolesPermitidos == null) {
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

package security;

import model.enums.Rol;

public class AutorizacionService {

    private final SesionUsuario sesion;

    public AutorizacionService() {
        this.sesion = SesionUsuario.getInstancia();
    }

    public boolean puedeConsultarClientes() {
        return sesion.tieneRol(Rol.ASESOR, Rol.SUPERVISOR, Rol.GERENTE);
    }

    public boolean puedeRegistrarClientes() {
        return sesion.tieneRol(Rol.ASESOR, Rol.SUPERVISOR, Rol.GERENTE);
    }

    public boolean puedeActualizarClientes() {
        return sesion.tieneRol(Rol.ASESOR, Rol.SUPERVISOR, Rol.GERENTE);
    }

    public boolean puedeRadicarSolicitudes() {
        return sesion.tieneRol(Rol.ASESOR, Rol.SUPERVISOR, Rol.GERENTE);
    }

    public boolean puedeConsultarTodasSolicitudes() {
        return sesion.tieneRol(Rol.ASESOR, Rol.SUPERVISOR, Rol.GERENTE);
    }

    public boolean puedeActualizarSolicitudes() {
        return sesion.tieneRol(Rol.ASESOR, Rol.SUPERVISOR, Rol.GERENTE);
    }

    public boolean puedeEliminarSolicitudes() {
        return sesion.tieneRol(Rol.ASESOR, Rol.SUPERVISOR, Rol.GERENTE);
    }

    public boolean puedeAprobarSolicitudes() {
        return sesion.tieneRol(Rol.SUPERVISOR, Rol.GERENTE);
    }

    public boolean puedeRechazarSolicitudes() {
        return sesion.tieneRol(Rol.SUPERVISOR, Rol.GERENTE);
    }

    public boolean puedeDesembolsarPrestamos() {
        return sesion.tieneRol(Rol.SUPERVISOR, Rol.GERENTE);
    }

    public boolean puedeConsultarTodosPrestamos() {
        return sesion.tieneRol(Rol.SUPERVISOR, Rol.GERENTE);
    }

    public boolean puedeRegistrarPagos() {
        return sesion.tieneRol(Rol.ASESOR, Rol.SUPERVISOR, Rol.GERENTE);
    }

    public boolean puedeConsultarReportesGenerales() {
        return sesion.tieneRol(Rol.SUPERVISOR, Rol.GERENTE);
    }

    public boolean puedeGestionarEmpleados() {
        return sesion.tieneRol(Rol.GERENTE);
    }

    public boolean puedeConsultarReportesAdministrativos() {
        return sesion.tieneRol(Rol.GERENTE);
    }

    public boolean validarPermiso(boolean permisoConcedido, String operacion) {
        if (!sesion.estaAutenticado()) {
            System.out.println("\n[ACCESO DENEGADO]: No hay una sesion activa en el sistema.");
            return false;
        }
        if (!permisoConcedido) {
            System.out.printf("\n[ACCESO DENEGADO]: El rol '%s' no tiene autorizacion para '%s'.\n",
                sesion.getRol(), operacion);
            return false;
        }
        return true;
    }
}

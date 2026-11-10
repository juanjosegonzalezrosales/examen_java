package controller;

import dao.EmpleadoDAO;
import exception.EntidadNoEncontradaException;
import exception.ValidacionException;
import java.util.List;
import model.Empleado;
import security.AutenticacionService;
import security.AutorizacionService;
import view.comun.EntradaConsola;
import view.comun.MensajesView;
import view.empleados.EmpleadoView;

public class EmpleadoController {

    private final EmpleadoDAO empleadoDAO;
    private final AutenticacionService autenticacionService;
    private final AutorizacionService autorizacionService;
    private final EmpleadoView empleadoView;
    private final EntradaConsola entrada;
    private final MensajesView mensajes;

    public EmpleadoController(EmpleadoDAO empleadoDAO, AutenticacionService autenticacionService,
                              AutorizacionService autorizacionService, EmpleadoView empleadoView,
                              EntradaConsola entrada, MensajesView mensajes) {
        this.empleadoDAO = empleadoDAO;
        this.autenticacionService = autenticacionService;
        this.autorizacionService = autorizacionService;
        this.empleadoView = empleadoView;
        this.entrada = entrada;
        this.mensajes = mensajes;
    }

    public void consultarPerfilPropio(Empleado empActual) {
        if (empActual == null) return;
        Empleado recargado = empleadoDAO.buscarPorId(empActual.getId());
        empleadoView.mostrarDetalleEmpleado(recargado != null ? recargado : empActual);
        entrada.pausar();
    }

    public void modificarDatosPersonalesPropio(Empleado empActual) {
        if (empActual == null) return;
        try {
            Empleado e = empleadoDAO.buscarPorId(empActual.getId());
            if (e == null) e = empActual;
            empleadoView.pedirModificacionDatosPersonales(e);
            e.validar();
            if (empleadoDAO.actualizar(e)) {
                empActual.setNombre(e.getNombre());
                empActual.setCorreo(e.getCorreo());
                mensajes.exito("Datos personales actualizados correctamente.");
            } else {
                mensajes.error("No se pudieron actualizar los datos en la base de datos.");
            }
        } catch (ValidacionException e) {
            mensajes.error(e.getMessage());
        }
        entrada.pausar();
    }

    public void listarEmpleados() {
        if (!autorizacionService.validarPermiso(autorizacionService.puedeGestionarEmpleados(), "Consultar lista de empleados")) return;
        List<Empleado> lista = empleadoDAO.listarTodos();
        empleadoView.mostrarTablaEmpleados(lista);
        entrada.pausar();
    }

    public void registrarEmpleado() {
        if (!autorizacionService.validarPermiso(autorizacionService.puedeGestionarEmpleados(), "Registrar nuevo empleado")) return;
        try {
            Empleado emp = empleadoView.pedirNuevoEmpleado();
            emp.validar();
            if (empleadoDAO.guardar(emp)) {
                mensajes.exito("Empleado registrado exitosamente con ID #" + emp.getId());
                String asig = entrada.leerTexto("Desea asignarle contrasena de acceso ahora? (S/N): ");
                if (asig.equalsIgnoreCase("S")) {
                    String pass = entrada.leerTexto("Contrasena para el empleado: ");
                    autenticacionService.asignarCredencialEmpleado(emp.getId(), pass.toCharArray());
                    mensajes.exito("Contrasena asignada exitosamente.");
                }
            } else {
                mensajes.error("No se pudo registrar el empleado.");
            }
        } catch (ValidacionException | EntidadNoEncontradaException e) {
            mensajes.error(e.getMessage());
        }
        entrada.pausar();
    }

    public void asignarContrasenaEmpleado() {
        if (!autorizacionService.validarPermiso(autorizacionService.puedeGestionarEmpleados(), "Asignar contrasena a empleado")) return;
        try {
            int id = entrada.leerEntero("ID del empleado: ", 1, Integer.MAX_VALUE);
            Empleado e = empleadoDAO.buscarPorId(id);
            if (e == null) throw new EntidadNoEncontradaException("Empleado con ID " + id + " no encontrado.");
            String pass = entrada.leerTexto("Nueva contrasena para " + e.getNombre() + ": ");
            if (autenticacionService.asignarCredencialEmpleado(e.getId(), pass.toCharArray())) {
                mensajes.exito("Credenciales del empleado actualizadas correctamente.");
            } else {
                mensajes.error("No se pudieron actualizar las credenciales.");
            }
        } catch (EntidadNoEncontradaException | ValidacionException e) {
            mensajes.error(e.getMessage());
        }
        entrada.pausar();
    }
}


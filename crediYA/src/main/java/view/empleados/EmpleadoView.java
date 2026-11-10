package view.empleados;

import java.math.BigDecimal;
import java.util.List;
import model.Cliente;
import model.Empleado;
import model.enums.Rol;
import view.comun.EntradaConsola;
import view.comun.MensajesView;

public class EmpleadoView {

    private final EntradaConsola entrada;
    private final MensajesView mensajes;

    public EmpleadoView(EntradaConsola entrada, MensajesView mensajes) {
        this.entrada = entrada;
        this.mensajes = mensajes;
    }

    public Empleado pedirNuevoEmpleado() {
        mensajes.encabezado("REGISTRAR NUEVO EMPLEADO");
        String nombre = entrada.leerTexto("Nombre completo: ");
        String doc = entrada.leerTexto("Documento de identidad: ");
        String correo = entrada.leerTexto("Correo electronico: ");
        BigDecimal sal = entrada.leerBigDecimal("Salario asignado ($): ", BigDecimal.ZERO);
        System.out.println("Roles disponibles: 1. ASESOR | 2. SUPERVISOR | 3. GERENTE");
        int rOp = entrada.leerEntero("Seleccione rol [1-3]: ", 1, 3);
        Rol rol = (rOp == 1) ? Rol.ASESOR : (rOp == 2) ? Rol.SUPERVISOR : Rol.GERENTE;
        return new Empleado(nombre, doc, correo, rol, sal);
    }

    public void pedirModificacionDatosPersonales(Empleado emp) {
        mensajes.encabezado("ACTUALIZAR DATOS PERSONALES DEL FUNCIONARIO");
        System.out.printf("Empleado ID: %d | Documento: %s | Rol: %s\n", emp.getId(), emp.getDocumento(), emp.getRol());
        emp.setNombre(entrada.leerTexto("Nuevo nombre [" + emp.getNombre() + "]: "));
        emp.setCorreo(entrada.leerTexto("Nuevo correo [" + emp.getCorreo() + "]: "));
    }

    public void mostrarTablaEmpleados(List<Empleado> lista) {
        mensajes.encabezado("DIRECTORIO GENERAL DE EMPLEADOS (" + lista.size() + " REGISTROS)");
        if (lista.isEmpty()) {
            System.out.println("No hay empleados registrados en el sistema.");
            return;
        }
        System.out.printf("%-5s %-25s %-15s %-15s $%-12s\n", "ID", "NOMBRE", "DOCUMENTO", "ROL", "SALARIO");
        System.out.println("-------------------------------------------------------------------------------");
        for (Empleado e : lista) {
            System.out.printf("%-5d %-25s %-15s %-15s $%,12.2f\n",
                e.getId(), e.getNombre(), e.getDocumento(), e.getRol(), e.getSalario());
        }
    }

    public void mostrarDetalleEmpleado(Empleado emp) {
        mensajes.encabezado("INFORMACION PERSONAL DEL FUNCIONARIO");
        System.out.printf(" ID Empleado : %d\n", emp.getId());
        System.out.printf(" Nombre      : %s\n", emp.getNombre());
        System.out.printf(" Documento   : %s\n", emp.getDocumento());
        System.out.printf(" Correo      : %s\n", emp.getCorreo());
        System.out.printf(" Rol Actual  : %s\n", emp.getRol());
        System.out.printf(" Salario     : $%,.2f\n", emp.getSalario());
        System.out.println("--------------------------------------------------");
    }

    public void mostrarClientesAtendidos(List<Cliente> clientes, Empleado emp) {
        mensajes.encabezado("CLIENTES ATENDIDOS POR " + emp.getNombre().toUpperCase() + " (" + emp.getRol() + ")");
        if (clientes.isEmpty()) {
            System.out.println("No registra clientes con creditos o solicitudes vinculadas a su gestion.");
            return;
        }
        System.out.printf("%-5s %-25s %-15s %-25s %-15s\n", "ID", "NOMBRE", "DOCUMENTO", "CORREO", "TELEFONO");
        System.out.println("-----------------------------------------------------------------------------------------");
        for (Cliente c : clientes) {
            System.out.printf("%-5d %-25s %-15s %-25s %-15s\n",
                c.getId(), c.getNombre(), c.getDocumento(), c.getCorreo(), c.getTelefono());
        }
    }
}


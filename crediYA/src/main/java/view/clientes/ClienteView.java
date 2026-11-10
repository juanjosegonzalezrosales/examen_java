package view.clientes;

import java.util.List;
import model.Cliente;
import view.comun.EntradaConsola;
import view.comun.MensajesView;

public class ClienteView {

    private final EntradaConsola entrada;
    private final MensajesView mensajes;

    public ClienteView(EntradaConsola entrada, MensajesView mensajes) {
        this.entrada = entrada;
        this.mensajes = mensajes;
    }

    public Cliente pedirNuevoCliente() {
        mensajes.encabezado("REGISTRAR NUEVO CLIENTE");
        String nombre = entrada.leerTexto("Nombre completo: ");
        String doc = entrada.leerTexto("Documento de identidad: ");
        String correo = entrada.leerTexto("Correo electronico: ");
        String tel = entrada.leerTexto("Telefono de contacto: ");
        return new Cliente(nombre, doc, correo, tel);
    }

    public void pedirModificacionCliente(Cliente c) {
        mensajes.encabezado("MODIFICAR DATOS DE CLIENTE");
        System.out.printf("Cliente ID: %d | Documento: %s\n", c.getId(), c.getDocumento());
        c.setNombre(entrada.leerTexto("Nuevo nombre [" + c.getNombre() + "]: "));
        c.setCorreo(entrada.leerTexto("Nuevo correo [" + c.getCorreo() + "]: "));
        c.setTelefono(entrada.leerTexto("Nuevo telefono [" + c.getTelefono() + "]: "));
    }

    public void mostrarTablaClientes(List<Cliente> lista) {
        mensajes.encabezado("DIRECTORIO GENERAL DE CLIENTES (" + lista.size() + " REGISTROS)");
        if (lista.isEmpty()) {
            System.out.println("No hay clientes registrados en el sistema.");
            return;
        }
        System.out.printf("%-5s %-25s %-15s %-25s %-15s\n", "ID", "NOMBRE", "DOCUMENTO", "CORREO", "TELEFONO");
        System.out.println("-----------------------------------------------------------------------------------------");
        for (Cliente c : lista) {
            System.out.printf("%-5d %-25s %-15s %-25s %-15s\n",
                c.getId(), c.getNombre(), c.getDocumento(), c.getCorreo(), c.getTelefono());
        }
    }

    public void mostrarDetalleCliente(Cliente c) {
        mensajes.encabezado("DATOS PERSONALES DEL CLIENTE");
        System.out.printf(" ID Cliente : %d\n", c.getId());
        System.out.printf(" Nombre     : %s\n", c.getNombre());
        System.out.printf(" Documento  : %s\n", c.getDocumento());
        System.out.printf(" Correo     : %s\n", c.getCorreo());
        System.out.printf(" Telefono   : %s\n", c.getTelefono());
        System.out.println("--------------------------------------------------");
    }
}


package controller;

import dao.ClienteDAO;
import dao.CredencialDAO;
import dao.PrestamoDAO;
import dao.SolicitudDAO;
import exception.EntidadNoEncontradaException;
import exception.ValidacionException;
import java.util.List;
import model.Cliente;
import security.AutenticacionService;
import security.AutorizacionService;
import view.clientes.ClienteView;
import view.comun.EntradaConsola;
import view.comun.MensajesView;

public class ClienteController {

    private final ClienteDAO clienteDAO;
    private final SolicitudDAO solicitudDAO;
    private final PrestamoDAO prestamoDAO;
    private final CredencialDAO credencialDAO;
    private final AutenticacionService autenticacionService;
    private final AutorizacionService autorizacionService;
    private final ClienteView clienteView;
    private final EntradaConsola entrada;
    private final MensajesView mensajes;

    public ClienteController(ClienteDAO clienteDAO, SolicitudDAO solicitudDAO, PrestamoDAO prestamoDAO,
                             CredencialDAO credencialDAO, AutenticacionService autenticacionService,
                             AutorizacionService autorizacionService, ClienteView clienteView,
                             EntradaConsola entrada, MensajesView mensajes) {
        this.clienteDAO = clienteDAO;
        this.solicitudDAO = solicitudDAO;
        this.prestamoDAO = prestamoDAO;
        this.credencialDAO = credencialDAO;
        this.autenticacionService = autenticacionService;
        this.autorizacionService = autorizacionService;
        this.clienteView = clienteView;
        this.entrada = entrada;
        this.mensajes = mensajes;
    }

    public void registrarCliente() {
        if (!autorizacionService.validarPermiso(autorizacionService.puedeRegistrarClientes(), "Registrar clientes")) return;
        try {
            Cliente cli = clienteView.pedirNuevoCliente();
            cli.validar();
            if (clienteDAO.guardar(cli)) {
                mensajes.exito("Cliente registrado exitosamente con ID #" + cli.getId());
                String genCred = entrada.leerTexto("Desea generar credenciales de acceso para el portal del cliente? (S/N): ");
                if (genCred.equalsIgnoreCase("S")) {
                    String pass = entrada.leerTexto("Asigne una contrasena para el cliente (por defecto 123456): ");
                    autenticacionService.asignarCredencialCliente(cli.getId(), pass.toCharArray());
                    mensajes.exito("Credenciales de acceso asignadas al cliente correctamente.");
                }
            } else {
                mensajes.error("No se pudo registrar el cliente en la base de datos.");
            }
        } catch (ValidacionException | EntidadNoEncontradaException e) {
            mensajes.error(e.getMessage());
        }
        entrada.pausar();
    }

    public void listarClientes() {
        if (!autorizacionService.validarPermiso(autorizacionService.puedeConsultarClientes(), "Consultar y listar clientes")) return;
        List<Cliente> lista = clienteDAO.listarTodos();
        clienteView.mostrarTablaClientes(lista);
        entrada.pausar();
    }

    public void consultarCliente() {
        if (!autorizacionService.validarPermiso(autorizacionService.puedeConsultarClientes(), "Consultar cliente")) return;
        try {
            System.out.println("Criterio de busqueda: 1. Por Documento | 2. Por ID");
            int crit = entrada.leerEntero("Seleccione opcion [1-2]: ", 1, 2);
            Cliente c;
            if (crit == 1) {
                String doc = entrada.leerTexto("Documento de identidad: ");
                c = clienteDAO.buscarPorDocumento(doc);
                if (c == null) throw new EntidadNoEncontradaException("Cliente con documento " + doc + " no encontrado.");
            } else {
                int id = entrada.leerEntero("ID del cliente: ", 1, Integer.MAX_VALUE);
                c = clienteDAO.buscarPorId(id);
                if (c == null) throw new EntidadNoEncontradaException("Cliente con ID " + id + " no encontrado.");
            }
            clienteView.mostrarDetalleCliente(c);
        } catch (EntidadNoEncontradaException e) {
            mensajes.error(e.getMessage());
        }
        entrada.pausar();
    }

    public void modificarCliente() {
        if (!autorizacionService.validarPermiso(autorizacionService.puedeActualizarClientes(), "Actualizar cliente")) return;
        try {
            int id = entrada.leerEntero("ID del cliente a modificar: ", 1, Integer.MAX_VALUE);
            Cliente c = clienteDAO.buscarPorId(id);
            if (c == null) {
                throw new EntidadNoEncontradaException("Cliente con ID " + id + " no encontrado.");
            }
            clienteView.pedirModificacionCliente(c);
            c.validar();
            if (clienteDAO.actualizar(c)) {
                mensajes.exito("Datos del cliente actualizados exitosamente.");
            } else {
                mensajes.error("Error al actualizar datos en base de datos.");
            }
        } catch (EntidadNoEncontradaException | ValidacionException e) {
            mensajes.error(e.getMessage());
        }
        entrada.pausar();
    }

    public void eliminarCliente() {
        if (!autorizacionService.validarPermiso(autorizacionService.puedeActualizarClientes(), "Eliminar cliente")) return;
        try {
            int id = entrada.leerEntero("ID del cliente a eliminar: ", 1, Integer.MAX_VALUE);
            Cliente c = clienteDAO.buscarPorId(id);
            if (c == null) {
                throw new EntidadNoEncontradaException("Cliente con ID " + id + " no encontrado.");
            }

            boolean tieneSolicitudes = solicitudDAO.listarTodos().stream()
                    .anyMatch(s -> s.getCliente() != null && id == s.getCliente().getId());
            boolean tienePrestamos = !prestamoDAO.listarPorCliente(id).isEmpty();

            if (tieneSolicitudes || tienePrestamos) {
                mensajes.error("No se puede eliminar el cliente '" + c.getNombre() + "' porque registra historial financiero (solicitudes/prestamos). Esto violaria la integridad referencial.");
                entrada.pausar();
                return;
            }

            String conf = entrada.leerTexto("Confirmar eliminacion del cliente '" + c.getNombre() + "'? (S/N): ");
            if (conf.equalsIgnoreCase("S")) {
                if (clienteDAO.eliminar(id)) {
                    mensajes.exito("Cliente eliminado exitosamente.");
                } else {
                    mensajes.error("No se pudo eliminar el cliente.");
                }
            } else {
                mensajes.informativo("Operacion cancelada.");
            }
        } catch (EntidadNoEncontradaException e) {
            mensajes.error(e.getMessage());
        }
        entrada.pausar();
    }
}


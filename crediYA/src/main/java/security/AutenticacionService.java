package security;

import dao.ClienteDAO;
import dao.CredencialDAO;
import dao.EmpleadoDAO;
import exception.AutenticacionException;
import exception.EntidadNoEncontradaException;
import exception.ReglaNegocioException;
import exception.ValidacionException;
import model.Cliente;
import model.Credencial;
import model.Empleado;
import model.Persona;
import model.enums.Rol;

public class AutenticacionService {

    private final EmpleadoDAO empleadoDAO;
    private final ClienteDAO clienteDAO;
    private final CredencialDAO credencialDAO;

    public AutenticacionService() {
        this.empleadoDAO = new EmpleadoDAO();
        this.clienteDAO = new ClienteDAO();
        this.credencialDAO = new CredencialDAO();
    }

    public Persona autenticar(String documento, char[] password) throws AutenticacionException, ValidacionException {
        if (documento == null || !documento.trim().matches("\\d{4,10}")) {
            throw new ValidacionException("El documento de identidad debe contener entre 4 y 10 digitos numericos.");
        }
        if (password == null || password.length == 0) {
            throw new ValidacionException("La contrasena no puede estar vacia.");
        }

        String docTrim = documento.trim();
        Empleado empleado = empleadoDAO.buscarPorDocumento(docTrim);
        if (empleado != null) {
            Credencial credencial = credencialDAO.buscarPorEmpleadoId(empleado.getId());
            if (credencial == null) {
                throw new AutenticacionException("El empleado '" + empleado.getNombre() + "' no tiene credenciales activas configuradas.");
            }

            boolean valida = SeguridadUtil.verificarPassword(
                password,
                credencial.getSalt(),
                credencial.getIteraciones(),
                credencial.getPasswordHash()
            );

            if (valida) {
                return empleado;
            } else {
                throw new AutenticacionException("Contrasena incorrecta para el usuario: " + empleado.getNombre());
            }
        }

        Cliente cliente = clienteDAO.buscarPorDocumento(docTrim);
        if (cliente != null) {
            Credencial credencial = credencialDAO.buscarPorClienteId(cliente.getId());
            if (credencial == null) {
                throw new AutenticacionException("El cliente '" + cliente.getNombre() + "' no tiene credenciales de acceso configuradas.");
            }

            boolean valida = SeguridadUtil.verificarPassword(
                password,
                credencial.getSalt(),
                credencial.getIteraciones(),
                credencial.getPasswordHash()
            );

            if (valida) {
                return cliente;
            } else {
                throw new AutenticacionException("Contrasena incorrecta para el cliente: " + cliente.getNombre());
            }
        }

        throw new AutenticacionException("No existe ningun usuario o cliente registrado con el documento: " + docTrim);
    }

    public boolean tieneCredencialesConfiguradas() {
        return credencialDAO.contarCredenciales() > 0;
    }

    public boolean registrarCredencialGerente(String documento, char[] password) throws ValidacionException, EntidadNoEncontradaException, ReglaNegocioException {
        if (documento == null || !documento.trim().matches("\\d{8,10}")) {
            throw new ValidacionException("El documento de identidad debe contener entre 8 y 10 digitos numericos.");
        }
        if (password == null || password.length == 0) {
            throw new ValidacionException("La contrasena no puede estar vacia.");
        }

        Empleado emp = empleadoDAO.buscarPorDocumento(documento.trim());
        if (emp == null) {
            throw new EntidadNoEncontradaException("No se encontro ningun empleado con el documento: " + documento);
        }

        if (emp.getRol() != Rol.GERENTE) {
            throw new ReglaNegocioException("ACCESO DENEGADO: El empleado '" + emp.getNombre()
                               + "' tiene rol " + emp.getRol() + ", no es GERENTE.");
        }

        if (credencialDAO.buscarPorEmpleadoId(emp.getId()) != null) {
            throw new ReglaNegocioException("El gerente '" + emp.getNombre() + "' ya tiene credenciales registradas.");
        }

        String salt = SeguridadUtil.generarSalt();
        String hash = SeguridadUtil.hashearPassword(password, salt, SeguridadUtil.ITERACIONES_DEFECTO);

        Credencial cred = new Credencial(emp.getId(), hash, salt, SeguridadUtil.ITERACIONES_DEFECTO);
        return credencialDAO.guardar(cred);
    }

    public boolean asignarCredencialEmpleado(Integer empleadoId, char[] password) throws ValidacionException, EntidadNoEncontradaException {
        if (empleadoId == null) {
            throw new ValidacionException("El ID de empleado no puede ser nulo.");
        }
        if (password == null || password.length == 0) {
            throw new ValidacionException("La contrasena no puede estar vacia.");
        }

        Empleado emp = empleadoDAO.buscarPorId(empleadoId);
        if (emp == null) {
            throw new EntidadNoEncontradaException("No se encontro ningun empleado con el ID: " + empleadoId);
        }

        String salt = SeguridadUtil.generarSalt();
        String hash = SeguridadUtil.hashearPassword(password, salt, SeguridadUtil.ITERACIONES_DEFECTO);

        Credencial existente = credencialDAO.buscarPorEmpleadoId(empleadoId);
        if (existente != null) {
            existente.setSalt(salt);
            existente.setPasswordHash(hash);
            existente.setIteraciones(SeguridadUtil.ITERACIONES_DEFECTO);
            return credencialDAO.actualizar(existente);
        } else {
            Credencial nueva = new Credencial(empleadoId, hash, salt, SeguridadUtil.ITERACIONES_DEFECTO);
            return credencialDAO.guardar(nueva);
        }
    }

    public boolean asignarCredencialCliente(Integer clienteId, char[] password) throws ValidacionException, EntidadNoEncontradaException {
        if (clienteId == null) {
            throw new ValidacionException("El ID de cliente no puede ser nulo.");
        }
        if (password == null || password.length == 0) {
            throw new ValidacionException("La contrasena no puede estar vacia.");
        }

        Cliente cli = clienteDAO.buscarPorId(clienteId);
        if (cli == null) {
            throw new EntidadNoEncontradaException("No se encontro ningun cliente con el ID: " + clienteId);
        }

        String salt = SeguridadUtil.generarSalt();
        String hash = SeguridadUtil.hashearPassword(password, salt, SeguridadUtil.ITERACIONES_DEFECTO);

        Credencial existente = credencialDAO.buscarPorClienteId(clienteId);
        if (existente != null) {
            existente.setSalt(salt);
            existente.setPasswordHash(hash);
            existente.setIteraciones(SeguridadUtil.ITERACIONES_DEFECTO);
            return credencialDAO.actualizar(existente);
        } else {
            Credencial nueva = Credencial.paraCliente(clienteId, hash, salt, SeguridadUtil.ITERACIONES_DEFECTO);
            return credencialDAO.guardar(nueva);
        }
    }
}

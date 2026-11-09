package security;

import dao.CredencialDAO;
import dao.EmpleadoDAO;
import model.Credencial;
import model.Empleado;
import model.enums.Rol;

public class AutenticacionService {

    private final EmpleadoDAO empleadoDAO;
    private final CredencialDAO credencialDAO;

    public AutenticacionService() {
        this.empleadoDAO = new EmpleadoDAO();
        this.credencialDAO = new CredencialDAO();
    }

    public Empleado autenticar(String documento, char[] password) {
        if (documento == null || password == null || documento.trim().isEmpty()) {
            return null;
        }

        Empleado empleado = empleadoDAO.buscarPorDocumento(documento.trim());
        if (empleado == null) {
            System.out.println("[Seguridad] No existe ningún empleado con el documento: " + documento);
            return null;
        }

        Credencial credencial = credencialDAO.buscarPorEmpleadoId(empleado.getId());
        if (credencial == null) {
            System.out.println("[Seguridad] El empleado '" + empleado.getNombre() + "' no tiene credenciales activas.");
            return null;
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
            System.out.println("[Seguridad] Contraseña incorrecta para el usuario.");
            return null;
        }
    }

    public boolean tieneCredencialesConfiguradas() {
        return credencialDAO.contarCredenciales() > 0;
    }

    public boolean registrarCredencialGerente(String documento, char[] password) {
        if (documento == null || password == null || password.length == 0) {
            System.out.println("[Seguridad] Documento o contraseña inválidos.");
            return false;
        }

        Empleado emp = empleadoDAO.buscarPorDocumento(documento.trim());
        if (emp == null) {
            System.out.println("[Seguridad] No se encontró ningún empleado con el documento: " + documento);
            return false;
        }

        if (emp.getRol() != Rol.GERENTE) {
            System.out.println("[Seguridad] ACCESO DENEGADO: El empleado '" + emp.getNombre()
                               + "' tiene rol " + emp.getRol() + ", no es GERENTE.");
            return false;
        }

        if (credencialDAO.buscarPorEmpleadoId(emp.getId()) != null) {
            System.out.println("[Seguridad] El gerente ya tiene credenciales registradas.");
            return false;
        }

        String salt = SeguridadUtil.generarSalt();
        String hash = SeguridadUtil.hashearPassword(password, salt, SeguridadUtil.ITERACIONES_DEFECTO);

        Credencial cred = new Credencial(emp.getId(), hash, salt, SeguridadUtil.ITERACIONES_DEFECTO);
        return credencialDAO.guardar(cred);
    }

    public boolean asignarCredencialEmpleado(Integer empleadoId, char[] password) {
        if (empleadoId == null || password == null || password.length == 0) {
            return false;
        }

        Empleado emp = empleadoDAO.buscarPorId(empleadoId);
        if (emp == null) return false;

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
}

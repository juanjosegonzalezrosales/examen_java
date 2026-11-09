package dao;

import database.Conexion;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import model.Empleado;
import model.enums.Rol;

public class EmpleadoDAO {

    private final Conexion conexion;

    public EmpleadoDAO() {
        this.conexion = Conexion.getInstancia();
    }

    public boolean guardar(Empleado empleado) {
        if (empleado == null) {
            System.out.println("[EmpleadoDAO] Error: El empleado no puede ser nulo.");
            return false;
        }

        if (empleado.getRol() == null) {
            System.out.println("[EmpleadoDAO] Error: El empleado debe tener un rol asignado.");
            return false;
        }

        String sql = "INSERT INTO empleados (nombre, documento, rol_id, correo, salario) VALUES (?, ?, ?, ?, ?)";

        try (Connection con = conexion.conectar()) {
            Integer rolId = obtenerRolId(empleado.getRol(), con);
            if (rolId == null) {
                System.out.println("[EmpleadoDAO] Error: El rol '" + empleado.getRol() + "' no existe en el catálogo de roles.");
                return false;
            }

            try (PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, empleado.getNombre());
                ps.setString(2, empleado.getDocumento());
                ps.setInt(3, rolId);
                ps.setString(4, empleado.getCorreo());
                ps.setBigDecimal(5, empleado.getSalario() != null ? empleado.getSalario() : BigDecimal.ZERO);

                int filasAfectadas = ps.executeUpdate();
                if (filasAfectadas > 0) {
                    try (ResultSet rs = ps.getGeneratedKeys()) {
                        if (rs.next()) {
                            empleado.setId(rs.getInt(1));
                            return true;
                        }
                    }
                }
            }
        } catch (SQLException e) {
            if (e.getErrorCode() == 1062) {
                System.out.println("[EmpleadoDAO] Error: Ya existe un empleado con el documento '"
                                   + empleado.getDocumento() + "'.");
            } else {
                System.out.println("[EmpleadoDAO] Error SQL al guardar empleado: " + e.getMessage());
            }
        }
        return false;
    }

    public Empleado buscarPorId(Integer id) {
        if (id == null) {
            return null;
        }

        String sql = "SELECT e.id, e.nombre, e.documento, e.correo, e.salario, r.nombre AS rol_nombre "
                   + "FROM empleados e "
                   + "INNER JOIN roles r ON e.rol_id = r.id "
                   + "WHERE e.id = ?";

        try (Connection con = conexion.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapearEmpleado(rs);
                }
            }
        } catch (SQLException e) {
            System.out.println("[EmpleadoDAO] Error SQL al buscar empleado por ID: " + e.getMessage());
        }
        return null;
    }

    public Empleado buscarPorDocumento(String documento) {
        if (documento == null || documento.trim().isEmpty()) {
            return null;
        }

        String sql = "SELECT e.id, e.nombre, e.documento, e.correo, e.salario, r.nombre AS rol_nombre "
                   + "FROM empleados e "
                   + "INNER JOIN roles r ON e.rol_id = r.id "
                   + "WHERE e.documento = ?";

        try (Connection con = conexion.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, documento.trim());

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapearEmpleado(rs);
                }
            }
        } catch (SQLException e) {
            System.out.println("[EmpleadoDAO] Error SQL al buscar empleado por documento: " + e.getMessage());
        }
        return null;
    }

    public List<Empleado> listarTodos() {
        List<Empleado> empleados = new ArrayList<>();
        String sql = "SELECT e.id, e.nombre, e.documento, e.correo, e.salario, r.nombre AS rol_nombre "
                   + "FROM empleados e "
                   + "INNER JOIN roles r ON e.rol_id = r.id "
                   + "ORDER BY e.id ASC";

        try (Connection con = conexion.conectar();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                empleados.add(mapearEmpleado(rs));
            }
        } catch (SQLException e) {
            System.out.println("[EmpleadoDAO] Error SQL al listar empleados: " + e.getMessage());
        }
        return empleados;
    }

    public boolean actualizar(Empleado empleado) {
        if (empleado == null || empleado.getId() == null) {
            System.out.println("[EmpleadoDAO] Error: Empleado invalido o sin identificador para actualizar.");
            return false;
        }

        if (empleado.getRol() == null) {
            System.out.println("[EmpleadoDAO] Error: El empleado debe tener un rol asignado.");
            return false;
        }

        String sql = "UPDATE empleados SET nombre = ?, documento = ?, rol_id = ?, correo = ?, salario = ? WHERE id = ?";

        try (Connection con = conexion.conectar()) {
            Integer rolId = obtenerRolId(empleado.getRol(), con);
            if (rolId == null) {
                System.out.println("[EmpleadoDAO] Error: El rol '" + empleado.getRol() + "' no existe en el catálogo de roles.");
                return false;
            }

            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setString(1, empleado.getNombre());
                ps.setString(2, empleado.getDocumento());
                ps.setInt(3, rolId);
                ps.setString(4, empleado.getCorreo());
                ps.setBigDecimal(5, empleado.getSalario() != null ? empleado.getSalario() : BigDecimal.ZERO);
                ps.setInt(6, empleado.getId());

                int filasAfectadas = ps.executeUpdate();
                if (filasAfectadas > 0) {
                    return true;
                }

                return buscarPorId(empleado.getId()) != null;
            }
        } catch (SQLException e) {
            if (e.getErrorCode() == 1062) {
                System.out.println("[EmpleadoDAO] Error: El documento '" + empleado.getDocumento()
                                   + "' ya pertenece a otro empleado.");
            } else {
                System.out.println("[EmpleadoDAO] Error SQL al actualizar empleado: " + e.getMessage());
            }
        }
        return false;
    }

    public boolean eliminar(Integer id) {
        if (id == null) {
            return false;
        }

        String sql = "DELETE FROM empleados WHERE id = ?";

        try (Connection con = conexion.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, id);

            int filasAfectadas = ps.executeUpdate();
            return filasAfectadas > 0;

        } catch (SQLException e) {

            if (e.getErrorCode() == 1451) {
                System.out.println("[EmpleadoDAO] No se puede eliminar el empleado con ID " + id
                                   + " porque tiene solicitudes de credito vinculadas.");
            } else {
                System.out.println("[EmpleadoDAO] Error SQL al eliminar empleado: " + e.getMessage());
            }
        }
        return false;
    }

    private Integer obtenerRolId(Rol rol, Connection con) throws SQLException {
        if (rol == null) {
            return null;
        }
        String sql = "SELECT id FROM roles WHERE nombre = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, rol.name());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("id");
                }
            }
        }
        return null;
    }

    private Empleado mapearEmpleado(ResultSet rs) throws SQLException {
        Integer id = rs.getInt("id");
        String nombre = rs.getString("nombre");
        String documento = rs.getString("documento");
        String correo = rs.getString("correo");
        BigDecimal salario = rs.getBigDecimal("salario");
        String rolNombre = rs.getString("rol_nombre");
        Rol rol = Rol.valueOf(rolNombre);

        return new Empleado(id, nombre, documento, correo, rol, salario);
    }
}

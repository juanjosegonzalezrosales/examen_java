package dao;

import database.Conexion;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import model.Credencial;

public class CredencialDAO {

    private final Conexion conexion;

    public CredencialDAO() {
        this.conexion = Conexion.getInstancia();
    }

    public boolean guardar(Credencial credencial) {
        if (credencial == null || (credencial.getEmpleadoId() == null && credencial.getClienteId() == null)) {
            System.out.println("[CredencialDAO] Error: Credencial o ID de entidad invalido.");
            return false;
        }

        String sql = "INSERT INTO credenciales (empleado_id, cliente_id, password_hash, salt, iteraciones) VALUES (?, ?, ?, ?, ?)";

        try (Connection con = conexion.conectar();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            if (credencial.getEmpleadoId() != null) {
                ps.setInt(1, credencial.getEmpleadoId());
            } else {
                ps.setNull(1, java.sql.Types.INTEGER);
            }

            if (credencial.getClienteId() != null) {
                ps.setInt(2, credencial.getClienteId());
            } else {
                ps.setNull(2, java.sql.Types.INTEGER);
            }

            ps.setString(3, credencial.getPasswordHash());
            ps.setString(4, credencial.getSalt());
            ps.setInt(5, credencial.getIteraciones());

            int filas = ps.executeUpdate();
            if (filas > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        credencial.setId(rs.getInt(1));
                        return true;
                    }
                }
            }
        } catch (SQLException e) {
            if (e.getErrorCode() == 1062) {
                System.out.println("[CredencialDAO] El usuario ya posee credenciales registradas.");
            } else {
                System.out.println("[CredencialDAO] Error SQL al guardar credencial: " + e.getMessage());
            }
        }
        return false;
    }

    public Credencial buscarPorEmpleadoId(Integer empleadoId) {
        if (empleadoId == null) return null;

        String sql = "SELECT id, empleado_id, cliente_id, password_hash, salt, iteraciones, fecha_creacion, fecha_actualizacion "
                   + "FROM credenciales WHERE empleado_id = ?";

        try (Connection con = conexion.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, empleadoId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapearCredencial(rs);
                }
            }
        } catch (SQLException e) {
            System.out.println("[CredencialDAO] Error SQL al consultar credencial de empleado: " + e.getMessage());
        }
        return null;
    }

    public Credencial buscarPorClienteId(Integer clienteId) {
        if (clienteId == null) return null;

        String sql = "SELECT id, empleado_id, cliente_id, password_hash, salt, iteraciones, fecha_creacion, fecha_actualizacion "
                   + "FROM credenciales WHERE cliente_id = ?";

        try (Connection con = conexion.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, clienteId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapearCredencial(rs);
                }
            }
        } catch (SQLException e) {
            System.out.println("[CredencialDAO] Error SQL al consultar credencial de cliente: " + e.getMessage());
        }
        return null;
    }

    private Credencial mapearCredencial(ResultSet rs) throws SQLException {
        Integer empId = rs.getObject("empleado_id") != null ? rs.getInt("empleado_id") : null;
        Integer cliId = rs.getObject("cliente_id") != null ? rs.getInt("cliente_id") : null;
        Credencial c = new Credencial(
            rs.getInt("id"),
            empId,
            cliId,
            rs.getString("password_hash"),
            rs.getString("salt"),
            rs.getInt("iteraciones")
        );
        Timestamp fCre = rs.getTimestamp("fecha_creacion");
        Timestamp fAct = rs.getTimestamp("fecha_actualizacion");
        if (fCre != null) c.setFechaCreacion(fCre.toLocalDateTime());
        if (fAct != null) c.setFechaActualizacion(fAct.toLocalDateTime());
        return c;
    }

    public boolean actualizar(Credencial credencial) {
        if (credencial == null) return false;

        String sql;
        int idParam;
        if (credencial.getEmpleadoId() != null) {
            sql = "UPDATE credenciales SET password_hash = ?, salt = ?, iteraciones = ? WHERE empleado_id = ?";
            idParam = credencial.getEmpleadoId();
        } else if (credencial.getClienteId() != null) {
            sql = "UPDATE credenciales SET password_hash = ?, salt = ?, iteraciones = ? WHERE cliente_id = ?";
            idParam = credencial.getClienteId();
        } else {
            return false;
        }

        try (Connection con = conexion.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, credencial.getPasswordHash());
            ps.setString(2, credencial.getSalt());
            ps.setInt(3, credencial.getIteraciones());
            ps.setInt(4, idParam);

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("[CredencialDAO] Error SQL al actualizar credencial: " + e.getMessage());
        }
        return false;
    }

    public int contarCredenciales() {
        String sql = "SELECT count(*) FROM credenciales";
        try (Connection con = conexion.conectar();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.out.println("[CredencialDAO] Error SQL al contar credenciales: " + e.getMessage());
        }
        return 0;
    }
}

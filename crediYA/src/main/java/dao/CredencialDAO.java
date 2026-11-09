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
        if (credencial == null || credencial.getEmpleadoId() == null) {
            System.out.println("[CredencialDAO] Error: Credencial o ID de empleado inválido.");
            return false;
        }

        String sql = "INSERT INTO credenciales (empleado_id, password_hash, salt, iteraciones) VALUES (?, ?, ?, ?)";

        try (Connection con = conexion.conectar();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, credencial.getEmpleadoId());
            ps.setString(2, credencial.getPasswordHash());
            ps.setString(3, credencial.getSalt());
            ps.setInt(4, credencial.getIteraciones());

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
                System.out.println("[CredencialDAO] El empleado ya posee credenciales registradas.");
            } else {
                System.out.println("[CredencialDAO] Error SQL al guardar credencial: " + e.getMessage());
            }
        }
        return false;
    }

    public Credencial buscarPorEmpleadoId(Integer empleadoId) {
        if (empleadoId == null) return null;

        String sql = "SELECT id, empleado_id, password_hash, salt, iteraciones, fecha_creacion, fecha_actualizacion "
                   + "FROM credenciales WHERE empleado_id = ?";

        try (Connection con = conexion.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, empleadoId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Credencial c = new Credencial(
                        rs.getInt("id"),
                        rs.getInt("empleado_id"),
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
            }
        } catch (SQLException e) {
            System.out.println("[CredencialDAO] Error SQL al consultar credencial: " + e.getMessage());
        }
        return null;
    }

    public boolean actualizar(Credencial credencial) {
        if (credencial == null || credencial.getEmpleadoId() == null) return false;

        String sql = "UPDATE credenciales SET password_hash = ?, salt = ?, iteraciones = ? WHERE empleado_id = ?";

        try (Connection con = conexion.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, credencial.getPasswordHash());
            ps.setString(2, credencial.getSalt());
            ps.setInt(3, credencial.getIteraciones());
            ps.setInt(4, credencial.getEmpleadoId());

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

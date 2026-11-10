package dao;

import database.Conexion;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import model.Cliente;
import model.Empleado;
import model.Solicitud;
import model.enums.EstadoSolicitud;
import model.enums.ModalidadAmortizacion;

public class SolicitudDAO {

    private final Conexion conexion;
    private final ClienteDAO clienteDAO;
    private final EmpleadoDAO empleadoDAO;

    public SolicitudDAO() {
        this.conexion = Conexion.getInstancia();
        this.clienteDAO = new ClienteDAO();
        this.empleadoDAO = new EmpleadoDAO();
    }

    public boolean guardar(Solicitud solicitud) {
        if (solicitud == null || solicitud.getCliente() == null || solicitud.getEmpleado() == null) {
            System.out.println("[SolicitudDAO] Error: Solicitud, cliente o empleado no pueden ser nulos.");
            return false;
        }

        String sql = "INSERT INTO solicitudes (cliente_id, empleado_id, monto_solicitado, plazo_meses, modalidad, fecha_solicitud, estado, observaciones, fecha_resolucion) "
                   + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection con = conexion.conectar();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, solicitud.getCliente().getId());
            ps.setInt(2, solicitud.getEmpleado().getId());
            ps.setBigDecimal(3, solicitud.getMontoSolicitado());
            ps.setInt(4, solicitud.getPlazoMeses());
            ps.setString(5, solicitud.getModalidad().name());
            ps.setDate(6, Date.valueOf(solicitud.getFechaSolicitud() != null ? solicitud.getFechaSolicitud() : LocalDate.now()));
            ps.setString(7, solicitud.getEstado() != null ? solicitud.getEstado().name() : EstadoSolicitud.PENDIENTE.name());
            ps.setString(8, solicitud.getObservaciones());

            if (solicitud.getFechaResolucion() != null) {
                ps.setDate(9, Date.valueOf(solicitud.getFechaResolucion()));
            } else {
                ps.setNull(9, Types.DATE);
            }

            int filas = ps.executeUpdate();
            if (filas > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        solicitud.setId(rs.getInt(1));
                        return true;
                    }
                }
            }
        } catch (SQLException e) {
            System.out.println("[SolicitudDAO] Error SQL al guardar solicitud: " + e.getMessage());
        }
        return false;
    }

    public Solicitud buscarPorId(Integer id) {
        if (id == null) return null;
        String sql = "SELECT * FROM solicitudes WHERE id = ?";
        try (Connection con = conexion.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapearSolicitud(rs);
                }
            }
        } catch (SQLException e) {
            System.out.println("[SolicitudDAO] Error SQL al buscar solicitud por ID: " + e.getMessage());
        }
        return null;
    }

    public List<Solicitud> listarTodos() {
        List<Solicitud> lista = new ArrayList<>();
        String sql = "SELECT * FROM solicitudes ORDER BY id DESC";
        try (Connection con = conexion.conectar();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(mapearSolicitud(rs));
            }
        } catch (SQLException e) {
            System.out.println("[SolicitudDAO] Error SQL al listar solicitudes: " + e.getMessage());
        }
        return lista;
    }

    public List<Solicitud> listarPorEstado(EstadoSolicitud estado) {
        List<Solicitud> lista = new ArrayList<>();
        if (estado == null) return lista;
        String sql = "SELECT * FROM solicitudes WHERE estado = ? ORDER BY id DESC";
        try (Connection con = conexion.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, estado.name());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapearSolicitud(rs));
                }
            }
        } catch (SQLException e) {
            System.out.println("[SolicitudDAO] Error SQL al listar por estado: " + e.getMessage());
        }
        return lista;
    }

    public List<Solicitud> listarPorEmpleado(Integer empleadoId) {
        List<Solicitud> lista = new ArrayList<>();
        if (empleadoId == null) return lista;
        String sql = "SELECT * FROM solicitudes WHERE empleado_id = ? ORDER BY id DESC";
        try (Connection con = conexion.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, empleadoId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapearSolicitud(rs));
                }
            }
        } catch (SQLException e) {
            System.out.println("[SolicitudDAO] Error SQL al listar por empleado: " + e.getMessage());
        }
        return lista;
    }

    public boolean actualizar(Solicitud solicitud) {
        if (solicitud == null || solicitud.getId() == null) return false;

        String sql = "UPDATE solicitudes SET monto_solicitado = ?, plazo_meses = ?, modalidad = ?, estado = ?, observaciones = ?, fecha_resolucion = ? WHERE id = ?";
        try (Connection con = conexion.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setBigDecimal(1, solicitud.getMontoSolicitado());
            ps.setInt(2, solicitud.getPlazoMeses());
            ps.setString(3, solicitud.getModalidad().name());
            ps.setString(4, solicitud.getEstado().name());
            ps.setString(5, solicitud.getObservaciones());
            if (solicitud.getFechaResolucion() != null) {
                ps.setDate(6, Date.valueOf(solicitud.getFechaResolucion()));
            } else {
                ps.setNull(6, Types.DATE);
            }
            ps.setInt(7, solicitud.getId());

            int filas = ps.executeUpdate();
            return filas > 0;
        } catch (SQLException e) {
            System.out.println("[SolicitudDAO] Error SQL al actualizar solicitud: " + e.getMessage());
        }
        return false;
    }

    public boolean eliminar(Integer id) {
        if (id == null) return false;
        String sql = "DELETE FROM solicitudes WHERE id = ?";
        try (Connection con = conexion.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            if (e.getErrorCode() == 1451) {
                System.out.println("[SolicitudDAO] No se puede eliminar la solicitud porque ya tiene un prestamo asociado.");
            } else {
                System.out.println("[SolicitudDAO] Error SQL al eliminar solicitud: " + e.getMessage());
            }
        }
        return false;
    }

    private Solicitud mapearSolicitud(ResultSet rs) throws SQLException {
        Integer id = rs.getInt("id");
        int clienteId = rs.getInt("cliente_id");
        int empleadoId = rs.getInt("empleado_id");
        Cliente cliente = clienteDAO.buscarPorId(clienteId);
        Empleado empleado = empleadoDAO.buscarPorId(empleadoId);

        Date fSol = rs.getDate("fecha_solicitud");
        Date fRes = rs.getDate("fecha_resolucion");

        return new Solicitud(
            id,
            cliente,
            empleado,
            rs.getBigDecimal("monto_solicitado"),
            rs.getInt("plazo_meses"),
            ModalidadAmortizacion.valueOf(rs.getString("modalidad")),
            fSol != null ? fSol.toLocalDate() : null,
            EstadoSolicitud.valueOf(rs.getString("estado")),
            rs.getString("observaciones"),
            fRes != null ? fRes.toLocalDate() : null
        );
    }
}

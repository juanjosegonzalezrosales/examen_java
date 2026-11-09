package dao;

import database.Conexion;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import model.Cliente;

public class ClienteDAO {

    private final Conexion conexion;

    public ClienteDAO() {
        this.conexion = Conexion.getInstancia();
    }

    public boolean guardar(Cliente cliente) {
        if (cliente == null) {
            System.out.println("[ClienteDAO] Error: El cliente no puede ser nulo.");
            return false;
        }

        String sql = "INSERT INTO clientes (nombre, documento, correo, telefono) VALUES (?, ?, ?, ?)";

        try (Connection con = conexion.conectar();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, cliente.getNombre());
            ps.setString(2, cliente.getDocumento());
            ps.setString(3, cliente.getCorreo());
            ps.setString(4, cliente.getTelefono());

            int filasAfectadas = ps.executeUpdate();

            if (filasAfectadas > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        cliente.setId(rs.getInt(1));
                        return true;
                    }
                }
            }
        } catch (SQLException e) {
            if (e.getErrorCode() == 1062) {
                System.out.println("[ClienteDAO] Error: Ya existe un cliente con el documento '"
                                   + cliente.getDocumento() + "'.");
            } else {
                System.out.println("[ClienteDAO] Error SQL al guardar cliente: " + e.getMessage());
            }
        }
        return false;
    }

    public Cliente buscarPorId(Integer id) {
        if (id == null) {
            return null;
        }

        String sql = "SELECT id, nombre, documento, correo, telefono FROM clientes WHERE id = ?";

        try (Connection con = conexion.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapearCliente(rs);
                }
            }
        } catch (SQLException e) {
            System.out.println("[ClienteDAO] Error SQL al buscar cliente por ID: " + e.getMessage());
        }
        return null;
    }

    public Cliente buscarPorDocumento(String documento) {
        if (documento == null || documento.trim().isEmpty()) {
            return null;
        }

        String sql = "SELECT id, nombre, documento, correo, telefono FROM clientes WHERE documento = ?";

        try (Connection con = conexion.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, documento.trim());

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapearCliente(rs);
                }
            }
        } catch (SQLException e) {
            System.out.println("[ClienteDAO] Error SQL al buscar cliente por documento: " + e.getMessage());
        }
        return null;
    }

    public List<Cliente> listarTodos() {
        List<Cliente> clientes = new ArrayList<>();
        String sql = "SELECT id, nombre, documento, correo, telefono FROM clientes ORDER BY id ASC";

        try (Connection con = conexion.conectar();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                clientes.add(mapearCliente(rs));
            }
        } catch (SQLException e) {
            System.out.println("[ClienteDAO] Error SQL al listar clientes: " + e.getMessage());
        }
        return clientes;
    }

    public boolean actualizar(Cliente cliente) {
        if (cliente == null || cliente.getId() == null) {
            System.out.println("[ClienteDAO] Error: Cliente invalido o sin identificador para actualizar.");
            return false;
        }

        String sql = "UPDATE clientes SET nombre = ?, documento = ?, correo = ?, telefono = ? WHERE id = ?";

        try (Connection con = conexion.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, cliente.getNombre());
            ps.setString(2, cliente.getDocumento());
            ps.setString(3, cliente.getCorreo());
            ps.setString(4, cliente.getTelefono());
            ps.setInt(5, cliente.getId());

            int filasAfectadas = ps.executeUpdate();
            if (filasAfectadas > 0) {
                return true;
            }

            return buscarPorId(cliente.getId()) != null;

        } catch (SQLException e) {
            if (e.getErrorCode() == 1062) {
                System.out.println("[ClienteDAO] Error: El documento '" + cliente.getDocumento()
                                   + "' ya pertenece a otro cliente.");
            } else {
                System.out.println("[ClienteDAO] Error SQL al actualizar cliente: " + e.getMessage());
            }
        }
        return false;
    }

    public boolean eliminar(Integer id) {
        if (id == null) {
            return false;
        }

        String sql = "DELETE FROM clientes WHERE id = ?";

        try (Connection con = conexion.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, id);

            int filasAfectadas = ps.executeUpdate();
            return filasAfectadas > 0;

        } catch (SQLException e) {

            if (e.getErrorCode() == 1451) {
                System.out.println("[ClienteDAO] No se puede eliminar el cliente con ID " + id
                                   + " porque tiene solicitudes o prestamos vinculados en la base de datos.");
            } else {
                System.out.println("[ClienteDAO] Error SQL al eliminar cliente: " + e.getMessage());
            }
        }
        return false;
    }

    private Cliente mapearCliente(ResultSet rs) throws SQLException {
        Integer id = rs.getInt("id");
        String nombre = rs.getString("nombre");
        String documento = rs.getString("documento");
        String correo = rs.getString("correo");
        String telefono = rs.getString("telefono");

        return new Cliente(id, nombre, documento, correo, telefono);
    }
}

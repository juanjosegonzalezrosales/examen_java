package dao;

import database.Conexion;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import model.Cuota;
import model.Prestamo;
import model.Solicitud;
import model.enums.EstadoCuota;
import model.enums.EstadoPrestamo;
import model.enums.EstadoSolicitud;
import model.enums.ModalidadAmortizacion;

public class PrestamoDAO {

    private final Conexion conexion;
    private final SolicitudDAO solicitudDAO;

    public PrestamoDAO() {
        this.conexion = Conexion.getInstancia();
        this.solicitudDAO = new SolicitudDAO();
    }

    public boolean crearPrestamo(Prestamo prestamo) {
        if (prestamo == null || prestamo.getSolicitud() == null) {
            System.out.println("[PrestamoDAO] Error: El préstamo y su solicitud no pueden ser nulos.");
            return false;
        }

        Solicitud sol = prestamo.getSolicitud();
        if (sol.getEstado() != EstadoSolicitud.APROBADA) {
            System.out.println("[PrestamoDAO] Error: Solo se pueden originar préstamos de solicitudes APROBADAS.");
            return false;
        }

        if (buscarPorSolicitudId(sol.getId()) != null) {
            System.out.println("[PrestamoDAO] Error: Ya existe un contrato de préstamo para la solicitud #" + sol.getId());
            return false;
        }

        calcularPlanAmortizacion(prestamo);

        String sqlPrestamo = "INSERT INTO prestamos (solicitud_id, monto_capital, tasa_interes_anual, plazo_cuotas, "
                           + "modalidad, fecha_inicio, monto_total, valor_cuota_referencia, saldo_pendiente, estado) "
                           + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        String sqlCuota = "INSERT INTO cuotas (prestamo_id, version_plan, numero_cuota, fecha_vencimiento, "
                        + "valor_cuota, valor_capital, valor_interes, saldo_restante_capital, valor_pagado, "
                        + "capital_pagado, interes_pagado, vigente, estado) "
                        + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, 0.00, 0.00, 0.00, TRUE, 'PENDIENTE')";

        try (Connection con = conexion.conectar()) {
            con.setAutoCommit(false);
            try {

                try (PreparedStatement psP = con.prepareStatement(sqlPrestamo, Statement.RETURN_GENERATED_KEYS)) {
                    psP.setInt(1, sol.getId());
                    psP.setBigDecimal(2, prestamo.getMontoCapital());
                    psP.setBigDecimal(3, prestamo.getTasaInteresAnual());
                    psP.setInt(4, prestamo.getPlazoCuotas());
                    psP.setString(5, prestamo.getModalidad().name());
                    psP.setDate(6, Date.valueOf(prestamo.getFechaInicio() != null ? prestamo.getFechaInicio() : LocalDate.now()));
                    psP.setBigDecimal(7, prestamo.getMontoTotal());
                    psP.setBigDecimal(8, prestamo.getValorCuotaReferencia());
                    psP.setBigDecimal(9, prestamo.getSaldoPendiente());
                    psP.setString(10, EstadoPrestamo.ACTIVO.name());

                    psP.executeUpdate();
                    try (ResultSet rs = psP.getGeneratedKeys()) {
                        if (rs.next()) {
                            prestamo.setId(rs.getInt(1));
                        } else {
                            throw new SQLException("No se obtuvo el ID generado para el préstamo.");
                        }
                    }
                }

                try (PreparedStatement psC = con.prepareStatement(sqlCuota)) {
                    for (Cuota c : prestamo.getCuotas()) {
                        psC.setInt(1, prestamo.getId());
                        psC.setInt(2, c.getVersionPlan());
                        psC.setInt(3, c.getNumeroCuota());
                        psC.setDate(4, Date.valueOf(c.getFechaVencimiento()));
                        psC.setBigDecimal(5, c.getValorCuota());
                        psC.setBigDecimal(6, c.getValorCapital());
                        psC.setBigDecimal(7, c.getValorInteres());
                        psC.setBigDecimal(8, c.getSaldoRestanteCapital());
                        psC.addBatch();
                    }
                    psC.executeBatch();
                }

                con.commit();
                return true;

            } catch (SQLException e) {
                con.rollback();
                System.out.println("[PrestamoDAO] Error en transacción al crear préstamo: " + e.getMessage());
            } finally {
                con.setAutoCommit(true);
            }
        } catch (SQLException e) {
            System.out.println("[PrestamoDAO] Error de conexión: " + e.getMessage());
        }
        return false;
    }

    public Prestamo buscarPorId(Integer id) {
        if (id == null) return null;
        String sql = "SELECT * FROM prestamos WHERE id = ?";
        try (Connection con = conexion.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Prestamo p = mapearPrestamo(rs);
                    p.setCuotas(listarCuotasVigentes(p.getId(), con));
                    return p;
                }
            }
        } catch (SQLException e) {
            System.out.println("[PrestamoDAO] Error SQL al buscar préstamo por ID: " + e.getMessage());
        }
        return null;
    }

    public Prestamo buscarPorSolicitudId(Integer solicitudId) {
        if (solicitudId == null) return null;
        String sql = "SELECT * FROM prestamos WHERE solicitud_id = ?";
        try (Connection con = conexion.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, solicitudId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Prestamo p = mapearPrestamo(rs);
                    p.setCuotas(listarCuotasVigentes(p.getId(), con));
                    return p;
                }
            }
        } catch (SQLException e) {
            System.out.println("[PrestamoDAO] Error SQL al buscar por solicitud: " + e.getMessage());
        }
        return null;
    }

    public List<Prestamo> listarTodos() {
        List<Prestamo> lista = new ArrayList<>();
        String sql = "SELECT * FROM prestamos ORDER BY id DESC";
        try (Connection con = conexion.conectar();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(mapearPrestamo(rs));
            }
        } catch (SQLException e) {
            System.out.println("[PrestamoDAO] Error SQL al listar préstamos: " + e.getMessage());
        }
        return lista;
    }

    public List<Prestamo> listarPorCliente(Integer clienteId) {
        List<Prestamo> lista = new ArrayList<>();
        if (clienteId == null) return lista;
        String sql = "SELECT p.* FROM prestamos p "
                   + "INNER JOIN solicitudes s ON p.solicitud_id = s.id "
                   + "WHERE s.cliente_id = ? ORDER BY p.id DESC";
        try (Connection con = conexion.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, clienteId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapearPrestamo(rs));
                }
            }
        } catch (SQLException e) {
            System.out.println("[PrestamoDAO] Error SQL al listar préstamos por cliente: " + e.getMessage());
        }
        return lista;
    }

    public List<Prestamo> listarPorEmpleado(Integer empleadoId) {
        List<Prestamo> lista = new ArrayList<>();
        if (empleadoId == null) return lista;
        String sql = "SELECT p.* FROM prestamos p "
                   + "INNER JOIN solicitudes s ON p.solicitud_id = s.id "
                   + "WHERE s.empleado_id = ? ORDER BY p.id DESC";
        try (Connection con = conexion.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, empleadoId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapearPrestamo(rs));
                }
            }
        } catch (SQLException e) {
            System.out.println("[PrestamoDAO] Error SQL al listar préstamos por empleado: " + e.getMessage());
        }
        return lista;
    }

    public List<Cuota> listarCuotas(Integer prestamoId) {
        List<Cuota> cuotas = new ArrayList<>();
        if (prestamoId == null) return cuotas;
        try (Connection con = conexion.conectar()) {
            return listarCuotasVigentes(prestamoId, con);
        } catch (SQLException e) {
            System.out.println("[PrestamoDAO] Error al consultar cuotas: " + e.getMessage());
        }
        return cuotas;
    }

    public boolean actualizarSaldoYEstado(Integer prestamoId, BigDecimal nuevoSaldo, EstadoPrestamo nuevoEstado, Connection con) throws SQLException {
        String sql = "UPDATE prestamos SET saldo_pendiente = ?, estado = ? WHERE id = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setBigDecimal(1, nuevoSaldo);
            ps.setString(2, nuevoEstado.name());
            ps.setInt(3, prestamoId);
            return ps.executeUpdate() > 0;
        }
    }

    private List<Cuota> listarCuotasVigentes(Integer prestamoId, Connection con) throws SQLException {
        List<Cuota> cuotas = new ArrayList<>();
        String sql = "SELECT * FROM cuotas WHERE prestamo_id = ? AND vigente = TRUE ORDER BY numero_cuota ASC";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, prestamoId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Date vto = rs.getDate("fecha_vencimiento");
                    Cuota c = new Cuota(
                        rs.getInt("id"),
                        rs.getInt("prestamo_id"),
                        rs.getInt("version_plan"),
                        rs.getInt("numero_cuota"),
                        vto != null ? vto.toLocalDate() : null,
                        rs.getBigDecimal("valor_cuota"),
                        rs.getBigDecimal("valor_capital"),
                        rs.getBigDecimal("valor_interes"),
                        rs.getBigDecimal("saldo_restante_capital"),
                        rs.getBigDecimal("valor_pagado"),
                        rs.getBigDecimal("capital_pagado"),
                        rs.getBigDecimal("interes_pagado"),
                        rs.getBoolean("vigente"),
                        EstadoCuota.valueOf(rs.getString("estado"))
                    );
                    cuotas.add(c);
                }
            }
        }
        return cuotas;
    }

    private Prestamo mapearPrestamo(ResultSet rs) throws SQLException {
        Integer id = rs.getInt("id");
        int solId = rs.getInt("solicitud_id");
        Solicitud sol = solicitudDAO.buscarPorId(solId);
        Date fIni = rs.getDate("fecha_inicio");

        return new Prestamo(
            id,
            sol,
            rs.getBigDecimal("monto_capital"),
            rs.getBigDecimal("tasa_interes_anual"),
            rs.getInt("plazo_cuotas"),
            ModalidadAmortizacion.valueOf(rs.getString("modalidad")),
            fIni != null ? fIni.toLocalDate() : null,
            rs.getBigDecimal("monto_total"),
            rs.getBigDecimal("valor_cuota_referencia"),
            rs.getBigDecimal("saldo_pendiente"),
            EstadoPrestamo.valueOf(rs.getString("estado"))
        );
    }

    private void calcularPlanAmortizacion(Prestamo p) {
        BigDecimal P = p.getMontoCapital();
        int n = p.getPlazoCuotas();
        LocalDate fechaInicio = p.getFechaInicio() != null ? p.getFechaInicio() : LocalDate.now();
        BigDecimal tasaAnual = p.getTasaInteresAnual();

        BigDecimal i = tasaAnual.divide(BigDecimal.valueOf(1200), 10, RoundingMode.HALF_UP);

        List<Cuota> cuotas = new ArrayList<>();
        BigDecimal saldoRestante = P;
        BigDecimal sumaTotal = BigDecimal.ZERO;

        if (p.getModalidad() == ModalidadAmortizacion.FRANCES) {

            BigDecimal C;
            if (i.compareTo(BigDecimal.ZERO) == 0) {
                C = P.divide(BigDecimal.valueOf(n), 2, RoundingMode.HALF_UP);
            } else {

                double factor = 1.0 - Math.pow(1.0 + i.doubleValue(), -n);
                C = P.multiply(i).divide(BigDecimal.valueOf(factor), 2, RoundingMode.HALF_UP);
            }

            p.setValorCuotaReferencia(C);

            for (int k = 1; k <= n; k++) {
                BigDecimal interes = saldoRestante.multiply(i).setScale(2, RoundingMode.HALF_UP);
                BigDecimal capital;
                if (k == n) {
                    capital = saldoRestante;
                    C = capital.add(interes);
                } else {
                    capital = C.subtract(interes).min(saldoRestante);
                }
                saldoRestante = saldoRestante.subtract(capital).max(BigDecimal.ZERO);
                sumaTotal = sumaTotal.add(C);

                LocalDate vto = fechaInicio.plusMonths(k);
                Cuota cuota = new Cuota(null, 1, k, vto, C, capital, interes, saldoRestante);
                cuotas.add(cuota);
            }
        } else {

            BigDecimal A = P.divide(BigDecimal.valueOf(n), 2, RoundingMode.HALF_UP);

            for (int k = 1; k <= n; k++) {
                BigDecimal interes = saldoRestante.multiply(i).setScale(2, RoundingMode.HALF_UP);
                BigDecimal capital = (k == n) ? saldoRestante : A;
                BigDecimal cuotaTotal = capital.add(interes);

                if (k == 1) {
                    p.setValorCuotaReferencia(cuotaTotal);
                }

                saldoRestante = saldoRestante.subtract(capital).max(BigDecimal.ZERO);
                sumaTotal = sumaTotal.add(cuotaTotal);

                LocalDate vto = fechaInicio.plusMonths(k);
                Cuota cuota = new Cuota(null, 1, k, vto, cuotaTotal, capital, interes, saldoRestante);
                cuotas.add(cuota);
            }
        }

        p.setMontoTotal(sumaTotal);
        p.setSaldoPendiente(P);
        p.setCuotas(cuotas);
        p.setEstado(EstadoPrestamo.ACTIVO);
    }
}

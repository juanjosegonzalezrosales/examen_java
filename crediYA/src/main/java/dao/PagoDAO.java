package dao;

import database.Conexion;
import exception.ValidacionException;
import java.math.BigDecimal;
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
import model.Pago;
import model.enums.EstadoCuota;
import model.enums.EstadoPrestamo;
import model.enums.OpcionAbonoExtra;
import model.enums.TipoPago;

public class PagoDAO {

    private final Conexion conexion;

    public PagoDAO() {
        this.conexion = Conexion.getInstancia();
    }

    public boolean registrarPago(Pago pago) throws ValidacionException {
        if (pago == null || pago.getPrestamoId() == null) {
            throw new ValidacionException("El registro de pago y su prestamo asociado no pueden ser nulos.");
        }

        if (pago.getMonto() == null || pago.getMonto().compareTo(BigDecimal.ZERO) <= 0) {
            throw new ValidacionException("El monto a pagar debe ser mayor a cero.");
        }

        if (!pago.validarConsistenciaMonto()) {
            throw new ValidacionException("El monto total ($" + pago.getMonto()
                + ") no coincide con el desglose (mora: $" + pago.getMontoMora()
                + " + interes: $" + pago.getMontoInteres()
                + " + capital: $" + pago.getMontoCapital() + ").");
        }

        String sqlInsertPago = "INSERT INTO pagos (prestamo_id, cuota_id, fecha_pago, monto, monto_mora, "
                             + "monto_interes, monto_capital, tipo_pago, opcion_abono, observaciones) "
                             + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        String sqlUpdateCuota = "UPDATE cuotas SET capital_pagado = capital_pagado + ?, "
                              + "interes_pagado = interes_pagado + ?, "
                              + "valor_pagado = valor_pagado + ?, "
                              + "estado = CASE WHEN (capital_pagado + ?) >= valor_capital AND (interes_pagado + ?) >= valor_interes "
                              + "THEN 'PAGADA' ELSE estado END "
                              + "WHERE id = ? AND prestamo_id = ?";

        String sqlConsultarSaldoPrestamo = "SELECT saldo_pendiente FROM prestamos WHERE id = ? FOR UPDATE";
        String sqlUpdatePrestamo = "UPDATE prestamos SET saldo_pendiente = ?, estado = ? WHERE id = ?";

        try (Connection con = conexion.conectar()) {
            con.setAutoCommit(false);
            try {

                try (PreparedStatement psP = con.prepareStatement(sqlInsertPago, Statement.RETURN_GENERATED_KEYS)) {
                    psP.setInt(1, pago.getPrestamoId());
                    if (pago.getCuotaId() != null) {
                        psP.setInt(2, pago.getCuotaId());
                    } else {
                        psP.setNull(2, Types.INTEGER);
                    }
                    psP.setDate(3, Date.valueOf(pago.getFechaPago() != null ? pago.getFechaPago() : LocalDate.now()));
                    psP.setBigDecimal(4, pago.getMonto());
                    psP.setBigDecimal(5, pago.getMontoMora());
                    psP.setBigDecimal(6, pago.getMontoInteres());
                    psP.setBigDecimal(7, pago.getMontoCapital());
                    psP.setString(8, pago.getTipoPago().name());

                    if (pago.getOpcionAbono() != null) {
                        psP.setString(9, pago.getOpcionAbono().name());
                    } else {
                        psP.setNull(9, Types.VARCHAR);
                    }
                    psP.setString(10, pago.getObservaciones());

                    psP.executeUpdate();
                    try (ResultSet rs = psP.getGeneratedKeys()) {
                        if (rs.next()) {
                            pago.setId(rs.getInt(1));
                        }
                    }
                }

                if (pago.getTipoPago() == TipoPago.CUOTA_PROGRAMADA && pago.getCuotaId() != null) {
                    try (PreparedStatement psC = con.prepareStatement(sqlUpdateCuota)) {
                        BigDecimal cap = pago.getMontoCapital();
                        BigDecimal inte = pago.getMontoInteres();
                        BigDecimal totCuota = cap.add(inte);

                        psC.setBigDecimal(1, cap);
                        psC.setBigDecimal(2, inte);
                        psC.setBigDecimal(3, totCuota);
                        psC.setBigDecimal(4, cap);
                        psC.setBigDecimal(5, inte);
                        psC.setInt(6, pago.getCuotaId());
                        psC.setInt(7, pago.getPrestamoId());
                        psC.executeUpdate();
                    }
                }

                BigDecimal saldoActual = BigDecimal.ZERO;
                try (PreparedStatement psS = con.prepareStatement(sqlConsultarSaldoPrestamo)) {
                    psS.setInt(1, pago.getPrestamoId());
                    try (ResultSet rs = psS.executeQuery()) {
                        if (rs.next()) {
                            saldoActual = rs.getBigDecimal("saldo_pendiente");
                        }
                    }
                }

                BigDecimal nuevoSaldo = saldoActual.subtract(pago.getMontoCapital()).max(BigDecimal.ZERO);
                EstadoPrestamo nuevoEstado = (nuevoSaldo.compareTo(BigDecimal.ZERO) == 0)
                        ? EstadoPrestamo.PAGADO
                        : EstadoPrestamo.ACTIVO;

                try (PreparedStatement psUpP = con.prepareStatement(sqlUpdatePrestamo)) {
                    psUpP.setBigDecimal(1, nuevoSaldo);
                    psUpP.setString(2, nuevoEstado.name());
                    psUpP.setInt(3, pago.getPrestamoId());
                    psUpP.executeUpdate();
                }

                con.commit();
                return true;

            } catch (SQLException e) {
                con.rollback();
                System.out.println("[PagoDAO] Error en transaccion al registrar pago: " + e.getMessage());
            } finally {
                con.setAutoCommit(true);
            }
        } catch (SQLException e) {
            System.out.println("[PagoDAO] Error de conexion: " + e.getMessage());
        }
        return false;
    }

    public List<Pago> listarPorPrestamo(Integer prestamoId) {
        List<Pago> lista = new ArrayList<>();
        if (prestamoId == null) return lista;
        String sql = "SELECT * FROM pagos WHERE prestamo_id = ? ORDER BY id DESC";
        try (Connection con = conexion.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, prestamoId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapearPago(rs));
                }
            }
        } catch (SQLException e) {
            System.out.println("[PagoDAO] Error SQL al listar pagos por prestamo: " + e.getMessage());
        }
        return lista;
    }

    public List<Pago> listarTodos() {
        List<Pago> lista = new ArrayList<>();
        String sql = "SELECT * FROM pagos ORDER BY id DESC";
        try (Connection con = conexion.conectar();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(mapearPago(rs));
            }
        } catch (SQLException e) {
            System.out.println("[PagoDAO] Error SQL al listar todos los pagos: " + e.getMessage());
        }
        return lista;
    }

    private Pago mapearPago(ResultSet rs) throws SQLException {
        Integer id = rs.getInt("id");
        int prestamoId = rs.getInt("prestamo_id");
        int cId = rs.getInt("cuota_id");
        Integer cuotaId = rs.wasNull() ? null : cId;
        Date fPago = rs.getDate("fecha_pago");
        String opAbono = rs.getString("opcion_abono");

        return new Pago(
            id,
            prestamoId,
            cuotaId,
            fPago != null ? fPago.toLocalDate() : null,
            rs.getBigDecimal("monto"),
            rs.getBigDecimal("monto_mora"),
            rs.getBigDecimal("monto_interes"),
            rs.getBigDecimal("monto_capital"),
            TipoPago.valueOf(rs.getString("tipo_pago")),
            opAbono != null ? OpcionAbonoExtra.valueOf(opAbono) : null,
            rs.getString("observaciones")
        );
    }
}

package model;

import java.math.BigDecimal;
import java.time.LocalDate;
import model.enums.OpcionAbonoExtra;
import model.enums.TipoPago;

public class Pago implements java.io.Serializable {

    private static final long serialVersionUID = 1L;

    private Integer id;
    private Integer prestamoId;
    private Integer cuotaId;
    private LocalDate fechaPago;
    private BigDecimal monto;
    private BigDecimal montoMora;
    private BigDecimal montoInteres;
    private BigDecimal montoCapital;
    private TipoPago tipoPago;
    private OpcionAbonoExtra opcionAbono;
    private String observaciones;

    public Pago() {
        this.montoMora = BigDecimal.ZERO;
        this.montoInteres = BigDecimal.ZERO;
        this.montoCapital = BigDecimal.ZERO;
    }

    public Pago(Integer id, Integer prestamoId, Integer cuotaId, LocalDate fechaPago,
                BigDecimal monto, BigDecimal montoMora, BigDecimal montoInteres,
                BigDecimal montoCapital, TipoPago tipoPago, OpcionAbonoExtra opcionAbono,
                String observaciones) {
        this.id = id;
        this.prestamoId = prestamoId;
        this.cuotaId = cuotaId;
        this.fechaPago = fechaPago;
        this.monto = monto;
        this.montoMora = (montoMora != null) ? montoMora : BigDecimal.ZERO;
        this.montoInteres = (montoInteres != null) ? montoInteres : BigDecimal.ZERO;
        this.montoCapital = (montoCapital != null) ? montoCapital : BigDecimal.ZERO;
        this.tipoPago = tipoPago;
        this.opcionAbono = opcionAbono;
        this.observaciones = observaciones;
    }

    public Pago(Integer prestamoId, Integer cuotaId, LocalDate fechaPago,
                BigDecimal monto, BigDecimal montoMora, BigDecimal montoInteres,
                BigDecimal montoCapital, TipoPago tipoPago, OpcionAbonoExtra opcionAbono,
                String observaciones) {
        this(null, prestamoId, cuotaId, fechaPago, monto, montoMora,
             montoInteres, montoCapital, tipoPago, opcionAbono, observaciones);
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getPrestamoId() {
        return prestamoId;
    }

    public void setPrestamoId(Integer prestamoId) {
        this.prestamoId = prestamoId;
    }

    public Integer getCuotaId() {
        return cuotaId;
    }

    public void setCuotaId(Integer cuotaId) {
        this.cuotaId = cuotaId;
    }

    public LocalDate getFechaPago() {
        return fechaPago;
    }

    public void setFechaPago(LocalDate fechaPago) {
        this.fechaPago = fechaPago;
    }

    public BigDecimal getMonto() {
        return monto;
    }

    public void setMonto(BigDecimal monto) {
        this.monto = monto;
    }

    public BigDecimal getMontoMora() {
        return montoMora;
    }

    public void setMontoMora(BigDecimal montoMora) {
        this.montoMora = montoMora;
    }

    public BigDecimal getMontoInteres() {
        return montoInteres;
    }

    public void setMontoInteres(BigDecimal montoInteres) {
        this.montoInteres = montoInteres;
    }

    public BigDecimal getMontoCapital() {
        return montoCapital;
    }

    public void setMontoCapital(BigDecimal montoCapital) {
        this.montoCapital = montoCapital;
    }

    public TipoPago getTipoPago() {
        return tipoPago;
    }

    public void setTipoPago(TipoPago tipoPago) {
        this.tipoPago = tipoPago;
    }

    public OpcionAbonoExtra getOpcionAbono() {
        return opcionAbono;
    }

    public void setOpcionAbono(OpcionAbonoExtra opcionAbono) {
        this.opcionAbono = opcionAbono;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }

    public boolean validarConsistenciaMonto() {
        if (monto == null || montoMora == null || montoInteres == null || montoCapital == null) {
            return false;
        }
        return monto.compareTo(montoMora.add(montoInteres).add(montoCapital)) == 0;
    }

    public boolean esValidoSegunTipo() {
        if (tipoPago == null || !validarConsistenciaMonto()) {
            return false;
        }
        switch (tipoPago) {
            case CUOTA_PROGRAMADA:
                return cuotaId != null && opcionAbono == null;
            case ABONO_EXTRAORDINARIO:
                return cuotaId == null
                        && opcionAbono != null
                        && montoMora.compareTo(BigDecimal.ZERO) == 0
                        && montoInteres.compareTo(BigDecimal.ZERO) == 0
                        && montoCapital.compareTo(monto) == 0;
            case PAGO_TOTAL:
                return cuotaId == null && opcionAbono == null;
            default:
                return false;
        }
    }

    @Override
    public String toString() {
        return "Pago{" +
                "id=" + id +
                ", prestamoId=" + prestamoId +
                ", cuotaId=" + cuotaId +
                ", fecha=" + fechaPago +
                ", monto=" + monto +
                ", mora=" + montoMora +
                ", int=" + montoInteres +
                ", cap=" + montoCapital +
                ", tipo=" + tipoPago +
                ", opcion=" + opcionAbono +
                '}';
    }
}

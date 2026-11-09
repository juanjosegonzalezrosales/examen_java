package model;

import java.math.BigDecimal;
import java.time.LocalDate;
import model.enums.EstadoCuota;

public class Cuota implements java.io.Serializable {

    private static final long serialVersionUID = 1L;

    private Integer id;
    private Integer prestamoId;
    private Integer versionPlan;
    private Integer numeroCuota;
    private LocalDate fechaVencimiento;
    private BigDecimal valorCuota;
    private BigDecimal valorCapital;
    private BigDecimal valorInteres;
    private BigDecimal saldoRestanteCapital;
    private BigDecimal valorPagado;
    private BigDecimal capitalPagado;
    private BigDecimal interesPagado;
    private boolean vigente;
    private EstadoCuota estado;

    public Cuota() {
        this.versionPlan = 1;
        this.valorPagado = BigDecimal.ZERO;
        this.capitalPagado = BigDecimal.ZERO;
        this.interesPagado = BigDecimal.ZERO;
        this.vigente = true;
        this.estado = EstadoCuota.PENDIENTE;
    }

    public Cuota(Integer id, Integer prestamoId, Integer versionPlan, Integer numeroCuota,
                 LocalDate fechaVencimiento, BigDecimal valorCuota, BigDecimal valorCapital,
                 BigDecimal valorInteres, BigDecimal saldoRestanteCapital, BigDecimal valorPagado,
                 BigDecimal capitalPagado, BigDecimal interesPagado, boolean vigente, EstadoCuota estado) {
        this.id = id;
        this.prestamoId = prestamoId;
        this.versionPlan = versionPlan;
        this.numeroCuota = numeroCuota;
        this.fechaVencimiento = fechaVencimiento;
        this.valorCuota = valorCuota;
        this.valorCapital = valorCapital;
        this.valorInteres = valorInteres;
        this.saldoRestanteCapital = saldoRestanteCapital;
        this.valorPagado = (valorPagado != null) ? valorPagado : BigDecimal.ZERO;
        this.capitalPagado = (capitalPagado != null) ? capitalPagado : BigDecimal.ZERO;
        this.interesPagado = (interesPagado != null) ? interesPagado : BigDecimal.ZERO;
        this.vigente = vigente;
        this.estado = (estado != null) ? estado : EstadoCuota.PENDIENTE;
    }

    public Cuota(Integer prestamoId, Integer versionPlan, Integer numeroCuota,
                 LocalDate fechaVencimiento, BigDecimal valorCuota, BigDecimal valorCapital,
                 BigDecimal valorInteres, BigDecimal saldoRestanteCapital) {
        this(null, prestamoId, versionPlan, numeroCuota, fechaVencimiento, valorCuota,
             valorCapital, valorInteres, saldoRestanteCapital, BigDecimal.ZERO,
             BigDecimal.ZERO, BigDecimal.ZERO, true, EstadoCuota.PENDIENTE);
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

    public Integer getVersionPlan() {
        return versionPlan;
    }

    public void setVersionPlan(Integer versionPlan) {
        this.versionPlan = versionPlan;
    }

    public Integer getNumeroCuota() {
        return numeroCuota;
    }

    public void setNumeroCuota(Integer numeroCuota) {
        this.numeroCuota = numeroCuota;
    }

    public LocalDate getFechaVencimiento() {
        return fechaVencimiento;
    }

    public void setFechaVencimiento(LocalDate fechaVencimiento) {
        this.fechaVencimiento = fechaVencimiento;
    }

    public BigDecimal getValorCuota() {
        return valorCuota;
    }

    public void setValorCuota(BigDecimal valorCuota) {
        this.valorCuota = valorCuota;
    }

    public BigDecimal getValorCapital() {
        return valorCapital;
    }

    public void setValorCapital(BigDecimal valorCapital) {
        this.valorCapital = valorCapital;
    }

    public BigDecimal getValorInteres() {
        return valorInteres;
    }

    public void setValorInteres(BigDecimal valorInteres) {
        this.valorInteres = valorInteres;
    }

    public BigDecimal getSaldoRestanteCapital() {
        return saldoRestanteCapital;
    }

    public void setSaldoRestanteCapital(BigDecimal saldoRestanteCapital) {
        this.saldoRestanteCapital = saldoRestanteCapital;
    }

    public BigDecimal getValorPagado() {
        return valorPagado;
    }

    public void setValorPagado(BigDecimal valorPagado) {
        this.valorPagado = valorPagado;
    }

    public BigDecimal getCapitalPagado() {
        return capitalPagado;
    }

    public void setCapitalPagado(BigDecimal capitalPagado) {
        this.capitalPagado = capitalPagado;
    }

    public BigDecimal getInteresPagado() {
        return interesPagado;
    }

    public void setInteresPagado(BigDecimal interesPagado) {
        this.interesPagado = interesPagado;
    }

    public boolean isVigente() {
        return vigente;
    }

    public void setVigente(boolean vigente) {
        this.vigente = vigente;
    }

    public EstadoCuota getEstado() {
        return estado;
    }

    public void setEstado(EstadoCuota estado) {
        this.estado = estado;
    }

    public BigDecimal getSaldoPendienteCuota() {
        if (valorCuota == null) {
            return BigDecimal.ZERO;
        }
        BigDecimal pagado = (valorPagado != null) ? valorPagado : BigDecimal.ZERO;
        return valorCuota.subtract(pagado).max(BigDecimal.ZERO);
    }

    public BigDecimal getCapitalPendiente() {
        if (valorCapital == null) {
            return BigDecimal.ZERO;
        }
        BigDecimal capPag = (capitalPagado != null) ? capitalPagado : BigDecimal.ZERO;
        return valorCapital.subtract(capPag).max(BigDecimal.ZERO);
    }

    public BigDecimal getInteresPendiente() {
        if (valorInteres == null) {
            return BigDecimal.ZERO;
        }
        BigDecimal intPag = (interesPagado != null) ? interesPagado : BigDecimal.ZERO;
        return valorInteres.subtract(intPag).max(BigDecimal.ZERO);
    }

    public boolean esVencida(LocalDate fechaReferencia) {
        if (fechaReferencia == null || fechaVencimiento == null) {
            return false;
        }
        return estado == EstadoCuota.PENDIENTE
                && fechaVencimiento.isBefore(fechaReferencia)
                && getSaldoPendienteCuota().compareTo(BigDecimal.ZERO) > 0;
    }

    @Override
    public String toString() {
        return "Cuota{" +
                "id=" + id +
                ", prestamoId=" + prestamoId +
                ", vPlan=" + versionPlan +
                ", num=" + numeroCuota +
                ", vto=" + fechaVencimiento +
                ", valor=" + valorCuota +
                ", cap=" + valorCapital +
                ", int=" + valorInteres +
                ", pagado=" + valorPagado +
                ", vigente=" + vigente +
                ", estado=" + estado +
                '}';
    }
}

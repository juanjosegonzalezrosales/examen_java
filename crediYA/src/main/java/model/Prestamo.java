package model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import model.enums.EstadoPrestamo;
import model.enums.ModalidadAmortizacion;

public class Prestamo implements java.io.Serializable {

    private static final long serialVersionUID = 1L;

    private Integer id;
    private Solicitud solicitud;
    private BigDecimal montoCapital;
    private BigDecimal tasaInteresAnual;
    private Integer plazoCuotas;
    private ModalidadAmortizacion modalidad;
    private LocalDate fechaInicio;
    private BigDecimal montoTotal;
    private BigDecimal valorCuotaReferencia;
    private BigDecimal saldoPendiente;
    private EstadoPrestamo estado;
    private List<Cuota> cuotas;

    public Prestamo() {
        this.cuotas = new ArrayList<>();
    }

    public Prestamo(Integer id, Solicitud solicitud, BigDecimal montoCapital,
                    BigDecimal tasaInteresAnual, Integer plazoCuotas,
                    ModalidadAmortizacion modalidad, LocalDate fechaInicio,
                    BigDecimal montoTotal, BigDecimal valorCuotaReferencia,
                    BigDecimal saldoPendiente, EstadoPrestamo estado) {
        this.id = id;
        this.solicitud = solicitud;
        this.montoCapital = montoCapital;
        this.tasaInteresAnual = tasaInteresAnual;
        this.plazoCuotas = plazoCuotas;
        this.modalidad = modalidad;
        this.fechaInicio = fechaInicio;
        this.montoTotal = montoTotal;
        this.valorCuotaReferencia = valorCuotaReferencia;
        this.saldoPendiente = saldoPendiente;
        this.estado = estado;
        this.cuotas = new ArrayList<>();
    }

    public Prestamo(Solicitud solicitud, BigDecimal montoCapital,
                    BigDecimal tasaInteresAnual, Integer plazoCuotas,
                    ModalidadAmortizacion modalidad, LocalDate fechaInicio,
                    BigDecimal montoTotal, BigDecimal valorCuotaReferencia,
                    BigDecimal saldoPendiente) {
        this(null, solicitud, montoCapital, tasaInteresAnual, plazoCuotas,
             modalidad, fechaInicio, montoTotal, valorCuotaReferencia,
             saldoPendiente, EstadoPrestamo.ACTIVO);
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Solicitud getSolicitud() {
        return solicitud;
    }

    public void setSolicitud(Solicitud solicitud) {
        this.solicitud = solicitud;
    }

    public BigDecimal getMontoCapital() {
        return montoCapital;
    }

    public void setMontoCapital(BigDecimal montoCapital) {
        this.montoCapital = montoCapital;
    }

    public BigDecimal getTasaInteresAnual() {
        return tasaInteresAnual;
    }

    public void setTasaInteresAnual(BigDecimal tasaInteresAnual) {
        this.tasaInteresAnual = tasaInteresAnual;
    }

    public Integer getPlazoCuotas() {
        return plazoCuotas;
    }

    public void setPlazoCuotas(Integer plazoCuotas) {
        this.plazoCuotas = plazoCuotas;
    }

    public ModalidadAmortizacion getModalidad() {
        return modalidad;
    }

    public void setModalidad(ModalidadAmortizacion modalidad) {
        this.modalidad = modalidad;
    }

    public LocalDate getFechaInicio() {
        return fechaInicio;
    }

    public void setFechaInicio(LocalDate fechaInicio) {
        this.fechaInicio = fechaInicio;
    }

    public BigDecimal getMontoTotal() {
        return montoTotal;
    }

    public void setMontoTotal(BigDecimal montoTotal) {
        this.montoTotal = montoTotal;
    }

    public BigDecimal getValorCuotaReferencia() {
        return valorCuotaReferencia;
    }

    public void setValorCuotaReferencia(BigDecimal valorCuotaReferencia) {
        this.valorCuotaReferencia = valorCuotaReferencia;
    }

    public BigDecimal getSaldoPendiente() {
        return saldoPendiente;
    }

    public void setSaldoPendiente(BigDecimal saldoPendiente) {
        this.saldoPendiente = saldoPendiente;
    }

    public EstadoPrestamo getEstado() {
        return estado;
    }

    public void setEstado(EstadoPrestamo estado) {
        this.estado = estado;
    }

    public List<Cuota> getCuotas() {
        return cuotas;
    }

    public void setCuotas(List<Cuota> cuotas) {
        this.cuotas = (cuotas != null) ? cuotas : new ArrayList<>();
    }

    public void agregarCuota(Cuota cuota) {
        if (this.cuotas == null) {
            this.cuotas = new ArrayList<>();
        }
        this.cuotas.add(cuota);
    }

    public Cliente getCliente() {
        return (solicitud != null) ? solicitud.getCliente() : null;
    }

    public Empleado getEmpleado() {
        return (solicitud != null) ? solicitud.getEmpleado() : null;
    }

    @Override
    public String toString() {
        return "Prestamo{" +
                "id=" + id +
                ", solicitudId=" + (solicitud != null ? solicitud.getId() : "null") +
                ", cliente=" + (getCliente() != null ? getCliente().getNombre() : "null") +
                ", montoCapital=" + montoCapital +
                ", tasaInteresAnual=" + tasaInteresAnual + "%" +
                ", plazoCuotas=" + plazoCuotas +
                ", modalidad=" + modalidad +
                ", fechaInicio=" + fechaInicio +
                ", valorCuotaRef=" + valorCuotaReferencia +
                ", saldoPendiente=" + saldoPendiente +
                ", estado=" + estado +
                '}';
    }
}

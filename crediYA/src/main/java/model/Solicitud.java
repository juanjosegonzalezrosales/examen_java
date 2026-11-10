package model;

import exception.ValidacionException;
import java.math.BigDecimal;
import java.time.LocalDate;
import model.enums.EstadoSolicitud;
import model.enums.ModalidadAmortizacion;

public class Solicitud implements java.io.Serializable {

    private static final long serialVersionUID = 1L;

    private Integer id;
    private Cliente cliente;
    private Empleado empleado;
    private BigDecimal montoSolicitado;
    private Integer plazoMeses;
    private ModalidadAmortizacion modalidad;
    private LocalDate fechaSolicitud;
    private EstadoSolicitud estado;
    private String observaciones;
    private LocalDate fechaResolucion;

    public Solicitud() {
    }

    public Solicitud(Integer id, Cliente cliente, Empleado empleado, BigDecimal montoSolicitado,
                     Integer plazoMeses, ModalidadAmortizacion modalidad, LocalDate fechaSolicitud,
                     EstadoSolicitud estado, String observaciones, LocalDate fechaResolucion) {
        this.id = id;
        this.cliente = cliente;
        this.empleado = empleado;
        this.montoSolicitado = montoSolicitado;
        this.plazoMeses = plazoMeses;
        this.modalidad = modalidad;
        this.fechaSolicitud = fechaSolicitud;
        this.estado = estado;
        this.observaciones = observaciones;
        this.fechaResolucion = fechaResolucion;
    }

    public Solicitud(Cliente cliente, Empleado empleado, BigDecimal montoSolicitado,
                     Integer plazoMeses, ModalidadAmortizacion modalidad, LocalDate fechaSolicitud,
                     String observaciones) {
        this(null, cliente, empleado, montoSolicitado, plazoMeses, modalidad,
             fechaSolicitud, EstadoSolicitud.PENDIENTE, observaciones, null);
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Cliente getCliente() {
        return cliente;
    }

    public void setCliente(Cliente cliente) {
        this.cliente = cliente;
    }

    public Empleado getEmpleado() {
        return empleado;
    }

    public void setEmpleado(Empleado empleado) {
        this.empleado = empleado;
    }

    public BigDecimal getMontoSolicitado() {
        return montoSolicitado;
    }

    public void setMontoSolicitado(BigDecimal montoSolicitado) {
        this.montoSolicitado = montoSolicitado;
    }

    public Integer getPlazoMeses() {
        return plazoMeses;
    }

    public void setPlazoMeses(Integer plazoMeses) {
        this.plazoMeses = plazoMeses;
    }

    public ModalidadAmortizacion getModalidad() {
        return modalidad;
    }

    public void setModalidad(ModalidadAmortizacion modalidad) {
        this.modalidad = modalidad;
    }

    public LocalDate getFechaSolicitud() {
        return fechaSolicitud;
    }

    public void setFechaSolicitud(LocalDate fechaSolicitud) {
        this.fechaSolicitud = fechaSolicitud;
    }

    public EstadoSolicitud getEstado() {
        return estado;
    }

    public void setEstado(EstadoSolicitud estado) {
        this.estado = estado;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }

    public LocalDate getFechaResolucion() {
        return fechaResolucion;
    }

    public void setFechaResolucion(LocalDate fechaResolucion) {
        this.fechaResolucion = fechaResolucion;
    }

    public void validar() throws ValidacionException {
        if (cliente == null) {
            throw new ValidacionException("La solicitud debe estar asociada a un cliente registrado.");
        }
        if (empleado == null) {
            throw new ValidacionException("La solicitud debe contar con un empleado radicador.");
        }
        if (montoSolicitado == null || montoSolicitado.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ValidacionException("El monto solicitado debe ser un valor positivo mayor a cero.");
        }
        if (plazoMeses == null || plazoMeses < 1 || plazoMeses > 72) {
            throw new ValidacionException("El plazo de la solicitud debe estar entre 1 y 72 meses, inclusive.");
        }
        if (modalidad == null) {
            throw new ValidacionException("La modalidad de amortizacion es obligatoria (FRANCES o ALEMAN).");
        }
    }

    @Override
    public String toString() {
        return "Solicitud{" +
                "id=" + id +
                ", cliente=" + (cliente != null ? cliente.getNombre() : "null") +
                ", empleado=" + (empleado != null ? empleado.getNombre() : "null") +
                ", montoSolicitado=" + montoSolicitado +
                ", plazoMeses=" + plazoMeses +
                ", modalidad=" + modalidad +
                ", fechaSolicitud=" + fechaSolicitud +
                ", estado=" + estado +
                ", observaciones='" + observaciones + '\'' +
                ", fechaResolucion=" + fechaResolucion +
                '}';
    }
}

package model;

import java.io.Serializable;
import java.time.LocalDateTime;

public class Credencial implements Serializable {

    private static final long serialVersionUID = 1L;

    private Integer id;
    private Integer empleadoId;
    private Integer clienteId;
    private String passwordHash;
    private String salt;
    private int iteraciones;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaActualizacion;

    public Credencial() {
        this.iteraciones = 65536;
    }

    public Credencial(Integer id, Integer empleadoId, Integer clienteId, String passwordHash, String salt, int iteraciones) {
        this.id = id;
        this.empleadoId = empleadoId;
        this.clienteId = clienteId;
        this.passwordHash = passwordHash;
        this.salt = salt;
        this.iteraciones = iteraciones;
    }

    public Credencial(Integer id, Integer empleadoId, String passwordHash, String salt, int iteraciones) {
        this(id, empleadoId, null, passwordHash, salt, iteraciones);
    }

    public Credencial(Integer empleadoId, String passwordHash, String salt, int iteraciones) {
        this(null, empleadoId, null, passwordHash, salt, iteraciones);
    }

    public static Credencial paraCliente(Integer clienteId, String passwordHash, String salt, int iteraciones) {
        return new Credencial(null, null, clienteId, passwordHash, salt, iteraciones);
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getEmpleadoId() {
        return empleadoId;
    }

    public void setEmpleadoId(Integer empleadoId) {
        this.empleadoId = empleadoId;
    }

    public Integer getClienteId() {
        return clienteId;
    }

    public void setClienteId(Integer clienteId) {
        this.clienteId = clienteId;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public String getSalt() {
        return salt;
    }

    public void setSalt(String salt) {
        this.salt = salt;
    }

    public int getIteraciones() {
        return iteraciones;
    }

    public void setIteraciones(int iteraciones) {
        this.iteraciones = iteraciones;
    }

    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(LocalDateTime fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }

    public LocalDateTime getFechaActualizacion() {
        return fechaActualizacion;
    }

    public void setFechaActualizacion(LocalDateTime fechaActualizacion) {
        this.fechaActualizacion = fechaActualizacion;
    }

    @Override
    public String toString() {
        return "Credencial{" +
                "id=" + id +
                ", empleadoId=" + empleadoId +
                ", clienteId=" + clienteId +
                ", iteraciones=" + iteraciones +
                '}';
    }
}

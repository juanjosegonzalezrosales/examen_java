package model;

import exception.ValidacionException;
import java.math.BigDecimal;
import model.enums.Rol;

public class Empleado extends Persona {

    private Rol rol;
    private BigDecimal salario;

    public Empleado() {
        super();
    }

    public Empleado(Integer id, String nombre, String documento, String correo, Rol rol, BigDecimal salario) {
        super(id, nombre, documento, correo);
        this.rol = rol;
        this.salario = salario;
    }

    public Empleado(String nombre, String documento, String correo, Rol rol, BigDecimal salario) {
        this(null, nombre, documento, correo, rol, salario);
    }

    public Rol getRol() {
        return rol;
    }

    public void setRol(Rol rol) {
        this.rol = rol;
    }

    public BigDecimal getSalario() {
        return salario;
    }

    public void setSalario(BigDecimal salario) {
        this.salario = salario;
    }

    public void validar() throws ValidacionException {
        if (getNombre() == null || getNombre().trim().isEmpty()) {
            throw new ValidacionException("El nombre del empleado no puede estar vacio.");
        }
        if (getDocumento() == null || !getDocumento().trim().matches("\\d{8,10}")) {
            throw new ValidacionException("El documento de identidad debe contener entre 8 y 10 digitos numericos.");
        }
        if (getCorreo() == null || getCorreo().trim().isEmpty() || !getCorreo().contains("@") || !getCorreo().contains(".") || getCorreo().contains(" ")) {
            throw new ValidacionException("El correo electronico es invalido. Debe contener '@', un punto y no contener espacios.");
        }
        if (rol == null) {
            throw new ValidacionException("El rol del empleado es obligatorio.");
        }
        if (salario == null || salario.compareTo(BigDecimal.ZERO) < 0) {
            throw new ValidacionException("El salario del empleado no puede ser nulo ni negativo.");
        }
    }

    @Override
    public String toString() {
        return "Empleado{" + super.toString() + ", rol=" + rol + ", salario=" + salario + "}";
    }
}

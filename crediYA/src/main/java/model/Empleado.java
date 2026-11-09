package model;

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

    @Override
    public String toString() {
        return "Empleado{" + super.toString() + ", rol=" + rol + ", salario=" + salario + "}";
    }
}

package model;

public class Cliente extends Persona {

    private String telefono;

    public Cliente() {
        super();
    }

    public Cliente(Integer id, String nombre, String documento, String correo, String telefono) {
        super(id, nombre, documento, correo);
        this.telefono = telefono;
    }

    public Cliente(String nombre, String documento, String correo, String telefono) {
        this(null, nombre, documento, correo, telefono);
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    @Override
    public String toString() {
        return "Cliente{" + super.toString() + ", telefono='" + telefono + "'}";
    }
}

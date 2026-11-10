package model;

import exception.ValidacionException;

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

    public void validar() throws ValidacionException {
        if (getNombre() == null || getNombre().trim().isEmpty()) {
            throw new ValidacionException("El nombre del cliente no puede estar vacio.");
        }
        if (getDocumento() == null || !getDocumento().trim().matches("\\d{8,10}")) {
            throw new ValidacionException("El documento de identidad debe contener entre 8 y 10 digitos numericos.");
        }
        if (getCorreo() == null || getCorreo().trim().isEmpty() || !getCorreo().contains("@") || !getCorreo().contains(".") || getCorreo().contains(" ")) {
            throw new ValidacionException("El correo electronico es invalido. Debe contener '@', un punto y no contener espacios.");
        }
        if (telefono == null || telefono.trim().isEmpty() || telefono.replaceAll("\\D", "").length() < 7) {
            throw new ValidacionException("El telefono debe contener como minimo 7 digitos numericos.");
        }
    }

    @Override
    public String toString() {
        return "Cliente{" + super.toString() + ", telefono='" + telefono + "'}";
    }
}

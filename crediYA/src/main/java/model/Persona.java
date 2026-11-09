package model;

public abstract class Persona implements java.io.Serializable {

    private static final long serialVersionUID = 1L;

    private Integer id;
    private String nombre;
    private String documento;
    private String correo;

    public Persona() {
    }

    public Persona(Integer id, String nombre, String documento, String correo) {
        this.id = id;
        this.nombre = nombre;
        this.documento = documento;
        this.correo = correo;
    }

    public Persona(String nombre, String documento, String correo) {
        this(null, nombre, documento, correo);
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getDocumento() {
        return documento;
    }

    public void setDocumento(String documento) {
        this.documento = documento;
    }

    public String getCorreo() {
        return correo;
    }

    public void setCorreo(String correo) {
        this.correo = correo;
    }

    @Override
    public String toString() {
        return "id=" + id + ", nombre='" + nombre + "', documento='" + documento + "', correo='" + correo + "'";
    }
}

package model.chain;

public class ResultadoCadena {

    private final boolean autorizado;
    private final String mensaje;

    public ResultadoCadena(boolean autorizado, String mensaje) {
        this.autorizado = autorizado;
        this.mensaje = mensaje;
    }

    public boolean isAutorizado() {
        return autorizado;
    }

    public boolean isExitoso() {
        return autorizado;
    }

    public String getMensaje() {
        return mensaje;
    }

    @Override
    public String toString() {
        return "ResultadoCadena{autorizado=" + autorizado + ", mensaje='" + mensaje + '\'' + '}';
    }
}

package exception;

public class AutenticacionException extends CrediYaException {

    public AutenticacionException(String mensaje) {
        super(mensaje);
    }

    public AutenticacionException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}


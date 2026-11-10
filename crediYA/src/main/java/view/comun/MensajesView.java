package view.comun;

public class MensajesView {

    public void exito(String mensaje) {
        System.out.println("\n>> OK: " + mensaje);
    }

    public void error(String mensaje) {
        System.out.println("\n>> ERROR: " + mensaje);
    }

    public void informativo(String mensaje) {
        System.out.println("\n>> AVISO: " + mensaje);
    }

    public void encabezado(String titulo) {
        System.out.println("\n==================================================");
        System.out.println("  " + titulo);
        System.out.println("==================================================");
    }
}


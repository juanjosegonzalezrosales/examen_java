package view.autenticacion;

import view.comun.EntradaConsola;
import view.comun.MensajesView;

public class LoginView {

    private final EntradaConsola entrada;
    private final MensajesView mensajes;

    public LoginView(EntradaConsola entrada, MensajesView mensajes) {
        this.entrada = entrada;
        this.mensajes = mensajes;
    }

    public String[] pedirCredenciales(String titulo) {
        mensajes.encabezado(titulo != null ? titulo : "INICIO DE SESION");
        String documento = entrada.leerTexto("Documento de identidad: ");
        String password = entrada.leerTexto("Contrasena: ");
        return new String[]{documento, password};
    }
}


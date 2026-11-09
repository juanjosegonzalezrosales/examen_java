package controller;

import database.Conexion;
import view.MenuConsola;

public class CrediYA {

    public void iniciar() {
        System.out.println("==================================================");
        System.out.println("        SISTEMA DE GESTION CREDIYA S.A.S.         ");
        System.out.println("   Plataforma de Creditos Personales y Cobranza   ");
        System.out.println("==================================================");
        System.out.println("Iniciando componentes y verificando conexion...");
        System.out.println();

        Conexion conexion = Conexion.getInstancia();

        boolean conexionExitosa = conexion.probarConexion();

        System.out.println();
        if (conexionExitosa) {
            System.out.println(">> [ESTADO]: Conexion verificada. Cargando modulos funcionales...");

            MenuConsola menu = new MenuConsola();
            menu.iniciarMenuPrincipal();
        } else {
            System.out.println(">> [AVISO]: La aplicacion no pudo conectar a MySQL.");
            System.out.println("   Verifique que el servicio MySQL este activo y el archivo 'db.properties'");
            System.out.println("   contenga las credenciales correctas.");
        }
    }
}

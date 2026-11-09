package database;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class Conexion {

    private static Conexion instancia;

    private String url;
    private String usuario;
    private String password;

    private Conexion() {
        cargarConfiguracion();
    }

    public static synchronized Conexion getInstancia() {
        if (instancia == null) {
            instancia = new Conexion();
        }
        return instancia;
    }

    private void cargarConfiguracion() {
        Properties propiedades = new Properties();

        File archivo = new File("db.properties");
        if (!archivo.exists()) {
            archivo = new File("crediYA/db.properties");
        }

        if (archivo.exists()) {
            try (InputStream input = new FileInputStream(archivo)) {
                propiedades.load(input);
                this.url = propiedades.getProperty("db.url", "jdbc:mysql://localhost:3306/crediya_db");
                this.usuario = propiedades.getProperty("db.user", "root");
                this.password = propiedades.getProperty("db.password", "");
                return;
            } catch (IOException e) {
                System.out.println("[Conexion] Advertencia: no se pudo leer el archivo 'db.properties': " + e.getMessage());
            }
        }

        this.url = System.getenv("DB_URL") != null ? System.getenv("DB_URL") : "jdbc:mysql://localhost:3306/crediya_db";
        this.usuario = System.getenv("DB_USER") != null ? System.getenv("DB_USER") : "root";
        this.password = System.getenv("DB_PASSWORD") != null ? System.getenv("DB_PASSWORD") : "";
    }

    public Connection conectar() throws SQLException {
        return DriverManager.getConnection(url, usuario, password);
    }

    public void cerrarConexion(Connection conexion) {
        if (conexion != null) {
            try {
                if (!conexion.isClosed()) {
                    conexion.close();
                }
            } catch (SQLException e) {
                System.out.println("[Conexion] Error al cerrar conexion: " + e.getMessage());
            }
        }
    }

    public boolean probarConexion() {
        System.out.println("[Conexion] Intentando conectar a la base de datos MySQL...");
        try (Connection con = conectar()) {
            if (con != null && !con.isClosed()) {
                System.out.println("[Conexion] OK -> Conexion establecida con exito.");
                System.out.println("[Conexion] Motor: " + con.getMetaData().getDatabaseProductName()
                                   + " " + con.getMetaData().getDatabaseProductVersion());
                System.out.println("[Conexion] Catalogo activo: " + con.getCatalog());
                return true;
            }
        } catch (SQLException e) {
            System.out.println("[Conexion] ERROR -> No se pudo establecer la conexion con MySQL.");

            int errorCode = e.getErrorCode();
            if (errorCode == 1045) {
                System.out.println("  -> Motivo: Acceso denegado (usuario o clave incorrectos).");
                System.out.println("  -> Solucion: Verifique el usuario y la contrasena en 'db.properties'.");
            } else if (errorCode == 1049) {
                System.out.println("  -> Motivo: La base de datos 'crediya_db' no existe en el servidor MySQL.");
                System.out.println("  -> Solucion: Ejecute en MySQL Workbench: CREATE DATABASE crediya_db;");
            } else if (errorCode == 0 || e.getMessage().toLowerCase().contains("communications link failure")) {
                System.out.println("  -> Motivo: No hay comunicacion con el servidor MySQL (puerto 3306).");
                System.out.println("  -> Solucion: Verifique que el servicio Windows 'MySQL80' este en ejecucion.");
            } else {
                System.out.println("  -> Detalle del error (Codigo " + errorCode + "): " + e.getMessage());
            }
        }
        return false;
    }
}

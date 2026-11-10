package utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import model.Prestamo;

public class ReporteArchivoService {

    public boolean exportarTexto(List<Prestamo> prestamos, File archivo) {
        if (prestamos == null || archivo == null) {
            return false;
        }
        try (PrintWriter writer = new PrintWriter(new FileWriter(archivo))) {
            writer.println("================================================================================");
            writer.println("                 CREDIYA S.A.S. - REPORTE OFICIAL DE PRESTAMOS                  ");
            writer.println("Fecha de emision: " + LocalDate.now());
            writer.println("================================================================================");
            writer.printf("%-5s %-25s %-15s %-15s %-10s %-10s\n", "ID", "CLIENTE", "CAPITAL", "SALDO PEND.", "MODALIDAD", "ESTADO");
            writer.println("--------------------------------------------------------------------------------");

            for (Prestamo p : prestamos) {
                writer.printf("%-5d %-25s $%,12.2f $%,12.2f %-10s %-10s\n",
                    p.getId(),
                    p.getCliente() != null ? p.getCliente().getNombre() : "N/A",
                    p.getMontoCapital(),
                    p.getSaldoPendiente(),
                    p.getModalidad(),
                    p.getEstado());
            }

            writer.println("================================================================================");
            writer.println("Total registros: " + prestamos.size());
            return true;
        } catch (IOException e) {
            System.out.println(">> Error al escribir archivo .txt: " + e.getMessage());
            return false;
        }
    }

    public boolean exportarBinario(List<Prestamo> prestamos, File archivo) {
        if (prestamos == null || archivo == null) {
            return false;
        }
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(archivo))) {
            oos.writeObject(prestamos);
            return true;
        } catch (IOException e) {
            System.out.println(">> Error al serializar a .dat: " + e.getMessage());
            return false;
        }
    }

    @SuppressWarnings("unchecked")
    public List<Prestamo> importarBinario(File archivo) {
        if (archivo == null || !archivo.exists() || !archivo.isFile()) {
            System.out.println(">> El archivo binario no existe o no es valido: " + (archivo != null ? archivo.getAbsolutePath() : "null"));
            return new ArrayList<>();
        }
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(archivo))) {
            Object obj = ois.readObject();
            if (obj instanceof List<?> lista) {
                return (List<Prestamo>) lista;
            } else {
                System.out.println(">> Formato de archivo invalido: no contiene una lista de prestamos.");
                return new ArrayList<>();
            }
        } catch (IOException e) {
            System.out.println(">> Error de E/S al deserializar .dat: " + e.getMessage());
            return new ArrayList<>();
        } catch (ClassNotFoundException e) {
            System.out.println(">> Error: Clase no encontrada al deserializar .dat: " + e.getMessage());
            return new ArrayList<>();
        }
    }
}


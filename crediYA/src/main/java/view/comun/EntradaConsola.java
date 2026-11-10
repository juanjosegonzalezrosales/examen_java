package view.comun;

import java.math.BigDecimal;
import java.util.Scanner;

public class EntradaConsola {

    private final Scanner scanner;

    public EntradaConsola() {
        this.scanner = new Scanner(System.in);
    }

    public EntradaConsola(Scanner scanner) {
        this.scanner = scanner;
    }

    public int leerEntero(String prompt, int min, int max) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();
            try {
                int val = Integer.parseInt(input);
                if (val >= min && val <= max) return val;
                System.out.printf(">> Ingrese un valor entre %d y %d.\n", min, max);
            } catch (NumberFormatException e) {
                System.out.println(">> Error: Debe ingresar un numero entero.");
            }
        }
    }

    public BigDecimal leerBigDecimal(String prompt, BigDecimal min) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim().replace(",", ".");
            try {
                BigDecimal val = new BigDecimal(input);
                if (val.compareTo(min) >= 0) return val;
                System.out.printf(">> El monto no puede ser menor a $%,.2f\n", min);
            } catch (NumberFormatException e) {
                System.out.println(">> Error: Debe ingresar un valor numerico decimal valido.");
            }
        }
    }

    public String leerTexto(String prompt) {
        while (true) {
            System.out.print(prompt);
            String texto = scanner.nextLine().trim();
            if (!texto.isEmpty()) return texto;
            System.out.println(">> El campo no puede estar vacio.");
        }
    }

    public void pausar() {
        System.out.print("\nPresione ENTER para continuar...");
        scanner.nextLine();
    }
}


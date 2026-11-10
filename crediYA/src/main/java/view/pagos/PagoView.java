package view.pagos;

import java.math.BigDecimal;
import java.util.List;
import model.Cuota;
import model.Pago;
import model.enums.OpcionAbonoExtra;
import view.comun.EntradaConsola;
import view.comun.MensajesView;

public class PagoView {

    private final EntradaConsola entrada;
    private final MensajesView mensajes;

    public PagoView(EntradaConsola entrada, MensajesView mensajes) {
        this.entrada = entrada;
        this.mensajes = mensajes;
    }

    public int mostrarMenuOperaciones() {
        mensajes.encabezado("GESTION Y OPERACIONES DE CARTERA - PAGOS");
        System.out.println("  1. Pagar cuota programada ordinaria");
        System.out.println("  2. Registrar abono extraordinario a capital");
        System.out.println("  3. Cancelar totalidad del prestamo (Liquidacion)");
        System.out.println("  4. Consultar historico de pagos por prestamo");
        System.out.println("  5. Consultar pagos de cuotas programadas");
        System.out.println("  6. Consultar abonos extraordinarios");
        System.out.println("  0. Volver");
        return entrada.leerEntero("Seleccione opcion [0-6]: ", 0, 6);
    }

    public boolean confirmarPagoCuota(Cuota c, BigDecimal total, BigDecimal cap, BigDecimal inte) {
        System.out.printf("Cuota #%d | Vencimiento: %s | Total: $%,.2f (Capital: $%,.2f, Interes: $%,.2f)\n",
            c.getNumeroCuota(), c.getFechaVencimiento(), total, cap, inte);
        String conf = entrada.leerTexto("Confirmar registro del pago de la cuota? (S/N): ");
        return conf.equalsIgnoreCase("S");
    }

    public Object[] pedirAbonoExtraordinario(BigDecimal saldoPendiente) {
        System.out.printf("Saldo capital pendiente del credito: $%,.2f\n", saldoPendiente);
        BigDecimal monto = entrada.leerBigDecimal("Monto del abono extraordinario a capital ($): ", BigDecimal.ONE);
        System.out.println("Modalidad de aplicacion: 1. REDUCIR_CUOTA | 2. REDUCIR_PLAZO");
        int op = entrada.leerEntero("Seleccione opcion [1-2]: ", 1, 2);
        OpcionAbonoExtra opcion = (op == 1) ? OpcionAbonoExtra.REDUCIR_CUOTA : OpcionAbonoExtra.REDUCIR_PLAZO;
        return new Object[]{monto, opcion};
    }

    public boolean confirmarCancelacionTotal(BigDecimal saldoPendiente) {
        System.out.printf("Monto de cancelacion total requerida: $%,.2f\n", saldoPendiente);
        String conf = entrada.leerTexto("Confirmar cancelacion y cierre definitivo del prestamo? (S/N): ");
        return conf.equalsIgnoreCase("S");
    }

    public void mostrarTablaPagos(List<Pago> lista, String titulo) {
        mensajes.encabezado(titulo != null ? titulo : "HISTORIAL DE TRANSACCIONES DE PAGO");
        if (lista.isEmpty()) {
            System.out.println("No se encontraron registros de pago.");
            return;
        }
        System.out.printf("%-5s %-12s %-14s %-10s %-10s %-12s %-22s %s\n",
            "ID", "FECHA", "MONTO TOTAL", "MORA", "INTERES", "CAPITAL", "TIPO PAGO", "OBSERVACIONES");
        System.out.println("-------------------------------------------------------------------------------------------------------------");
        for (Pago p : lista) {
            System.out.printf("%-5d %-12s $%,12.2f $%,8.2f $%,8.2f $%,10.2f %-22s %s\n",
                p.getId(),
                p.getFechaPago(),
                p.getMonto(),
                p.getMontoMora(),
                p.getMontoInteres(),
                p.getMontoCapital(),
                p.getTipoPago(),
                p.getObservaciones() != null ? p.getObservaciones() : "Sin observaciones");
        }
    }
}


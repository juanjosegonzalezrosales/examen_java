package view.prestamos;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import model.Cuota;
import model.Prestamo;
import view.comun.EntradaConsola;
import view.comun.MensajesView;

public class PrestamoView {

    private final EntradaConsola entrada;
    private final MensajesView mensajes;

    public PrestamoView(EntradaConsola entrada, MensajesView mensajes) {
        this.entrada = entrada;
        this.mensajes = mensajes;
    }

    public BigDecimal pedirTasaDesembolso() {
        return entrada.leerBigDecimal("Tasa nominal anual porcentual (%) [ej. 24.0]: ", BigDecimal.ZERO);
    }

    public void mostrarTablaPrestamos(List<Prestamo> lista) {
        mensajes.encabezado("LISTADO GENERAL DE PRESTAMOS (" + lista.size() + " CONTRATOS)");
        if (lista.isEmpty()) {
            System.out.println("No se encontraron contratos de prestamo.");
            return;
        }
        System.out.printf("%-5s %-20s %-14s %-14s %-10s %-10s\n",
            "ID", "CLIENTE", "CAPITAL", "SALDO PEND.", "MODALIDAD", "ESTADO");
        System.out.println("--------------------------------------------------------------------------------");
        for (Prestamo p : lista) {
            System.out.printf("%-5d %-20s $%,12.2f $%,12.2f %-10s %-10s\n",
                p.getId(),
                p.getCliente() != null ? p.getCliente().getNombre() : "N/A",
                p.getMontoCapital(),
                p.getSaldoPendiente(),
                p.getModalidad(),
                p.getEstado());
        }
    }

    public void mostrarTablaAmortizacion(Prestamo p) {
        mensajes.encabezado("PLAN DE AMORTIZACION - PRESTAMO #" + p.getId());
        System.out.printf("Titular: %s | Capital: $%,.2f | Tasa Anual: %,.2f%% | Plazo: %d cuotas | Modalidad: %s\n",
            p.getCliente() != null ? p.getCliente().getNombre() : "N/A",
            p.getMontoCapital(),
            p.getTasaInteresAnual(),
            p.getPlazoCuotas(),
            p.getModalidad());
        System.out.println("--------------------------------------------------------------------------------------------------------");

        List<Cuota> cuotas = p.getCuotas();
        if (cuotas == null || cuotas.isEmpty()) {
            System.out.println("No hay cuotas generadas para este prestamo.");
            return;
        }

        LocalDate hoy = LocalDate.now();
        System.out.printf("%-5s %-12s %-13s %-13s %-13s %-14s %-13s %-10s\n",
            "NUM", "VENCIMIENTO", "VALOR CUOTA", "CAPITAL", "INTERES", "SALDO RESTANTE", "SALDO CUOTA", "ESTADO");
        System.out.println("--------------------------------------------------------------------------------------------------------");
        for (Cuota c : cuotas) {
            System.out.printf("%-5d %-12s $%,11.2f $%,11.2f $%,11.2f $%,12.2f $%,11.2f %-10s\n",
                c.getNumeroCuota(),
                c.getFechaVencimiento(),
                c.getValorCuota(),
                c.getValorCapital(),
                c.getValorInteres(),
                c.getSaldoRestanteCapital(),
                c.getSaldoPendienteCuota(),
                c.getEstadoVisual(hoy));
        }
    }

    public void mostrarDetallePrestamo(Prestamo p) {
        mensajes.encabezado("DETALLE DEL CONTRATO DE PRESTAMO #" + p.getId());
        System.out.printf(" ID Prestamo       : %d\n", p.getId());
        System.out.printf(" Solicitud Origen  : #%d\n", p.getSolicitud() != null ? p.getSolicitud().getId() : 0);
        System.out.printf(" Cliente Titular   : %s (%s)\n",
            p.getCliente() != null ? p.getCliente().getNombre() : "N/A",
            p.getCliente() != null ? p.getCliente().getDocumento() : "N/A");
        System.out.printf(" Monto Capital     : $%,.2f\n", p.getMontoCapital());
        System.out.printf(" Tasa Interes Anual: %,.2f%%\n", p.getTasaInteresAnual());
        System.out.printf(" Plazo Pactado     : %d meses / cuotas\n", p.getPlazoCuotas());
        System.out.printf(" Modalidad Calculo : %s\n", p.getModalidad());
        System.out.printf(" Fecha de Inicio   : %s\n", p.getFechaInicio());
        System.out.printf(" Monto Total Cred. : $%,.2f\n", p.getMontoTotal());
        System.out.printf(" Cuota Referencia  : $%,.2f\n", p.getValorCuotaReferencia());
        System.out.printf(" Saldo Pendiente   : $%,.2f\n", p.getSaldoPendiente());
        System.out.printf(" Estado Operativo  : %s\n", p.getEstado());
        System.out.println("--------------------------------------------------");
    }
}


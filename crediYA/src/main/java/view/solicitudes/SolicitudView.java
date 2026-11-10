package view.solicitudes;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import model.Cliente;
import model.Empleado;
import model.Solicitud;
import model.enums.ModalidadAmortizacion;
import view.comun.EntradaConsola;
import view.comun.MensajesView;

public class SolicitudView {

    private final EntradaConsola entrada;
    private final MensajesView mensajes;

    public SolicitudView(EntradaConsola entrada, MensajesView mensajes) {
        this.entrada = entrada;
        this.mensajes = mensajes;
    }

    public Solicitud pedirNuevaSolicitud(Cliente cli, Empleado emp) {
        mensajes.encabezado("RADICAR NUEVA SOLICITUD DE CREDITO");
        System.out.printf("Cliente titular: %s (ID: %d | Doc: %s)\n", cli.getNombre(), cli.getId(), cli.getDocumento());
        BigDecimal monto = entrada.leerBigDecimal("Monto solicitado ($): ", new BigDecimal("100000.00"));
        int plazo = entrada.leerEntero("Plazo en meses [1-72]: ", 1, 72);
        System.out.println("Modalidad: 1. FRANCES (Cuota fija) | 2. ALEMAN (Capital fijo)");
        int opMod = entrada.leerEntero("Seleccione modalidad [1-2]: ", 1, 2);
        ModalidadAmortizacion mod = (opMod == 1) ? ModalidadAmortizacion.FRANCES : ModalidadAmortizacion.ALEMAN;
        String obs = entrada.leerTexto("Destino del credito / Observaciones: ");
        String nota = "[" + LocalDate.now() + "] Creada por " + emp.getRol() + " " + emp.getNombre() + ": " + obs;
        return new Solicitud(cli, emp, monto, plazo, mod, LocalDate.now(), nota);
    }

    public void mostrarTablaSolicitudes(List<Solicitud> lista) {
        mensajes.encabezado("LISTADO GENERAL DE SOLICITUDES (" + lista.size() + " REGISTROS)");
        if (lista.isEmpty()) {
            System.out.println("No se encontraron solicitudes de credito.");
            return;
        }
        System.out.printf("%-5s %-20s %-15s %-12s %-6s %-10s %-12s\n",
            "ID", "CLIENTE", "DOC. CLIENTE", "MONTO", "PLAZO", "MODALIDAD", "ESTADO");
        System.out.println("-----------------------------------------------------------------------------------------");
        for (Solicitud s : lista) {
            System.out.printf("%-5d %-20s %-15s $%,11.2f %-6d %-10s %-12s\n",
                s.getId(),
                s.getCliente() != null ? s.getCliente().getNombre() : "N/A",
                s.getCliente() != null ? s.getCliente().getDocumento() : "N/A",
                s.getMontoSolicitado(),
                s.getPlazoMeses(),
                s.getModalidad(),
                s.getEstado());
        }
    }

    public void mostrarDetalleSolicitud(Solicitud s) {
        mensajes.encabezado("DETALLE DE LA SOLICITUD #" + s.getId());
        System.out.printf(" ID Solicitud     : %d\n", s.getId());
        System.out.printf(" Fecha Radicacion : %s\n", s.getFechaSolicitud());
        System.out.printf(" Estado Actual    : %s\n", s.getEstado());
        System.out.printf(" Cliente Titular  : %s (%s)\n",
            s.getCliente() != null ? s.getCliente().getNombre() : "N/A",
            s.getCliente() != null ? s.getCliente().getDocumento() : "N/A");
        if (s.getEmpleado() != null) {
            System.out.printf(" Funcionario Rad. : %s (%s)\n", s.getEmpleado().getNombre(), s.getEmpleado().getRol());
        }
        System.out.printf(" Monto Solicitado : $%,.2f\n", s.getMontoSolicitado());
        System.out.printf(" Plazo Solicitado : %d meses\n", s.getPlazoMeses());
        System.out.printf(" Modalidad        : %s\n", s.getModalidad());
        System.out.printf(" Fecha Resolucion : %s\n", s.getFechaResolucion() != null ? s.getFechaResolucion() : "Pendiente de resolucion");
        System.out.println(" Historial Notas  : " + (s.getObservaciones() != null ? s.getObservaciones() : "Sin observaciones"));
        System.out.println("--------------------------------------------------");
    }
}


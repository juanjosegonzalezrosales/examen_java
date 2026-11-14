package view.reportes;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import model.Cliente;
import model.Cuota;
import model.Empleado;
import model.Pago;
import model.Prestamo;
import model.Solicitud;
import model.enums.EstadoCuota;
import view.comun.EntradaConsola;
import view.comun.MensajesView;

public class ReporteView {

    private final EntradaConsola entrada;
    private final MensajesView mensajes;

    public ReporteView(EntradaConsola entrada, MensajesView mensajes) {
        this.entrada = entrada;
        this.mensajes = mensajes;
    }

    public void mostrarPrestamosActivos(List<Prestamo> activos) {
        mensajes.encabezado("REPORTE: PRESTAMOS ACTIVOS EN CARTERA (" + activos.size() + " CONTRATOS)");
        if (activos.isEmpty()) {
            System.out.println("No hay prestamos activos actualmente.");
            return;
        }
        System.out.printf("%-5s %-22s %-14s %-14s %-10s\n", "ID", "CLIENTE", "CAPITAL", "SALDO PEND.", "MODALIDAD");
        System.out.println("-----------------------------------------------------------------------------");
        for (Prestamo p : activos) {
            System.out.printf("%-5d %-22s $%,12.2f $%,12.2f %-10s\n",
                p.getId(),
                p.getCliente() != null ? p.getCliente().getNombre() : "N/A",
                p.getMontoCapital(),
                p.getSaldoPendiente(),
                p.getModalidad());
        }
    }

    public void mostrarCuotasVencidasYMorosos(List<Prestamo> morosos, LocalDate hoy) {
        mensajes.encabezado("REPORTE: CUOTAS VENCIDAS Y CLIENTES EN MORA A FECHA " + hoy);
        if (morosos.isEmpty()) {
            System.out.println("No existen clientes con cuotas en mora.");
            return;
        }
        for (Prestamo p : morosos) {
            System.out.printf("\n[MORA] Prestamo #%d | Cliente: %s | Tel: %s | Saldo: $%,.2f\n",
                p.getId(),
                p.getCliente() != null ? p.getCliente().getNombre() : "N/A",
                p.getCliente() != null ? p.getCliente().getTelefono() : "N/A",
                p.getSaldoPendiente());
            List<Cuota> vencidas = p.getCuotas().stream().filter(c -> c.esVencida(hoy)).toList();
            for (Cuota c : vencidas) {
                System.out.printf("   -> Cuota #%d vencida el %s | Estado: %s | Saldo cuota: $%,.2f (Cap: $%,.2f, Int: $%,.2f)\n",
                    c.getNumeroCuota(), c.getFechaVencimiento(), c.getEstadoVisual(hoy), c.getSaldoPendienteCuota(), c.getCapitalPendiente(), c.getInteresPendiente());
            }
        }
    }
    
    public void mostrarCuotasVencidasYMorososExamen(List<Prestamo> morosos, LocalDate hoy) {
        mensajes.encabezado("REPORTE: Prestamos Vencidos con la cuota exacta vencida al corte de " + hoy);
        if (morosos.isEmpty()) {
            System.out.println("No existen clientes con cuotas en mora.");
            return;
        }
        for (Prestamo p : morosos) {
            System.out.printf("\n[MORA] Prestamo #%d | Cliente: %s | Tel: %s | Saldo: $%,.2f\n",
                p.getId(),
                p.getCliente() != null ? p.getCliente().getNombre() : "N/A",
                p.getCliente() != null ? p.getCliente().getTelefono() : "N/A",
                p.getSaldoPendiente());
            List<Cuota> vencidas = p.getCuotas().stream().filter(c -> c.esVencida(hoy)).toList();
            for (Cuota c : vencidas) {
                System.out.printf("   -> Cuota #%d vencida el %s | Estado: %s | Saldo cuota: $%,.2f (Cap: $%,.2f, Int: $%,.2f)\n",
                    c.getNumeroCuota(), c.getFechaVencimiento(), c.getEstadoVisual(hoy), c.getSaldoPendienteCuota(), c.getCapitalPendiente(), c.getInteresPendiente());
            }
        }
    }

    public void mostrarClientesMorosos(List<Prestamo> morosos) {
        mensajes.encabezado("REPORTE: LISTADO CONSOLIDADO DE CLIENTES MOROSOS");
        if (morosos.isEmpty()) {
            System.out.println("No se registran clientes deudores en estado de mora.");
            return;
        }
        System.out.printf("%-5s %-25s %-15s %-15s %-15s\n", "ID P.", "CLIENTE", "DOCUMENTO", "TELEFONO", "SALDO TOTAL");
        System.out.println("--------------------------------------------------------------------------------");
        for (Prestamo p : morosos) {
            System.out.printf("%-5d %-25s %-15s %-15s $%,12.2f\n",
                p.getId(),
                p.getCliente() != null ? p.getCliente().getNombre() : "N/A",
                p.getCliente() != null ? p.getCliente().getDocumento() : "N/A",
                p.getCliente() != null ? p.getCliente().getTelefono() : "N/A",
                p.getSaldoPendiente());
        }
    }

    public void mostrarPrestamosPagados(List<Prestamo> pagados) {
        mensajes.encabezado("REPORTE: PRESTAMOS TOTALMENTE PAGADOS / LIQUIDADOS (" + pagados.size() + ")");
        if (pagados.isEmpty()) {
            System.out.println("No se registran prestamos totalmente liquidados.");
            return;
        }
        System.out.printf("%-5s %-25s %-15s %-15s %-10s\n", "ID", "CLIENTE", "CAPITAL ORIGINAL", "MONTO TOTAL", "MODALIDAD");
        System.out.println("--------------------------------------------------------------------------------");
        for (Prestamo p : pagados) {
            System.out.printf("%-5d %-25s $%,12.2f $%,12.2f %-10s\n",
                p.getId(),
                p.getCliente() != null ? p.getCliente().getNombre() : "N/A",
                p.getMontoCapital(),
                p.getMontoTotal(),
                p.getModalidad());
        }
    }

    public void mostrarIndicadoresCartera(Map<String, String> indicadores) {
        mensajes.encabezado("RESUMEN DE INDICADORES DE CARTERA (HASHMAP)");
        System.out.println("------------------------------------------------------------");
        System.out.printf("%-40s | %-15s\n", "INDICADOR OPERATIVO / FINANCIERO", "VALOR CALCULADO");
        System.out.println("------------------------------------------------------------");
        for (Map.Entry<String, String> entry : indicadores.entrySet()) {
            System.out.printf("%-40s | %-15s\n", entry.getKey(), entry.getValue());
        }
        System.out.println("------------------------------------------------------------");
    }

    public void mostrarFichaIntegralCliente(Cliente cli, List<Solicitud> solicitudes, List<Prestamo> prestamos, Map<Integer, List<Pago>> pagosPorPrestamo) {
        mensajes.encabezado("FICHA INTEGRAL DE CLIENTE: " + cli.getNombre().toUpperCase());
        System.out.printf(" ID Cliente : %d\n", cli.getId());
        System.out.printf(" Nombre     : %s\n", cli.getNombre());
        System.out.printf(" Documento  : %s\n", cli.getDocumento());
        System.out.printf(" Correo     : %s\n", cli.getCorreo());
        System.out.printf(" Telefono   : %s\n", cli.getTelefono());
        System.out.println("--------------------------------------------------------------------------------");

        System.out.println("1. SOLICITUDES DE CREDITO (" + solicitudes.size() + " registradas):");
        if (solicitudes.isEmpty()) {
            System.out.println("   El cliente no registra solicitudes de credito.");
        } else {
            System.out.printf("   %-5s %-12s %-14s %-8s %-10s %-14s %s\n", "ID", "FECHA", "MONTO SOLIC.", "PLAZO", "MODALIDAD", "ESTADO", "OBSERVACIONES");
            System.out.println("   -----------------------------------------------------------------------------------------");
            for (Solicitud s : solicitudes) {
                System.out.printf("   %-5d %-12s $%,12.2f %-8d %-10s %-14s %s\n",
                    s.getId(), s.getFechaSolicitud(), s.getMontoSolicitado(), s.getPlazoMeses(), s.getModalidad(), s.getEstado(),
                    s.getObservaciones() != null ? s.getObservaciones() : "Sin observaciones");
            }
        }
        System.out.println("--------------------------------------------------------------------------------");

        System.out.println("2. CONTRATOS DE PRESTAMO (" + prestamos.size() + " contratos):");
        if (prestamos.isEmpty()) {
            System.out.println("   El cliente no registra contratos de prestamo.");
        } else {
            LocalDate hoy = LocalDate.now();
            for (Prestamo p : prestamos) {
                System.out.printf("\n   >> CONTRATO DE PRESTAMO #%d [%s]\n", p.getId(), p.getEstado());
                System.out.printf("      Monto Capital : $%,.2f | Saldo Pendiente: $%,.2f\n", p.getMontoCapital(), p.getSaldoPendiente());
                System.out.printf("      Modalidad     : %s | Tasa Anual: %,.2f%% | Plazo: %d cuotas\n",
                    p.getModalidad(), p.getTasaInteresAnual(), p.getPlazoCuotas());
                System.out.printf("      Fecha Inicio  : %s | Solicitud Origen: #%d\n",
                    p.getFechaInicio(), p.getSolicitud() != null ? p.getSolicitud().getId() : 0);

                List<Cuota> cuotas = p.getCuotas();
                if (cuotas != null && !cuotas.isEmpty()) {
                    long pagadas = cuotas.stream().filter(c -> c.getEstado() == EstadoCuota.PAGADA).count();
                    long vencidas = cuotas.stream().filter(c -> c.esVencida(hoy)).count();
                    long pendientes = cuotas.stream().filter(c -> !c.esVencida(hoy) && c.getEstado() == EstadoCuota.PENDIENTE).count();
                    System.out.printf("      Resumen cuotas (Total: %d) -> Pagadas: %d | Al dia: %d | En mora: %d\n",
                        cuotas.size(), pagadas, pendientes, vencidas);
                }

                List<Pago> pagos = pagosPorPrestamo != null ? pagosPorPrestamo.get(p.getId()) : null;
                System.out.printf("      Historial de Pagos (%d registrados):\n", pagos != null ? pagos.size() : 0);
                if (pagos != null && !pagos.isEmpty()) {
                    for (Pago pg : pagos) {
                        System.out.printf("      -> Pago #%d | Fecha: %s | Monto: $%,.2f | Tipo: %s | %s\n",
                            pg.getId(), pg.getFechaPago(), pg.getMonto(), pg.getTipoPago(),
                            pg.getObservaciones() != null ? pg.getObservaciones() : "Sin observaciones");
                    }
                }
            }
        }
        System.out.println("================================================================================\n");
    }

    public void mostrarCreditosPropiosCliente(List<Prestamo> prestamos) {
        mensajes.encabezado("MIS CREDITOS PERSONALES (" + prestamos.size() + " CONTRATOS)");
        if (prestamos.isEmpty()) {
            System.out.println("Usted no posee contratos de credito registrados a su nombre.");
            return;
        }
        System.out.printf("%-5s %-14s %-10s %-8s %-8s %-12s %-10s %-14s\n",
            "ID", "MONTO CAP.", "MODALIDAD", "PLAZO", "TASA", "FECHA INICIO", "ESTADO", "SALDO PEND.");
        System.out.println("-------------------------------------------------------------------------------------------");
        for (Prestamo p : prestamos) {
            System.out.printf("%-5d $%,12.2f %-10s %-8d %,5.2f%% %-12s %-10s $%,12.2f\n",
                p.getId(), p.getMontoCapital(), p.getModalidad(), p.getPlazoCuotas(),
                p.getTasaInteresAnual(), p.getFechaInicio(), p.getEstado(), p.getSaldoPendiente());
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

    public void mostrarCuotasPropiasCliente(List<Prestamo> prestamos, LocalDate hoy) {
        mensajes.encabezado("MIS CUOTAS DE CREDITO");
        if (prestamos.isEmpty()) {
            System.out.println("No registra creditos con cuotas activas.");
            return;
        }
        for (Prestamo p : prestamos) {
            System.out.printf("\n>> Prestamo #%d | Modalidad: %s | Saldo: $%,.2f | Estado: %s\n",
                p.getId(), p.getModalidad(), p.getSaldoPendiente(), p.getEstado());
            List<Cuota> cuotas = p.getCuotas();
            if (cuotas == null || cuotas.isEmpty()) {
                System.out.println("   No registra cuotas cargadas.");
                continue;
            }
            System.out.printf("   %-5s %-12s %-12s %-12s %-12s %-12s %-10s\n",
                "NUM", "VENCIMIENTO", "CAPITAL", "INTERES", "VALOR CUOTA", "SALDO CUOTA", "ESTADO");
            System.out.println("   -----------------------------------------------------------------------------");
            for (Cuota c : cuotas) {
                System.out.printf("   %-5d %-12s $%,10.2f $%,10.2f $%,10.2f $%,10.2f %-10s\n",
                    c.getNumeroCuota(), c.getFechaVencimiento(), c.getValorCapital(), c.getValorInteres(),
                    c.getValorCuota(), c.getSaldoPendienteCuota(), c.getEstadoVisual(hoy));
            }
        }
    }

    public void mostrarReporteEmpleado(Empleado emp, BigDecimal ingresosAnuales, List<Cliente> clientesAtendidos) {
        mensajes.encabezado("REPORTE PERSONAL DE FUNCIONARIO: " + emp.getNombre().toUpperCase());
        System.out.printf(" ID Funcionario     : %d\n", emp.getId());
        System.out.printf(" Documento          : %s\n", emp.getDocumento());
        System.out.printf(" Rol Institucional  : %s\n", emp.getRol());
        System.out.printf(" Salario Mensual    : $%,.2f\n", emp.getSalario());
        System.out.printf(" Ingresos Anuales   : $%,.2f (12 salarios ordinarios)\n", ingresosAnuales);
        System.out.println("--------------------------------------------------");
        System.out.printf(" Clientes Atendidos : %d clientes con creditos/solicitudes gestionadas\n", clientesAtendidos.size());
        if (!clientesAtendidos.isEmpty()) {
            System.out.printf(" %-5s %-25s %-15s %-20s\n", "ID", "NOMBRE", "DOCUMENTO", "TELEFONO");
            System.out.println(" -----------------------------------------------------------------");
            for (Cliente c : clientesAtendidos) {
                System.out.printf(" %-5d %-25s %-15s %-20s\n",
                    c.getId(), c.getNombre(), c.getDocumento(), c.getTelefono());
            }
        }
        System.out.println("--------------------------------------------------");
    }

    public void mostrarPrestamosDeserializados(List<Prestamo> recuperados) {
        mensajes.encabezado("PRESTAMOS RECUPERADOS DESDE ARCHIVO BINARIO (.DAT)");
        if (recuperados == null || recuperados.isEmpty()) {
            System.out.println("No se encontraron registros de prestamos en el archivo binario.");
            return;
        }
        System.out.println(">> Total prestamos deserializados con exito: " + recuperados.size());
        System.out.println("--------------------------------------------------------------------------------------------------");
        System.out.printf("%-5s %-25s %-15s %-15s %-10s %-10s\n", "ID", "CLIENTE", "CAPITAL", "SALDO PEND.", "MODALIDAD", "ESTADO");
        System.out.println("--------------------------------------------------------------------------------------------------");
        for (Prestamo p : recuperados) {
            System.out.printf("%-5d %-25s $%,12.2f $%,12.2f %-10s %-10s\n",
                p.getId(),
                p.getCliente() != null ? p.getCliente().getNombre() : "N/A",
                p.getMontoCapital(),
                p.getSaldoPendiente(),
                p.getModalidad(),
                p.getEstado());
        }
        System.out.println("--------------------------------------------------------------------------------------------------");
    }
}

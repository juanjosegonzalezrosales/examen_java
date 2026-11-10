package model.amortizacion;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import model.Cuota;
import model.Prestamo;
import model.enums.EstadoPrestamo;

public class AmortizacionAlemana implements IEstrategiaAmortizacion {

    @Override
    public List<Cuota> calcularCuotas(Prestamo p) {
        BigDecimal P = p.getMontoCapital();
        int n = p.getPlazoCuotas();
        LocalDate fechaInicio = p.getFechaInicio() != null ? p.getFechaInicio() : LocalDate.now();
        BigDecimal tasaAnual = p.getTasaInteresAnual();

        BigDecimal i = tasaAnual.divide(BigDecimal.valueOf(1200), 10, RoundingMode.HALF_UP);

        List<Cuota> cuotas = new ArrayList<>();
        BigDecimal saldoRestante = P;
        BigDecimal sumaTotal = BigDecimal.ZERO;

        BigDecimal A = P.divide(BigDecimal.valueOf(n), 2, RoundingMode.HALF_UP);

        for (int k = 1; k <= n; k++) {
            BigDecimal interes = saldoRestante.multiply(i).setScale(2, RoundingMode.HALF_UP);
            BigDecimal capital = (k == n) ? saldoRestante : A;
            BigDecimal cuotaTotal = capital.add(interes);

            if (k == 1) {
                p.setValorCuotaReferencia(cuotaTotal);
            }

            saldoRestante = saldoRestante.subtract(capital).max(BigDecimal.ZERO);
            sumaTotal = sumaTotal.add(cuotaTotal);

            LocalDate vto = fechaInicio.plusMonths(k);
            Cuota cuota = new Cuota(null, 1, k, vto, cuotaTotal, capital, interes, saldoRestante);
            cuotas.add(cuota);
        }

        p.setMontoTotal(sumaTotal);
        p.setSaldoPendiente(P);
        p.setCuotas(cuotas);
        p.setEstado(EstadoPrestamo.ACTIVO);

        return cuotas;
    }
}


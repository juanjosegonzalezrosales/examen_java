package model.amortizacion;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import model.Cuota;
import model.Prestamo;
import model.enums.EstadoPrestamo;

public class AmortizacionFrancesa implements IEstrategiaAmortizacion {

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

        BigDecimal C;
        if (i.compareTo(BigDecimal.ZERO) == 0) {
            C = P.divide(BigDecimal.valueOf(n), 2, RoundingMode.HALF_UP);
        } else {
            double factor = 1.0 - Math.pow(1.0 + i.doubleValue(), -n);
            C = P.multiply(i).divide(BigDecimal.valueOf(factor), 2, RoundingMode.HALF_UP);
        }

        p.setValorCuotaReferencia(C);

        for (int k = 1; k <= n; k++) {
            BigDecimal interes = saldoRestante.multiply(i).setScale(2, RoundingMode.HALF_UP);
            BigDecimal capital;
            if (k == n) {
                capital = saldoRestante;
                C = capital.add(interes);
            } else {
                capital = C.subtract(interes).min(saldoRestante);
            }
            saldoRestante = saldoRestante.subtract(capital).max(BigDecimal.ZERO);
            sumaTotal = sumaTotal.add(C);

            LocalDate vto = fechaInicio.plusMonths(k);
            Cuota cuota = new Cuota(null, 1, k, vto, C, capital, interes, saldoRestante);
            cuotas.add(cuota);
        }

        p.setMontoTotal(sumaTotal);
        p.setSaldoPendiente(P);
        p.setCuotas(cuotas);
        p.setEstado(EstadoPrestamo.ACTIVO);

        return cuotas;
    }
}


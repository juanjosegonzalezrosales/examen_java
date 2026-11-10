package model.amortizacion;

import java.util.List;
import model.Cuota;
import model.Prestamo;

public interface IEstrategiaAmortizacion {
    List<Cuota> calcularCuotas(Prestamo prestamo);
}

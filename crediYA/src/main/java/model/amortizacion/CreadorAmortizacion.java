package model.amortizacion;

import java.util.List;
import model.Cuota;
import model.Prestamo;
import model.enums.ModalidadAmortizacion;

public abstract class CreadorAmortizacion {

    public abstract IEstrategiaAmortizacion crearEstrategia();

    public List<Cuota> calcular(Prestamo prestamo) {
        IEstrategiaAmortizacion estrategia = crearEstrategia();
        return estrategia.calcularCuotas(prestamo);
    }

    public static CreadorAmortizacion obtenerCreador(ModalidadAmortizacion modalidad) {
        if (modalidad == ModalidadAmortizacion.ALEMAN) {
            return new CreadorAleman();
        }
        return new CreadorFrances();
    }
}


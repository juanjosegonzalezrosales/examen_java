package model.amortizacion;

public class CreadorFrances extends CreadorAmortizacion {

    @Override
    public IEstrategiaAmortizacion crearEstrategia() {
        return new AmortizacionFrancesa();
    }
}


package model.amortizacion;

public class CreadorAleman extends CreadorAmortizacion {

    @Override
    public IEstrategiaAmortizacion crearEstrategia() {
        return new AmortizacionAlemana();
    }
}


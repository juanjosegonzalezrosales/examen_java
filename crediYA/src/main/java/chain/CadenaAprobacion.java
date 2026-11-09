package chain;

public final class CadenaAprobacion {

    private CadenaAprobacion() {
    }

    public static ManejadorSolicitud obtenerCadena() {
        ManejadorSolicitud asesor = new AsesorHandler();
        ManejadorSolicitud supervisor = new SupervisorHandler();
        ManejadorSolicitud gerente = new GerenteHandler();

        asesor.setSiguiente(supervisor);
        supervisor.setSiguiente(gerente);

        return asesor;
    }
}

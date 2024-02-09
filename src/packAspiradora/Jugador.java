package packAspiradora;

public class Jugador extends Figura{

    private int numMovimientos;
    private int estado;
    private boolean terminado;
    private boolean visible;
    private int pasos;

    public Jugador(int x, int y, int width, int height, int estado, boolean terminado, boolean visible) {
        super(x, y, width, height);
        this.numMovimientos = 0;
        this.estado = estado;
        this.terminado = terminado;
        this.visible = visible;
        this.pasos = 0; // el contador real de pasos
    }

    public boolean isVisible() {
        return visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    public int getNumMovimientos() {
        return numMovimientos;
    }

    public void agregarUncontador() {
        this.pasos +=1;
        if (this.pasos ==6) { // cada 6 pasos se agregara un numero de movimientos
            numMovimientos += 1;
            pasos =0;
        }
    }

    public boolean isTerminado() {
        return terminado;
    }

    public void setTerminado(boolean terminado) {
        this.terminado = terminado;
    }

    public int getEstado() {
        return estado;
    }
    public void setEstado(int estado) {
        if (estado > 8)
            this.estado = 1;
        else if (estado < 1)
            this.estado = 8;
        else
            this.estado = estado;
    }
}

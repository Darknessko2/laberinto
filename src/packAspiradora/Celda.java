package packAspiradora;

public class Celda extends Figura{
    private boolean visitada;
    private boolean terminada;

    public Celda(int x, int y, int width, int height, boolean visitada, boolean terminada) {
        super(x, y, width, height);
        this.visitada = visitada;
        this.terminada = terminada;
    }

    public boolean isVisitada() {
        return visitada;
    }

    public void setVisitada(boolean visitada) {
        this.visitada = visitada;
    }

    public boolean isTerminada() {
        return terminada;
    }

    public void setTerminada(boolean terminada) {
        this.terminada = terminada;
    }
}

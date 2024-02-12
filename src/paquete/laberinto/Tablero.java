package paquete.laberinto;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;
import java.util.Arrays;

public class Tablero extends JPanel implements KeyListener {
    // TABLERO
    private int boardWidth;
    private int boardHeight;
    private final int sizePixel = 15; // 15
    private JLabel mensajeLabel;
    private JPanel headerPanel;
    private Image bandera;

    // JUGADOR
    private Jugador player, maquina;
    private int currentFrame = 0;
    private Image[] animationFrames;

    // ANIMACION
    private Timer animationJugador;
    private Timer timeCelda;
    private Timer timerMaquina;
    private Timer animationMachine;
    private boolean animacionEnProgreso = false;

    //CELDA
    Obstaculo listaMuros[][];
    private Celda celda;
    private int[][] visited;
    private int[][] twoTimesVisited;

    //MAQUINA
    int[][] coorMachine;
    private int siguienteCoordenada;

    public Tablero(int boardWidth, int boardHeight, JLabel mensajeLabel,JPanel headerPanel) {
        this.boardWidth = boardWidth;
        this.boardHeight = boardHeight;
        this.mensajeLabel = mensajeLabel;
        this.headerPanel = headerPanel;

        setPreferredSize(new Dimension(this.boardWidth,this.boardHeight));
        setBackground(new Color(15,17,26));

        ImageIcon  banderaIcon = new ImageIcon("./src/img/tabla/goal.png");
        bandera = banderaIcon.getImage();

        player = new Jugador(1,1,3,4);
        maquina = new Jugador(1, 1,2,3);

        listaMuros = crearMuros();
        movimientosCelda();


        // las funciones de las teclas
        addKeyListener(this);
        setFocusable(true);
        setFocusTraversalKeysEnabled(false);

        animationFrames = new Image[3];
        updateAnimationFrames("soldier",player);

    }


    private int [][] getCaminoMaquina(){

        int[][] caminoMaquina = visited.clone();
        // extraigo las celdas visitadas por el algoritmo de generacion de laberintos

        int indice = searchCoordenadaInicial(caminoMaquina);
        // busco el indice coordenada inicial 1,1

        int[][] camino = Arrays.copyOf(caminoMaquina,indice);
        // necesito los datos solo hasta que llege a las coordenada inicial 1,1

        return getCaminoOptimizado(camino);
    }

    private int[][] getCaminoOptimizado(int[][] camino) {
        int[][] caminoOptimizado = new int[0][0];

        int xAnterior = camino[camino.length-1][0];
        int yAnterior = camino[camino.length-1][1];

        for (int i = camino.length-1; i >=0; i--) {

            int x = camino[i][0];
            int y = camino[i][1];

            if (xAnterior >(x+5) || xAnterior < (x-5) || yAnterior > (y+5) || yAnterior < (y-5)) {
                continue; // si ha habido un cambio brusco de coordenadas
            }
            else{
                xAnterior = x;
                yAnterior = y;
                caminoOptimizado = increaseArr(caminoOptimizado,new int[]{x,y});
            }
        }

        // y apartir de ese indice se genera un camino aun mejor
        int index = searchInicio(caminoOptimizado);

        caminoOptimizado = Arrays.copyOfRange(caminoOptimizado,index,caminoOptimizado.length);
        // segundo filtro para el camino aun mas optimo se busca las coordenadas 1.6 o 6.1


        return caminoOptimizado;
    }

    private int searchInicio(int [][] coordenada){ // busca la coordenada 1.6  o 6.1
        for (int i = 0; i < coordenada.length; i++) {
            if (coordenada[i][0] == 1 && coordenada[i][1] == 6 ||
                coordenada[i][0] == 6 && coordenada[i][1] == 1)
                return i;
        }
        return -1;
    }


    private int searchCoordenadaInicial(int[][]coordenada){ // La coordenada inicial a buscar es 1,1
        for (int i = 0; i < coordenada.length ; i++) {
            if (coordenada[i][0] == 1 && coordenada[i][1] == 1)
                return i;
        }
        return -1;
    }


    private void movimientoMaquina(int [][] arr) {

            timerMaquina = new Timer(50, new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {

                    if (!animacionEnProgreso) {
                        int[][] caminos = new int[0][0];
                        boolean finalizado = false;
                        int proximoX = arr[siguienteCoordenada][0];
                        int proximoY = arr[siguienteCoordenada][1];
                        int direccion = estadoMachine(proximoX, proximoY); // calcula el estado dependiendo de la proxima coordenada
                        int x = maquina.getX();
                        int y = maquina.getY();

                        while (!finalizado) {
                            if (direccion != -1)
                                maquina.setEstado(direccion);

                            if (direccion == 5)
                                y = y + 1;

                            if (direccion == 3)
                                x = x + 1;

                            if (direccion == 1)
                                y -= 1;

                            if (direccion == 7)
                                x -= 1;

                            updateAnimationFrames("machine",maquina);


                            caminos = increaseArr(caminos, new int[]{x, y});

                            if (proximoX == x && proximoY == y || direccion == -1) {
                                animacionMaquina(caminos);
                                finalizado = true;
                                actualizarEncabezado("MAQUINA PASOS : "+maquina.getNumMovimientos()
                                        +"                        JUGADOR PASOS : "+player.getNumMovimientos(),
                                        new Color(15,17,26),new Color(248, 35, 93));

                            }
                        }
                        siguienteCoordenada++;

                        if (siguienteCoordenada >= coorMachine.length)
                            timerMaquina.stop();

                    }
                }
            });
        timerMaquina.start();
    }

    private void checkVictory() { // si la maquina ya ha llegado al final

            if (maquina.getNumMovimientos() != player.getNumMovimientos()) {
                String ganador = (maquina.getNumMovimientos() < player.getNumMovimientos()) ? "LA MAQUINA" : "EL JUGADOR";
                actualizarEncabezado("EL GANADOR ES "+ganador
                        +"                                   JUGADOR : "+player.getNumMovimientos() +" MAQUINA : "+maquina.getNumMovimientos(),new Color(15,17,26),new Color(250, 246, 100));
                if (ganador.equals("LA MAQUINA"))
                    sonido("src/sound/lost.wav");
                else
                    sonido("src/sound/win.wav");
            }else
                actualizarEncabezado("HA HABIDO EMPATE",new Color(15,17,26),new Color(250, 246, 100));
    }

    private void animacionMaquina(int [][] caminos){

        animacionEnProgreso = true;
        int imagenesPorPaso = caminos.length;
        animationMachine = new Timer(40, new ActionListener() {
            int veces = 0;
            public void actionPerformed(ActionEvent e) {
                if (veces < imagenesPorPaso) {
                    maquina.setX(caminos[veces][0]);
                    maquina.setY(caminos[veces][1]);
                    currentFrame = direction(currentFrame);
                    maquina.agregarUncontador();
                    veces++;
                    repaint();
                }else {
                    animationMachine.stop();
                    animacionEnProgreso = false;
                    if (maquina.getX() == boardWidth / sizePixel-4  && maquina.getY() == boardHeight / sizePixel -4) {
                        maquina.setVisible(false);
                        maquina.setTerminado(true);
                        sonido("src/sound/meta.wav");
                        checkVictory();
                        repaint();
                    }
                }
            }
        });
        animationMachine.start();


    }
    private int estadoMachine(int xProximo,int yProximo){

        if (maquina.getX() == xProximo && maquina.getY() < yProximo)// abajo
            return 5;
        if (maquina.getX() < xProximo && maquina.getY() == yProximo) // derecha
            return 3;
        if (maquina.getX() > xProximo && maquina.getY() == yProximo) // izquierda
            return 7;
        if (maquina.getX() == xProximo && maquina.getY() > yProximo) // arriba
            return 1;

        return -1;
    }


    private void movimientosCelda(){
        int dimensionCelda = 4;
        celda = new Celda(boardWidth/sizePixel-dimensionCelda, boardHeight/sizePixel-dimensionCelda,
                dimensionCelda, dimensionCelda, false, false); //1,1,4,4,false,true

        visited = new int [][]{{celda.getX(),celda.getY()}};
        twoTimesVisited = new int[0][0];

        timeCelda = new Timer(60, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) { // recorre ejes de 5 en 5

                boolean paso = false;
                int[] listaRandom = new int[0];
                do {
                    int random = (int) (Math.random() * 4);  // 4 posibilidades arriba0, abajo1 , derecha2, izquierda3

                    listaRandom = almacenarRandom(random, listaRandom); // se almacenan los distinos random para evitar duplicados

                    int x = getNewX(random);
                    int y = getNewY(random);

                    if (x >= 0 && y >= 0 && x <= boardWidth / sizePixel && y <= boardHeight / sizePixel) {
                        if (!repetido(x, y, visited)) {
                            celda.setX(x);
                            celda.setY(y);
                            visited = increaseArr(visited, getCoordenadas(x, y)); // guardar coordenadas visitadas
                            int xMuro = getNewXmuro(random);
                            int yMuro = getNewYmuro(random);
                            Obstaculo muro = buscarObstaculo(xMuro, yMuro);
                            muro.setVisible(false); // dicho muro ya no esta visible

                            sonido("./src/sound/pop.wav");
                            repaint();
                            paso = true;

                        } else if (listaRandom.length == 4) { // si todos los caminos han fallado
                            for (int i = visited.length - 1; i >= 0 && !paso; i--) { // si no hay camino vuelve en el array a una coordenada anterior
                                int xVisited = visited[i][0];
                                int yVisited = visited[i][1];
                                if (!repetido(xVisited, yVisited, twoTimesVisited)) {

                                    twoTimesVisited = increaseArr(twoTimesVisited, getCoordenadas(xVisited, yVisited));

                                    celda.setX(twoTimesVisited[twoTimesVisited.length - 1][0]);
                                    celda.setY(twoTimesVisited[twoTimesVisited.length - 1][1]);

                                    paso = true;
                                } else if (xVisited == visited[0][0] && yVisited == visited[0][1]) {
                                    celda.setTerminada(true); // la celda ya ha terminado
                                    actualizarEncabezado("PULSA ESPACIO PARA COMENZAR", new Color(15,17,26));
                                    timeCelda.stop();
                                    paso = true;
                                    player.setVisible(true);
                                    sonido("./src/sound/principal.wav");
                                }
                            }
                            repaint();
                        }
                    }
                } while (!paso); // el bucle no dependera de timer
            }
        });

        timeCelda.start();
    }

    private void actualizarEncabezado(String nuevoMensaje, Color newColor) {
        mensajeLabel.setText(nuevoMensaje);
        mensajeLabel.setForeground(Color.cyan);
        headerPanel.setBackground(newColor);
    }
    private void actualizarEncabezado(String nuevoMensaje, Color newColor,Color cMensaje) {
        mensajeLabel.setText(nuevoMensaje);
        mensajeLabel.setForeground(cMensaje);
        headerPanel.setBackground(newColor);
    }


    private int [] almacenarRandom(int random, int[] listaRandom) {
        if (!coincide(random, listaRandom)){
            listaRandom = Arrays.copyOf(listaRandom, listaRandom.length+1);
            listaRandom[listaRandom.length-1] = random;
        }
        return listaRandom;
    }

    private int getNewX(int random){ // el valor de x solo cambia si random es 2 o 3
        int x = celda.getX();
        if (random == 2){ // derecha
            x +=5;
        } else if (random == 3) { // izquierda
            x -=5;
        }
        return x;
    }
    private int getNewY(int random){ // el valor de y cambia si random es 0 o 1
        int y = celda.getY();
        if (random == 0){ // arriba
            y -= 5;
        } else if (random == 1) { // abajo
            y += 5;
        }
        return y;
    }
    private int getNewYmuro(int random){ // se busca el muro que que hay que eliminar x e y
        int yMuro = celda.getY();
        if (random == 0)
            yMuro += 4;
        else
            yMuro -= 1;

        return yMuro;
    }
    private int getNewXmuro(int random){ // coordenada x solo cambiara en estos dos posibles caminos
        int xMuro = celda.getX();
        if (random == 2) // derecha
            xMuro -= 1;
        else if (random == 3) { // izquierda
            xMuro += 4;
        }
        return xMuro;
    }

    private int[][] increaseArr(int [][]coordenadasVisitadas,int[] coordenadas){ // se almacenan las coordenadas visitaadas
        coordenadasVisitadas = Arrays.copyOf(coordenadasVisitadas,coordenadasVisitadas.length+1);
        coordenadasVisitadas[coordenadasVisitadas.length-1] = coordenadas;
        return coordenadasVisitadas;
    }
    private int [] getCoordenadas(int x, int y){
        return new int[]{x,y};
    }
    private Obstaculo buscarObstaculo(int x ,int y){ // buscara el obstaculo en el array bidimensional
        for (Obstaculo[] list : listaMuros)
            for (Obstaculo muro : list)
                if (muro.getX() == x && muro.getY() == y)
                    return muro;

        return null;
    }
    private boolean repetido(int x, int y, int[][] visited){ // si la coordenada ya ha sido visitada
        for (int i = 0; i < visited.length; i++) {
            if (visited[i][0] == x && visited[i][1] == y)
                return true;
        }
        return false;
    }
    private boolean coincide(int random,int[] listaRandom){ // solo admitira numeros no repetidos
        for(int num : listaRandom)
            if (num == random)
                return true;
        return false;
    }

    public void sonido(String ruta){
        try {
            // Cargar el archivo de sonido
            AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(new File(ruta));

            Clip clip = AudioSystem.getClip();

            // Abrir el flujo de audio y cargar los datos en el Clip
            clip.open(audioInputStream);


            clip.start();
            clip.drain();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Obstaculo[][] crearMuros(){
        Obstaculo[][] bidimensional = new Obstaculo[boardHeight/sizePixel][boardWidth/sizePixel];
        int y = 0;
        for (int i = 0; i < boardHeight / sizePixel; i++) {
            int x = 0;
            for (int j = 0; j < boardWidth / sizePixel; j++) {
                if (j%2==0)
                    bidimensional[i][j] = new Obstaculo(x,y,1,6,true); // vertical antes h5
                else {
                    x+=1;
                    bidimensional[i][j] = new Obstaculo(x, y, 5, 1, true); // horizontal
                    x+=4;
                }
            }
            y += 5;
        }
        return bidimensional;
    }
    public void updateAnimationFrames(String ruta,Jugador player) {
        int estado = player.getEstado();
        animationFrames[0] = new ImageIcon("./src/img/"+ruta+"/" + estado + "-0.png").getImage();
        animationFrames[1] = new ImageIcon("./src/img/"+ruta+"/" + estado + "-1.png").getImage();
        animationFrames[2] = new ImageIcon("./src/img/"+ruta+"/" + estado + "-2.png").getImage();
    }

    public int direction(int num){
        if (num >= 2)
            return 0;

        return num+1;
    }


    public void paintComponent(Graphics g){
        super.paintComponent(g);
        draw(g);

    }
    public void draw(Graphics g){

        // tabla
        /*
        for (int i = 0; i < boardWidth/ sizePixel; i++) {
            g.drawLine(i* sizePixel,0,i* sizePixel,boardHeight);
            g.drawLine(0,i* sizePixel,boardWidth,i* sizePixel);
        }

         */

        // el cuadrado de colision dentro del personaje cuanto mas se modifique mejor sera la colision
        int xCuadrado = player.getX()*sizePixel+20;
        int yCuadrado = player.getY()*sizePixel+25;
        int altoCuadrado = (player.getHeight()*sizePixel)-40;
        int anchoCuadrado = (player.getWidth()*sizePixel)-50;

        // muro
        g.setColor(Color.red);
        for (Obstaculo[] list : listaMuros) {
            for (Obstaculo muro : list) {
                if (muro.isVisible()) {

                    if (maquina.isVisible()) {
                        g.setColor(new Color(248, 35, 93));
                    }else if(maquina.isTerminado())
                        g.setColor(new Color(250, 246, 100));
                    else if (!celda.isTerminada()) {
                        g.setColor(Color.white);
                    }else
                        g.setColor(Color.cyan);

                    g.fillRect((muro.getX() * sizePixel), muro.getY() * sizePixel, muro.getWidth() * sizePixel, muro.getHeight() * sizePixel);

                }

            }
        }

        // celda
        if (celda.isTerminada()){
            g.drawImage(bandera, celda.getX()* sizePixel, celda.getY()*sizePixel,celda.getWidth()*sizePixel, celda.getHeight()*sizePixel,this );
        }else
            g.fillRect(celda.getX() * sizePixel, celda.getY() * sizePixel, celda.getWidth() * sizePixel, celda.getHeight() * sizePixel);

        // jugador
        if (player.isVisible())
            g.drawImage(animationFrames[currentFrame], player.getX() * sizePixel, player.getY() *sizePixel,player.getWidth()*sizePixel, player.getHeight()*sizePixel, this);

        // maquina
        if (maquina.isVisible())
            g.drawImage(animationFrames[currentFrame], maquina.getX() * sizePixel, maquina.getY() *sizePixel,maquina.getWidth()*sizePixel, maquina.getHeight()*sizePixel, this);
    }



    @Override
    public void keyTyped(KeyEvent e) {
        teclas(e);
    }
    @Override
    public void keyPressed(KeyEvent e) {
        teclas(e);
    }

    public void teclas(KeyEvent e) {

            if (e.getKeyCode() == 32) { // 32 es el espacio
                if (!player.isTerminado()) {
                    if (!animacionEnProgreso) {
                        animacionJugador(player);
                        actualizarEncabezado("Nº DE PASOS: " + player.getNumMovimientos(), new Color(15, 17, 26));
                    }
                }
            } else if (e.getKeyCode() == KeyEvent.VK_A) {
                player.setEstado(player.getEstado()-1);
                updateAnimationFrames("soldier",player);
            } else if (e.getKeyCode() == KeyEvent.VK_D) {
                player.setEstado(player.getEstado()+1);
                updateAnimationFrames("soldier",player);
            } else if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                if (player.isTerminado()) {
                    maquina.setVisible(true);
                    coorMachine = getCaminoMaquina();
                    movimientoMaquina(coorMachine);
                }
            }
        repaint();


    }

    public int[] nextDirection(int x, int y){

        int xOriginal = x;
        int yOriginal = y;

        switch (player.getEstado()){
            case 1: // arriba
                y = y-1;
                break;
            case 2: // diagonal derecha
                x = x+1;
                y = y-1;
                break;
            case 3: // derecha
                x = x+1;
                break;
            case 4:
                x = x+1;
                y = y+1;
                break;
            case 5: // abajo
                y = y + 1;
                break;
            case 6:
                y = y+1;
                x -=1;
                break;
            case 7: // izquierda
                x -=1;
                break;
            case 8:
                y -= 1;
                x -= 1;
                break;
        }
        if (colision(x, y)){ // si va a chocar no cambian las coordenadas
            return new int[]{xOriginal, yOriginal};

        }else return new int[]{x,y};
    }
    public boolean colision(int x , int y) {

        boolean choque = false;

        for (Obstaculo[] list : listaMuros) {
            for (Obstaculo muro : list) {
                if (muro.isVisible()) {
                    if (deteccion(x, y, muro)) { // si en ALGUNO de los muros hay colision
                        choque = true;
                    }
                }
            }
        }
        return choque;
    }

    private boolean deteccion(int x, int y, Obstaculo muro) { // Si las posibles nuevas coordenadas van a colisionar

        // los datos de las colisiones del cuadrado dentro del jugador
        int altoCuadrado = (player.getHeight()*sizePixel)-40;
        int anchoCuadrado = (player.getWidth()*sizePixel)-50;

        if (muro.getX() * sizePixel > ((x * sizePixel) + 20 + anchoCuadrado)) {
            return false;
        }

        if ((muro.getX() + muro.getWidth()) * sizePixel < (x * sizePixel) + 20) {
            return false;
        }

        if (muro.getY() * sizePixel > ((y * sizePixel) + 25 + altoCuadrado)) {
            return false;
        }

        if ((muro.getY() + muro.getHeight()) * sizePixel < (y * sizePixel) + 25) {
            return false;
        }

        return true;
    }
    private boolean deteccion(int x, int y, Celda celda) { // Si las posibles nuevas coordenadas van a colisionar

        // los datos de las colisiones del cuadrado dentro del jugador
        int altoCuadrado = (player.getHeight()*sizePixel)-40;
        int anchoCuadrado = (player.getWidth()*sizePixel)-50;

        if (celda.getX() * sizePixel > ((x * sizePixel) + 20 + anchoCuadrado)) {
            return false;
        }

        if ((celda.getX() + celda.getWidth()) * sizePixel < (x * sizePixel) + 20) {
            return false;
        }

        if (celda.getY() * sizePixel > ((y * sizePixel) + 25 + altoCuadrado)) {
            return false;
        }

        if ((celda.getY() + celda.getHeight()) * sizePixel < (y * sizePixel) + 25) {
            return false;
        }

        return true;
    }

    public void animacionJugador(Jugador player){
        animacionEnProgreso = true;
        int imagenesPorPaso = 6; // lo ideal es que sea multiplo de 3
        int fps = 30;

        animationJugador = new Timer(1000/fps, new ActionListener() { // 60 40
            int veces = 0;
            public void actionPerformed(ActionEvent e) {
                if (veces < imagenesPorPaso) {
                    if (!player.isTerminado()) {
                        int[] coordenadas = nextDirection(player.getX(), player.getY());
                        player.setX(coordenadas[0]);
                        player.setY(coordenadas[1]);
                        currentFrame = direction(currentFrame);
                        player.agregarUncontador();
                        repaint();
                        veces++;
                    }
                }else {
                    if (player.isVisible() && deteccion(player.getX(), player.getY(),celda)) {
                        sonido("src/sound/meta.wav");
                        player.setVisible(false);
                        actualizarEncabezado("PULSA ENTER PARA QUE EMPIEZE LA MAQUINA",new Color(15,17,26));
                        player.setTerminado(true);
                        repaint();

                    }
                    animationJugador.stop();
                    animacionEnProgreso = false;
                }
            }
        });
        animationJugador.start();
    }


    @Override
    public void keyReleased(KeyEvent e) {

    }
}

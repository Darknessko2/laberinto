package paquete.laberinto;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;

public class Pruebas {
    public class CocheGame extends JPanel implements KeyListener {
        private int boardWidth;
        private int boardHeight;
        private int sizePixel;
        private int nBombas;
        private int nMuros;
        private Image carroImagen,muros,barriles,metaImg,boom,ganador;
        private int[][] coorBombas,coorMuros;
        private String[][]lista;

        private class Car {
            int x;
            int y;
            Car(int x, int y){
                this.x = x;
                this.y = y;
            }
        }

        Car carro;
        CocheGame(int boardWidth,int boardHeight,int sizePixel,int nBombas,int nMuros)  {
            this.boardHeight=boardHeight;
            this.boardWidth=boardWidth;
            this.sizePixel=sizePixel;
            this.nBombas=nBombas;
            this.nMuros=nMuros;

            setPreferredSize(new Dimension(this.boardWidth,this.boardHeight));
            setBackground(Color.BLACK);

            try {
                sonido("./src/packCoche/sound/car-start.wav");
            } catch (UnsupportedAudioFileException e) {
                throw new RuntimeException(e);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            int dimension = boardWidth / sizePixel;

            // coordenadas del coche para que no aparezca al lado de la meta
            int coche_y =(int) (Math.random()*(dimension/2));
            int coche_x =(int) (Math.random()*(dimension/2));

            lista=new String[dimension][dimension];
            rellenarTablar(lista,coche_y,coche_x);
            coordenadasVacias(lista,coche_y,coche_x); // coregir
            coorBombas = rellenarFigura(lista, nBombas, "B");
            coorMuros = rellenarFigura(lista,nMuros,"M");

            carro = new Car(coche_y, coche_x);

            // Cargar imagenes
            ImageIcon icon = new ImageIcon("./src/packCoche/img/carro-deportivo.png");
            carroImagen = icon.getImage();

            ImageIcon barrilesIcon = new ImageIcon("./src/packCoche/img/time-bomb.png");
            barriles = barrilesIcon.getImage();

            ImageIcon murrosIcon = new ImageIcon("./src/packCoche/img/wall.png");
            muros = murrosIcon.getImage();

            ImageIcon meta = new ImageIcon("./src/packCoche/img/goal-flag.png");
            metaImg = meta.getImage();

            ImageIcon explosion = new ImageIcon("./src/packCoche/img/explode.png");
            boom = explosion.getImage();

            ImageIcon win = new ImageIcon("./src/packCoche/img/winner.png");
            ganador = win.getImage();

            // las funciones de las teclas
            addKeyListener(this);
            setFocusable(true);
            setFocusTraversalKeysEnabled(false);
        }

        public void paintComponent(Graphics c){
            super.paintComponent(c);
            draw(c);
        }
        public void draw(Graphics g){
            // tabla
            for (int i = 0; i < boardWidth/ sizePixel; i++) {
                g.drawLine(i* sizePixel,0,i* sizePixel,boardHeight);
                g.drawLine(0,i* sizePixel,boardWidth,i* sizePixel);
            }

            //bombas
            for (int[] coordenadas : coorBombas) {
                g.drawImage(barriles, coordenadas[0] * sizePixel,
                        coordenadas[1] * sizePixel, sizePixel, sizePixel, this);
            }

            //muros
            for (int[] coordenadas : coorMuros) {
                g.drawImage(muros, coordenadas[0] * sizePixel,
                        coordenadas[1] * sizePixel, sizePixel, sizePixel, this);
            }

            // coche
            if (!gameOver())
                g.drawImage(carroImagen, carro.x * sizePixel, carro.y * sizePixel, sizePixel, sizePixel, this);
            else if(pierde(carro.x, carro.y)) {
                g.drawImage(boom, carro.x * sizePixel, carro.y * sizePixel, sizePixel, sizePixel, this);

                try {
                    sonido("./src/packCoche/sound/explosion.wav");
                } catch (UnsupportedAudioFileException e) {
                    throw new RuntimeException(e);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }

            //meta
            if (!gameOver())
                g.drawImage(metaImg,(boardWidth/sizePixel-1) * sizePixel,(boardWidth/sizePixel-1) * sizePixel, sizePixel, sizePixel,this);
            else if (gana(carro.x, carro.y)) {
                g.drawImage(ganador, (boardWidth / sizePixel - 1) * sizePixel, (boardWidth / sizePixel - 1) * sizePixel, sizePixel, sizePixel, this);

                try {
                    sonido("./src/packCoche/sound/victoria.wav");
                } catch (UnsupportedAudioFileException e) {
                    throw new RuntimeException(e);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }

        public void sonido(String ruta) throws UnsupportedAudioFileException, IOException {
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

        @Override
        public void keyTyped(KeyEvent e) {
            // Es necesario porque estamos implementando la interfaz KeyListener.
        }

        @Override
        public void keyPressed(KeyEvent e) {

            if (!gameOver()) {
                int nuevoX = carro.x;
                int nuevoY = carro.y;

                if (e.getKeyCode() == KeyEvent.VK_D) {
                    if (carro.x < boardWidth / sizePixel - 1)
                        nuevoX = carro.x + 1;
                } else if (e.getKeyCode() == KeyEvent.VK_W) {
                    if (carro.y > 0)
                        nuevoY = carro.y - 1;
                } else if (e.getKeyCode() == KeyEvent.VK_S) {
                    if (carro.y < boardWidth / sizePixel - 1)
                        nuevoY = carro.y + 1;
                } else if (e.getKeyCode() == KeyEvent.VK_A) {
                    if (carro.x > 0)
                        nuevoX = carro.x - 1;
                }

                // si hay un muro
                if (!lista[nuevoX][nuevoY].equals("M")) {
                    carro.x = nuevoX;
                    carro.y = nuevoY;
                    repaint();
                    try {
                        sonido("./src/packCoche/sound/short.wav");
                    } catch (UnsupportedAudioFileException ex) {
                        throw new RuntimeException(ex);
                    } catch (IOException ex) {
                        throw new RuntimeException(ex);
                    }
                }
            }
        }

        // METODOS PARA LA LOGICA DEL JUEGO

        //  Rellenara la tabla con X figura y devolvera una array con dichas coordenadas
        static int[][] rellenarFigura(String [][]lista, int elemento, String figura){
            int[][] result = new int [0][0];
            while (elemento>0){
                int columna = (int) (Math.random()*lista.length);
                int fila = (int) (Math.random()*(lista.length));

                if (lista[fila][columna].equals(" ")){
                    lista[fila][columna] = figura;
                    elemento--;
                    result = agrandarArray(result,crearArray(fila,columna));
                }
            }
            return result;
        }
        // llenara la tabla con huecos vacios y luego colocara al coche y la meta
        static void rellenarTablar(String[][]lista,int coche_y,int coche_x){
            for (int i = 0; i < lista.length; i++) {
                for (int j = 0; j < lista[i].length; j++) {
                    lista[i][j]=" ";
                }
            }
            lista[coche_y][coche_x]="C"; // COCHE
            lista[lista.length-1][lista.length-1]="W"; //META
        }
        // con este metodo el coche siempre podra ganar ya que primero se creara un camino libre de obstaculos
        static int[][] coordenadasVacias(String[][] lista , int coche_y, int coche_x) {
            int[][] result = new int [0][0];

            while (coche_x != lista.length - 1 || coche_y != lista.length-1) {
                int random = (int) (Math.random() * 3); //3 caminos posibles

                if (random == 0 && coche_y > 0 && coche_x != lista.length-1) { // arriba
                    coche_y--;
                } else if (random == 2 && coche_y != lista.length - 1) { //abajo
                    coche_y++;
                } else if (random == 1 && coche_x!= lista.length-1 && coche_y > (lista.length/2)) {  // si pasa la mitad podra ir en direccion a la meta
                    coche_x++;
                }

                if (!lista[coche_y][coche_x].equals("C") && !(repetido(result,coche_y,coche_x))) {
                    lista[coche_y][coche_x] = "X"; // si hay un X no se podra colocar bombas ni muros
                    result = agrandarArray(result,crearArray(coche_y,coche_x));
                }

            }
            return result;
        }
        // con este metodo no se repetira las coordenadas que ya estan en el camino por lo que se creara un camino mas dificil para el jugador
        static boolean repetido(int[][] arrGrande,int y , int x){
            for (int i = 0; i < arrGrande.length; i++) {
                if (arrGrande[i][0] == y && arrGrande[i][1]== x)
                    return true;
            }
            return false;
        }

        static int[]crearArray(int y,int x){
            int[] arr = new int[2];
            arr[0]=y;
            arr[1]=x;
            return arr;
        }

        static int[][]agrandarArray(int[][]arrGrande,int[] arr){
            arrGrande = Arrays.copyOf(arrGrande,arrGrande.length+1);
            arrGrande[arrGrande.length-1]=arr;
            return arrGrande;
        }

        // Condiciones de fin del juego
        public boolean gameOver(){
            return (gana(carro.x, carro.y) || pierde(carro.x, carro.y));
        }
        // si llega a la meta
        public boolean gana(int x, int y){
            return (x == boardWidth / sizePixel -1 && y == boardWidth / sizePixel -1);
        }
        // si explota una bomba
        public boolean pierde(int x,int y){
            for (int i = 0; i < coorBombas.length; i++) {
                if (carro.x == coorBombas[i][0] && carro.y == coorBombas[i][1])
                    return true;
            }
            return false;
        }
        @Override
        public void keyReleased(KeyEvent e) {
            // Es necesario porque estamos implementando la interfaz KeyListener.
        }
    }
}

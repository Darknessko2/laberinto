package paquete.laberinto;

import javax.swing.*;
import java.awt.*;

public class Main {
	
    public static void main(String[] args) {
    	final int boardWidth = 914;
    	final int boardHeight = 760;

        JFrame frame = new JFrame("Laberinto");

        frame.setVisible(true);
        frame.setSize(boardWidth , boardHeight);
        frame.setLocationRelativeTo(null);
        frame.setResizable(false);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel headerPanel = new JPanel();
        JLabel mensajeLabel = new JLabel("GENERANDO LABERINTO...");
        mensajeLabel.setForeground(Color.white);
        headerPanel.setBackground(new Color(15,17,26));

        Tablero tablero = new Tablero(boardWidth,boardHeight,mensajeLabel,headerPanel);

        frame.setLayout(new BorderLayout());
        frame.add(headerPanel, BorderLayout.NORTH);
        frame.add(tablero, BorderLayout.CENTER);

        Font font = new Font("Arial", Font.PLAIN, 16);
        mensajeLabel.setFont(font);
        headerPanel.add(mensajeLabel);

        frame.add(tablero);
        frame.pack();
        frame.toFront();
        frame.setAlwaysOnTop(true);
        frame.requestFocus();
        tablero.requestFocusInWindow();

    }
}

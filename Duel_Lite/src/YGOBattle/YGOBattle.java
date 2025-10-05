package YGOBattle;


import APIYgo.Card;
import APIYgo.YgoApiClient;

import java.awt.Image;
import javax.swing.*;
import java.util.List;

public class YGOBattle {
    private JPanel mainPanel;
    private JLabel BATLE;
    private JButton iniciarBatallaButton;
    private JButton REPARTIRCARTASButton;

    private JLabel Label_Turno;

    private JLabel labelJugador1;
    private JLabel labelJugador2;
    private JLabel labelJugador3;

    private JLabel labelMaquina1;
    private JLabel labelMaquina2;
    private JLabel labelMaquina3;

    private JPanel PanelJugador;
    private JPanel PanelMaquina;
    private JLabel PartidasJugador;
    private JLabel partidasMaquina;
    private JLabel labelGanador;
    private JButton REINICIARButton;

    private final YgoApiClient apiClient = new YgoApiClient();

    private List<Card> cartasJugador;
    private List<Card> cartasMaquina;

    // Método para repartir cartas
    private void cargarCartas() {
        // Evita bloquear la interfaz
        new Thread(() -> {
            try {
                //Carga cartas de jugador y máquina en segundo plano
                cartasJugador = apiClient.getRandomCards(3);
                cartasMaquina = apiClient.getRandomCards(3);

                //URL para las cartas boca abajo (Por arreglar)
               /* String backUrl = "https://www.pngkey.com/detail/u2w7u2a9w7y3w7t4_1980-1300-in-yugioh-card-back-yu/";
                ImageIcon backIcon = new ImageIcon(new java.net.URL(backUrl));
                Image backScaled = backIcon.getImage().getScaledInstance(120, 180, Image.SCALE_SMOOTH);
                ImageIcon backFinal = new ImageIcon(backScaled);*/

                //Ahora actualizamos la interfaz (en el hilo principal de Swing)
                SwingUtilities.invokeLater(() -> {
                    JLabel[] labelsJugador = {labelJugador1, labelJugador2, labelJugador3};
                    JLabel[] labelsMaquina = {labelMaquina1, labelMaquina2, labelMaquina3};

                    // para mostrar cartas del jugador (boca arriba)
                    for (int i = 0; i < cartasJugador.size() && i < labelsJugador.length; i++) {
                        Card c = cartasJugador.get(i);
                        JLabel lbl = labelsJugador[i];
                        try {
                            ImageIcon icon = new ImageIcon(new java.net.URL(c.getImageUrl()));
                            Image scaled = icon.getImage().getScaledInstance(120, 180, Image.SCALE_SMOOTH);
                            lbl.setIcon(new ImageIcon(scaled));
                            lbl.setText("<html><center>" + c.getName() +
                                    "<br>ATK: " + c.getAtk() +
                                    "<br>DEF: " + c.getDef() +
                                    "</center></html>");
                            lbl.setHorizontalTextPosition(JLabel.CENTER);
                            lbl.setVerticalTextPosition(JLabel.BOTTOM);
                            lbl.setHorizontalAlignment(SwingConstants.CENTER);
                        } catch (Exception e) {
                            lbl.setText(c.getName() + " (sin imagen)");
                            lbl.setIcon(null);
                        }
                    }

                    //Logica Provisional de Maquina
                    for (int i = 0; i < cartasMaquina.size() && i < labelsMaquina.length; i++) {
                        Card c = cartasMaquina.get(i);
                        JLabel lbl = labelsMaquina[i];
                        try {
                            ImageIcon icon = new ImageIcon(new java.net.URL(c.getImageUrl()));
                            Image scaled = icon.getImage().getScaledInstance(120, 180, Image.SCALE_SMOOTH);
                            lbl.setIcon(new ImageIcon(scaled));
                            lbl.setText("<html><center>" + c.getName() +
                                    "<br>ATK: " + c.getAtk() +
                                    "<br>DEF: " + c.getDef() +
                                    "</center></html>");
                            lbl.setHorizontalTextPosition(JLabel.CENTER);
                            lbl.setVerticalTextPosition(JLabel.BOTTOM);
                            lbl.setHorizontalAlignment(SwingConstants.CENTER);
                        } catch (Exception e) {
                            lbl.setText(c.getName() + " (sin imagen)");
                            lbl.setIcon(null);
                        }
                    }

                    // Mostrar cartas de la máquina (boca abajo)
                   /* for (JLabel lbl : labelsMaquina) {
                        lbl.setIcon(backFinal);
                        lbl.setText("");
                        lbl.setHorizontalAlignment(SwingConstants.CENTER);
                    }*/

                    //Refrescamos ambos paneles
                    PanelJugador.revalidate();
                    PanelJugador.repaint();
                    PanelMaquina.revalidate();
                    PanelMaquina.repaint();
                });

            } catch (Exception ex) {
                SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(mainPanel,
                        "Error al cargar cartas: " + ex.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE));
            }
        }).start();
    }

    //Constructor
    public YGOBattle() {
        REPARTIRCARTASButton.addActionListener(e -> cargarCartas());

        iniciarBatallaButton.addActionListener(e ->
                JOptionPane.showMessageDialog(mainPanel,
                        "¡La batalla comenzará próximamente!",
                        "Iniciar Batalla", JOptionPane.INFORMATION_MESSAGE));
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("YGOBattle");
        frame.setContentPane(new YGOBattle().mainPanel);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
    }

    private void createUIComponents() {
        // TODO: place custom component creation code here
    }

}

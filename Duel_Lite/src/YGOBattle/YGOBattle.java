package YGOBattle;

import APIYgo.Card;
import APIYgo.YgoApiClient;

import javax.swing.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.Random;

public class YGOBattle {
    private JPanel mainPanel;
    private JLabel BATLE;
    private JButton iniciarBatallaButton;
    private JButton REPARTIRCARTASButton;
    private JLabel labelJugador1;
    private JLabel labelJugador2;
    private JLabel labelJugador3;

    private List<Card> cartasJugador;
    private List<Card> cartasMaquina;

    private YgoApiClient apiClient = new YgoApiClient();
    private String turnoActual; // "Jugador" o "Máquina"
    private Card cartaSeleccionadaJugador;

    public YGOBattle() {
        // 🔹 Repartir cartas al presionar el botón
        REPARTIRCARTASButton.addActionListener(e -> repartirCartas());

        // 🔹 Asignar turno aleatorio al presionar Iniciar Batalla
        iniciarBatallaButton.addActionListener(e -> iniciarBatalla());

        // 🔹 Escuchadores de clic para las cartas del jugador
        configurarClicksCartasJugador();
    }

    private void repartirCartas() {
        SwingWorker<Void, String> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() {
                try {
                    cartasJugador = apiClient.getRandomCards(3);
                    cartasMaquina = apiClient.getRandomCards(3);
                    publish("Cartas repartidas correctamente.");
                } catch (Exception ex) {
                    publish("Error al repartir cartas: " + ex.getMessage());
                }
                return null;
            }

            @Override
            protected void process(List<String> chunks) {
                JOptionPane.showMessageDialog(mainPanel, chunks.get(0));
            }
        };
        worker.execute();
    }

    private void iniciarBatalla() {
        if (cartasJugador == null || cartasMaquina == null) {
            JOptionPane.showMessageDialog(mainPanel, "Primero reparte las cartas antes de iniciar la batalla.");
            return;
        }

        Random random = new Random();
        boolean jugadorEmpieza = random.nextBoolean();
        turnoActual = jugadorEmpieza ? "Jugador" : "Máquina";

        JOptionPane.showMessageDialog(mainPanel,
                "¡La batalla comienza!\n" + turnoActual + " tiene el primer turno.",
                "Inicio de batalla",
                JOptionPane.INFORMATION_MESSAGE);
    }

    // 🔹 Configura eventos de clic sobre las cartas del jugador
    private void configurarClicksCartasJugador() {
        JLabel[] labels = {labelJugador1, labelJugador2, labelJugador3};

        for (int i = 0; i < labels.length; i++) {
            final int index = i;
            labels[i].addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    if (!"Jugador".equals(turnoActual)) {
                        JOptionPane.showMessageDialog(mainPanel, "No es tu turno todavía.");
                        return;
                    }
                    if (cartasJugador == null || cartasJugador.size() < 3) {
                        JOptionPane.showMessageDialog(mainPanel, "Primero debes repartir las cartas.");
                        return;
                    }

                    cartaSeleccionadaJugador = cartasJugador.get(index);
                    JOptionPane.showMessageDialog(mainPanel,
                            "Has elegido: " + cartaSeleccionadaJugador.getName() +
                                    " (ATK: " + cartaSeleccionadaJugador.getAtk() +
                                    " / DEF: " + cartaSeleccionadaJugador.getDef() + ")");
                    // Aquí más adelante compararemos con la carta de la máquina
                }
            });
        }
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("YGOBattle");
        frame.setContentPane(new YGOBattle().mainPanel);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
    }
}

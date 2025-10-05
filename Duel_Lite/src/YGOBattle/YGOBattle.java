package YGOBattle;


import APIYgo.Card;
import APIYgo.YgoApiClient;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

public class YGOBattle {
    private JPanel mainPanel;
    private JLabel BATLE;
    private JButton iniciarBatallaButton;
    private JButton CARTASJUGADORButton;
    private JButton CARTASMAQUINAButton;

    private YgoApiClient apiClient = new YgoApiClient();

    //para descargar las cartas sin bloquear la interfaz graf
    private void cargarCartas() {

        SwingWorker<Void, String> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() {
                try {
                    List<Card> cartas = apiClient.getRandomCards(3);
                    StringBuilder sb = new StringBuilder("Tus cartas:\n");
                    for (Card c : cartas) {
                        sb.append("- ").append(c.toString()).append("\n");
                    }
                    publish(sb.toString());
                } catch (Exception ex) {
                    publish("Error al cargar cartas: " + ex.getMessage());
                }
                return null;
            }

            @Override
            protected void process(List<String> chunks) {
                JOptionPane.showMessageDialog(mainPanel, chunks.get(chunks.size() - 1));
            }
        };
        worker.execute();
    }
    //Conexión del boton iniciar batalla
    public YGOBattle() {
        iniciarBatallaButton.addActionListener(e -> cargarCartas());
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

package YGOBattle;

import APIYgo.Card;
import APIYgo.YgoApiClient;

import javax.swing.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.Random;

public class YGOBattle {
    // Componentes de la interfaz gráfica
    private JPanel mainPanel;
    private JLabel BATLE;
    private JButton iniciarBatallaButton;
    private JButton REPARTIRCARTASButton;
    private JButton REINICIARButton;

    // Paneles donde se mostrarán las cartas
    private JPanel PanelJugador;
    private JPanel PanelMaquina;
    // Labels para las cartas
    private JLabel labelJugador1;
    private JLabel labelJugador2;
    private JLabel labelJugador3;

    private JLabel labelMaquina1;
    private JLabel labelMaquina2;
    private JLabel labelMaquina3;
    // Labels para mostrar estadísticas del duelo
    private JLabel PartidasJugador;
    private JLabel partidasMaquina;
    private JLabel labelGanador;
    private JLabel Label_Turno;
    private JTextArea textArea1;
    // Variables de control del juego
    private int puntosJugador = 0;
    private int puntosMaquina = 0;
    private int rondasJugadas = 0;

    //guardan las cartas obtenidas desde la api
    private List<Card> cartasJugador;
    private List<Card> cartasMaquina;

    // Cliente para obtener cartas desde la API de Yu-Gi-Oh
    private YgoApiClient apiClient = new YgoApiClient();
    private String turnoActual; // "Jugador" o "Máquina"
    // Cartas seleccionadas por cada uno
    private CartaJugada cartaSeleccionadaJugador;
    private CartaJugada cartaSeleccionadaMaquina;

    public YGOBattle() {
        REPARTIRCARTASButton.addActionListener(e -> repartirCartas());
        iniciarBatallaButton.addActionListener(e -> iniciarBatalla());
        REINICIARButton.addActionListener(e -> reiniciarDuelo());
        configurarClicksCartasJugador();
    }

    // Clase interna para representar una carta jugada con su posición
    private static class CartaJugada {
        Card carta;
        String posicion; // "Ataque" o "Defensa"

        CartaJugada(Card carta, String posicion) {
            this.carta = carta;
            this.posicion = posicion;
        }
    }

    // --- MÉTODO NUEVO ---
    private void log(String mensaje) {
        if (textArea1 != null) {
            textArea1.append(mensaje + "\n");
            textArea1.setCaretPosition(textArea1.getDocument().getLength());
        }
    }

    //reparte las cartas a jugador y máquina desde la api
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
                log(chunks.get(0));
                mostrarCartas();
            }
        };
        worker.execute();
    }

    // elegir quien va  a comenzar a jugar
    private void iniciarBatalla() {
        if (cartasJugador == null || cartasMaquina == null) {
            JOptionPane.showMessageDialog(mainPanel, "Primero reparte las cartas antes de iniciar la batalla.");
            log("Primero reparte las cartas antes de iniciar la batalla.");
            return;
        }
        // Turno aleatorio
        Random random = new Random();
        boolean jugadorEmpieza = random.nextBoolean();
        turnoActual = jugadorEmpieza ? "Jugador" : "Máquina";

        Label_Turno.setText("Turno: " + turnoActual);
        JOptionPane.showMessageDialog(mainPanel,
                "¡La batalla comienza!\n" + turnoActual + " tiene el primer turno.");
        log("¡La batalla comienza! " + turnoActual + " tiene el primer turno.");

        if (turnoActual.equals("Máquina")) {
            turnoMaquina(null);
        }
    }

    // Asigna eventos de clic a las cartas del jugador
    private void configurarClicksCartasJugador() {
        JLabel[] labels = {labelJugador1, labelJugador2, labelJugador3};

        for (int i = 0; i < labels.length; i++) {
            final int index = i;
            labels[i].addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    if (!"Jugador".equals(turnoActual)) {
                        JOptionPane.showMessageDialog(mainPanel, "No es tu turno todavía.");
                        log("Intento inválido: no es tu turno todavía.");
                        return;
                    }
                    if (cartasJugador == null || cartasJugador.size() < 3) {
                        JOptionPane.showMessageDialog(mainPanel, "Primero debes repartir las cartas.");
                        log("Primero debes repartir las cartas.");
                        return;
                    }
                    Card cartaElegida = cartasJugador.get(index);
                    String[] opciones = {"Ataque", "Defensa"};
                    String posicion = (String) JOptionPane.showInputDialog(
                            mainPanel,
                            "Elige la posición de la carta:",
                            "Posición de la carta",
                            JOptionPane.QUESTION_MESSAGE,
                            null,
                            opciones,
                            "Ataque");

                    if (posicion == null) return;

                    cartaSeleccionadaJugador = new CartaJugada(cartaElegida, posicion);

                    JOptionPane.showMessageDialog(mainPanel,
                            "Has elegido: " + cartaElegida.getName() + "\nPosición: " + posicion +
                                    "\n(ATK: " + cartaElegida.getAtk() +
                                    " / DEF: " + cartaElegida.getDef() + ")");
                    log("Jugador juega: " + cartaElegida.getName() + " (" + posicion + ") ATK:" + cartaElegida.getAtk() + " DEF:" + cartaElegida.getDef());

                    turnoActual = "Máquina";
                    Label_Turno.setText("Turno: Máquina");
                    log("Turno: Máquina");
                    turnoMaquina(cartaSeleccionadaJugador);
                }
            });
        }
    }

    //Logica del turno de la máquina (elige carta y posición aleatoriamente)
    private void turnoMaquina(CartaJugada jugadaJugador) {
        new Thread(() -> {
            try {
                Thread.sleep(1000);
                Random random = new Random();
                int index = random.nextInt(cartasMaquina.size());
                Card cartaElegida = cartasMaquina.get(index);

                String posicion = random.nextBoolean() ? "Ataque" : "Defensa";
                cartaSeleccionadaMaquina = new CartaJugada(cartaElegida, posicion);

                JLabel[] labelsMaquina = {labelMaquina1, labelMaquina2, labelMaquina3};
                ImageIcon icon = new ImageIcon(new java.net.URL(cartaElegida.getImageUrl()));
                ImageIcon scaled = new ImageIcon(icon.getImage().getScaledInstance(90, 130, java.awt.Image.SCALE_SMOOTH));
                labelsMaquina[index].setIcon(scaled);
                labelsMaquina[index].setText("<html><center>" + cartaElegida.getName() + "<br>ATK: " + cartaElegida.getAtk() + "<br>DEF: " + cartaElegida.getDef() + "<br>(" + posicion + ")</center></html>");
                labelsMaquina[index].setHorizontalTextPosition(SwingConstants.CENTER);
                labelsMaquina[index].setVerticalTextPosition(SwingConstants.BOTTOM);

                JOptionPane.showMessageDialog(mainPanel,
                        "La máquina juega: " + cartaElegida.getName() + "\nPosición: " + posicion);
                log("Máquina juega: " + cartaElegida.getName() + " (" + posicion + ") ATK:" + cartaElegida.getAtk() + " DEF:" + cartaElegida.getDef());

                if (jugadaJugador != null) {
                    compararCartas(jugadaJugador, cartaSeleccionadaMaquina);
                }

                turnoActual = "Jugador";
                Label_Turno.setText("Turno: Jugador");
                log("Turno: Jugador");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    //Compara las cartas sus posiciones y valores en ataque y defensa
    private void compararCartas(CartaJugada jugador, CartaJugada maquina) {
        String resultado;
        rondasJugadas++;

        int valorJugador, valorMaquina;

        if (jugador.posicion.equals("Ataque") && maquina.posicion.equals("Ataque")) {
            valorJugador = jugador.carta.getAtk();
            valorMaquina = maquina.carta.getAtk();
        } else if (jugador.posicion.equals("Ataque") && maquina.posicion.equals("Defensa")) {
            valorJugador = jugador.carta.getAtk();
            valorMaquina = maquina.carta.getDef();
        } else if (jugador.posicion.equals("Defensa") && maquina.posicion.equals("Ataque")) {
            valorJugador = jugador.carta.getDef();
            valorMaquina = maquina.carta.getAtk();
        } else {
            valorJugador = jugador.carta.getDef();
            valorMaquina = maquina.carta.getDef();
        }

        if (valorJugador > valorMaquina) {
            puntosJugador++;
            resultado = "El jugador gana la ronda " + rondasJugadas + " 🎉";
        } else if (valorMaquina > valorJugador) {
            puntosMaquina++;
            resultado = "La máquina gana la ronda " + rondasJugadas + " 🤖";
        } else {
            resultado = "Empate en la ronda " + rondasJugadas;
        }

        JOptionPane.showMessageDialog(mainPanel, resultado);
        log(resultado);

        PartidasJugador.setText("Partidas ganadas: " + puntosJugador);
        partidasMaquina.setText("Partidas ganadas: " + puntosMaquina);

        if (puntosJugador == 2 || puntosMaquina == 2) {
            String ganador = (puntosJugador == 2) ? "Jugador" : "Máquina";
            labelGanador.setText("GANADOR: " + ganador);
            JOptionPane.showMessageDialog(mainPanel, "🎊 ¡" + ganador + " gana el duelo!");
            log("🎊 ¡" + ganador + " gana el duelo!");
            reiniciarDuelo();
        }
    }

    // Reinicia todo
    private void reiniciarDuelo() {
        puntosJugador = 0;
        puntosMaquina = 0;
        rondasJugadas = 0;
        cartasJugador = null;
        cartasMaquina = null;
        labelGanador.setText("");
        PartidasJugador.setText("Partidas ganadas: 0");
        partidasMaquina.setText("Partidas ganadas: 0");
        Label_Turno.setText("Turno: -");
        log("Duelo reiniciado.\n");
    }

    // Muestra las imágenes e info de las cartas del jugador y oculta las de la máquina
    private void mostrarCartas() {
        try {
            JLabel[] labelsJugador = {labelJugador1, labelJugador2, labelJugador3};
            JLabel[] labelsMaquina = {labelMaquina1, labelMaquina2, labelMaquina3};

            for (int i = 0; i < cartasJugador.size() && i < labelsJugador.length; i++) {
                Card c = cartasJugador.get(i);
                ImageIcon icon = new ImageIcon(new java.net.URL(c.getImageUrl()));
                ImageIcon scaled = new ImageIcon(icon.getImage().getScaledInstance(90, 130, java.awt.Image.SCALE_SMOOTH));
                labelsJugador[i].setIcon(scaled);
                labelsJugador[i].setText("<html><center>" + c.getName() + "<br>ATK: " + c.getAtk() + "<br>DEF: " + c.getDef() + "</center></html>");
                labelsJugador[i].setHorizontalTextPosition(SwingConstants.CENTER);
                labelsJugador[i].setVerticalTextPosition(SwingConstants.BOTTOM);
            }

            for (JLabel label : labelsMaquina) {
                label.setIcon(null);
                label.setText("🂠");
            }

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(mainPanel, "Error al mostrar las imágenes: " + e.getMessage());
            log("Error al mostrar las imágenes: " + e.getMessage());
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

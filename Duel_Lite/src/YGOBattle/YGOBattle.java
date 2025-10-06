package YGOBattle;

import APIYgo.Card;
import APIYgo.YgoApiClient;

import javax.swing.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.Random;
import java.awt.*;
import javax.swing.border.*;


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

    private boolean cartasRepartidas = false;


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

        aplicarEstiloAnime();


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
    // reparte las cartas a jugador y máquina desde la api
    private void repartirCartas() {
        // Evitar que reparta dos veces
        if (cartasRepartidas) {
            JOptionPane.showMessageDialog(mainPanel, "Las cartas ya fueron repartidas. No puedes volver a hacerlo.");
            log("Intento de repartir cartas nuevamente bloqueado.");
            return;
        }

        SwingWorker<Void, String> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() {
                try {
                    cartasJugador = apiClient.getRandomCards(3);
                    cartasMaquina = apiClient.getRandomCards(3);
                    publish("✅ Cartas repartidas correctamente.");
                } catch (Exception ex) {
                    publish("No se pudieron cargar las cartas.\nVerifica tu conexión a Internet o intenta nuevamente.\n" + ex.getMessage());
                    ex.printStackTrace();
                }
                return null;
            }

            @Override
            protected void process(List<String> chunks) {
                String mensaje = chunks.get(chunks.size() - 1);
                JOptionPane.showMessageDialog(mainPanel, mensaje);
                log(mensaje);

                if (mensaje.contains("✅")) {
                    mostrarCartas();

                    // Marcar que ya se repartieron
                    cartasRepartidas = true;

                    // Deshabilitar el botón para evitar repartir otra vez
                    REPARTIRCARTASButton.setEnabled(false);

                    // Verificar si ambas manos están listas para permitir iniciar
                    if (cartasJugador != null && cartasMaquina != null &&
                            cartasJugador.size() == 3 && cartasMaquina.size() == 3) {
                        iniciarBatallaButton.setEnabled(true);
                        log("✅ Cartas listas. Puedes iniciar la batalla.");
                    } else {
                        iniciarBatallaButton.setEnabled(false);
                        log("❌ Las cartas no se cargaron correctamente.");
                    }
                }
            }
        };
        worker.execute();
    }




    // elegir quien va  a comenzar a jugar
    private void iniciarBatalla() {
        if (!cartasRepartidas || cartasJugador == null || cartasMaquina == null ||
                cartasJugador.size() < 3 || cartasMaquina.size() < 3) {
            JOptionPane.showMessageDialog(mainPanel, "No puedes iniciar la batalla hasta que ambos tengan sus 3 cartas cargadas.");
            log("Intento de iniciar batalla sin cartas completas.");
            return;
        }

        Random random = new Random();
        boolean jugadorEmpieza = random.nextBoolean();
        turnoActual = jugadorEmpieza ? "Jugador" : "Máquina";

        Label_Turno.setText("Turno: " + turnoActual);
        JOptionPane.showMessageDialog(mainPanel, "¡La batalla comienza!\n" + turnoActual + " tiene el primer turno.");
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
        JFrame frame = (JFrame) SwingUtilities.getWindowAncestor(mainPanel);
        cartasRepartidas = false;
        REPARTIRCARTASButton.setEnabled(true);
        iniciarBatallaButton.setEnabled(false);
        frame.dispose(); // Cierra la ventana actual
        SwingUtilities.invokeLater(() -> {
            JFrame newFrame = new JFrame("YGOBattle");
            newFrame.setContentPane(new YGOBattle().mainPanel);
            newFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            newFrame.pack();
            newFrame.setVisible(true);
        });
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
                label.setText("");
            }

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(mainPanel, "Error al mostrar las imágenes: " + e.getMessage());
            log("Error al mostrar las imágenes: " + e.getMessage());
        }
    }
    private void aplicarEstiloAnime() {
        // Colores base
        Color fondoPrincipal = new Color(240, 240, 255);
        Color fondoCampo = new Color(210, 225, 255);
        Color fondoBoton = new Color(255, 214, 120);
        Color bordeCampo = new Color(120, 150, 255);
        Color texto = new Color(30, 30, 30);

        // Fuentes
        Font fuenteTitulo = new Font("SansSerif", Font.BOLD, 18);
        Font fuenteNormal = new Font("SansSerif", Font.PLAIN, 14);

        //Config panel principal
        mainPanel.setBackground(fondoPrincipal);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        mainPanel.setLayout(new BorderLayout(15, 15));

        //Campo de batalla dividido: máquina arriba, jugador abajo
        JPanel campoDeBatalla = new JPanel(new GridLayout(2, 1, 0, 20));
        campoDeBatalla.setOpaque(false);

        // --- PANEL MÁQUINA ---
        JPanel panelMaquinaContainer = new JPanel(new FlowLayout(FlowLayout.CENTER, 40, 15));
        panelMaquinaContainer.setBackground(fondoCampo);
        panelMaquinaContainer.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(bordeCampo, 2),
                "MÁQUINA",
                0, 0, fuenteTitulo, texto
        ));

        // --- PANEL JUGADOR ---
        JPanel panelJugadorContainer = new JPanel(new FlowLayout(FlowLayout.CENTER, 40, 15));
        panelJugadorContainer.setBackground(fondoCampo);
        panelJugadorContainer.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(bordeCampo, 2),
                "JUGADOR",
                0, 0, fuenteTitulo, texto
        ));

        // Ajuste de tamaño de las cartas
        Dimension tamCarta = new Dimension(150, 210);

        JLabel[] cartasM = {labelMaquina1, labelMaquina2, labelMaquina3};
        JLabel[] cartasJ = {labelJugador1, labelJugador2, labelJugador3};

        for (JLabel c : cartasM) {
            c.setPreferredSize(tamCarta);
            c.setHorizontalAlignment(SwingConstants.CENTER);
            c.setVerticalAlignment(SwingConstants.CENTER);
            c.setVerticalTextPosition(SwingConstants.BOTTOM);
            c.setHorizontalTextPosition(SwingConstants.CENTER);
            c.setFont(new Font("SansSerif", Font.PLAIN, 12));
            panelMaquinaContainer.add(c);
        }

        for (JLabel c : cartasJ) {
            c.setPreferredSize(tamCarta);
            c.setHorizontalAlignment(SwingConstants.CENTER);
            c.setVerticalAlignment(SwingConstants.CENTER);
            c.setVerticalTextPosition(SwingConstants.BOTTOM);
            c.setHorizontalTextPosition(SwingConstants.CENTER);
            c.setFont(new Font("SansSerif", Font.PLAIN, 12));
            panelJugadorContainer.add(c);
        }

        campoDeBatalla.add(panelMaquinaContainer);
        campoDeBatalla.add(panelJugadorContainer);

        // Panel lateral derecho (log)
        JPanel panelLog = new JPanel(new BorderLayout());
        panelLog.setPreferredSize(new Dimension(300, 0));
        panelLog.setBackground(new Color(0, 0, 0, 60));
        textArea1.setEditable(false);
        textArea1.setFont(new Font("Monospaced", Font.PLAIN, 13));
        textArea1.setBackground(new Color(255, 255, 255, 220));
        textArea1.setForeground(Color.BLACK);
        JScrollPane scrollLog = new JScrollPane(textArea1);
        scrollLog.setBorder(BorderFactory.createLineBorder(bordeCampo, 2));
        panelLog.add(scrollLog, BorderLayout.CENTER);

        // Panel inferior con botones
        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.CENTER, 25, 10));
        panelBotones.setOpaque(false);

        JButton[] botones = {REPARTIRCARTASButton, iniciarBatallaButton, REINICIARButton};
        for (JButton b : botones) {
            b.setBackground(fondoBoton);
            b.setForeground(texto);
            b.setFont(fuenteNormal);
            b.setFocusPainted(false);
            b.setBorder(BorderFactory.createLineBorder(new Color(100, 100, 100), 2, true));
            b.setPreferredSize(new Dimension(180, 45));
        }

        panelBotones.add(REPARTIRCARTASButton);
        panelBotones.add(iniciarBatallaButton);
        panelBotones.add(REINICIARButton);


        mainPanel.removeAll();
        mainPanel.add(campoDeBatalla, BorderLayout.CENTER);
        mainPanel.add(panelLog, BorderLayout.EAST);
        mainPanel.add(panelBotones, BorderLayout.SOUTH);

        mainPanel.revalidate();
        mainPanel.repaint();
    }


    public static void main(String[] args) {
        JFrame frame = new JFrame("YGOBattle");
        frame.setContentPane(new YGOBattle().mainPanel);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
    }
}

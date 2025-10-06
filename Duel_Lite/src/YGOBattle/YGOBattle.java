package YGOBattle;

import APIYgo.Card;
import APIYgo.YgoApiClient;

import javax.swing.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.Random;
import java.awt.*;

/**
 * Versión corregida y funcional de YGOBattle.
 * Inicializa todos los componentes y evita reinicios repetidos de la ventana.
 */
public class YGOBattle {
    // Componentes de la interfaz gráfica
    private JPanel mainPanel;
    private JButton iniciarBatallaButton;
    private JButton REPARTIRCARTASButton;
    private JButton REINICIARButton;

    // Paneles donde se mostrarán las cartas
    private JPanel PanelMaquina;
    private JPanel PanelJugador;
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
    private JTextArea JT;
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

    // Tamaño por defecto para imágenes (ajustable)
    private final Dimension tamCarta = new Dimension(150, 210);

    public YGOBattle() {
        // Inicializar componentes Swing manualmente (evita dependencia del .form)
        initComponents();

        // Configurar listeners
        REPARTIRCARTASButton.addActionListener(e -> repartirCartas());
        iniciarBatallaButton.addActionListener(e -> iniciarBatalla());
        REINICIARButton.addActionListener(e -> reiniciarDuelo());
        configurarClicksCartasJugador();

        aplicarEstiloAnime();

        // Estado inicial
        iniciarBatallaButton.setEnabled(false);
        REPARTIRCARTASButton.setEnabled(true);
    }

    // Inicializa todos los componentes Swing (para evitar NullPointerException)
    private void initComponents() {
        mainPanel = new JPanel();
        iniciarBatallaButton = new JButton("Iniciar Batalla");
        REPARTIRCARTASButton = new JButton("Repartir Cartas");
        REINICIARButton = new JButton("Reiniciar");

        PanelMaquina = new JPanel();
        PanelJugador = new JPanel();

        labelJugador1 = new JLabel();
        labelJugador2 = new JLabel();
        labelJugador3 = new JLabel();

        labelMaquina1 = new JLabel();
        labelMaquina2 = new JLabel();
        labelMaquina3 = new JLabel();

        PartidasJugador = new JLabel("Partidas ganadas: 0");
        partidasMaquina = new JLabel("Partidas ganadas: 0");
        labelGanador = new JLabel("GANADOR: -");
        Label_Turno = new JLabel("Turno: -");
        JT = new JTextArea(10, 25);
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
        if (JT != null) {
            JT.append(mensaje + "\n");
            JT.setCaretPosition(JT.getDocument().getLength());
        }
    }

    //reparte las cartas a jugador y máquina desde la api
    private void repartirCartas() {
        // Evitar que reparta dos veces
        if (cartasRepartidas) {
            JOptionPane.showMessageDialog(mainPanel, "Las cartas ya fueron repartidas. No puedes volver a hacerlo.");
            log("Intento de repartir cartas nuevamente bloqueado.");
            return;
        }

        REPARTIRCARTASButton.setEnabled(false);


        SwingWorker<Void, String> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() {
                try {
                    cartasJugador = apiClient.getRandomCards(3);
                    cartasMaquina = apiClient.getRandomCards(3);
                    publish("✅ Cartas repartidas correctamente.");
                } catch (Exception ex) {
                    publish("No se pudieron cargar las cartas. " + ex.getMessage());
                    ex.printStackTrace();
                }
                return null;
            }

            @Override
            protected void process(java.util.List<String> chunks) {
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
                } else {
                    // falló, permitir reintentar
                    REPARTIRCARTASButton.setEnabled(true);
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

        // Si la máquina empieza, ejecutar su turno
        if ("Máquina".equals(turnoActual)) {
            turnoMaquina(null);
        }
    }

    // Asigna eventos de clic a las cartas del jugador
    private void configurarClicksCartasJugador() {
        JLabel[] labels = {labelJugador1, labelJugador2, labelJugador3};

        for (int i = 0; i < labels.length; i++) {
            final int index = i;
            JLabel lbl = labels[i];
            lbl.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            lbl.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    // Protecciones
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
                    if (index >= cartasJugador.size()) {
                        JOptionPane.showMessageDialog(mainPanel, "Carta inválida.");
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
        // SwingWorker para no bloquear la UI
        SwingWorker<CartaJugada, Void> worker = new SwingWorker<>() {
            @Override
            protected CartaJugada doInBackground() throws Exception {
                Thread.sleep(700); // breve espera para simular pensamiento
                Random random = new Random();
                int index = random.nextInt(cartasMaquina.size());
                Card cartaElegida = cartasMaquina.get(index);
                String posicion = random.nextBoolean() ? "Ataque" : "Defensa";
                cartaSeleccionadaMaquina = new CartaJugada(cartaElegida, posicion);

                // Actualizar icono en EDT en done()
                return cartaSeleccionadaMaquina;
            }

            @Override
            protected void done() {
                try {
                    CartaJugada maquinaJugada = get();
                    // Encuentra índice para actualizar la etiqueta correspondiente (busca por referencia)
                    int indice = cartasMaquina.indexOf(maquinaJugada.carta);
                    if (indice < 0) indice = 0;
                    JLabel[] labelsMaquina = {labelMaquina1, labelMaquina2, labelMaquina3};

                    try {
                        ImageIcon icon = new ImageIcon(new java.net.URL(maquinaJugada.carta.getImageUrl()));
                        ImageIcon scaled = new ImageIcon(icon.getImage().getScaledInstance(tamCarta.width - 10, tamCarta.height - 40, Image.SCALE_SMOOTH));
                        labelsMaquina[indice].setIcon(scaled);
                        labelsMaquina[indice].setText("<html><center>" + maquinaJugada.carta.getName() + "<br>ATK: " + maquinaJugada.carta.getAtk() + "<br>DEF: " + maquinaJugada.carta.getDef() + "<br>(" + maquinaJugada.posicion + ")</center></html>");
                        labelsMaquina[indice].setHorizontalTextPosition(SwingConstants.CENTER);
                        labelsMaquina[indice].setVerticalTextPosition(SwingConstants.BOTTOM);
                    } catch (Exception e) {
                        // Si falla al cargar la imagen, sólo muestra texto
                        labelsMaquina[indice].setIcon(null);
                        labelsMaquina[indice].setText("<html><center>" + maquinaJugada.carta.getName() + "<br>(" + maquinaJugada.posicion + ")</center></html>");
                    }

                    JOptionPane.showMessageDialog(mainPanel,
                            "La máquina juega: " + maquinaJugada.carta.getName() + "\nPosición: " + maquinaJugada.posicion);
                    log("Máquina juega: " + maquinaJugada.carta.getName() + " (" + maquinaJugada.posicion + ") ATK:" + maquinaJugada.carta.getAtk() + " DEF:" + maquinaJugada.carta.getDef());

                    if (jugadaJugador != null) {
                        compararCartas(jugadaJugador, maquinaJugada);
                    }

                    // Cambiar el turno sólo si el duelo no terminó
                    if (puntosJugador < 2 && puntosMaquina < 2) {
                        turnoActual = "Jugador";
                        Label_Turno.setText("Turno: Jugador");
                        log("Turno: Jugador");
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        worker.execute();
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

        // Cuando alguien llega a 2 puntos -> final
        if (puntosJugador == 2 || puntosMaquina == 2) {
            String ganador = (puntosJugador == 2) ? "Jugador" : "Máquina";
            labelGanador.setText("GANADOR: " + ganador);
            JOptionPane.showMessageDialog(mainPanel, "🎊 ¡" + ganador + " gana el duelo!");
            log("🎊 ¡" + ganador + " gana el duelo!");

            // Reiniciar estado del juego (sin cerrar la ventana)
            resetGameState();
        }
    }

    // Reinicia estado del juego sin destruir la ventana (evita bucle de abrir/cerrar)
    private void reiniciarDuelo() {
        int opcion = JOptionPane.showConfirmDialog(mainPanel, "¿Deseas reiniciar el duelo ahora?", "Confirmar reinicio", JOptionPane.YES_NO_OPTION);
        if (opcion != JOptionPane.YES_OPTION) return;
        resetGameState();
        log("Juego reiniciado por el usuario.");
    }

    // Resetea las variables y la UI para poder jugar otra vez
    private void resetGameState() {
        // limpiar variables de control
        puntosJugador = 0;
        puntosMaquina = 0;
        rondasJugadas = 0;
        cartasRepartidas = false;
        cartasJugador = null;
        cartasMaquina = null;
        cartaSeleccionadaJugador = null;
        cartaSeleccionadaMaquina = null;
        turnoActual = null;

        // limpiar UI (etiquetas, iconos, textos)
        JLabel[] labelsJugador = {labelJugador1, labelJugador2, labelJugador3};
        JLabel[] labelsMaquina = {labelMaquina1, labelMaquina2, labelMaquina3};

        for (JLabel l : labelsJugador) {
            l.setIcon(null);
            l.setText("");
        }
        for (JLabel l : labelsMaquina) {
            l.setIcon(null);
            l.setText("");
        }

        iniciarBatallaButton.setEnabled(false);
        REPARTIRCARTASButton.setEnabled(true);
    }

    // Muestra las imágenes e info de las cartas del jugador y oculta las de la máquina
    private void mostrarCartas() {
        try {
            JLabel[] labelsJugador = {labelJugador1, labelJugador2, labelJugador3};
            JLabel[] labelsMaquina = {labelMaquina1, labelMaquina2, labelMaquina3};

            for (int i = 0; i < cartasJugador.size() && i < labelsJugador.length; i++) {
                Card c = cartasJugador.get(i);
                try {
                    ImageIcon icon = new ImageIcon(new java.net.URL(c.getImageUrl()));
                    ImageIcon scaled = new ImageIcon(icon.getImage().getScaledInstance(tamCarta.width - 10, tamCarta.height - 40, java.awt.Image.SCALE_SMOOTH));
                    labelsJugador[i].setIcon(scaled);
                } catch (Exception e) {
                    labelsJugador[i].setIcon(null);
                }
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

        mainPanel.setBackground(fondoPrincipal);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        mainPanel.setLayout(new BorderLayout(15, 15));

        // Campo de batalla dividido: máquina arriba, jugador abajo
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

        // Ajuste de tamaño de las cartas (ya definido en tamCarta)
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

        // Panel lateral derecho (log + info)
        // Panel lateral derecho solo con el log de batalla
        JPanel panelLog = new JPanel(new BorderLayout());
        panelLog.setPreferredSize(new Dimension(300, 0));
        panelLog.setBackground(new Color(0, 0, 0, 30));

        JT.setEditable(false);
        JT.setFont(new Font("Monospaced", Font.PLAIN, 13));
        JT.setBackground(new Color(255, 255, 255, 220));
        JT.setForeground(Color.BLACK);

        JScrollPane scrollLog = new JScrollPane(JT);
        scrollLog.setBorder(BorderFactory.createLineBorder(bordeCampo, 2));

// ❌ Eliminamos el infoPanel (etiquetas arriba del JTextArea)
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
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("YGOBattle");
            frame.setContentPane(new YGOBattle().mainPanel);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }
}

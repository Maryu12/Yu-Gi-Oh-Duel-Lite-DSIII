package YGOBattle;

import javax.swing.*;

public class YGOBattle {
    private JPanel mainPanel;
    private JLabel BATLE;
    private JButton iniciarBatallaButton;
    private JButton CARTASJUGADORButton;
    private JButton CARTASMAQUINAButton;


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

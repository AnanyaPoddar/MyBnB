package mybnb;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

public class GUI {
    public GUI(){
        JFrame frame = new JFrame();
        frame.setSize(1000, 1000);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        JPanel panel = new JPanel();
        frame.add(panel);
        

        JLabel label = new JLabel("SIN");
        panel.add(label);

        JTextField sin = new JTextField(20);
        panel.add(sin);

        JLabel label2 = new JLabel("Password");
        panel.add(label2);

        JPasswordField password = new JPasswordField(20);

        panel.add(password);

        frame.setVisible(true);

        JButton login = new JButton("Login");
        panel.add(login);

    }
}

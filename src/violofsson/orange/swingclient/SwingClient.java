package violofsson.orange.swingclient;

import violofsson.orange.protocol.*;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class SwingClient extends JFrame implements GenericClientController {
    private ClientConnection connection;
    private JComboBox<String> categoryChooser;
    private JPanel categoryPanel = new JPanel();
    private JTextArea messageArea = new JTextArea();

    private JButton categoryButton = new JButton("Start Game");
    private JButton continueButton = new JButton("Continue");
    private JButton[] buttons = new JButton[4];
    private JLabel playerOne = new JLabel("s1");
    private JLabel playerTwo = new JLabel("s2");
    private JPanel buttonPanel = new JPanel(new GridLayout(2, 2));

    private Function<String, Boolean> checkAnswer;

    public SwingClient() throws IOException {
        connection = new ClientConnection();

        // TODO Rensa upp... allt
        String[] colors = {"Candy", "Egg", "Famous", "Random"};
        categoryChooser = new JComboBox<>(colors);
        categoryChooser.setSelectedIndex(0);

        categoryButton.addActionListener(e -> {
            getConnection().send(categoryChooser.getSelectedItem());
            categoryChooser.setEnabled(false);
            categoryButton.setEnabled(false);
        });

        categoryPanel.add(categoryChooser);
        categoryPanel.add(categoryButton);

        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.add(messageArea, BorderLayout.CENTER);

        for (int i = 0; i < buttons.length; i++) {
            String[] strings = {"Allan", "Fazli Zekiqi", "Victor J", "Victor O"};
            buttons[i] = new JButton(strings[i]);
            ActionListener alternativesListener = e -> {
                JButton temp = (JButton) e.getSource();
                userAnswer = temp.getText();
                disableCategories(true);
                disableAnswers(true);
                //changeButtonColors(userAnswer);
                disableContinue(false);
            };
            buttons[i].addActionListener(alternativesListener);
            buttons[i].setEnabled(false);
            buttons[i].setBackground(Color.BLACK);
            buttons[i].setForeground(Color.WHITE);
            buttonPanel.add(buttons[i]);
        }

        buttonPanel.setPreferredSize(new Dimension(500, 200));
        buttonPanel.setBorder(new EmptyBorder(0, 30, 0, 0));

        disableContinue(true);
        JPanel leftPanel = new JPanel();
        leftPanel.add(playerOne);
        JPanel rightPanel = new JPanel();
        rightPanel.add(playerTwo);
        leftPanel.setBackground(Color.ORANGE);
        rightPanel.setBackground(Color.ORANGE);
        add(leftPanel, BorderLayout.WEST);
        add(rightPanel, BorderLayout.EAST);

        playerTwo.setBackground(Color.ORANGE);
        centerPanel.add(buttonPanel, BorderLayout.SOUTH);

        messageArea.setEditable(false);
        messageArea.setLineWrap(true);
        messageArea.setDisabledTextColor(Color.BLACK);
        messageArea.setBackground(Color.ORANGE);
        messageArea.setForeground(Color.BLACK);

        messageArea.setFont(messageArea.getFont().deriveFont(15.0f));

        categoryPanel.setBackground(Color.ORANGE);
        buttonPanel.setBackground(Color.ORANGE);
        centerPanel.setBackground(Color.ORANGE);

        setLayout(new BorderLayout());
        add(centerPanel, BorderLayout.CENTER);
        add(categoryPanel, BorderLayout.NORTH);
        continueButton.addActionListener(continueButtonListener);
        add(continueButton, BorderLayout.SOUTH);

        setBackground(Color.ORANGE);
        setSize(700, 600);
        setVisible(true);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        new Thread(this).start();
    }

    @Override
    public void displayMessage(String s) {
        messageArea.setText("\n\n\n\n\n\n             " + s);
    }

    @Override
    public void displayMessage(ServerMessage message) {
        if (message.HEADER == ServerMessage.Headers.WELCOME) {
            playerOne.setText("Player 1");
            playerTwo.setText("Player 2");
            displayMessage(message.BODY);
        } else if (message.HEADER == ServerMessage.Headers.WAIT) {
            disableAnswers(true);
            disableCategories(true);
            displayMessage(message.BODY);
        } else if (message.HEADER == ServerMessage.Headers.CHOOSE_CATEGORY) {
            String[] categories = message.BODY.split(";");
            for (int i = 0; i < categories.length && i < buttonPanel.getComponents().length; i++) {
                JButton btn = (JButton) buttonPanel.getComponents()[i];
                btn.setText(categories[i]);
            }
            displayMessage("Choose category");
            disableAnswers(false);
        } else if (message.HEADER == ServerMessage.Headers.YOU_WIN) {
            showMessageDialog("You win!", "Congratulations!");
        } else if (message.HEADER == ServerMessage.Headers.YOU_LOSE) {
            showMessageDialog("You lose!", "Too bad!");
        } else if (message.HEADER == ServerMessage.Headers.YOU_TIED) {
            showMessageDialog("You tied!", "How unexpected!");
        } else {
            displayMessage(message.BODY);
        }
    }

    @Override
    public void displayQuestion(Question q) {
        displayMessage(q.getQuestion());
        java.util.List<String> alt = q.getAlternatives();
        checkAnswer = (q::isRightAnswer);
        for (Component c : buttonPanel.getComponents()) {
            c.setEnabled(true);
        }
        for (int i = 0; i < alt.size(); i++) {
            buttons[i].setText(alt.get(i));
        }
    }

    @Override
    public void displayScores(Integer[] scores) {
        playerOne.setText("P1 : " + scores[0]);
        playerTwo.setText("P2 : " + scores[1]);
    }

    @Override
    public void displayScores(ArrayList<List<Integer>> scores) {
        System.out.println(getScoreSummary("Player 1", scores.get(0)));
        System.out.println();
        System.out.println(getScoreSummary("Player 2", scores.get(1)));
    }

    @Override
    public ClientConnection getConnection() {
        return connection;
    }

    private void changeButtonColors(String answer) {
        for (JButton b : buttons) {
            if (b.getText().equals(answer)
                    && !checkAnswer.apply(b.getText())) {
                b.setBackground(Color.RED);
                b.setOpaque(true);
            } else if (checkAnswer.apply(b.getText())) {
                b.setBackground(Color.GREEN);
                b.setOpaque(true);
            }
        }
    }

    void disableAnswers(boolean b) {
        for (Component c : buttonPanel.getComponents()) {
            c.setEnabled(!b);
        }
    }

    void disableCategories(boolean b) {
        for (Component c : categoryPanel.getComponents()) {
            c.setEnabled(!b);
        }
    }

    void disableContinue(boolean b) {
        continueButton.setEnabled(!b);
        continueButton.setVisible(!b);
    }

    private String getScoreSummary(String playerName, java.util.List<Integer> scores) {
        StringBuilder s = new StringBuilder(playerName + ":");
        int sum = 0;
        for (int i = 0; i < scores.size(); i++) {
            sum += scores.get(i);
            s.append(String.format("\nRond %d: %d", i + 1, scores.get(i)));
        }
        s.append("\nSumma: ").append(sum);
        return s.toString();
    }

    private void showMessageDialog(String title, String content) {
        JOptionPane.showMessageDialog(this, content, title,
                JOptionPane.INFORMATION_MESSAGE);
    }

    private String userAnswer;
    private ActionListener continueButtonListener = e -> {
        disableContinue(true);
        for (JButton button : buttons) {
            button.setBackground(Color.BLACK);
        }
        getConnection().send(userAnswer);
    };

    public static void main(String[] args) {
        try {
            new SwingClient();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

package violofsson.orange.swingclient;

import violofsson.orange.protocol.Question;
import violofsson.orange.protocol.ServerMessage;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.function.Function;

public class SwingClient extends JFrame implements Runnable {
    private SwingClientSession session;
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
        session = new SwingClientSession();

        // TODO Rensa upp... allt
        String[] colors = {"Candy", "Egg", "Famous", "Random"};
        categoryChooser = new JComboBox<>(colors);
        categoryChooser.setSelectedIndex(0);

        categoryButton.addActionListener(e -> {
            session.send(categoryChooser.getSelectedItem());
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
                setCategoryDisable(true);
                setAnswersDisable(true);
                //changeButtonColors(userAnswer);
                setContinueDisable(false);
            };
            buttons[i].addActionListener(alternativesListener);
            buttons[i].setEnabled(false);
            buttons[i].setBackground(Color.BLACK);
            buttons[i].setForeground(Color.WHITE);
            buttonPanel.add(buttons[i]);
        }

        buttonPanel.setPreferredSize(new Dimension(500, 200));
        buttonPanel.setBorder(new EmptyBorder(0, 30, 0, 0));

        setContinueDisable(true);
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
    public void run() {
        Object obj;
        try {
            while ((obj = session.receive()) != null) {
                if (obj instanceof Question) {
                    Question question = (Question) obj;
                    displayQuestion(question);
                } else if (obj instanceof ServerMessage) {
                    ServerMessage fromServer = (ServerMessage) obj;
                    processServerMessage(fromServer);
                } else if (obj instanceof String) {
                    String message = (String) obj;
                    displayMessage(message);
                } else if (obj instanceof Integer[]) {
                    Integer[] points = (Integer[]) obj;
                    showPoints(points);
                } else if (obj instanceof ArrayList) {
                    // Kontrollera typer!
                    ArrayList<java.util.List> lista = (ArrayList) obj;
                    java.util.List<Integer> playerOneHistory = lista.get(0);
                    java.util.List<Integer> playerTwoHistory = lista.get(1);
                    String playerOneText = getScoreSummary("Spelare 1",
                            playerOneHistory);
                    String playerTwoText = getScoreSummary("Spelare 2",
                            playerTwoHistory);
                    JOptionPane.showMessageDialog(this, playerOneText + "\n\n" + playerTwoText);
                }
            }
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
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

    private void displayMessage(String msg) {
        messageArea.setText("\n\n\n\n\n\n             " + msg);
    }

    private void displayQuestion(Question q) {
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

    private String getScoreSummary(String playerName, java.util.List<Integer> scores) {
        StringBuilder s = new StringBuilder(playerName + ":");
        int sum = 0;
        for (int i = 0; i < scores.size(); i++) {
            sum += scores.get(i);
            s.append(String.format("\nRond %d: %d", i + 1, scores.get(i)));
        }
        s.append("\n\nSumma: ").append(sum);
        return s.toString();
    }

    void processServerMessage(ServerMessage fromServer) {
        if (fromServer.HEADER == ServerMessage.Headers.WELCOME) {
            playerOne.setText("Player 1");
            playerTwo.setText("Player 2");
            displayMessage(fromServer.MESSAGE);
        } else if (fromServer.HEADER == ServerMessage.Headers.WAIT) {
            setAnswersDisable(true);
            setCategoryDisable(true);
            displayMessage(fromServer.MESSAGE);
        } else if (fromServer.HEADER == ServerMessage.Headers.CHOOSE_CATEGORY) {
            String[] categories = fromServer.MESSAGE.split(";");
            for (int i = 0; i < categories.length && i < buttonPanel.getComponents().length; i++) {
                JButton btn = (JButton) buttonPanel.getComponents()[i];
                btn.setText(categories[i]);
            }
            displayMessage("Choose category");
            setAnswersDisable(false);
        } else if (fromServer.HEADER == ServerMessage.Headers.YOU_WIN) {
            showMessageDialog("You win!", "Congratulations!");
        } else if (fromServer.HEADER == ServerMessage.Headers.YOU_LOSE) {
            showMessageDialog("You lose!", "Too bad!");
        } else if (fromServer.HEADER == ServerMessage.Headers.YOU_TIED) {
            showMessageDialog("You tied!", "How unexpected!");
        } else {
            displayMessage(fromServer.MESSAGE);
        }
    }

    void setAnswersDisable(boolean b) {
        for (Component c : buttonPanel.getComponents()) {
            c.setEnabled(!b);
        }
    }

    void setCategoryDisable(boolean b) {
        for (Component c : categoryPanel.getComponents()) {
            c.setEnabled(!b);
        }
    }

    void setContinueDisable(boolean b) {
        continueButton.setEnabled(!b);
        continueButton.setVisible(!b);
    }

    private void showMessageDialog(String title, String content) {
        JOptionPane.showMessageDialog(this, content, title,
                JOptionPane.INFORMATION_MESSAGE);
    }

    private void showPoints(Integer[] points) {
        playerOne.setText("P1 : " + points[0]);
        playerTwo.setText("P2 : " + points[1]);
    }

    private String userAnswer;
    private ActionListener continueButtonListener = e -> {
        setContinueDisable(true);
        for (JButton button : buttons) {
            button.setBackground(Color.BLACK);
        }
        session.send(userAnswer);
    };

    public static void main(String[] args) {
        try {
            new SwingClient();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}


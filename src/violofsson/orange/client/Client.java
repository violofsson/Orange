/*package violofsson.orange.client;

import violofsson.orange.protocol.Question;
import violofsson.orange.protocol.ServerMessage;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class Client extends JFrame implements Runnable {
    private ClientSession session;
    private JComboBox<String> categoryChooser;
    private JPanel categoryPanel = new JPanel();
    private JTextArea label = new JTextArea();

    private JButton categoryButton = new JButton("Start Game");
    private JButton continueButton = new JButton("Continue");
    private JButton[] buttons = new JButton[4];
    private JLabel playerOne = new JLabel("s1");
    private JLabel playerTwo = new JLabel("s2");
    private JPanel buttonPanel = new JPanel(new GridLayout(2, 2));
    private JPanel leftPanel = new JPanel();
    private JPanel rightPanel = new JPanel();
    private JPanel centerPanel = new JPanel(new BorderLayout());

    private Function<String, Boolean> checkAnswer;

    public Client() throws IOException {
        session = new ClientSession();

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

        centerPanel.add(label, BorderLayout.CENTER);

        for (int i = 0; i < buttons.length; i++) {
            String[] strings = {"Allan", "Fazli Zekiqi", "Victor J", "Victor O"};
            buttons[i] = new JButton(strings[i]);
            buttons[i].addActionListener(alternativesListener);
            buttons[i].setEnabled(false);
            buttons[i].setBackground(Color.BLACK);
            buttons[i].setForeground(Color.WHITE);
            buttonPanel.add(buttons[i]);
        }

        buttonPanel.setPreferredSize(new Dimension(500, 200));
        buttonPanel.setBorder(new EmptyBorder(0, 30, 0, 0));

        continueButton.setVisible(false);
        leftPanel.add(playerOne);
        rightPanel.add(playerTwo);
        leftPanel.setBackground(Color.ORANGE);
        rightPanel.setBackground(Color.ORANGE);
        add(leftPanel, BorderLayout.WEST);
        add(rightPanel, BorderLayout.EAST);

        playerTwo.setBackground(Color.ORANGE);
        centerPanel.add(buttonPanel, BorderLayout.SOUTH);

        label.setEnabled(false);
        label.setLineWrap(true);
        label.setDisabledTextColor(Color.BLACK);
        label.setBackground(Color.ORANGE);
        label.setForeground(Color.BLACK);

        label.setFont(label.getFont().deriveFont(15.0f));

        categoryPanel.setBackground(Color.ORANGE);
        buttonPanel.setBackground(Color.ORANGE);
        centerPanel.setBackground(Color.ORANGE);

        setLayout(new BorderLayout());
        add(centerPanel, BorderLayout.CENTER);
        add(categoryPanel, BorderLayout.NORTH);
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
        continueButton.addActionListener(continueButtonListener);

        try {
            while ((obj = session.receive()) != null) {
                if (obj instanceof Question) {
                    Question question = (Question) obj;
                    showQuestion(question);
                } else if (obj instanceof ServerMessage) {
                    ServerMessage fromServer = (ServerMessage) obj;
                    processServerMessage(fromServer);
                } else if (obj instanceof String) {
                    String message = (String) obj;
                    showTheMessageFromServer(message);
                } else if (obj instanceof Integer[]) {
                    Integer[] points = (Integer[]) obj;
                    showPoints(points);
                } else if (obj instanceof ArrayList) {
                    // Kontrollera typer!
                    ArrayList<java.util.List> lista = (ArrayList) obj;
                    List<Integer> playerOneHistory = lista.get(0);
                    List<Integer> playerTwoHistory = lista.get(1);
                    String playerOneText = getScoreSummary("Spelare 1",
                            playerOneHistory);
                    String playerTwoText = getScoreSummary("Spelare 2",
                            playerTwoHistory);
                    JOptionPane.showMessageDialog(this, playerOneText + "\n\n" + playerTwoText);
                }
            }//while
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }//run()

    private void showQuestion(Question question) {
        label.setText("\n\n\n\n\n\n             " + question.getQuestion());
        List<String> alt = question.getAlternatives();
        checkAnswer = (question::isRightAnswer);
        for (Component c : buttonPanel.getComponents()) {
            c.setEnabled(true);
        }
        for (int i = 0; i < alt.size(); i++) {
            buttons[i].setText(alt.get(i));
        }
    }

    private void showPoints(Integer[] points) {
        if (playerOne.getText().equals("Player 1")) {
            playerOne.setText("P1 : " + points[0]);
            playerTwo.setText("P2 : " + points[1]);
        } else {
            playerTwo.setText("P2 : " + points[1]);
            playerOne.setText("P1 : " + points[0]);
        }
    }

    private void processServerMessage(ServerMessage fromServer) {
        if (fromServer.HEADER == ServerMessage.Headers.WELCOME) {
            if (fromServer.MESSAGE.contains("1")) {
                playerOne.setText(fromServer.MESSAGE);
                setTitle(fromServer.MESSAGE);
                playerTwo.setText("Player 2");
            } else {
                playerTwo.setText(fromServer.MESSAGE);
                setTitle(fromServer.MESSAGE);
                playerOne.setText("Player 1");
            }
        } else if (fromServer.HEADER == ServerMessage.Headers.WAIT) {
            for (Component c : buttonPanel.getComponents()) {
                c.setEnabled(false);
            }
            categoryChooser.setEnabled(false);
            categoryButton.setEnabled(false);
            label.setText("\n\n\n\n\n\n\n                    " + fromServer.MESSAGE);
        } else if (fromServer.HEADER == ServerMessage.Headers.CHOOSE_CATEGORY) {
            String[] categories = fromServer.MESSAGE.split(";");
            categoryChooser.removeAllItems();
            for (String s : categories) {
                categoryChooser.addItem(s);
            }
            categoryChooser.setEnabled(true);
            categoryButton.setEnabled(true);
            label.setText("\n\n\n\n\n\n                     " + "Choose category");
        } else if (fromServer.HEADER == ServerMessage.Headers.YOU_WIN) {
            JOptionPane.showMessageDialog(this, "YOU WIN", "Congratulations",
                    JOptionPane.INFORMATION_MESSAGE);
        } else if (fromServer.HEADER == ServerMessage.Headers.YOU_LOSE) {
            JOptionPane.showMessageDialog(this, "YOU LOSE", "You're defeated", JOptionPane.ERROR_MESSAGE);
        } else if (fromServer.HEADER == ServerMessage.Headers.YOU_TIED) {
            JOptionPane.showMessageDialog(this, "YOU TIED", " ", JOptionPane.INFORMATION_MESSAGE);
        } else {
            label.setText("\n\n\n\n\n\n                     " + fromServer.MESSAGE);
        }
    }

    private void showTheMessageFromServer(String message) {
        if (message.startsWith("Welcome")) {
            message = message.substring(message.indexOf(' '));
            if (message.contains("1")) {
                playerOne.setText(message);
                setTitle(message);
                playerTwo.setText("Player 2");
            } else {
                playerTwo.setText(message);
                setTitle(message);
                playerOne.setText("Player 1");
            }
        } else if (message.startsWith("Wait")) {
            for (Component c : buttonPanel.getComponents()) {
                c.setEnabled(false);
            }
            categoryPanel.setEnabled(false);
            label.setText("\n\n\n\n\n\n\n                    " + message);
        } else if (message.startsWith("YOU WIN")) {
            JOptionPane.showMessageDialog(this, "YOU WIN", "Congratulations", JOptionPane.INFORMATION_MESSAGE);
        } else if (message.startsWith("YOU LOSE")) {
            JOptionPane.showMessageDialog(this, "YOU LOSE", "You're defeated", JOptionPane.ERROR_MESSAGE);
        } else if (message.startsWith("YOU TIED")) {
            JOptionPane.showMessageDialog(this, "YOU TIED", " ", JOptionPane.INFORMATION_MESSAGE);
        } else {
            categoryChooser.setEnabled(true);
            categoryButton.setEnabled(true);
            label.setText("\n\n\n\n\n\n                     " + message);
        }
    }

    private String theAnswerFromUser;
    private ActionListener continueButtonListener = e -> {
        continueButton.setVisible(false);
        for (JButton button : buttons) {
            button.setBackground(Color.BLACK);
        }
        session.send(theAnswerFromUser);
    };

    private ActionListener alternativesListener = e -> {
        JButton temp = (JButton) e.getSource();
        categoryChooser.setEnabled(false);
        categoryButton.setEnabled(false);
        for (Component c : buttonPanel.getComponents()) {
            c.setEnabled(false);
        }
        changeButtonsColor(temp);
        continueButton.setVisible(true);
        theAnswerFromUser = temp.getText();
    };

    private void changeButtonsColor(JButton temp) {
        if (checkAnswer.apply(temp.getText())) {
            temp.setBackground(Color.GREEN);
            temp.setOpaque(true);
        } else {
            temp.setBackground(Color.RED);
            temp.setOpaque(true);

            for (JButton button : buttons) {
                if (checkAnswer.apply(button.getText())) {
                    button.setBackground(Color.green);
                    button.setOpaque(true);
                }
            }
        }
    }

    private String getScoreSummary(String playerName, List<Integer> scores) {
        StringBuilder s = new StringBuilder(playerName + ":");
        int sum = 0;
        for (int i = 0; i < scores.size(); i++) {
            sum += scores.get(i);
            s.append(String.format("\nRond %d: %d", i + 1, scores.get(i)));
        }
        s.append("\n\nSumma: ").append(sum);
        return s.toString();
    }

    public static void main(String[] args) {
        try {
            new Client();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}*/

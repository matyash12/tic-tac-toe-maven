package com.example;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Set;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;

import org.json.*;

public final class App {
    private App() {
    }

    static String[] GetData(int gameid) {
        Unirest.setTimeouts(0, 0);
        try {
            HttpResponse<String> response = Unirest
                    .get("http://localhost:8080/tic-tac-toe-game/getdata.php?id=" + gameid)
                    .asString();

            JSONObject json = new JSONObject(response.getBody());
            int status = json.getInt("status");
            if (status == 400) {
                System.out.println(json.getString("description"));
                return null;
            }
            JSONObject data = json.getJSONObject("data");
            String[] board = new String[9];
            board[0] = data.getString("one");
            board[1] = data.getString("two");
            board[2] = data.getString("three");
            board[3] = data.getString("four");
            board[4] = data.getString("five");
            board[5] = data.getString("six");
            board[6] = data.getString("seven");
            board[7] = data.getString("eight");
            board[8] = data.getString("nine");

            return board;
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;
    }

    // 0 = noone win
    // 1 = i win
    // 2 = computer won
    private static int CheckForWin(int gameid) {
        Unirest.setTimeouts(0, 0);
        HttpResponse<String> response;

        try {
            response = Unirest
                    .get("http://localhost:8080/tic-tac-toe-game/checkforwin.php?id=" + gameid)
                    .asString();

            JSONObject json = new JSONObject(response.getBody());

            int status = json.getInt("status");

            if (status == 400) {
                System.out.println(json.getString("description"));
                return 0;
            }
            int win = json.getJSONObject("data").getInt("Win");
            if (win == 1) {
                JOptionPane.showMessageDialog(null, "You won!");
                return 1;
            } else if (win == 2) {
                JOptionPane.showMessageDialog(null, "You lost!");
                return 2;
            }
            return win;
        } catch (Exception e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
        return 0;
    }

    public static void Game(int gameid) {

        JFrame frame = new JFrame("Game id: " + gameid);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setSize(400, 300);

        // Create a JPanel with a GridLayout
        JPanel panel = new JPanel(new GridLayout(3, 3));

        // X = 1
        // O = 2
        String[] board;
        JButton[] buttons = new JButton[9];
        try {
            board = GetData(gameid);

        } catch (Exception _) {

            return;
        }
        if (board == null) {
            JOptionPane.showMessageDialog(null, "Game with id:" + gameid + " doesn't exist.");
            frame.dispose();
            return;
        }

        System.out.println(Arrays.toString(board));
        for (int i = 0; i < 9; i++) {
            String buttontext = "";
            if (board[i].equals("1")) {
                buttontext = "X";
            }
            if (board[i].equals("2")) {
                buttontext = "0";
            }
            final int index = i;
            JButton button = new JButton(buttontext);
            buttons[index] = button;
            button.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    if (!button.getText().equals("")) {
                        JOptionPane.showMessageDialog(null, "You can't play here...");
                        return;
                    }

                    // i = my id
                    Unirest.setTimeouts(0, 0);
                    HttpResponse<String> response;
                    String[] fieldnames = { "one", "two", "three", "four", "five", "six", "seven", "eight", "nine" };

                    String fieldname = fieldnames[index];
                    try {
                        response = Unirest
                                .get("http://localhost:8080/tic-tac-toe-game/makeamove.php?id=" + gameid + "&field="
                                        + fieldname)
                                .asString();

                        JSONObject json = new JSONObject(response.getBody());

                        int status = json.getInt("status");

                        if (status == 400) {
                            System.out.println(json.getString("description"));
                        }
                        UpdateBoard(buttons, gameid);

                    } catch (Exception e1) {
                        // TODO Auto-generated catch block
                        e1.printStackTrace();
                    }
                    if (CheckForWin(gameid) == 1) {
                        frame.dispose();
                        return;
                    }

                    try {
                        response = Unirest
                                .get("http://localhost:8080/tic-tac-toe-game/computermove.php?id=" + gameid)
                                .asString();

                        JSONObject json = new JSONObject(response.getBody());

                        int status = json.getInt("status");

                        if (status == 400) {
                            System.out.println(json.getString("description"));
                        }
                        UpdateBoard(buttons, gameid);

                    } catch (Exception e1) {
                        // TODO Auto-generated catch block
                        e1.printStackTrace();
                    }
                    if (CheckForWin(gameid) == 1) {
                        frame.dispose();
                        return;
                    }

                }
            });
            panel.add(button);
        }

        // Add the panel to the frame
        frame.add(panel);

        // Set the frame visible
        frame.setVisible(true);

        if (CheckForWin(gameid) == 1) {
            frame.dispose();
        }
    }

    public static boolean isNumeric(String strNum) {
        if (strNum == null) {
            return false;
        }
        try {
            int d = Integer.parseInt(strNum);
        } catch (NumberFormatException nfe) {
            return false;
        }
        return true;
    }

    /**
     * Says hello to the world.
     * 
     * @param args The arguments of the program.
     */
    public static void main(String[] args) {
        JFrame frame = new JFrame("Tic tac toe");
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setSize(400, 300);
        JPanel panel = new JPanel(new FlowLayout());
        JTextField inputfield = new JTextField(20);

        JButton loadButton = new JButton("Load Game");
        loadButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String input = inputfield.getText();
                if (isNumeric(input)){
                int gameid = Integer.parseInt(input);
                inputfield.setText("");
                Game(gameid);
                }else{
                    JOptionPane.showMessageDialog(null, "This is not a valid number");
                }

            }
        });
        panel.add(inputfield);
        panel.add(loadButton);

        JButton createButton = new JButton("New Game", null);
        createButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Create new game
                Unirest.setTimeouts(0, 0);
                HttpResponse<String> response;

                try {
                    response = Unirest
                            .get("http://localhost:8080/tic-tac-toe-game/createnewgame.php")
                            .asString();

                    JSONObject json = new JSONObject(response.getBody());

                    int status = json.getInt("status");

                    if (status == 400) {
                        System.out.println(json.getString("description"));
                    }
                    JSONObject data = json.getJSONObject("data");
                    int gameid = data.getInt("gameid");
                    Game(gameid);
                } catch (Exception ex) {
                    System.out.println("Try again later.");
                }
            }
        });
        panel.add(createButton);

        frame.add(panel);
        frame.setVisible(true);
    }

    static void UpdateBoard(JButton[] buttons, int gameid) {
        String[] board = GetData(gameid);
        System.out.println("Updating board!");
        System.out.println(Arrays.toString(board));
        for (int i = 0; i < 9; i++) {
            String buttontext = "";
            if (board[i].equals("1")) {
                buttontext = "X";
            }
            if (board[i].equals("2")) {
                buttontext = "0";
            }
            buttons[i].setText(buttontext);
        }
    }
}

package com.example.tictactoe;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.fxml.FXMLLoader;

import java.io.IOException;
import java.util.ArrayList;
import java.util.ResourceBundle;
import java.net.URL;
import java.util.Arrays;


public class pvpLogic implements Initializable {
    @FXML
    private Button button00;

    @FXML
    private Button button01;

    @FXML
    private Button button02;

    @FXML
    private Button button10;

    @FXML
    private Button button11;

    @FXML
    private Button button12;

    @FXML
    private Button button20;

    @FXML
    private Button button21;

    @FXML
    private Button button22;

    @FXML
    private Text winnerText;

    private int playerTurn = 0;
    ArrayList<Button> buttons;

    @Override
    public void initialize(URL url, ResourceBundle resource) {
        buttons = new ArrayList<>(Arrays.asList(button00,button01,button02,button10,button11,button12,button20,button21,button22));
        buttons.forEach(button -> {
            setupButton(button);
            button.setFocusTraversable(false);
        });
    }

    @FXML
    void restart(ActionEvent event) {
        buttons.forEach(this::resetButton);
        winnerText.setText("Kółko i krzyżyk");
        playerTurn = 0;
    }

    public void resetButton(Button button) {
        button.setDisable(false);
        button.setText("");
    }

    private void setupButton(Button button) {
        button.setOnMouseClicked(event -> {
            setPlayerSymbol(button);
            button.setDisable(true);
            checkWinCondition();
        });
    }

    public void setPlayerSymbol(Button button) {
        if (playerTurn % 2 == 0) {
            button.setText("X");
            button.setStyle("-fx-text-fill: blue; -fx-opacity: 1; -fx-background-color: #C3B091;");
            playerTurn++;
        } else {
            button.setText("O");
            button.setStyle("-fx-text-fill: red; -fx-opacity: 1; -fx-background-color: #C3B091;");
            playerTurn--;
        }
    }

    public void checkWinCondition() {
        for (int i = 0; i < 8; i++) {
            String check = switch (i) {
                case 0 -> button00.getText() + button01.getText() + button02.getText();
                case 1 -> button10.getText() + button11.getText() + button12.getText();
                case 2 -> button20.getText() + button21.getText() + button22.getText();
                case 3 -> button00.getText() + button10.getText() + button20.getText();
                case 4 -> button01.getText() + button11.getText() + button21.getText();
                case 5 -> button02.getText() + button12.getText() + button22.getText();
                case 6 -> button00.getText() + button11.getText() + button22.getText();
                case 7 -> button02.getText() + button11.getText() + button20.getText();
                default -> null;
            };

            if (check.equals("XXX")) {
                winnerText.setText("Wygrał krzyżyk");
                disableAllButtons();
                return;
            } else if (check.equals("OOO")) {
                winnerText.setText("Wygrało kółko");
                disableAllButtons();
                return;
            }

        }

        boolean isDraw = buttons.stream().allMatch(button -> !button.getText().isEmpty());
        if (isDraw) {
            winnerText.setText("Remis");
            disableAllButtons();
        }

    }

    private void disableAllButtons() {
        buttons.forEach(button -> button.setDisable(true));
        buttons.forEach(button -> button.setStyle("-fx-opacity: 1; -fx-background-color: #C3B091;"));
    }


    @FXML
    public void switchToMenu(ActionEvent event) throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("main-menu.fxml"));
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }
}
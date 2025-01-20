package com.example.tictactoe;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class Menu extends Application {
    private static final String SERVER_ADDRESS = "localhost";
    private static final int SERVER_PORT = 8080;

    private static Menu instance;  // Instancja klasy Menu
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private volatile boolean isConnected = false;

    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("main-menu.fxml"));
        Scene scene = new Scene(loader.load());

        stage.setTitle("Kółko i krzyżyk");
        stage.setScene(scene);

        stage.setOnCloseRequest(event -> {
            disconnect();
            System.exit(0);
        });

        stage.show();

        new Thread(() -> {
            try {
                connect();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }

    public void connect() throws IOException {
        new Thread(() -> {
            try {
                System.out.println("Próbuję połączyć z serwerem...");
                socket = new Socket(SERVER_ADDRESS, SERVER_PORT);
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                out = new PrintWriter(socket.getOutputStream(), true);

                setConnected(true);

                // Notify the GUI thread
                Platform.runLater(() -> {
                    System.out.println("Połączenie z serwerem nawiązane!");
                    System.out.println("Aktualny stan połączenia (w GUI): " + isConnected());
                });

                System.out.println(in.readLine());

            } catch (IOException e) {
                System.out.println("Błąd połączenia z serwerem: " + e.getMessage());
                setConnected(false);

                Platform.runLater(() -> {
                    System.out.println("Błąd połączenia z serwerem.");
                });
            }
        }).start();
    }


    public void disconnect() {
        try {
            if (out != null) out.close();
            if (in != null) in.close();
            if (socket != null) socket.close();
            System.out.println("Połączenie z serwerem zostało zamknięte.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static Menu getInstance() {
        if (instance == null) {
            instance = new Menu();
        }
        return instance;
    }

    public PrintWriter getOut() {
        return out;
    }

    public synchronized boolean isConnected() {
        return isConnected;
    }

    public synchronized void setConnected(boolean connected) {
        isConnected = connected;
    }

    public void sendDifficultyToServer(String difficulty) {
        if (isConnected()) {
            if (out != null) {
                out.println("SET_DIFFICULTY:" + difficulty);
                System.out.println("Wysłano poziom trudności: " + difficulty);
            } else {
                System.out.println("Strumień wyjściowy (out) nie został zainicjowany.");
            }
        } else {
            System.out.println("Połączenie z serwerem nie zostało jeszcze nawiązane.");
        }
    }

    public static void main(String[] args) {
        launch();
    }
}

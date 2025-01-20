package com.example.tictactoe;

import java.io.*;
import java.net.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class Server {
    private static final int PORT = 8080;
    private static final int MAX_CLIENTS = 2;

    private static final AtomicInteger clientCounter = new AtomicInteger(1);

    public static void main(String[] args) {
        ExecutorService threadPool = Executors.newFixedThreadPool(MAX_CLIENTS);

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Serwer nasłuchuje na porcie " + PORT);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Połączono z klientem: " + clientSocket.getInetAddress());

                int clientId = clientCounter.getAndIncrement();

                threadPool.submit(new ClientHandler(clientSocket, clientId));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static class ClientHandler implements Runnable {

        private Socket clientSocket;
        private int clientId;

        public ClientHandler(Socket clientSocket, int clientId) {
            this.clientSocket = clientSocket;
            this.clientId = clientId;
        }

        @Override
        public void run() {
            try (
                    BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                    PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true)
            ) {
                out.println("Witaj na serwerze, Twój ID to: " + clientId);

                String message;
                while ((message = in.readLine()) != null) {
                    System.out.println("Otrzymano od klienta " + clientId + ": " + message);

                    if (message.startsWith("SET_DIFFICULTY:")) {
                        String difficulty = message.substring("SET_DIFFICULTY:".length());
                        System.out.println("Ustawiony poziom trudności od klienta " + clientId + ": " + difficulty);
                    } else {
                        out.println("Potwierdzenie dla klienta " + clientId + ": " + message);
                    }
                }

                System.out.println("Połączenie z klientem " + clientId + " zostało zakończone.");
            } catch (IOException e) {
                System.out.println("Błąd połączenia z klientem " + clientId + ": " + e.getMessage());
            } finally {
                try {
                    clientSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}

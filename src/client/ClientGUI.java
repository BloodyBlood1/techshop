package client;

import java.io.*;
import java.net.Socket;
import java.util.Scanner;

public class Client {

    private static final String HOST = "localhost";
    private static final int    PORT = 8080;

    public static void main(String[] args) {
        System.out.println("Подключение к серверу " + HOST + ":" + PORT + "...");

        try (
                Socket     socket  = new Socket(HOST, PORT);
                BufferedReader in  = new BufferedReader(new InputStreamReader(socket.getInputStream(),  "UTF-8"));
                PrintWriter    out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF-8"), true);
                Scanner scanner    = new Scanner(System.in)
        ) {
            System.out.println("Успешно подключено!\n");

            // Отдельный поток читает ответы сервера и печатает их
            Thread readerThread = new Thread(() -> {
                try {
                    String line;
                    while ((line = in.readLine()) != null) {
                        if (line.equals("END")) {
                            // Конец ответа — печатаем приглашение ввода
                            System.out.print("\n> ");
                        } else {
                            System.out.println(line);
                        }
                    }
                } catch (IOException e) {
                    System.out.println("\nСоединение с сервером разорвано.");
                }
            });
            readerThread.setDaemon(true);
            readerThread.start();

            // Главный поток — вводим команды и отправляем на сервер
            while (true) {
                String command = scanner.nextLine().trim();
                if (command.isEmpty()) {
                    System.out.print("> ");
                    continue;
                }
                out.println(command); // отправляем команду

                if (command.equalsIgnoreCase("EXIT")) {
                    Thread.sleep(400); // ждём последний ответ сервера
                    break;
                }
            }

        } catch (IOException e) {
            System.out.println("Не удалось подключиться: " + e.getMessage());
            System.out.println("Убедитесь, что сервер запущен на порту " + PORT);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        System.out.println("Клиент завершил работу.");
    }
}
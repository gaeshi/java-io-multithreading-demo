package dev.gaeshi.demo.multithreading;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;

public class VerboseChatter {
    public static void main(String[] args) throws IOException {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 1000; i++) {
            sb.append("Hello Ist");
        }

        Socket socket = new Socket("localhost", 8080);
        PrintStream out = new PrintStream(socket.getOutputStream());
        BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        for (int i = 0; i < 10000; i++) {
            out.println(sb);
            in.readLine();
        }
    }
}

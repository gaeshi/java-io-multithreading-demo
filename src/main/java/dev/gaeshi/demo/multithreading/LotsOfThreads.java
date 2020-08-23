package dev.gaeshi.demo.multithreading;

import java.util.concurrent.atomic.AtomicInteger;

public class LotsOfThreads {
    public static void main(String[] args) {
        AtomicInteger i = new AtomicInteger();
        while (true) {
            new Thread(() -> {
                System.out.println(i.incrementAndGet());
                try {
                    Thread.sleep(100000000);
                } catch (InterruptedException ignored) {
                }
            }).start();
        }
    }
}

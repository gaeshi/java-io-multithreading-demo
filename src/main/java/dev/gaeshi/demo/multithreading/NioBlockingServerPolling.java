package dev.gaeshi.demo.multithreading;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

// BAD CLASS, DON'T USE IT
public class NioBlockingServerPolling {
    public static void main(String[] args) throws IOException {
        ServerSocketChannel ss = ServerSocketChannel.open();
        ss.bind(new InetSocketAddress(8080));
        ss.configureBlocking(false);
        Map<SocketChannel, ByteBuffer> sockets = new ConcurrentHashMap<>();

        while (true) {
            SocketChannel s = ss.accept();
            // Non-blocking, almost always null
            if (s != null) {
                System.out.println(s);
                s.configureBlocking(false);
                sockets.put(s, ByteBuffer.allocateDirect(80));
            }
            sockets.keySet().removeIf(socketChannel -> !socketChannel.isOpen());
            sockets.forEach(NioBlockingServerPolling::handle);
        }
    }

    private static void handle(SocketChannel socket, ByteBuffer buf) {
        try {
            int data = socket.read(buf);
            if (data == -1) {
                close(socket);
            } else if (data != 0) {
                buf.flip();
                transmogrify(buf);
                while (buf.hasRemaining()) {
                    socket.write(buf);
                }
                buf.compact();
            }
        } catch (IOException e) {
            close(socket);
            throw new UncheckedIOException(e);
        }
    }

    private static void close(SocketChannel socket) {
        try {
            socket.close();
        } catch (IOException ignored) {
        }
    }

    private static void transmogrify(ByteBuffer buf) {
        for (int i = 0; i < buf.limit(); i++) {
            buf.put(i, (byte) transmogrify(buf.get(i)));
        }
    }

    private static int transmogrify(int data) {
        return Character.isLetter(data) ? data ^ ' ' : data;
    }
}

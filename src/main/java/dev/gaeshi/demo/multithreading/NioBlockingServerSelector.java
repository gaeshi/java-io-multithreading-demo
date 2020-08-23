package dev.gaeshi.demo.multithreading;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class NioBlockingServerSelector {
    private final static Map<SocketChannel, ByteBuffer> sockets = new ConcurrentHashMap<>();

    public static void main(String[] args) throws IOException {
        ServerSocketChannel ss = ServerSocketChannel.open();
        ss.bind(new InetSocketAddress(8080));
        ss.configureBlocking(false);

        Selector selector = Selector.open();
        ss.register(selector, SelectionKey.OP_ACCEPT);

        while (true) {
            selector.select(); // blocking
            Set<SelectionKey> keys = selector.selectedKeys();
            for (Iterator<SelectionKey> it = keys.iterator(); it.hasNext(); ) {
                SelectionKey key = it.next();
                it.remove();
                try {
                    if (key.isValid()) {
                        if (key.isAcceptable()) {
                            accept(key);
                        } else if (key.isReadable()) {
                            read(key);
                        } else if (key.isWritable()) {
                            write(key);
                        }
                    }
                } catch (IOException e) {
                    System.err.println(e);
                }

                sockets.keySet().removeIf(socketChannel -> !socketChannel.isOpen());
            }
        }
    }

    private static void accept(SelectionKey key) throws IOException {
        ServerSocketChannel ssc = (ServerSocketChannel) key.channel();
        SocketChannel s = ssc.accept();
        // Non-blocking, but never null
        System.out.println(s);
        s.configureBlocking(false);
        s.register(key.selector(), SelectionKey.OP_READ);
        sockets.put(s, ByteBuffer.allocateDirect(80));
    }

    private static void read(SelectionKey key) throws IOException {
        SocketChannel s = (SocketChannel) key.channel();
        ByteBuffer buf = sockets.get(s);
        int data = s.read(buf);
        if (data == -1) {
            s.close();
            sockets.remove(s);
        }
        buf.flip();
        transmogrify(buf);
        key.interestOps(SelectionKey.OP_WRITE);
    }

    private static void write(SelectionKey key) throws IOException {
        SocketChannel s = (SocketChannel) key.channel();
        ByteBuffer buf = sockets.get(s);
        s.write(buf); // won't always write everything
        if (!buf.hasRemaining()) {
            buf.compact();
            key.interestOps(SelectionKey.OP_READ);
        } else {
            System.out.println("There was some left");
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

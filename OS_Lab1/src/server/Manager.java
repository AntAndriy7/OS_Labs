package server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.Scanner;

public class Manager {
    private final static String hostname = "localhost";
    private final static int port = 50055;
    private static int result = 0;
    private static int clientID = 0;

    public static void main(String[] args) throws IOException {
        startServer();
    }

    private static void startServer() throws IOException {
        final var server =
                AsynchronousServerSocketChannel.open().bind(new InetSocketAddress(hostname, port));

        System.out.println("Server is starting...");
        System.out.print("Please enter the value of x: ");
        Scanner scn = new Scanner(System.in);
        int startValue = scn.nextInt();

        server.accept(null, new CompletionHandler<AsynchronousSocketChannel, Void>() {
            @Override
            public void completed(AsynchronousSocketChannel socketChannel, Void attachment) {
                server.accept(null, this);
                handle(socketChannel, startValue);
            }

            @Override
            public void failed(Throwable e, Void attachment) {
                System.err.println("Accept error: " + e);
            }
        });

        try {
            Thread.currentThread().join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private static void handle(AsynchronousSocketChannel socketChannel, int startValue) {
        long startTime = System.currentTimeMillis();
        ByteBuffer sendBuffer = ByteBuffer.allocate(8);
        sendBuffer.putInt(startValue);
        sendBuffer.flip();

        socketChannel.write(sendBuffer, null, new CompletionHandler<Integer, Void>() {
            @Override
            public void completed(Integer bytesWritten, Void attachment) {
                ByteBuffer receiveBuffer = ByteBuffer.allocate(8);
                socketChannel.read(receiveBuffer, null, new CompletionHandler<Integer, Void>() {
                    @Override
                    public void completed(Integer bytesRead, Void attachment) {
                        receiveBuffer.flip();
                        int clientResult = receiveBuffer.getInt();
                        int clientCrits = receiveBuffer.getInt();

                        long endTime = System.currentTimeMillis();
                        long clientExecutionTime = endTime - startTime;

                        handleResult(clientResult, clientCrits, clientExecutionTime);
                        close(socketChannel);
                        try {
                            socketChannel.close();
                            if (clientID == 2) {
                                System.exit(0);
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void failed(Throwable e, Void attachment) {
                        System.err.println("Read error: " + e);
                    }
                });
            }

            @Override
            public void failed(Throwable e, Void attachment) {
                System.err.println("Write error: " + e);
            }
        });
    }

    private static void handleResult(int clientResult, int clientCrits, long clientExecutionTime) {
        System.out.println("Client " + (clientID + 1) + " result: " + clientResult);
        System.out.println("Client " + (clientID + 1) + " execution time: " + clientExecutionTime + " ms");

        if (clientResult != -1) {
            if (clientCrits == 0) {
                System.out.println("Execution status: best-case scenario");
            } else if (clientCrits > 0 && clientCrits <= 30) {
                System.out.println("Execution status: non-critical scenario");
                System.out.println("Number of critical errors: " + clientCrits);
            }
            switch (clientID) {
                case 0:
                    result = clientResult;
                    clientID++;
                    break;
                case 1:
                    System.out.print("XOR result: " + result + " ^ " + clientResult);
                    result ^= clientResult;
                    System.out.println(" = " + result);
                    clientID++;
                    break;
            }
        } else {
            System.out.println("Execution status: worst-case scenario");
            System.out.println("Number of critical errors: " + clientCrits);
            System.err.println("Відмова!");
            System.exit(0);
        }
    }

    private static void close(AsynchronousSocketChannel socketChannel) {
        try {
            socketChannel.close();
            if (clientID == 2) {
                System.exit(0);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
import java.util.Scanner;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.net.InetSocketAddress;

public class Server {
    private final static String hostname = "localhost";
    private final static int port = 50055;
    private static int result = 0;
    private static int clientID = 0;

    public static void main(String[] args) throws IOException {
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

                                System.out.println("Client " + (clientID + 1) + " result: " + clientResult);
                                System.out.println("Client " + (clientID + 1) + " execution time: " + clientExecutionTime + " ms");

                                if (clientCrits == 30) {
                                    System.out.println("Execution status: worst-case scenario");
                                    System.out.println("Number of critical errors: " + clientCrits);
                                } else if (clientCrits > 0) {
                                    System.out.println("Execution status: non-critical scenario");
                                    System.out.println("Number of critical errors: " + clientCrits);
                                } else {
                                    System.out.println("Execution status: best-case scenario");
                                }

                                if (clientResult != -1) {
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
                                    try {
                                        socketChannel.close();
                                        System.err.println("Відмова!");
                                        System.exit(0);
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                }

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
}
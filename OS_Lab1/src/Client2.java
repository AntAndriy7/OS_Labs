import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.net.InetSocketAddress;

public class Client2 {
    private final static String hostname = "localhost";
    private final static int port = 50055;

    public static void main(String[] args) throws IOException, InterruptedException {
        var client = AsynchronousSocketChannel.open();
        client.connect(new InetSocketAddress(hostname, port), null, new CompletionHandler<Void, Void>() {
            @Override
            public void completed(Void result, Void attachment) {
                if (client.isOpen()) {
                    System.out.println("Client connected to the server");
                    ByteBuffer receiveBuffer = ByteBuffer.allocate(4);

                    client.read(receiveBuffer, null, new CompletionHandler<Integer, Void>() {
                        @Override
                        public void completed(Integer bytesRead, Void attachment) {
                            receiveBuffer.flip();
                            int receivedNumber = receiveBuffer.getInt();
                            System.out.println("Сlient received the value: " + receivedNumber);
                            FunctionG function = new FunctionG(receivedNumber);

                            int[] result = function.g();
                            int clientResult = result[0];
                            int crit = result[1];

                            ByteBuffer sendBuffer = ByteBuffer.allocate(8);
                            sendBuffer.putInt(clientResult);
                            sendBuffer.putInt(crit);
                            sendBuffer.flip();

                            client.write(sendBuffer, null, new CompletionHandler<Integer, Void>() {
                                @Override
                                public void completed(Integer bytesWritten, Void attachment) {
                                    try {
                                        client.close();
                                        System.exit(0);
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                }

                                @Override
                                public void failed(Throwable e, Void attachment) {
                                    System.err.println("Write error: " + e);
                                    System.exit(0);
                                }
                            });
                        }

                        @Override
                        public void failed(Throwable e, Void attachment) {
                            System.err.println("Read error: " + e);
                            System.exit(0);
                        }
                    });
                }
            }

            @Override
            public void failed(Throwable e, Void attachment) {
                System.err.println("Connect error: " + e);
                System.exit(0);
            }
        });

        Thread.currentThread().join();
    }
}

class FunctionG {
    private int x;
    private boolean flag = false;
    private long timeLimit = 1500;

    public FunctionG(int x) {
        this.x = x;
    }

    public int[] g() {
        Thread timerThread = new Thread(() -> {
            try {
                Thread.sleep(timeLimit);
                System.err.println("Execution time exceeded the limit of " + timeLimit + " milliseconds.");
                flag = true;
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });

        int crit = 0;
        int count = 0;
        int[] result = new int[2];

        timerThread.start();

        while (x > 1 && !flag) {
            try {
                if (crit != 30) {
                    if (x > 1e6) {
                        crit++;
                    }
                } else {
                    result[0] = -1;
                    result[1] = crit;
                    System.err.println("The maximum number of critical errors has been reached.");
                    return result;
                }
                Thread.sleep(5);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if (flag) {
                break;
            }
            if (x % 2 == 1) {
                x = x * 3 + 1;
                System.out.println(x);
            } else {
                x = x / 2;
                System.out.println(x);
            }
            count++;
        }

        if (flag) {
            timerThread.interrupt();
        }

        result[0] = count % 2;
        result[1] = crit;
        return result;
    }
}
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;

public class BankServer {
    private final HashMap<Integer, Integer> accounts = new HashMap<>();
    private final int port;
    private int clientNum = 0;

    public BankServer(String bankName, int dnsPort) throws IOException {
        ServerSocket serverSocket = new ServerSocket(0);
        port = serverSocket.getLocalPort();
        sendBankInfoToDNS(bankName, dnsPort);
        new ServerImpl(serverSocket, this).start();
    }

    private void sendBankInfoToDNS(String bankName, int dnsPort) throws IOException {
        Socket dnsSocket = new Socket("localhost", dnsPort);
        System.out.println("Successfully connected to dns!");
        DataOutputStream dataOutputStream = new DataOutputStream(new BufferedOutputStream(dnsSocket.getOutputStream()));
        dataOutputStream.writeUTF("CreateBank_" + bankName + "_" + port);
        dataOutputStream.flush();
        dataOutputStream.close();
        dnsSocket.close();
    }

    private void handleTransaction(int userId, int amount) {
        if (!accounts.containsKey(userId)) {
            if (amount >= 0) {
                accounts.put(userId, amount);
            }
        } else {
            if (!(amount < 0 && accounts.get(userId) < -amount)) {
                accounts.put(userId, accounts.get(userId) + amount);
            }
        }
    }

    private class ClientHandler extends Thread {
        private final BankServer BANK_SERVER;
        private final Socket socket;
        private final DataOutputStream dataOutputStream;
        private final DataInputStream dataInputStream;

        public ClientHandler(Socket clientSocket, BankServer BANK_SERVER, DataInputStream dataInputStream, DataOutputStream dataOutputStream) {
            this.BANK_SERVER = BANK_SERVER;
            this.socket = clientSocket;
            this.dataInputStream = dataInputStream;
            this.dataOutputStream = dataOutputStream;
        }

        private void handleClient() {
            String input;
            while (true) {
                try {
                    input = dataInputStream.readUTF();
                    System.out.println("bank client sent :" + input);

                    if (input.startsWith("Transaction")) {
                        BANK_SERVER.handleTransaction(Integer.parseInt(input.split("_")[1]), Integer.parseInt(input.split("_")[2]));
                        dataOutputStream.writeUTF("successful");
                        dataOutputStream.flush();
                    } else if (input.startsWith("AddClient")) {
                        BANK_SERVER.clientNum++;
                        dataOutputStream.writeUTF("successful");
                        dataOutputStream.flush();
                    } else {
                        socket.close();
                        break;
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        @Override
        public void run() {
            handleClient();
        }
    }

    private class ServerImpl extends Thread {
        private final ServerSocket server;
        private final BankServer BANK_SERVER;

        public ServerImpl(ServerSocket server, BankServer BANK_SERVER) {
            this.server = server;
            this.BANK_SERVER = BANK_SERVER;
        }

        @Override
        public void run() {
            while (true) {
                try {
                    System.out.println("Bank is Waiting for Client...");
                    Socket socket = server.accept();
                    System.out.println("A client Connected to bank");
                    DataInputStream dataInputStream = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
                    DataOutputStream dataOutputStream = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));
                    new ClientHandler(socket, BANK_SERVER, dataInputStream, dataOutputStream).start();
                } catch (IOException e) {
                    e.printStackTrace();
                    break;
                }
            }
        }
    }

    public int getBalance(int userId) {
        return accounts.getOrDefault(userId, 0);
    }

    public int getNumberOfConnectedClients() {
        return clientNum;
    }
}

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;

public class DNS {
    private final HashMap<String, Integer> bankPorts = new HashMap<>();

    public DNS(int dnsPort) throws IOException {
        ServerSocket serverSocket = new ServerSocket(dnsPort);
        new ClientHandler(serverSocket, this).start();
    }

    private class ClientHandler extends Thread {
        private final ServerSocket server;
        private final DNS DNS_SERVER;

        public ClientHandler(ServerSocket server, DNS DNS_SERVER) {
            this.server = server;
            this.DNS_SERVER = DNS_SERVER;
        }

        @Override
        public void run() {
            while (true) {
                try {
                    System.out.println("Waiting for Client...");
                    Socket socket = server.accept();
                    System.out.println("A client Connected!");

                    DataInputStream dataInputStream = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
                    DataOutputStream dataOutputStream = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));
                    String input = dataInputStream.readUTF();
                    System.out.println("client sent :" + input);
                    if (input.startsWith("CreateBank")) {
                        DNS_SERVER.addBankPort(input.split("_")[1], Integer.parseInt(input.split("_")[2]));
                    } else if (input.startsWith("MyServerPort")) {
                        dataOutputStream.writeUTF("" + DNS_SERVER.bankPorts.get(input.split("_")[1]));
                        dataOutputStream.flush();
                    } else {
                        socket.close();
                        break;
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                    break;
                }
            }
        }
    }

    private void addBankPort(String bankName, int port) {
        bankPorts.put(bankName, port);
    }

    public int getBankServerPort(String bankName) {
        return bankPorts.getOrDefault(bankName, -1);
    }
}

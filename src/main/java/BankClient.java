import java.io.*;
import java.net.Socket;
import java.util.Scanner;

import static java.lang.Thread.sleep;

public class BankClient {

    private static final String PATH = "./src/main/java/";
    private final DataOutputStream dataOutputStream;
    private final DataInputStream dataInputStream;
    private int bankPort;

    public BankClient(String bankName, int dnsPort) throws IOException {
        getBankPortFromDNS(bankName, dnsPort);
        Socket socket = new Socket("localhost", bankPort);
        dataOutputStream = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));
        dataInputStream = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
        notifyBankServer();
    }

    private void getBankPortFromDNS(String bankName, int dnsPort) throws IOException {
        Socket dnsSocket = new Socket("localhost", dnsPort);
        System.out.println("client connected to dns");
        DataOutputStream dataOutputStream = new DataOutputStream(new BufferedOutputStream(dnsSocket.getOutputStream()));
        DataInputStream dataInputStream = new DataInputStream(new BufferedInputStream(dnsSocket.getInputStream()));
        dataOutputStream.writeUTF("MyServerPort_" + bankName);
        dataOutputStream.flush();
        bankPort = Integer.parseInt(dataInputStream.readUTF());
        dataInputStream.close();
        dataOutputStream.close();
        dnsSocket.close();
    }

    private void notifyBankServer() throws IOException {
        dataOutputStream.writeUTF("AddClient");
        dataOutputStream.flush();
        dataInputStream.readUTF();
    }

    //blocking
    public void sendTransaction(int userId, int amount) {
        try {
            dataOutputStream.writeUTF("Transaction_" + userId + "_" + amount);
            dataOutputStream.flush();
            dataInputStream.readUTF();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //non-blocking
    public void sendAllTransactions(String fileName, final int timeBetweenTransactions) {
        new Thread(() -> {
            final File file = new File(PATH + fileName);
            try {
                Scanner scanner = new Scanner(file);
                String[] userId = new String[1000];
                String[] amount = new String[1000];
                int i = 0;
                while (scanner.hasNextLine()) {
                    String accountLine = scanner.nextLine();
                    userId[i] = accountLine.split("\\s+")[0];
                    amount[i] = accountLine.split("\\s+")[1];
                    i++;
                }
                for (int j = 0; j < i; j++) {
                    dataOutputStream.writeUTF("Transaction_" + userId[j] + "_" + amount[j]);
                    dataOutputStream.flush();
                    if (timeBetweenTransactions > 0) {
                        sleep(timeBetweenTransactions);
                    }
                }
                scanner.close();
            } catch (InterruptedException | IOException e) {
                e.printStackTrace();
            }
        }).start();
    }
}

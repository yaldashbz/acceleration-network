import org.junit.BeforeClass;
import org.junit.Test;
import java.io.IOException;
import static org.junit.Assert.*;

public class BlockingRequestsTest {
    static DNS dnsServer;
    static BankServer server1;
    static BankServer server2;
    static final int DNS_PORT = 8080;


    @BeforeClass
    public static void createServers_B() throws IOException {
        dnsServer = new DNS(DNS_PORT);
        assertEquals(-1, dnsServer.getBankServerPort("mellat"));
        server1 = new BankServer("mellat", DNS_PORT);
        assertNotSame(-1, dnsServer.getBankServerPort("mellat"));
        server2 = new BankServer("melli", DNS_PORT);
    }

    @Test
    public void testClientCreation_B() throws IOException {
        int priorNumberOfClients = server2.getNumberOfConnectedClients();
        new BankClient("melli", DNS_PORT);
        assertEquals(priorNumberOfClients + 1, server2.getNumberOfConnectedClients());
        //...
    }

    @Test
    public void testSingleServerSingleClient_B() throws IOException {
        BankClient client1 = new BankClient("mellat", DNS_PORT);
        assertEquals(0, server1.getBalance(111));
        assertEquals(0, server1.getBalance(222));
        client1.sendTransaction(111, +7);
        client1.sendTransaction(222, +7);
        client1.sendTransaction(111, +5);
        assertEquals(7, server1.getBalance(222));
        assertEquals(12, server1.getBalance(111));
        //...
    }


    /**should be run alone
     * not with the class
     */
    @Test
    public void testSingleServerMultiClient_B() throws IOException {
        BankClient client1 = new BankClient("mellat", DNS_PORT);
        BankClient client2 = new BankClient("mellat", DNS_PORT);
        BankClient client3 = new BankClient("mellat", DNS_PORT);
        client1.sendTransaction(1, -10);
        client2.sendTransaction(1, 19);
        client2.sendTransaction(2, 10);
        client2.sendTransaction(2, -10);
        client2.sendTransaction(3, 100);
        client1.sendTransaction(1, 20);
        client1.sendTransaction(2, 20);
        client3.sendTransaction(1, 111);
        client3.sendTransaction(3, -110);
        assertEquals(100, server1.getBalance(3));
        assertEquals(20, server1.getBalance(2));
        assertEquals(150, server1.getBalance(1));
        assertEquals(3, server1.getNumberOfConnectedClients());
    }
}

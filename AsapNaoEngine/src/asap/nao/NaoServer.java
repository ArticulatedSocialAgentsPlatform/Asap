/*******************************************************************************
 *******************************************************************************/
package asap.nao;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

import com.google.common.base.Charsets;

public class NaoServer implements Runnable
{

    public static final int PORT = 8881;

    private Socket clientSocket;

    private DataOutputStream outToClient;
    private BufferedReader inFromClient;

    /**
     * Start the server in a new thread
     */

    public NaoServer()
    {
        (new Thread(this)).start();
    }

    /**
     * Runs the server so it accepts incoming clients and connects to the Nao
     */

    @Override
    public void run()
    {
        try
        {
            ServerSocket ssock = new ServerSocket(PORT);
            clientSocket = ssock.accept();
            while (true)
            {
                inFromClient = new BufferedReader(new InputStreamReader(clientSocket.getInputStream(), Charsets.UTF_8));
                outToClient = new DataOutputStream(clientSocket.getOutputStream());

                while (true)
                {
                    String clientSentence = inFromClient.readLine();

                    // berichten van de NAO
                    System.out.println(clientSentence);

                    if (clientSentence.equals("NAO_SAY_DONE"))
                    {
                        System.out.println("Activate voice recognizer");
                    }

                }
            }
        }
        catch (IOException e)
        {
        }
    }

    /**
     * Sends a message to the Nao
     * @param message the message to be send
     */

    public void sendMessage(String message)
    {

        try
        {
            if (outToClient != null)
            {
                outToClient.writeBytes(message);
                System.out.println("Server: " + message);
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

    }
}

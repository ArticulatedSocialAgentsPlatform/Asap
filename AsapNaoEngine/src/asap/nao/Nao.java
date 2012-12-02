package asap.nao;

public class Nao
{

    private String name;
    private NaoServer naoServer;

    /**
     * initializes the Nao by setting the name and initilizing a server
     */

    public Nao()
    {
        this.name = "Nao";
        this.naoServer = new NaoServer();
    }

    /**
     * Return the name of the Nao
     * @return the name of the Nao
     */

    public String getName()
    {
        return this.name;
    }

    /**
     * The proof of concept action. It should only print a line, so we know the system is working
     */

    public void DoeIets()
    {
        System.out.println("Het werkt!");
    }

    /**
     * The say action. It should tell the Nao to say the line given as argument
     * @param text the text that should be said
     */

    public void Say(String text)
    {
        naoServer.sendMessage(" DO_SAY:" + text + "\n");
    }

    /**
     * The PlayChoreprapheClip action. It should tell the Nao which clip it should execute by giving it's name as an argument
     * @param filename the name of the file that should be executed
     */

    public void playChoregrapheClip(String filename)
    {
        naoServer.sendMessage(" DO_MODULE:" + filename + "\n");
    }

}

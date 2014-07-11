package asap.nao;

import pk.aamir.stompj.Connection;
import pk.aamir.stompj.StompJException;

public class Nao
{

    private String name;
	private Connection connection;

	private static final String APOLLO_IP = "localhost";
	private static final int APOLLO_PORT = 61613;
	private static final String APOLLO_USERNAME = "admin";
	private static final String APOLLO_PASSWORD = "password";
	
    /**
     * Creates a connection to the Apollo server, which in turn functions as a bridge to ROS using STOMP messages
     */

    public Nao()
    {
        this.name = "Nao";
        try {
            this.connection = new Connection(APOLLO_IP, APOLLO_PORT, APOLLO_USERNAME, APOLLO_PASSWORD);
			this.connection.connect();
		} catch (StompJException e) {
			System.out.println("An error was thrown while connecting to the Apollo server: ");
			e.printStackTrace();
		}
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

    public void doeIets()
    {
        System.out.println("Het werkt!");
    }

    /**
     * The say action. It should tell the Nao to say the line given as argument
     * @param text the text that should be said
     */

    public void say(String text)
    {
        
    }

    /**
     * The PlayChoreprapheClip action. It should tell the Nao which clip it should execute by giving it's name as an argument
     * @param filename the name of the file that should be executed
     */

    public void playChoregrapheClip(String filename)
    {
    	String XMLPlayBehaviour = 	String.format("<data>"+
	    								"<header type=\"dict\">"+
	    									"<stamp type=\"time\">0 0</stamp>"+
	        								"<frame_id type=\"str\" />"+
	        								"<seq type=\"int\">0</seq>"+
	        							"</header>"+
	    								"<goal_id type=\"dict\">"+
		    								"<stamp type=\"time\">0 0</stamp>"+
		    								"<id type=\"str\" />"+
	    								"</goal_id>"+
	    								"<goal type=\"dict\">"+
	    									"<behavior type=\"str\">%s</behavior>"+
	    								"</goal>"+
	        						"</data>", filename);
    	
    }

}

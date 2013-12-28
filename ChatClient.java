
import java.io.*;
import java.net.*;
import java.util.ArrayList;


//import p2pchat.common.*;


/* ChatClient class provides the main component of the p2p chat client.
 * Launched from the command line, it connects to the registration server on
 * the specified IP address and port, receives a list of peers, and can then 
 * send and receive messages to and from the chat network
 */

public class ChatClient {
	//Class Variables
	private ServerSocket clistener = null;
	public static String regServerIp = null;				//The IP address of the registration server
	public static int regServerPort = 0;					//The TCP port the registration server is listening on
	public static String ourHandle = null;					//The handle/nickname by which this client will be known
	public int listenPort = 0;								//The TCP port our chat client listens for messages on
	public String listenIp = null;							//The IP address our chat client listens for messages on
	public static RegClient ourClient = null;				//Static variable to hold our own client info
	public static PeerList peerlist = null;					//Static variable to hold this clients peerlist.
	public static int maxPeers = 16;								//Maximum number of peers the client will hold info about
	public static ArrayList<String> seenMessages = null;	//Keep track of our seen messages
	public static int TTL = 64;								//Number of times an instance of a message can be forwarded ("Hops"). Prevents flooding.

	/* Constructor will initialise an empty peerlist to hold the peers this
	 * client is aware of, and the messages it has already seen.
	 */
	public ChatClient() {
		ChatClient.peerlist = new PeerList(new ArrayList<RegClient>()); 
		ChatClient.seenMessages = new ArrayList<String>();
	}
	
	/* Main method will just confirm usage parameters, assign some static variables
	 * and call the 'server' portion of the client which creates a listensocket
	 * and forks some actions
	 */
	public static void main(String[] args) throws IOException {
		/* First ensure we received the correct number of arguments
		 * Usage: ChatClient reg_server_ip reg_server_port handle
		 */
		if(args.length != 3){
			System.out.println("Usage: ChatClient <registration server IP> <registration server port> <handle>");
			System.exit(1);
		}
		/* All being well, read the command line params we were passed, and hope they're valid! */
		else {
			regServerIp = args[0];
			regServerPort = Integer.parseInt(args[1]);	//Command arguments passed as strings, need an int.
			ourHandle = args[2];		
		}
		ChatClient chatclient = new ChatClient();
		chatclient.listenSocket();
	}
	
	/* listenSocket function will create a socket to listen on */
	public void listenSocket(){
		  try{
			  clistener = new ServerSocket(this.listenPort);
			  System.out.println("ChatClient listening on interface " + clistener.getInetAddress().getHostAddress() + ":" + clistener.getLocalPort());
		  } catch (IOException e) {
			  	System.out.println("Unable to listen on port " + this.listenPort);
		    	e.printStackTrace();
			  	System.exit(-1);
		  }
		  
		  try {
			  /* We need to register our own client information with the main method as well
			  * as with the remote server (which will be a seperate thread).
			  */
			  ChatClient.ourClient = new RegClient(clistener.getInetAddress().toString().split("/")[1], clistener.getLocalPort(), ourHandle);
			  
			  //System.out.println("DEBUG: Our client IS " + ourClient.handle + "@" + ourClient.ip + ":" + ourClient.port);					

			  Thread regThread = new Thread(new Registration(ourClient,regServerIp, regServerPort));
			  regThread.start();
			  regThread.join(); //we want to register before doing anything else.
			  
			  /* Next, start a thread to accept local keyboard chat messages from the user */
			  Thread inputThread = new Thread(new ChatInputHandler());
			  inputThread.start();
			  
			  /* We also want to periodically poll our own peers, to make sure
			   * they are still alive and delete them if not;
			   */
			  Thread peerMon = new Thread(new PeerMonitor(peerlist));
			  peerMon.start();
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		}
		  
		  /* Now repeatedly await connections bringing chat messages from other clients. */
		  while(true){	    
		    try {	    					
		    	/* Wait for connections, and when a connection is received, fork a MessageHandler thread to handle it. */
		    	Thread mht = new Thread(new MessageHandler(clistener.accept()));
		    	mht.start();
		    	/* We want to increase the priority of the MessageHandler slightly,
		    	 * to improve the speed of chat propagation.
		    	 */
		    	mht.setPriority(Thread.NORM_PRIORITY + 1);
		    	
			} catch (Exception e) {
				e.printStackTrace();
			} 
		   }
	}
	
	
	

}

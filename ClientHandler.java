
import java.io.*;
import java.net.*;


/* ClientHandler class, a thread called by
 * RegServer to handle individual ChatClient registrations
 * with the chat server. 
 */
public class ClientHandler implements Runnable {

	
	public Socket client = null;	//Client connection
	public String clientIp = null;;	//IP address of client listener
	public String handle = null;	//Handle/Nickname of client chat user
	public int clientPort = 0;		//TCP port of the client listener
	public PeerList peers = null;	//Peerlist new client will be added to.
	protected int maxPeers = 4;		//Maximum number of peers a client will receive
	
	/*Constructor, obtain tcp connection info from client Socket.
	*/
	public ClientHandler (Socket client) {
		this.client = client;
	}
	
	//Thread start function, accepts registration data from a new client connection
	public void run() {	
	    try{
	    	//Fetch the RegClient instance we are being passed. Should contain
	    	//an ip, port, and handle
	    	ObjectInputStream ois = new ObjectInputStream(client.getInputStream());  
	    	RegClient rClient = (RegClient) ois.readObject();  
	    	if (rClient.ip != null) {	//quickly checks the validity of the received object
	    		//We should now have a RegClient object. Tell the server console of the incoming request.
	    		//System.out.println("Incoming JOIN request from " + rClient.handle + "@" + rClient.ip + ":" + rClient.port);    			    	
	    	  synchronized(this) {
				/* Whether or not the client already existed, send them a list of peers
		    	 * using the existing socket connection. This allows peers to periodically
	    		 * (or on demand) obtain new peers from the Registration Server by re-issuing
	    		 * a JOIN request. 
	    		 */
	    		 /* Re-use existing socket (instead of new thread) for efficiency */
		    	RegServer.pl.SendPeers(this.client, maxPeers);		
		    	/* Now add this peer to the global PeerList. Our AddPeer function
		    	* checks to see if it's already registered.
		    	*/
	    		RegServer.pl.AddPeer(rClient);
	    		/* Print the current peer list, for the sake of it */
	    		RegServer.pl.printPeers();
	    	  }
	    	}
	    	ois.close();
	    } catch (IOException | ClassNotFoundException e) {
	      System.out.println("Unexpected Data Received from " + this.client.getInetAddress().getHostAddress() 
	    		  	+ ":" + this.client.getPort() + " - is this really a registration request?");
	      //e.printStackTrace();
	      System.exit(-1);
	    }
	}
	
	protected void finalize(){
		//Objects created in run method are finalized when
		//program terminates and thread exits
		     try{
		        client.close();
		        //System.out.println("Connection Closed");
		    } catch (IOException e) {
		        System.out.println("Could not close socket");
		        System.exit(-1);
		    }
		  }
	

}

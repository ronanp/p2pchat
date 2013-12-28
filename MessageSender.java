
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;



/* The MessageSender class is a thread which transports a chat message
 *  to an individual client */

public class MessageSender implements Runnable {

	public RegClient sender = null;	//The RegClient object of the message originator
	public RegClient peer = null;	//RegClient object of our next recipient
	public ArrayList<RegClient> seenBy = null;	//The list of RegClients who we know have already seen it
	public String text = null;	//The text body of the message we are sending
	private Socket sendSocket;	//A client socket we create to send the message to the peer
	public Message msg = null;	//A holder for a Message object to stream across the socket.
	protected int TTL = 0;		//Number of hops the message can take
	private String msgUID = null; 	//Unique identifier for this message on our network.
	
	//Constructor just takes the message parameters from the call.
	public MessageSender(RegClient sender, RegClient peer, ArrayList<RegClient> seenBy, String text, String msgUID, int TTL) {
		this.sender = sender;
		this.peer = peer;
		this.seenBy = seenBy;
		this.text = text;	
		this.TTL = TTL;
		this.msgUID = msgUID;
	}
	
	//our thread...
	public void run() {
			try {
				//Build the message object
				Message msg = new Message(sender, text, msgUID, TTL, seenBy);
				//Create our client socket and stream the object across it
				sendSocket = new Socket(peer.ip, peer.port);
				OutputStream os = sendSocket.getOutputStream();  
				ObjectOutputStream oos = new ObjectOutputStream(os);   
				oos.writeObject(msg);  
				//Close our Output Stream
				oos.close();  
				os.close();
				sendSocket.close();
			} catch (UnknownHostException e) {
				e.printStackTrace();
			} catch (IOException e) {
				//e.printStackTrace();
				/* If a client disconnects before we send the message, but prior to our monitoring daemon noticing, the socket will fail
				 * and throw a ConnectException. The message will continue to propagate, so we don't need to "worry" the user by flooding
				 * the console with stack traces.
				 */
			} 
	}
	
	protected void finalize(){
		//Objects created in run method are finalized when
		//program terminates and thread exits
		    if (sendSocket.isConnected()) {
		     try{
		    
		    	 //Just in case we caught an exception above without closing our socket...
		        sendSocket.close();
		        //System.out.println("Connection Closed");
		     } catch (IOException e) {
		        System.out.println("Could not close socket");
		        System.exit(-1);
		     }
		    }
		  }
			

	
}

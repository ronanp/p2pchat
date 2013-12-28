
import java.io.*;
import java.net.*;
import java.util.ArrayList;


/* Registration class - a thread which is spawned by the ChatClient on startup, to
 * register with the Registration Server and/or obtain a fresh list of peers.
 */
public class Registration implements Runnable {
	
	public RegClient ourClient = null;					//The RegClient object that contains this clients details
	public String serverIp = null;						//IP address or hostname of the registration server
	public int serverPort = 0;							//TCP port to connect to on Registration Server
	public Socket s = null;								//Client socket to communicate with Registration Server
	private ArrayList<RegClient> tmpNewPeers = null; 	//Temporary ArrayList to store new peers .
	
	/* Constructor takes our client details and details about the registration server */
	public Registration (RegClient client, String serverIp, int serverPort) {
		this.ourClient = client;
		this.serverIp = serverIp;
		this.serverPort = serverPort;
		this.tmpNewPeers = new ArrayList<RegClient>();
	}
	
	public void run () {
		try {
			/* Create a socket to use for comms with the Registration Server */
			Socket s = new Socket(this.serverIp, this.serverPort);  
			
			/* First, start a thread to register the client with the server
			 * Extract the correct IP address from our socket (getHostAddress may return the
			 * wildcard (0.0.0.0) address).
			 */
			
			ourClient.ip = s.getInetAddress().toString().split("/")[1];
			
			/* Setup an output stream to send our RegClient object, which
			 * contains information about this host.
			 */
			OutputStream os = s.getOutputStream();  
			ObjectOutputStream oos = new ObjectOutputStream(os);   
			oos.writeObject(ourClient);  
			
			/* Now setup an input stream to receive data back from the server	*/		
			InputStream is = s.getInputStream();
			ObjectInputStream ois = new ObjectInputStream(is);
			PeerList pl = (PeerList)ois.readObject();  
			if (pl != null) {
				/* track our new clients */
				//int newPeers = 0;	//initialise counter (Only used in debug line below, uncomment to activate)
				
				/* We already have a global PeerList from the ChatClient class. We just add
				 * any new peers we've received into it. The PeerList class will handle
				 * and duplicates.
				 */				
				/* First make sure there were some peers in it to begin with */
				if (pl.clientlist.size() > 0) {
				  /* lock peerlist for modification */
				  synchronized(ChatClient.peerlist) {
					for (RegClient peer: pl.clientlist) {
					  /* make sure we are not one of the peers, twould be a waste of a slot! */
					  if (!(peer.equals(ourClient))) {	
						ChatClient.peerlist.AddPeer(peer);
						/* Temporary list taken now - we'll contact them after we've relinquished our hold on the global peerlist. */
						this.tmpNewPeers.add(peer);	
						//newPeers++; //(Only used in debug line below, uncomment to activate)
						
					  }
					}
				  } 
				}	
				
				//If we got new peers, shout about it
				//System.out.println("DEBUG: Received " + newPeers + " new peers from Reg Server. Current Peers:");
				ChatClient.peerlist.printPeers();
				
			}  
			//Close our Output Stream
			oos.close();  
			os.close(); 
			//And our Input Stream
			is.close(); 
			ois.close();
			s.close();    
			
			/* The final step is to say hi to our new peers, so they can add us to their peerlist.
			*  We say hi with an empty message. TTL 1 ensures this message doesn't propagate, so no
			*  need for any unique identifier on the message.
			*  If we wish, we can increase the TTL to propagate deeper into the network. The default
			*  TTL for the chatclient has been used here.
			*/
			if (ChatClient.peerlist.clientlist.size() > 0) {
				try {
					long timenow = System.currentTimeMillis();
					Message msg = new Message (ChatClient.ourClient, "", ChatInputHandler.md5(ChatClient.ourClient.handle+ChatClient.ourClient.ip+ChatClient.ourClient.port+"HELLO"+timenow), 
							ChatClient.TTL, new ArrayList<RegClient>());
					//System.out.println("Marking message as read");
					msg.markRead(ChatClient.ourClient);
					//msg.showSeenBy();
					msg.send();
					Thread.sleep(10000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}

		} catch (IOException | ClassNotFoundException e) {
			System.out.println("An error occured during the chatclient handshake with the Server");
			//e.printStackTrace();
		}  		
	}
	
	protected void finalize(){
		//Objects created in run method are finalized when
		//program terminates and thread exits
		     try{	 
		        s.close();	//quietly close our client socket.
		    } catch (IOException e) {
		        System.out.println("Could not close socket");
		        System.exit(-1);
		    }
		  }
}

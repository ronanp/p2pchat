
import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.Random;



/* PeerList class keeps a list of registered chat peers.
 * Each peer is stored in an object of type RegClass  
 * (String Ip; int Port; String Handle).
 * PeerList is used by both RegServer and ChatClient to
 * keep a list of know chat clients/peers.
 */
public class PeerList implements Serializable {

	//Class Variables
	private static final long serialVersionUID = 1L;
	public ArrayList<RegClient> clientlist = new ArrayList<RegClient>();	//An ArrayList will hold our list of chat client objects
	public static int numPeers = 0;		//Class variable keeps track of the number of peers on our chat network.
	
	/* Initialise our peerList by determining the number of peers
	 *it contains at creation and assigning them to an ArrayList.
	 */
	public PeerList(ArrayList<RegClient> cl) {	
		numPeers = cl.size();
		this.clientlist = cl;
	}
	
	public void AddPeer(RegClient peer) {
		/* Need to ensure peer doesn't already exist. */
		if (!(PeerExists(peer, clientlist)))  {
		  try {  
			/* prevent concurrent modification from occurring */
			  synchronized(this.clientlist) {
				this.clientlist.add(peer);
				//System.out.println("DEBUG: Added new peer " + peer.handle + "@" + peer.ip + ":" +peer.port);
				numPeers = this.clientlist.size();	//Recalculate the number of peers on the system based on the size of clientlist. 
			  }
			} catch (Exception e) {
				System.out.println("Failed to add new peer " + peer.handle + " from " + peer.ip + ":" +peer.port);
				e.printStackTrace();
			}
		}	//else do nothing!
	}
	
	public void DeletePeer(RegClient peer) {
	  /* prevent concurrent modification from occurring	*/  
	   if (PeerExists(peer, new ArrayList<RegClient>(clientlist))) {
		 try {
			synchronized(this.clientlist) {
			 this.clientlist.remove(peer);
				//System.out.println("DEBUG"+ peer.handle + " from " + peer.ip + ":" + peer.port + "has disconnected..");
				numPeers = this.clientlist.size();	//Decrement the number of peers on the system
			}
			 
		  } catch (Exception e) {
				e.printStackTrace();
		  }
		} //else do nothing again!
	  
	}
	
	/* Check if a peer exists already */
	public static boolean PeerExists(RegClient peer, ArrayList<RegClient> clist) {
		try {
		  /* Iteration is performed on a copy of the list, so no need to synchronise */
			for (RegClient existing: clist) {
				if (existing.equals(peer)) {
					/* Peer already exists, so exit and return true */
					return true;
					//System.out.println("DEBUG: Not adding client " + peer.ip + ":" + peer.port + ". Client already exits")
				} 
				//System.out.println("DEBUG:" + peer.ip + ":" + peer.port + " was not already present");
			}
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		/* Once the existing list is checked, and we haven't already returned a value, peer doesn't exist already. */
		return false;
	}
	
	/* Function to send a specified number (maxPeers, determined by ClientHandler class) of peers to a client over an existing socket connection.
	 *If the number is less than the total number of clients, we send a random selection
	 */
	public void SendPeers(Socket client, int maxPeers) {
		/* To create a mini list, we'll first create a new empty PeerList */
		ArrayList<RegClient> localpeerlist = new ArrayList<RegClient>();
		ArrayList<RegClient> sendlist = new ArrayList<RegClient>();
		
		/* provided we're not looking for the whole list, make a random sublist the size of maxPeers */
		if (maxPeers < numPeers) {
			while (localpeerlist.size() < maxPeers) {
				int indx = new Random().nextInt(numPeers); //Get random index value we can use to take random peer from clientlist
				RegClient localpeer = clientlist.get(indx); //take a local copy of this peer
				if (!(PeerExists(localpeer,localpeerlist))) {
					/* Peer does not exist, add it now */
					localpeerlist.add(localpeer);					
				}
			}
			sendlist = localpeerlist;
		}
		else {	//if maxPeers is smaller than the whole list of clients, we just send the whole list
			sendlist = clientlist;
		}
		/* Do the sending */
		try {
			/* First re-wrap the list in a PeerList object */
			PeerList peerListEnvelope = new PeerList(sendlist);
			/* Now send the object across the existing socket. */
			ObjectOutputStream objectOutput = new ObjectOutputStream(client.getOutputStream());
			objectOutput.writeObject(peerListEnvelope);
			System.out.println("Sent Client list to " + client.getInetAddress().getHostAddress() + ":" + client.getPort());
		} catch (IOException e) {
			e.printStackTrace();
		} 
	}
	
	public void printPeers() {;
	  /* Synchronise iteration over clientlist to prevent concurrent mod errors. */
	  synchronized(clientlist) {
		System.out.print(clientlist.size() + "peers: ");
		for (RegClient peer: clientlist) {
			System.out.print(peer.handle + "@" + peer.ip + ":" + peer.port + ",");
		}
		System.out.println("End of PeerList - " + clientlist.size() + " peers present");
	  }
	}
	
	
	
}

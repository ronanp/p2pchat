

import java.io.*;
import java.util.ArrayList;



/* "Envelope" for Messages passed around the chat system */

public class Message implements Serializable {

	
	private static final long serialVersionUID = 1L;
	public String msgUID = null;
	public String fromIP = null;	//Source IP address of the Message
	public int fromPort = 0;		//Source TCP Port number for the message
	public String fromHandle = null;//nickname of sender
	public RegClient sender = null;	//RegClient instance of message sender
	public String text = null;		//text body of the message
	public ArrayList<RegClient> seenBy = new ArrayList<RegClient>();	//array of Clients who have seen this message
	protected int TTL = 0;
	
	//Constructor, accept variables.
	public Message(RegClient msgSender, String msgText, String msgUID, int TTL, ArrayList<RegClient> seenBy) {
		this.sender = msgSender;
		this.text = msgText;
		this.seenBy = seenBy;
		this.msgUID = msgUID;
		this.TTL = TTL;
		//ArrayList<RegClient>
	}
	
	//Function allowing us to mark a message as having been read by a client peer
	public void markRead(RegClient peer) {
		try {
			if (!(isRead(peer))) {
				this.seenBy.add(peer);
				
			}
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	//Function allows us to see if a peer has already read a message
	public boolean isRead(RegClient peer) {
		for (RegClient client: this.seenBy) {
			if (client.equals(peer)) {
				return true;
			}
		}
		return false;
	}
	
	//Function to show the clients who've seen the message - for troubleshooting problems
	public void showSeenBy() {
		for (RegClient peer: this.seenBy) {
			System.out.println("Via: " + peer.handle + "@" + peer.ip + ":" + peer.port);
		}
	}
	
	public void send() {
		/* first make sure peer hasn't already seen the message
		 * need to lock the peerlist while we do this
		 * First obtain a copy of the peerlist (passing by reference
		 * can cause concurrency problems where the global PeerList is
		 * being modified by another thread)
		 */
		 ArrayList<RegClient> clCopy = new ArrayList<RegClient>();
		 //synchronise now and fork the send processes later, to release the clientlist to other threads
		 synchronized(ChatClient.peerlist.clientlist) {
			for (RegClient peer: ChatClient.peerlist.clientlist) {
			   //rebuild our list as a list of only peers who haven't already seen the message
				if (!(this.isRead(peer))) {
				  clCopy.add(peer);
				}
			}
		 } 

		/* Now the actual send process. We want to send the message to any user on
		 * our list who hasn't already seen it. When sending to each, we need to mark it as read for
		 * the other recipients we are sending to, to prevent duplicate messages being received
		 */
		if (this.TTL > 0) {		//We don't forward a message that has timed out
		  if (clCopy.size() > 0) {							//make sure we have a list of peers to copy to, and the message TTL isn't zero
			for (RegClient peer: clCopy) {					//Iterate through the peer list, and for each peer, we want to \
				ArrayList<RegClient> neighbourlist = new ArrayList<RegClient>();  //  also build a list of our peers that excludes this one
				//System.out.println("Planning to send to " + peer.port);
				for (RegClient neighbour: clCopy) {							
					if (!(peer.equals(neighbour))) {
						//System.out.println("Adding neighbour to neighbourlist" + neighbour.port);
						neighbourlist.add(neighbour);
					}

				}
				//System.out.println("DEBUG: List of neighbours is as follows:");
				//for (RegClient nbr: neighbourlist) {
				//	System.out.print("*"+nbr.port+"*\n");
				//}
				try {
					ArrayList<RegClient> newSeenBy = new ArrayList<RegClient>(seenBy);  	//Shallow copy of the class ArrayList (we don't want to change it)
					for(RegClient recip: neighbourlist) {
						if (!(isRead(recip))) {			//if the recipient isnt already on the seenby list, add him to the temporary version.
							newSeenBy.add(recip);
						}
					}
					//Make sure recipient hasn't already seen this
					if (!(newSeenBy.contains(peer))) {
						/* Now we should be ready to send our message to this peer along with our temporary seenby list */
						//System.out.println("Sending to" + peer.port + "with the following seenbys: ");
						//for (RegClient cclient: newSeenBy) {
						//	System.out.print(" " + cclient.port);
						//}
						Thread mst = new Thread(new MessageSender(sender, peer, seenBy, text, msgUID, TTL-1));	//Decrement the TTL with each message.
						mst.start();
					}
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		   } //end of "if clCopy.size > 0"
		
		   else {		/* We seem to be the dead end in a chain of peers. Better request more from the registration server! */
			//while (clCopy.isEmpty()) {	//if there's something in the list of peers who haven't seen it, we've done our bit.
			  try {
				Thread regThread = new Thread(new Registration(ChatClient.ourClient,ChatClient.regServerIp, ChatClient.regServerPort));
				  regThread.start();
				  regThread.join();  /* we need to wait for the regthread to finish before trying to send the message again */
				  
				  /* Resend the message, in case we got new peers. First build a list of new recipients */
				  /*synchronized(ChatClient.peerlist.clientlist) {
						for (RegClient peer: ChatClient.peerlist.clientlist) {
						   //rebuild our list as a list of only peers who haven't already seen the message
							if (!(this.isRead(peer))) {
							  clCopy.add(peer);		//clCopy can only have been empty on joining this else loop.
							}
						}
					 }
				  for (RegClient newpeer: clCopy) {
					  //This is a last resort message - we dont want to be stuck trying to send it forever.
					  Thread lastResort = new Thread(new MessageSender(sender, newpeer, seenBy, text, msgUID, TTL-1));	//Decrement the TTL with each message.
					  lastResort.start();
				  }*/ //commented out block for re-sending message to new peers due to, eh, implementation problems..
			  } catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			 }
			//}
		 }
		}
	}
	
	public void print() {
		try {
			System.out.println(sender.handle + ": " + text);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
}

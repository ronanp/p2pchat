
import java.io.*;
import java.net.Socket;




/* MessageHandler is a thread which is forked when the client receives an incoming message
 * It should accept the message and, if this is a new message, mark it as read by the 
 * client and forward it to other clients who haven't read it.
 */
public class MessageHandler implements Runnable {

	public Socket senderClientSocket = null;
	
	public MessageHandler(Socket senderClient) {
		this.senderClientSocket = senderClient;
	}

	public void run() {
		
			  try {
				  /* Fetch the Message instance we are being passed. Should contain
				   * an ip, port, and handle. Some text would be handy too!
				   */
				  ObjectInputStream ois = new ObjectInputStream(senderClientSocket.getInputStream());  
				  Message chatMessage = (Message) ois.readObject();  
				  if (chatMessage != null){
					  
					  /* We hopefully have a Message object. Tell the console of the incoming arrival. */
					   //System.out.println("DEBUG: Incoming MESSAGE from " + chatMessage.sender.handle + "@" + chatMessage.sender.ip + ":" + chatMessage.sender.port);
					  /* Have we seen this before? If not, print it and send it on */
					  
					  /* If multiple messages are received simultaneously, the seenmessages global may be checked on one thread while
					   * being updated with the same msgUID on another. Need to synchronize this process.
					   */
					 synchronized(ChatClient.seenMessages) {
					  if (!(ChatClient.seenMessages.contains(chatMessage.msgUID))) {
						  	//System.out.println("DEBUG: we are " + ChatClient.ourClient.ip + ChatClient.ourClient.handle + ChatClient.ourClient.port);
						  	/* If this message containts any chat, show it before we forward it */
						  	if (chatMessage.text.length() > 1) {		  
						  		chatMessage.print();
						  		
						  		//System.out.println("DEBUG: Message is "+chatMessage.text.length()+" chars long");
						  	}
						  	  //chatMessage.showSeenBy();
							  chatMessage.markRead(ChatClient.ourClient);	//Mark it read by us, both on the message itself and in our own version
							  ChatClient.seenMessages.add(chatMessage.msgUID);		
							  
							  //DEBUG: chatMessage.showSeenBy();
							  chatMessage.send();
						 
							  /* If its a blank message, its a peer saying hi. Either way, add this peer to our list if
							   * we don't have them already. (AddPeer will check for pre-existence)
							   */
							  //System.out.println("DEBUG: About to try add peer " +chatMessage.sender.port);
							  ChatClient.peerlist.AddPeer(chatMessage.sender);
							  //ChatClient.peerlist.printPeers();
						   
							  /* We should grab any new peers from the "seenBy" arraylist while we're at it.
							   * It should help with overall peer propagation.
							   */
							  for (RegClient newpeer: chatMessage.seenBy) {
								  if (!(newpeer.equals(ChatClient.ourClient))) {
									  ChatClient.peerlist.AddPeer(newpeer);
								  }
							  }
						 
					  }
					  else {	//We have read the message before, but send it on anyway (we may have peers others dont) (NOTE: removed: contradicts spec!)
						//  chatMessage.send();  //Removed, see above.
					  	//System.out.println("How did I get here?");
					  	//System.out.println(chatMessage.msgUID + "is the ID");
					  }
					 }	//end of synchronized(ChatClient.seenMessages)
				  ois.close();
				  }
			} catch (ClassNotFoundException | IOException e) {
				/* The registration server will periodically test the connection
				*  to our chat client, which will throw this exception, so
				* no need to complain to the console when we catch one.
				*/
				
				//e.printStackTrace();
			} finally {
				try {
					senderClientSocket.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			  
	}
	

}

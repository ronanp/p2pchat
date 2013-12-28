
import java.io.*;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;

//import p2pchat.common.*;

/* ChatInputHandler class is a thread which accepts input from the
 * keyboard and turns it into a Message object
 */
public class ChatInputHandler implements Runnable {
		
	
	public void run() {
		try {
			/* Read input from the keyboard */
			String CurLine = ""; // Line read from standard in
			System.out.println("Please type your chat text below: ");
			InputStreamReader converter = new InputStreamReader(System.in);
			BufferedReader in = new BufferedReader(converter);
			
			
			while (!(CurLine.equals("quit"))){
				
				//Take in a line of text
				CurLine = in.readLine();
				
				if (!(CurLine.equals("quit")) && (!(CurLine.isEmpty()))){
					
					/*Create a new empty "seenBy" list and add ourselves to it, so 
					* other clients don't waste resources sending it back to us.
					* (prevents network flooding).
					*/
					ArrayList<RegClient> seenlist = new ArrayList<RegClient>();
					seenlist.add(ChatClient.ourClient);
					
					/* Also generate a unique identifier for this message on the network.
					* Clients will keep track of unique identifiers they have seen before,
					* and drop the message. Identifier will combine our client, the system
					* time, and the message itself to ensure uniqueness.
					*/
					long timenow = System.currentTimeMillis();
					String signature = md5(ChatClient.ourClient.handle+ChatClient.ourClient.ip+ChatClient.ourClient.port+CurLine+timenow);
					
					/* Specify a maximum number of hops we want our message to take
					 * before it dies off
					 */	
					 
					/* Now send the message */
					Message msg = new Message(ChatClient.ourClient, CurLine, signature, ChatClient.TTL, seenlist);
					  msg.print();
					  //System.out.println("Marking message as read");
					  msg.markRead(ChatClient.ourClient);
					  //msg.showSeenBy();
					  msg.send();
				}
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			e.getMessage();
		}
	}
	
	//Function to generate the md5 hash of a string. Used to generate the unique ID of a chat message
		public static String md5(String str) {
	        
	        String md5 = null;   
	        if(str == null) {
	        	return null;   
	        }
	        try {
	        	//Create MessageDigest object for MD5
	        	MessageDigest digest = MessageDigest.getInstance("MD5");       
	        	//Update input string in message digest
	        	digest.update(str.getBytes(), 0, str.length());
	 
	        	//Convert to hex
	        	md5 = new BigInteger(1, digest.digest()).toString(16);
	        } catch (NoSuchAlgorithmException e) {
	            e.printStackTrace();
	        }
	        //System.out.println("md5 is " +md5);
	        return md5;
	    }
}

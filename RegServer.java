
import java.io.*;
import java.net.*;
import java.util.*;


/* RegServer class - defines our Registration server
 * Just sets up a socket and listens for connections,
 * which are each handled by an individual ClientHandler
 * thread
 */
public class RegServer {
	//Define some class variables
	public ServerSocket server = null;		//The socket we'll create to listen for Registration requests.
	public final int listenPort = 2222;		//The TCP port number to listen on.
	public static PeerList pl;				//A static list of global peers we are tracking.
	
	//To construct our class, we just create a new empty peer list and assign it to the
	//static class variable
	public RegServer() {
		RegServer.pl = new PeerList(new ArrayList<RegClient>());
	}
	
	//In main, we just want to initialise our RegServer and start the listensocket
	//function.
	public static void main(String[] args) throws IOException {
		RegServer rs = new RegServer();
		rs.listenSocket();
	}
	
	//listenSocket function binds to a serversocket, and forks a thread for each
	//individual client connection received.
	private void listenSocket() throws IOException{
		  try{
			  //Create our socket
			  server = new ServerSocket(this.listenPort);
			  System.out.println("Listening on interface " + server.getInetAddress().getHostAddress() + ":" + listenPort);
		  } catch (IOException e) {
			  	System.out.println("Unable to listen on interface " + server.getInetAddress().getHostAddress() + ":" + listenPort);
		    	e.printStackTrace();
			  	System.exit(-1);
		  }
		  //We also want to periodically poll our client peers, to make sure
		  //they are still alive and delete them from the global peerlist if not.
		  //We do this in a single thread, which should sleep at the end of every full iteration
		  //to avoid locking up a cpu
		  Thread peerMon = new Thread(new PeerMonitor(pl));
		  peerMon.start();
		
		  
		  while(true){
		    try{
		    	//server.accept returns a client connection, which we use to create a threaded
		    	//instance of our ClientHander class which will carry out the registration process, 
		    	// and/or distribute peers to clients.
		    	Thread chThread = new Thread(new ClientHandler(server.accept()));
		    	chThread.start();
		    } catch (IOException e) {
		      System.out.println("Accept failed on interface " + server.getInetAddress().getHostAddress() + ":" + listenPort);
		      e.printStackTrace();
		      System.exit(-1);
		    } 
		   }
	}
	
	
	
	

}

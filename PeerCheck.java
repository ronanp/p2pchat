
import java.io.IOException;
import java.net.Socket;

/* PeerCheck is an individual thread which attempts to connect 
 * to a client on a specified port.
 */
public class PeerCheck implements Runnable {

	public String ip = "null;";			//Client IP to test
	public int port = 0;				//TCP port to test
	public RegClient peer = null;		//RegClient object representing the peer this thread tests.
	public PeerList peerlist = null;    //peerlist to add/delete peers from
	
	//Constructor, obtain client and peerlist info from passed params.
	public PeerCheck(RegClient rc, PeerList pl) {
		this.ip = rc.ip;
		this.port = rc.port;
		this.peer = rc;
		this.peerlist = pl;	
	}
	public void run() {
		try {
			Socket peerSock = new Socket(ip,port);    
			peerSock.close();
			//No exception thus far means our peer is a live, do nothing!
		} catch (IOException e) {
			//If we catch a ConnectException, the port is closed, delete the peer
			// Need exclusive access to the peerlist first
			synchronized(peerlist) {
				peerlist.DeletePeer(peer);
			}
			//System.out.println("Deleted peer " + peer.handle + "@" + peer.ip + ":" + peer.port);
			//e.printStackTrace();
		}
		
	}
	

}

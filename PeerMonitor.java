
import java.util.ArrayList;

/* The PeerMonitor is a Threaded process which accesses the
 * a PeerList and polls each repeatedly to remove
 * any clients who have disconnected.
 */
public class PeerMonitor implements Runnable {

	public PeerList peerlist = null;	//PeerList containing our currently known peers
	private final int period = 20000;	//Poll interval in milliseconds
	private ArrayList<RegClient> tmplist = null;	//temporary list of peers
	
	public PeerMonitor(PeerList pl) {
		this.peerlist = pl;
	}

	public void run() {
		while (true) {		//never stop monitoring!
			/* Always start by taking a copy of the clientlist - if we try to delete from the
			 * global clientlist while we are iterating through it, we get a concurrent modification exception
			 */
			synchronized(this.peerlist.clientlist) {
				tmplist = new ArrayList<RegClient>(this.peerlist.clientlist);
			}
			for (RegClient peer: tmplist) {	
				Thread t = new Thread(new PeerCheck(peer, this.peerlist));
				t.start();
			}
			    //take a breather to avoid locking a cpu.
			try {
				Thread.sleep(period);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}	
	}
}

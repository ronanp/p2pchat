
import java.io.*;

/* RegClient class, just defines an instance of a chat client as would
 * be registered with the Registration Server. Must be serialised to be passed.
 */

public class RegClient implements Serializable {
	
	
	private static final long serialVersionUID = 1L;
	public String ip = null;		//The IP address the client listener is running on.
	public int port = 0;			//The tcp port the client listener is listening on.
	public String handle = null;	//The nickname/handle associated with this chat client.
	
		/* Constructor just converts input variables to instance variables */
		public RegClient(String clientIp, int clientPort, String clientHandle) {
			this.ip = clientIp;
			this.port = clientPort;
			this.handle = clientHandle;
		}
		
		/* equals function to allow us to compare two RegClient objects directly
		 * (including the current instance), returns true if clients identical
		 */
		public boolean equals(RegClient that) {
			if ((this.handle.equals(that.handle)) && (this.ip.equals(that.ip)) &&
					(this.port == that.port)) {
				return true;
			}
			else return false;
			
		}

		
}

package ccs.neu.edu.andang ;

// Util 
import java.util.Random ;

// Exceptions:
import java.net.SocketException ;
import java.net.UnknownHostException ;
import java.io.IOException ;

// Networking:
import java.net.URL ;
import java.net.InetAddress ;
import java.net.ServerSocket ;
import com.savarese.rocksaw.net.RawSocket;
import static com.savarese.rocksaw.net.RawSocket.PF_INET;

public class RawSocketClient{

	private RawSocket rSock ;

/* TCP functionalities supported:

Packet = IP Header + TCP Header + Data

-- Verify the checksums of incoming TCP packets (how?)
-- Generate correct checksums for outgoing packets. (IP)
-- Select a valid local port to send traffic on (done)
-- Perform the three-way handshake
-- Handle connection teardown. 
-- Handle sequence and acknowledgement numbers. 
-- Manage the advertised window as you see fit. 
-- Include basic timeout functionality: 
==> if a packet is not ACKed within 1 minute, assume the packet is lost and retransmit it. 
-- Able to receive out-of-order incoming packets and put them back into the correct order
-- Identify and discard duplicate packets. 
-- Implement a basic congestion window: 
==> start with cwnd=1, 
==> increment the cwnd after each succesful ACK, up to a fixed maximum of 1000 
-- If your program observes a packet drop or a timeout, reset the cwnd to 1.

*/
    private String remoteHost ;
    private int remotePort ;
    private InetAddress remoteAddress ;

    // TODO: set up the sender and receiver raw socks
    public RawSocketClient( String remoteHost, int remotePort ){
		this.remoteHost = remoteHost ;
		this.remotePort = remotePort ;
    }

    // TODO: handling TCP teardown process
    public void disconnect(){
		try{
			this.rSock.close() ;
		}
		catch(IOException ex){
			System.out.println( "Unable to disconnect: " + ex.toString() ) ;
		}
    }
    
    // TODO: connect to the remote server + doing the handshake
    public boolean connect() throws UnknownHostException, SocketException, IOException{

    	this.rSock = new RawSocket () ;
		this.rSock.open( PF_INET, RawSocket.getProtocolByName("tcp")) ;

		URL destURL = new URL( this.remoteHost ) ;

  	    this.remoteAddress = InetAddress.getByName( destURL.getHost() );

  	    if( remoteAddress.isAnyLocalAddress() || remoteAddress.isLoopbackAddress()
  	     || remoteAddress.isLinkLocalAddress() ){
  	    	return false ;
  	    }

  	    return true ;
    }
    
   
    // send the message to the remote server that we connect with
    // return: the InputStream from the server
    public void sendMessage( String message ) throws IOException{

    	TCPPacket packet = new TCPPacket() ;
    	packet.header.setSourcePort( (short) getAvailablePort() ) ;
    	packet.header.setDestinationPort( (short) 80) ;

    	packet.header.setSequenceNumber( 0 ) ;
    	packet.header.setACKNumber( 0 ) ;

    	packet.header.setHeaderLength( 20 ) ;
    	packet.header.setSYN() ;


    	this.rSock.write( this.remoteAddress , packet.header.getBaseHeader() ) ;
    

    }	

    boolean isIPSupported() throws SocketException {
    	return this.rSock.getIPHeaderInclude() ;
    } 


    // Strategy: pick a random port from [49152,65535]
    // use ServerSocket to verify that it iss open
    private int getAvailablePort() throws IOException {
    	int port = 0;
    	do {
        	port = (new Random()).nextInt(65535 - 49152 + 1) + 49152;
    	} while (!isPortAvailable(port));

    	return port;
	}

	private boolean isPortAvailable( int port ) throws IOException {

	    ServerSocket sock = null;
	    try {
	        sock = new ServerSocket(port);
	        sock.setReuseAddress(true);
	        return true;
	    } catch ( IOException e) {
	    	System.out.println( e.toString() ) ;
	    } finally {
	        if (ss != null) {
	            sock.close();
	        }
	    }

	    return false;
	}



	public static void main( String args[] ){
				
		try{
			RawSocketClient client = new RawSocketClient( 
				"http://www.ccs.neu.edu/home/cbw/4700/project4.html",80 ) ;
			client.connect() ;
			client.sendMessage( "" ) ;
		}
		catch (SocketException ex){
			System.out.println( ex.toString() ) ;
		}
		catch( IOException ex ){
			System.out.println( ex.toString() ) ;
		}
	}
}
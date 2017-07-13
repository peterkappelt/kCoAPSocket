/**
 * 
 */
package net.kappelt.kCoAPSocket;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Properties;

/**
 * @author peter
 *
 */
public class TcpServerThread implements Runnable {

	private int port;
	
	private String coapPSK;
	
	public TcpServerThread(int port, String coapPSK) {
		this.port = port;
		this.coapPSK = coapPSK;
	}

	
	/* (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run() {
		try {
			//listen to localhost only
			@SuppressWarnings("resource")
			ServerSocket socket = new ServerSocket(port, 0, InetAddress.getLoopbackAddress());
			System.out.println("[TcpServerThread] Binding of socket @ port " + port	+ " successfull.");

			/**
			 * Coap instance
			 */
			Coap coapClient = new Coap(coapPSK);
			coapClient.debugOutputEnable();
			
			//accept new connections in an endless loop
			//@todo I don't think this is a good idea -> what to do, if someone opens a lot of connections to attack?
			while (true){
				try{
				    //block, until there's a connection request
					Socket clientSocket = socket.accept();
					System.out.println("[TcpServerThread] Connection at port " + port + " opened");
					
					Thread threadHandler = new Thread(new TcpServerHandler(clientSocket, coapClient));
					threadHandler.start();
				}catch (IOException e){
					System.out.println("[TcpServerThread] New connection request failed");
					System.exit(-1);
				}
		      
		    }
		} catch (IOException e) {
			System.out.println("[TcpServerThread] Error while binding socket: " + e.getMessage());
			System.exit(-1);
		} catch (Exception e) {
			System.out.println("[TcpServerThread] Caught exception: " + e.getMessage());
			System.exit(-1);
		}
	}

}

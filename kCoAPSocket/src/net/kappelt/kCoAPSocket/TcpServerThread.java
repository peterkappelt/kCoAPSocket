/**
 * 
 */
package net.kappelt.kCoAPSocket;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * @author peter
 *
 */
public class TcpServerThread implements Runnable {

	private int port;
	
	private String coapPSK;
	private String coapAddress;
	
	private Boolean debugEnable;
	
	public TcpServerThread(int port, String coapPSK, String coapAddress, Boolean debugEnable) {
		this.port = port;
		this.coapPSK = coapPSK;
		this.coapAddress = coapAddress;
		this.debugEnable = debugEnable;
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
			//ServerSocket socket = new ServerSocket(port);
			System.out.println("[TcpServerThread] Binding of socket @ port " + port	+ " successfull.");

			/**
			 * Coap instance
			 */
			Coap coapClient = new Coap(coapPSK);
			
			//debugEnable is true if user gave -d parameter
			if(debugEnable){
				coapClient.debugOutputEnable();
			}
			
			//accept new connections in an endless loop
			//@todo I don't think this is a good idea -> what to do, if someone opens a lot of connections to attack?
			while (true){
				try{
				    //block, until there's a connection request
					Socket clientSocket = socket.accept();
					System.out.println("[TcpServerThread] Connection at port " + port + " opened");
					
					Thread threadHandler = new Thread(new TcpServerHandler(clientSocket, coapClient, coapAddress));
					threadHandler.start();
				}catch (IOException e){
					System.out.println("[TcpServerThread] New connection request failed");
					System.exit(-1);
				}
		      
		    }
		} catch (IOException e) {
			System.out.println("[TcpServerThread] Error while binding socket: " + e.getMessage());
			e.printStackTrace();
			System.exit(-1);
		} catch (Exception e) {
			System.out.println("[TcpServerThread] Caught exception: " + e.getMessage());
			e.printStackTrace();
			System.exit(-1);
		}
	}

}

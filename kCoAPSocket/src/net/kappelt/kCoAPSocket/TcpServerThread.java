/**
 * 
 */
package net.kappelt.kCoAPSocket;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Objects;

import org.eclipse.californium.core.CoapHandler;
import org.eclipse.californium.core.CoapResponse;

/**
 * @author peter
 *
 */
public class TcpServerThread implements Runnable {

	private int port;
	private String serverPSK = "xxx";
	
	public TcpServerThread(int port) {
		this.port = port;
	}

	public void writeResponse(Socket socketToWriteTo, String text){		
		PrintWriter out = null;
		try {
			out = new PrintWriter(socketToWriteTo.getOutputStream(), true);
		} catch (IOException e) {
			System.err.println(e.getMessage());
		}
		
		System.out.println("[TcpServerThread] Sending response: " + text);
		out.println(text + "\r\n");
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run() {
		while (true) {
			try {
				//listen to localhost only
				@SuppressWarnings("resource")
				ServerSocket socket = new ServerSocket(port, 0, InetAddress.getLoopbackAddress());
				System.out.println("[TcpServerThread] Binding of socket @ port " + port	+ " successfull.");
				
				Socket clientSocket = socket.accept();
				System.out.println("[TcpServerThread] Connection at port " + port + " opened");

				/**
				 * Coap instance
				 */
				Coap client = new Coap(this.serverPSK);
				
				while (true) {
					// read data
					BufferedReader inData = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
					String dataLine = inData.readLine();
					
					if(dataLine != null){
						System.out.println("[TcpServerThread] Received data: " + dataLine);
						
						dataLine.replace("\n", "").replace("\r", "");		//remove newlines, like at the end
						String[] commandParts = dataLine.split("\\|");		//split the command, separated by "|"
						
						if(commandParts.length < 1){
							System.out.println("[TcpServerThread] Not enough command parts!");
							continue;
						}
						
						if(Objects.equals(commandParts[0], "ping")){
							writeResponse(clientSocket, "pong");
						}else if(Objects.equals(commandParts[0], "setPSK")){
							if(commandParts.length < 2){
								System.out.println("[TcpServerThread] Command \"setPSK\" requires one parameter");
								continue;
							}
							System.out.println("[TcpServerThread] Set PSK to " + commandParts[1]);
							this.serverPSK = commandParts[1];
							client.setPsk(this.serverPSK);
						}else if(Objects.equals(commandParts[0], "coapGet")){
							if(commandParts.length < 2){
								System.out.println("[TcpServerThread] Command \"coapGet\" requires one parameter");
								continue;
							}
							
							String response = client.get(commandParts[1]).getResponseText();
							writeResponse(clientSocket, response);
						}else if(Objects.equals(commandParts[0], "coapPostJSON")){
							if(commandParts.length < 3){
								System.out.println("[TcpServerThread] Command \"coapPostJSON\" requires two parameters");
								continue;
							}
							
							String response = client.postJSON(commandParts[1], commandParts[2]).getResponseText();
							writeResponse(clientSocket, response);
						}else if(Objects.equals(commandParts[0], "coapPutJSON")){
							if(commandParts.length < 3){
								System.out.println("[TcpServerThread] Command \"coapPutJSON\" requires two parameters");
								continue;
							}
							
							String response = client.putJSON(commandParts[1], commandParts[2]).getResponseText();
							writeResponse(clientSocket, response);
						}else if(Objects.equals(commandParts[0], "coapObserveStart")){
							if(commandParts.length < 2){
								System.out.println("[TcpServerThread] Command \"coapObserveStart\" requires one parameter");
								continue;
							}
							
							client.observe(commandParts[1], new CoapHandler(){
								@Override public void onLoad(CoapResponse response){
									System.out.println("[TcpServerThread] New state for observed resource " + commandParts[1] + ": " + response.getResponseText());
									writeResponse(clientSocket, "observedUpdate|" + commandParts[1] + "|" + response.getResponseText());
								}
								
								@Override public void onError() {
									System.err.println("[TcpServerThread] Observing failed");
								}
							});
							
						}
						
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

}

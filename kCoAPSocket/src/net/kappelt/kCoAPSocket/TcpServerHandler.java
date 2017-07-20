/**
 * 
 */
package net.kappelt.kCoAPSocket;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Objects;

import org.eclipse.californium.core.CoapHandler;
import org.eclipse.californium.core.CoapResponse;

/**
 * @author peter
 *
 */
public class TcpServerHandler implements Runnable {

	/**
	 * data for handling
	 */
	private Socket clientSocket;
	private Coap coapClient;
	private String gatewayAddress;
	
	/**
	 * Construct a new Handler
	 */
	public TcpServerHandler(Socket clientSocket, Coap coapClient, String gatewayAddress) {
		this.clientSocket = clientSocket;
		this.coapClient = coapClient;
		this.gatewayAddress = gatewayAddress;
	}

	/* (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run() {
		try{
			// read data and write data
			BufferedReader inData = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
			String dataLine = null;
			
			PrintWriter outData = new PrintWriter(clientSocket.getOutputStream(), true);
			
			//this is used to stop the thread, once the connection is broken
			Boolean executeLoop = true;
			
			System.out.println("[TcpServerThread] Fetching well-known...");
			System.out.println("Well Known: " + coapClient.get("coaps://" + gatewayAddress + "/.well-known/core").getResponseText());
			
			while(executeLoop){
				dataLine = inData.readLine();
				
				if(dataLine != null){
					System.out.println("[TcpServerThread] Received data: " + dataLine);
					
					dataLine.replace("\n", "").replace("\r", "");		//remove newlines, like at the end
					String[] commandParts = dataLine.split("\\|");		//split the command, separated by "|"
					
					if(commandParts.length < 1){
						System.out.println("[TcpServerThread] Not enough command parts!");
						continue;
					}
					
					if(Objects.equals(commandParts[0], "ping")){
						outData.println("pong");
					}else if(Objects.equals(commandParts[0], "coapGet")){
						if(commandParts.length < 2){
							System.out.println("[TcpServerThread] Command \"coapGet\" requires one parameter");
							continue;
						}
						
						String response = coapClient.get(commandParts[1]).getResponseText();
						outData.println("coapGet|" + commandParts[1] + "|" + response);
						System.out.println("[TcpServerThread] Command \"coapGet\" on \"" + commandParts[1] + "\" returned \"" + response + "\"");
					}else if(Objects.equals(commandParts[0], "coapPostJSON")){
						if(commandParts.length < 3){
							System.out.println("[TcpServerThread] Command \"coapPostJSON\" requires two parameters");
							continue;
						}
						
						String response = coapClient.postJSON(commandParts[1], commandParts[2]).getResponseText();
						outData.println("coapPostJSON|" + commandParts[1] + "|" + response);
						System.out.println("[TcpServerThread] Command \"coapPostJSON\" on \"" + commandParts[1] + "\" returned \"" + response + "\"");
					}else if(Objects.equals(commandParts[0], "coapPutJSON")){
						if(commandParts.length < 3){
							System.out.println("[TcpServerThread] Command \"coapPutJSON\" requires two parameters");
							continue;
						}
						
						String response = coapClient.putJSON(commandParts[1], commandParts[2]).getResponseText();
						outData.println("coapPutJSON|" + commandParts[1] + "|" + response);
						System.out.println("[TcpServerThread] Command \"coapPutJSON\" on \"" + commandParts[1] + "\" returned \"" + response + "\"");
					}else if(Objects.equals(commandParts[0], "coapObserveStart")){
						if(commandParts.length < 2){
							System.out.println("[TcpServerThread] Command \"coapObserveStart\" requires one parameter");
							continue;
						}
						
						coapClient.observe(commandParts[1], new CoapHandler(){
							@Override public void onLoad(CoapResponse response){
								System.out.println("[TcpServerThread] New state for observed resource " + commandParts[1] + ": " + response.getResponseText());
								outData.println("observedUpdate|" + commandParts[1] + "|" + response.getResponseText());
							}
							
							@Override public void onError() {
								System.err.println("[TcpServerThread] Observing failed");
							}
						});	
					}else{
						System.err.println("[TcpServerThread] Unknown command: " + commandParts[1]);
					}
					
				}

				if((dataLine == null) || clientSocket.isClosed()){
					//the received dataline was "null" -> probably the remote side closed the socket
					//additionally, the socket could be closed on the server side, though that isn't implemented here
					executeLoop = false;
					System.out.println("[TcpServerThread] Client socket was probably closed by remote");
					
					coapClient.observeStopAll();
					System.out.println("[TcpServerThread] Stopped observing all resources");
				}
			}
		}catch(Exception e){
			System.err.println("[TcpServerThread] Error in client socket, ending this socket: " + e.getMessage());
		}
	}

}

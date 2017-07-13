/**
 * 
 */
package net.kappelt.kCoAPSocket;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;

/**
 * @author peter
 *
 */
public class kCoapSocketMain {
	@Parameter(names = {"-p", "--port"}, description = "Port for the TCP socket to use, default 1505")
	private Integer port = 1505;
	
	@Parameter(names = {"-s", "--secret"}, description = "PSK that is used to connect to the gateway")
	private String coapPSK = "";
	
	public static void main(String[] args) {
		kCoapSocketMain main = new kCoapSocketMain();
		
		JCommander.newBuilder()
			.addObject(main)
			.build()
			.parse(args);
		
		main.startThreadHandler();
	}
	
	public void startThreadHandler(){
		//run the background socket thread
		Thread threadHandler = new Thread(new TcpServerThread(port, coapPSK));
		threadHandler.start();
	}

}

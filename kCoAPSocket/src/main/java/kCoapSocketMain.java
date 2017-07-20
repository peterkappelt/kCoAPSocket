/**
 * 
 */
package main.java;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;

import net.kappelt.kCoAPSocket.TcpServerThread;

/**
 * @author peter
 *
 */
public class kCoapSocketMain {
	@Parameter(names = {"-p", "--port"}, description = "Port for the TCP socket to use, default 1505", help=true)
	private Integer port = 1505;
	
	@Parameter(names = {"-s", "--secret"}, description = "PSK that is used to connect to the gateway", required=true, help=true)
	private String coapPSK = "";
	
	@Parameter(names = {"-g", "--gateway"}, description = "IP or DNS name of the gateway to connect to, without leading or trailing slash", required=true, help=true)
	private String coapAddress = "";
	
	public static void main(String[] args) {
		kCoapSocketMain main = new kCoapSocketMain();
		
		JCommander cmdLine = JCommander.newBuilder().addObject(main).build();	
		try{
			cmdLine.parse(args);
		}catch(Exception e){
			System.err.println("Parameter error: " + e.getMessage());
			cmdLine.usage();
			System.exit(-1);
		}
		
		System.out.println("kCoAPSocket 0.0.3-snapshot");
		System.out.println();
		
		main.startThreadHandler();
	}
	
	public void startThreadHandler(){
		if(coapPSK.length() != 16){
			System.out.println("Warning: Your set PSK \"" + coapPSK + "\" doesn't seem to be a correct Tradfri-PSK. This could be a false alarm.");
			System.out.println();
		}
		//run the background socket thread
		Thread threadHandler = new Thread(new TcpServerThread(port, coapPSK, coapAddress));
		threadHandler.start();
	}

}

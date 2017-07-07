/**
 * 
 */
package net.kappelt.kCoAPSocket;

import org.eclipse.californium.core.CoapResponse;
import org.eclipse.californium.core.Utils;

/**
 * @author peter
 *
 */
public class DemoMain {
	public static void main(String[] args) {
		//run the background socket thread
		Thread threadHandler = new Thread(new TcpServerThread(505));
		threadHandler.start();
		
		Coap client = new Coap();
		
		client.debugOutputDisable();
		client.setPsk("xxx");
		
		CoapResponse resp = client.get("coaps://192.168.2.65/.well-known/core");
		
		if (resp != null) {

			System.out.println(resp.getCode());
			System.out.println(resp.getOptions());
			System.out.println(resp.getResponseText());

			System.out.println("\nADVANCED\n");
			System.out.println(Utils.prettyPrint(resp));

		} else {
			System.out.println("No response received.");
		}
		
		//System.exit(0);
		
		
	}

}

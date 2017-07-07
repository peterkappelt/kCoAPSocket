/**
 * 
 */
package net.kappelt.kCoAPSocket;

import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.util.logging.Level;

import org.eclipse.californium.core.CaliforniumLogger;
import org.eclipse.californium.core.CoapClient;
import org.eclipse.californium.core.CoapResponse;
import org.eclipse.californium.core.coap.MediaTypeRegistry;
import org.eclipse.californium.core.network.CoapEndpoint;
import org.eclipse.californium.core.network.config.NetworkConfig;
import org.eclipse.californium.scandium.DTLSConnector;
import org.eclipse.californium.scandium.ScandiumLogger;
import org.eclipse.californium.scandium.config.DtlsConnectorConfig;
import org.eclipse.californium.scandium.dtls.pskstore.StaticPskStore;

/**
 * @author peter
 *
 */
public class Coap {

	/**
	 * Fixed variables for trust key stores
	 * @ToDo Migrate the project "demo-certs" to a own one, for increased security
	 */
	private static final String TRUST_STORE_PASSWORD = "rootPass";
	private static final String KEY_STORE_PASSWORD = "endPass";
	private static final String KEY_STORE_LOCATION = "certs/keyStore.jks";
	private static final String TRUST_STORE_LOCATION = "certs/trustStore.jks";
	
	/**
	 * DTLS connector, in order to provide security features to CoAP-Classes
	 */
	private DTLSConnector dtlsConnector;
	
	/**
	 * The Californium CoAP-Client-Instance itself
	 */
	private CoapClient client;
	
	public Coap() {
		client = new CoapClient();
	}
	
	public Coap(String psk){
		client = new CoapClient();
		this.setPsk(psk);
	}
	
	/**
	 * Enable the verbosity of the Scandium/ Californium library
	 */
	public void debugOutputEnable(){
		CaliforniumLogger.initialize();
		CaliforniumLogger.setLevel(Level.WARNING);
		
		ScandiumLogger.initialize();
		ScandiumLogger.setLevel(Level.FINER);
	}
	
	/**
	 * Mute the output of the Scandium/ Californium library
	 */
	public void debugOutputDisable(){
		CaliforniumLogger.initialize();
		CaliforniumLogger.setLevel(Level.OFF);
		
		ScandiumLogger.initialize();
		ScandiumLogger.setLevel(Level.OFF);
	}
	
	/**
	 * Necessary if you use CoAP with DTLS
	 * You need to call this method after constructing in order to define the PSK
	 * It sets the PSK and inits the keystore
	 * @param psk The pre-shared key
	 */
	public void setPsk(String psk){
		try {
			// load key store
			KeyStore keyStore = KeyStore.getInstance("JKS");
			InputStream in = getClass().getClassLoader().getResourceAsStream(KEY_STORE_LOCATION);
			keyStore.load(in, KEY_STORE_PASSWORD.toCharArray());
			in.close();

			// load trust store
			KeyStore trustStore = KeyStore.getInstance("JKS");
			in = getClass().getClassLoader().getResourceAsStream(TRUST_STORE_LOCATION);
			trustStore.load(in, TRUST_STORE_PASSWORD.toCharArray());
			in.close();

			// You can load multiple certificates if needed
			Certificate[] trustedCertificates = new Certificate[1];
			trustedCertificates[0] = trustStore.getCertificate("root");

			DtlsConnectorConfig.Builder builder = new DtlsConnectorConfig.Builder(new InetSocketAddress(0));
			builder.setPskStore(new StaticPskStore("Client_identity", psk.getBytes()));
			builder.setIdentity((PrivateKey)keyStore.getKey("client", KEY_STORE_PASSWORD.toCharArray()),
					keyStore.getCertificateChain("client"), true);
			builder.setTrustStore(trustedCertificates);
			dtlsConnector = new DTLSConnector(builder.build());

		} catch (Exception e) {
			System.err.println("[Coap] Error in Coap.setPSK (while initializing keystore): ");
			e.printStackTrace();
			System.exit(-1);
		}
		
		client.setEndpoint(new CoapEndpoint(dtlsConnector, NetworkConfig.getStandard()));
	}
	
	/**
	 * Do a Get-Request on a specified URI.
	 * @param URI the URL with the coap path. Must start with coap:// or coaps://
	 * @return The response, null if the request wasn't successfull
	 */
	CoapResponse get(String uri){
		CoapResponse response = null;
		try {
			//construct an URI to check validity
			@SuppressWarnings("unused")
			URI temp = new URI(uri);
			
			client.setURI(uri);
			response = client.get();

		} catch (URISyntaxException e) {
			System.err.println("[Coap] Invalid URI in Coap.get: " + e.getMessage());
			System.exit(-1);
		}

		return response;
	}

	/**
	 * Do a Post-Request on a specified URI. The data gets the content type application/json
	 * @param URI the URL with the coap path. Must start with coap:// or coaps://
	 * @return The response, null if the request wasn't successfull
	 */
	CoapResponse postJSON(String uri, String payload){
		CoapResponse response = null;
		try {
			//construct an URI to check validity
			@SuppressWarnings("unused")
			URI temp = new URI(uri);
			
			client.setURI(uri);
			response = client.post(payload, MediaTypeRegistry.APPLICATION_JSON);

		} catch (URISyntaxException e) {
			System.err.println("[Coap] Invalid URI in Coap.postJSON: " + e.getMessage());
			System.exit(-1);
		}

		return response;
	}
	
	/**
	 * Do a Put-Request on a specified URI. The data gets the content type application/json
	 * @param URI the URL with the coap path. Must start with coap:// or coaps://
	 * @return The response, null if the request wasn't successfull
	 */
	CoapResponse putJSON(String uri, String payload){
		CoapResponse response = null;
		try {
			//construct an URI to check validity
			@SuppressWarnings("unused")
			URI temp = new URI(uri);
			
			client.setURI(uri);
			response = client.put(payload, MediaTypeRegistry.APPLICATION_JSON);

		} catch (URISyntaxException e) {
			System.err.println("[Coap] Invalid URI in Coap.putJSON: " + e.getMessage());
			System.exit(-1);
		}

		return response;
	}

}

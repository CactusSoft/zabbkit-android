package ru.zabbkitserver.android.remote.client;


import java.io.IOException;
import java.io.OutputStreamWriter;

import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.HttpCookie;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;

import java.security.SecureRandom;

import java.security.cert.X509Certificate;

import java.util.Collections;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import com.thetransactioncompany.jsonrpc2.JSONRPC2Notification;
import com.thetransactioncompany.jsonrpc2.JSONRPC2ParseException;
import com.thetransactioncompany.jsonrpc2.JSONRPC2Request;
import com.thetransactioncompany.jsonrpc2.JSONRPC2Response;

import ru.zabbkitserver.android.remote.request.Request;
import ru.zabbkitserver.android.remote.utils.HttpAuth;


/** 
 * Sends requests and / or notifications to a specified JSON-RPC 2.0 server 
 * URL. The JSON-RPC 2.0 messages are dispatched by means of HTTP(S) POST.
 * This class is thread-safe.
 *
 * <p>The client-session class has a number of {@link JSONRPC2SessionOptions 
 * optional settings}. To change them pass a modified options instance to the
 * {@link #setOptions setOptions()} method.
 *
 * <p>Example JSON-RPC 2.0 client session:
 *
 * <pre>
 * // First, import the required packages:
 * 
 * // The Client sessions package
 * import com.thetransactioncompany.jsonrpc2.client.*;
 * 
 * // The Base package for representing JSON-RPC 2.0 messages
 * import com.thetransactioncompany.jsonrpc2.*;
 * 
 * // The JSON Smart package for JSON encoding/decoding (optional)
 * import net.minidev.json.*;
 * 
 * // For creating URLs
 * import java.net.*;
 * 
 * // ...
 * 
 * 
 * // Creating a new session to a JSON-RPC 2.0 web service at a specified URL
 * 
 * // The JSON-RPC 2.0 server URL
 * URL serverURL = null;
 * 
 * try {
 * 	serverURL = new URL("http://jsonrpc.example.com:8080");
 * 	
 * } catch (MalformedURLException e) {
 * 	// handle exception...
 * }
 * 
 * // Create new JSON-RPC 2.0 client session
 *  JSONRPC2Session mySession = new JSONRPC2Session(serverURL);
 * 
 * 
 * // Once the client session object is created, you can use to send a series
 * // of JSON-RPC 2.0 requests and notifications to it.
 * 
 * // Sending an example "getServerTime" request:
 * 
 *  // Construct new request
 *  String method = "getServerTime";
 *  int requestID = 0;
 *  JSONRPC2Request request = new JSONRPC2Request(method, requestID);
 * 
 *  // Send request
 *  JSONRPC2Response response = null;
 * 
 *  try {
 *          response = mySession.send(request);
 * 
 *  } catch (JSONRPC2SessionException e) {
 * 
 *          System.err.println(e.getMessage());
 *          // handle exception...
 *  }
 * 
 *  // Print response result / error
 *  if (response.indicatesSuccess())
 * 	System.out.println(response.getResult());
 *  else
 * 	System.out.println(response.getError().getMessage());
 * 
 * </pre>
 *
 * @author Vladimir Dzhuvinov
 */
public class JSONRPC2Session {

    private String httpLogin;
    private String httpPass;


	/** 
	 * The server URL, which protocol must be HTTP or HTTPS. 
	 *
	 * <p>Example URL: "http://jsonrpc.example.com:8080"
	 */
	private URL url;


	/**
	 * The client-session options.
	 */
	private JSONRPC2SessionOptions options;


	/**
	 * Custom HTTP URL connection configurator.
	 */
	private ConnectionConfigurator connectionConfigurator;
	
	
	/**
	 * Optional HTTP raw response inspector.
	 */
	private RawResponseInspector responseInspector;
	
	
	/**
	 * Optional HTTP cookie manager. 
	 */
	private CookieManager cookieManager;


	/**
	 * Trust-all-certs (including self-signed) SSL socket factory.
	 */
	private static SSLSocketFactory trustAllSocketFactory = createTrustAllSocketFactory();

	public JSONRPC2Session(final Request request) {

        URL url = null;
        try {
            url = new URL(request.url);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        if(url !=null) {
            if (!url.getProtocol().equalsIgnoreCase("http") &&
                    !url.getProtocol().equalsIgnoreCase("https"))
                throw new IllegalArgumentException("The URL protocol must be HTTP or HTTPS");

            this.url = url;
        }

        this.httpLogin = request.httpAuthLogin;
        this.httpPass = request.httpAuthPass;

		// Default session options
		options = new JSONRPC2SessionOptions();

		// No initial connection configurator
		connectionConfigurator = null;
	}
	
	
	/**
	 * Creates a trust-all-certificates SSL socket factory. Encountered 
	 * exceptions are not rethrown.
	 *
	 * @return The SSL socket factory.
	 */
	public static SSLSocketFactory createTrustAllSocketFactory() {
	
		TrustManager[] trustAllCerts = new TrustManager[] {
			
			new X509TrustManager() {

				public X509Certificate[] getAcceptedIssuers() { return new X509Certificate[]{}; }

				public void checkClientTrusted(X509Certificate[] certs, String authType) { }

				public void checkServerTrusted(X509Certificate[] certs, String authType) { }
			}
		};

		try {
			SSLContext sc = SSLContext.getInstance("SSL");
			sc.init(null, trustAllCerts, new SecureRandom());
			return sc.getSocketFactory();

		} catch (Exception e) {
			
			// Ignore
			return null;
		}
	}


	/**
	 * Gets the JSON-RPC 2.0 server URL.
	 *
	 * @return The server URL.
	 */
	public URL getURL() {

		return url;
	}


	/**
	 * Sets the JSON-RPC 2.0 server URL.
	 *
	 * @param url The server URL. Must not be {@code null}.
	 */
	public void setURL(final URL url) {

		if (url == null)
			throw new IllegalArgumentException("The server URL must not be null");
		
		this.url = url;
	}


	/**
	 * Gets the JSON-RPC 2.0 client session options.
	 *
	 * @return The client session options.
	 */
	public JSONRPC2SessionOptions getOptions() {

		return options;
	}


	/**
	 * Sets the JSON-RPC 2.0 client session options.
	 *
	 * @param options The client session options, must not be {@code null}.
	 */
	public void setOptions(final JSONRPC2SessionOptions options) {

		if (options == null)
			throw new IllegalArgumentException("The client session options must not be null");

		this.options = options;
	}


	/**
	 * Gets the custom HTTP URL connection configurator.
	 *
	 * @since 1.5
	 *
	 * @return The connection configurator, {@code null} if none is set.
	 */
	public ConnectionConfigurator getConnectionConfigurator() {

		return connectionConfigurator;
	}


	/**
	 * Specifies a custom HTTP URL connection configurator. It will be
	 * {@link ConnectionConfigurator#configure applied} to each new HTTP
	 * connection after the {@link JSONRPC2SessionOptions session options}
	 * are applied and before the connection is established.
	 *
	 * <p>This method may be used to set custom HTTP request headers, 
	 * timeouts or other properties.
	 *
	 * @since 1.5
	 *
	 * @param connectionConfigurator A custom HTTP URL connection 
	 *                               configurator, {@code null} to remove
	 *                               a previously set one.
	 */
	public void setConnectionConfigurator(final ConnectionConfigurator connectionConfigurator) {

		this.connectionConfigurator = connectionConfigurator;
	}
	
	
	/**
	 * Gets the optional inspector for the raw HTTP responses.
	 * 
	 * @since 1.6
	 * 
	 * @return The optional inspector for the raw HTTP responses, 
	 *         {@code null} if none is set.
	 */
	public RawResponseInspector getRawResponseInspector() {
		
		return responseInspector;
	}
	
	
	/**
	 * Specifies an optional inspector for the raw HTTP responses to 
	 * JSON-RPC 2.0 requests and notifications. Its 
	 * {@link RawResponseInspector#inspect inspect} method will be called 
	 * upon reception of a HTTP response.
	 * 
	 * <p>You can use the {@link RawResponseInspector} interface to 
	 * retrieve the unparsed response content and headers.
	 * 
	 * @since 1.6
	 * 
	 * @param responseInspector An optional inspector for the raw HTTP 
	 *                          responses, {@code null} to remove a 
	 *                          previously set one.
	 */
	public void setRawResponseInspector(final RawResponseInspector responseInspector) {
		
		this.responseInspector = responseInspector;
	}
	
	
	/**
	 * Gets all non-expired HTTP cookies currently stored in the client.
	 * 
	 * @return The HTTP cookies, or empty list if none were set by the 
	 *         server or cookies are not 
	 *         {@link JSONRPC2SessionOptions#acceptCookies accepted}.
	 */
	public List<HttpCookie> getCookies() {
		
		if (cookieManager == null) {

			List<HttpCookie> emptyList = Collections.emptyList();
			return emptyList;
		}

		return cookieManager.getCookieStore().getCookies();
	}	


	/**
	 * Applies the required headers to the specified URL connection.
	 *
	 * @param con The URL connection which must be open.
	 *
	 * @throws JSONRPC2SessionException If an exception is encountered.
	 */
	private void applyHeaders(final URLConnection con)
		throws JSONRPC2SessionException {

		// Expect UTF-8 for JSON
		con.setRequestProperty("Accept-Charset", "UTF-8");

		// Add "Content-Type" header?
		if (options.getRequestContentType() != null)
			con.setRequestProperty("Content-Type", options.getRequestContentType());

		// Add "Origin" header?
		if (options.getOrigin() != null)
			con.setRequestProperty("Origin", options.getOrigin());

		// Add "Accept-Encoding: gzip, deflate" header?
		if (options.enableCompression())
			con.setRequestProperty("Accept-Encoding", "gzip, deflate");
		
		// Add "Cookie" headers?
		if (options.acceptCookies()) {

			StringBuilder buf = new StringBuilder();
			
			for (HttpCookie cookie: getCookies()) {

				if (buf.length() > 0)
					buf.append("; ");

				buf.append(cookie.toString());
			}

			con.setRequestProperty("Cookie", buf.toString());
		}
	}
	
	
	/**
	 * Creates and configures a new URL connection to the JSON-RPC 2.0 
	 * server endpoint according to the session settings.
	 *
	 * @return The URL connection, configured and ready for output (HTTP 
	 *         POST).
	 *
	 * @throws JSONRPC2SessionException If the URL connection couldn't be
	 *                                  created or configured.
	 */
	private URLConnection createURLConnection()
		throws JSONRPC2SessionException {
		
		// Open HTTP connection
		URLConnection con = null;

		try {
			// Use proxy?
			if (options.getProxy() != null)
				con = url.openConnection(options.getProxy());
			else
				con = url.openConnection();

		} catch (IOException e) {

			throw new JSONRPC2SessionException(
					"Network exception: " + e.getMessage(),
					JSONRPC2SessionException.NETWORK_EXCEPTION,
					e);
		}
		
		con.setConnectTimeout(options.getConnectTimeout());
		con.setReadTimeout(options.getReadTimeout());

		applyHeaders(con);

		// Set POST mode
		con.setDoOutput(true);

		// Set trust all certs SSL factory?
		if (con instanceof HttpsURLConnection && options.trustsAllCerts()) {
		
			if (trustAllSocketFactory == null)
				throw new JSONRPC2SessionException("Couldn't obtain trust-all SSL socket factory");
		
			((HttpsURLConnection)con).setSSLSocketFactory(trustAllSocketFactory);
		}

		// Apply connection configurator?
		if (connectionConfigurator != null)
			connectionConfigurator.configure((HttpURLConnection)con);

        HttpAuth httpAuth = new HttpAuth();
        con = httpAuth.tryDigestAuthentication((HttpURLConnection) con, httpLogin, httpPass,
                options, connectionConfigurator);
		return con;
	}
	
	
	/**
	 * Posts string data (i.e. JSON string) to the specified URL 
	 * connection.
	 *
	 * @param con  The URL connection. Must be in HTTP POST mode. Must not 
	 *             be {@code null}.
	 * @param data The string data to post. Must not be {@code null}.
	 *
	 * @throws JSONRPC2SessionException If an I/O exception is encountered.
	 */
	private static void postString(final URLConnection con, final String data)
		throws JSONRPC2SessionException {
		
		try {
			OutputStreamWriter wr = new OutputStreamWriter(con.getOutputStream(), "UTF-8");
			wr.write(data);
			wr.flush();
			wr.close();

		} catch (IOException e) {

			throw new JSONRPC2SessionException(
					"Network exception: " + e.getMessage(),
					JSONRPC2SessionException.NETWORK_EXCEPTION,
					e);
		}
	}
	
	private RawResponse readRawResponse(final URLConnection con)
		throws JSONRPC2SessionException {
	
		RawResponse rawResponse = null;
		
		try {
			rawResponse = RawResponse.parse((HttpURLConnection)con);

		} catch (IOException e) {

			throw new JSONRPC2SessionException(
					"Network exception: " + e.getMessage(),
					JSONRPC2SessionException.NETWORK_EXCEPTION,
					e);
		}
		
		if (responseInspector != null)
			responseInspector.inspect(rawResponse);
		
		if (options.acceptCookies()) {

			// Init cookie manager?
			if (cookieManager == null)
				cookieManager = new CookieManager(null, CookiePolicy.ACCEPT_ALL);

			try {
				cookieManager.put(con.getURL().toURI(), rawResponse.getHeaderFields());

			} catch (URISyntaxException e) {

				throw new JSONRPC2SessionException(
					"Network exception: " + e.getMessage(),
					JSONRPC2SessionException.NETWORK_EXCEPTION,
					e);

			} catch (IOException e) {

				throw new JSONRPC2SessionException(
					"I/O exception: " + e.getMessage(),
					JSONRPC2SessionException.NETWORK_EXCEPTION,
					e);
			}
		}
		
		return rawResponse;
	}

	/** 
	 * Sends a JSON-RPC 2.0 request using HTTP POST and returns the server
	 * response.
	 *
	 * @param request The JSON-RPC 2.0 request to send. Must not be 
	 *                {@code null}.
	 *
	 * @return The JSON-RPC 2.0 response returned by the server.
	 *
	 * @throws JSONRPC2SessionException On a network error, unexpected HTTP 
	 *                                  response content type or invalid 
	 *                                  JSON-RPC 2.0 response.
	 */
	public JSONRPC2Response send(final JSONRPC2Request request)
		throws JSONRPC2SessionException {

		// Create and configure URL connection to server endpoint
		URLConnection con = createURLConnection();

		// Send request encoded as JSON
		postString(con, request.toString());

		// Get the response
		RawResponse rawResponse = readRawResponse(con);

		// Check response content type
		String contentType = rawResponse.getContentType();

		if (! options.isAllowedResponseContentType(contentType)) {

			String msg = null;

			if (contentType == null)
				msg = "Missing Content-Type header in the HTTP response";
			else
				msg = "Unexpected \"" + contentType + "\" content type of the HTTP response";

			throw new JSONRPC2SessionException(msg, JSONRPC2SessionException.UNEXPECTED_CONTENT_TYPE);
		}

		// Parse and return the response
		JSONRPC2Response response = null;

		try {
			response = JSONRPC2Response.parse(rawResponse.getContent(), 
					                  options.preservesParseOrder(), 
							  options.ignoresVersion(),
							  options.parsesNonStdAttributes());

		} catch (JSONRPC2ParseException e) {

			throw new JSONRPC2SessionException(
					"Invalid JSON-RPC 2.0 response",
					JSONRPC2SessionException.BAD_RESPONSE,
					e);
		}

		// Response ID must match the request ID, except for
		// -32700 (parse error), -32600 (invalid request) and 
		// -32603 (internal error)

		Object reqID = request.getID();
		Object resID = response.getID();

		if (reqID != null && resID !=null && reqID.toString().equals(resID.toString()) ) {
			// ok
		}
		else if (reqID == null && resID == null) {
			// ok
		}
		else if (! response.indicatesSuccess() && ( response.getError().getCode() == -32700 ||
				response.getError().getCode() == -32600 ||
				response.getError().getCode() == -32603    )) {
			// ok
		}
		else {
			throw new JSONRPC2SessionException(
					"Invalid JSON-RPC 2.0 response: ID mismatch: Returned " + 
					resID.toString() + ", expected " + reqID.toString(),
					JSONRPC2SessionException.BAD_RESPONSE);
		}

		return response;
	}


	/**
	 * Sends a JSON-RPC 2.0 notification using HTTP POST. Note that 
	 * contrary to requests, notifications produce no server response.
	 *
	 * @param notification The JSON-RPC 2.0 notification to send. Must not
	 *                     be {@code null}.
	 *
	 * @throws JSONRPC2SessionException On a network error.
	 */
	public void send(final JSONRPC2Notification notification)
		throws JSONRPC2SessionException {

		// Create and configure URL connection to server endpoint
		URLConnection con = createURLConnection();

		// Send notification encoded as JSON
		postString(con, notification.toString());

		// Get the response /for the inspector only/
		RawResponse rawResponse = readRawResponse(con);
	}
}


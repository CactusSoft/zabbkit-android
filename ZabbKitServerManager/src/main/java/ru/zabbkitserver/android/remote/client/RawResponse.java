package ru.zabbkitserver.android.remote.client;


import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.IOException;

import java.net.HttpURLConnection;

import java.util.List;
import java.util.Map;

import java.util.zip.GZIPInputStream;
import java.util.zip.Inflater;
import java.util.zip.InflaterInputStream;


/**
 * Represents the raw HTTP response to a JSON-RPC 2.0 request or notification. 
 * Can be used to retrieve the unparsed response content and headers.
 *
 * @since 1.6
 * @author Vladimir Dzhuvinov
 */
public class RawResponse {


	/**
	 * The HTTP response headers.
	 */
	private Map<String,List<String>> headers;
	
	
	/**
	 * The HTTP response code.
	 */
	private int statusCode;
	
	
	/**
	 * The HTTP response message.
	 */
	private String statusMessage;


	/**
	 * The content length.
	 */
	private int contentLength;


	/**
	 * The content type.
	 */
	private String contentType;


	/**
	 * The content encoding.
	 */
	private String contentEncoding;


	/**
	 * The raw HTTP response content.
	 */
	private String content;


	/**
	 * No public instantiation.
	 */
	private RawResponse() {}


	/**
	 * Parses the raw HTTP response from the specified URL connection.
	 * 
	 * @param connection The URL connection, must be in state completed and 
	 *                   not {@code null}.
	 * 
	 * @throws IOException If the response content couldn't be read.
	 */
	protected static RawResponse parse(final HttpURLConnection connection)
		throws IOException {

		// Check for HTTP compression
		String encoding = connection.getContentEncoding();

		InputStream is;

		if (encoding != null && encoding.equalsIgnoreCase("gzip"))
			is = new GZIPInputStream(connection.getInputStream());

		else if (encoding != null && encoding.equalsIgnoreCase("deflate"))
			is = new InflaterInputStream(connection.getInputStream(), new Inflater(true));

		else
			is = connection.getInputStream();


		// Read the response content
		StringBuilder responseText = new StringBuilder();

		BufferedReader input = new BufferedReader(new InputStreamReader(is, "UTF-8"));

		String line;

		while ((line = input.readLine()) != null) {
			responseText.append(line);
			responseText.append(System.getProperty("line.separator"));
		}

		input.close();

		RawResponse response = new RawResponse();

		response.content = responseText.toString();
		
		// Save HTTP code + message
		response.statusCode = connection.getResponseCode();
		response.statusMessage = connection.getResponseMessage();

		// Save headers
		response.headers = connection.getHeaderFields();

		response.contentLength = connection.getContentLength();
		response.contentType = connection.getContentType();
		response.contentEncoding = encoding;

		return response;
	}
	
	
	/**
	 * Gets the status code from the HTTP response message, e.g. 200 on 
	 * success.
	 * 
	 * @return The HTTP status code, or -1.
	 */
	public int getStatusCode() {
		
		return statusCode;
	}
	
	
	/**
	 * Gets the HTTP response status message, if any, returned along with 
	 * the status code from a server.
	 *  
	 * @return The HTTP status message, or {@code null}.
	 */
	public String getStatusMessage() {
		
		return statusMessage;
	}


	/**
	 * Returns the raw response content.
	 * 
	 * @return The raw content.
	 */
	public String getContent() {

		return content;
	}
	
	
	/**
	 * Returns an unmodifiable Map of the header fields. The Map keys are 
	 * Strings that represent the response header field names. Each Map 
	 * value is an unmodifiable List of Strings that represents the 
	 * corresponding field values.
	 * 
	 * @return A Map of the header fields.
	 */
	public Map<String,List<String>> getHeaderFields() {
		
		return headers;
	}


	/**
	 * Returns the value of the named header field. If called on a 
	 * connection that sets the same header multiple times with possibly 
	 * different values, only the last value is returned.
	 *  
	 * @param name The name of the header.
	 * 
	 * @return The value of the named header field, {@code null} if there 
	 *         is no such field in the header.
	 */
	public String getHeaderField(final String name) {

		List <String> values = headers.get(name);

		if (values == null | values.size() <= 0)
			return null;

		return values.get(0);
	}


	/**
	 * Returns the value of the "Content-Length" header field. 
	 * 
	 * @return The content length of the response, or -1 if the content 
	 *         length  is not known.
	 */
	public int getContentLength() {

		return contentLength;
	}


	/**
	 * Returns the value of the "Content-Type" header field.
	 * 
	 * @return The content type of the response, or {@code null} if not 
	 *         known.
	 */
	public String getContentType() {

		return contentType;
	}

	/**
	 * Returns the value of the "Content-Encoding" header field.
	 * 
	 * @return The content encoding of the response, or {@code null} if not 
	 *         known.
	 */
	public String getContentEncoding() {

		return contentEncoding;
	}
}

package ru.zabbkitserver.android.remote.client;


import java.net.Proxy;

import java.util.regex.Pattern;


/**
 * Optional settings for JSON-RPC 2.0 client sessions. The no-argument 
 * constructor specifies the default settings that the {@link JSONRPC2Session}
 * uses. To apply different ones instantiate a new settings instance, set the
 * desired ones to your liking, and then {@link JSONRPC2Session#setOptions pass} 
 * it to your {@link JSONRPC2Session} instance.
 * 
 * <p>Overview of the available session options:
 *
 * <ul>
 *     <li>Customise the "Content-Type" header in HTTP POST requests.
 *     <li>Set an "Origin" header in HTTP POST requests to simulate 
 *         Cross-Origin Resource Sharing (CORS) requests from a browser.
 *     <li>Accept HTTP cookies (if client sessions are established by this 
 *         mean instead of through the JSON-RPC protocol itself).
 *     <li>Customise the allowable "Content-Type" header values in HTTP POST
 *         responses.
 *     <li>Preserve parse order of JSON object members in JSON-RPC 2.0 response
 *         results (for human-facing clients, e.g. the JSON-RPC 2.0 Shell).
 *     <li>Ignore version 2.0 checks when parsing responses to allow client 
 *         sessions to older JSON-RPC (1.0) servers.
 *     <li>Parse non-standard attributes in JSON-RPC 2.0 responses.
 *     <li>Set an HTTP connect timeout.
 *     <li>Set an HTTP read timeout.
 *     <li>Set an HTTP proxy.
 *     <li>Enable HTTP response compression (using GZIP or DEFLATE content
 *         encoding).
 *     <li>Trust all X.509 server certificates (for HTTPS connections), 
 *         including self-signed.
 * </ul>
 *
 * @since 1.4
 * @author Vladimir Dzhuvinov
 */
public class JSONRPC2SessionOptions {
	
	
	/**
	 * The "Content-Type" (MIME) header value of HTTP POST requests. If
	 * {@code null} the header will not be set.
	 */
	private String requestContentType = DEFAULT_CONTENT_TYPE;
	
	
	/**
	 * The default "Content-Type" (MIME) header value of HTTP POST 
	 * requests. Set to {@code application/json}.
	 */
	public static final String DEFAULT_CONTENT_TYPE = "application/json";
	
	
	/**
	 * The allowed "Content-Type" (MIME) header values of HTTP responses. 
	 * If {@code null} any header value will be accepted.
	 */
	private String[] allowedResponseContentTypes = 
		DEFAULT_ALLOWED_RESPONSE_CONTENT_TYPES;
	
	
	/**
	 * The default allowed "Content-Type" (MIME) header values of HTTP
	 * responses. Set to {@code application/json} and {@code text/plain}.
	 */
	public static final String[] DEFAULT_ALLOWED_RESPONSE_CONTENT_TYPES =
		{"application/json", "text/plain"};
	 
	 
	/** 
	 * Optional CORS "Origin" header. If {@code null} the header will not 
	 * be set.
	 */
	private String origin = DEFAULT_ORIGIN;
	
	
	/**
	 * The default CORS "Origin" header value. Set to {@code null} (none).
	 */
	public static final String DEFAULT_ORIGIN = null;
	
	
	/**
	 * Specifies whether to accept HTTP cookies.
	 */
	private boolean acceptCookies = DEFAULT_ACCEPT_COOKIES;
	
	
	/**
	 * The default HTTP cookie acceptance policy.
	 */
	public static final boolean DEFAULT_ACCEPT_COOKIES = false;
	
	
	/**
	 * If {@code true} the order of parsed JSON object members must be
	 * preserved.
	 */
	private boolean preserveObjectMemberOrder = 
		DEFAULT_PRESERVE_OBJECT_MEMBER_ORDER;
	
	
	/**
	 * The default policy for preserving the order of parsed JSON object
	 * members. Set to {@code false} (no preserve).
	 */
	public static final boolean DEFAULT_PRESERVE_OBJECT_MEMBER_ORDER = false;
	
	
	/**
	 * If {@code true} version 2.0 checking of received responses must be
	 * disabled.
	 */
	private boolean ignoreVersion = DEFAULT_IGNORE_VERSION ;
	
	
	/**
	 * The default policy for version 2.0 checking. Set to {@code false}
	 * (strict checking).
	 */
	public static final boolean DEFAULT_IGNORE_VERSION = false;
	
	
	/**
	 * If {@code true} non-standard attributes appended to the JSON-RPC 2.0
	 * responses must be parsed too.
	 */
	private boolean parseNonStdAttributes = DEFAULT_PARSE_NON_STD_ATTRIBUTES;
	
	
	/**
	 * The default policy for parsing non-standard attributes in JSON-RPC 
	 * 2.0 messages. Set to {@code false} (non-standard attributes ignored).
	 */
	public static final boolean DEFAULT_PARSE_NON_STD_ATTRIBUTES = false;
	
	
	/**
	 * The HTTP connect timeout, in milliseconds. Zero implies the option 
	 * is disabled (timeout of infinity).
	 */
	private int connectTimeout = DEFAULT_CONNECT_TIMEOUT;
	
	
	/**
	 * The default HTTP connect timeout. Set to zero (disabled).
	 */
	public static final int DEFAULT_CONNECT_TIMEOUT = 0;
	
	
	/**
	 * The HTTP read timeout, in milliseconds. Zero implies the option is
	 * disabled (timeout of infinity).
	 */
	private int readTimeout = DEFAULT_READ_TIMEOUT;
	
	
	/**
	 * The default HTTP read timeout. Set to zero (disabled).
	 */
	public static final int DEFAULT_READ_TIMEOUT = 0;


	/**
	 * Optional HTTP proxy.
	 */
	private Proxy proxy = null;


	/**
	 * Enable / disable HTTP GZIP and DEFLATE compression.
	 */
	private boolean enableCompression = DEFAULT_ENABLE_COMPRESSION;


	/**
	 * The default HTTP GZIP and DEFLATE compression enable policy.
	 */
	public static final boolean DEFAULT_ENABLE_COMPRESSION = false;
	
	
	/**
	 * If {@code true} self-signed certificates presented by the JSON-RPC 
	 * 2.0 server must be accepted.
	 */
	private boolean trustAll = DEFAULT_TRUST_ALL;
	
	
	/**
	 * The default policy for trusting self-signed certificates. Set to
	 * {@code false} (self-signed certificates not accepted).
	 */
	public static final boolean DEFAULT_TRUST_ALL = false;


	/**
	 * Creates a new default JSON-RPC 2.0 client session options instance.
	 *
	 * <p>The "Content-Type" (MIME) header value of HTTP POST requests will
	 * be set to "application/json". To change it use 
	 * {@link #setRequestContentType}.
	 *
	 * <p>"Origin" HTTP headers will not be added. To add one use
	 * {@link #setOrigin}.
	 * 
	 * <p>HTTP cookies will be ignored. To accept cookies, e.g. for 
	 * browser-like session handling, use {@link #acceptCookies}.
	 *
	 * <p>The allowed HTTP response content types are set to 
	 * "application/json" and "text/plain". To change them use
	 * {@link #setAllowedResponseContentTypes}.
	 *
	 * <p>The parse order of JSON object members in JSON-RPC 2.0 response
	 * results will not be preserved. To change this behaviour use
	 * {@link #preserveParseOrder}.
	 *
	 * <p>Strict 2.0 version checking will be performed. To ignore the
	 * JSON-RPC version attribute use {@link #ignoreVersion(boolean)}.
	 *
	 * <p>HTTP connect timeouts will be disabled. To specify a value use
	 * {@link #setConnectTimeout}.
	 *
	 * <p>HTTP read timeouts will be disabled. To specify a value use
	 * {@link #setReadTimeout}.
	 *
	 * <p>No proxy is used. To specify one use {@link #setProxy}.
	 *
	 * <p>HTTP response compression (GZIP or DEFLATE) is disabled. To 
	 * enable it use {@link #enableCompression(boolean)}.
	 *
	 * <p>Self-signed X.509 certificates presented by the JSON-RPC 2.0
	 * server will not be accepted. To relax certificate cheking use
	 * {@link #trustAllCerts}.
	 */
	public JSONRPC2SessionOptions() {
	
		// check fields for default init values
	}
	
	
	/**
	 * Gets the value of the "Content-Type" (MIME) header for HTTP POST
	 * requests.
	 *
	 * @return The "Content-Type" (MIME) header value, {@code null} if the
	 *         header is not added to HTTP POST requests.
	 */
	public String getRequestContentType() {
	
		return requestContentType;
	}
	
	
	/**
	 * Sets the value of the HTTP "Content-Type" (MIME) header. Use this 
	 * method if you wish to change the default "application/json" content 
	 * type.
	 *
	 * @param contentType The value of the "Content-Type" (MIME) header
	 *                    in HTTP POST requests, {@code null} to suppress
	 *                    the header.
	 */
	public void setRequestContentType(final String contentType) {
	
		this.requestContentType = contentType;
	}
	
	
	/**
	 * Gets the value of the "Origin" HTTP header.
	 *
	 * <p>This header can be used to simulate Cross-Origin Resource Sharing
	 * (CORS) requests from a browser.
	 *
	 * @return The "Origin" header value, {@code null} if the header is not
	 *         added to HTTP requests.
	 */
	public String getOrigin() {
		
		return origin;
	}
	
	
	/**
	 * Sets the value of the "Origin" HTTP header.
	 *
	 * <p>This header can be used to simulate Cross-Origin Resource Sharing
	 * (CORS) requests from a browser.
	 *
	 * @param origin The value of the "Origin" header in HTTP requests, 
	 *               {@code null} to suppress the header.
	 */
	public void setOrigin(final String origin) {
	
		this.origin = origin;
	}
	
	
	/**
	 * Returns {@code true} if HTTP cookies are accepted, else 
	 * {@code false} if they are ignored.
	 *
	 * @return {@code true} if HTTP cookies are accepted, else 
	 *         {@code false}.
	 */
	public boolean acceptCookies() {
	
		return acceptCookies;
	}
	
	
	/**
	 * Specifies whether to accept HTTP cookies contained in the server 
	 * response. Some JSON-RPC servers may use cookies instead of tokens
	 * passed through the JSON-RPC protocol itself to establish client 
	 * sessions.
	 *
	 * @param acceptCookies {@code true} to accept HTTP cookies, else
	 *                      {@code false} to ignore them.
	 */
	public void acceptCookies(final boolean acceptCookies) {
	
		this.acceptCookies = acceptCookies;
	}
	
	
	/**
	 * Gets the allowed "Content-Type" (MIME) header values of HTTP 
	 * responses. 
	 *
	 * <p>The {@code JSONRPC2Session.send(...)} method will throw a
	 * {@link JSONRPC2SessionException#UNEXPECTED_CONTENT_TYPE} if the
	 * received HTTP response "Content-Type" (MIME) header value is not
	 * allowed.
	 *
	 * @return The allowed header values, if {@code null} any header value 
	 *         is allowed.
	 */
	public String[] getAllowedResponseContentTypes() {
	
		return allowedResponseContentTypes;
	}
	
	
	/**
	 * Sets the allowed "Content-Type" (MIME) header values of HTTP 
	 * responses.
	 *
	 * <p>The {@code JSONRPC2Session.send(...)} method will throw a 
	 * {@link JSONRPC2SessionException#UNEXPECTED_CONTENT_TYPE} if the
	 * received HTTP response "Content-Type" (MIME) header value is not
	 * allowed.
	 *
	 * @param contentTypes The allowed header values, {@code null} to allow
	 *                     any header value.
	 */
	public void setAllowedResponseContentTypes(final String[] contentTypes) {
	
		this.allowedResponseContentTypes = contentTypes;
	}
	
	
	/**
	 * Checks if the specified HTTP "Content-Type" (MIME) header value is 
	 * allowed.
	 *
	 * @param contentType The "Content-Type" (MIME) header value.
	 *
	 * @return {@code true} if the content type is allowed, else 
	 *         {@code false}.
	 */
	public boolean isAllowedResponseContentType(final String contentType) {
	
		// Allow any?
		if (allowedResponseContentTypes == null)
			return true;
		
		if (contentType == null)
			return false; // missing
		
		for (String t: allowedResponseContentTypes) {

			// Note: the content type may include optional parameters, 
			// which must be ignored during matching, e.g.
			// "application/json; charset=ISO-8859-1; ..."	
			if (contentType.matches("^" + Pattern.quote(t) + "(\\s|;|$)?.*"))
				return true;
		}
		
		return false; // nothing matched
	}
	
	
	/**
	 * Returns {@code true} if the member order of parsed JSON objects in
	 * JSON-RPC 2.0 response results is preserved.
	 *
	 * @return {@code true} if the parse order of JSON object members is
	 *         preserved, else {@code false}.
	 */
	public boolean preservesParseOrder() {
	
		return preserveObjectMemberOrder;
	}
	
	
	/**
	 * Controls the behaviour of the JSON parser when processing object
	 * members in JSON-RPC 2.0 response results. The default behaviour is
	 * to store the members in a {@code java.util.HashMap} in a 
	 * non-deterministic order. To preserve the original parse order pass a 
	 * boolean {@code true} to this method. Note that this will slow down 
	 * parsing and retrieval performance somewhat.
	 *
	 * @param preserve If {@code true} the parse order of JSON object 
	 *                 members will be preserved, else not.
	 */
	public void preserveParseOrder(final boolean preserve) {
	
		preserveObjectMemberOrder = preserve;
	}
	
	
	/**
	 * Returns {@code true} if strict parsing of received JSON-RPC 2.0
	 * responses is disabled and the "jsonrpc" version attribute is not 
	 * checked for "2.0" equality. Returns {@code false} if received 
	 * JSON-RPC 2.0 responses must strictly conform to the JSON-RPC 2.0 
	 * specification.
	 *
	 * @return {@code true} if the {@code "jsonrpc":"2.0"} version 
	 *         attribute is ignored, {@code false} if parsing is strict.
	 */
	public boolean ignoresVersion() {
	
		return ignoreVersion;
	}
	
	
	/**
	 * Controls the strictness of the JSON-RPC 2.0 response parser. The
	 * default behaviour is to check responses for strict compliance to
	 * the JSON-RPC 2.0 specification. By passing a boolean {@code true}
	 * parsing is relaxed and the "jsonrpc" version attribute will not be
	 * checked for "2.0" equality.
	 *
	 * @param ignore {@code true} to ignore the 2.0 {@code "jsonrpc":"2.0"} 
	 *               version attribute, {@code false} for strict parsing.
	 */
	public void ignoreVersion(final boolean ignore) {
	
		ignoreVersion = ignore;
	}
	
	
	/**
	 * Specifies whether to parse non-standard attributes found in JSON-RPC 
	 * 2.0 responses. 
	 * 
	 * @param enable {@code true} to parse non-standard attributes, else 
	 *               {@code false}.
	 */
	public void parseNonStdAttributes(final boolean enable) {
	
		parseNonStdAttributes = enable;
	}
	
	
	/**
	 * Returns {@code true} if non-standard attributes in JSON-RPC 2.0 
	 * responses are parsed. 
	 *
	 * @return {@code true} if non-standard attributes are parsed, else 
	 *         {@code false}.
	 */
	public boolean parsesNonStdAttributes() {
	
		return parseNonStdAttributes;
	}
	
	
	/**
	 * Sets the HTTP connect timeout.
	 *
	 * @since 1.8
	 *
	 * @param timeout The HTTP connect timeout, in milliseconds. Zero
	 *                implies the option is disabled (timeout of infinity).
	 */
	public void setConnectTimeout(final int timeout) {
	
		if (timeout < 0)
			throw new IllegalArgumentException("The HTTP connect timeout must be zero or positive");
		
		connectTimeout = timeout;
	}
	
	
	/**
	 * Gets the HTTP connect timeout.
	 *
	 * @since 1.8
	 *
	 * @return The HTTP connect timeout, in milliseconds. Zero implies the
	 *         option is disabled (timeout of infinity).
	 */
	public int getConnectTimeout() {
	
		return connectTimeout;
	}
	
	
	/**
	 * Sets the HTTP read timeout.
	 *
	 * @since 1.8
	 *
	 * @param timeout The HTTP read timeout, in milliseconds. Zero implies
	 *                the option is disabled (timeout of infinity).
	 */
	public void setReadTimeout(final int timeout) {
	
		if (timeout < 0)
			throw new IllegalArgumentException("The HTTP read timeout must be zero or positive");
		
		readTimeout = timeout;
	}
	
	
	/**
	 * Gets the HTTP read timeout.
	 *
	 * @since 1.8
	 *
	 * @return The HTTP read timeout, in milliseconds. Zero implies the 
	 *         option is disabled (timeout of infinity).
	 */
	public int getReadTimeout() {
	
		return readTimeout;
	}


	/**
	 * Sets an HTTP proxy.
	 *
	 * @since 1.10
	 *
	 * @param proxy The HTTP proxy to use, {@code null} if none.
	 */
	public void setProxy(final Proxy proxy) {

		this.proxy = proxy;
	}


	/**
	 * Gets the HTTP proxy.
	 *
	 * @since 1.10
	 *
	 * @return The HTTP proxy to use, {@code null} if none.
	 */
	public Proxy getProxy() {

		return proxy;
	}


	/**
	 * Enables or disables HTTP response compression using GZIP or DEFLATE
	 * content encoding. If compression is enabled but the HTTP server 
	 * doesn't support compression this setting will have no effect.
	 *
	 * @param enable If {@code true} HTTP compression will be enabled, 
	 *               else compression will be disabled.
	 */
	public void enableCompression(final boolean enable) {

		enableCompression = enable;
	}


	/**
	 * Checks if HTTP response compression using GZIP or DEFLATE content 
	 * encoding is enabled or disabled. If compression is enabled but the 
	 * HTTP server doesn't support compression this setting will have no 
	 * effect.
	 *
	 * @return {@code true} if HTTP compression is enabled, else 
	 *         {@code false}.
	 */
	public boolean enableCompression() {

		return enableCompression;
	}
	
	
	/**
	 * Controls checking of X.509 certificates presented by the server when
	 * establishing a secure HTTPS connection. The default behaviour is to 
	 * accept only certicates issued by a trusted certificate authority 
	 * (CA), as determined by the default Java trust store. By passing a
	 * boolean {@code false} this security check is disabled and all 
	 * certificates will be trusted, including self-signed ones. Use this
	 * for testing and development purposes only.
	 *
	 * @param trustAll If {@code true} all X.509 certificates presented by 
	 *                 the web server will be trusted, including self-signed
	 *                 ones. If {@code false} the default security policy
	 *                 will be restored.
	 */
	public void trustAllCerts(final boolean trustAll) {
	
		this.trustAll = trustAll;
	}
	
	
	/**
	 * Returns {@code true} if all X.509 certificates presented by the web
	 * server will be trusted, including self-signed ones. If {@code false} 
	 * the default security policy applies.
	 *
	 * @return {@code true} if all X.509 certificates are trusted, else
	 *         {@code false}.
	 */
	public boolean trustsAllCerts() {
	
		return trustAll;
	}
}

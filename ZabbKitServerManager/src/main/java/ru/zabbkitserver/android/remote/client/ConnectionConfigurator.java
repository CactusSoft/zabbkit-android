package ru.zabbkitserver.android.remote.client;


import java.net.HttpURLConnection;


/**
 * Interface allowing for additional configuration of HTTP URL connections, 
 * such as setting a custom header.
 *
 * @since 1.5
 * @author Vladimir Dzhuvinov
 */
public interface ConnectionConfigurator {


	/**
	 * Allows for additional configuration of the specified HTTP URL 
	 * connection before it is established. This may include setting a
	 * custom HTTP header, detc.
	 *
	 * @param connection The HTTP URL connection to configure. Must not be
	 *                   {@code null}.
	 */
	public void configure(final HttpURLConnection connection);

}

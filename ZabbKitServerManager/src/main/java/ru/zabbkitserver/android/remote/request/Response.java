package ru.zabbkitserver.android.remote.request;

import com.thetransactioncompany.jsonrpc2.JSONRPC2Response;

/**
 * POJO representation of RPC HTTP response.
 * 
 * @author Elena.Bukarova
 */
public class Response {

	public final JSONRPC2Response response;
	public final int id;
	public final Boolean isArray;

	/**
	 * Constructor
	 * 
	 * @param response - answer from server
	 * @param id - id of request
	 * @param isArray - type of answer (array or not) 
	 */
	public Response(JSONRPC2Response response, int id, Boolean isArray) {
		this.response = response;
		this.id = id;
		this.isArray = isArray;
	}

}

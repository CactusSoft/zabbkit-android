package ru.zabbkitserver.android.remote.model;

/**
 * Hosts model class
 * 
 * @author Elena.Bukarova
 * 
 */

public class Host extends HostBase{

	private String hostid;
	private String host;
	
	public Host(String hostid, String name, String host){
		super(name);
		this.hostid = hostid;
		this.host = host;		
	}

	public String getHostid() {
		return hostid;
	}

	public void setHostid(String hostid) {
		this.hostid = hostid;
	}	

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}			
}

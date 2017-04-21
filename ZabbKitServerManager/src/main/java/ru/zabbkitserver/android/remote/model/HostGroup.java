package ru.zabbkitserver.android.remote.model;

/**
 * Group of hosts model class
 * 
 * @author Elena.Bukarova
 * 
 */

public class HostGroup extends HostBase{

	private String groupid;
	private String internal;
	private String hosts;

	public HostGroup(String groupid, String name, String internal, String hosts) {
		super(name);
		this.groupid = groupid;		
		this.internal = internal;
		this.hosts = hosts;
	}

	public void setGroupid(String groupid) {
		this.groupid = groupid;
	}

	public void setInternal(String internal) {
		this.internal = internal;
	}

	public void setHosts(String hosts) {
		this.hosts = hosts;
	}

	public String getGroupid() {
		return groupid;
	}

	public String getInternal() {
		return internal;
	}

	public String getHosts() {
		return hosts;
	}

}

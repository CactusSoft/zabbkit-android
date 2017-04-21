package ru.zabbkitserver.android.remote.model;

public abstract class HostBase {
	
	private String name;
	
	public HostBase(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	

}

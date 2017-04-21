package ru.zabbkitserver.android.remote.model;

import java.util.ArrayList;

/**
 * Events model class
 * 
 * @author Elena.Bukarova
 * 
 */

public class Event {

	public static final int OBJECT_TRIGGER = 0;
	public static final int OBJECT_DISCOVERED_HOST = 1;
	public static final int OBJECT_DISCOVERED_SERVICE = 2;
	public static final int OBJECT_AUTO_REGISTERED_HOST = 3;
	
	private String eventid;
	private String clock;
	public ArrayList<Hosts> hosts;
	public ArrayList<Triggers> triggers;
	private String object;
	private String value;
	private String description;

	public Event(String eventid, String clock, ArrayList<Hosts> hosts, ArrayList<Triggers> triggers, String object,
			String value, String description) {
		this.eventid = eventid;
		this.clock = clock;				
		this.object = object;
		this.value = value;
		this.description = description;
		this.hosts = new ArrayList<Event.Hosts>();
		this.hosts.addAll(hosts);
		this.triggers = new ArrayList<Event.Triggers>();
		this.triggers.addAll(triggers);
	}
	
	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getEventid() {
		return eventid;
	}

	public void setEventid(String eventid) {
		this.eventid = eventid;
	}

	public String getClock() {
		return clock;
	}

	public void setClock(String clock) {
		this.clock = clock;
	}	

	public String getObject() {
		return object;
	}

	public void setObject(String object) {
		this.object = object;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}
	
	public class Hosts{
		public String host;
		public String hostid;			
	}
	public class Triggers{
		public String description;
		public String triggerid;
		public String priority;
	}
}

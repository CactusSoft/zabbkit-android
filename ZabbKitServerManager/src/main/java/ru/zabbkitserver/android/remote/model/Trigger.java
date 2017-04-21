package ru.zabbkitserver.android.remote.model;

import java.util.ArrayList;

/**
 * Triggers model class
 * 
 * @author Elena.Bukarova
 * 
 */

public class Trigger {

	private String value;
	private String priority;
	private String description;
	private String triggerid;
    private String url;
    private String comments;
	public ArrayList<Groups> groups;
	public ArrayList<Hosts> hosts;

	public Trigger(String value, String priority, String description, String triggerid, String url, String comments,
			ArrayList<Groups> groups, ArrayList<Hosts> hosts) {
		this.value = value;
		this.priority = priority;
		this.description = description;
        this.url = url;
        this.comments = comments;
		this.triggerid = triggerid;
		this.groups = new ArrayList<Trigger.Groups>();
		this.groups.addAll(groups);
		this.hosts = new ArrayList<Trigger.Hosts>();
		this.hosts.addAll(hosts);
	}

	public void setValue(String value) {
		this.value = value;
	}

	public void setPriority(String priority) {
		this.priority = priority;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public void setTriggerid(String triggerid) {
		this.triggerid = triggerid;
	}

	public String getValue() {
		return value;
	}

	public String getPriority() {
		return priority;
	}

	public String getDescription() {
		return description;
	}

	public String getTriggerid() {
		return triggerid;
	}

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }


    public class Groups {
		public String groupid;
		public String name;
	}

	public class Hosts {
		public String hostid;
		public String host;
	}
}

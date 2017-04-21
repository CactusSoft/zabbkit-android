package ru.zabbkit.android.db.entity;

public class BookmarkEntity {
	private int id;
	private int period;
	private String serverId;
	private String graphId;
	private String paramName;
	private String serverName;

	public BookmarkEntity() {
	}

	public BookmarkEntity(int id, String serverId, String graphId,
			String paramName, String serverName, int period) {
		this.id = id;
		this.serverId = serverId;
		this.graphId = graphId;
		this.paramName = paramName;
		this.serverName = serverName;
		this.period = period;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getPeriod() {
		return period;
	}

	public void setPeriod(int period) {
		this.period = period;
	}

	public String getServerId() {
		return serverId;
	}

	public void setServerId(String serverId) {
		this.serverId = serverId;
	}

	public String getGraphId() {
		return graphId;
	}

	public void setGraphId(String graphId) {
		this.graphId = graphId;
	}

	public String getParamName() {
		return paramName;
	}

	public void setParamName(String paramName) {
		this.paramName = paramName;
	}

	public String getServerName() {
		return serverName;
	}

	public void setServerName(String serverName) {
		this.serverName = serverName;
	}

	@Override
	public String toString() {
		return "BookmarkEntity [id=" + id + ", period=" + period
				+ ", serverId=" + serverId + ", graphId=" + graphId
				+ ", paramName=" + paramName + ", serverName=" + serverName
				+ "]";
	}
}

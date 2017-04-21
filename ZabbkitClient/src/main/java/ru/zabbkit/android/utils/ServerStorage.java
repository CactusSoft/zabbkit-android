package ru.zabbkit.android.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import ru.zabbkitserver.android.remote.model.Host;
import ru.zabbkitserver.android.remote.model.HostGroup;

public final class ServerStorage {

	private static final ServerStorage INSTANCE = new ServerStorage();

	private Map<String, String> servers;
	private Map<String, Map<String, String>> hosts;

	private ServerStorage() {
		servers = new TreeMap<String, String>();
		hosts = new TreeMap<String, Map<String, String>>();
	}

	public static ServerStorage getInstance() {
		return INSTANCE;
	}

	public void addServer(String serverId, String serverName) {
		servers.put(serverId, serverName);
	}

	public void addHost(Map<String, String> host, String serverId) {
		hosts.put(serverId, host);
	}

	public List<HostGroup> getServers() {
		List<HostGroup> itemList = new ArrayList<HostGroup>();
		for (Map.Entry<String, String> entry : servers.entrySet()) {
			HostGroup item = new HostGroup(entry.getKey(), entry.getValue(),
					"", "");
			itemList.add(item);
		}
		return itemList;
	}

	public List<Host> getHosts(String serverId) {
		List<Host> itemList = new ArrayList<Host>();
		if (hosts.containsKey(serverId)) {
			Map<String, String> hostValue = hosts.get(serverId);
			for (Map.Entry<String, String> entry : hostValue.entrySet()) {
				Host item = new Host(entry.getKey(), entry.getValue(), "");
				itemList.add(item);
			}
		}
		return itemList;
	}

	public void clearServer() {
		servers.clear();
	}

	public void clearHost() {
		hosts.clear();
	}
}

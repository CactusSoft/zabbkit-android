package ru.zabbkitserver.android.remote.model;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Item model class
 * 
 * @author Dmitry.Kalenchuk
 */
public class ZabbixItem {

	public static final int VALUE_TYPE_FLOAT = 0;
	public static final int VALUE_TYPE_CHAR = 1;
	public static final int VALUE_TYPE_LOG = 2;
	public static final int VALUE_TYPE_UINT = 3;
	public static final int VALUE_TYPE_TEXT = 4;

	public static final int DATA_TYPE_DECIMAL = 0;
	public static final int DATA_TYPE_OCTAL = 1;
	public static final int DATA_TYPE_HEXADECIMAL = 2;
	public static final int DATA_TYPE_BOOLEAN = 3;

	public static final int STATUS_ENABLED = 0;

	@SerializedName("itemid")
	private String itemId;

	@SerializedName("hostid")
	private String hostId;

	@SerializedName("interfaceid")
	private String interfaceId;

	@SerializedName("key_")
	private String key;

	@SerializedName("name")
	private String name;

	@SerializedName("type")
	private int type;

	@SerializedName("value_type")
	private int valueType;

	@SerializedName("data_type")
	private int dataType;

	@SerializedName("lastclock")
	private long lastClock;

	@SerializedName("lastvalue")
	private String lastValue;

	@SerializedName("hosts")
	private List<Host> hosts;

	@SerializedName("formula")
	private float formula;

	@SerializedName("units")
	private String units;

	@SerializedName("status")
	private int status;

	@SerializedName("graphs")
	private List<Graph> graphs;

	private String parsedValue;
	private String parsedName;

	public void setItemId(String itemId) {
		this.itemId = itemId;
	}

	public String getItemId() {
		return itemId;
	}

	public void setHostId(String hostId) {
		this.hostId = hostId;
	}

	public String getHostId() {
		return hostId;
	}

	public void setInterfaceId(String interfaceId) {
		this.interfaceId = interfaceId;
	}

	public String getInterfaceId() {
		return interfaceId;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public void setType(int type) {
		this.type = type;
	}

	public int getType() {
		return type;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public String getKey() {
		return key;
	}

	public void setLastClock(long lastClock) {
		this.lastClock = lastClock;
	}

	public long getLastClock() {
		return lastClock;
	}

	public void setLastValue(String value) {
		this.lastValue = value;
	}

	public String getLastValue() {
		return lastValue;
	}
	
	public String getParsedName() {
		return parsedName;
	}

	public void setParsedName(String parsedName) {
		this.parsedName = parsedName;
	}

	public void setValueType(int valueType) {
		this.valueType = valueType;
	}

	public int getValueType() {
		return valueType;
	}

	public void setDataType(int dataType) {
		this.dataType = dataType;
	}

	public int getDataType() {
		return dataType;
	}

	public void setHosts(ArrayList<Host> hosts) {
		this.hosts = hosts;
	}

	public List<Host> getHosts() {
		return hosts;
	}

	public void setFormula(float formula) {
		this.formula = formula;
	}

	public float getFormula() {
		return formula;
	}

	public void setUnits(String units) {
		this.units = units;
	}

	public String getUnits() {
		return units;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public int getStatus() {
		return status;
	}

	public List<Host> getHost() {
		return hosts;
	}

	public void setParsedValue(String parserValueString) {
		this.parsedValue = parserValueString;
	}

	public String getParsedValue() {
		return parsedValue;
	}

	public List<Graph> getGraphs() {
		return graphs==null ? Collections.EMPTY_LIST : graphs;
	}

	public void setGraphs(List<Graph> graphs) {
		this.graphs = graphs;
	}

}

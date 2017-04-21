package ru.zabbkit.android.app;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public final class Constants {

	public static final String STRING_ZERO = "0";
	public static final String STRING_ONE = "1";
	public static final String HOST_ID = "hostid";
	public static final String HOST_NAME = "hostName";

	public static final String DEFAULT_SERVER_REQUEST_ID = STRING_ONE;
	public static final String API_ADDRESS = "api_jsonrpc.php";
	public static final char HTTP_SYMBOL = '/';
    public static final String HTTP_POSTFIX = HTTP_SYMBOL + API_ADDRESS;
	public static final int MS_IN_SEC = 1000;
	public static final String DATE_FORMAT = "dd MMM yyyyy  HH:mm:ss";

    public static final String CERTIFICATE_FAIL = "No peer certificate";
    public static final String HTTP_AUTH_FAIL = "Authorization Required";

	// Push notifications
	// "http://vserver.inside.cactussoft.biz/zabbkit-test/api/devices";
	public static final String GCM_APP_SERVER_URL = "http://zabbkit.inside.cactussoft.biz/api/devices";
	public static final String SENDER_ID = "406145368298";

	// constants for SharedPreferences
	public static final String PREFS_NAME = "PrefsFile";
	public static final String PREFS_USER = "user";
	public static final String PREFS_PASSWORD = "password";
	public static final String PREFS_USER_DATA = "userData";
	public static final String PREFS_UPDATE_DATE_OVERVIEW = "updateDateOverview";
	public static final String PREFS_UPDATE_DATE_EVENTS = "updateDateEvents";
	public static final String PREFS_UPDATE_DATE_TRIGGERS = "updateDateTriggers";
	public static final String PREFS_HOST_NAME = HOST_NAME;
	public static final String PREFS_AUTH = "auth";
	public static final String PREFS_URL_FULL = "url_full";
	public static final String PREFS_URL_SHORTCUT = "url_shortcup";
	public static final String PREFS_IS_AUTHORIZED = "isAuthorized";
	public static final String PREFS_HOSTGROUP_ID = "hostgroupId";
	public static final String PREFS_HOST_ID = HOST_ID;
	public static final String PREFS_SERVER_NAME = "serverName";
    public static final String PREFS_HTTP_AUTH_LOGIN = "httpAuthLogin";
    public static final String PREFS_HTTP_AUTH_PASS = "httpAuthPass";
    public static final String PREFS_HTTP_AUTH_TYPE = "httpAuthType";

	// constants for graphs
	public static final String ZABBKIT_PICTURES_DIR = "ZabbKit";
	public static final String ZABBKIT_PICTURES_TMP_DIR = "temp";

	// constants for bookmarks
	public static final String PERIOD = "period";
	public static final String GRAPH_ID = "graphid";
	public static final String PARAM_NAME = "paramName";

	// constants for trigger value states
	public static final String STATE_OK = STRING_ZERO;
	public static final String STATE_PROBLEM = STRING_ONE;

	// constants for trigger priority states
	public static final int STATE_NOT_CLASSIFIED = 0;
	public static final int STATE_INFORMATION = 1;
	public static final int STATE_WARNING = 2;
	public static final int STATE_AVERAGE = 3;
	public static final int STATE_HIGH = 4;
	public static final int STATE_DISASTER = 5;

	public static final int STATES_COUNT = 6;

	// parameters name for requests
	public static final String REQ_OUTPUT = "output";
	public static final String REQ_MONITORED = "monitored";
	public static final String REQ_EXPAND_DESCRIPTION = "expandDescription";
	public static final String REQ_MONITORED_HOSTS = "monitored_hosts";
	public static final String REQ_GROUP_IDS = "groupids";
	public static final String REQ_HOST_IDS = "hostids";
	public static final String REQ_FILTER = "filter";
	public static final String REQ_TIME_FROM = "time_from";
	public static final String REQ_VALUE = "value";
	public static final String REQ_OBJECT = "object";
	public static final String REQ_SOURCE = "source";
	public static final String REQ_SELECT_TRIGGERS = "selectTriggers";
	public static final String REQ_SELECT_HOSTS = "selectHosts";
	public static final String REQ_SELECT_GRAPHS = "selectGraphs";
	public static final String REQ_TRIGGER_IDS = "triggerids";
	public static final String REQ_SORT_FIELD = "sortfield";
	public static final String REQ_EVENT_ID = "eventid";
	public static final String REQ_SORT_ORDER = "sortorder";
	public static final String REQ_DESC = "DESC";
	public static final String REQ_SELECT_GROUPS = "selectGroups";
	public static final String REQ_EVENTS_LIMIT = "limit";

	// parameters value for requests
	public static final int REQ_VAL_EVENTS_LIMIT = 1000;
	public static final String REQ_VAL_EXTEND = "extend";
	public static final String REQ_VAL_DESCRIPTION = "description";
	public static final String REQ_VAL_SOURCE = STRING_ZERO;
    public static final String REQ_VAL_OBJECT = STRING_ZERO;
	public static final List<Integer> REQ_VAL_VALUE = Collections
			.unmodifiableList(Arrays.asList(0, 1));
	public static final long REQ_VAL_EVENT_LOAD_PERIOD = 7 * 24 * 60 * 60
			* 1000;
	public static final String REQ_VAL_HOST_ID = HOST_ID;
	public static final String REQ_VAL_HOST = "host";
	public static final List<String> REQ_VAL_DATA = Collections
			.unmodifiableList(Arrays.asList("itemid", "hostid", "interfaceid",
					"key_", "name", "type", "value_type", "data_type",
					"lastclock", "lastvalue", "hosts", "formula", "units",
					"status"));

	// parameters for intent
	public static final String INT_TRIGGER_NAME = "trigger_name";
	public static final String INT_IS_HOST_GROUP = "isHostGroup";

	public static final String FLURRY_APP_KEY = "VD6HBCJGPCRFHHT39YDS";

	public static final String TRIGGER_TAB_TAG = "trigger_tab";
	public static final String DATA_TAB_TAG = "data_tab";
	public static final String EVENT_TAB_TAG = "event_tab";

	public static final String URL_PARAM = "url";
	public static final String COMMENTS_PARM = "comments";

	public static final String COOKIE_BASE_PATH = "/";
	public static final String COOKIE_AUTH_NAME = "zbx_sessionid";
	public static final Long COOKIE_LIFETIME_YEAR = 31557600000l;

	public static final String GRAPH_REQ_QUERY = "/chart2.php?graphid=%s&width=%d&height=%d&stime=%d&period=%d";

    public static final int HOST_SELECT_CODE = 345;
    public static final int LOGIN_CHANGE_CODE = 346;
    public static final String HOST_BUNDLE_URL = "hostUrl";
    public static final String HOST_BUNDLE_LOGIN = "hostLogin";
    public static final String HOST_BUNDLE_PASSWORD = "hostPassword";
    public static final String AUTO_LOGIN = "autoLogin";

    public static final String URL_CHECK_HTTP = "http://";
    public static final String URL_CHECK_HTTPS = "https://";

    public static final String UNKNOWN_ERROR = "An unknown error has occurred";
    public static final String REQUEST_TIMEOUT_MESSAGE = "Timeout expired";
    public static final Integer REQUEST_TIMEOUT = 15;

    public static final int DATA_REQUEST = 128;

    public static final String DATE_FORMAT_CERTIFICATE = "dd MMM yyyy hh:mm:ss";

	private Constants() {
	}
}

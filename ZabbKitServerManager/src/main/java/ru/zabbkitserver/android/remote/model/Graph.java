package ru.zabbkitserver.android.remote.model;

/**
 * Created with IntelliJ IDEA.
 * User: Eugene Avrukevich
 * Date: 10/10/13
 * Time: 1:58 PM
 */
public class Graph {

    private String graphid;

    public Graph(String graphId) {
        this.graphid = graphId;
    }

    public String getGraphId() {
        return graphid;
    }

    public void setGraphId(String graphId) {
        this.graphid = graphId;
    }

}

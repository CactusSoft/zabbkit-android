package ru.zabbkit.android.db.entity;

/**
 * Created by Alex.Shimborsky on 20/10/2014.
 */
public class HostEntity {

    private int id;
    private String url;
    private String name;
    private String login;
    private String password;
    private int ssl;

    public HostEntity(int id, String url, String name, String login,
                      String password, int ssl) {
        this.id = id;
        this.url = url;
        this.name = name;
        this.login = login;
        this.password = password;
        this.ssl = ssl;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public int getSsl() {
        return ssl;
    }

    public void setSsl(int ssl) {
        this.ssl = ssl;
    }

    @Override
    public String toString() {
        return "HostEntity [id=" + id + ", url=" + url
                + ", name=" + name + ", login=" + login
                + ", password=" + password + ", ssl=" + ssl
                + "]";
    }
}

package kry.assignment.servicepoller;

import java.util.Date;

public class Service {
    private String name;
    private String url;
    private Date created_at;
    private String last_status;

    public Service(String name, String url) {
        this(name, url, new Date(), "UNKNOWN");
    }

    public Service(String name, String url, Date created_at, String last_status) {
        this.name = name;
        this.url = url;
        this.created_at = created_at;
        this.last_status = last_status;
    }

    public String getName() {
        return name;
    }

    public String getUrl() {
        return url;
    }

    public Date getCreatedAt() {
        return created_at;
    }

    public String getStatus() {
        return last_status;
    }

    public void setStatus(String status) {
        this.last_status = status;
    }
}

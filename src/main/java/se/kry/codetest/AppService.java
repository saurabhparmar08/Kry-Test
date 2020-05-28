package se.kry.codetest;

import java.util.Objects;

public class AppService{

    private String name;
    private String url;
    private StatusEnum status = StatusEnum.UNKNOWN;
    private String creationDate;


    public AppService(String name, String url) {
        this.name = name;
        this.url = url;
    }
    public AppService(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public String getUrl() {
        return url;
    }

    public StatusEnum getStatus() {
        return status;
    }

    public void setStatus(StatusEnum status) {
        this.status = status;
    }

    public String getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(String creationDate) {
        this.creationDate = creationDate;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AppService service = (AppService) o;
        return Objects.equals(name, service.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }

}

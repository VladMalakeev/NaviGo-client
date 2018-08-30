package ga.navigo.model;

/**
 * Created by asus on 12.03.2018.
 */

public class CityInfo {
    private String name;
    private Double startLat;
    private Double startLon;
    private Double northLat;
    private Double eastLon;
    private Double southLat;
    private Double westLon;
    private Integer minZoom;
    private Integer maxZoom;

    public Double getEastLon() {
        return eastLon;
    }

    public void setEastLon(Double eastLon) {
        this.eastLon = eastLon;
    }

    public Double getSouthLat() {
        return southLat;
    }

    public void setSouthLat(Double southLat) {
        this.southLat = southLat;
    }



    public Double getWestLon() {
        return westLon;
    }

    public void setWestLon(Double westLon) {
        this.westLon = westLon;
    }



    public Integer getMinZoom() {
        return minZoom;
    }

    public void setMinZoom(Integer minZoom) {
        this.minZoom = minZoom;
    }

    public Double getNorthLat() {
        return northLat;
    }

    public void setNorthLat(Double northLat) {
        this.northLat = northLat;
    }

    public Integer getMaxZoom() {
        return maxZoom;
    }

    public void setMaxZoom(Integer maxZoom) {
        this.maxZoom = maxZoom;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Double getStartLon() {
        return startLon;
    }

    public void setStartLon(Double startLon) {
        this.startLon = startLon;
    }

    public Double getStartLat() {
        return startLat;
    }

    public void setStartLat(Double startLat) {
        this.startLat = startLat;
    }
}

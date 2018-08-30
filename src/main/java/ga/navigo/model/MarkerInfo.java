package ga.navigo.model;

/**
 * Created by ASUS 553 on 17.03.2018.
 */

public class MarkerInfo {
    private String name;
    private String type;
    private String lat;
    private String lon;
    private String descript;
    private String image;


    public String getName(){return name;}
    public String getLatitude(){return lat;}
    public String getLongetude(){return lon;}
    public String getType(){return type;}
    public String getDescription(){return descript;}
    public String getImage(){return image;}

    public void setName(String name){this.name = name;}
    public void setType(String type){this.type = type;}
    public void setLatitude(String latitude){this.lat = latitude;}
    public void setLongetude(String longetude){this.lon = longetude;}
    public void setDescription(String description){this.descript = description;}
    public void setImage(String image){this.image = image;}
}

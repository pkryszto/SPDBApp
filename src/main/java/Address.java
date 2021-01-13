import org.jxmapviewer.viewer.GeoPosition;

public class Address {

    public final GeoPosition location;
    public String name;


    public Address(GeoPosition location, String name) {
        this.location = location;
        this.name = name;

    }

    @Override
    public String toString() {
        return "Address{" +
                "geolocation=" + location +
                ", name='" + name + '\'' +
                '}';
    }

    public GeoPosition getLocation() {
        return location;
    }

    public String getName() {
        return name;
    }

}

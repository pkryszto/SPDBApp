import org.jxmapviewer.viewer.GeoPosition;

public class Address {

    public final GeoPosition location;
    public String name;
    public String key;

    public Address(GeoPosition location, String name) {
        this.location = location;
        this.name = name;
        this.key = name+", " + String.format("%.2f", location.getLatitude()) + ", " + String.format("%.2f", location.getLongitude());

    }

    @Override
    public String toString() {
        return "Address{" +
                "geolocation=" + location +
                ", name='" + name + '\'' +
                ", key='" + key + '\'' +
                '}';
    }

    public GeoPosition getLocation() {
        return location;
    }

    public String getName() {
        return name;
    }

    public String getKey() {return key;}

    public String decodeNameFromKey(String str) {
        return str.split(", ")[0];
    }
}

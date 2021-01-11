import org.jxmapviewer.viewer.GeoPosition;

import java.util.ArrayList;

public class Poi {

    public final GeoPosition location;
    public String name;


    public Poi(GeoPosition location,String name ) {
        this.location = location;
        this.name = name;

    }

    @Override
    public String toString() {
        return "Poi{" +
                "location=" + location +
                ", name='" + name + '\'' +
                '}';
    }
}

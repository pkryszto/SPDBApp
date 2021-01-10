import org.jxmapviewer.viewer.GeoPosition;

import java.util.ArrayList;

public class Route {
    public final ArrayList<ArrayList<GeoPosition>> route;
    public double distance;
    public int time;

    public Route(ArrayList<ArrayList<GeoPosition>> route,double distance,int time ) {
        this.route = route;
        this.distance = distance;
        this.time = time;
    }
}
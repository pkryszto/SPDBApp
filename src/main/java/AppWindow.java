import javax.swing.*;
import javax.swing.event.MouseInputListener;

import org.jxmapviewer.JXMapViewer;
import org.jxmapviewer.OSMTileFactoryInfo;
import org.jxmapviewer.input.CenterMapListener;
import org.jxmapviewer.input.PanKeyListener;
import org.jxmapviewer.input.PanMouseInputListener;
import org.jxmapviewer.input.ZoomMouseWheelListenerCenter;
import org.jxmapviewer.painter.CompoundPainter;
import org.jxmapviewer.painter.Painter;
import org.jxmapviewer.viewer.DefaultTileFactory;
import org.jxmapviewer.viewer.DefaultWaypoint;
import org.jxmapviewer.viewer.GeoPosition;
import org.jxmapviewer.viewer.TileFactoryInfo;
import org.jxmapviewer.viewer.Waypoint;
import org.jxmapviewer.viewer.WaypointPainter;

import java.awt.event.*;
import java.sql.ResultSet;
import java.util.*;

import static java.lang.Integer.parseInt;

public class AppWindow extends JFrame{
    private JPanel mainJPanel;
    private JPanel infoPanel;
    private JTextField fromTextField;
    private JComboBox choosePOIComboBox;
    private JLabel fromLabel;
    private JLabel toLabel;
    private JTextField toTextField;
    private JLabel maxDistanceLabel;
    private JTextField maxDistanceTextField;
    private JLabel maxDelayLabel;
    private JTextField maxDelayTextField;
    private JLabel distancePOILabel;
    private JTextField distancePOITextField;
    private JLabel timePOILabel;
    private JTextField timePOITextField;
    private JTextField minDistanceToFinishTextField;
    private JTextField minTimeToFinishTextField;
    private JButton findRouteButton;
    private JXMapViewer mapViewer;
    private JTextArea textArea1;
    private JTextArea distanceText;
    private JTextArea timeText;
    private JPopupMenu mapPopupMenu;
    private JMenuItem startPointItem;
    private JMenuItem endPointItem;

    ArrayList<Waypoint> listOfPoints;
    DefaultWaypoint startPoint;
    DefaultWaypoint endPoint;
    java.awt.Point mapPoint;

    private int distanceOfRoute;
    private int timeOfRoute;


    public AppWindow()
    {
        add(mainJPanel);
        setTitle("SPDB Application");
        setSize(800,600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        //setResizable(false);
        mapViewer.setSize(400,300);
        mainJPanel.setSize(600,600);
        infoPanel.setSize(200, 600);

        initialize();

        listOfPoints = new ArrayList<Waypoint>();
    }

    private void initialize()
    {
        initializeMapViewer();
        initializePopupMenu();
        initializeCategories();
        initializeTextFields();
        initializeButton();
    }

    private void initializeMapViewer()
    {
        MouseInputListener mia = new PanMouseInputListener(mapViewer);
        mapViewer.addMouseListener(mia);
        mapViewer.addMouseMotionListener(mia);
        mapViewer.addMouseListener(new CenterMapListener(mapViewer));
        mapViewer.addMouseWheelListener(new ZoomMouseWheelListenerCenter(mapViewer));
        mapViewer.addKeyListener(new PanKeyListener(mapViewer));

        // Create a TileFactoryInfo for OpenStreetMap
        TileFactoryInfo info = new OSMTileFactoryInfo();
        DefaultTileFactory tileFactory = new DefaultTileFactory(info);
        mapViewer.setTileFactory(tileFactory);

        GeoPosition warsaw = new GeoPosition(51.13, 21);
        GeoPosition plock = new GeoPosition(52.32, 19.42);
        GeoPosition nowyDwor = new GeoPosition(52.26, 20.43);

        // Create a track from the geo-positions
        List<GeoPosition> track = Arrays.asList(warsaw, plock, nowyDwor);

        // Set the focus
        mapViewer.zoomToBestFit(new HashSet<GeoPosition>(track), 0.7);
    }

    private void initializePopupMenu()
    {
        mapPopupMenu = new JPopupMenu();
        startPointItem = new JMenuItem("Set as starting point");
        endPointItem = new JMenuItem("Set as end point");
        mapPopupMenu.add(startPointItem);
        mapPopupMenu.add(endPointItem);

        mapViewer.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if(SwingUtilities.isRightMouseButton(e))
                {
                    mapPoint = e.getPoint();
                    mapPopupMenu.show(mapViewer, e.getX(), e.getY());
                }
            }
        });

        startPointItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                GeoPosition geo = mapViewer.convertPointToGeoPosition(mapPoint);
                setStartPoint(new DefaultWaypoint(geo));
            }
        });

        endPointItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                GeoPosition geo = mapViewer.convertPointToGeoPosition(mapPoint);
                setEndPoint(new DefaultWaypoint(geo));
            }
        });
    }

    private void initializeTextFields()
    {
        allowOnlyNumbers(maxDistanceTextField);
        allowOnlyNumbers(maxDelayTextField);
        allowOnlyNumbers(minDistanceToFinishTextField);
        allowOnlyNumbers(minTimeToFinishTextField);
        allowOnlyNumbers(distancePOITextField);
        allowOnlyNumbers(timePOITextField);
    }

    private void initializeCategories()
    {
        ArrayList<String> categories = QueryExecuter.getPOICategories();

        for(String cat : categories)
        {
            choosePOIComboBox.addItem(cat);
        }
    }

    private void initializeButton()
    {
        findRouteButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(startPoint == null || endPoint == null) return;

                ArrayList<ArrayList<GeoPosition>> routes = findPOIs();
                CompoundPainter<JXMapViewer> painter = createPainters(routes);

                mapViewer.setOverlayPainter(painter);
            }
        });
    }

    private void allowOnlyNumbers(JTextField t)
    {
        t.addKeyListener(new KeyAdapter() {
            public void keyTyped(KeyEvent e) {
                char c = e.getKeyChar();
                if (!((c >= '0') && (c <= '9') ||
                        (c == KeyEvent.VK_BACK_SPACE) ||
                        (c == KeyEvent.VK_DELETE))) {
                    getToolkit().beep();
                    e.consume();
                }
            }
        });
    }

    private void addPointOnMap(GeoPosition position)
    {
        DefaultWaypoint toAdd = new DefaultWaypoint(position);

        listOfPoints.add(toAdd);
        // Create a waypoint painter that takes all the waypoints
        WaypointPainter<Waypoint> waypointPainter = new WaypointPainter<Waypoint>();
        HashSet<Waypoint> hList = new HashSet<Waypoint>(listOfPoints);
        waypointPainter.setWaypoints(hList);

        // Create a compound painter that uses both the route-painter and the waypoint-painter
        List<Painter<JXMapViewer>> painters = new ArrayList<Painter<JXMapViewer>>();
        //painters.add(routePainter);
        painters.add(waypointPainter);

        CompoundPainter<JXMapViewer> painter = new CompoundPainter<JXMapViewer>(painters);
        mapViewer.setOverlayPainter(painter);
    }

    private void setStartPoint(DefaultWaypoint point)
    {
        if(startPoint != null)
        {
            for(int i = 0; i < listOfPoints.size(); i++)
            {
                if(listOfPoints.get(i).getPosition() == startPoint.getPosition())
                {
                    listOfPoints.remove(i);
                    break;
                }
            }
        }
        startPoint = point;
        fromTextField.setText(startPoint.getPosition().toString());
        addPointOnMap(point.getPosition());
        displaySimpleRoute();
    }

    private void setEndPoint(DefaultWaypoint point)
    {
        if(endPoint != null)
        {
            for(int i = 0; i < listOfPoints.size(); i++)
            {
                if(listOfPoints.get(i).getPosition() == endPoint.getPosition())
                {
                    listOfPoints.remove(i);
                    break;
                }
            }
        }
        endPoint = point;
        toTextField.setText(endPoint.getPosition().toString());
        addPointOnMap(point.getPosition());
        displaySimpleRoute();
    }

    private void drawRoute(ArrayList<GeoPosition> points)
    {
        RoutePainter routePainter = new RoutePainter(points);
        List<Painter<JXMapViewer>> painters = new ArrayList<Painter<JXMapViewer>>();
        painters.add(routePainter);

        WaypointPainter<Waypoint> waypointPainter = new WaypointPainter<Waypoint>();
        HashSet<Waypoint> hList = new HashSet<Waypoint>(listOfPoints);
        waypointPainter.setWaypoints(hList);
        painters.add(waypointPainter);

        CompoundPainter<JXMapViewer> painter = new CompoundPainter<JXMapViewer>(painters);
        mapViewer.setOverlayPainter(painter);
    }

    private void displaySimpleRoute()
    {
        if(startPoint == null || endPoint == null) return;
        ArrayList<GeoPosition> points = new ArrayList<GeoPosition>();
        points.add(startPoint.getPosition());
        points.add(endPoint.getPosition());
        points = findRoute(points);
        drawRoute(points);

        updateDistanceText(getDistanceOfRoute(points));
        updateTimeText(getTimeOfRoute(points));
    }

    private ArrayList<GeoPosition> findRoute(ArrayList<GeoPosition> points)
    {
        return QueryExecuter.findRoute(points);
    }

    private int getDistanceOfRoute(ArrayList<GeoPosition> points)
    {
        return QueryExecuter.getDistanceOfRoute(points);
    }

    private int getTimeOfRoute(ArrayList<GeoPosition> points)
    {
        return QueryExecuter.getTimeOfRoute(points);
    }

    private void updateDistanceText(int km)
    {
        distanceText.setText("Distance: " + km + "km");
        distanceOfRoute = km;
    }

    private void updateTimeText(int mins)
    {
        timeText.setText("Time: " + convertMinutesToTime(mins));
        timeOfRoute = mins;
    }

    private int getPOIDistance()
    {
        if(distancePOITextField.getText().isEmpty()) return -1;
        return parseInt(distancePOITextField.getText());
    }

    private int getPOITime()
    {
        if(timePOITextField.getText().isEmpty()) return -1;
        return parseInt(timePOITextField.getText());
    }

    private int getMaxDistance()
    {
        if(maxDistanceTextField.getText().isEmpty()) return -1;
        return parseInt(maxDistanceTextField.getText());
    }

    private int getMaxTime()
    {
        if(maxDelayTextField.getText().isEmpty()) return -1;
        return parseInt(maxDelayTextField.getText());
    }

    private int getMinDistance()
    {
        if(minDistanceToFinishTextField.getText().isEmpty()) return -1;
        return parseInt(minDistanceToFinishTextField.getText());
    }

    private int getMinTime()
    {
        if(minTimeToFinishTextField.getText().isEmpty()) return -1;
        return parseInt(minTimeToFinishTextField.getText());
    }


    private int computePOINumber()
    {
        int distanceValue = getPOIDistance();
        int timeValue = getPOITime();

        int distanceNumber = distanceOfRoute / distanceValue;
        if (distanceOfRoute % distanceValue == 0) distanceNumber--;
        int timeNumber = timeOfRoute / timeValue;
        if (timeOfRoute % timeValue == 0) timeNumber--;

        if(distanceValue == -1) return timeNumber;
        else if(timeValue == -1) return distanceNumber;

        return distanceNumber < timeNumber ? distanceNumber : timeNumber;
    }

    private ArrayList<ArrayList<GeoPosition>> findPOIs()
    {
        int POInumber = computePOINumber();
        int maxDistance = getMaxDistance();
        int maxTime = getMaxTime();
        int distancePOI = getPOIDistance();
        int timePOI = getPOITime();
        int minDistance = getMinDistance();
        int minTime = getMinTime();
        ArrayList<GeoPosition> points = new ArrayList<GeoPosition>(Arrays.asList(startPoint.getPosition(), endPoint.getPosition()));

        return QueryExecuter.findPOIs(POInumber, points, maxDistance, maxTime, distancePOI, timePOI, minDistance, minTime);
    }

    private CompoundPainter<JXMapViewer> createPainters(ArrayList<ArrayList<GeoPosition>> routes)
    {
        List<Painter<JXMapViewer>> painters = new ArrayList<Painter<JXMapViewer>>();
        for (ArrayList<GeoPosition> list : routes) addPainter(painters, list);

        CompoundPainter<JXMapViewer> painter = new CompoundPainter<JXMapViewer>(painters);
        return painter;
    }

    private void addPainter(List<Painter<JXMapViewer>> painters, ArrayList<GeoPosition> points)
    {
        RoutePainter routePainter = new RoutePainter(points);
        painters.add(routePainter);

        ArrayList<Waypoint> waypoints = new ArrayList<Waypoint>();
        for(GeoPosition geo : points) waypoints.add(new DefaultWaypoint(geo));

        WaypointPainter<Waypoint> waypointPainter = new WaypointPainter<Waypoint>();
        HashSet<Waypoint> hList = new HashSet<Waypoint>(waypoints);
        waypointPainter.setWaypoints(hList);
        painters.add(waypointPainter);
    }

    private String convertMinutesToTime(int minutes)
    {
        int hours = minutes / 60;
        int mins = minutes % 60;
        if(hours == 0) return ""+mins+"min";
        return ""+hours+"h "+mins+"min";
    }

}

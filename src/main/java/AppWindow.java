import javax.lang.model.type.NullType;
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

import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.sql.SQLException;
import java.util.*;
import java.util.List;

import static java.lang.Integer.parseInt;

public class AppWindow extends JFrame {
    private JPanel mainJPanel;
    private JPanel infoPanel;
    private JTextField fromTextField;
    private JComboBox choosePOIComboBox;
    private JLabel fromLabel;
    private JLabel toLabel;
    private JTextField toTextField;
    private JTextField minDistanceFromStartTextField;
    private JTextField minTimeFromStartTextField;
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
    private JTextArea minDistanceFromStartText;
    private JTextArea minTimeFromStartText;
    private JTextArea nameApp;
    private JTextArea minTimeToFinishLabel;
    private JButton fromSearchBtn;
    private JButton toSearchBtn;
    private JPopupMenu mapPopupMenu;
    private JMenuItem startPointItem;
    private JMenuItem endPointItem;
    private Font f1_sans;
    private Font f2_sans_bold;
    private Font f3_sans_small;

    ArrayList<Waypoint> listOfPoints;
    DefaultWaypoint startPoint;
    DefaultWaypoint endPoint;
    java.awt.Point mapPoint;

    private int distanceOfRoute;
    private int timeOfRoute;


    private QueryExecuter queryExecuter;

    public AppWindow(QueryExecuter queryExecuter) {
        add(mainJPanel);
        setTitle("SPDB Application");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        //setResizable(false);
        mapViewer.setSize(500, 600);
        mainJPanel.setSize(800, 600);
        infoPanel.setSize(300, 600);
        f1_sans  = new Font(Font.SANS_SERIF, Font.PLAIN,  12);
        f2_sans_bold  = new Font(Font.SANS_SERIF, Font.BOLD,  12);
        f3_sans_small  = new Font(Font.SANS_SERIF, Font.PLAIN,  9);

        infoPanel.setMaximumSize(new Dimension(300, -1));
        setMinimumSize(new Dimension(400, 600));
        this.addComponentListener(new ComponentAdapter(){
            public void componentResized(ComponentEvent e){
                Dimension d= getSize();
                Dimension minD = getMinimumSize();
                if(d.width<minD.width)
                    d.width=minD.width;
                if(d.height<minD.height)
                    d.height=minD.height;
                setSize(d);
            }
        });

        initialize();

        listOfPoints = new ArrayList<Waypoint>();

        this.queryExecuter = queryExecuter;
    }

    private void initialize() {
        initializeMapViewer();
        initializePopupMenu();
        initializeCategories();
        initializeTextFields();
        initializeButton();
        initializeFont();
        initializeSearchButtons();
    }

    private void initializeMapViewer() {
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

    private void initializePopupMenu() {
        mapPopupMenu = new JPopupMenu();
        startPointItem = new JMenuItem("Set as starting point");
        endPointItem = new JMenuItem("Set as end point");
        mapPopupMenu.add(startPointItem);
        mapPopupMenu.add(endPointItem);

        mapViewer.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (SwingUtilities.isRightMouseButton(e)) {
                    mapPoint = e.getPoint();
                    mapPopupMenu.show(mapViewer, e.getX(), e.getY());
                }
            }
        });

        startPointItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                GeoPosition geo = mapViewer.convertPointToGeoPosition(mapPoint);
                try {
                    setStartPoint(new DefaultWaypoint(geo));
                } catch (SQLException throwables) {
                    throwables.printStackTrace();
                }
            }
        });

        endPointItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                GeoPosition geo = mapViewer.convertPointToGeoPosition(mapPoint);
                try {
                    setEndPoint(new DefaultWaypoint(geo));
                } catch (SQLException throwables) {
                    throwables.printStackTrace();
                }
            }
        });
    }

    private void initializeTextFields() {
        allowOnlyNumbers(minDistanceFromStartTextField);
        allowOnlyNumbers(minTimeFromStartTextField);
        allowOnlyNumbers(minDistanceToFinishTextField);
        allowOnlyNumbers(minTimeToFinishTextField);
        allowOnlyNumbers(distancePOITextField);
        allowOnlyNumbers(timePOITextField);
    }

    private void initializeCategories() {
        ArrayList<String> categories = queryExecuter.getPOICategories();

        for (String cat : categories) {
            choosePOIComboBox.addItem(cat);
        }
    }

    private void initializeButton() {
        findRouteButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (startPoint == null || endPoint == null) return;

                ArrayList<Poi> pois = null;
                ArrayList<Route> routes = null;

                try {
                    pois = findPOIs();
                    routes = findRouteForPOIs(pois);
                    drawRouteAndPois(pois, routes);
                    updateTimeAndDistance(routes);
                } catch (SQLException throwables) {
                    throwables.printStackTrace();
                    try {
                        displaySimpleRoute();
                    } catch (SQLException sqlException) {
                        sqlException.printStackTrace();
                    }
                }

                //CompoundPainter<JXMapViewer> painter = createPainters(routes);

                //mapViewer.setOverlayPainter(painter);
            }
        });
    }

    private void allowOnlyNumbers(JTextField t) {
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

    private void addPointOnMap(GeoPosition position) {
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

    private void setStartPoint(DefaultWaypoint point) throws SQLException {
        if (startPoint != null) {
            for (int i = 0; i < listOfPoints.size(); i++) {
                if (listOfPoints.get(i).getPosition() == startPoint.getPosition()) {
                    listOfPoints.remove(i);
                    break;
                }
            }
        }
        startPoint = point;
        fromTextField.setText(getCity(startPoint.getPosition()));
        addPointOnMap(point.getPosition());
        displaySimpleRoute();
    }

    private void setEndPoint(DefaultWaypoint point) throws SQLException {
        if (endPoint != null) {
            for (int i = 0; i < listOfPoints.size(); i++) {
                if (listOfPoints.get(i).getPosition() == endPoint.getPosition()) {
                    listOfPoints.remove(i);
                    break;
                }
            }
        }
        endPoint = point;
        toTextField.setText(getCity(endPoint.getPosition()));
        addPointOnMap(point.getPosition());
        displaySimpleRoute();
    }

    private void drawRoute(ArrayList<GeoPosition> points) {
        RoutePainter routePainter = new RoutePainter(points, 0);
        List<Painter<JXMapViewer>> painters = new ArrayList<Painter<JXMapViewer>>();
        painters.add(routePainter);

        WaypointPainter<Waypoint> waypointPainter = new WaypointPainter<Waypoint>();
        HashSet<Waypoint> hList = new HashSet<Waypoint>(listOfPoints);
        waypointPainter.setWaypoints(hList);
        painters.add(waypointPainter);

        CompoundPainter<JXMapViewer> painter = new CompoundPainter<JXMapViewer>(painters);
        mapViewer.setOverlayPainter(painter);
    }

    private void drawRouteInParts(ArrayList<ArrayList<GeoPosition>> points) {
        List<Painter<JXMapViewer>> painters = new ArrayList<Painter<JXMapViewer>>();

        for (ArrayList<GeoPosition> route : points) {
            RoutePainter routePainter = new RoutePainter(route,0);
            painters.add(routePainter);
        }

        WaypointPainter<Waypoint> waypointPainter = new WaypointPainter<Waypoint>();
        HashSet<Waypoint> hList = new HashSet<Waypoint>(listOfPoints);
        waypointPainter.setWaypoints(hList);
        painters.add(waypointPainter);

        CompoundPainter<JXMapViewer> painter = new CompoundPainter<JXMapViewer>(painters);
        mapViewer.setOverlayPainter(painter);
    }

    private void displaySimpleRoute() throws SQLException {
        if (startPoint == null || endPoint == null) return;
        ArrayList<GeoPosition> points = new ArrayList<GeoPosition>();
        points.add(startPoint.getPosition());
        points.add(endPoint.getPosition());

        //DO TESTÓW 1337
        queryExecuter.setSessionNumber(1337);
        //queryExecuter.setSessionNumber((int) (Math.random() * 99999));
        Route route = findRoute(points);
        drawRouteInParts(route.route);

        updateDistanceText((int) route.distance);
        updateTimeText(route.time);
    }

    private Route findRoute(ArrayList<GeoPosition> points) throws SQLException {
        return queryExecuter.findRoute(points);
    }

    private void updateDistanceText(int km) {
        distanceText.setText("Distance: " + km + "km");
        distanceOfRoute = km;
    }

    private void updateTimeText(int mins) {
        timeText.setText("Time: " + convertMinutesToTime(mins));
        timeOfRoute = mins;
    }

    private int getPOIDistance() {
        if (distancePOITextField.getText().isEmpty()) return -1;
        return parseInt(distancePOITextField.getText());
    }

    private int getPOITime() {
        if (timePOITextField.getText().isEmpty()) return -1;
        return parseInt(timePOITextField.getText());
    }

    private int getMinDistanceFromStart() {
        if (minDistanceFromStartTextField.getText().isEmpty()) return -1;
        return parseInt(minDistanceFromStartTextField.getText());
    }

    private int getMinTimeFromStart() {
        if (minTimeFromStartTextField.getText().isEmpty()) return -1;
        return parseInt(minTimeFromStartTextField.getText());
    }

    private int getMinDistance() {
        if (minDistanceToFinishTextField.getText().isEmpty()) return -1;
        return parseInt(minDistanceToFinishTextField.getText());
    }

    private int getMinTime() {
        if (minTimeToFinishTextField.getText().isEmpty()) return -1;
        return parseInt(minTimeToFinishTextField.getText());
    }

    private String getPOICategory() {
        if (choosePOIComboBox.getSelectedItem().toString().equals("Choose POI category")) return "-1";
        return choosePOIComboBox.getSelectedItem().toString();
    }


    private int computePOINumber() {
        int distanceValue = getPOIDistance();
        int timeValue = getPOITime();

        int distanceNumber = distanceOfRoute / distanceValue;
        if (distanceOfRoute % distanceValue == 0) distanceNumber--;
        int timeNumber = timeOfRoute / timeValue;
        if (timeOfRoute % timeValue == 0) timeNumber--;

        if (distanceValue == -1) return timeNumber;
        else if (timeValue == -1) return distanceNumber;

        return distanceNumber < timeNumber ? distanceNumber : timeNumber;
    }

    private ArrayList<Poi> findPOIs() throws SQLException {
        int minDistanceFromStart = getMinDistanceFromStart();
        int minTimeFromStart = getMinTimeFromStart();
        int distancePOI = getPOIDistance();
        int timePOI = getPOITime();
        int minDistance = getMinDistance();
        int minTime = getMinTime();
        String POICategory = getPOICategory();

        ArrayList<Poi> pois = queryExecuter.findPOIs(minDistance, minTime, distancePOI, minDistanceFromStart, timePOI, minTimeFromStart, POICategory);

        return pois;
    }

    private ArrayList<Route> findRouteForPOIs(ArrayList<Poi> pois) throws SQLException
    {
        ArrayList<GeoPosition> points = new ArrayList<GeoPosition>();

        points.add(startPoint.getPosition());
        for (Poi poi : pois){
            points.add(poi.location);
        }
        points.add(endPoint.getPosition());

        ArrayList<Route> finalRoute = new ArrayList<>();

        for (int i = 0; i< points.size()-1;i++){
            ArrayList<GeoPosition> tempPoints = new ArrayList<GeoPosition>();
            tempPoints.add(points.get(i));
            tempPoints.add(points.get(i+1));
            finalRoute.add(queryExecuter.findFinalRoute(tempPoints));
        }

        return finalRoute;
    }

    private CompoundPainter<JXMapViewer> createPaintersFromList(ArrayList<Route> routes)
    {
        System.out.println(routes.size() +" SIZE");
        List<Painter<JXMapViewer>> painters = new ArrayList<Painter<JXMapViewer>>();
        List<Painter<JXMapViewer>> temp =  new ArrayList<Painter<JXMapViewer>>();
        int i = 0;

        for(Route route : routes)
        {
            for (ArrayList<GeoPosition> path : route.route)
            {
                RoutePainter routePainter = new RoutePainter(path, i);
                painters.add(routePainter);
            }
            i = (i+1) % 8;
        }

        CompoundPainter<JXMapViewer> painter = new CompoundPainter<JXMapViewer>(painters);
        return painter;
    }

    private List<Painter<JXMapViewer>> createPainters(ArrayList<ArrayList<GeoPosition>> routes) {
        List<Painter<JXMapViewer>> painters = new ArrayList<Painter<JXMapViewer>>();
        for (ArrayList<GeoPosition> list : routes) addPainter(painters, list);
        return painters;
    }

    private void addPainter(List<Painter<JXMapViewer>> painters, ArrayList<GeoPosition> points) {
        ArrayList<GeoPosition> route = new ArrayList<GeoPosition>();
        RoutePainter routePainter = new RoutePainter(route, 0);
        painters.add(routePainter);
    }

    private void addPOIPainter(CompoundPainter<JXMapViewer> painters,List<Poi> pois)
    {
        HashSet<Waypoint> hList = new HashSet<Waypoint>();

        for(Poi poi : pois)
        {
            DefaultWaypoint toAdd = new DefaultWaypoint(poi.location);
            hList.add(toAdd);
        }

        WaypointPainter<Waypoint> waypointPainter = new WaypointPainter<Waypoint>();
        waypointPainter.setWaypoints(hList);
        painters.addPainter(waypointPainter);
    }

    private String convertMinutesToTime(int minutes) {
        int hours = minutes / 60;
        int mins = minutes % 60;
        if (hours == 0) return "" + mins + "min";
        return "" + hours + "h " + mins + "min";
    }

    private void drawRouteAndPois(ArrayList<Poi> pois, ArrayList<Route> route)
    {
        CompoundPainter painter = createPaintersFromList(route);

        addPOIPainter(painter, pois);

        WaypointPainter<Waypoint> waypointPainter = new WaypointPainter<Waypoint>();
        HashSet<Waypoint> hList = new HashSet<Waypoint>(listOfPoints);
        waypointPainter.setWaypoints(hList);

        painter.addPainter(waypointPainter);

        mapViewer.setOverlayPainter(painter);
    }

    private void updateTimeAndDistance(ArrayList<Route> route)
    {
        int distance = 0;
        int time = 0;

        for(Route path : route)
        {
            distance += path.distance;
            time += path.time;
        }

        updateDistanceText((int) distance);
        updateTimeText(time);
    }

    private void showError()
    {
        JOptionPane.showMessageDialog(mainJPanel, "Route doesn't exist", "Error", JOptionPane.PLAIN_MESSAGE);
    }

    private String getCity(GeoPosition geo) throws SQLException {
        return queryExecuter.getCityName(geo);
    }

    private void initializeFont() {
        fromLabel.setFont(f1_sans);
        toLabel.setFont(f1_sans);
        distancePOILabel.setFont(f2_sans_bold);
        timePOILabel.setFont(f2_sans_bold);
        textArea1.setFont(f1_sans);
        distanceText.setFont(f2_sans_bold);
        timeText.setFont(f2_sans_bold);
        minDistanceFromStartText.setFont(f1_sans);
        minTimeFromStartText.setFont(f1_sans);
        nameApp.setFont(f3_sans_small);
        minTimeToFinishLabel.setFont(f1_sans);
    }

    private void initializeSearchButtons() {
        fromSearchBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (fromTextField.getText().equals("")) return;

                ArrayList<Address> addresses = null;

                try {
                    addresses = findAddresses(getFromTextField());
                } catch (SQLException throwables) {
                    throwables.printStackTrace();
                    return;
                }

                if (addresses == null || addresses.isEmpty())
                    return;

                ArrayList<String> possAddresses = new ArrayList<String>();
                for (Address tmp : addresses) {
                    possAddresses.add(tmp.getKey());
                }
                Object[] possibilities = possAddresses.toArray();
                Image image = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
                String chosenName = (String)JOptionPane.showInputDialog(
                        null,
                        "Choose location:",
                        "From location",
                        JOptionPane.PLAIN_MESSAGE,
                        new ImageIcon(image),
                        possibilities,
                        "");
                for (Address tmp : addresses) {
                    if (tmp.getKey().equals(chosenName)) {
                        DefaultWaypoint newStartPoint = new DefaultWaypoint(tmp.getLocation());
                        try {
                            setStartPoint(newStartPoint);
                        } catch (SQLException throwables) {
                            throwables.printStackTrace();
                        }
                        break;
                    }
                }
            }
        });

        toSearchBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (fromTextField.getText().equals("")) return;

                ArrayList<Address> addresses = null;

                try {
                    addresses = findAddresses(getToTextField());
                } catch (SQLException throwables) {
                    throwables.printStackTrace();
                    return;
                }

                if (addresses == null || addresses.isEmpty())
                    return;

                ArrayList<String> possAddresses = new ArrayList<String>();
                for (Address tmp : addresses) {
                    possAddresses.add(tmp.getKey());
                }
                Object[] possibilities = possAddresses.toArray();
                Image image = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
                String chosenName = (String)JOptionPane.showInputDialog(
                        null,
                        "Choose location:",
                        "From location",
                        JOptionPane.PLAIN_MESSAGE,
                        new ImageIcon(image),
                        possibilities,
                        "");
                for (Address tmp : addresses) {
                    if (tmp.getKey().equals(chosenName)) {
                        DefaultWaypoint newEndPoint = new DefaultWaypoint(tmp.getLocation());
                        try {
                            setEndPoint(newEndPoint);
                        } catch (SQLException throwables) {
                            throwables.printStackTrace();
                        }
                        break;
                    }
                }
            }
        });
    }

    private ArrayList<Address> findAddresses(String addressName) throws SQLException {
        ArrayList<Address> addresses = null;
        try {
            addresses = queryExecuter.findAddresses(addressName);
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return addresses;
    }

    public String getFromTextField() {
        return fromTextField.getText();
    }

    public String getToTextField() {
        return toTextField.getText();
    }
}

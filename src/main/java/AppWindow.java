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
import java.lang.constant.Constable;
import java.util.*;

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

        listOfPoints = new ArrayList<Waypoint>();
        initializeMapViewer();
        initializePopupMenu();
        initializeCategories();
        initializeTextFields();

        GeoPosition frankfurt = new GeoPosition(50,  7, 0, 8, 41, 0);
        GeoPosition wiesbaden = new GeoPosition(50,  5, 0, 8, 14, 0);
        GeoPosition mainz     = new GeoPosition(50,  0, 0, 8, 16, 0);
        GeoPosition darmstadt = new GeoPosition(49, 52, 0, 8, 39, 0);
        GeoPosition offenbach = new GeoPosition(50,  6, 0, 8, 46, 0);


        // Create a track from the geo-positions
        List<GeoPosition> track = Arrays.asList(frankfurt, wiesbaden, mainz, darmstadt, offenbach);

        // Set the focus
        mapViewer.zoomToBestFit(new HashSet<GeoPosition>(track), 0.7);
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
        return points;
    }

    private String getDistanceOfRoute(ArrayList<GeoPosition> points)
    {
        return "2137km";
    }

    private String getTimeOfRoute(ArrayList<GeoPosition> points)
    {
        return "21h 37min";
    }

    private void updateDistanceText(String text)
    {
        distanceText.setText("Distance: " + text);
    }

    private void updateTimeText(String text)
    {
        timeText.setText("Time: " + text);
    }

    private void initializeCategories()
    {
        String categories[] = {
                "Night club",
                "Restaurant",
                "Gas station"
        };

        for(String cat : categories)
        {
            choosePOIComboBox.addItem(cat);
        }
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

    private void initializeTextFields()
    {
        allowOnlyNumbers(maxDistanceTextField);
        allowOnlyNumbers(maxDelayTextField);
        allowOnlyNumbers(minDistanceToFinishTextField);
        allowOnlyNumbers(minTimeToFinishTextField);
        allowOnlyNumbers(distancePOITextField);
        allowOnlyNumbers(timePOITextField);
    }

}

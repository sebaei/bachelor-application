package com.vogella.maven.quickstart;

import com.vogella.maven.quickstart.OutputCSV;
import java.awt.Color;
import java.io.Writer;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Date;
import java.util.ArrayList;
import java.util.List;
import java.awt.Dimension;
import java.awt.event.MouseMotionListener;
import java.awt.geom.Point2D;
import java.sql.Timestamp;
import java.util.Calendar;
import java.awt.event.MouseEvent;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.Rectangle;

import javax.swing.*;

import org.jxmapviewer.JXMapKit;
import org.jxmapviewer.JXMapViewer;
import org.jxmapviewer.OSMTileFactoryInfo;
import org.jxmapviewer.viewer.DefaultTileFactory;
import org.jxmapviewer.viewer.GeoPosition;
import org.jxmapviewer.viewer.TileFactoryInfo;
 

public class TrackMouse extends JPanel implements MouseMotionListener {
    Tr blankArea;
    JTextArea textArea;
    final static JXMapKit jXMapKit = new JXMapKit();
    
    static final String NEWLINE = System.getProperty("line.separator");
     
    public static void main(String[] args) throws Exception {
    	
    	
        TileFactoryInfo info = new OSMTileFactoryInfo();
        DefaultTileFactory tileFactory = new DefaultTileFactory(info);
        jXMapKit.setTileFactory(tileFactory);

        // GeoPostion of center of Europe
        GeoPosition eu = new GeoPosition(54.5260, 15.2551); 

        final JToolTip tooltip = new JToolTip();
        tooltip.setTipText("Center of Europe");
        tooltip.setComponent(jXMapKit.getMainMap());
        jXMapKit.getMainMap().add(tooltip);

        jXMapKit.setZoom(14);
        jXMapKit.setAddressLocation(eu);
        
        jXMapKit.getMainMap().addMouseMotionListener(new MouseMotionListener() {
            @Override
            public void mouseDragged(MouseEvent e) { 
                
            }
            
            @Override
            public void mouseMoved(MouseEvent e)
            {
               
                JXMapViewer map = jXMapKit.getMainMap();
                
            	
                // convert to world bitmap
                Point2D worldPos = map.getTileFactory().geoToPixel(eu, map.getZoom());

                Rectangle rect = map.getViewportBounds();
                int sx = (int) worldPos.getX() - rect.x;
                int sy = (int) worldPos.getY() - rect.y;
                Point screenPos = new Point(sx, sy);
                

                if (screenPos.distance(e.getPoint()) < 20)
                {
                    screenPos.x -= tooltip.getWidth() / 2;

                    tooltip.setLocation(screenPos);
                    tooltip.setVisible(true);
                }
                else
                {
                    tooltip.setVisible(false);
                }
               
            }
            
        });
        try {
            UIManager.setLookAndFeel("javax.swing.plaf.metal.MetalLookAndFeel");
        } catch (UnsupportedLookAndFeelException ex) {
            ex.printStackTrace();
        } catch (IllegalAccessException ex) {
            ex.printStackTrace();
        } catch (InstantiationException ex) {
 
            ex.printStackTrace();
        } catch (ClassNotFoundException ex) {
            ex.printStackTrace();
        }
        UIManager.put("swing.boldMetal", Boolean.FALSE);
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                createAndShowGUI();
            }
        });
        
    }
     
    private static void createAndShowGUI() {
        JFrame frame = new JFrame("Europe Map");
        //frame.setLayout(new GridLayout(2, 0));
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        JComponent newContentPane = new TrackMouse();
        newContentPane.setOpaque(false); 
        frame.add(newContentPane);
        //frame.getContentPane().add(jXMapKit);
        frame.pack();
        frame.setVisible(true);
    }
     
    public TrackMouse() {
        super(new GridLayout(2,1));
		/*
		 * blankArea = new Tr(Color.BLUE,jXMapKit);
		 * jXMapKit.addMouseMotionListener(this);
		 * blankArea.addMouseMotionListener(this);
		 */
        add(jXMapKit);
        textArea = new JTextArea();
        textArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(textArea,
                JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setPreferredSize(new Dimension(75, 75));
        add(scrollPane);
        
        addMouseMotionListener(this);
        setPreferredSize(new Dimension(1000, 700));
        setBorder(BorderFactory.createEmptyBorder(0,10,0,10));
    }
    
     
    void eventOutput(String eventDescription, MouseEvent e)  {
    	Calendar calendar = Calendar.getInstance();
    	long timeMilSec = new Date().getTime();
    	calendar.setTimeInMillis(timeMilSec);long lastSec = 0;
    	Timestamp currentTimestamp = new java.sql.Timestamp(calendar.getTime().getTime());
    	float distance = ( ( e.getX()*e.getX()) + (e.getY()*e.getY()) ) /2;
    	float actualspeed = distance/timeMilSec;
        textArea.append(eventDescription
                + "(" + e.getX() + "," + e.getY() + ")"
                + " detected on "
                + currentTimestamp + "  " + timeMilSec + "  " + distance + "  " + actualspeed
                + NEWLINE);
        textArea.setCaretPosition(textArea.getDocument().getLength());
    }
    	
    	
    
    void toCSV(String eventDescription, MouseEvent e) throws Exception {
    	Calendar calendar = Calendar.getInstance();
    	Timestamp currentTimestamp = new java.sql.Timestamp(calendar.getTime().getTime());
    	String csvFile = "C:\\Users\\Lenovo\\Desktop\\eclipse\\CSV\\output.csv";
        FileWriter writer = new FileWriter(csvFile,true);
        writer.append( "[" + "(" + e.getX() + ")" + "(" +e.getY() +")" + "]" + "," + currentTimestamp + '\n');
		
        
        
        /*
		 * writer.append(eventDescription + "," + "[" + "(" + e.getX() + ")" + "(" +
		 * e.getY() +")" + "]" + "," + " detected on " + "," + currentTimestamp + "," +
		 * /n);
		 */
		/*
		 * List<String[]> datalines = new ArrayList<>(); datalines.add(new String[]
		 * {"Mouse moved to "," [" + "(" + e.getX() + ")" + "(" + e.getY() +")" + "]",
		 * " detected on ", "" + currentTimestamp + "" } );
		 * 
		 * for (String[] t : datalines) { List<String> list = new ArrayList<>();
		 * list.add("Mouse moved to "); list.add(" [" + "(" + e.getX() + ")" + "(" +
		 * e.getY() +")" + "]"); list.add(" detected on "); list.add("" +
		 * currentTimestamp + "" );
		 */
        
		/*
		 * writer.append(eventDescription + "(" + e.getX() + "," + e.getY() + ")" +
		 * " detected on " //+ e.getComponent().getClass().getName() + currentTimestamp
		 * + NEWLINE);
		 */
   
        writer.flush();
        writer.close();
    }
    

     
    public void mouseMoved(MouseEvent e) {
			try {
				toCSV("Mouse moved to ", e);
			} catch (Exception e1) {
				e1.printStackTrace();
			}
			eventOutput("Mouse moved to ", e);
    }
			
				
			
			
    
     
    public void mouseDragged(MouseEvent e) {
			eventOutput("Mouse dragged to ", e);
	
    }
}

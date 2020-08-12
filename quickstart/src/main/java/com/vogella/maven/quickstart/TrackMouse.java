package com.vogella.maven.quickstart;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.geom.Point2D;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Date;

import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.SimpleScriptContext;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JToolTip;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import org.jxmapviewer.JXMapKit;
import org.jxmapviewer.JXMapViewer;
import org.jxmapviewer.OSMTileFactoryInfo;
import org.jxmapviewer.viewer.DefaultTileFactory;
import org.jxmapviewer.viewer.GeoPosition;
import org.jxmapviewer.viewer.TileFactoryInfo;

public class TrackMouse extends JPanel implements MouseMotionListener {
	Tr blankArea;
	static String Direction;
	static JTextArea textArea;
	static double startX;
	static double startY;
	static Timestamp starttime;
	static boolean start;
	static int zoomlevel = 13;
	final static JXMapKit jXMapKit = new JXMapKit();
	static double g = 0.0;
	static double b = 0.0;
	static double c = 0.0;
	static double d = 0.0;
	// GeoPostion of center of Europe
	static GeoPosition eu = new GeoPosition(54.5260, 15.2551);
	static double my = eu.getLatitude();
	static double mx = eu.getLongitude();

	static final String NEWLINE = System.getProperty("line.separator");

	public static void main(String[] args) throws Exception {

		TileFactoryInfo info = new OSMTileFactoryInfo();
		DefaultTileFactory tileFactory = new DefaultTileFactory(info);
		jXMapKit.setTileFactory(tileFactory);
		final JToolTip tooltip = new JToolTip();
		tooltip.setTipText("Center of Europe");
		tooltip.setComponent(jXMapKit.getMainMap());
		jXMapKit.getMainMap().add(tooltip);
		jXMapKit.setZoom(zoomlevel);
		jXMapKit.setAddressLocation(eu);

		/*
		 * OutputStream outStream = p.getOutputStream(); BufferedWriter writer = new
		 * BufferedWriter(new OutputStreamWriter(outStream));
		 */
		jXMapKit.getMainMap().addMouseMotionListener(new MouseMotionListener() {
			@Override
			public void mouseDragged(MouseEvent e) {

			}

			@Override
			public void mouseMoved(MouseEvent e) {
				Calendar calendar = Calendar.getInstance();
				long timeMilSec = new Date().getTime();
				calendar.setTimeInMillis(timeMilSec);
				long seconds = timeMilSec / 1000;
				if (!start) {
					start = !start;
					startX = e.getX();
					startY = e.getY();
					starttime = new java.sql.Timestamp(calendar.getTime().getTime());
					;
				} else {
					double endX = e.getX();
					double endY = e.getY();
					double diffX = Math.pow((endX - startX), 2);
					double diffY = Math.pow((endY - startY), 2);
					if (diffX > diffY) {
						if ((endX - startX) > 0) {
							Direction = "Right";
						}
						if ((endX - startX) < 0) {
							Direction = "Left";
						}
					}

					if (diffY > diffX) {
						if ((endY - startY) > 0) {
							Direction = "Down";
						}
						if ((endY - startY) < 0) {
							Direction = "Up";
						}
					}
					double distance = Math.pow(Math.pow((endX - startX), 2) + Math.pow((endY - startY), 2), 0.5);
					if (distance > 125) {
						Timestamp endtime = new java.sql.Timestamp(calendar.getTime().getTime());
						long actualtime = endtime.getTime() - starttime.getTime();
						
						double speed = (distance / actualtime)*1000;
						speed = speed*1.7;

						// Call Python Script
						try {

							  System.out.println(Direction);//Print user's mouse movement direction
							  
							  
							  
							  double i = 0.0;
							 
							ProcessBuilder pb = new ProcessBuilder("python", "module1.py", 490 + "", actualtime + "",
									1 + "", 1 + "", distance + "", speed + "");
							pb.directory(new File(
									"C:\\Users\\Lenovo\\Documents\\Visual Studio 2017\\Backup Files\\ispeed-Project-master\\"));
							Process p = pb.start();
							BufferedReader stdInput = new BufferedReader(new InputStreamReader(p.getInputStream()));
							BufferedReader stdError = new BufferedReader(new InputStreamReader(p.getErrorStream()));

							// Read the output from the command
							String s = null;
							while ((s = stdInput.readLine()) != null) {
							
								System.out.println("The user is currently moving with speed:");
								System.out.println(s);
								char sp = s.charAt(1);
								int a = Integer.parseInt(sp+"");
								
								switch(a) {
								  case 0:
									  
									    if (Direction == "Up") {
									    	
									    	g = g + 1.0;
									    	i = 9.0; //* g);
									    	my = my + i;
									    	GeoPosition eur = new GeoPosition(my, mx);
									    	jXMapKit.setAddressLocation(eur);
											/*
											 * System.out.println(g); System.out.println(i);
											 */
									    	System.out.println(my);
											System.out.println(mx);
										}
										if (Direction == "Right") {
											
											b = b + 1.0;
											i = 9.0; // * b);
											mx = mx + i;
											GeoPosition eur = new GeoPosition(my, mx);
											jXMapKit.setAddressLocation(eur);
											/*
											 * System.out.println(b); System.out.println(i);
											 */
											System.out.println(my);
											System.out.println(mx);
										}
										if (Direction == "Down") {
											
											c = c + 1.0;
											i = 9.0; // * c);
											my = my - i;
											GeoPosition eur = new GeoPosition(my, mx);
											jXMapKit.setAddressLocation(eur);
											/*
											 * System.out.println(c); System.out.println(i);
											 */
											System.out.println(my);
											System.out.println(mx);
											
										}
										if (Direction == "Left") {
											
											d = d + 1.0;
											i = 9.0; // * d);
											mx = mx - i;
											GeoPosition eur = new GeoPosition(my, mx);
											jXMapKit.setAddressLocation(eur);
											/*
											 * System.out.println(d); System.out.println(i);
											 */
											System.out.println(my);
											System.out.println(mx);
										}
										
								    break;
								  case 1:
										
										if (Direction == "Up") {
											zoomlevel = zoomlevel-1;
										}
										if (Direction == "Right") {
											zoomlevel = zoomlevel-1;
										}
										if (Direction == "Down") {
											zoomlevel = zoomlevel+1;
										}
										if (Direction == "Left") {
											zoomlevel = zoomlevel+1;
										}
										
									    jXMapKit.setZoom(zoomlevel);
								    break;
								  case 3:
										
										if (Direction == "Up") {
											zoomlevel = zoomlevel-2;
										}
										if (Direction == "Right") {
											zoomlevel = zoomlevel-2;
										}
										if (Direction == "Down") {
											zoomlevel = zoomlevel+2;
										}
										if (Direction == "Left") {
											zoomlevel = zoomlevel+2;
										}
										
										jXMapKit.setZoom(zoomlevel);
									break;
								  
								}
							}
							// Read any errors from the attempted command
							while ((s = stdError.readLine()) != null) {
								System.out.println("Here is the standard error of the command (if any):");
								System.out.println(s);
							}
							
							// Process p = Runtime.getRuntime().exec("python
							// C:\\Users\\Lenovo\\Documents\\Visual Studio 2017\\Backup
							// Files\\ispeed-Project-master");
						} catch (IOException e1) {
							e1.printStackTrace();
						}

						starttime = endtime;
						startX = endX;
						startY = endY;
						
	

					}

				}

				JXMapViewer map = jXMapKit.getMainMap();
				// eventOutput("Mouse moved to ", e);
				// convert to world bitmap
				Point2D worldPos = map.getTileFactory().geoToPixel(eu, map.getZoom());

				Rectangle rect = map.getViewportBounds();
				int sx = (int) worldPos.getX() - rect.x;
				
				int sy = (int) worldPos.getY() - rect.y;
				
				Point screenPos = new Point(sx, sy);

				if (screenPos.distance(e.getPoint()) < 20) {
					screenPos.x -= tooltip.getWidth() / 2;

					tooltip.setLocation(screenPos);
					tooltip.setVisible(true);
				} else {
					tooltip.setVisible(false);
				}

			}

			private String resolvePythonScriptPath(String string) {
				// TODO Auto-generated method stub
				return null;
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
		frame.setSize(1400, 700);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		//JComponent newContentPane = new TrackMouse();
		//newContentPane.setOpaque(false);
		//frame.add(newContentPane);
		frame.add(jXMapKit);
		frame.setVisible(true);
	}

	
	/*
	 * public TrackMouse() {
	 * 
	 * super(new GridLayout(2,1)); blankArea = new Tr(Color.BLUE);
	 * jXMapKit.addMouseMotionListener(this);
	 * blankArea.addMouseMotionListener(this);
	 * 
	 * add(jXMapKit); textArea = new JTextArea(); textArea.setEditable(false);
	 * JScrollPane scrollPane = new JScrollPane(textArea,
	 * JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
	 * JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED); scrollPane.setPreferredSize(new
	 * Dimension(75, 75)); add(scrollPane);
	 * 
	 * addMouseMotionListener(this); setPreferredSize(new Dimension(1000, 700));
	 * setBorder(BorderFactory.createEmptyBorder(0,10,0,10)); }
	 */
	 

	@Override
	public void mouseDragged(MouseEvent arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mouseMoved(MouseEvent arg0) {
		// TODO Auto-generated method stub

	}

	
	/*
	 * static void eventOutput(String eventDescription, MouseEvent e) { Calendar
	 * calendar = Calendar.getInstance(); long timeMilSec = new Date().getTime();
	 * calendar.setTimeInMillis(timeMilSec);long lastSec = 0; Timestamp
	 * currentTimestamp = new java.sql.Timestamp(calendar.getTime().getTime());
	 * float distance = ( ( e.getX()*e.getX()) + (e.getY()*e.getY()) ) /2; float
	 * actualspeed = distance/timeMilSec; textArea.append(eventDescription + "(" +
	 * e.getX() + "," + e.getY() + ")" + " detected on " + currentTimestamp + "  " +
	 * timeMilSec + "  " + distance + "  " + actualspeed + NEWLINE);
	 * textArea.setCaretPosition(textArea.getDocument().getLength()); }
	 */

	
	/*
	 * public void mouseMoved(MouseEvent e) {
	 * 
	 * eventOutput("Mouse moved to ", e);
	 * 
	 * }
	 * 
	 * public void mouseDragged(MouseEvent e) { eventOutput("Mouse dragged to ", e);
	 * }
	 */
	 
}
/*
 * System.out.println("SX " + startX + NEWLINE + "SY " + startY + NEWLINE +
 * "EX " + endX + NEWLINE + "EY " + endY + NEWLINE + "Distance " + distance +
 * NEWLINE + "Actualtime " + actualtime + NEWLINE + "Speed " + speed);
 * System.out.println(Direction);
 */
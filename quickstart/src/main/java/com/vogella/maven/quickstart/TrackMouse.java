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
import java.sql.Timestamp;
import java.util.Calendar;
import java.awt.event.MouseEvent;
import java.awt.GridLayout;
 
import javax.swing.*;
 

public class TrackMouse extends JPanel implements MouseMotionListener {
    Tr blankArea;
    JTextArea textArea;
    
    static final String NEWLINE = System.getProperty("line.separator");
     
    public static void main(String[] args) throws Exception {
     
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
       
        JFrame frame = new JFrame("MouseTracking");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        JComponent newContentPane = new TrackMouse();
        newContentPane.setOpaque(true); 
        frame.setContentPane(newContentPane);
        frame.pack();
        frame.setVisible(true);
    }
     
    public TrackMouse() {
        super(new GridLayout(0,1));
        blankArea = new Tr(Color.YELLOW);
        add(blankArea);   
        textArea = new JTextArea();
        textArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(textArea,
                JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setPreferredSize(new Dimension(200, 75));
        add(scrollPane);
        blankArea.addMouseMotionListener(this);
        addMouseMotionListener(this);
         
        setPreferredSize(new Dimension(450, 450));
        setBorder(BorderFactory.createEmptyBorder(20,20,20,20));
    }
    
     
    void eventOutput(String eventDescription, MouseEvent e)  {
    	Calendar calendar = Calendar.getInstance();
    	long timeMilSec = new Date().getTime();
    	calendar.setTimeInMillis(timeMilSec);
    	Timestamp currentTimestamp = new java.sql.Timestamp(calendar.getTime().getTime());
    	int distance = ( ( e.getX() *2) + (e.getY() *2) ) /2;
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
    	Timestamp currentTimestamp2 = new java.sql.Timestamp(calendar.getTime().getTime());
    	String csvFile = "C:\\Users\\Lenovo\\Desktop\\eclipse\\CSV\\output.csv";
        FileWriter writer = new FileWriter(csvFile,true);
        writer.append( "[" + "(" + e.getX() + ")" + "(" +e.getY() +")" + "]" + "," + currentTimestamp2 + '\n');
		
        
        
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

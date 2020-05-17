package org.jxmapviewer.input;

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.*; 
import java.awt.event.*; 
import javax.swing.*;
import org.jxmapviewer.viewer.GeoPosition;
import java.awt.Cursor;
import java.awt.geom.Point2D;
import javax.swing.SwingUtilities;
import javax.swing.event.MouseInputAdapter;

import org.jxmapviewer.JXMapViewer;



public class ZoomOnMovement extends MouseAdapter implements MouseMotionListener
{
    private JXMapViewer viewer;
    
    
    public ZoomOnMovement(JXMapViewer viewer)
    {
        this.viewer = viewer;
    }
    
    @Override
    public void mouseMoved(MouseEvent evt) 
    { 
    	Point current = evt.getPoint();
        Rectangle bounds = viewer.getViewportBounds();
        int x = bounds.x + evt.getX();
        int y = bounds.y + evt.getY();
        
    	zoomMap(evt);
    	
    }   
    
    public void zoomMap(MouseEvent evt)
    {
        viewer.setZoom(viewer.getZoom() - 1);
        viewer.repaint();
    }
    
    



}

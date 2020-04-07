package org.jxmapviewer.input;

import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;

import javax.swing.SwingUtilities;

import org.jxmapviewer.JXMapViewer;

public class CenterMapListener extends MouseAdapter
{
    private JXMapViewer viewer;
    
    public CenterMapListener(JXMapViewer viewer)
    {
        this.viewer = viewer;
    }

    @Override
    public void mousePressed(MouseEvent evt)
    {
        boolean left = SwingUtilities.isLeftMouseButton(evt);
        boolean middle = SwingUtilities.isMiddleMouseButton(evt);
        boolean doubleClick = (evt.getClickCount() == 2);

        if (middle || (left && doubleClick))
        {
            recenterMap(evt);
        }
    }
    
    private void recenterMap(MouseEvent evt)
    {
        Rectangle bounds = viewer.getViewportBounds();
        double x = bounds.getX() + evt.getX();
        double y = bounds.getY() + evt.getY();
        viewer.setCenter(new Point2D.Double(x, y));
                viewer.setZoom(viewer.getZoom() - 1);
        viewer.repaint();
    }
}




package org.jxmapviewer.input;

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.SwingUtilities;

import org.jxmapviewer.JXMapViewer;
import org.jxmapviewer.viewer.GeoPosition;


public abstract class MapClickListener extends MouseAdapter {

    private final JXMapViewer viewer;

    
    public MapClickListener(JXMapViewer viewer) {
        this.viewer = viewer;
    }

    
    @Override
    public void mouseClicked(MouseEvent evt) {
        final boolean left = SwingUtilities.isLeftMouseButton(evt);
        final boolean singleClick = (evt.getClickCount() == 1);

        if ((left && singleClick)) {
            Rectangle bounds = viewer.getViewportBounds();
            int x = bounds.x + evt.getX();
            int y = bounds.y + evt.getY();
            Point pixelCoordinates = new Point(x, y);
            mapClicked(viewer.getTileFactory().pixelToGeo(pixelCoordinates, viewer.getZoom()));
        }
    }

    
    public abstract void mapClicked(GeoPosition location);
}

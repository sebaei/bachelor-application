package org.jxmapviewer.viewer;

import org.jxmapviewer.beans.AbstractBean;


public class DefaultWaypoint extends AbstractBean implements Waypoint 
{
    private GeoPosition position;

    
    public DefaultWaypoint()
    {
        this(new GeoPosition(0, 0));
    }

    
    public DefaultWaypoint(double latitude, double longitude)
    {
        this(new GeoPosition(latitude, longitude));
    }

   
    public DefaultWaypoint(GeoPosition coord)
    {
        this.position = coord;
    }

    @Override
    public GeoPosition getPosition()
    {
        return position;
    }

    
    public void setPosition(GeoPosition coordinate)
    {
        GeoPosition old = getPosition();
        this.position = coordinate;
        firePropertyChange("position", old, getPosition());
    }

}

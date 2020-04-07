package org.jxmapviewer.viewer;

import java.awt.Dimension;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

import org.jxmapviewer.cache.LocalCache;
import org.jxmapviewer.viewer.util.GeoUtil;


public abstract class TileFactory
{
    private TileFactoryInfo info;
    private List<TileListener> tileListeners = new ArrayList<TileListener>();

    
    protected TileFactory(TileFactoryInfo info)
    {
        this.info = info;
    }

    
    public int getTileSize(int zoom)
    {
        return getInfo().getTileSize(zoom);
    }

    
    public Dimension getMapSize(int zoom)
    {
        return GeoUtil.getMapSize(zoom, getInfo());
    }

    
    public abstract Tile getTile(int x, int y, int zoom);

   
    public GeoPosition pixelToGeo(Point2D pixelCoordinate, int zoom)
    {
        return GeoUtil.getPosition(pixelCoordinate, zoom, getInfo());
    }

    
    public Point2D geoToPixel(GeoPosition c, int zoomLevel)
    {
        return GeoUtil.getBitmapCoordinate(c, zoomLevel, getInfo());
    }

    
    public TileFactoryInfo getInfo()
    {
        return info;
    }

    
    public void addTileListener(TileListener listener)
    {
        tileListeners.add(listener);
    }

    
    public void removeTileListener(TileListener listener)
    {
        tileListeners.remove(listener);
    }

    
    public abstract void dispose();

    
    protected void fireTileLoadedEvent(Tile tile)
    {
        for (TileListener listener : tileListeners)
        {
            listener.tileLoaded(tile);
        }
    }

    
    protected abstract void startLoading(Tile tile);

    
    public void setLocalCache(LocalCache cache) { 
    	
    }
}


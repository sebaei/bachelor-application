package org.jxmapviewer.viewer;


import java.awt.image.BufferedImage;
import java.lang.ref.SoftReference;

import org.jxmapviewer.beans.AbstractBean;



public class Tile extends AbstractBean
{
    
    @SuppressWarnings("javadoc")
    public enum Priority
    {
        High, Low
    }

    private Priority priority = Priority.High;

    private TileFactory dtf;

    private boolean isLoading = false;

    
    private String url;

    

    private boolean loaded = false;
    
    
    
    private boolean loadFailed = false;
    
    
    
    private int zoom, x, y;

    
    SoftReference<BufferedImage> image = new SoftReference<BufferedImage>(null);

    
    public Tile(int x, int y, int zoom)
    {
        loaded = false;
        this.zoom = zoom;
        this.x = x;
        this.y = y;
    }

    
    Tile(int x, int y, int zoom, String url, Priority priority, TileFactory dtf)
    {
        this.url = url;
        loaded = false;
        this.zoom = zoom;
        this.x = x;
        this.y = y;
        this.priority = priority;
        this.dtf = dtf;
        
    }

    
    public synchronized boolean isLoaded()
    {
        return loaded;
    }

    
    synchronized void setLoaded(boolean loaded)
    {
        boolean old = isLoaded();
        this.loaded = loaded;
        firePropertyChange("loaded", old, isLoaded());
    }
      
    
    public synchronized boolean loadingFailed()
    {
        return loadFailed;
    }

    
    synchronized void setLoadingFailed(boolean fail)
    {
        loadFailed = fail;
    }
    

    
    public BufferedImage getImage()
    {
        BufferedImage img = image.get();
        if (img == null)
        {
            setLoaded(false);
            
            
            if (dtf != null)
            {
                dtf.startLoading(this);
            }
        }

        return img;
    }

    
    public int getZoom()
    {
        return zoom;
    }

    
    public boolean isLoading()
    {
        return isLoading;
    }

    
    public void setLoading(boolean isLoading)
    {
        this.isLoading = isLoading;
    }

   
    public Priority getPriority()
    {
        return priority;
    }

    
    public void setPriority(Priority priority)
    {
        this.priority = priority;
    }

    
    public String getURL()
    {
        return url;
    }

    
    public int getX()
    {
        return x;
    }

    
    public int getY()
    {
        return y;
    }

}
package org.jxmapviewer.viewer;

import java.awt.geom.Point2D;


public class TileFactoryInfo
{
    private int minimumZoomLevel;
    private int maximumZoomLevel;
    private int totalMapZoom;
    
    private int tileSize = 256;

    
    private int[] mapWidthInTilesAtZoom;
    
    private Point2D[] mapCenterInPixelsAtZoom;

    
    private double[] longitudeDegreeWidthInPixels;

    
    private double[] longitudeRadianWidthInPixels;

    
    protected String baseURL;
    private String xparam;
    private String yparam;
    private String zparam;
    private boolean xr2l = true;
    private boolean yt2b = true;

    private int defaultZoomLevel;

   
    private String name;

    
    public TileFactoryInfo(int minimumZoomLevel, int maximumZoomLevel, int totalMapZoom, int tileSize, boolean xr2l,
            boolean yt2b, String baseURL, String xparam, String yparam, String zparam)
    {
        this("name not provided", minimumZoomLevel, maximumZoomLevel, totalMapZoom, tileSize, xr2l, yt2b, baseURL,
                xparam, yparam, zparam);
    }

    
    public TileFactoryInfo(String name, int minimumZoomLevel, int maximumZoomLevel, int totalMapZoom, int tileSize,
            boolean xr2l, boolean yt2b, String baseURL, String xparam, String yparam, String zparam)
    {
        this.name = name;
        this.minimumZoomLevel = minimumZoomLevel;
        this.maximumZoomLevel = maximumZoomLevel;
        this.totalMapZoom = totalMapZoom;
        this.baseURL = baseURL;
        this.xparam = xparam;
        this.yparam = yparam;
        this.zparam = zparam;
        this.setXr2l(xr2l);
        this.setYt2b(yt2b);

        this.tileSize = tileSize;

        
        int tilesize = this.getTileSize(0);

        longitudeDegreeWidthInPixels = new double[totalMapZoom + 1];
        longitudeRadianWidthInPixels = new double[totalMapZoom + 1];
        mapCenterInPixelsAtZoom = new Point2D.Double[totalMapZoom + 1];
        mapWidthInTilesAtZoom = new int[totalMapZoom + 1];

        
        for (int z = totalMapZoom; z >= 0; --z)
        {
            
            longitudeDegreeWidthInPixels[z] = tilesize / 360.0;
            
            longitudeRadianWidthInPixels[z] = tilesize / (2.0 * Math.PI);
            int t2 = tilesize / 2;
            mapCenterInPixelsAtZoom[z] = new Point2D.Double(t2, t2);
            mapWidthInTilesAtZoom[z] = tilesize / this.getTileSize(0);
            tilesize *= 2;
        }

    }

    
    public int getMinimumZoomLevel()
    {
        return minimumZoomLevel;
    }

    
    public int getMaximumZoomLevel()
    {
        return maximumZoomLevel;
    }

    
    public int getTotalMapZoom()
    {
        return totalMapZoom;
    }

   
    public int getMapWidthInTilesAtZoom(int zoom)
    {
        return mapWidthInTilesAtZoom[zoom];
    }

    
    public Point2D getMapCenterInPixelsAtZoom(int zoom)
    {
        return mapCenterInPixelsAtZoom[zoom];
    }

    

    public String getTileUrl(int x, int y, int zoom)
    {
        // System.out.println("getting tile at zoom: " + zoom);
        // System.out.println("map width at zoom = " + getMapWidthInTilesAtZoom(zoom));
        String ypart = "&" + yparam + "=" + y;
        

        if (!yt2b)
        {
            int tilemax = getMapWidthInTilesAtZoom(zoom);
            
            ypart = "&" + yparam + "=" + (tilemax / 2 - y - 1);
        }
        
        String url = baseURL + "&" + xparam + "=" + x + ypart +
        
                "&" + zparam + "=" + zoom;
        return url;
    }

    
    public int getTileSize(int zoom)
    {
        return tileSize;
    }

   
    public double getLongitudeDegreeWidthInPixels(int zoom)
    {
        return longitudeDegreeWidthInPixels[zoom];
    }

    
    public double getLongitudeRadianWidthInPixels(int zoom)
    {
        return longitudeRadianWidthInPixels[zoom];
    }

    
    public boolean isXr2l()
    {
        return xr2l;
    }

    
    public void setXr2l(boolean xr2l)
    {
        this.xr2l = xr2l;
    }

    
    public boolean isYt2b()
    {
        return yt2b;
    }

    
    public void setYt2b(boolean yt2b)
    {
        this.yt2b = yt2b;
    }

    
    public int getDefaultZoomLevel()
    {
        return defaultZoomLevel;
    }

    
    public void setDefaultZoomLevel(int defaultZoomLevel)
    {
        this.defaultZoomLevel = defaultZoomLevel;
    }

    
    public String getName()
    {
        return name;
    }

    
    public String getBaseURL()
    {
        return baseURL;
    }

    
    public String getAttribution() {
        return null;
    }

    
    public String getLicense() {
        return null;
    }


}

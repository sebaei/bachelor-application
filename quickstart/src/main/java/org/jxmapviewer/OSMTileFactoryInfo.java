package org.jxmapviewer;

import org.jxmapviewer.viewer.TileFactoryInfo;


public class OSMTileFactoryInfo extends TileFactoryInfo
{
    private static final int MAX_ZOOM = 19;

    
    public OSMTileFactoryInfo()
    {
        this("OpenStreetMap", "http://tile.openstreetmap.org");
    }
    
    public OSMTileFactoryInfo(String name, String baseURL)
    {
        super(name,
                0, MAX_ZOOM, MAX_ZOOM,
                256, true, true,                     
                baseURL,
                "x", "y", "z");                        
        }

    @Override
    public String getTileUrl(int x, int y, int zoom)
    {
        int invZoom = MAX_ZOOM - zoom;
        String url = this.baseURL + "/" + invZoom + "/" + x + "/" + y + ".png";
        return url;
    }

    @Override
    public String getAttribution() {
        return "\u00A9 OpenStreetMap contributors";
    }

    @Override
    public String getLicense() {
        return "Creative Commons Attribution-ShareAlike 2.0";
    }



}

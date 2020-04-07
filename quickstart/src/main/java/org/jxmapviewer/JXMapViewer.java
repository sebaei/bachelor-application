package org.jxmapviewer;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.beans.DesignMode;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.net.URL;
import java.util.Set;

import javax.imageio.ImageIO;
import javax.swing.JPanel;

import org.jxmapviewer.input.PanMouseInputListener;
import org.jxmapviewer.painter.AbstractPainter;
import org.jxmapviewer.painter.Painter;
import org.jxmapviewer.viewer.GeoPosition;
import org.jxmapviewer.viewer.Tile;
import org.jxmapviewer.viewer.TileFactory;
import org.jxmapviewer.viewer.TileFactoryInfo;
import org.jxmapviewer.viewer.TileListener;
import org.jxmapviewer.viewer.empty.EmptyTileFactory;


public class JXMapViewer extends JPanel implements DesignMode
{
    private static final long serialVersionUID = -3530746298586937321L;

    
    private int zoomLevel = 1;

   
    private Point2D center = new Point2D.Double(0, 0);

 
    private boolean drawTileBorders = false;

   
    private TileFactory factory;

    
    private GeoPosition addressLocation;


    private Painter<? super JXMapViewer> overlay;

    private boolean designTime;

    private Image loadingImage;

    private boolean restrictOutsidePanning = true;
    private boolean horizontalWrapped = true;
    private boolean infiniteMapRendering = true;


    private boolean panningEnabled = true;

    public JXMapViewer()
    {
        factory = new EmptyTileFactory();
       
        try
        {
            URL url = JXMapViewer.class.getResource("/images/loading.png");
            this.setLoadingImage(ImageIO.read(url));
        }
        catch (Exception ex)
        {
            System.out.println("could not load 'loading.png'");
            BufferedImage img = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2 = img.createGraphics();
            g2.setColor(Color.black);
            g2.fillRect(0, 0, 16, 16);
            g2.dispose();
            this.setLoadingImage(img);
        }

        
    }

    @Override
    protected void paintComponent(Graphics g)
    {
        super.paintComponent(g);

        doPaintComponent(g);
    }

    
    private void doPaintComponent(Graphics g)
    {

        if (isDesignTime())
        {
            
        }
        else
        {
            int z = getZoom();
            Rectangle viewportBounds = getViewportBounds();
            drawMapTiles(g, z, viewportBounds);
            drawOverlays(z, g, viewportBounds);
        }

        super.paintBorder(g);
    }

    
    @Override
    public void setDesignTime(boolean b)
    {
        this.designTime = b;
    }

   
    @Override
    public boolean isDesignTime()
    {
        return designTime;
    }

    
    protected void drawMapTiles(final Graphics g, final int zoom, Rectangle viewportBounds)
    {
        int size = getTileFactory().getTileSize(zoom);
        Dimension mapSize = getTileFactory().getMapSize(zoom);

        
        int numWide = viewportBounds.width / size + 2;
        int numHigh = viewportBounds.height / size + 2;

        
        TileFactoryInfo info = getTileFactory().getInfo();

        // number of tiles in x direction
        int tpx = (int) Math.floor(viewportBounds.getX() / info.getTileSize(0));
        // number of tiles in y direction
        int tpy = (int) Math.floor(viewportBounds.getY() / info.getTileSize(0));
        
        for (int x = 0; x <= numWide; x++)
        {
            for (int y = 0; y <= numHigh; y++)
            {
                int itpx = x + tpx;
                int itpy = y + tpy;
                
                if (g.getClipBounds().intersects(
                        new Rectangle(itpx * size - viewportBounds.x, itpy * size - viewportBounds.y, size, size)))
                {
                    Tile tile = getTileFactory().getTile(itpx, itpy, zoom);
                    int ox = ((itpx * getTileFactory().getTileSize(zoom)) - viewportBounds.x);
                    int oy = ((itpy * getTileFactory().getTileSize(zoom)) - viewportBounds.y);

                    
                    if (!isTileOnMap(itpx, itpy, mapSize))
                    {
                        if (isOpaque())
                        {
                            g.setColor(getBackground());
                            g.fillRect(ox, oy, size, size);
                        }
                    }
                    else if (tile.isLoaded())
                    {
                        g.drawImage(tile.getImage(), ox, oy, null);
                    }
                    else
                    {
                        Tile superTile = null;

                        
                        if (zoom < info.getMaximumZoomLevel()) {
                            superTile = getTileFactory().getTile(itpx / 2, itpy / 2, zoom + 1);
                        }

                        if ( superTile != null && superTile.isLoaded())
                        {
                            int offX = (itpx % 2) * size / 2;
                            int offY = (itpy % 2) * size / 2;
                            g.drawImage(superTile.getImage(), ox, oy, ox + size, oy + size, offX, offY, offX + size / 2, offY + size / 2, null);
                        }
                        else
                        {
                            int imageX = (getTileFactory().getTileSize(zoom) - getLoadingImage().getWidth(null)) / 2;
                            int imageY = (getTileFactory().getTileSize(zoom) - getLoadingImage().getHeight(null)) / 2;
                            g.setColor(Color.GRAY);
                            g.fillRect(ox, oy, size, size);
                            g.drawImage(getLoadingImage(), ox + imageX, oy + imageY, null);
                        }
                    }
                    if (isDrawTileBorders())
                    {

                        g.setColor(Color.black);
                        g.drawRect(ox, oy, size, size);
                        g.drawRect(ox + size / 2 - 5, oy + size / 2 - 5, 10, 10);
                        g.setColor(Color.white);
                        g.drawRect(ox + 1, oy + 1, size, size);

                        String text = itpx + ", " + itpy + ", " + getZoom();
                        g.setColor(Color.BLACK);
                        g.drawString(text, ox + 10, oy + 30);
                        g.drawString(text, ox + 10 + 2, oy + 30 + 2);
                        g.setColor(Color.WHITE);
                        g.drawString(text, ox + 10 + 1, oy + 30 + 1);
                    }
                }
            }
        }
    }

    @SuppressWarnings("unused")
    private void drawOverlays(final int zoom, final Graphics g, final Rectangle viewportBounds)
    {
        if (overlay != null)
        {
            overlay.paint((Graphics2D) g, this, getWidth(), getHeight());
        }
    }

    private boolean isTileOnMap(int x, int y, Dimension mapSize)
    {
        return (y >= 0 && y < mapSize.getHeight()) &&
                  (isInfiniteMapRendering() || x >= 0 && x < mapSize.getWidth());
    }

   
    public void setOverlayPainter(Painter<? super JXMapViewer> overlay)
    {
        Painter<? super JXMapViewer> old = getOverlayPainter();
        this.overlay = overlay;

        PropertyChangeListener listener = new PropertyChangeListener()
        {
            @Override
            public void propertyChange(PropertyChangeEvent evt)
            {
                if (evt.getNewValue().equals(Boolean.TRUE))
                {
                    repaint();
                }
            }
        };

        if (old instanceof AbstractPainter)
        {
            AbstractPainter<?> ap = (AbstractPainter<?>) old;
            ap.removePropertyChangeListener("dirty", listener);
        }

        if (overlay instanceof AbstractPainter)
        {
            AbstractPainter<?> ap = (AbstractPainter<?>) overlay;
            ap.addPropertyChangeListener("dirty", listener);
        }

        firePropertyChange("mapOverlay", old, getOverlayPainter());
        repaint();
    }

    
    public Painter<? super JXMapViewer> getOverlayPainter()
    {
        return overlay;
    }

   
    public Rectangle getViewportBounds()
    {
        return calculateViewportBounds(getCenter());
    }

    private Rectangle calculateViewportBounds(Point2D centr)
    {
        Insets insets = getInsets();
        
        int viewportWidth = getWidth() - insets.left - insets.right;
        int viewportHeight = getHeight() - insets.top - insets.bottom;
        double viewportX = (centr.getX() - viewportWidth / 2);
        double viewportY = (centr.getY() - viewportHeight / 2);
        return new Rectangle((int) viewportX, (int) viewportY, viewportWidth, viewportHeight);
    }

    
    public void setZoom(int zoom)
    {
        if (zoom == this.zoomLevel)
        {
            return;
        }

        TileFactoryInfo info = getTileFactory().getInfo();
        
        if (info != null && (zoom < info.getMinimumZoomLevel() || zoom > info.getMaximumZoomLevel()))
        {
            return;
        }

        
        int oldzoom = this.zoomLevel;
        Point2D oldCenter = getCenter();
        Dimension oldMapSize = getTileFactory().getMapSize(oldzoom);
        this.zoomLevel = zoom;
        this.firePropertyChange("zoom", oldzoom, zoom);

        Dimension mapSize = getTileFactory().getMapSize(zoom);

        setCenter(new Point2D.Double(oldCenter.getX() * (mapSize.getWidth() / oldMapSize.getWidth()), oldCenter.getY()
                * (mapSize.getHeight() / oldMapSize.getHeight())));

        repaint();
    }

    
    public int getZoom()
    {
        return this.zoomLevel;
    }

    
    public GeoPosition getAddressLocation()
    {
        return addressLocation;
    }

   
    public void setAddressLocation(GeoPosition addressLocation)
    {
        GeoPosition old = getAddressLocation();
        this.addressLocation = addressLocation;
        setCenter(getTileFactory().geoToPixel(addressLocation, getZoom()));

        firePropertyChange("addressLocation", old, getAddressLocation());
        repaint();
    }

   
    public void recenterToAddressLocation()
    {
        setCenter(getTileFactory().geoToPixel(getAddressLocation(), getZoom()));
        repaint();
    }

 
    public boolean isDrawTileBorders()
    {
        return drawTileBorders;
    }

    public void setDrawTileBorders(boolean drawTileBorders)
    {
        boolean old = isDrawTileBorders();
        this.drawTileBorders = drawTileBorders;
        firePropertyChange("drawTileBorders", old, isDrawTileBorders());
        repaint();
    }

   
    public void setCenterPosition(GeoPosition geoPosition)
    {
        GeoPosition oldVal = getCenterPosition();
        setCenter(getTileFactory().geoToPixel(geoPosition, zoomLevel));
        repaint();
        GeoPosition newVal = getCenterPosition();
        firePropertyChange("centerPosition", oldVal, newVal);
    }

    
    public GeoPosition getCenterPosition()
    {
        return getTileFactory().pixelToGeo(getCenter(), zoomLevel);
    }

    
    public TileFactory getTileFactory()
    {
        return factory;
    }

   
    public void setTileFactory(TileFactory factory)
    {
        if (factory == null)
            throw new NullPointerException("factory must not be null");

        this.factory.removeTileListener(tileLoadListener);
        this.factory.dispose();

        this.factory = factory;
        this.setZoom(factory.getInfo().getDefaultZoomLevel());

        factory.addTileListener(tileLoadListener);

        repaint();
    }


    public Image getLoadingImage()
    {
        return loadingImage;
    }


    public void setLoadingImage(Image loadingImage)
    {
        this.loadingImage = loadingImage;
    }

    
    public Point2D getCenter()
    {
        return center;
    }

    
    public void setCenter(Point2D center)
    {
        Point2D old = this.getCenter();

        double centerX = center.getX();
        double centerY = center.getY();

        Dimension mapSize = getTileFactory().getMapSize(getZoom());
        int mapHeight = (int) mapSize.getHeight() * getTileFactory().getTileSize(getZoom());
        int mapWidth = (int) mapSize.getWidth() * getTileFactory().getTileSize(getZoom());

        if (isRestrictOutsidePanning())
        {
            Insets insets = getInsets();
            int viewportHeight = getHeight() - insets.top - insets.bottom;
            int viewportWidth = getWidth() - insets.left - insets.right;

            // don't let pan over the top edge
            Rectangle newVP = calculateViewportBounds(center);
            if (newVP.getY() < 0)
            {
                centerY = viewportHeight / 2;
            }

            // don't let pan over the left edge
            if (!isHorizontalWrapped() && newVP.getX() < 0)
            {
                centerX = viewportWidth / 2;
            }

            // don't let pan over the bottom edge
            if (newVP.getY() + newVP.getHeight() > mapHeight)
            {
                centerY = mapHeight - viewportHeight / 2;
            }

            // don't let pan over the right edge
            if (!isHorizontalWrapped() && (newVP.getX() + newVP.getWidth() > mapWidth))
            {
                centerX = mapWidth - viewportWidth / 2;
            }

          
            if (mapHeight < newVP.getHeight())
            {
                centerY = mapHeight / 2;
            }

            
            if (!isHorizontalWrapped() && mapWidth < newVP.getWidth())
            {
                centerX = mapWidth / 2;
            }
        }

        
        {
            centerX = centerX % mapWidth;
            centerY = centerY % mapHeight;

            if (centerX < 0)
                centerX += mapWidth;

            if (centerY < 0)
                centerY += mapHeight;
        }

        GeoPosition oldGP = this.getCenterPosition();
        this.center = new Point2D.Double(centerX, centerY);
        firePropertyChange("center", old, this.center);
        firePropertyChange("centerPosition", oldGP, this.getCenterPosition());
        repaint();
    }

    public void calculateZoomFrom(Set<GeoPosition> positions)
    {
        
        if (positions.size() < 2)
        {
            return;
        }

        int zoom = getZoom();
        Rectangle2D rect = generateBoundingRect(positions, zoom);
        
        int count = 0;
        while (!getViewportBounds().contains(rect))
        {
            
            Point2D centr = new Point2D.Double(rect.getX() + rect.getWidth() / 2, rect.getY() + rect.getHeight() / 2);
            GeoPosition px = getTileFactory().pixelToGeo(centr, zoom);
            
            setCenterPosition(px);
            count++;
            if (count > 30)
                break;

            if (getViewportBounds().contains(rect))
            {
                
                break;
            }
            zoom = zoom + 1;
            if (zoom > 15) 
            {
                break;
            }
            setZoom(zoom);
            rect = generateBoundingRect(positions, zoom);
        }
    }

    public void zoomToBestFit(Set<GeoPosition> positions, double maxFraction)
    {
        if (positions.isEmpty())
            return;

        if (maxFraction <= 0 || maxFraction > 1)
            throw new IllegalArgumentException("maxFraction must be between 0 and 1");

        TileFactory tileFactory = getTileFactory();
        TileFactoryInfo info = tileFactory.getInfo();

        if(info == null)
            return;

       
        GeoPosition centre = computeGeoCenter(positions);
        setCenterPosition(centre);

        if (positions.size() == 1)
            return;

        
        int bestZoom = info.getMaximumZoomLevel();

        Rectangle2D viewport = getViewportBounds();

        Rectangle2D bounds = generateBoundingRect(positions, bestZoom);

        
        while (bestZoom >= info.getMinimumZoomLevel() &&
               bounds.getWidth() < viewport.getWidth() * maxFraction &&
               bounds.getHeight() < viewport.getHeight() * maxFraction)
        {
            bestZoom--;
            bounds = generateBoundingRect(positions, bestZoom);
        }

        setZoom(bestZoom + 1);
    }

    private Rectangle2D generateBoundingRect(final Set<GeoPosition> positions, int zoom)
    {
        Point2D point1 = getTileFactory().geoToPixel(positions.iterator().next(), zoom);
        Rectangle2D rect = new Rectangle2D.Double(point1.getX(), point1.getY(), 0, 0);

        for (GeoPosition pos : positions)
        {
            Point2D point = getTileFactory().geoToPixel(pos, zoom);
            rect.add(point);
        }
        return rect;
    }

    private GeoPosition computeGeoCenter(final Set<GeoPosition> positions)
    {
        double sumLat = 0;
        double sumLon = 0;

        for (GeoPosition pos : positions)
        {
            sumLat += pos.getLatitude();
            sumLon += pos.getLongitude();
        }
        double avgLat = sumLat / positions.size();
        double avgLon = sumLon / positions.size();
        return new GeoPosition(avgLat, avgLon);
    }

    
    private TileListener tileLoadListener = new TileListener()
    {
        @Override
        public void tileLoaded(Tile tile)
        {
                if (tile.getZoom() == getZoom())
                {
                    repaint();
                    
                }
            }

    };

    
    public boolean isRestrictOutsidePanning()
    {
        return restrictOutsidePanning;
    }

   
    public void setRestrictOutsidePanning(boolean restrictOutsidePanning)
    {
        this.restrictOutsidePanning = restrictOutsidePanning;
    }

    
    public boolean isHorizontalWrapped()
    {
        return horizontalWrapped;
    }


    public void setInfiniteMapRendering(boolean infiniteMapRendering)
    {
        this.infiniteMapRendering = infiniteMapRendering;
    }

    
    public boolean isInfiniteMapRendering()
    {
        return horizontalWrapped || infiniteMapRendering;
    }

    public void setHorizontalWrapped(boolean horizontalWrapped)
    {
        this.horizontalWrapped = horizontalWrapped;
    }

    public Point2D convertGeoPositionToPoint(GeoPosition pos)
    {
        
        Point2D pt = getTileFactory().geoToPixel(pos, getZoom());
        
        Rectangle bounds = getViewportBounds();
        return new Point2D.Double(pt.getX() - bounds.getX(), pt.getY() - bounds.getY());
    }

    public GeoPosition convertPointToGeoPosition(Point2D pt)
    {
        
        Rectangle bounds = getViewportBounds();
        Point2D pt2 = new Point2D.Double(pt.getX() + bounds.getX(), pt.getY() + bounds.getY());

        
        GeoPosition pos = getTileFactory().pixelToGeo(pt2, getZoom());
        return pos;
    }

    @Deprecated
    public boolean isNegativeYAllowed()
    {
        return true;
    }

   
    public void setPanEnabled(boolean enabled)
    {
        this.panningEnabled = enabled;
    }


    public boolean isPanningEnabled()
    {
        return this.panningEnabled;
    }
}

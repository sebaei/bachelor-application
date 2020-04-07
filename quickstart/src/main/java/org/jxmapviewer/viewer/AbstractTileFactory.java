package org.jxmapviewer.viewer;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.SoftReference;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.ThreadFactory;

import javax.imageio.ImageIO;
import javax.swing.SwingUtilities;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jxmapviewer.cache.LocalCache;
import org.jxmapviewer.cache.NoOpLocalCache;
import org.jxmapviewer.util.ProjectProperties;
import org.jxmapviewer.viewer.util.GeoUtil;


public abstract class AbstractTileFactory extends TileFactory
{
    private static final Log log = LogFactory.getLog(AbstractTileFactory.class);

   
    private static final String DEFAULT_USER_AGENT = ProjectProperties.INSTANCE.getName() + "/"
            + ProjectProperties.INSTANCE.getVersion();

    private int threadPoolSize = 4;
    private String userAgent = DEFAULT_USER_AGENT;
    private ExecutorService service;

    
    private Map<String, Tile> tileMap = new HashMap<String, Tile>();

    private TileCache cache = new TileCache();

    
    public AbstractTileFactory(TileFactoryInfo info)
    {
        super(info);
    }

    
    @Override
    public Tile getTile(int x, int y, int zoom)
    {
        return getTile(x, y, zoom, true);
    }

    private Tile getTile(int tpx, int tpy, int zoom, boolean eagerLoad)
    {
        
        int tileX = tpx;
        int numTilesWide = (int) getMapSize(zoom).getWidth();
        if (tileX < 0)
        {
            tileX = numTilesWide - (Math.abs(tileX) % numTilesWide);
        }

        tileX = tileX % numTilesWide;
        int tileY = tpy;
        
        String url = getInfo().getTileUrl(tileX, tileY, zoom);
        

        Tile.Priority pri = Tile.Priority.High;
        if (!eagerLoad)
        {
            pri = Tile.Priority.Low;
        }
        Tile tile;
        
        if (!tileMap.containsKey(url))
        {
            if (!GeoUtil.isValidTile(tileX, tileY, zoom, getInfo()))
            {
                tile = new Tile(tileX, tileY, zoom);
            }
            else
            {
                tile = new Tile(tileX, tileY, zoom, url, pri, this);
                startLoading(tile);
            }
            tileMap.put(url, tile);
        }
        else
        {
            tile = tileMap.get(url);

            
            if (tile.loadingFailed()) {
                log.info("Removing from map: " + tile.getURL() + ", tile failed to load");
                tileMap.remove(url);
            }

            
            if (tile.getPriority() == Tile.Priority.Low && eagerLoad && !tile.isLoaded())
            {
                
                promote(tile);
            }
        }

        

        return tile;
    }

    
    
    public TileCache getTileCache()
    {
        return cache;
    }

    
    public void setTileCache(TileCache cache)
    {
        this.cache = cache;
    }

    
    private BlockingQueue<Tile> tileQueue = new PriorityBlockingQueue<Tile>(5, new Comparator<Tile>()
    {
        @Override
        public int compare(Tile o1, Tile o2)
        {
            if (o1.getPriority() == Tile.Priority.Low && o2.getPriority() == Tile.Priority.High)
            {
                return 1;
            }
            if (o1.getPriority() == Tile.Priority.High && o2.getPriority() == Tile.Priority.Low)
            {
                return -1;
            }
            return 0;

        }
    });

    private LocalCache localCache = new NoOpLocalCache();

    
    protected synchronized ExecutorService getService()
    {
        if (service == null)
        {
            
            service = Executors.newFixedThreadPool(threadPoolSize, new ThreadFactory()
            {
                private int count = 0;

                @Override
                public Thread newThread(Runnable r)
                {
                    Thread t = new Thread(r, "tile-pool-" + count++);
                    t.setPriority(Thread.MIN_PRIORITY);
                    t.setDaemon(true);
                    return t;
                }
            });
        }
        return service;
    }

    @Override
    public void dispose()
    {
        if (service != null)
        {
            service.shutdown();
            service = null;
        }
    }

    
    public void setThreadPoolSize(int size)
    {
        if (size <= 0)
        {
            throw new IllegalArgumentException("size invalid: " + size
                    + ". The size of the threadpool must be greater than 0.");
        }
        threadPoolSize = size;
    }

    
    public void setUserAgent(String userAgent) {
        if (userAgent == null || userAgent.isEmpty()) {
            throw new IllegalArgumentException("User agent can't be null or empty.");
        }

        this.userAgent = userAgent;
    }


    @Override
    protected synchronized void startLoading(Tile tile)
    {
        if (tile.isLoading())
        {
            
            return;
        }
        tile.setLoading(true);
        try
        {
            tileQueue.put(tile);
            getService().submit(createTileRunner(tile));
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }

    
    protected Runnable createTileRunner(Tile tile)
    {
        return new TileRunner();
    }

    
    public synchronized void promote(Tile tile)
    {
        if (tileQueue.contains(tile))
        {
            try
            {
                tileQueue.remove(tile);
                tile.setPriority(Tile.Priority.High);
                tileQueue.put(tile);
            }
            catch (Exception ex)
            {
                ex.printStackTrace();
            }
        }
    }

    @Override
    public void setLocalCache(LocalCache cache) {
        this.localCache = cache;
    }

    
    public synchronized int getPendingTiles() {
        return tileQueue.size();
    }

    
    private class TileRunner implements Runnable
    {
        
        protected URI getURI(Tile tile) throws URISyntaxException
        {
            if (tile.getURL() == null)
            {
                return null;
            }
            return new URI(tile.getURL());
        }

        @Override
        public void run()
        {
            
            final Tile tile = tileQueue.remove();
            tile.setLoadingFailed(false);

            int remainingAttempts = 3;
            while (!tile.isLoaded() && remainingAttempts > 0)
            {
                remainingAttempts--;
                try
                {
                    URI uri = getURI(tile);
                    BufferedImage img = cache.get(uri);
                    if (img == null)
                    {
                        byte[] bimg = cacheInputStream(uri.toURL());
                        
                        img = ImageIO.read(new ByteArrayInputStream(bimg));
                        cache.put(uri, bimg, img);
                        img = cache.get(uri);
                    }
                    if (img == null)
                    {
                        // System.out.println("error loading: " + uri);
                        log.info("Failed to load: " + uri);
                    }
                    else
                    {
                        final BufferedImage i = img;
                        SwingUtilities.invokeAndWait(new Runnable()
                        {
                            @Override
                            public void run()
                            {
                                tile.image = new SoftReference<BufferedImage>(i);
                                tile.setLoaded(true);
                                fireTileLoadedEvent(tile);
                            }
                        });
                    }
                }
                catch (OutOfMemoryError memErr)
                {
                    cache.needMoreMemory();
                }
                catch (FileNotFoundException fnfe)  
                {
                    log.error("Unable to load tile: " + fnfe.getMessage());
                    remainingAttempts = 0;
                    tile.setLoadingFailed(true);
                }
                catch (Throwable e)
                {
                    if (remainingAttempts == 0)
                    {
                        log.error("Failed to load a tile at URL: " + tile.getURL() + ", stopping", e);
                        tile.setLoadingFailed(true);
                    }
                    else
                    {
                        log.warn("Failed to load a tile at URL: " + tile.getURL() + ", retrying", e);
                    }
                }
            }
            tile.setLoading(false);
        }

        private byte[] cacheInputStream(URL url) throws IOException
        {
            InputStream ins = localCache.get(url);
            if (ins == null) {
                URLConnection connection = url.openConnection();
                connection.setRequestProperty("User-Agent", userAgent);
                ins = connection.getInputStream();
            }
            try {
                byte[] data = readAllBytes(ins);
                localCache.put(url, new ByteArrayInputStream(data));
                return data;
            }
            finally {
                ins.close();
            }
        }

        private byte[] readAllBytes(InputStream ins) throws IOException {
            ByteArrayOutputStream bout = new ByteArrayOutputStream();
            byte[] buf = new byte[256];
            while (true)
            {
                int n = ins.read(buf);
                if (n == -1)
                    break;
                bout.write(buf, 0, n);
            }
            return bout.toByteArray();
        }
    }
}


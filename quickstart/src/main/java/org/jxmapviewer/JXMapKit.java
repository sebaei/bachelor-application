package org.jxmapviewer;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.geom.Point2D;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.HashSet;
import java.util.Set;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.event.MouseInputListener;

import org.jxmapviewer.input.CenterMapListener;
import org.jxmapviewer.input.PanMouseInputListener;
import org.jxmapviewer.input.ZoomMouseWheelListenerCursor;
import org.jxmapviewer.painter.AbstractPainter;
import org.jxmapviewer.painter.CompoundPainter;
import org.jxmapviewer.painter.Painter;
import org.jxmapviewer.viewer.DefaultTileFactory;
import org.jxmapviewer.viewer.DefaultWaypoint;
import org.jxmapviewer.viewer.GeoPosition;
import org.jxmapviewer.viewer.TileFactory;
import org.jxmapviewer.viewer.TileFactoryInfo;
import org.jxmapviewer.viewer.Waypoint;
import org.jxmapviewer.viewer.WaypointPainter;



public class JXMapKit extends JPanel
{
    private static final long serialVersionUID = -8366577998349912380L;
    private boolean miniMapVisible = true;
    private boolean zoomSliderVisible = true;
    private boolean zoomButtonsVisible = true;
    private final boolean sliderReversed = false;

    @SuppressWarnings("javadoc")
    public enum DefaultProviders
    {
        OpenStreetMaps, Custom
    }

    private DefaultProviders defaultProvider = DefaultProviders.OpenStreetMaps;

    private boolean addressLocationShown = true;

    private boolean dataProviderCreditShown = true;

    
    public JXMapKit()
    {
        initComponents();
        setDataProviderCreditShown(false);

        zoomSlider.setOpaque(false);
        try
        {
            Icon minusIcon = new ImageIcon(JXMapKit.class.getResource("/images/minus.png"));
            this.zoomOutButton.setIcon(minusIcon);
            this.zoomOutButton.setText("");
            Icon plusIcon = new ImageIcon(JXMapKit.class.getResource("/images/plus.png"));
            this.zoomInButton.setIcon(plusIcon);
            this.zoomInButton.setText("");
        }
        catch (Throwable thr)
        {
            System.out.println("error: " + thr.getMessage());
            thr.printStackTrace();
        }

        TileFactoryInfo info = new OSMTileFactoryInfo();
        TileFactory tileFactory = new DefaultTileFactory(info);
        setTileFactory(tileFactory);

        mainMap.setCenterPosition(new GeoPosition(0, 0));
        miniMap.setCenterPosition(new GeoPosition(0, 0));
        mainMap.setRestrictOutsidePanning(true);
        miniMap.setRestrictOutsidePanning(true);

        rebuildMainMapOverlay();

       

        mainMap.addPropertyChangeListener("center", new PropertyChangeListener()
        {
            @Override
            public void propertyChange(PropertyChangeEvent evt)
            {
                Point2D mapCenter = (Point2D) evt.getNewValue();
                TileFactory tf = mainMap.getTileFactory();
                GeoPosition mapPos = tf.pixelToGeo(mapCenter, mainMap.getZoom());
                miniMap.setCenterPosition(mapPos);
            }
        });

        mainMap.addPropertyChangeListener("centerPosition", new PropertyChangeListener()
        {
            @Override
            public void propertyChange(PropertyChangeEvent evt)
            {
                mapCenterPosition = (GeoPosition) evt.getNewValue();
                miniMap.setCenterPosition(mapCenterPosition);
                Point2D pt = miniMap.getTileFactory().geoToPixel(mapCenterPosition, miniMap.getZoom());
                miniMap.setCenter(pt);
                miniMap.repaint();
            }
        });


        
        MouseInputListener mia = new PanMouseInputListener(mainMap);
        mainMap.addMouseListener(mia);
        mainMap.addMouseMotionListener(mia);

        mainMap.addMouseListener(new CenterMapListener(mainMap));

        mainMap.addMouseWheelListener(new ZoomMouseWheelListenerCursor(mainMap));

        //mainMap.addKeyListener(new PanKeyListener(mainMap));

        mainMap.addPropertyChangeListener("zoom", new PropertyChangeListener()
        {
            @Override
            public void propertyChange(PropertyChangeEvent evt)
            {
                zoomSlider.setValue(mainMap.getZoom());
                miniMap.setZoom(mainMap.getZoom() + 4);
            }
        });

        
        miniMap.setOverlayPainter(new Painter<JXMapViewer>()
        {
            @Override
            public void paint(Graphics2D g, JXMapViewer map, int width, int height)
            {
                
                Rectangle mainMapBounds = mainMap.getViewportBounds();

               
                Point2D upperLeft2D = mainMapBounds.getLocation();
                Point2D lowerRight2D = new Point2D.Double(upperLeft2D.getX() + mainMapBounds.getWidth(), upperLeft2D
                        .getY() + mainMapBounds.getHeight());

              
                GeoPosition upperLeft = mainMap.getTileFactory().pixelToGeo(upperLeft2D, mainMap.getZoom());
                GeoPosition lowerRight = mainMap.getTileFactory().pixelToGeo(lowerRight2D, mainMap.getZoom());

                
                upperLeft2D = map.getTileFactory().geoToPixel(upperLeft, map.getZoom());
                lowerRight2D = map.getTileFactory().geoToPixel(lowerRight, map.getZoom());

                g = (Graphics2D) g.create();
                Rectangle rect = map.getViewportBounds();
                
                g.translate(-rect.x, -rect.y);

                g.setPaint(Color.RED);
                
                g.drawRect((int) upperLeft2D.getX(), (int) upperLeft2D.getY(),
                        (int) (lowerRight2D.getX() - upperLeft2D.getX()),
                        (int) (lowerRight2D.getY() - upperLeft2D.getY()));
                g.setPaint(new Color(255, 0, 0, 50));
                g.fillRect((int) upperLeft2D.getX(), (int) upperLeft2D.getY(),
                        (int) (lowerRight2D.getX() - upperLeft2D.getX()),
                        (int) (lowerRight2D.getY() - upperLeft2D.getY()));
                
                g.dispose();
            }
        });

        if (getDefaultProvider() == DefaultProviders.OpenStreetMaps)
        {
            setZoom(10);
        }
        else
        {
            setZoom(3);
        }
        this.setCenterPosition(new GeoPosition(0, 0));
    }

    
    private GeoPosition mapCenterPosition = new GeoPosition(0, 0);
    private boolean zoomChanging = false;

  
    public void setZoom(int zoom)
    {
        zoomChanging = true;
        mainMap.setZoom(zoom);
        miniMap.setZoom(mainMap.getZoom() + 4);
        if (sliderReversed)
        {
            zoomSlider.setValue(zoomSlider.getMaximum() - zoom);
        }
        else
        {
            zoomSlider.setValue(zoom);
        }
        zoomChanging = false;
    }

    
    public Action getZoomOutAction()
    {
        Action act = new AbstractAction()
        {
            
            private static final long serialVersionUID = 5525706163434375107L;

            @Override
            public void actionPerformed(ActionEvent e)
            {
                setZoom(mainMap.getZoom() - 1);
            }
        };
        act.putValue(Action.NAME, "-");
        return act;
    }

   
    public Action getZoomInAction()
    {
        Action act = new AbstractAction()
        {
            
            private static final long serialVersionUID = 5779971489365451352L;

            @Override
            public void actionPerformed(ActionEvent e)
            {
                setZoom(mainMap.getZoom() + 1);
            }
        };
        act.putValue(Action.NAME, "+");
        return act;
    }

    
    private void initComponents()
    {
        java.awt.GridBagConstraints gridBagConstraints;

        mainMap = new org.jxmapviewer.JXMapViewer();
        miniMap = new org.jxmapviewer.JXMapViewer();
        jPanel1 = new javax.swing.JPanel();
        zoomInButton = new javax.swing.JButton();
        zoomOutButton = new javax.swing.JButton();
        zoomSlider = new javax.swing.JSlider();

        setLayout(new java.awt.GridBagLayout());

        mainMap.setLayout(new java.awt.GridBagLayout());

        miniMap.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        miniMap.setMinimumSize(new java.awt.Dimension(100, 100));
        miniMap.setPreferredSize(new java.awt.Dimension(100, 100));
        miniMap.setLayout(new java.awt.GridBagLayout());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.SOUTHEAST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        mainMap.add(miniMap, gridBagConstraints);

        jPanel1.setOpaque(false);
        jPanel1.setLayout(new java.awt.GridBagLayout());

        zoomInButton.setAction(getZoomOutAction());
        zoomInButton.setIcon(new ImageIcon(JXMapKit.class.getResource("/images/plus.png")));
        zoomInButton.setMargin(new java.awt.Insets(2, 2, 2, 2));
        zoomInButton.setMaximumSize(new java.awt.Dimension(20, 20));
        zoomInButton.setMinimumSize(new java.awt.Dimension(20, 20));
        zoomInButton.setOpaque(false);
        zoomInButton.setPreferredSize(new java.awt.Dimension(20, 20));
        zoomInButton.addActionListener(new java.awt.event.ActionListener()
        {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                zoomInButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHEAST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        jPanel1.add(zoomInButton, gridBagConstraints);

        zoomOutButton.setAction(getZoomInAction());
        zoomOutButton.setIcon(new ImageIcon(JXMapKit.class.getResource("/images/minus.png")));
        zoomOutButton.setMargin(new java.awt.Insets(2, 2, 2, 2));
        zoomOutButton.setMaximumSize(new java.awt.Dimension(20, 20));
        zoomOutButton.setMinimumSize(new java.awt.Dimension(20, 20));
        zoomOutButton.setOpaque(false);
        zoomOutButton.setPreferredSize(new java.awt.Dimension(20, 20));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weighty = 1.0;
        jPanel1.add(zoomOutButton, gridBagConstraints);

        zoomSlider.setMajorTickSpacing(1);
        zoomSlider.setMaximum(15);
        zoomSlider.setMinimum(10);
        zoomSlider.setMinorTickSpacing(1);
        zoomSlider.setOrientation(SwingConstants.VERTICAL);
        zoomSlider.setPaintTicks(true);
        zoomSlider.setSnapToTicks(true);
        zoomSlider.setMinimumSize(new java.awt.Dimension(35, 100));
        zoomSlider.setPreferredSize(new java.awt.Dimension(35, 190));
        zoomSlider.addChangeListener(new javax.swing.event.ChangeListener()
        {
            @Override
            public void stateChanged(javax.swing.event.ChangeEvent evt)
            {
                zoomSliderStateChanged(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.VERTICAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
        jPanel1.add(zoomSlider, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.SOUTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(4, 4, 4, 4);
        mainMap.add(jPanel1, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        add(mainMap, gridBagConstraints);
    }

    @SuppressWarnings("unused")
    private void zoomInButtonActionPerformed(java.awt.event.ActionEvent evt)
    {
    }

    @SuppressWarnings("unused")
    private void zoomSliderStateChanged(javax.swing.event.ChangeEvent evt)
    {
        if (!zoomChanging)
        {
            setZoom(zoomSlider.getValue());
        }
       
    }

    
    private javax.swing.JPanel jPanel1;
    private org.jxmapviewer.JXMapViewer mainMap;
    private org.jxmapviewer.JXMapViewer miniMap;
    private javax.swing.JButton zoomInButton;
    private javax.swing.JButton zoomOutButton;
    private javax.swing.JSlider zoomSlider;

    
    public boolean isMiniMapVisible()
    {
        return miniMapVisible;
    }

    
    public void setMiniMapVisible(boolean miniMapVisible)
    {
        boolean old = this.isMiniMapVisible();
        this.miniMapVisible = miniMapVisible;
        miniMap.setVisible(miniMapVisible);
        firePropertyChange("miniMapVisible", old, this.isMiniMapVisible());
    }

    
    public boolean isZoomSliderVisible()
    {
        return zoomSliderVisible;
    }

    
    public void setZoomSliderVisible(boolean zoomSliderVisible)
    {
        boolean old = this.isZoomSliderVisible();
        this.zoomSliderVisible = zoomSliderVisible;
        zoomSlider.setVisible(zoomSliderVisible);
        firePropertyChange("zoomSliderVisible", old, this.isZoomSliderVisible());
    }

    
    public boolean isZoomButtonsVisible()
    {
        return zoomButtonsVisible;
    }

    
    public void setZoomButtonsVisible(boolean zoomButtonsVisible)
    {
        boolean old = this.isZoomButtonsVisible();
        this.zoomButtonsVisible = zoomButtonsVisible;
        zoomInButton.setVisible(zoomButtonsVisible);
        zoomOutButton.setVisible(zoomButtonsVisible);
        firePropertyChange("zoomButtonsVisible", old, this.isZoomButtonsVisible());
    }

    
    public void setTileFactory(TileFactory fact)
    {
        mainMap.setTileFactory(fact);
        mainMap.setZoom(fact.getInfo().getDefaultZoomLevel());
        mainMap.setCenterPosition(new GeoPosition(0, 0));
        miniMap.setTileFactory(fact);
        miniMap.setZoom(fact.getInfo().getDefaultZoomLevel() + 3);
        miniMap.setCenterPosition(new GeoPosition(0, 0));
        zoomSlider.setMinimum(fact.getInfo().getMinimumZoomLevel());
        zoomSlider.setMaximum(fact.getInfo().getMaximumZoomLevel());
    }

    
    public void setCenterPosition(GeoPosition pos)
    {
        mainMap.setCenterPosition(pos);
        miniMap.setCenterPosition(pos);
    }

    
    public GeoPosition getCenterPosition()
    {
        return mainMap.getCenterPosition();
    }

    
    public GeoPosition getAddressLocation()
    {
        return mainMap.getAddressLocation();
    }

    
    public void setAddressLocation(GeoPosition pos)
    {
        mainMap.setAddressLocation(pos);
    }

   
    public JXMapViewer getMainMap()
    {
        return this.mainMap;
    }

    
    public JXMapViewer getMiniMap()
    {
        return this.miniMap;
    }

    
    public JButton getZoomInButton()
    {
        return this.zoomInButton;
    }

    
    public JButton getZoomOutButton()
    {
        return this.zoomOutButton;
    }

    
    public JSlider getZoomSlider()
    {
        return this.zoomSlider;
    }

    
    public void setAddressLocationShown(boolean b)
    {
        boolean old = isAddressLocationShown();
        this.addressLocationShown = b;
        addressLocationPainter.setVisible(b);
        firePropertyChange("addressLocationShown", old, b);
        repaint();
    }

    
    public boolean isAddressLocationShown()
    {
        return addressLocationShown;
    }

    
    public void setDataProviderCreditShown(boolean b)
    {
        boolean old = isDataProviderCreditShown();
        this.dataProviderCreditShown = b;
        dataProviderCreditPainter.setVisible(b);
        repaint();
        firePropertyChange("dataProviderCreditShown", old, b);
    }

    
    public boolean isDataProviderCreditShown()
    {
        return dataProviderCreditShown;
    }

    @SuppressWarnings("unchecked")
    private void rebuildMainMapOverlay()
    {
        CompoundPainter<JXMapViewer> cp = new CompoundPainter<JXMapViewer>();
        cp.setCacheable(false);
        
        cp.setPainters(dataProviderCreditPainter, addressLocationPainter);
        mainMap.setOverlayPainter(cp);
    }

    
    public void setDefaultProvider(DefaultProviders prov)
    {
        DefaultProviders old = this.defaultProvider;
        this.defaultProvider = prov;
        if (prov == DefaultProviders.OpenStreetMaps)
        {
            TileFactoryInfo info = new OSMTileFactoryInfo();
            TileFactory tf = new DefaultTileFactory(info);
            setTileFactory(tf);
            setZoom(11);
            setAddressLocation(new GeoPosition(51.5, 0));
        }
        firePropertyChange("defaultProvider", old, prov);
        repaint();
    }

    
    public DefaultProviders getDefaultProvider()
    {
        return this.defaultProvider;
    }

    private AbstractPainter<JXMapViewer> dataProviderCreditPainter = new AbstractPainter<JXMapViewer>(false)
    {
        @Override
        protected void doPaint(Graphics2D g, JXMapViewer map, int width, int height)
        {
            g.setPaint(Color.WHITE);
            g.drawString("data ", 50, map.getHeight() - 10);
        }
    };

    private WaypointPainter<Waypoint> addressLocationPainter = new WaypointPainter<Waypoint>()
    {
        @Override
        public Set<Waypoint> getWaypoints()
        {
            Set<Waypoint> set = new HashSet<Waypoint>();
            if (getAddressLocation() != null)
            {
                set.add(new DefaultWaypoint(getAddressLocation()));
            }
            else
            {
                set.add(new DefaultWaypoint(0, 0));
            }
            return set;
        }
    };

    
    public static void main(String... args)
    {
        SwingUtilities.invokeLater(new Runnable()
        {
            @Override
            public void run()
            {
                JXMapKit kit = new JXMapKit();
                kit.setDefaultProvider(DefaultProviders.OpenStreetMaps);

                TileFactoryInfo info = new OSMTileFactoryInfo();
                TileFactory tf = new DefaultTileFactory(info);
                kit.setTileFactory(tf);
                kit.setZoom(14);
                kit.setAddressLocation(new GeoPosition(51.5, 0));
                kit.getMainMap().setDrawTileBorders(true);
                kit.getMainMap().setRestrictOutsidePanning(true);
                kit.getMainMap().setHorizontalWrapped(false);

                ((DefaultTileFactory) kit.getMainMap().getTileFactory()).setThreadPoolSize(8);
                JFrame frame = new JFrame("JXMapKit test");
                frame.add(kit);
                frame.pack();
                frame.setSize(500, 300);
                frame.setVisible(true);
            }
        });
    }
}
package org.jxmapviewer.viewer;

import java.io.Serializable;


public class GeoPosition implements Serializable {

    private double latitude;
    private double longitude;

    
    public GeoPosition(double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

   
    public GeoPosition(double[] coords) {
        this.latitude = coords[0];
        this.longitude = coords[1];
    }

    
    public GeoPosition(int latDegrees, int latMinutes, int latSeconds,
            int lonDegrees, int lonMinutes, int lonSeconds) {
        this(latDegrees + (latMinutes + latSeconds / 60.0) / 60.0,
                lonDegrees + (lonMinutes + lonSeconds / 60.0) / 60.0);
    }

    
    public double getLatitude() {
        return latitude;
    }

    
    public double getLongitude() {
        return longitude;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        long temp;
        temp = Double.doubleToLongBits(latitude);
        result = prime * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(longitude);
        result = prime * result + (int) (temp ^ (temp >>> 32));
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof GeoPosition)) {
            return false;
        }
        GeoPosition other = (GeoPosition) obj;
        if (Double.doubleToLongBits(latitude)
                != Double.doubleToLongBits(other.latitude)) {
            return false;
        }
        if (Double.doubleToLongBits(longitude)
                != Double.doubleToLongBits(other.longitude)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "[" + latitude + ", " + longitude + "]";
    }
}
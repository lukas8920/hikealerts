package org.hikingdev.microsoft_hackathon.util.geodata;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKBReader;
import org.locationtech.jts.io.WKTWriter;
import org.locationtech.jts.linearref.LengthIndexedLine;

import java.util.ArrayList;
import java.util.List;

public class Math {
    private static final WKBReader wkbReader = new WKBReader();
    private static final WKTWriter wktWriter = new WKTWriter();

    public static String convertByteArrayToString(byte[] coordinates) throws ParseException {
        Geometry geometry = wkbReader.read(coordinates);
        return wktWriter.write(geometry);
    }

    public static Coordinate determineMid(LineString line){
        LengthIndexedLine indexedLine = new LengthIndexedLine(line);
        double midLength = line.getLength() / 2;
        return indexedLine.extractPoint(midLength);
    }

    public static LineString convertToWGS84(Geometry geometry){
        List<Coordinate> transformedCoords = new ArrayList<>();
        for (Coordinate coord : geometry.getCoordinates()) {
            Coordinate transformedCoordinate = convertCoordinateToWGS84(coord);
            transformedCoords.add(transformedCoordinate);
        }
        return new GeometryFactory().createLineString(transformedCoords.toArray(new Coordinate[0]));
    }

    public static Coordinate convertCoordinateToWGS84(Coordinate coordinate){
        double x = mercToLon(coordinate.x);
        double y = mercToLat(coordinate.y);
        return new Coordinate(x, y);
    }

    public static LineString convertLinestringToEPSG3857(Geometry geometry){
        List<Coordinate> transformedCoords = new ArrayList<>();
        for (Coordinate coord : geometry.getCoordinates()) {
            Coordinate transformedCoordinate = convertCoordinateToEPSG3857(coord);
            transformedCoords.add(transformedCoordinate);
        }
        return new GeometryFactory().createLineString(transformedCoords.toArray(new Coordinate[0]));
    }

    public static Coordinate convertCoordinateToEPSG3857(Coordinate coordinate){
        double x = lonToMerc(coordinate.x);
        double y = latToMerc(coordinate.y);
        return new Coordinate(x, y);
    }

    public static double[] transformBbox(double[] bbox){
        return new double[]{
                lonToMerc(bbox[0]),
                latToMerc(bbox[1]),
                lonToMerc(bbox[2]),
                latToMerc(bbox[3])
        };
    }

    // Convert longitude to EPSG:3857
    private static double lonToMerc(double lon) {
        return lon * 20037508.34 / 180;
    }

    // Convert latitude to EPSG:3857
    private static double latToMerc(double lat) {
        double rad = java.lang.Math.toRadians(lat);
        return java.lang.Math.log(java.lang.Math.tan(java.lang.Math.PI / 4 + rad / 2)) * 6378137;
    }

    // Convert EPSG:3857 X (Mercator) to WGS84
    public static double mercToLon(double x) {
        return x * 180 / 20037508.34;
    }

    // Convert EPSG:3857 Y (Mercator) to WGS84
    public static double mercToLat(double y) {
        double rad = java.lang.Math.PI / 2 - 2 * java.lang.Math.atan(java.lang.Math.exp(-y / 6378137));
        return java.lang.Math.toDegrees(rad);
    }
}

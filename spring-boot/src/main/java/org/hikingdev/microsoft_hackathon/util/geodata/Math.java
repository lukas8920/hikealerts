package org.hikingdev.microsoft_hackathon.util.geodata;

import org.locationtech.jts.geom.*;
import org.locationtech.jts.geom.impl.CoordinateArraySequence;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKBReader;
import org.locationtech.jts.io.WKTWriter;
import org.locationtech.jts.linearref.LengthIndexedLine;

import java.util.ArrayList;
import java.util.List;

public class Math {
    private static final WKBReader wkbReader = new WKBReader();
    private static final WKTWriter wktWriter = new WKTWriter();
    private static final GeometryFactory geometryFactory = new GeometryFactory();

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

    public static boolean isValidWGS84(LineString lineString) {
        if (lineString == null || lineString.isEmpty()) {
            return false;
        }

        for (Coordinate coord : lineString.getCoordinates()) {
            double lon = coord.x;
            double lat = coord.y;

            if (lon < -180 || lon > 180 || lat < -90 || lat > 90) {
                return false;
            }
        }

        return true;
    }

    public static boolean isValidEPSG3857(LineString lineString){
        for (Coordinate coord : lineString.getCoordinates()) {
            if (java.lang.Math.abs(coord.x) > 20037508.34 || java.lang.Math.abs(coord.y) > 20037508.34) {
                return false;
            }
        }
        return true;
    }

    public static LineString joinLineStrings(LineString line1, LineString line2){
        Coordinate[] mergedCoords = new Coordinate[line1.getNumPoints() + line2.getNumPoints()];

        boolean removeDuplicate = line1.getEndPoint() != null && line1.getEndPoint().getCoordinate().equals2D(line2.getStartPoint().getCoordinate());

        int offset = line1.getNumPoints();
        if (removeDuplicate) {
            mergedCoords = new Coordinate[line1.getNumPoints() + line2.getNumPoints() - 1];
        }

        System.arraycopy(line1.getCoordinates(), 0, mergedCoords, 0, offset);
        System.arraycopy(line2.getCoordinates(), removeDuplicate ? 1 : 0, mergedCoords, offset, line2.getNumPoints() - (removeDuplicate ? 1 : 0));

        CoordinateSequence seq = new CoordinateArraySequence(mergedCoords);
        return new LineString(seq, geometryFactory);
    }

    public static LineString voidLineString(){
        Coordinate[] coordinates = new Coordinate[]{};
        CoordinateSequence seq = new CoordinateArraySequence(coordinates);
        return new LineString(seq, geometryFactory);
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

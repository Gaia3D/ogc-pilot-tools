package com.gaia3d;

import lombok.extern.slf4j.Slf4j;
import org.geotools.data.geojson.GeoJSONDataStoreFactory;
import org.geotools.data.geojson.GeoJSONWriter;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.feature.DefaultFeatureCollection;
import org.geotools.feature.simple.SimpleFeatureImpl;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.geotools.filter.identity.FeatureIdImpl;
import org.geotools.geojson.GeoJSON;
import org.geotools.geojson.feature.FeatureJSON;
import org.geotools.geojson.geom.GeometryJSON;
import org.geotools.referencing.CRS;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.joml.Vector2d;
import org.joml.Vector3d;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Polygon;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import java.io.*;
import java.util.List;

@Slf4j
public class GeojsonWriter {

    public void write(GaiaGMLObject object, File file) {
        log.info("Writing Geojson file: " + file.getAbsolutePath());

        try {
            BufferedOutputStream fileOutputStream = new BufferedOutputStream(new FileOutputStream(file));
            GeoJSONWriter writer = new GeoJSONWriter(fileOutputStream);
            //writer.setEncodeFeatureCollectionCRS(false);

            DefaultFeatureCollection collection = new DefaultFeatureCollection();
            List<Gaia2DPolygon> polygons = object.getPolygons();
            for (Gaia2DPolygon polygon : polygons) {
                SimpleFeature feature = createFeature(polygon);
                collection.add(feature);
            }

            for (Gaia3DPolyline polyline : object.getPolylines()) {
                SimpleFeature feature = createFeature(polyline);
                collection.add(feature);
            }

//            writer.writeFeatureCollection(collection);
//            writer.close();

            // FeatureJSON을 사용하여 CRS 포함
            FeatureJSON featureJSON = new FeatureJSON();
            featureJSON.setEncodeFeatureCollectionCRS(true);
            featureJSON.setFeatureType(collection.getSchema());
            featureJSON.setEncodeFeatureCollectionBounds(true);
            featureJSON.writeFeatureCollection(collection, fileOutputStream);
            fileOutputStream.flush();
            fileOutputStream.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    public void writeOld(GaiaGMLObject object, File file) {
        log.info("Writing Geojson file: " + file.getAbsolutePath());

        try {
            BufferedOutputStream fileOutputStream = new BufferedOutputStream(new FileOutputStream(file));
            GeoJSONWriter writer = new GeoJSONWriter(fileOutputStream);
            writer.setEncodeFeatureCollectionCRS(true);

            // 1. GeometryFactory 생성
            GeometryFactory geometryFactory = new GeometryFactory();

            // 2. Polygon 좌표 생성 (예제: 사각형)
            Coordinate[] coordinates = new Coordinate[]{
                    new Coordinate(127.0, 37.0),
                    new Coordinate(128.0, 37.0),
                    new Coordinate(128.0, 38.0),
                    new Coordinate(127.0, 38.0),
                    new Coordinate(127.0, 37.0) // 폐곡선 (시작점 == 끝점)
            };

            // 3. Polygon 객체 생성
            Polygon polygon = geometryFactory.createPolygon(coordinates);
            Geometry geom = (Geometry) polygon;

            String id = "1";

            SimpleFeatureTypeBuilder builder = new SimpleFeatureTypeBuilder();
            builder.setName("Polygon");
            builder.setCRS(DefaultGeographicCRS.WGS84);
            builder.add("geometry", Polygon.class);
            SimpleFeatureType featureType = builder.buildFeatureType();

            FeatureIdImpl featureId = new FeatureIdImpl(id);
            List<Object> polygons = List.of(polygon);
            SimpleFeature feature = new SimpleFeatureImpl(polygons, featureType, featureId);
            //feature.setAttribute("geometry", geom);

            GeometryJSON geometryJSON = new GeometryJSON();
            geometryJSON.write(polygon, System.out);


            DefaultFeatureCollection featureCollection = new DefaultFeatureCollection(id, featureType);
            featureCollection.add(feature);

            SimpleFeatureCollection collection = featureCollection;

            writer.writeFeatureCollection(collection);
            writer.close();

        } catch (IOException e) {
            throw new RuntimeException(e);
        }


        List<Gaia2DPolygon> polygons = object.getPolygons();
        for (Gaia2DPolygon polygon : polygons) {
            log.info("Writing polygon: " + polygon.getName());


        }
    }

    private SimpleFeature createFeature(Gaia2DPolygon polygon) {
        List<Vector2d> exteriorRing =  polygon.getExteriorRing();
        int size = exteriorRing.size();

        Coordinate[] coordinates = new Coordinate[size];
        for (int i = 0; i < size; i++) {
            Vector2d vector = exteriorRing.get(i);
            coordinates[i] = new Coordinate(vector.x, vector.y);
        }

        GeometryFactory geometryFactory = new GeometryFactory();
        Polygon jtsPolygon = geometryFactory.createPolygon(coordinates);

        CoordinateReferenceSystem crs = DefaultGeographicCRS.WGS84;
        try {
            crs = CRS.decode("EPSG:5186");
        } catch (FactoryException e) {
            throw new RuntimeException(e);
        }

        SimpleFeatureTypeBuilder builder = new SimpleFeatureTypeBuilder();
        builder.setName("Polygon");
        builder.setCRS(crs);
        builder.add("geometry", Polygon.class);
        SimpleFeatureType featureType = builder.buildFeatureType();

        FeatureIdImpl featureId = new FeatureIdImpl(polygon.getName());
        List<Object> polygons = List.of(jtsPolygon);
        return new SimpleFeatureImpl(polygons, featureType, featureId);
    }

    private SimpleFeature createFeature(Gaia3DPolyline polyline) {
        List<Vector3d> points = polyline.getExteriorRing();
        int size = points.size();

        Coordinate[] coordinates = new Coordinate[size];
        for (int i = 0; i < size; i++) {
            Vector3d vector = points.get(i);
            coordinates[i] = new Coordinate(vector.x, vector.y, vector.z);
        }
        log.info("Polyline: " + polyline.getName() + ", size: " + size);

        GeometryFactory geometryFactory = new GeometryFactory();
        org.locationtech.jts.geom.LineString jtsLineString = geometryFactory.createLineString(coordinates);

        CoordinateReferenceSystem crs = DefaultGeographicCRS.WGS84;
        try {
            crs = CRS.decode("EPSG:5186");
        } catch (FactoryException e) {
            throw new RuntimeException(e);
        }

        SimpleFeatureTypeBuilder builder = new SimpleFeatureTypeBuilder();
        builder.setName("Polyline");
        builder.setCRS(crs);
        builder.add("geometry", org.locationtech.jts.geom.LineString.class);
        SimpleFeatureType featureType = builder.buildFeatureType();

        FeatureIdImpl featureId = new FeatureIdImpl(polyline.getName());
        List<Object> polylines = List.of(jtsLineString);
        return new SimpleFeatureImpl(polylines, featureType, featureId);
    }
}

package com.gaia3d.converter.geometry.geopackage;

import com.gaia3d.command.mago.GlobalOptions;
import com.gaia3d.converter.kml.AttributeReader;
import com.gaia3d.converter.kml.KmlInfo;
import com.gaia3d.util.GlobeUtils;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.geotools.data.Transaction;
import org.geotools.data.simple.SimpleFeatureReader;
import org.geotools.geopkg.FeatureEntry;
import org.geotools.geopkg.GeoPackage;
import org.joml.Vector3d;
import org.locationtech.jts.geom.*;
import org.locationtech.proj4j.CoordinateReferenceSystem;
import org.locationtech.proj4j.ProjCoordinate;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.type.FeatureType;
import org.opengis.feature.type.PropertyDescriptor;
import org.opengis.filter.Filter;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * KmlReader is a class that reads kml files.
 * It reads kml files and returns the information of the kml file.
 */
@Slf4j
@NoArgsConstructor
public class GeoPackageInstanceConverter implements AttributeReader {

    //read kml file
    @Override
    public KmlInfo read(File file) {
        log.error("ShapePointReader read method is not implemented yet.");
        return null;
    }

    @Override
    public List<KmlInfo> readAll(File file) {
        GlobalOptions globalOptions = GlobalOptions.getInstance();

        List<KmlInfo> result = new ArrayList<>();
        boolean isDefaultCrs = globalOptions.getCrs().equals(GlobalOptions.DEFAULT_CRS);
        String altitudeColumnName = globalOptions.getAltitudeColumn();
        String headingColumnName = globalOptions.getHeadingColumn();
        int instancePolygonContainsPointCounts = GlobalOptions.INSTANCE_POLYGON_CONTAINS_POINT_COUNTS;

        GeoPackage geoPackage = null;
        try {
            geoPackage = new GeoPackage(file);
            List<FeatureEntry> features = geoPackage.features();
            for (FeatureEntry featureEntry : features) {
                Filter filter = Filter.INCLUDE;
                Transaction transaction = Transaction.AUTO_COMMIT;
                SimpleFeatureReader simpleFeatureReader = geoPackage.reader(featureEntry, filter, transaction);
                while (simpleFeatureReader.hasNext()) {
                    SimpleFeature feature = simpleFeatureReader.next();
                    Geometry geom = (Geometry) feature.getDefaultGeometry();

                    List<Point> points = new ArrayList<>();
                    if (geom instanceof MultiPolygon multiPolygon) {
                        for (int i = 0; i < multiPolygon.getNumGeometries(); i++) {
                            Polygon polygon = (Polygon) multiPolygon.getGeometryN(i);
                            points.addAll(getRandomContainsPoints(polygon, geom.getFactory(), instancePolygonContainsPointCounts));
                        }
                    } else if (geom instanceof Polygon polygon) {
                        points.addAll(getRandomContainsPoints(polygon, geom.getFactory(), instancePolygonContainsPointCounts));
                    } else if (geom instanceof MultiPoint) {
                        GeometryFactory factory = geom.getFactory();
                        Coordinate[] coordinates = geom.getCoordinates();
                        for (Coordinate coordinate : coordinates) {
                            Point point = factory.createPoint(coordinate);
                            points.add(point);
                        }
                    } else if (geom instanceof Point point) {
                        points.add(point);
                    } else {
                        log.error("Geometry type is not supported.");
                        continue;
                    }

                    for (Point point : points) {
                        Map<String, String> attributes = new HashMap<>();
                        FeatureType featureType = feature.getFeatureType();
                        Collection<PropertyDescriptor> featureDescriptors = featureType.getDescriptors();
                        AtomicInteger index = new AtomicInteger(0);
                        featureDescriptors.forEach(attributeDescriptor -> {
                            Object attribute = feature.getAttribute(index.getAndIncrement());
                            if (attribute instanceof Geometry) {
                                return;
                            }
                            String attributeString = castStringFromObject(attribute, "null");
                            attributes.put(attributeDescriptor.getName().getLocalPart(), attributeString);
                        });

                        double x = point.getX();
                        double y = point.getY();
                        double heading = getNumberAttribute(feature, headingColumnName, 0.0d);
                        double altitude = getNumberAttribute(feature, altitudeColumnName, 0.0d);

                        Vector3d position;
                        CoordinateReferenceSystem crs = globalOptions.getCrs();
                        if (crs != null) {
                            ProjCoordinate projCoordinate = new ProjCoordinate(x, y, 0.0d);
                            ProjCoordinate centerWgs84 = GlobeUtils.transform(crs, projCoordinate);
                            position = new Vector3d(centerWgs84.x, centerWgs84.y, altitude);
                        } else {
                            position = new Vector3d(x, y, altitude);
                        }

                        KmlInfo kmlInfo = KmlInfo.builder()
                                .name("I3dmFromGeoPackage")
                                .position(position)
                                .heading(heading)
                                .tilt(0.0d)
                                .roll(0.0d)
                                .scaleX(1.0d)
                                .scaleY(1.0d)
                                .scaleZ(1.0d)
                                .properties(attributes)
                                .build();
                        result.add(kmlInfo);
                    }
                }
            }
            geoPackage.close();
        } catch (IOException e) {
            if (geoPackage != null) {
                geoPackage.close();
            }
            log.error("Error : ", e);
            throw new RuntimeException(e);
        }
        return result;
    }

    private double getNumberAttribute(SimpleFeature feature, String column, double defaultValue) {
        double result = defaultValue;
        Object attributeLower = feature.getAttribute(column);
        Object attributeUpper = feature.getAttribute(column.toUpperCase());
        Object attributeObject = null;
        if (attributeLower != null) {
            attributeObject = attributeLower;
        } else if (attributeUpper != null) {
            attributeObject = attributeUpper;
        }

        if (attributeObject instanceof Short) {
            result = result + (short) attributeObject;
        } else if (attributeObject instanceof Integer) {
            result = result + (int) attributeObject;
        } else if (attributeObject instanceof Long) {
            result = result + (Long) attributeObject;
        } else if (attributeObject instanceof Double) {
            result = result + (double) attributeObject;
        } else if (attributeObject instanceof String) {
            result = Double.parseDouble((String) attributeObject);
        }
        return result;
    }

    private String castStringFromObject(Object object, String defaultValue) {
        String result;
        if (object == null) {
            result = defaultValue;
        } else if (object instanceof String) {
            result = (String) object;
        } else if (object instanceof Integer) {
            result = String.valueOf((int) object);
        } else if (object instanceof Long) {
            result = String.valueOf(object);
        } else if (object instanceof Double) {
            result = String.valueOf((double) object);
        } else if (object instanceof Short) {
            result = String.valueOf((short) object);
        } else {
            result = object.toString();
        }
        return result;
    }
}

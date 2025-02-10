package com.gaia3d;

import com.gaia3d.basic.geometry.GaiaBoundingBox;
import lombok.extern.slf4j.Slf4j;
import org.citygml4j.core.model.common.GeometryInfo;
import org.citygml4j.core.model.core.AbstractCityObject;
import org.citygml4j.core.model.core.AbstractCityObjectProperty;
import org.citygml4j.core.model.core.CityModel;
import org.citygml4j.xml.CityGMLContext;
import org.citygml4j.xml.CityGMLContextException;
import org.citygml4j.xml.reader.CityGMLInputFactory;
import org.citygml4j.xml.reader.CityGMLReadException;
import org.joml.Vector2d;
import org.xmlobjects.gml.model.geometry.DirectPositionList;
import org.xmlobjects.gml.model.geometry.GeometricPositionList;
import org.xmlobjects.gml.model.geometry.GeometryProperty;
import org.xmlobjects.gml.model.geometry.aggregates.MultiCurve;
import org.xmlobjects.gml.model.geometry.primitives.*;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class GaiaGMLReader {

    public GaiaGMLObject read(File file) {
        GaiaGMLObject cityGMLObject = new GaiaGMLObject();
        List<Gaia2DPolygon> gaia2DPolygons = new ArrayList<>();
        cityGMLObject.setPolygons(gaia2DPolygons);

        try {
            CityGMLContext context = CityGMLContext.newInstance();
            CityGMLInputFactory factory = context.createCityGMLInputFactory();
            org.citygml4j.xml.reader.CityGMLReader reader = factory.createCityGMLReader(file);

            while (reader.hasNext()) {
                CityModel cityModel = (CityModel) reader.next();
                List<AbstractCityObjectProperty> cityObjectMembers = cityModel.getCityObjectMembers();
                for (AbstractCityObjectProperty cityObjectProperty : cityObjectMembers) {
                    AbstractCityObject cityObject = cityObjectProperty.getObject();
                    log.info("[CityObject] " + cityObject.getId());

                    GeometryInfo geometryInfo = cityObject.getGeometryInfo();
                    log.info("[CityObject][GeometryInfo] " + geometryInfo.getGeometries().size());
                    List<GeometryProperty<?>> geometries = geometryInfo.getGeometries();
                    for (GeometryProperty<?> geometry : geometries) {
                        log.info("[CityObject][GeometryInfo][Geometry] " + geometry.getType());

                        if (geometry.getObject() instanceof Solid) {
                            Solid solid = (Solid) geometry.getObject();
                            log.info("[CityObject][GeometryInfo][Geometry][Solid] " + solid);

                            ShellProperty exterior = solid.getExterior();
                            if (exterior != null) {
                                log.info("[CityObject][GeometryInfo][Geometry][Solid][Exterior] " + exterior);
                                Shell shell = exterior.getObject();

                                List<Polygon> polygons = extractPolygons(shell);
                                GaiaBoundingBox boundingBox = calculateBoundingBox(polygons);

                                Gaia2DPolygon gaia2DPolygon = extractFloorPolygon(boundingBox, polygons);
                                gaia2DPolygon.setName(cityObject.getId());
                                gaia2DPolygons.add(gaia2DPolygon);
                            }
                        } else if (geometry.getObject() instanceof MultiCurve) {
                            log.info("[CityObject][GeometryInfo][Geometry][MultiCurve] " + geometry);
                            MultiCurve multiCurve = (MultiCurve) geometry.getObject();
                            List<CurveProperty> curveMembers = multiCurve.getCurveMember();
                            for (CurveProperty curveProperty : curveMembers) {
                                AbstractCurve curve = curveProperty.getObject();
                                if (curve instanceof LineString) {
                                    LineString lineString = (LineString) curve;
                                    GeometricPositionList posList = lineString.getControlPoints();
                                    DirectPositionList directPositionList = posList.getPosList();
                                    List<Double> xyzPositions = directPositionList.getValue();
                                    int directPositionSize = xyzPositions.size() / 3;

                                    List<Vector2d> points = new ArrayList<>();
                                    for (int i = 0; i < directPositionSize; i++) {
                                        double x = xyzPositions.get(i * 3);
                                        double y = xyzPositions.get(i * 3 + 1);
                                        double z = xyzPositions.get(i * 3 + 2);

                                        log.info("[CityObject][GeometryInfo][Geometry][MultiCurve][LineString][DirectPosition] " + x + ", " + y + ", " + z);
                                        points.add(new Vector2d(x, y));
                                    }

                                    Gaia2DPolygon gaia2DPolygon = new Gaia2DPolygon();
                                    gaia2DPolygon.getExteriorRing().addAll(points);
                                    gaia2DPolygon.setHeight(0);
                                    gaia2DPolygon.setAltitude(0);
                                    gaia2DPolygon.setName(cityObject.getId());
                                    gaia2DPolygons.add(gaia2DPolygon);
                                }
                            }
                        }
                    }
                }
            }
        } catch (CityGMLContextException | CityGMLReadException e) {
            log.error("Error reading CityGML file", e);
            throw new RuntimeException(e);
        }

        return cityGMLObject;
    }

    public GaiaGMLObject readOld(File file) {
        GaiaGMLObject cityGMLObject = new GaiaGMLObject();
        try {
            CityGMLContext context = CityGMLContext.newInstance();
            CityGMLInputFactory factory = context.createCityGMLInputFactory();
            org.citygml4j.xml.reader.CityGMLReader reader = factory.createCityGMLReader(file);

            while (reader.hasNext()) {
                CityModel cityModel = (CityModel) reader.next();
                List<AbstractCityObjectProperty> cityObjectMembers = cityModel.getCityObjectMembers();
                for (AbstractCityObjectProperty cityObjectProperty : cityObjectMembers) {
                    AbstractCityObject cityObject = cityObjectProperty.getObject();
                    log.info("[CityObject] " + cityObject.getId());

                    GeometryInfo geometryInfo = cityObject.getGeometryInfo();
                    log.info("[CityObject][GeometryInfo] " + geometryInfo.getGeometries().size());
                    List<GeometryProperty<?>> geometries = geometryInfo.getGeometries();
                    for (GeometryProperty<?> geometry : geometries) {
                        log.info("[CityObject][GeometryInfo][Geometry] " + geometry.getType());

                        if (geometry.getObject() instanceof Solid) {
                            Solid solid = (Solid) geometry.getObject();
                            log.info("[CityObject][GeometryInfo][Geometry][Solid] " + solid);

                            ShellProperty exterior = solid.getExterior();
                            if (exterior != null) {
                                log.info("[CityObject][GeometryInfo][Geometry][Solid][Exterior] " + exterior);
                                Shell shell = exterior.getObject();
                                shell.getSurfaceMembers().forEach(surfaceMember -> {
                                    log.info("[CityObject][GeometryInfo][Geometry][Solid][Exterior][Shell][SurfaceMember] " + surfaceMember);
                                    AbstractSurface surface = surfaceMember.getObject();
                                    if (surfaceMember.getObject() instanceof Polygon) {
                                        Polygon polygon = (Polygon) surface;
                                        log.info("[CityObject][GeometryInfo][Geometry][Solid][Exterior][Shell][SurfaceMember][Polygon] " + polygon);
                                        AbstractRingProperty exteriorRing = polygon.getExterior();
                                        LinearRing ring = (LinearRing) exteriorRing.getObject();
                                        GeometricPositionList posList = ring.getControlPoints();
                                        DirectPositionList directPositionList = posList.getPosList();
                                        List<Double> xyzPositions = directPositionList.getValue();
                                        int directPositionSize = xyzPositions.size() / 3;

                                        /* calculate the bounding box */
                                        GaiaBoundingBox boundingBox = new GaiaBoundingBox();
                                        for (int i = 0; i < directPositionSize; i++) {
                                            double x = xyzPositions.get(i * 3);
                                            double y = xyzPositions.get(i * 3 + 1);
                                            double z = xyzPositions.get(i * 3 + 2);
                                            boundingBox.addPoint(x, y, z);
                                        }

                                        List<Vector2d> points = new ArrayList<>();
                                        for (int i = 0; i < directPositionSize; i++) {
                                            double x = xyzPositions.get(i * 3);
                                            double y = xyzPositions.get(i * 3 + 1);
                                            double z = xyzPositions.get(i * 3 + 2);

                                            log.info("[CityObject][GeometryInfo][Geometry][Solid][Exterior][Shell][SurfaceMember][Polygon][Exterior][LinearRing][DirectPosition] " + x + ", " + y + ", " + z);
                                            boundingBox.addPoint(x, y, z);
                                        }

                                        // Set the bounding box of the cityGMLObject


                                    }
                                });
                            }
                        }
                    }

                }
            }
        } catch (CityGMLContextException | CityGMLReadException e) {
            log.error("Error reading CityGML file", e);
            throw new RuntimeException(e);
        }
        return cityGMLObject;
    }

    private List<Polygon> extractPolygons(Shell shell) {
        List<Polygon> polygons = new ArrayList<>();
        shell.getSurfaceMembers().forEach(surfaceMember -> {
            AbstractSurface surface = surfaceMember.getObject();
            if (surface instanceof Polygon) {
                Polygon polygon = (Polygon) surface;
                polygons.add(polygon);
            }
        });
        return polygons;
    }

    private GaiaBoundingBox calculateBoundingBox(List<Polygon> polygons) {
        GaiaBoundingBox boundingBox = new GaiaBoundingBox();

        for (Polygon polygon : polygons) {
            AbstractRingProperty exteriorRing = polygon.getExterior();
            LinearRing ring = (LinearRing) exteriorRing.getObject();
            GeometricPositionList posList = ring.getControlPoints();
            DirectPositionList directPositionList = posList.getPosList();
            List<Double> xyzPositions = directPositionList.getValue();
            int directPositionSize = xyzPositions.size() / 3;
            for (int i = 0; i < directPositionSize; i++) {
                double x = xyzPositions.get(i * 3);
                double y = xyzPositions.get(i * 3 + 1);
                double z = xyzPositions.get(i * 3 + 2);
                boundingBox.addPoint(x, y, z);
            }
        }
        return boundingBox;
    }

    private Gaia2DPolygon extractFloorPolygon(GaiaBoundingBox boundingBox, List<Polygon> polygons) throws RuntimeException {
        double minZ = boundingBox.getMinZ();
        for (Polygon polygon : polygons) {
            AbstractRingProperty exteriorRing = polygon.getExterior();
            LinearRing ring = (LinearRing) exteriorRing.getObject();
            GeometricPositionList posList = ring.getControlPoints();
            DirectPositionList directPositionList = posList.getPosList();
            List<Double> xyzPositions = directPositionList.getValue();

            List<Vector2d> floor = new ArrayList<>();
            int directPositionSize = xyzPositions.size() / 3;
            for (int i = 0; i < directPositionSize; i++) {
                double x = xyzPositions.get(i * 3);
                double y = xyzPositions.get(i * 3 + 1);
                double z = xyzPositions.get(i * 3 + 2);
                if (z == minZ) {
                    floor.add(new Vector2d(x, y));
                }
            }

            if (floor.size() == directPositionSize) {
                Gaia2DPolygon gaia2DPolygon = new Gaia2DPolygon();
                gaia2DPolygon.getExteriorRing().addAll(floor);
                gaia2DPolygon.setHeight(boundingBox.getMaxZ() - boundingBox.getMinZ());
                gaia2DPolygon.setAltitude(boundingBox.getMinZ());
                return gaia2DPolygon;
            }
        }
        throw new RuntimeException("Failed to extract floor");
    }
}

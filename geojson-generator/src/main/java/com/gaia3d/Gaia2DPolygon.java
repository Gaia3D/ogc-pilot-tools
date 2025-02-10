package com.gaia3d;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.joml.Vector2d;

import java.util.*;

@Slf4j
@Getter
@Setter
@RequiredArgsConstructor
public class Gaia2DPolygon {
    private String name = "unnamed";
    private double height = 0.0d;
    private double altitude = 0.0d;
    private final Map<String, String> attributes = new HashMap<>();
    private final List<List<Vector2d>> interiorRings = new ArrayList<>();
    private final List<Vector2d> exteriorRing = new ArrayList<>();
}

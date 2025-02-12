package com.gaia3d;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.joml.Vector3d;

import java.util.*;

@Slf4j
@Getter
@Setter
@RequiredArgsConstructor
public class Gaia3DPolyline {
    private String name = "unnamed";
    private double height = 0.0d;
    private double altitude = 0.0d;
    private final Map<String, Object> attributes = new LinkedHashMap<>();
    private final List<Vector3d> exteriorRing = new ArrayList<>();
}

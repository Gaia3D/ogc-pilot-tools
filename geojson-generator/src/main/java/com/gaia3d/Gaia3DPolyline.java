package com.gaia3d;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.joml.Vector3d;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Getter
@Setter
@RequiredArgsConstructor
public class Gaia3DPolyline {
    private String name = "unnamed";
    private double height = 0.0d;
    private double altitude = 0.0d;
    private final Map<String, String> attributes = new HashMap<>();
    private final List<Vector3d> exteriorRing = new ArrayList<>();
}

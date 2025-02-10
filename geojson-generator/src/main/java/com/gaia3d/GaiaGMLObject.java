package com.gaia3d;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
@Getter
@Setter
@RequiredArgsConstructor
public class GaiaGMLObject {
    private String name;
    private List<Gaia2DPolygon> polygons;
    private List<Gaia3DPolyline> polylines;
}

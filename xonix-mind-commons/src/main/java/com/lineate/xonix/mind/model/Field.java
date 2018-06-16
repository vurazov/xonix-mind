package com.lineate.xonix.mind.model;

import lombok.Data;

import java.util.Map;

@Data
public class Field {

    final int height;

    final int width;

    final Map<Point, Cell> cells; //ideally it should be immutable map but will be later

    public Field(int height, int width, Map<Point, Cell> cells) {
        this.height = height;
        this.width = width;
        this.cells = cells;
    }
}
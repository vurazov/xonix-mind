package com.lineate.xonix.mind.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import lombok.Value;

@Value
public class Player {
    ArrayList<Point> body; // mutable

    public Optional<Point> head() {
        return body.isEmpty()?
            Optional.empty():
            Optional.of(body.get(body.size() - 1));
    }

    public Optional<List<Point>> tail() {
        return body.isEmpty()?
            Optional.empty():
            Optional.of(body.subList(0, body.size() - 1));
    }

    public int size() {
        return body.size();
    }

    public boolean contains(Point point) {
        return body.contains(point);
    }

    public boolean isEmpty() {
        return body.isEmpty();
    }

    public void setOnlyHead(Point p) {
        body.clear();
        body.add(p);
    }

    public void addNewHead(Point p) {
        body.add(p);
    }

    public void makeBody(Point head, List<Point> rest) {
        if (rest.contains(head)) {
            body.clear();
            body.addAll(rest);
        } else {
            body.clear();
            body.addAll(rest);
            body.add(head);
        }
    }
}

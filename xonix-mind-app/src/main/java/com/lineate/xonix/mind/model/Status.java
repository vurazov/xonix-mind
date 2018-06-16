package com.lineate.xonix.mind.model;

public enum Status {
    New(0),
    Work(1),
    Finish(2),
    Failed(3);

    private int id;

    Status(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public static Status fromInt(int id) {
        for (Status status : values()) {
            if (status.getId() == id) {
                return status;
            }
        }
        throw new IllegalArgumentException("Invalid Status value: " + id);
    }

}
package com.lineate.xonix.mind.model;

import java.util.Objects;

public class Cell {
    private final CellType cellType;
    private final Integer botId;
    private static final Cell EMPTY = new Cell(CellType.EMPTY, -1);
    private static final Cell BORDER = new Cell(CellType.BORDER, -1);

    private Cell(CellType cellType, int botId) {
        this.cellType = cellType;
        this.botId = botId;
    }

    public static Cell empty() {
        return EMPTY;
    }

    public static Cell border() {
        return BORDER;
    }

    public static Cell owned(int pid) {
        return new Cell(CellType.OWNED, pid);
    }

    public boolean isEmpty() { return cellType == CellType.EMPTY; }

    public boolean isBorder() { return cellType == CellType.BORDER; }

    public boolean isOwned() { return cellType == CellType.OWNED; }

    public CellType getCellType() {
        return cellType;
    }

    public Integer getBotId() {
        return botId;
    }

    public Cell copy() {
        return new Cell(this.cellType, this.botId);
    }

    @Override
    public String toString() {
        switch (this.getCellType()) {
            case EMPTY:
                return "Empty";
            case BORDER:
                return "Border";
            case OWNED:
                return "Owned(" + this.getBotId() + ")";
        }
        return "";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Cell)) return false;
        Cell cell = (Cell) o;
        return getCellType() == cell.getCellType() &&
                Objects.equals(getBotId(), cell.getBotId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getCellType(), getBotId());
    }
}

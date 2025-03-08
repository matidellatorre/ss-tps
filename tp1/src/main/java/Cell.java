import java.util.Objects;

public class Cell {
    private final int x;
    private final int y;
    private final int number;

    public Cell(int x, int y) {
        this.x = x;
        this.y = y;
        this.number = this.x+1+Constants.m*this.y;

    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getNumber() {
        return number;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Cell cell = (Cell) o;
        return x == cell.x && y == cell.y && number == cell.number;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y, number);
    }

    @Override
    public String toString() {
        return "Cell{" +
                "x=" + x +
                ", y=" + y +
                ", number=" + number +
                '}';
    }
}

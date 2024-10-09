import java.util.List;

public class Result {
    private final double floodY;
    private final List<Pier> piers;
    private final GroundLine ground;

    Result(double flood, List<Pier> pier, GroundLine ground) {
        this.floodY = flood;
        this.piers = pier;
        this.ground = ground;
    }

    public double showFloodY() {
        return floodY;
    }
    public List<Pier> showPiers() {
        return piers;
    }
    public GroundLine showGround() {
        return ground;
    }
}

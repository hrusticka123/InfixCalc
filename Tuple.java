import java.util.HashMap;

public class Tuple {
    boolean okay;
    HashMap<String,Double> variables;

    public Tuple() {
        okay = true;
        variables = new HashMap<>();
    }

    public Tuple(boolean okay) {
        this.okay = okay;
    }
}
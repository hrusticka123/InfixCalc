import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class main {

    public static void main(String[] srgs) throws IOException {
        Calculator calc = new Calculator();
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        String line = br.readLine();
        while(line != null) {
            if (!line.isEmpty()) {
                calc.processExpr(line);
            }
            line = br.readLine();
        }
    }
}

import org.kabeja.parser.ParseException;
import java.io.FileNotFoundException;
import org.yaml.snakeyaml.Yaml;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Collections;


public class Main {
    public static void main(String[] args) throws FileNotFoundException, ParseException {
        InputStream inputStream = new FileInputStream("config.yaml");
        Yaml yaml = new Yaml();
        Map<String, Object> config = yaml.load(inputStream);
        String fileName = config.get("filePath").toString();
        Result data = Utils.dataLoader(fileName, config);
        List<Pier> pierList = data.showPiers();
        GroundLine ground = data.showGround();
        double floodY = data.showFloodY();
        Collections.sort(pierList, new Comparator<Pier>() {
            @Override
            public int compare(Pier o1, Pier o2) {
                return (int)(o1.xMin - o2.xMin);
            }
        });
        pierList = ground.isAbovePier(pierList);
        Result dataResult = Utils.changeCoordinate(pierList, ground);
        Utils.saveData(dataResult.showGround(), dataResult.showPiers(), config);
    }
}

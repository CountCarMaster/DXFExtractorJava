import org.kabeja.dxf.*;
import org.kabeja.parser.ParseException;
import org.kabeja.parser.Parser;
import org.kabeja.parser.ParserBuilder;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.*;
import java.io.PrintStream;

public class Utils {
    public static Result dataLoader(String fileName, Map<String, Object> config) throws FileNotFoundException, ParseException {
        Parser dxfParser = ParserBuilder.createDefaultParser();
        dxfParser.parse(new FileInputStream(fileName), "UTF-8");
        DXFDocument doc = dxfParser.getDocument();
        Iterator iter = doc.getDXFLayerIterator();
        double floodY = 0;
        List<Pier> piers = new ArrayList<>();
        GroundLine ground = null;

        while(iter.hasNext()) {
            DXFLayer layer = (DXFLayer) iter.next();
            String layerName = layer.getName();
            if (Objects.equals(layerName, config.get("floodLayerName"))) {
                floodY = loadFloodY(layer);
            }
            if (Objects.equals(layerName, config.get("pierLayerName"))) {
                piers = loadPier(layer);
            }
            if (Objects.equals(layerName, config.get("groundLayerName"))) {
                ground = loadGround(layer);
            }
        }
        List<Pier> tmp = new ArrayList<>();
        for(int i = 0; i < piers.size(); i++) {
            piers.get(i).fitFlood(floodY);
            if(piers.get(i).data.length > 0) tmp.add(piers.get(i));
        }
        ground.fit(floodY);
//        for(int i = 0; i < tmp.size(); i++){
//            System.out.println(tmp.get(i).data[0][0]);
//            System.out.println(tmp.get(i).data.length);
//        }
        List<Pier> tmp2 = new ArrayList<>();
        for(int i = 0; i < tmp.size(); i++) {
            boolean aa = tmp.get(i).fitGround(ground);
            if(aa) tmp2.add(tmp.get(i));
        }
//        for(int i = 0; i < tmp2.size(); i++){
//            System.out.println(tmp2.get(i).data[0][0]);
//            System.out.println(tmp2.get(i).data.length);
//        }
        Result res = new Result(floodY, tmp2, ground);
        return res;
    }

    private static double loadFloodY(DXFLayer layer) {
        List polyLines = layer.getDXFEntities(DXFConstants.ENTITY_TYPE_LWPOLYLINE);
        DXFLWPolyline polyLine = (DXFLWPolyline)polyLines.getFirst();
        double floodY = polyLine.getVertex(0).getY();
        return floodY;
    }

    private static List<Pier> loadPier(DXFLayer layer) {
        List polyLines = layer.getDXFEntities(DXFConstants.ENTITY_TYPE_LWPOLYLINE);
        int pierNum = polyLines.size();
        List<Pier> piers = new ArrayList<>();
        for(int i = 0; i < pierNum; i++) {
            DXFLWPolyline polyLine = (DXFLWPolyline)polyLines.get(i);
            int vertexNum = polyLine.getVertexCount();
            double[][] tmpPierData = new double[vertexNum][2];
            for(int j = 0; j < vertexNum; j++) {
                tmpPierData[j][0] = polyLine.getVertex(j).getX();
                tmpPierData[j][1] = polyLine.getVertex(j).getY();
            }
            Pier pier = new Pier(tmpPierData);
            piers.add(pier);
        }
        return piers;
    }

    private static GroundLine loadGround(DXFLayer layer) {
        List polyLines = layer.getDXFEntities(DXFConstants.ENTITY_TYPE_LWPOLYLINE);
        DXFLWPolyline polyLine = (DXFLWPolyline)polyLines.getFirst();
        int vertexNum = polyLine.getVertexCount();
        double[][] tmpGroundData = new double[vertexNum][2];
        for(int j = 0; j < vertexNum; j++) {
            tmpGroundData[j][0] = polyLine.getVertex(j).getX();
            tmpGroundData[j][1] = polyLine.getVertex(j).getY();
        }
        GroundLine ground = new GroundLine(tmpGroundData);
        return ground;
    }

    private static double min(double x, double y) {
        if(x > y) return y;
        return x;
    }

    public static Result changeCoordinate(List<Pier> pierList, GroundLine ground) {
        double xMinn = ground.xMin;
        double yMinn = ground.yMin;
        for(int i = 0; i < pierList.size(); i++) {
            xMinn = min(pierList.get(i).xMin, xMinn);
            yMinn = min(pierList.get(i).yMin, yMinn);
        }
        double [][] dataGround = new double[ground.data.length][2];
        for(int i = 0; i < ground.data.length; i++) {
            dataGround[i][0] = ground.data[i][0] - xMinn;
            dataGround[i][1] = ground.data[i][1] - yMinn;
        }

        List<Pier> pierData = new ArrayList<>();
        for(int i = 0; i < pierList.size(); i++) {
            double [][] pierTmp = new double[pierList.get(i).data.length][2];
            for(int j = 0; j < pierList.get(i).data.length; j++) {
                pierTmp[j][0] = pierList.get(i).data[j][0] - xMinn;
                pierTmp[j][1] = pierList.get(i).data[j][1] - yMinn;
            }
            pierData.add(new Pier(pierTmp));
        }
        return new Result(-1, pierData, new GroundLine(dataGround));
    }

    private static double calculateArea(double [][] coords) {
        int n = coords.length;
        double area = 0;
        int j = n - 1;
        for(int i = 0; i < n; i++) {
            area += (coords[j][0] + coords[i][0]) * (coords[j][1] - coords[i][1]);
            j = i;
        }
        return Math.abs(area / 2);
    }

    public static void saveData(GroundLine ground, List<Pier> pierList, Map<String, Object> config) throws FileNotFoundException {
        for(int i = 0; i < pierList.size(); i++) {
            if(!pierList.get(i).isMain) continue;
            for(int j = 0; j < pierList.size(); j++) {
                if(i == j) continue;
                if(!pierList.get(j).isMain) continue;
                if(pierList.get(i).xMin <= pierList.get(j).xMin && pierList.get(i).xMax >= pierList.get(j).xMax && pierList.get(i).yMin <= pierList.get(j).yMin && pierList.get(i).yMax >= pierList.get(j).yMax) {
                    pierList.get(i).fittings.add(pierList.get(j));
                    pierList.get(j).isMain = false;
                }
            }
        }
        String fileName = config.get("outputFileName").toString();
        String fileNameShorter = fileName.substring(0, fileName.length() - 8);
        PrintStream ps = new PrintStream(fileName);
        System.setOut(ps);
        System.out.println("{");
        System.out.println("\"type\": \"FeatureCollection\",");
        System.out.println("\"name\": \"" + fileNameShorter + "\",");
        System.out.println("\"crs\": { \"type\": \"name\", \"properties\": { \"name\": \"urn:ogc:def:crs:EPSG::3857\" } },");
        System.out.println("\"features\": [");

        // groundLine
        System.out.print("{ \"type\": \"Feature\", \"properties\": { \"name\": \"");
        System.out.print(config.get("groundOutputName").toString());
        System.out.print("\", \"type\": \"");
        System.out.print(config.get("groundType").toString());
        System.out.print("\", \"area\": ");
        System.out.print(calculateArea(ground.data));
        System.out.print(" }, \"geometry\": { \"type\": \"Polyline\", \"coordinates\": [ [ ");
        for(int i = 0; i < ground.data.length; i++) {
            System.out.print("[ " + ground.data[i][0] + ", " + ground.data[i][1] + " ]");
            if(i != ground.data.length - 1) System.out.print(", ");
        }
        System.out.println(" ] ] } },");
        // piers
        int pierNum = 0;
        int holeNum = 0;
        for(int i = 0; i < pierList.size(); i++) {
            if(pierList.get(i).isMain) {
                System.out.print("{ \"type\": \"Feature\", \"properties\": { \"name\": \"");
                System.out.print(config.get("pierOutputName").toString());
                System.out.print(++pierNum);
                System.out.print("\", \"type\": \"");
                System.out.print(config.get("pierType").toString());
                System.out.print("\", \"area\": ");
                double areaTmp = calculateArea(pierList.get(i).data);
                if(pierList.get(i).fittings.size() > 0) {
                    for(int j = 0; j < pierList.get(i).fittings.size(); j++) {
                        areaTmp -= calculateArea(pierList.get(i).fittings.get(j).data);
                    }
                }
                System.out.print(areaTmp);
                System.out.print(" }, \"geometry\": { \"type\": \"Polyline\", \"coordinates\": [ [ ");
                for(int j = 0; j < ground.data.length; j++) {
                    System.out.print("[ " + ground.data[j][0] + ", " + ground.data[j][1] + " ]");
                    if(j != ground.data.length - 1) System.out.print(", ");
                }
                System.out.println(" ] ] } },");
            }
            else {
                System.out.print("{ \"type\": \"Feature\", \"properties\": { \"name\": \"");
                System.out.print(config.get("holeOutputName").toString());
                System.out.print(++holeNum);
                System.out.print("\", \"type\": \"");
                System.out.print(config.get("pierType").toString());
                System.out.print("\", \"area\": ");
                System.out.print(calculateArea(pierList.get(i).data));
                System.out.print(" }, \"geometry\": { \"type\": \"Polyline\", \"coordinates\": [ [ ");
                for(int j = 0; j < ground.data.length; j++) {
                    System.out.print("[ " + ground.data[j][0] + ", " + ground.data[j][1] + " ]");
                    if(j != ground.data.length - 1) System.out.print(", ");
                }
                System.out.println(" ] ] } },");
            }
        }
        System.out.println("]");
        System.out.println("}");
    }
}
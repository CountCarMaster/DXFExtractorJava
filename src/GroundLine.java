import java.util.ArrayList;
import java.util.List;

public class GroundLine extends Entity {
    GroundLine(double [][] data) {
        super(data);
        int tmpLength = data.length;
        if(data[0][0] > data[tmpLength-1][0]) {
            double [][] tmpData = new double[tmpLength][2];
            for(int i = 0; i < tmpLength; i++) {
                tmpData[i][0] = data[tmpLength-i-1][0];
                tmpData[i][1] = data[tmpLength-i-1][1];
            }
            this.data = tmpData;
        }
    }

    public void fit(double floodY) {
        List<double[]> ans = new ArrayList<double[]>();
        for(int i = 0; i < data.length-1; i++) {
            if(data[i][1] >= floodY && data[i + 1][1] >= floodY) {
                continue;
            }
            else if(data[i][1] < floodY && data[i + 1][1] < floodY) {
                ans.add(data[i + 1]);
            }
            else if(data[i][1] >= floodY && data[i + 1][1] < floodY) {
                double tmp[] = new double[2];
                tmp[0] = data[i][0] + (data[i + 1][0] - data[i][0]) * ((data[i][1] - floodY) / (data[i][1] - data[i + 1][1]));
                tmp[1] = floodY;
                ans.add(tmp);
                ans.add(data[i + 1]);
            }
            else {
                double tmp[] = new double[2];
                tmp[0] = data[i][0] + (data[i + 1][0] - data[i][0]) * ((floodY - data[i][1]) / (data[i + 1][1] - data[i][1]));
                tmp[1] = floodY;
                ans.add(tmp);
            }
        }
        this.data = ans.toArray(new double[ans.size()][]);
    }

    private double min(double x, double y) {
        if(x < y) return x;
        else return y;
    }

    private double max(double x, double y) {
        if(x > y) return x;
        else return y;
    }

    public List<Pier> isAbovePier(List<Pier> pierList) {
        int dataLength = this.data.length;
        for(int i = 0; i < dataLength-1; i++) {
            int k = 0;
            for(int j = 0; j < pierList.size(); j++) {
                if(data[i][0] < pierList.get(j).xMin || data[i][0] > pierList.get(j).xMax) {
                    k += 1;
                    continue;
                }
                List<double[]> tmp = new ArrayList<>();
                for(int l = 0; l < pierList.get(j).data.length-1; l++) {
                    tmp.add(pierList.get(j).data[l]);
                    if(this.data[i][0] < min(pierList.get(j).data[l][0], pierList.get(j).data[l + 1][0]) || this.data[i][0] > max(pierList.get(j).data[l][0], pierList.get(j).data[l + 1][0])) {
                        continue;
                    }
                    double yy = pierList.get(j).data[l][1] + (pierList.get(j).data[l + 1][1] - pierList.get(j).data[l][1]) * ((this.data[i][0] - pierList.get(j).data[l][0]) / (pierList.get(j).data[l + 1][0] - pierList.get(j).data[l][0]));
                    if(yy < this.data[i][1]) {
                        tmp.add(this.data[i]);
                    }
                }
                tmp.add(pierList.get(j).data[pierList.get(j).data.length-1]);
                pierList.get(k).data = tmp.toArray(new double[tmp.size()][]);
                k += 1;
            }
        }
        return pierList;
    }
}

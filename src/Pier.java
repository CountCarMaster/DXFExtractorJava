import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class Pier extends Entity{
    public boolean isMain = true;
    public List<Pier> fittings = new ArrayList<>();
    Pier(double [][] data) {
        super(data);
        boolean isGround [] = new boolean[data.length];
    }

    public void fitFlood(double floodY) {
        List<double[]>ans = new ArrayList<double[]>();
        for(int i = 0; i < this.data.length-1; i++) {
            if(this.data[i][1] >= floodY && this.data[i + 1][1] >= floodY) {
                continue;
            }
            else if(this.data[i][1] < floodY && this.data[i + 1][1] < floodY) {
                ans.add(this.data[i]);
            }
            else if(this.data[i][1] >= floodY && this.data[i + 1][1] < floodY) {
                double[] tmp = new double[2];
                tmp[0] = this.data[i][0] + (this.data[i + 1][0] - this.data[i][0]) * ((floodY - this.data[i + 1][1]) / (this.data[i][1] - this.data[i + 1][1]));
                tmp[1] = floodY;
                ans.add(tmp);
            }
            else {
                ans.add(this.data[i]);
                double[] tmp = new double[2];
                tmp[0] = this.data[i][0] + (this.data[i + 1][0] - this.data[i][0]) * ((floodY - this.data[i][1]) / (this.data[i + 1][1] - this.data[i][1]));
                tmp[1] = floodY;
                ans.add(tmp);
            }
        }
        this.data = ans.toArray(new double[ans.size()][]);
    }

    private boolean isUnderGround(double x, double y, GroundLine ground) {
        int ans = -100;
        for(int i = 0; i < ground.data.length-1; i++) {
            if(x >= ground.data[i][0] && x <= ground.data[i + 1][0]) {
                ans = i;
                break;
            }
        }
        if(ans == -100) return true;
        double yy = ground.data[ans][1] + (ground.data[ans + 1][1] - ground.data[ans][1]) * ((x - ground.data[ans][0]) / (ground.data[ans + 1][0] - ground.data[ans][0]));
        if(y <= yy) return true;
        else return false;
    }

    private double [] lineIntersection(double[] line1, double[] line2) {
        double x1 = line1[0];
        double y1 = line1[1];
        double x2 = line1[2];
        double y2 = line1[3];
        double x3 = line2[0];
        double y3 = line2[1];
        double x4 = line2[2];
        double y4 = line2[3];
        double dx1 = x2 - x1;
        double dy1 = y2 - y1;
        double dx2 = x4 - x3;
        double dy2 = y4 - y3;

        double denominator = dx1 * dy2 - dy1 * dx2;

        if (denominator == 0) {
            return new double[]{-1, -1};
        }

        double dx3 = x3 - x1, dy3 = y3 - y1;
        double t1 = (dx3 * dy2 - dy3 * dx2) / denominator;
        double t2 = (dx3 * dy1 - dy3 * dx1) / denominator;

        if (0 <= t1 && t1 <= 1 && 0 <= t2 && t2 <= 1) {
            double intersection_x = x1 + t1 * dx1;
            double intersection_y = y1 + t1 * dy1;
            return new double[]{intersection_x, intersection_y};
        } else return new double[]{-1, -1};
    }

    public boolean fitGround(GroundLine ground) {
        List<List<double[]>> crossList = new LinkedList<>();
        boolean flag = false;
        List<double[]> tmp = new ArrayList<>();
        int hajime = -1000;
        for(int i = 0; i < this.data.length; i++) {
            if(!isUnderGround(this.data[i][0], this.data[i][1], ground)) {
                flag = true;
                break;
            }
        }
        if(!flag) return false;
        for(int i = 0; i < this.data.length; i++) {
            List<double[]> tmpList = new ArrayList<>();
            crossList.add(tmpList);
        }
        for(int i = 0; i < ground.data.length-1; i++) {
            double[] lineGround = new double[]{ground.data[i][0], ground.data[i][1], ground.data[i + 1][0], ground.data[i + 1][1]};
            for(int j = 0; j < this.data.length-1; j++) {
                double[] linePier = new double[]{this.data[j][0], this.data[j][1], this.data[j + 1][0], this.data[j + 1][1]};
                double[] crossAns = lineIntersection(lineGround, linePier);
                if(crossAns[0] == -1.0 && crossAns[1] == -1.0){
                    continue;
                }
                crossList.get(j).add(new double[]{i, crossAns[0], crossAns[1]});
            }
        }
        for(int i = 0; i < this.data.length; i++) {
            if(hajime < 0 && crossList.get(i).size() % 2 == 1) {
                hajime = (int)crossList.get(i).getLast()[0];
            }
            else if(hajime >= 0 && crossList.get(i).size() % 2 == 0 && !crossList.get(i).isEmpty()) {
                if(hajime < crossList.get(i).getFirst()[0]) {
                    for(int kk = hajime + 1; kk <= crossList.get(i).getFirst()[0]; kk++) {
                        tmp.add(ground.data[kk]);
                    }
                }
                else {
                    for(int kk = hajime + 1; kk > crossList.get(i).getFirst()[0] + 1; kk--) {
                        tmp.add(ground.data[kk]);
                    }
                }
                hajime = (int)crossList.get(i).getLast()[0];
            }
            else if(hajime >= 0 && crossList.get(i).size() % 2 == 1) {
                if(hajime < crossList.get(i).getFirst()[0]) {
                    for(int kk = hajime + 1; kk <= crossList.get(i).getFirst()[0]; kk++) {
                        tmp.add(ground.data[kk]);
                    }
                }
                else {
                    for(int kk = hajime + 1; kk > crossList.get(i).getFirst()[0] + 1; kk--) {
                        tmp.add(ground.data[kk]);
                    }
                }
                hajime = -1000;
            }
            if(isUnderGround(this.data[i][0], this.data[i][1], ground)) {
                for(int j = 0; j < crossList.get(i).size(); j++) {
                    tmp.add(new double[]{crossList.get(i).get(j)[1], crossList.get(i).get(j)[2]});
                }
            }
            else {
                tmp.add(this.data[i]);
                for(int j = 0; j < crossList.get(i).size(); j++) {
                    tmp.add(new double[]{crossList.get(i).get(j)[1], crossList.get(i).get(j)[2]});
                }
            }
        }
        this.data = tmp.toArray(new double[tmp.size()][]);
        return true;
    }
}

public abstract class Entity {
    public double [][] data;
    public double xMax = -1.0;
    public double yMax = -1.0;
    public double xMin = 10000000;
    public double yMin = 10000000;

    Entity(double [][] data) {
        this.data = data;
        for(int i = 0; i < data.length; i++) {
           if(data[i][0] > xMax) {
               this.xMax = data[i][0];
           }
           if(data[i][0] < xMin) {
               this.xMin = data[i][0];
           }
           if(data[i][1] > yMax) {
               this.yMax = data[i][1];
           }
           if(data[i][1] < yMin) {
               this.yMin = data[i][1];
           }
        }
    }
}

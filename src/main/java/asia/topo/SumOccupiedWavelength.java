package asia.topo;

import java.util.List;

/**
 * Created by 1 on 2016/4/15.
 * 需求：计算在某个时间所有edge中被占用的波长数量
 *       构造函数用来初始化全局变量，如果构造函数的入参命名和全局变量一样，用this.xx，如果不一样，将入参赋值给全局变量
 */
public class SumOccupiedWavelength {
    //全局变量
    public List<Link> edge;
    public double num;

    //构造函数
    public SumOccupiedWavelength(int num, List<Link> links){
        this.num = num;
        edge = links;
    }

    //方法：计算被占用总波长数
    public double sumNumOccupied() {
        double occupuy = 0;
        double wavelengthOccupiedRate;
        for (int i = 0; i < edge.size(); i++) {
            for (int j = 1; j <= 80; j++) {
                if (edge.get(i).wavelengths.get(j).isUsed ) {
                    occupuy = occupuy + 1;
                }
            }
        }
        wavelengthOccupiedRate = occupuy/edge.size()/80.0;
        return wavelengthOccupiedRate;
    }
}

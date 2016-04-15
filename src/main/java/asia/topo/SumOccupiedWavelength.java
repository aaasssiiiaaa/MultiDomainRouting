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
    public double time;

    //构造函数
    public SumOccupiedWavelength(double time){
        this.time=time;  //TODO 还没处理时间问题
    }

    //方法：计算被占用总波长数
    public int sumNumOccupied() {
        int occupuy = 0;
        for (int i = 0; i < edge.size(); i++) {
            for (int j = 1; j <= 80; j++) {
                if (edge.get(i).wavelengths.get(j).isUsed == true) {
                    occupuy = occupuy + 1;
                }
            }
        }
        return occupuy;
    }
}

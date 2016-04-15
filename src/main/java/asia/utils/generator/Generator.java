package asia.utils.generator;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by root on 16-4-14.
 * 业务发生器的抽象基类
 */
public class Generator {
    public double currentTime;
    public ArrayList<Event> eventQueue;

    /**
     * 生成a，b之间的随机整数
     * @param a
     * @param b
     * @return
     */
    private int genRandomInt(int a, int b){
        // TODO
    }

    /**
     *
     * @param a
     * @param b
     * @return
     */
    private double genRandomDouble(double a, double b){
       //TODO
    }

    private double genExponentDistributionRandom(double beta){
        // TODO
    }

    private double genArrivalTime(double time){

    }

    private genHoldTime(double time){

    }


    public Generator() {
    }
}

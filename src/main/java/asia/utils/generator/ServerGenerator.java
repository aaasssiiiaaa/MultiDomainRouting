package asia.utils.generator;

import javafx.util.Pair;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * 业务发生器的类，采用Poission模型
 * Created by root on 16-4-14.
 */
public class ServerGenerator extends Generator{
    // node number in graph
    private int nodesNum;
    //
    private double mu;
    private double rou;
    // service number need to be generated
    private int serviceNum;
    // in each service, the min and max wavelength number
    private int waveNumMin;
    private int waveNumMax;


    private void genEvent(int id){
        double arriveTime = genArrivalTime(mu*rou);
        double holdTime = genHoldTime(mu);
        Pair<Integer, Integer> srcDst = genRandomSrcDst();
        int waveNum = genRandomWaveNum(waveNumMin, waveNumMax);
        ServiceEvent event = new ServiceEvent(EventType.SERVICE_ARRIVAL, id, arriveTime,
                holdTime, srcDst.getKey(), srcDst.getValue(), waveNum);
        eventQueue.add(event);
        try {
            ServiceEvent eventEnd = (ServiceEvent) event.clone();
            eventEnd.setEventType(EventType.SERVICE_END);
            eventQueue.add(eventEnd);
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        currentTime = arriveTime;
    }

    /**
     * generate random source node and destination node by using identifier to mark node.
     * @return Pair
     */
    private Pair<Integer, Integer> genRandomSrcDst(){
        int src = (int)(Math.random()*nodesNum);
        int dst = src;
        do {
            dst = (int)(Math.random()*nodesNum);
        }while(dst == src);
        return new Pair<Integer, Integer>(src, dst);
    }

    /**
     * generate int number ranged [min, max)
     * @param min
     * @param max
     * @return
     */
    private int genRandomWaveNum(int min, int max){
        return genRandomInt(min, max);
    }

    /**
     * generate int number ranged [1, max)
     * @param waveNum
     * @return
     */
    private int genRandomWaveNum(int waveNum){
        return genRandomWaveNum(1, waveNum);
    }


    /**
     * generate event queue according to mu, rou, and other vars.
     * @return
     */
    public List<Event> genEventQueue(){
        for(int i=0; i<serviceNum; i++){
            genEvent(i);
        }
        Collections.sort(eventQueue, new EventComparator());
        return eventQueue;
    }



    public ServerGenerator(int nodesNum, double mu, double rou, int serviceNum, int waveNumMin, int waveNumMax) {
        this.nodesNum = nodesNum;
        this.mu = mu;
        this.rou = rou;
        this.serviceNum = serviceNum;
        this.waveNumMin = waveNumMin;
        this.waveNumMax = waveNumMax;
    }

    /**
     * usage demo
     * @param args no sense.
     */
    public static void main(String[] args){
        ServerGenerator generator = new ServerGenerator(20, 0.04,3,100,1,5);
        List<Event> queue = generator.genEventQueue();
        System.out.print(queue.size());
    }
}

/**
 * Comparator class for sorting List<Event>
 */
class EventComparator implements Comparator<Event> {

    public int compare(Event o1, Event o2) {
        double time1 = o1.obtainHappenTime();
        double time2 = o2.obtainHappenTime();
        Double dou = time1;
        return dou.compareTo(time2);
    }
}
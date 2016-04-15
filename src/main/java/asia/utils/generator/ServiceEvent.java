package asia.utils.generator;

/**
 * Created by root on 16-4-15.
 */
public class ServiceEvent extends Event implements Cloneable{

    private int src;
    private int dst;
    private int requiredWaveNum;

    public int getSrc() {
        return src;
    }

    public void setSrc(int src) {
        this.src = src;
    }

    public int getDst() {
        return dst;
    }

    public void setDst(int dst) {
        this.dst = dst;
    }

    public int getRequiredWaveNum() {
        return requiredWaveNum;
    }

    public void setRequiredWaveNum(int requiredWaveNum) {
        this.requiredWaveNum = requiredWaveNum;
    }

    public ServiceEvent(int src, int dst, int requiredWaveNum) {
        this.src = src;
        this.dst = dst;
        this.requiredWaveNum = requiredWaveNum;
    }

    public ServiceEvent(EventType eventType, int eventId, double arriveTime, double holdTime, int src, int dst, int requiredWaveNum) {
        super(eventType, eventId, arriveTime, holdTime);
        this.src = src;
        this.dst = dst;
        this.requiredWaveNum = requiredWaveNum;
    }


    /**
     * clone method override class Object.
     * @return
     * @throws CloneNotSupportedException
     */
    @Override
    protected Object clone() throws CloneNotSupportedException {
        ServiceEvent serviceEvent = new ServiceEvent(getEventType(), getEventId(),
                getArriveTime(),getHoldTime(), src,dst,requiredWaveNum);
        return serviceEvent;
    }
}

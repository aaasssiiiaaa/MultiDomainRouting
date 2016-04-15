package asia.utils.generator;

/**
 * Created by root on 16-4-15.
 * 服务相关的事件类，包括事件类型，事件id，事件到达时间以及维持时间。
 */
public class Event {

    private EventType eventType;
    private int eventId;
    private double arriveTime;
    private double holdTime;

    public double obtainEndTime(){
        return arriveTime+holdTime;
    }

    public Event() {
    }

    public Event(EventType eventType, int eventId, double arriveTime, double holdTime) {
        this.eventType = eventType;
        this.eventId = eventId;
        this.arriveTime = arriveTime;
        this.holdTime = holdTime;
    }

    public EventType getEventType() {
        return eventType;
    }

    public void setEventType(EventType eventType) {
        this.eventType = eventType;
    }

    public int getEventId() {
        return eventId;
    }

    public void setEventId(int eventId) {
        this.eventId = eventId;
    }

    public double getArriveTime() {
        return arriveTime;
    }

    public void setArriveTime(double arriveTime) {
        this.arriveTime = arriveTime;
    }

    public double getHoldTime() {
        return holdTime;
    }

    public void setHoldTime(double holdTime) {
        this.holdTime = holdTime;
    }
}

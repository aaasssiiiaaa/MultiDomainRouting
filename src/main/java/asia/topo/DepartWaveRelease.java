package asia.topo;

import java.util.List;

/**
 * Created by 1 on 2016/4/15.
 * 需求：业务离去，释放业务所占用波长资源
 */
public class DepartWaveRelease {
    public int serviceID;              //申请资源的业务ID
    public List<Link> edge;

    //构造函数——初始化全局变量
    public DepartWaveRelease(int eventID,List<Link> Links){
        serviceID=eventID;
    }

    //释放波长资源
    public void releaseWaveResource(){
        for (int i =0;i<edge.size();i++){
            for (int j =1;j<=80;j++){
                if (serviceID==edge.get(i).wavelengths.get(j).waveserviceID){
                    edge.get(i).wavelengths.get(j).isUsed=false;
                }
            }
        }
    }
}

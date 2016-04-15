package asia.topo;

import asia.utils.ReadTopo;
import asia.utils.generator.EventType;
import asia.utils.generator.ServerGenerator;
import asia.utils.generator.ServiceEvent;
import org.jgrapht.graph.DefaultDirectedWeightedGraph;

import java.util.List;

/**
 * Created by 1 on 2016/4/14.
 * 主函数
 */
public class Final {

    public static Graph graph;
    public static void main(String[] args) throws Exception {
        //资源读入
        ReadTopo.generateJsonFile(2, 3, -1);        //生成拓扑
        ReadTopo.readJsonFile();                    //读取JSON文件中拓扑信息
        List<Node> Nodes= ReadTopo.obtainNode();
        List<Link> Links= ReadTopo.obtainLink();
        //初始化资源
        graph = new Graph(Nodes,Links);
        System.out.println("节点和链路资源是否一致："+ graph.isMatchVertexEdge());
        DefaultDirectedWeightedGraph abstractGraph=graph.abstractGraph1();
        //随机业务生成
        ServerGenerator generator = new ServerGenerator(15, 0.04,3,100,1,5);
        List<ServiceEvent> servicequeue = generator.genEventQueue();
        for (int i = 0; i< servicequeue.size();i++){
            //记录业务到达总次数和失败次数
            int servicetotalNum=0;
            int servicefailureNum=0;
            //获取event中的ID、请求源节点和目的节点、请求带宽
            int serviceID = servicequeue.get(i).getEventId();
            int src = servicequeue.get(i).getSrc();
            int dst = servicequeue.get(i).getDst();
            int band = servicequeue.get(i).getRequiredWaveNum();
            //获取的是到达业务，分配资源
            if (servicequeue.get(i).getEventType()== EventType.SERVICE_ARRIVAL){
                //记录到达业务总次数
                servicetotalNum=servicetotalNum+1;
                //算路，判断，分配波长资源.若失败，失败次数+1；若成功，继续下一个业务
                PathCalculate pathcalculate = new PathCalculate(Nodes,Links,serviceID);
                Path pa = pathcalculate.calculatePath(graph.g,src,dst);
                boolean result = pathcalculate.domainWaveCalculate(pa,band);
                if (result==false){
                    //记录失败次数
                    servicefailureNum=servicefailureNum+1;
                }
            }else {
                //获取的是离去业务，释放资源
                DepartWaveRelease release = new DepartWaveRelease(serviceID,Links);
            }
            //第i个业务（时刻）对应的波长利用率和阻塞率
            SumOccupiedWavelength sumOccupied = new SumOccupiedWavelength(i);
            double wavelengthOccupiedRate=sumOccupied.sumNumOccupied();
            double blockRate = servicefailureNum/servicetotalNum;
            System.out.println("第"+i+"次业务（时刻）的波长利用率为："+wavelengthOccupiedRate);
            System.out.println("第"+i+"次业务（时刻）的阻塞率为："+blockRate);
        }
    }
}

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
        ReadTopo.setMultiDomain(ReadTopo.generateJsonFile(3, 5, -1));        //生成拓扑
//        ReadTopo.readJsonFile();                    //读取JSON文件中拓扑信息
        List<Node> Nodes= ReadTopo.obtainNode();
        List<Link> Links= ReadTopo.obtainLink();
        //初始化资源
        graph = new Graph(Nodes,Links);
        System.out.println("节点和链路资源是否一致："+ graph.isMatchVertexEdge());
        DefaultDirectedWeightedGraph abstractGraph=graph.abstractGraph1();
        //随机业务生成
        ServerGenerator generator = new ServerGenerator(15, 0.004,3,1000,1,20);
        List<ServiceEvent> servicequeue = generator.genEventQueue();
        //记录业务到达总次数和失败次数
        double servicetotalNum=0.0;
        double servicefailureNum=0.0;
        for (int i = 0; i< servicequeue.size();i++){
            //获取event中的ID、请求源节点和目的节点、请求带宽
            int serviceID = servicequeue.get(i).getEventId();
            int src = servicequeue.get(i).getSrc();
            int dst = servicequeue.get(i).getDst();
            int band = servicequeue.get(i).getRequiredWaveNum();
            Path pa;
            //获取的是到达业务，分配资源
            if (servicequeue.get(i).getEventType()== EventType.SERVICE_ARRIVAL){
                //记录到达业务总次数
                servicetotalNum=servicetotalNum+1.0;
                //算路，判断，分配波长资源.若失败，失败次数+1；若成功，继续下一个业务
                PathCalculate pathcalculate = new PathCalculate(Nodes,Links,serviceID);
                pa = pathcalculate.calculatePath(graph.g,src,dst);
                System.out.println("band is: "+band);
                boolean result = pathcalculate.domainWaveCalculate(pa,band);
                if (!result){
                    //记录失败次数
                    servicefailureNum=servicefailureNum+1.0;
                    System.out.println("the block service number is:"+serviceID);
                }
            }else {
                //获取的是离去业务，释放资源
                System.out.println("the leave service num is:"+serviceID);
                DepartWaveRelease release = new DepartWaveRelease(serviceID,Links);
                release.releaseWaveResource();
            }
            //第i个业务（时刻）对应的波长利用率和阻塞率
            SumOccupiedWavelength sumOccupied = new SumOccupiedWavelength(i, graph.edge);
            double wavelengthOccupiedRate=sumOccupied.sumNumOccupied();
            double blockRate = servicefailureNum/1000;
            System.out.println("the wavelengthOccupiedRate service "+serviceID+" is: "+wavelengthOccupiedRate);
            System.out.println("the blockRate service "+serviceID+" is: "+blockRate);
            System.out.println("--------------------------------------------------");
        }
    }
}

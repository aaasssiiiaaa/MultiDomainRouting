package asia.topo;

import asia.utils.ReadTopo;
import asia.utils.generator.EventType;
import asia.utils.generator.ServerGenerator;
import asia.utils.generator.ServiceEvent;
import org.jgrapht.graph.DefaultDirectedWeightedGraph;

import java.util.List;

/**
 * Created by 1 on 2016/4/14.
 * ������
 */
public class Final {

    public static Graph graph;
    public static void main(String[] args) throws Exception {
        //��Դ����
        ReadTopo.setMultiDomain(ReadTopo.generateJsonFile(3, 5, -1));        //��������
//        ReadTopo.readJsonFile();                    //��ȡJSON�ļ���������Ϣ
        List<Node> Nodes= ReadTopo.obtainNode();
        List<Link> Links= ReadTopo.obtainLink();
        //��ʼ����Դ
        graph = new Graph(Nodes,Links);
        System.out.println("�ڵ����·��Դ�Ƿ�һ�£�"+ graph.isMatchVertexEdge());
        DefaultDirectedWeightedGraph abstractGraph=graph.abstractGraph1();
        //���ҵ������
        ServerGenerator generator = new ServerGenerator(15, 0.004,3,1000,1,20);
        List<ServiceEvent> servicequeue = generator.genEventQueue();
        //��¼ҵ�񵽴��ܴ�����ʧ�ܴ���
        double servicetotalNum=0.0;
        double servicefailureNum=0.0;
        for (int i = 0; i< servicequeue.size();i++){
            //��ȡevent�е�ID������Դ�ڵ��Ŀ�Ľڵ㡢�������
            int serviceID = servicequeue.get(i).getEventId();
            int src = servicequeue.get(i).getSrc();
            int dst = servicequeue.get(i).getDst();
            int band = servicequeue.get(i).getRequiredWaveNum();
            Path pa;
            //��ȡ���ǵ���ҵ�񣬷�����Դ
            if (servicequeue.get(i).getEventType()== EventType.SERVICE_ARRIVAL){
                //��¼����ҵ���ܴ���
                servicetotalNum=servicetotalNum+1.0;
                //��·���жϣ����䲨����Դ.��ʧ�ܣ�ʧ�ܴ���+1�����ɹ���������һ��ҵ��
                PathCalculate pathcalculate = new PathCalculate(Nodes,Links,serviceID);
                pa = pathcalculate.calculatePath(graph.g,src,dst);
                System.out.println("band is: "+band);
                boolean result = pathcalculate.domainWaveCalculate(pa,band);
                if (!result){
                    //��¼ʧ�ܴ���
                    servicefailureNum=servicefailureNum+1.0;
                    System.out.println("the block service number is:"+serviceID);
                }
            }else {
                //��ȡ������ȥҵ���ͷ���Դ
                System.out.println("the leave service num is:"+serviceID);
                DepartWaveRelease release = new DepartWaveRelease(serviceID,Links);
                release.releaseWaveResource();
            }
            //��i��ҵ��ʱ�̣���Ӧ�Ĳ��������ʺ�������
            SumOccupiedWavelength sumOccupied = new SumOccupiedWavelength(i, graph.edge);
            double wavelengthOccupiedRate=sumOccupied.sumNumOccupied();
            double blockRate = servicefailureNum/1000;
            System.out.println("the wavelengthOccupiedRate service "+serviceID+" is: "+wavelengthOccupiedRate);
            System.out.println("the blockRate service "+serviceID+" is: "+blockRate);
            System.out.println("--------------------------------------------------");
        }
    }
}

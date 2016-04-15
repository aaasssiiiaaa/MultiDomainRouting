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
        ReadTopo.generateJsonFile(2, 3, -1);        //��������
        ReadTopo.readJsonFile();                    //��ȡJSON�ļ���������Ϣ
        List<Node> Nodes= ReadTopo.obtainNode();
        List<Link> Links= ReadTopo.obtainLink();
        //��ʼ����Դ
        graph = new Graph(Nodes,Links);
        System.out.println("�ڵ����·��Դ�Ƿ�һ�£�"+ graph.isMatchVertexEdge());
        DefaultDirectedWeightedGraph abstractGraph=graph.abstractGraph1();
        //���ҵ������
        ServerGenerator generator = new ServerGenerator(15, 0.04,3,100,1,5);
        List<ServiceEvent> servicequeue = generator.genEventQueue();
        for (int i = 0; i< servicequeue.size();i++){
            //��¼ҵ�񵽴��ܴ�����ʧ�ܴ���
            int servicetotalNum=0;
            int servicefailureNum=0;
            //��ȡevent�е�ID������Դ�ڵ��Ŀ�Ľڵ㡢�������
            int serviceID = servicequeue.get(i).getEventId();
            int src = servicequeue.get(i).getSrc();
            int dst = servicequeue.get(i).getDst();
            int band = servicequeue.get(i).getRequiredWaveNum();
            //��ȡ���ǵ���ҵ�񣬷�����Դ
            if (servicequeue.get(i).getEventType()== EventType.SERVICE_ARRIVAL){
                //��¼����ҵ���ܴ���
                servicetotalNum=servicetotalNum+1;
                //��·���жϣ����䲨����Դ.��ʧ�ܣ�ʧ�ܴ���+1�����ɹ���������һ��ҵ��
                PathCalculate pathcalculate = new PathCalculate(Nodes,Links,serviceID);
                Path pa = pathcalculate.calculatePath(graph.g,src,dst);
                boolean result = pathcalculate.domainWaveCalculate(pa,band);
                if (result==false){
                    //��¼ʧ�ܴ���
                    servicefailureNum=servicefailureNum+1;
                }
            }else {
                //��ȡ������ȥҵ���ͷ���Դ
                DepartWaveRelease release = new DepartWaveRelease(serviceID,Links);
            }
            //��i��ҵ��ʱ�̣���Ӧ�Ĳ��������ʺ�������
            SumOccupiedWavelength sumOccupied = new SumOccupiedWavelength(i);
            double wavelengthOccupiedRate=sumOccupied.sumNumOccupied();
            double blockRate = servicefailureNum/servicetotalNum;
            System.out.println("��"+i+"��ҵ��ʱ�̣��Ĳ���������Ϊ��"+wavelengthOccupiedRate);
            System.out.println("��"+i+"��ҵ��ʱ�̣���������Ϊ��"+blockRate);
        }
    }
}

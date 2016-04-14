package asia;

import com.sun.xml.internal.ws.api.streaming.XMLStreamReaderFactory;
import org.jgrapht.EdgeFactory;
import org.jgrapht.GraphPath;
import org.jgrapht.alg.DijkstraShortestPath;
import org.jgrapht.alg.KShortestPaths;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultDirectedWeightedGraph;
import org.jgrapht.graph.DefaultEdge;

import java.util.*;

/**
 * Created by 1 on 2016/4/11.
 *
 */

public class Graph {
    /**
     * ȫ�ֱ���
     */
    private List<Node> vertex;          //List��Ϊvertex������������Node��ļ�������,ȫ�ֱ���
    private List<Link> edge;
    private List<Wavelength> wavelength;
    //����ͼ
    DefaultDirectedWeightedGraph<Integer, DefaultEdge> g;
    //����ͼ(��)
    DefaultDirectedWeightedGraph<Integer, DefaultEdge> g1;

    /**
     * ���캯���ͷ���
     **/
    //���캯��(������һ��)����ͼ��
    public Graph(List<Node> Nodes, List<Link> Links) {
        //��ʼ���ڵ���Ϣ
        vertex = Nodes;
        //��ʼ����·��Ϣ,�����˲����ʹ���
        edge = Links;
        //�����ڵ�ͱ�,������ͼg
        for (int i = 0; i < Nodes.size(); i++) {
            g.addVertex(vertex.get(i).name);
            g.addEdge(edge.get(i).src.name, Links.get(i).dst.name);
            //g.setEdgeWeight(g.addEdge(edge.get(i).src.name, Links.get(i).dst.name),edge.get(i).weight);
        }
        //�жϵ�����Ƿ��Ӧ
        System.out.println("�ڵ����·�Ƿ�ƥ�䣺" + isMatchVertexEdge(vertex, edge));
    }

    //check�ڵ����·�Ƿ��Ӧ,ȡ��һ���ߵ�ԴĿ�ڵ�name�����Ƿ�name�����ڽڵ�List
    public boolean isMatchVertexEdge(List<Node> vv, List<Link> ee) {
        int x = 0;
        int y = 0;
        int z = 0;
        for (int i = 0; i < ee.size(); i++) {
            for (int j = 0; j < vv.size(); j++) {
                if (vv.get(j).name == ee.get(i).src.name) {
                    x = x + 1;
                }
                if (vv.get(j).name == ee.get(i).dst.name) {
                    y = y + 1;
                }
            }
            if (x == 1 && y == 1) {
                z = z + 1;
            } else return false;
        }
        if (z == ee.size()) {
            return true;
        } else return false;
    }

    // ��ͼ���������Ϊͼg1��������ͼg1��,��edge������Graph.edge��
    public Graph abstractGraph1(DefaultDirectedWeightedGraph gg, List<Link> ed) {
        List<Node> edegevts = new ArrayList<Node>();
        List<Link> edegelks = new ArrayList<Link>();
        Graph gp = new Graph(edegevts, edegelks);    //ͼ�ɽڵ����·���
        for (int i = 0; i < ed.size(); i++) {
            if (ed.get(i).src.domain != ed.get(i).dst.domain) {
                edegevts.add(ed.get(i).src);
                edegevts.add(ed.get(i).dst);
                edegelks.add(ed.get(i));
                gg.addVertex(ed.get(i).src.name);
                gg.addVertex(ed.get(i).dst.name);
                gg.addEdge(ed.get(i).src.name, ed.get(i).dst.name);
            }
        }
        return gp;   //�����ʣ�Graph�Ͷ���gg��������ʲô��ͬ�����ص���Graph����ͼ����gg������g1��
    }

    //K��·,Path���ͣ�����class Path��,���Ϊͼg/g1��Դ�ڵ㡢Ŀ�Ľڵ㣻����k�����·������ʽList<GraphPath<Node, Node>>
    public Path calculatePath(DefaultDirectedWeightedGraph ggg, Node Source, Node Dest) {
        Path pa = new Path();
        Path pa1 = new Path();
        //K�㷨ȡ3�����·��
        int i = 3;
        KShortestPaths<Node, Node> ksp = new KShortestPaths<Node, Node>(ggg, Source, i);
        pa.nodes = ksp.getPaths(Dest);
        return pa;
    }
    //��������Ĵ����ж��Ƿ�ռ��,����һ����,�����K���·����pa������������edge(Graph.edge)��
    public boolean checkUsed(Path p,int band,List<Link> ew){
        int m=0;
        int bandwave = (int) Math.ceil(band/3);  //�ݶ�ÿ����������Ϊ3M
        for (int x = 0; x < p.nodes.size(); x++) {
            //����ط�ȡ������������i�е�һ����·��
            p.nodes.get(x).getEdgeList();//��ȡ���ĵ�i��·����ֳɸ����ߣ����رߵ�List
            for (int y = 0; y < p.nodes.get(x).getEdgeList().size(); y++) {
                //��·����ɵ�sublistȡ�������յ�name�����ҳ����ڱߵĲ���
                int a = p.nodes.get(x).getEdgeList().subList(y, y + 1).get(0).name;
                int b = p.nodes.get(x).getEdgeList().subList(y, y + 1).get(1).name;
                //����name��������·��y�ı��Ƿ���List edge�У��������ռ䣬���ж��Ƿ�һ��
                for (int z = 0; z < ew.size(); z++) {
                    if (ew.get(z).src.name == a && ew.get(z).dst.name == b)
                    {
                        //�ж�һ���ԣ��Ƿ�ռ��
                        int v=1;
                        for (int n=v;n<=bandwave;n++)
                        {
                            if (bandwave>80)
                            {
                                return false;
                            }
                            else if (ew.get(z).wavelengths.get(n).isUsed)
                            {
                                v=v+bandwave;
                                bandwave=bandwave+bandwave;
                            }
                            else
                            {
                                m=m+1;
                                break;//��ôд�������ص�y,ȥ����һ��sublist
                            }
                        }
                    }
                }
            }
            if (m==p.nodes.get(x).getEdgeList().size())
            {
                return true;
            }
        }//��������ھ��Ҵζ�·�������Ǵζ�·�أ�����Ҫ����һ����������빦��ȥ���䲨����Դ��

}

/**
 * ��������Path��Link��Wavelength��Node
 *          ���Կ����ǻ������壬Ҳ�����Ƿ�����ֻҪ������һ�࣬���еĶ���ͷ������߱���
 */
//·��������������ÿ����Ҫ����
class Path{
    List<GraphPath<Node, Node>> nodes;     //��������һ��
    int wavelengthNum;          //������
}

//List Links�а������·������ݣ�������ֵ�����
class Link{
    Node src;                        //Դ��
    Node dst;                        //�޵�
    double weight;                   //Ȩ��
    List<Wavelength> wavelengths;    //80������
}

//List Wavelength�а������·������ݣ�������ֵ�����
class Wavelength {
    double bandwidth;               //�����Ĵ���
    boolean isUsed;                   //�����Ƿ�ռ��
    int identifier;                 //ÿ�������ı��
}

//List Nodes�а������·�������
class Node{
    int name;                       //�ڵ�����
    int domain;                        //�ڵ����ڵ���
    int degree;                        //�ڵ�Ķȣ�һ���ڵ������ߵĸ�����
}
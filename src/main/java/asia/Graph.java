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

    /**
     * ���캯���ͷ���
     **/
    //���캯��(������һ��)����ʼ��ͼ���˺Ͳ�����Դ��
    public Graph(List<Node> Nodes, List<Link> Links) {
        //��ʼ���ڵ���Ϣ
        vertex = Nodes;
        //��ʼ����·��Ϣ,�����˲����ʹ���
        edge = Links;
        //�����ڵ�ͱ�,������ͼg
        for (int i = 0; i < edge.size(); i++) {
            g.addVertex(edge.get(i).src.identifier);
            g.addVertex(edge.get(i).dst.identifier);
            g.addEdge(edge.get(i).src.identifier, edge.get(i).dst.identifier);
            //g.setEdgeWeight(g.addEdge(edge.get(i).src.name, Links.get(i).dst.name),edge.get(i).weight);
        }
        //�жϵ�����Ƿ��Ӧ
        System.out.println("�ڵ����·�Ƿ�ƥ�䣺" + isMatchVertexEdge());
        //��ʼ��wavelength
        for (int i =0;i<edge.size();i++){
            for (int j=1;j<=80;j++){
                edge.get(i).wavelengths.get(j).bandwidth=3;
                edge.get(i).wavelengths.get(j).isUsed=false;
                edge.get(i).wavelengths.get(j).waveidentifier=j;
            }
        }
    }

    //check�ڵ����·�Ƿ��Ӧ,ȡ��һ���ߵ�ԴĿ�ڵ�name�����Ƿ�name�����ڽڵ�List
    public boolean isMatchVertexEdge() {
        int x = 0;
        int y = 0;
        int z = 0;
        for (int i = 0; i <edge.size(); i++) {
            for (int j = 0; j <vertex.size(); j++) {
                if (vertex.get(j).identifier ==edge.get(i).src.identifier) {
                    x = x + 1;
                }
                if (vertex.get(j).identifier ==edge.get(i).dst.identifier) {
                    y = y + 1;
                }
            }
            if (x == 1 && y == 1) {
                z = z + 1;
            } else return false;
        }
        if (z ==edge.size()) {
            return true;
        } else return false;
    }

    // ����ͼ,����g1����ͼ
    public DefaultDirectedWeightedGraph abstractGraph1() {
        List<Node> edegevts = new ArrayList<Node>();
        List<Link> edegelks = new ArrayList<Link>();
        //�½������ͼ
        DefaultDirectedWeightedGraph<Integer, DefaultEdge> g1 = new DefaultDirectedWeightedGraph<Integer, DefaultEdge>(DefaultEdge.class);
        for (int i = 0; i < edge.size(); i++) {
            if (edge.get(i).src.domain != edge.get(i).dst.domain) {
                edegevts.add(edge.get(i).src);
                edegevts.add(edge.get(i).dst);
                edegelks.add(edge.get(i));
                g1.addVertex(edge.get(i).src.identifier);
                g1.addVertex(edge.get(i).dst.identifier);
                g1.addEdge(edge.get(i).src.identifier, edge.get(i).dst.identifier);
            }
        }
        return g1;
    }

    //K��·,Path���ͣ�����class Path��,���Ϊͼg/g1��Դ�ڵ㡢Ŀ�Ľڵ㣻����k�����·������ʽList<GraphPath<Node, Node>>
    public Path calculatePath(DefaultDirectedWeightedGraph ggg, Node Source, Node Dest) {
        Path pa = new Path();
        //K�㷨ȡ3�����·��
        int i = 3;
        KShortestPaths<Node, Node> ksp = new KShortestPaths<Node, Node>(ggg, Source, i);
        pa.nodes = ksp.getPaths(Dest);
        return pa;
    }

    //��������Ĵ����ж��Ƿ�ռ��,����һ����,�����K���·����pa������������edge(Graph.edge)��
    public boolean checkUsed(Path p, int band) {
        int bandwave = (int) Math.ceil(band / 3);  //�ݶ�ÿ����������Ϊ3M
        int searchwaveNum;
        int sanmeNum=0;
        boolean result;
        for (int x = 0; x < p.nodes.size(); x++) {
            if (bandwave > 80)
                return false;
            int a = p.nodes.get(x).getEdgeList().subList(0, 1).get(0).identifier;
            int b = p.nodes.get(x).getEdgeList().subList(0, 1).get(1).identifier;
            searchwaveNum = oneEdgeEnough(a, b, band);
            if (searchwaveNum == 0) {
                break;
            } else {
                //����ط�ȡ������������2�е�һ����·�������·���ζ�·��������
                sanmeNum=sanmeNum+1;
                for (int y = 1; y < p.nodes.get(x).getEdgeList().size(); y++) {
                    //��·����ɵ�sublistȡ�������յ�name�����ҳ����ڱߵĲ���
                    int c = p.nodes.get(x).getEdgeList().subList(y, y+1).get(0).identifier;
                    int d = p.nodes.get(x).getEdgeList().subList(y, y+1).get(1).identifier;
                    result=istheSame(searchwaveNum,searchwaveNum+bandwave,c,d);
                    if (result==true){
                        sanmeNum=sanmeNum+1;
                    }
                }
                if (sanmeNum==p.nodes.get(x).getEdgeList().size())
                    return true;
            }
        }
    }

    //�жϵ�һ�������·�������ϵĲ�����Դ�Ƿ����,���ز������
    public int oneEdgeEnough(int aa,int bb,int band){
        //����name��������·��y�ı��Ƿ���List edge�У��������ռ䣬���ж��Ƿ�һ��
        int bandwave = (int) Math.ceil(band / 3);  //�ݶ�ÿ����������Ϊ3M
        int v = 0; //��¼��������󲨳��������
        //��������Link
        int z=0;
        do {
            int f =0;   //��¼��ռ�õ�����������
            if (edge.get(z).src.identifier == aa && edge.get(z).dst.identifier == bb) {
            //�ж�һ���ԣ��Ƿ�ռ��
            for (int n=1; n <=80-bandwave+1; n++) {   //nһֱ�ӣ���Ҫ����bandware�ľ��룬��Ȼ�ܴ���ᳬ��80
                if (edge.get(z).wavelengths.get(n).isUsed) {
                    v=0;
                }else{
                    f=f+1;
                    if (f==bandwave){
                    v=n;   //���ڲ������
                       }
                    }
            }
        }
                    z++;
        }while (z < edge.size());
        return v-bandwave+1;
    }

    public boolean istheSame(int num1,int num2,int aa,int bb){
        int z=0;
        boolean result=false;
        do {
            int f =0;   //��¼��ռ�õ�����������
            if (edge.get(z).src.identifier == aa && edge.get(z).dst.identifier == bb) {
                //�ж�һ���ԣ��Ƿ�ռ��
                for (int n=num1; n <=num2; n++) {   //nһֱ�ӣ���Ҫ����bandware�ľ��룬��Ȼ�ܴ���ᳬ��80
                    if (edge.get(z).wavelengths.get(n).isUsed) {
                        result= false;
                    }else{
                        f=f+1;
                        if (f==num2-num1+1){
                            result= true;
                        }
                    }
                }
            }
            z++;
        }while (z < edge.size());
       return result;
    }
}

    /**
     * ��������Path��Link��Wavelength��Node
     * ���Կ����ǻ������壬Ҳ�����Ƿ�����ֻҪ������һ�࣬���еĶ���ͷ������߱���
     */
//·��������������ÿ����Ҫ����
    class Path {
        List<GraphPath<Node, Node>> nodes;     //��������һ��
        int wavelengthNum;          //������
        //wl id
    }

    //List Links�а������·������ݣ�������ֵ�����
    class Link {
        Node src;                        //Դ��
        Node dst;                        //�޵�
        double weight;                   //Ȩ��
        int occupiedWavelengthNum;
        List<Wavelength> wavelengths;    //80������
    }

    //List Wavelength�а������·������ݣ�������ֵ�����
    class Wavelength {
        double bandwidth;               //�����Ĵ���
        boolean isUsed;                   //�����Ƿ�ռ��
        int waveidentifier;                 //ÿ�������ı��
    }

    //List Nodes�а������·�������
    class Node {
        int identifier;                       //�ڵ�����
        int domain;                        //�ڵ����ڵ���
        int degree;                        //�ڵ�Ķȣ�һ���ڵ������ߵĸ�����
    }

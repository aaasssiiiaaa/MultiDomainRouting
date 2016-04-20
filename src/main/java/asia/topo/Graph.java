package asia.topo;

import org.jgrapht.graph.DefaultDirectedWeightedGraph;
import org.jgrapht.graph.DefaultEdge;
import java.util.*;

/**
 * Created by 1 on 2016/4/11.
 * ���󣺶����˽��г�ʼ��
 * 1. ���캯��������ʼ��ͼ�Ͳ�����Դ�����ݶ������ݣ�
 * 2. isMatchVertexEdge������boolean�������жϽڵ����·�Ƿ��Ӧ
 * 3. DefaultDirectedWeightedGraph������g1�������������ͼ
 *
 */

public class Graph {
    /**
     * ȫ�ֱ���
     */
    public List<Node> vertex;          //List��Ϊvertex������������Node��ļ�������,ȫ�ֱ���
    public List<Link> edge;
    public List<Wavelength> wavelength = new ArrayList<Wavelength>(81);
    //����ͼ
    DefaultDirectedWeightedGraph<Node, AccessEdge> g;

    /**
     * ���캯���ͷ���
     **/
    //���캯��(������һ��)���������ǳ�ʼ��ȫ�ֱ�������ʼ��ͼ���˺Ͳ�����Դ��
    public Graph(List<Node> Nodes, List<Link> Links) {
        //��ʼ���ڵ���Ϣ
        vertex = Nodes;
        //��ʼ����·��Ϣ,�����˲����ʹ���
        edge = Links;

        //��ʼ��wavelength
        for (int i =0;i<edge.size();i++) {
            edge.get(i).wavelengths = new ArrayList<Wavelength>(81);
//            edge.get(i).wavelengths.add(new Wavelength());
            for (int j = 0; j <= 80; j++) {
                Wavelength wv = new Wavelength();
                wv.bandwidth = 3;
                wv.isUsed = false;
                wv.waveidentifier = j;
//                System.out.println(j);
                edge.get(i).wavelengths.add(j, wv);
            }
        }
        //�����ڵ�ͱ�,������ͼg
        g = new DefaultDirectedWeightedGraph<Node, AccessEdge>(AccessEdge.class);
        for (int i = 0; i < edge.size(); i++) {
            Node src = vertex.get(edge.get(i).srcSeq);
            Node dst = vertex.get(edge.get(i).dstSeq);
            g.addVertex(src);
            g.addVertex(dst);
            g.addEdge(src,dst);
            //g.setEdgeWeight(g.addEdge(edge.get(i).src.name, Links.get(i).dst.name),edge.get(i).weight);
        }
    }

    //check�ڵ����·�Ƿ��Ӧ,ȡ��һ���ߵ�ԴĿ�ڵ�name�����Ƿ�name�����ڽڵ�List
    // TODO
    public boolean isMatchVertexEdge() {
        int x = 0;
        int y = 0;
        int z = 0;
        for (int i = 0; i <edge.size(); i++) {
            for (int j = 0; j <vertex.size(); j++) {
                if (vertex.get(j).identifier ==edge.get(i).srcSeq) {
                    x = x + 1;
                }
                if (vertex.get(j).identifier ==edge.get(i).dstSeq) {
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
        //�½������ͼ
        DefaultDirectedWeightedGraph<Node, AccessEdge> g1 = new DefaultDirectedWeightedGraph<Node, AccessEdge>(AccessEdge.class);
        int domain1=0;
        int domain2=0;
        for (int i = 0; i < edge.size(); i++) {
            for (int j = 0;j < vertex.size(); j++) {
                if (edge.get(i).srcSeq == vertex.get(j).identifier) {
                    domain1 = vertex.get(j).domain;
                }
            }
            for (int x=0;x<vertex.size();x++) {
                if (edge.get(i).dstSeq == vertex.get(x).identifier) {
                    domain2 = vertex.get(x).domain;
                }
            }
            if (domain1!=domain2){
//                g1.addVertex(edge.get(i).srcSeq);
//                g1.addVertex(edge.get(i).dstSeq);
//                g1.addEdge(edge.get(i).srcSeq, edge.get(i).dstSeq);
                Node src = vertex.get(edge.get(i).srcSeq);
                Node dst = vertex.get(edge.get(i).dstSeq);
                g1.addVertex(src);
                g1.addVertex(dst);
                g1.addEdge(src,dst);
            }
        }
        return g1;
    }
}









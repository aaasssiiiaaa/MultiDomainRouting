package asia.topo;

import org.jgrapht.graph.DefaultDirectedWeightedGraph;
import org.jgrapht.graph.DefaultEdge;
import java.util.*;

/**
 * Created by 1 on 2016/4/11.
 * 需求：对拓扑进行初始化
 * 1. 构造函数——初始化图和波长资源（根据读入数据）
 * 2. isMatchVertexEdge函数（boolean）——判断节点和链路是否对应
 * 3. DefaultDirectedWeightedGraph函数（g1）——构造抽象图
 *
 */

public class Graph {
    /**
     * 全局变量
     */
    public List<Node> vertex;          //List名为vertex，它的内容是Node类的几个属性,全局变量
    public List<Link> edge;
    public List<Wavelength> wavelength;
    //整体图
    DefaultDirectedWeightedGraph<Integer, DefaultEdge> g;

    /**
     * 构造函数和方法
     **/
    //构造函数(和类名一致)——作用是初始化全局变量（初始化图拓扑和波长资源）
    public Graph(List<Node> Nodes, List<Link> Links) {
        //初始化节点信息
        vertex = Nodes;
        //初始化链路信息,包含了波长和带宽
        edge = Links;
        //初始化wavelength
        for (int i =0;i<edge.size();i++) {
            for (int j = 1; j <= 80; j++) {
                wavelength.get(j).bandwidth = 3;
                wavelength.get(j).isUsed = false;
                wavelength.get(j).waveidentifier = j;
            }
        }
        //建立节点和边,构整体图g
        for (int i = 0; i < edge.size(); i++) {
            g.addVertex(edge.get(i).srcSeq);
            g.addVertex(edge.get(i).dstSeq);
            g.addEdge(edge.get(i).srcSeq, edge.get(i).dstSeq);
            //g.setEdgeWeight(g.addEdge(edge.get(i).src.name, Links.get(i).dst.name),edge.get(i).weight);
        }
    }

    //check节点和链路是否对应,取出一条边的源目节点name，看是否（name）属于节点List
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

    // 抽象图,返回g1抽象图
    public DefaultDirectedWeightedGraph abstractGraph1() {
        //新建抽象空图
        DefaultDirectedWeightedGraph<Integer, DefaultEdge> g1 = new DefaultDirectedWeightedGraph<Integer, DefaultEdge>(DefaultEdge.class);
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
                g1.addVertex(edge.get(i).srcSeq);
                g1.addVertex(edge.get(i).dstSeq);
                g1.addEdge(edge.get(i).srcSeq, edge.get(i).dstSeq);
            }
        }
        return g1;
    }
}









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
     * 全局变量
     */
    private List<Node> vertex;          //List名为vertex，它的内容是Node类的几个属性,全局变量
    private List<Link> edge;
    private List<Wavelength> wavelength;
    //整体图
    DefaultDirectedWeightedGraph<Integer, DefaultEdge> g;

    /**
     * 构造函数和方法
     **/
    //构造函数(和类名一致)（初始化图拓扑和波长资源）
    public Graph(List<Node> Nodes, List<Link> Links) {
        //初始化节点信息
        vertex = Nodes;
        //初始化链路信息,包含了波长和带宽
        edge = Links;
        //建立节点和边,构整体图g
        for (int i = 0; i < edge.size(); i++) {
            g.addVertex(edge.get(i).src.identifier);
            g.addVertex(edge.get(i).dst.identifier);
            g.addEdge(edge.get(i).src.identifier, edge.get(i).dst.identifier);
            //g.setEdgeWeight(g.addEdge(edge.get(i).src.name, Links.get(i).dst.name),edge.get(i).weight);
        }
        //判断点与边是否对应
        System.out.println("节点和链路是否匹配：" + isMatchVertexEdge());
        //初始化wavelength
        for (int i =0;i<edge.size();i++){
            for (int j=1;j<=80;j++){
                edge.get(i).wavelengths.get(j).bandwidth=3;
                edge.get(i).wavelengths.get(j).isUsed=false;
                edge.get(i).wavelengths.get(j).waveidentifier=j;
            }
        }
    }

    //check节点和链路是否对应,取出一条边的源目节点name，看是否（name）属于节点List
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

    // 抽象图,返回g1抽象图
    public DefaultDirectedWeightedGraph abstractGraph1() {
        List<Node> edegevts = new ArrayList<Node>();
        List<Link> edegelks = new ArrayList<Link>();
        //新建抽象空图
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

    //K算路,Path类型（后有class Path）,入参为图g/g1、源节点、目的节点；返回k条最短路径，形式List<GraphPath<Node, Node>>
    public Path calculatePath(DefaultDirectedWeightedGraph ggg, Node Source, Node Dest) {
        Path pa = new Path();
        //K算法取3个最短路径
        int i = 3;
        KShortestPaths<Node, Node> ksp = new KShortestPaths<Node, Node>(ggg, Source, i);
        pa.nodes = ksp.getPaths(Dest);
        return pa;
    }

    //根据请求的带宽判断是否被占用,满足一致性,入参是K最短路径（pa）、请求带宽和edge(Graph.edge)；
    public boolean checkUsed(Path p, int band) {
        int bandwave = (int) Math.ceil(band / 3);  //暂定每个波长带宽为3M
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
                //这个地方取出来的是属于2行的一整条路径（最短路、次短路。。。）
                sanmeNum=sanmeNum+1;
                for (int y = 1; y < p.nodes.get(x).getEdgeList().size(); y++) {
                    //把路径拆成的sublist取出起点和终点name，并找出所在边的波长
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

    //判断第一条（最短路径）边上的波长资源是否存在,返回波长编号
    public int oneEdgeEnough(int aa,int bb,int band){
        //根据name，查找子路径y的边是否在List edge中，在则分配空间，再判断是否一致
        int bandwave = (int) Math.ceil(band / 3);  //暂定每个波长带宽为3M
        int v = 0; //记录波长满足后波长所属编号
        //遍历所有Link
        int z=0;
        do {
            int f =0;   //记录被占用的连续波长数
            if (edge.get(z).src.identifier == aa && edge.get(z).dst.identifier == bb) {
            //判断一致性，是否被占用
            for (int n=1; n <=80-bandwave+1; n++) {   //n一直加，但要保持bandware的距离，不然总带宽会超过80
                if (edge.get(z).wavelengths.get(n).isUsed) {
                    v=0;
                }else{
                    f=f+1;
                    if (f==bandwave){
                    v=n;   //所在波长编号
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
            int f =0;   //记录被占用的连续波长数
            if (edge.get(z).src.identifier == aa && edge.get(z).dst.identifier == bb) {
                //判断一致性，是否被占用
                for (int n=num1; n <=num2; n++) {   //n一直加，但要保持bandware的距离，不然总带宽会超过80
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
     * 属性区域：Path、Link、Wavelength、Node
     * 属性可以是基本定义，也可以是方法。只要属于这一类，其中的定义和方法都具备。
     */
//路径的属性特征，每条都要满足
    class Path {
        List<GraphPath<Node, Node>> nodes;     //链表，有下一跳
        int wavelengthNum;          //波长数
        //wl id
    }

    //List Links中包含以下方面内容，具体数值上面给
    class Link {
        Node src;                        //源点
        Node dst;                        //宿点
        double weight;                   //权重
        int occupiedWavelengthNum;
        List<Wavelength> wavelengths;    //80个波长
    }

    //List Wavelength中包含以下方面内容，具体数值上面给
    class Wavelength {
        double bandwidth;               //波长的带宽
        boolean isUsed;                   //波长是否占用
        int waveidentifier;                 //每个波长的编号
    }

    //List Nodes中包含以下方面内容
    class Node {
        int identifier;                       //节点命名
        int domain;                        //节点所在的域
        int degree;                        //节点的度（一个节点所连边的个数）
    }

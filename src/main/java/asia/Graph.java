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
    //抽象图(空)
    DefaultDirectedWeightedGraph<Integer, DefaultEdge> g1;

    /**
     * 构造函数和方法
     **/
    //构造函数(和类名一致)（构图）
    public Graph(List<Node> Nodes, List<Link> Links) {
        //初始化节点信息
        vertex = Nodes;
        //初始化链路信息,包含了波长和带宽
        edge = Links;
        //建立节点和边,构整体图g
        for (int i = 0; i < Nodes.size(); i++) {
            g.addVertex(vertex.get(i).name);
            g.addEdge(edge.get(i).src.name, Links.get(i).dst.name);
            //g.setEdgeWeight(g.addEdge(edge.get(i).src.name, Links.get(i).dst.name),edge.get(i).weight);
        }
        //判断点与边是否对应
        System.out.println("节点和链路是否匹配：" + isMatchVertexEdge(vertex, edge));
    }

    //check节点和链路是否对应,取出一条边的源目节点name，看是否（name）属于节点List
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

    // 对图做抽象，入参为图g1（构抽象图g1）,边edge（或者Graph.edge）
    public Graph abstractGraph1(DefaultDirectedWeightedGraph gg, List<Link> ed) {
        List<Node> edegevts = new ArrayList<Node>();
        List<Link> edegelks = new ArrayList<Link>();
        Graph gp = new Graph(edegevts, edegelks);    //图由节点和链路组成
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
        return gp;   //有疑问？Graph和对象gg功能上有什么不同？返回的是Graph，构图了是gg（给了g1）
    }

    //K算路,Path类型（后有class Path）,入参为图g/g1、源节点、目的节点；返回k条最短路径，形式List<GraphPath<Node, Node>>
    public Path calculatePath(DefaultDirectedWeightedGraph ggg, Node Source, Node Dest) {
        Path pa = new Path();
        Path pa1 = new Path();
        //K算法取3个最短路径
        int i = 3;
        KShortestPaths<Node, Node> ksp = new KShortestPaths<Node, Node>(ggg, Source, i);
        pa.nodes = ksp.getPaths(Dest);
        return pa;
    }
    //根据请求的带宽判断是否被占用,满足一致性,入参是K最短路径（pa）、请求带宽和edge(Graph.edge)；
    public boolean checkUsed(Path p,int band,List<Link> ew){
        int m=0;
        int bandwave = (int) Math.ceil(band/3);  //暂定每个波长带宽为3M
        for (int x = 0; x < p.nodes.size(); x++) {
            //这个地方取出来的是属于i行的一整条路径
            p.nodes.get(x).getEdgeList();//将取出的第i行路径拆分成各条边，返回边的List
            for (int y = 0; y < p.nodes.get(x).getEdgeList().size(); y++) {
                //把路径拆成的sublist取出起点和终点name，并找出所在边的波长
                int a = p.nodes.get(x).getEdgeList().subList(y, y + 1).get(0).name;
                int b = p.nodes.get(x).getEdgeList().subList(y, y + 1).get(1).name;
                //根据name，查找子路径y的边是否在List edge中，在则分配空间，再判断是否一致
                for (int z = 0; z < ew.size(); z++) {
                    if (ew.get(z).src.name == a && ew.get(z).dst.name == b)
                    {
                        //判断一致性，是否被占用
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
                                break;//怎么写啊，返回到y,去找下一条sublist
                            }
                        }
                    }
                }
            }
            if (m==p.nodes.get(x).getEdgeList().size())
            {
                return true;
            }
        }//如果不等于就找次短路，如果标记次短路呢？还需要建立一个函数或加入功能去分配波长资源！

}

/**
 * 属性区域：Path、Link、Wavelength、Node
 *          属性可以是基本定义，也可以是方法。只要属于这一类，其中的定义和方法都具备。
 */
//路径的属性特征，每条都要满足
class Path{
    List<GraphPath<Node, Node>> nodes;     //链表，有下一跳
    int wavelengthNum;          //波长数
}

//List Links中包含以下方面内容，具体数值上面给
class Link{
    Node src;                        //源点
    Node dst;                        //宿点
    double weight;                   //权重
    List<Wavelength> wavelengths;    //80个波长
}

//List Wavelength中包含以下方面内容，具体数值上面给
class Wavelength {
    double bandwidth;               //波长的带宽
    boolean isUsed;                   //波长是否占用
    int identifier;                 //每个波长的编号
}

//List Nodes中包含以下方面内容
class Node{
    int name;                       //节点命名
    int domain;                        //节点所在的域
    int degree;                        //节点的度（一个节点所连边的个数）
}
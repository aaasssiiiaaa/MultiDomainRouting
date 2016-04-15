package asia.topo;

import org.jgrapht.alg.KShortestPaths;
import org.jgrapht.graph.DefaultDirectedWeightedGraph;
import java.util.List;

/**
 * Created by 1 on 2016/4/15.
 * 需求：业务请求——算路、判断占用、分配波长、计算占用波长数
 * 1. calculatePath函数（List<Graph<Node,Node>>）——计算最短路、次短路，k次短路
 * 2. checkUsed函数（boolean）——判断请求带宽波长是否被占用
 *    (1) oneEdgeEnough函数（int波长编号）——判断最短路是否有波长资源，如有返回所在波长编号，没有寻找次短路
 *    (2) secondThirdWave函数（int 统计数）——判断次短路、k次短路对最短路返回的波长编号是否都有波长资源满足，返回int
 *                                             在checkUsed函数中判断，统计数=k，则说明三条边都满足该波长编号
 *                                             统计数！=k，最短路编号++，再次判断次短路，循环。
 *                                             循环完之后，还是没有找到满足的，进入次短路
 *        (1.1)istheSam函数——判断secondThirdWave函数中波长是否一致
 *    (6) markOccupiedWavelength函数——对已经占用的波长进行标记，在checkUsed函数中
 * 3.计算所有edge中占用的波长总数
 */
public class PathCalculate {
    public List<Node> vertex;          //List名为vertex，它的内容是Node类的几个属性,全局变量
    public List<Link> edge;
    public PathCalculate(List<Node> Nodes, List<Link> Links){
        vertex = Nodes;
        edge = Links;
    }

    //K算路,Path类型（后有class Path）,入参为图g/g1、源节点、目的节点；返回k条最短路径，形式List<GraphPath<Node, Node>>
    public Path calculatePath(DefaultDirectedWeightedGraph ggg, Node Source, Node Dest) {
        Path pa = new Path();
        //K算法取3个最短路径
        int k = 3;
        KShortestPaths<Node, Node> ksp = new KShortestPaths<Node, Node>(ggg, Source, k);
        pa.nodes = ksp.getPaths(Dest);
        return pa;
    }

    //根据请求的带宽判断是否被占用,满足一致性,入参是K最短路径（pa）、请求带宽和edge(Graph.edge)；
    public boolean checkUsed(Path p, int band) {
        int bandwave = (int) Math.ceil(band / 3);  //暂定每个波长带宽为3M
        int searchwaveNum;     //函数oneEdgeEnough返回值
        int sameNum=0;        //记录每条边是否都满足波长区间
        int unedegNum=0;       //记录遍历完后仍然没找到的不可用的数
        int pathNum=0;         //记录不可用的路径数
        boolean result = false; // 返回值
        //获取三条路径
        for (int x = 0; x < p.nodes.size(); x++) {
            if (bandwave > 80)
                return false;
            //先获取第一条路径的第一条边
            int a = p.nodes.get(x).getEdgeList().subList(0, 1).get(0).identifier;
            int b = p.nodes.get(x).getEdgeList().subList(0, 1).get(1).identifier;
            //获取V值，先取得第一条边带宽请求的波长编号
            for (int i=1;i<=80-bandwave+1;i++){
                searchwaveNum = oneEdgeEnough(a, b, band);
                if (searchwaveNum == 0) {
                    break;//第一条路径不满足带宽请求，寻找下一条路径
                } else {
                    //这个地方取出来的是属于2行的一整条路径（最短路、次短路。。。）
                    //判断第二条、第三条边是否满足，满足sanmeNum+1
                    sameNum=sameNum+1;
                    sameNum=sameNum+secondThirdWave(searchwaveNum,bandwave,x,p);
                    if (sameNum==p.nodes.get(x).getEdgeList().size()){
                        markOccupiedWavelength(searchwaveNum, bandwave, x, p);
                        return result=true;
                    }else
                        unedegNum=unedegNum+1;
                }
            }
            if (unedegNum==80-bandwave+1){  //如果80-bandware+1次都没有找到的话，就换次短路
                pathNum=pathNum+1;
                continue;
            }
        }
        if (pathNum==p.nodes.size())      //如果k次都没有符合的路径的话，就返回false
            result=false;
        return result;
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
            if (edge.get(z).srcSeq == aa && edge.get(z).dstSeq == bb) {
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

    //对第一条路径的第二、三条边进行判断，是否和第一条边波长一致
    public int secondThirdWave(int searchNum ,int bandwave,int x,Path p){
        int sanmeNum=0;
        boolean result;        //函数istheSame返回值（判断后面的边和第一条是否波长一致）
        for (int y = 1; y < p.nodes.get(x).getEdgeList().size(); y++) {
            int c = p.nodes.get(x).getEdgeList().subList(y, y+1).get(0).identifier;
            int d = p.nodes.get(x).getEdgeList().subList(y, y+1).get(1).identifier;
            result=istheSame(searchNum,searchNum+bandwave,c,d);
            if (result==true){
                sanmeNum=sanmeNum+1;
            }
        }
        return sanmeNum;
    }

    public boolean istheSame(int searchNum,int bandware,int aa,int bb){
        int z=0;
        int searchNum1=searchNum+ bandware;
        boolean result=false;
        do {
            int f =0;   //记录被占用的连续波长数
            if (edge.get(z).srcSeq == aa && edge.get(z).dstSeq == bb) {
                //判断一致性，是否被占用
                for (int n=searchNum; n <=searchNum1; n++) {   //n一直加，但要保持bandware的距离，不然总带宽会超过80
                    if (edge.get(z).wavelengths.get(n).isUsed) {
                        result= false;
                    }else{
                        f=f+1;
                        if (f==searchNum1-searchNum+1){
                            result= true;
                        }
                    }
                }
            }
            z++;
        }while (z < edge.size());
        return result;
    }

    //判断波长能用之后，对波长isUsed进行占用
    public void markOccupiedWavelength(int searchNum,int banware,int x,Path p){
        int searchNum1=searchNum+banware;
        //k条路径进行遍历标记
        for (int y = 0; y < p.nodes.get(x).getEdgeList().size(); y++) {
            int c = p.nodes.get(x).getEdgeList().subList(y, y+1).get(0).identifier;
            int d = p.nodes.get(x).getEdgeList().subList(y, y+1).get(1).identifier;
            //找到对应的边,并进行标记
            for (int z=0;z<edge.size();z++) {
                if (edge.get(z).srcSeq == c && edge.get(z).dstSeq == d) {
                    for (int n = searchNum; n < searchNum1; n++) {
                        edge.get(z).wavelengths.get(n).isUsed = true;
                    }
                }
            }
        }
    }

    //计算所有edge中被占用的波长数量
    public int sumNumOccupiedWavelength() {
        int occupuy = 0;
        for (int i = 0; i < edge.size(); i++) {
            for (int j = 1; j <= 80; j++) {
                if (edge.get(i).wavelengths.get(j).isUsed == true) {
                    occupuy = occupuy + 1;
                }
            }
        }
        return occupuy;
    }
}

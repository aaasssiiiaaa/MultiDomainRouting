package asia.topo;

import org.jgrapht.alg.KShortestPaths;
import org.jgrapht.graph.DefaultDirectedWeightedGraph;
import java.util.List;

/**
 * Created by 1 on 2016/4/15.
 * 需求：业务请求——算路、判断占用、分配波长、计算占用波长数
 * 1. calculatePath函数（List<Graph<Node,Node>>）——计算最短路、次短路，k次短路
 * 2. domainWaveCalculate函数——对域进行判断，再调用checkUsed函数
 *    (1) subsecondThirdWave1/2函数（int 统计数）
 *          (1.1) subistheSam1/2函数
 * 3. checkUsed函数（boolean）——判断请求带宽波长是否被占用，在domainWaveCalculate函数
 *    (1) oneEdgeEnough函数（int波长编号）——判断最短路是否有波长资源，如有返回所在波长编号，没有寻找次短路
 *    (2) secondThirdWave函数（int 统计数）——判断次短路、k次短路对最短路返回的波长编号是否都有波长资源满足，返回int
 *                                             在checkUsed函数中判断，统计数=k，则说明三条边都满足该波长编号
 *                                             统计数！=k，最短路编号++，再次判断次短路，循环。
 *                                             循环完之后，还是没有找到满足的，进入次短路
 *        (1.1) istheSam函数——判断secondThirdWave函数中波长是否一致
 * 4. markOccupiedWavelength函数——对已经占用的波长进行标记，在checkUsed函数中
 * 
 */

public class PathCalculate {
    //全局变量
    public List<Node> vertex;          //List名为vertex，它的内容是Node类的几个属性,全局变量
    public List<Link> edge;
    public int serviceID;              //申请资源的业务ID

    //构造函数——初始化全局变量
    public PathCalculate(List<Node> Nodes, List<Link> Links,int eventID){
        vertex = Nodes;
        edge = Links;
        serviceID = eventID;
    }

    //方法1：K算路,Path类型（后有class Path）,入参为图g/g1、源节点、目的节点；返回k条最短路径，形式List<GraphPath<Node, Node>>
    public Path calculatePath(DefaultDirectedWeightedGraph ggg, Node Source, Node Dest) {
        Path pa = new Path();
        //K算法取3个最短路径
        int k = 3;
        KShortestPaths<Node, Node> ksp = new KShortestPaths<Node, Node>(ggg, Source, k);
        pa.nodes = ksp.getPaths(Dest);
        return pa;
    }

    //方法2：按域划分路径的边，域内波长一致（方法checkUsed），域与域波长不一致，将边缘边找出
    public boolean domainWaveCalculate(Path p,int band){
        int bandwave = (int) Math.ceil(band / 3);  //每个波长带宽为3M
        int edgedomain1=0;
        int edgedomain2=0;
        int pathNum=0;         //记录遍历完k条路径后的不可用的路径数
        boolean result = false;     // 缓存返回值
        if (bandwave > 80)
            return false;
            //k条路径遍历，最短、次短、k次短
            for (int x=0; x< p.nodes.size();x++){
                int edgesize=p.nodes.get(x).getEdgeList().size();
                int y=0;
                do {
                    //遍历找到不同域（边缘）的两个节点作为断点，并作为计算不同路径的起点和终点
                    int a = p.nodes.get(x).getEdgeList().subList(y, y+1).get(0).domain;
                    int b = p.nodes.get(x).getEdgeList().subList(y, y+1).get(1).domain;
                    if (a!=b) {
                        //如果在不同域，分成两个单域来分配资源
                        edgedomain1=p.nodes.get(x).getEdgeList().subList(y, y+1).get(0).identifier;//第一个域的结束点
                        edgedomain2=p.nodes.get(x).getEdgeList().subList(y, y+1).get(1).identifier;//第二个域的起始点
                        subCheckUsed1(p, band, x, edgedomain1);
                        subCheckUsed2(p, band, x, edgedomain2);
                    }
                    y++;
                }while (y<edgesize);
                //全都在同一个域内，调用方法checkUsed
                checkUsed(p,band,x);
                if (checkUsed(p,band,x)==true){
                    return result=true;
                }else {
                    pathNum=pathNum+1;
                }
        }
        if (pathNum==p.nodes.size())
            result=false;
     return result;
    }

    //方法3：根据请求的带宽判断是否被占用,满足一致性,入参是K最短路径（pa）、请求带宽和第几条路径；
    public boolean checkUsed(Path p, int band,int x) {
        int bandwave = (int) Math.ceil(band / 3);
        int searchwaveNum;     //函数oneEdgeEnough返回值
        int sameNum=0;        //记录每条边是否都满足波长区间
        int unedegNum=0;       //记录波长遍历完后仍然没找到的不可用的数
        boolean result = false; // 返回值
        //先获取第x条路径的第一条边
        int a = p.nodes.get(x).getEdgeList().subList(0, 1).get(0).identifier;
        int b = p.nodes.get(x).getEdgeList().subList(0, 1).get(1).identifier;
        //取符合第一条边带宽请求的波长编号
        for (int i=1;i<=80-bandwave+1;i++){
            searchwaveNum = oneEdgeEnough(a, b, band); //返回的波长编号
            if (searchwaveNum == 0) {
                return result=false;//第一条路径不满足带宽请求，寻找下一条路径
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
               result=false;
            }
        return result;
    }


    //方法4：checkUsed函数调用，判断第一条（最短路径）边上的波长资源是否存在,返回波长编号
    public int oneEdgeEnough(int a,int b,int band){
        int bandwave = (int) Math.ceil(band / 3);
        int v = 0; //记录波长满足后波长所属编号
        //遍历所有Link
        int z=0;
        //遍历edge，找到a，b对应的边，从头判断波长是否占用
        do {
            int f =0;   //记录被占用的连续波长数
            if (edge.get(z).srcSeq == a && edge.get(z).dstSeq == b) {
                //判断一致性，是否被占用
                for (int n=1; n <=80-bandwave+1; n++) {   //n一直加，但要保持bandware的距离，不然总带宽会超过80
                    if (edge.get(z).wavelengths.get(n).isUsed) {
                        v=0;
                    }else{
                        f=f+1;
                        if (f==bandwave){
                            v=n;   //所在波长编号
                            break;
                        }
                    }
                }
            }
            z++;
        }while (z < edge.size());
        return v-bandwave+1;
    }

    //方法5：checkUsed函数调用，对第一条路径的第二、三条边进行判断，是否和第一条边波长一致
    public int secondThirdWave(int searchNum ,int bandwave,int x,Path p){
        int sameNum=0;
        boolean result;        //函数istheSame返回值（判断后面的边和第一条是否波长一致）
        for (int y = 1; y < p.nodes.get(x).getEdgeList().size(); y++) {
            int c = p.nodes.get(x).getEdgeList().subList(y, y+1).get(0).identifier;
            int d = p.nodes.get(x).getEdgeList().subList(y, y+1).get(1).identifier;
            result=istheSame(searchNum, searchNum + bandwave, c, d);
            if (result==true){
                sameNum=sameNum+1;
            }
        }
        return sameNum;
    }

    //方法6：secondThirdWave函数调用
    public boolean istheSame(int searchNum,int bandware,int c,int d){
        int z=0;
        int searchNum1=searchNum+ bandware;
        boolean result=false;
        do {
            int f =0;   //记录被占用的连续波长数
            if (edge.get(z).srcSeq == c && edge.get(z).dstSeq == d) {
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

    //方法7：判断波长能用之后，对波长isUsed进行占用
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
                        edge.get(z).wavelengths.get(n).isUsed = true;  //标记已被占用
                        edge.get(z).wavelengths.get(n).waveserviceID=serviceID;     //标记占用该波长的业务
                    }
                }
            }
        }
    }

    //以下两种方法是对上面方法进行的修改
    //路径跨了几个域后，对其中一个单域进行路径处理
    public boolean subCheckUsed1(Path p, int band,int x,int edgedomain1) {
        int bandwave = (int) Math.ceil(band / 3);
        int searchwaveNum;     //函数oneEdgeEnough返回值
        int sameNum=0;        //记录每条边是否都满足波长区间
        int unedegNum=0;       //记录波长遍历完后仍然没找到的不可用的数
        boolean result = false; // 返回值
        //先获取第x条路径的第一条边
        int a = p.nodes.get(x).getEdgeList().subList(0, 1).get(0).identifier;
        int b = p.nodes.get(x).getEdgeList().subList(0, 1).get(1).identifier;
        //取符合第一条边带宽请求的波长编号
        for (int i=1;i<=80-bandwave+1;i++){
            searchwaveNum = oneEdgeEnough(a, b, band); //返回的波长编号
            if (searchwaveNum == 0) {
                return result=false;//第一条路径不满足带宽请求，寻找下一条路径
            } else {
                //这个地方取出来的是属于2行的一整条路径（最短路、次短路。。。）
                //判断第二条、第三条边是否满足，满足sanmeNum+1
                sameNum=sameNum+1;
                sameNum=sameNum+subsecondThirdWave1(searchwaveNum, bandwave, x, p,edgedomain1);
                if (sameNum==p.nodes.get(x).getEdgeList().size()){
                    markOccupiedWavelength(searchwaveNum, bandwave, x, p);
                    return result=true;
                }else
                    unedegNum=unedegNum+1;
            }
        }
        if (unedegNum==80-bandwave+1){  //如果80-bandware+1次都没有找到的话，就换次短路
            result=false;
        }
        return result;
    }
    //对该方法进行改造，同上
    public int subsecondThirdWave1(int searchNum ,int bandwave,int x,Path p,int edgedomain1){
        int sameNum=0;
        boolean result;        //函数istheSame返回值（判断后面的边和第一条是否波长一致）
        int i = 0;
        int j = 0;
        do {
            j=j+1;
            i++;
        }while (p.nodes.get(x).getEdgeList().subList(i, i+1).get(1).identifier==edgedomain1);
        for (int y = 1; y < j; y++) {
            int c = p.nodes.get(x).getEdgeList().subList(y, y+1).get(0).identifier;
            int d = p.nodes.get(x).getEdgeList().subList(y, y+1).get(1).identifier;
            result=istheSame(searchNum, searchNum + bandwave, c, d);
            if (result==true){
                sameNum=sameNum+1;
            }
        }
        return sameNum;
    }
    public boolean subCheckUsed2(Path p, int band,int x,int edgedomain2) {
        int bandwave = (int) Math.ceil(band / 3);
        int searchwaveNum;     //函数oneEdgeEnough返回值
        int sameNum=0;        //记录每条边是否都满足波长区间
        int unedegNum=0;       //记录波长遍历完后仍然没找到的不可用的数
        boolean result = false; // 返回值
        //先获取第x条路径的第一条边
        int a = p.nodes.get(x).getEdgeList().subList(0, 1).get(0).identifier;
        int b = p.nodes.get(x).getEdgeList().subList(0, 1).get(1).identifier;
        //取符合第一条边带宽请求的波长编号
        for (int i=1;i<=80-bandwave+1;i++){
            searchwaveNum = oneEdgeEnough(a, b, band); //返回的波长编号
            if (searchwaveNum == 0) {
                return result=false;//第一条路径不满足带宽请求，寻找下一条路径
            } else {
                //这个地方取出来的是属于2行的一整条路径（最短路、次短路。。。）
                //判断第二条、第三条边是否满足，满足sanmeNum+1
                sameNum=sameNum+1;
                sameNum=sameNum+subsecondThirdWave2(searchwaveNum, bandwave, x, p, edgedomain2);
                if (sameNum==p.nodes.get(x).getEdgeList().size()){
                    markOccupiedWavelength(searchwaveNum, bandwave, x, p);
                    return result=true;
                }else
                    unedegNum=unedegNum+1;
            }
        }
        if (unedegNum==80-bandwave+1){  //如果80-bandware+1次都没有找到的话，就换次短路
            result=false;
        }
        return result;
    }
    //对该方法进行改造，同上
    public int subsecondThirdWave2(int searchNum ,int bandwave,int x,Path p,int edgedomain2){
        int sameNum=0;
        boolean result;        //函数istheSame返回值（判断后面的边和第一条是否波长一致）
        int i = p.nodes.get(x).getEdgeList().size();
        int j = 0;
        do {
            j=j+1;
            i--;
        }while (p.nodes.get(x).getEdgeList().subList(i-1, i).get(1).identifier==edgedomain2);
        for (int y = p.nodes.get(x).getEdgeList().size()-j+1; y < p.nodes.get(x).getEdgeList().size(); y++) {
            int c = p.nodes.get(x).getEdgeList().subList(y, y+1).get(0).identifier;
            int d = p.nodes.get(x).getEdgeList().subList(y, y+1).get(1).identifier;
            result = istheSame(searchNum, searchNum + bandwave, c, d);
            if (result==true){
                sameNum=sameNum+1;
            }
        }
        return sameNum;
    }
}

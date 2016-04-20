package asia.topo;

import javafx.util.Pair;
import org.jgrapht.GraphPath;
import org.jgrapht.alg.KShortestPaths;
import org.jgrapht.graph.DefaultDirectedWeightedGraph;
import org.jgrapht.graph.DefaultEdge;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    public Path calculatePath(DefaultDirectedWeightedGraph ggg, int Source, int Dest) {
        Path pa = new Path();
        //K算法取3个最短路径
        int k = 3;
        KShortestPaths<Node, AccessEdge> ksp = new KShortestPaths<Node, AccessEdge>(ggg,
                Final.graph.vertex.get(Source), k);
        List<GraphPath<Node, AccessEdge>> list = ksp.getPaths(Final.graph.vertex.get(Dest));
        if(list == null){
            System.out.println("breakpoint");
        }
        pa.nodes = list;
        return pa;
    }

    //方法2：按域划分路径的边，域内波长一致（方法checkUsed），域与域波长不一致，将边缘边找出
    public boolean domainWaveCalculate(Path p,int band){
        int bandwave = band;  //每个波长带宽为3M
        int edgedomain1=0;
        int edgedomain2=0;
//        int pathNum=0;         //记录遍历完k条路径后的不可用的路径数
        if (bandwave > 80)
            return false;
            //k条路径遍历，最短、次短、k次短
            for (int x=0; x< p.nodes.size();x++) {
                List<AccessEdge> edgeList = p.nodes.get(x).getEdgeList();
                int edgesize = edgeList.size();
                // splitedSubPaths保存一个path的多个域的经过的点。
                List<List<Integer>> splitedSubPaths = new ArrayList<List<Integer>>();
                int nxtsrc = edgeList.get(0).getSource().identifier;
                List<Integer> subPath = new ArrayList<Integer>();
                subPath.add(edgeList.get(0).getSource().identifier);
                for (int y = 0; y < edgeList.size(); y++) {
                    //遍历找到不同域（边缘）的两个节点作为断点，并作为计算不同路径的起点和终点
                    AccessEdge accessEdge = edgeList.get(y);
                    int srcDomain = accessEdge.getSource().domain;
                    int dstDomain = accessEdge.getDest().domain;
                    // 如果edge两头不是同一个域
                    if (srcDomain != dstDomain) {
                        //如果在不同域，分成单域来分配资源
                       // int subdet = accessEdge.getSource().identifier;
//                        map.add(new Pair<Integer, Integer>(nxtsrc, subdet));
                        // 先把上一个域的内容保存一下
                        splitedSubPaths.add(subPath);
                        // 再新建一个List用于保存下一个域的内容
                        subPath = new ArrayList<Integer>();
                        subPath.add(accessEdge.getDest().identifier);
                        //nxtsrc = accessEdge.getDest().identifier;
                    }else {
                        // 如果edge两头同属一个域
                        subPath.add(accessEdge.getDest().identifier);
                    }
                    // 把最后一个域放进去。
                    if(y==edgeList.size()-1){
                        splitedSubPaths.add(subPath);
                    }
                }
                //判断三个单域是否都分别满足波长一致
                int j=0;
                for (int i=0;i<splitedSubPaths.size();i++){
                    boolean singleDomainUsed=subCheckUsed1(band,edgeList, edgedomain1,splitedSubPaths.get(i));
                    if (singleDomainUsed)
                        j=j+1;
                }
                if (j==splitedSubPaths.size()) {      //三个单域都可用
                    return true;
                }else{
                    // 释放之前的资源。
                    DepartWaveRelease release =new  DepartWaveRelease(serviceID,edge);
                    release.releaseWaveResource();
                }
                //全都在同一个域内，调用方法checkUsed
//                checkUsed(edgeList, band);
//                if (checkUsed(edgeList, band)) {
//                    return true;
//                }
            }
     return false;
    }
/*
    //方法3：根据请求的带宽判断是否被占用,满足一致性,入参是K最短路径（pa）、请求带宽和第几条路径；
    public boolean checkUsed(List<AccessEdge>  edgeList, int band) {
        int bandwave = band;
        int searchwaveNum;     //函数oneEdgeEnough返回值
        int sameNum=0;        //记录每条边是否都满足波长区间
        int unedegNum=0;       //记录波长遍历完后仍然没找到的不可用的数
        boolean result = false; // 返回值
        //先获取第x条路径的第一条边
        int a = edgeList.get(0).getSource().identifier;
        int b = edgeList.get(0).getDest().identifier;
        //取符合第一条边带宽请求的波长编号
        for (int i=1;i<=80-bandwave+1;i++){
            searchwaveNum = oneEdgeEnough(a, b, band,i); //返回的波长编号
            if (searchwaveNum == -1) {
                return false;//第一条路径不满足带宽请求，寻找下一条路径
                } else {
                    //这个地方取出来的是属于2行的一整条路径（最短路、次短路。。。）
                    //判断第二条、第三条边是否满足，满足sanmeNum+1
                    sameNum=sameNum+1;
                    sameNum=sameNum+secondThirdWave(searchwaveNum,bandwave,edgeList);
                    if (sameNum==edgeList.size()){
                        markOccupiedWavelength(searchwaveNum, bandwave,subSplited);
                        return true;
                    }else
                        unedegNum=unedegNum+1;
                }
            }
            if (unedegNum==80-bandwave+1){  //如果80-bandware+1次都没有找到的话，就换次短路
               result=false;
            }
        return result;
    }
*/

    //方法4：checkUsed函数调用，判断第一条（最短路径）边上的波长资源是否存在,返回波长编号
    public int oneEdgeEnough(int identifiera,int identifierb,int band,int searchNumStart){
        int bandwave = band;
        int v = 0; //记录波长满足后波长所属编号
        //遍历所有Link
        int z=0;
        //遍历edge，找到a，b对应的边，从头判断波长是否占用
        do {
            int f =0;   //记录被占用的连续波长数
            if (edge.get(z).srcSeq == identifiera && edge.get(z).dstSeq == identifierb) {
                //判断一致性，是否被占用
                for (int n=searchNumStart; n <=80-bandwave+1; n++) {   //n一直加，但要保持bandware的距离，不然总带宽会超过80
                    if (edge.get(z).wavelengths.get(n).isUsed) {
                        v=0;
                    }else{
                        f=f+1;
                        if (f==bandwave){
                            v=n;   //所在波长编号
                            return v-bandwave+1;
                        }
                    }
                }
                break;
            }
            z++;
        }while (z < edge.size());
        return -1;
    }

    //方法5：checkUsed函数调用，对第一条路径的第二、三条边进行判断，是否和第一条边波长一致
    public int secondThirdWave(int searchNum ,int bandwave,List<AccessEdge> edgeList){
        int sameNum=0;
        boolean result;        //函数istheSame返回值（判断后面的边和第一条是否波长一致）
        for (int y = 1; y < edgeList.size(); y++) {
            int c = edgeList.get(y).getSource().identifier;
            int d = edgeList.get(y).getDest().identifier;
            result=istheSame(searchNum, bandwave, c, d);
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
        if(searchNum1 >80){
            System.out.println("此处应有断点");
        }
        boolean result=false;
        do {
            int f =0;   //记录被占用的连续波长数
            if (edge.get(z).srcSeq == c && edge.get(z).dstSeq == d) {
                //判断一致性，是否被占用
                for (int n=searchNum; n <searchNum1; n++) {   //n一直加，但要保持bandware的距离，不然总带宽会超过80
                    if(n<=0 || n>80){
//                        int m = 1;
                    }
                    if (edge.get(z).wavelengths.get(n).isUsed) {
                        return false;
                    }else{
                        f=f+1;
                        if (f==searchNum1-searchNum){
                            return true;
                        }
                    }
                }
            }
            z++;
        }while (z < edge.size());
        return result;
    }

    //方法7：判断波长能用之后，对波长isUsed进行占用
    public void markOccupiedWavelength(int searchNum,int banware,List<Integer> subSplited){
        int searchNum1=searchNum+banware;
        //k条路径进行遍历标记
        for (int y = 0; y < subSplited.size()-1; y++) {
            int c = subSplited.get(y);
            int d = subSplited.get(y+1);
            //找到对应的边,并进行标记
            for (int z=0;z<edge.size();z++) {
                if (edge.get(z).srcSeq == c && edge.get(z).dstSeq == d) {
                    for (int n = searchNum; n < searchNum1; n++) {
                        edge.get(z).wavelengths.get(n).isUsed = true;  //标记已被占用
                        edge.get(z).wavelengths.get(n).waveserviceID=serviceID;     //标记占用该波长的业务
                    }
                }
                /*else if(edge.get(z).srcSeq == d && edge.get(z).dstSeq == c){
                    for (int n = searchNum; n < searchNum1; n++) {
                        edge.get(z).wavelengths.get(n).isUsed = true;  //标记已被占用
                        edge.get(z).wavelengths.get(n).waveserviceID=serviceID;     //标记占用该波长的业务
                    }
                }*/
            }
        }
    }

    //以下两种方法是对上面方法进行的修改
    //路径跨了几个域后，对其中一个单域进行路径处理
    public boolean subCheckUsed1(int band,List<AccessEdge> edgeList,int edgedomain1,List<Integer> subSplited) {
        int bandwave = band;
        int searchwaveNum;     //函数oneEdgeEnough返回值
        int unedegNum=0;       //记录波长遍历完后仍然没找到的不可用的数
        boolean result = false; // 返回值
        //先获取第x条路径的第一条边
        if (subSplited.size()==1) {            //如果这个域只有一个节点，返回true
            return true;
        }else if (subSplited.size()==2){      //如果这个域只有两个节点（即一条边），则判断是否有足够波长资源
            int identifiera = subSplited.get(0);
            int identifierb = subSplited.get(1);
            int occpuiedNum=0;
            for (int j=0;j< edge.size();j++){
                // TODO 没有mark标记
                if (edge.get(j).srcSeq==identifiera && edge.get(j).dstSeq==identifierb){
                    for (int i = 1; i <= 80 - bandwave + 1; i++){
                        if(!edge.get(j).wavelengths.get(i).isUsed){
                            occpuiedNum=occpuiedNum+1;
                        }else {
                            occpuiedNum =0;
                        }
                        if (occpuiedNum==band)
                            markOccupiedWavelength(i,band,subSplited);
                            return true;
                    }
                }
            }
        } else {
            int identifiera = subSplited.get(0);
            int identifierb = subSplited.get(1);
            //取符合第一条边带宽请求的波长编号
            for (int i = 1; i <= 80 - bandwave + 1; i++) {
                int sameNum=0;        //记录每条边是否都满足波长区间
                searchwaveNum = oneEdgeEnough(identifiera, identifierb, band, i); //返回的波长编号
                if (searchwaveNum == -1) {
                    return  false;//第一条路径不满足带宽请求，寻找下一条路径
                } else {
                    //这个地方取出来的是属于2行的一整条路径（最短路、次短路。。。）
                    //判断第二条、第三条边是否满足，满足sanmeNum+1
                    sameNum = sameNum + 1;
                    sameNum = sameNum + subsecondThirdWave1(searchwaveNum, bandwave, edgeList, subSplited);
                    if (sameNum == subSplited.size()-1) {
                        markOccupiedWavelength(searchwaveNum, bandwave, subSplited);
                        return true;
                    } else
                        unedegNum = unedegNum + 1;
                }
            }
            if (unedegNum == 80 - bandwave + 1) {  //如果80-bandware+1次都没有找到的话，就换次短路
                return false;
            }
        }
        return false;
    }
    //对该方法进行改造，同上
    public int subsecondThirdWave1(int searchNum ,int bandwave,List<AccessEdge> edgeList,List<Integer> splitedSubLen){
        int sameNum=0;
        boolean result;        //函数istheSame返回值（判断后面的边和第一条是否波长一致）
        for (int y = 1; y < splitedSubLen.size()-1; y++) {
            int c = splitedSubLen.get(y);
            int d = splitedSubLen.get(y+1);
            result=istheSame(searchNum, bandwave, c, d);
            if (result){
                sameNum = sameNum+1;
            }
        }
        return sameNum;
    }
}

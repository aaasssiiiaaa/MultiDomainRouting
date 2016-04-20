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
 * ����ҵ�����󡪡���·���ж�ռ�á����䲨��������ռ�ò�����
 * 1. calculatePath������List<Graph<Node,Node>>�������������·���ζ�·��k�ζ�·
 * 2. domainWaveCalculate����������������жϣ��ٵ���checkUsed����
 *    (1) subsecondThirdWave1/2������int ͳ������
 *          (1.1) subistheSam1/2����
 * 3. checkUsed������boolean�������ж�����������Ƿ�ռ�ã���domainWaveCalculate����
 *    (1) oneEdgeEnough������int������ţ������ж����·�Ƿ��в�����Դ�����з������ڲ�����ţ�û��Ѱ�Ҵζ�·
 *    (2) secondThirdWave������int ͳ�����������жϴζ�·��k�ζ�·�����·���صĲ�������Ƿ��в�����Դ���㣬����int
 *                                             ��checkUsed�������жϣ�ͳ����=k����˵�������߶�����ò������
 *                                             ͳ������=k�����·���++���ٴ��жϴζ�·��ѭ����
 *                                             ѭ����֮�󣬻���û���ҵ�����ģ�����ζ�·
 *        (1.1) istheSam���������ж�secondThirdWave�����в����Ƿ�һ��
 * 4. markOccupiedWavelength�����������Ѿ�ռ�õĲ������б�ǣ���checkUsed������
 *
 */

public class PathCalculate {
    //ȫ�ֱ���
    public List<Node> vertex;          //List��Ϊvertex������������Node��ļ�������,ȫ�ֱ���
    public List<Link> edge;
    public int serviceID;              //������Դ��ҵ��ID

    //���캯��������ʼ��ȫ�ֱ���
    public PathCalculate(List<Node> Nodes, List<Link> Links,int eventID){
        vertex = Nodes;
        edge = Links;
        serviceID = eventID;
    }

    //����1��K��·,Path���ͣ�����class Path��,���Ϊͼg/g1��Դ�ڵ㡢Ŀ�Ľڵ㣻����k�����·������ʽList<GraphPath<Node, Node>>
    public Path calculatePath(DefaultDirectedWeightedGraph ggg, int Source, int Dest) {
        Path pa = new Path();
        //K�㷨ȡ3�����·��
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

    //����2�����򻮷�·���ıߣ����ڲ���һ�£�����checkUsed���������򲨳���һ�£�����Ե���ҳ�
    public boolean domainWaveCalculate(Path p,int band){
        int bandwave = band;  //ÿ����������Ϊ3M
        int edgedomain1=0;
        int edgedomain2=0;
//        int pathNum=0;         //��¼������k��·����Ĳ����õ�·����
        if (bandwave > 80)
            return false;
            //k��·����������̡��ζ̡�k�ζ�
            for (int x=0; x< p.nodes.size();x++) {
                List<AccessEdge> edgeList = p.nodes.get(x).getEdgeList();
                int edgesize = edgeList.size();
                // splitedSubPaths����һ��path�Ķ����ľ����ĵ㡣
                List<List<Integer>> splitedSubPaths = new ArrayList<List<Integer>>();
                int nxtsrc = edgeList.get(0).getSource().identifier;
                List<Integer> subPath = new ArrayList<Integer>();
                subPath.add(edgeList.get(0).getSource().identifier);
                for (int y = 0; y < edgeList.size(); y++) {
                    //�����ҵ���ͬ�򣨱�Ե���������ڵ���Ϊ�ϵ㣬����Ϊ���㲻ͬ·���������յ�
                    AccessEdge accessEdge = edgeList.get(y);
                    int srcDomain = accessEdge.getSource().domain;
                    int dstDomain = accessEdge.getDest().domain;
                    // ���edge��ͷ����ͬһ����
                    if (srcDomain != dstDomain) {
                        //����ڲ�ͬ�򣬷ֳɵ�����������Դ
                       // int subdet = accessEdge.getSource().identifier;
//                        map.add(new Pair<Integer, Integer>(nxtsrc, subdet));
                        // �Ȱ���һ��������ݱ���һ��
                        splitedSubPaths.add(subPath);
                        // ���½�һ��List���ڱ�����һ���������
                        subPath = new ArrayList<Integer>();
                        subPath.add(accessEdge.getDest().identifier);
                        //nxtsrc = accessEdge.getDest().identifier;
                    }else {
                        // ���edge��ͷͬ��һ����
                        subPath.add(accessEdge.getDest().identifier);
                    }
                    // �����һ����Ž�ȥ��
                    if(y==edgeList.size()-1){
                        splitedSubPaths.add(subPath);
                    }
                }
                //�ж����������Ƿ񶼷ֱ����㲨��һ��
                int j=0;
                for (int i=0;i<splitedSubPaths.size();i++){
                    boolean singleDomainUsed=subCheckUsed1(band,edgeList, edgedomain1,splitedSubPaths.get(i));
                    if (singleDomainUsed)
                        j=j+1;
                }
                if (j==splitedSubPaths.size()) {      //�������򶼿���
                    return true;
                }else{
                    // �ͷ�֮ǰ����Դ��
                    DepartWaveRelease release =new  DepartWaveRelease(serviceID,edge);
                    release.releaseWaveResource();
                }
                //ȫ����ͬһ�����ڣ����÷���checkUsed
//                checkUsed(edgeList, band);
//                if (checkUsed(edgeList, band)) {
//                    return true;
//                }
            }
     return false;
    }
/*
    //����3����������Ĵ����ж��Ƿ�ռ��,����һ����,�����K���·����pa�����������͵ڼ���·����
    public boolean checkUsed(List<AccessEdge>  edgeList, int band) {
        int bandwave = band;
        int searchwaveNum;     //����oneEdgeEnough����ֵ
        int sameNum=0;        //��¼ÿ�����Ƿ����㲨������
        int unedegNum=0;       //��¼�������������Ȼû�ҵ��Ĳ����õ���
        boolean result = false; // ����ֵ
        //�Ȼ�ȡ��x��·���ĵ�һ����
        int a = edgeList.get(0).getSource().identifier;
        int b = edgeList.get(0).getDest().identifier;
        //ȡ���ϵ�һ���ߴ�������Ĳ������
        for (int i=1;i<=80-bandwave+1;i++){
            searchwaveNum = oneEdgeEnough(a, b, band,i); //���صĲ������
            if (searchwaveNum == -1) {
                return false;//��һ��·���������������Ѱ����һ��·��
                } else {
                    //����ط�ȡ������������2�е�һ����·�������·���ζ�·��������
                    //�жϵڶ��������������Ƿ����㣬����sanmeNum+1
                    sameNum=sameNum+1;
                    sameNum=sameNum+secondThirdWave(searchwaveNum,bandwave,edgeList);
                    if (sameNum==edgeList.size()){
                        markOccupiedWavelength(searchwaveNum, bandwave,subSplited);
                        return true;
                    }else
                        unedegNum=unedegNum+1;
                }
            }
            if (unedegNum==80-bandwave+1){  //���80-bandware+1�ζ�û���ҵ��Ļ����ͻ��ζ�·
               result=false;
            }
        return result;
    }
*/

    //����4��checkUsed�������ã��жϵ�һ�������·�������ϵĲ�����Դ�Ƿ����,���ز������
    public int oneEdgeEnough(int identifiera,int identifierb,int band,int searchNumStart){
        int bandwave = band;
        int v = 0; //��¼��������󲨳��������
        //��������Link
        int z=0;
        //����edge���ҵ�a��b��Ӧ�ıߣ���ͷ�жϲ����Ƿ�ռ��
        do {
            int f =0;   //��¼��ռ�õ�����������
            if (edge.get(z).srcSeq == identifiera && edge.get(z).dstSeq == identifierb) {
                //�ж�һ���ԣ��Ƿ�ռ��
                for (int n=searchNumStart; n <=80-bandwave+1; n++) {   //nһֱ�ӣ���Ҫ����bandware�ľ��룬��Ȼ�ܴ���ᳬ��80
                    if (edge.get(z).wavelengths.get(n).isUsed) {
                        v=0;
                    }else{
                        f=f+1;
                        if (f==bandwave){
                            v=n;   //���ڲ������
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

    //����5��checkUsed�������ã��Ե�һ��·���ĵڶ��������߽����жϣ��Ƿ�͵�һ���߲���һ��
    public int secondThirdWave(int searchNum ,int bandwave,List<AccessEdge> edgeList){
        int sameNum=0;
        boolean result;        //����istheSame����ֵ���жϺ���ıߺ͵�һ���Ƿ񲨳�һ�£�
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

    //����6��secondThirdWave��������
    public boolean istheSame(int searchNum,int bandware,int c,int d){
        int z=0;
        int searchNum1=searchNum+ bandware;
        if(searchNum1 >80){
            System.out.println("�˴�Ӧ�жϵ�");
        }
        boolean result=false;
        do {
            int f =0;   //��¼��ռ�õ�����������
            if (edge.get(z).srcSeq == c && edge.get(z).dstSeq == d) {
                //�ж�һ���ԣ��Ƿ�ռ��
                for (int n=searchNum; n <searchNum1; n++) {   //nһֱ�ӣ���Ҫ����bandware�ľ��룬��Ȼ�ܴ���ᳬ��80
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

    //����7���жϲ�������֮�󣬶Բ���isUsed����ռ��
    public void markOccupiedWavelength(int searchNum,int banware,List<Integer> subSplited){
        int searchNum1=searchNum+banware;
        //k��·�����б������
        for (int y = 0; y < subSplited.size()-1; y++) {
            int c = subSplited.get(y);
            int d = subSplited.get(y+1);
            //�ҵ���Ӧ�ı�,�����б��
            for (int z=0;z<edge.size();z++) {
                if (edge.get(z).srcSeq == c && edge.get(z).dstSeq == d) {
                    for (int n = searchNum; n < searchNum1; n++) {
                        edge.get(z).wavelengths.get(n).isUsed = true;  //����ѱ�ռ��
                        edge.get(z).wavelengths.get(n).waveserviceID=serviceID;     //���ռ�øò�����ҵ��
                    }
                }
                /*else if(edge.get(z).srcSeq == d && edge.get(z).dstSeq == c){
                    for (int n = searchNum; n < searchNum1; n++) {
                        edge.get(z).wavelengths.get(n).isUsed = true;  //����ѱ�ռ��
                        edge.get(z).wavelengths.get(n).waveserviceID=serviceID;     //���ռ�øò�����ҵ��
                    }
                }*/
            }
        }
    }

    //�������ַ����Ƕ����淽�����е��޸�
    //·�����˼�����󣬶�����һ���������·������
    public boolean subCheckUsed1(int band,List<AccessEdge> edgeList,int edgedomain1,List<Integer> subSplited) {
        int bandwave = band;
        int searchwaveNum;     //����oneEdgeEnough����ֵ
        int unedegNum=0;       //��¼�������������Ȼû�ҵ��Ĳ����õ���
        boolean result = false; // ����ֵ
        //�Ȼ�ȡ��x��·���ĵ�һ����
        if (subSplited.size()==1) {            //��������ֻ��һ���ڵ㣬����true
            return true;
        }else if (subSplited.size()==2){      //��������ֻ�������ڵ㣨��һ���ߣ������ж��Ƿ����㹻������Դ
            int identifiera = subSplited.get(0);
            int identifierb = subSplited.get(1);
            int occpuiedNum=0;
            for (int j=0;j< edge.size();j++){
                // TODO û��mark���
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
            //ȡ���ϵ�һ���ߴ�������Ĳ������
            for (int i = 1; i <= 80 - bandwave + 1; i++) {
                int sameNum=0;        //��¼ÿ�����Ƿ����㲨������
                searchwaveNum = oneEdgeEnough(identifiera, identifierb, band, i); //���صĲ������
                if (searchwaveNum == -1) {
                    return  false;//��һ��·���������������Ѱ����һ��·��
                } else {
                    //����ط�ȡ������������2�е�һ����·�������·���ζ�·��������
                    //�жϵڶ��������������Ƿ����㣬����sanmeNum+1
                    sameNum = sameNum + 1;
                    sameNum = sameNum + subsecondThirdWave1(searchwaveNum, bandwave, edgeList, subSplited);
                    if (sameNum == subSplited.size()-1) {
                        markOccupiedWavelength(searchwaveNum, bandwave, subSplited);
                        return true;
                    } else
                        unedegNum = unedegNum + 1;
                }
            }
            if (unedegNum == 80 - bandwave + 1) {  //���80-bandware+1�ζ�û���ҵ��Ļ����ͻ��ζ�·
                return false;
            }
        }
        return false;
    }
    //�Ը÷������и��죬ͬ��
    public int subsecondThirdWave1(int searchNum ,int bandwave,List<AccessEdge> edgeList,List<Integer> splitedSubLen){
        int sameNum=0;
        boolean result;        //����istheSame����ֵ���жϺ���ıߺ͵�һ���Ƿ񲨳�һ�£�
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

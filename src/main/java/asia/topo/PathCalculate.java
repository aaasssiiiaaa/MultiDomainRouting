package asia.topo;

import org.jgrapht.alg.KShortestPaths;
import org.jgrapht.graph.DefaultDirectedWeightedGraph;
import java.util.List;

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
    public Path calculatePath(DefaultDirectedWeightedGraph ggg, Node Source, Node Dest) {
        Path pa = new Path();
        //K�㷨ȡ3�����·��
        int k = 3;
        KShortestPaths<Node, Node> ksp = new KShortestPaths<Node, Node>(ggg, Source, k);
        pa.nodes = ksp.getPaths(Dest);
        return pa;
    }

    //����2�����򻮷�·���ıߣ����ڲ���һ�£�����checkUsed���������򲨳���һ�£�����Ե���ҳ�
    public boolean domainWaveCalculate(Path p,int band){
        int bandwave = (int) Math.ceil(band / 3);  //ÿ����������Ϊ3M
        int edgedomain1=0;
        int edgedomain2=0;
        int pathNum=0;         //��¼������k��·����Ĳ����õ�·����
        boolean result = false;     // ���淵��ֵ
        if (bandwave > 80)
            return false;
            //k��·����������̡��ζ̡�k�ζ�
            for (int x=0; x< p.nodes.size();x++){
                int edgesize=p.nodes.get(x).getEdgeList().size();
                int y=0;
                do {
                    //�����ҵ���ͬ�򣨱�Ե���������ڵ���Ϊ�ϵ㣬����Ϊ���㲻ͬ·���������յ�
                    int a = p.nodes.get(x).getEdgeList().subList(y, y+1).get(0).domain;
                    int b = p.nodes.get(x).getEdgeList().subList(y, y+1).get(1).domain;
                    if (a!=b) {
                        //����ڲ�ͬ�򣬷ֳ�����������������Դ
                        edgedomain1=p.nodes.get(x).getEdgeList().subList(y, y+1).get(0).identifier;//��һ����Ľ�����
                        edgedomain2=p.nodes.get(x).getEdgeList().subList(y, y+1).get(1).identifier;//�ڶ��������ʼ��
                        subCheckUsed1(p, band, x, edgedomain1);
                        subCheckUsed2(p, band, x, edgedomain2);
                    }
                    y++;
                }while (y<edgesize);
                //ȫ����ͬһ�����ڣ����÷���checkUsed
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

    //����3����������Ĵ����ж��Ƿ�ռ��,����һ����,�����K���·����pa�����������͵ڼ���·����
    public boolean checkUsed(Path p, int band,int x) {
        int bandwave = (int) Math.ceil(band / 3);
        int searchwaveNum;     //����oneEdgeEnough����ֵ
        int sameNum=0;        //��¼ÿ�����Ƿ����㲨������
        int unedegNum=0;       //��¼�������������Ȼû�ҵ��Ĳ����õ���
        boolean result = false; // ����ֵ
        //�Ȼ�ȡ��x��·���ĵ�һ����
        int a = p.nodes.get(x).getEdgeList().subList(0, 1).get(0).identifier;
        int b = p.nodes.get(x).getEdgeList().subList(0, 1).get(1).identifier;
        //ȡ���ϵ�һ���ߴ�������Ĳ������
        for (int i=1;i<=80-bandwave+1;i++){
            searchwaveNum = oneEdgeEnough(a, b, band); //���صĲ������
            if (searchwaveNum == 0) {
                return result=false;//��һ��·���������������Ѱ����һ��·��
                } else {
                    //����ط�ȡ������������2�е�һ����·�������·���ζ�·��������
                    //�жϵڶ��������������Ƿ����㣬����sanmeNum+1
                    sameNum=sameNum+1;
                    sameNum=sameNum+secondThirdWave(searchwaveNum,bandwave,x,p);
                    if (sameNum==p.nodes.get(x).getEdgeList().size()){
                        markOccupiedWavelength(searchwaveNum, bandwave, x, p);
                        return result=true;
                    }else
                        unedegNum=unedegNum+1;
                }
            }
            if (unedegNum==80-bandwave+1){  //���80-bandware+1�ζ�û���ҵ��Ļ����ͻ��ζ�·
               result=false;
            }
        return result;
    }


    //����4��checkUsed�������ã��жϵ�һ�������·�������ϵĲ�����Դ�Ƿ����,���ز������
    public int oneEdgeEnough(int a,int b,int band){
        int bandwave = (int) Math.ceil(band / 3);
        int v = 0; //��¼��������󲨳��������
        //��������Link
        int z=0;
        //����edge���ҵ�a��b��Ӧ�ıߣ���ͷ�жϲ����Ƿ�ռ��
        do {
            int f =0;   //��¼��ռ�õ�����������
            if (edge.get(z).srcSeq == a && edge.get(z).dstSeq == b) {
                //�ж�һ���ԣ��Ƿ�ռ��
                for (int n=1; n <=80-bandwave+1; n++) {   //nһֱ�ӣ���Ҫ����bandware�ľ��룬��Ȼ�ܴ���ᳬ��80
                    if (edge.get(z).wavelengths.get(n).isUsed) {
                        v=0;
                    }else{
                        f=f+1;
                        if (f==bandwave){
                            v=n;   //���ڲ������
                            break;
                        }
                    }
                }
            }
            z++;
        }while (z < edge.size());
        return v-bandwave+1;
    }

    //����5��checkUsed�������ã��Ե�һ��·���ĵڶ��������߽����жϣ��Ƿ�͵�һ���߲���һ��
    public int secondThirdWave(int searchNum ,int bandwave,int x,Path p){
        int sameNum=0;
        boolean result;        //����istheSame����ֵ���жϺ���ıߺ͵�һ���Ƿ񲨳�һ�£�
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

    //����6��secondThirdWave��������
    public boolean istheSame(int searchNum,int bandware,int c,int d){
        int z=0;
        int searchNum1=searchNum+ bandware;
        boolean result=false;
        do {
            int f =0;   //��¼��ռ�õ�����������
            if (edge.get(z).srcSeq == c && edge.get(z).dstSeq == d) {
                //�ж�һ���ԣ��Ƿ�ռ��
                for (int n=searchNum; n <=searchNum1; n++) {   //nһֱ�ӣ���Ҫ����bandware�ľ��룬��Ȼ�ܴ���ᳬ��80
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

    //����7���жϲ�������֮�󣬶Բ���isUsed����ռ��
    public void markOccupiedWavelength(int searchNum,int banware,int x,Path p){
        int searchNum1=searchNum+banware;
        //k��·�����б������
        for (int y = 0; y < p.nodes.get(x).getEdgeList().size(); y++) {
            int c = p.nodes.get(x).getEdgeList().subList(y, y+1).get(0).identifier;
            int d = p.nodes.get(x).getEdgeList().subList(y, y+1).get(1).identifier;
            //�ҵ���Ӧ�ı�,�����б��
            for (int z=0;z<edge.size();z++) {
                if (edge.get(z).srcSeq == c && edge.get(z).dstSeq == d) {
                    for (int n = searchNum; n < searchNum1; n++) {
                        edge.get(z).wavelengths.get(n).isUsed = true;  //����ѱ�ռ��
                        edge.get(z).wavelengths.get(n).waveserviceID=serviceID;     //���ռ�øò�����ҵ��
                    }
                }
            }
        }
    }

    //�������ַ����Ƕ����淽�����е��޸�
    //·�����˼�����󣬶�����һ���������·������
    public boolean subCheckUsed1(Path p, int band,int x,int edgedomain1) {
        int bandwave = (int) Math.ceil(band / 3);
        int searchwaveNum;     //����oneEdgeEnough����ֵ
        int sameNum=0;        //��¼ÿ�����Ƿ����㲨������
        int unedegNum=0;       //��¼�������������Ȼû�ҵ��Ĳ����õ���
        boolean result = false; // ����ֵ
        //�Ȼ�ȡ��x��·���ĵ�һ����
        int a = p.nodes.get(x).getEdgeList().subList(0, 1).get(0).identifier;
        int b = p.nodes.get(x).getEdgeList().subList(0, 1).get(1).identifier;
        //ȡ���ϵ�һ���ߴ�������Ĳ������
        for (int i=1;i<=80-bandwave+1;i++){
            searchwaveNum = oneEdgeEnough(a, b, band); //���صĲ������
            if (searchwaveNum == 0) {
                return result=false;//��һ��·���������������Ѱ����һ��·��
            } else {
                //����ط�ȡ������������2�е�һ����·�������·���ζ�·��������
                //�жϵڶ��������������Ƿ����㣬����sanmeNum+1
                sameNum=sameNum+1;
                sameNum=sameNum+subsecondThirdWave1(searchwaveNum, bandwave, x, p,edgedomain1);
                if (sameNum==p.nodes.get(x).getEdgeList().size()){
                    markOccupiedWavelength(searchwaveNum, bandwave, x, p);
                    return result=true;
                }else
                    unedegNum=unedegNum+1;
            }
        }
        if (unedegNum==80-bandwave+1){  //���80-bandware+1�ζ�û���ҵ��Ļ����ͻ��ζ�·
            result=false;
        }
        return result;
    }
    //�Ը÷������и��죬ͬ��
    public int subsecondThirdWave1(int searchNum ,int bandwave,int x,Path p,int edgedomain1){
        int sameNum=0;
        boolean result;        //����istheSame����ֵ���жϺ���ıߺ͵�һ���Ƿ񲨳�һ�£�
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
        int searchwaveNum;     //����oneEdgeEnough����ֵ
        int sameNum=0;        //��¼ÿ�����Ƿ����㲨������
        int unedegNum=0;       //��¼�������������Ȼû�ҵ��Ĳ����õ���
        boolean result = false; // ����ֵ
        //�Ȼ�ȡ��x��·���ĵ�һ����
        int a = p.nodes.get(x).getEdgeList().subList(0, 1).get(0).identifier;
        int b = p.nodes.get(x).getEdgeList().subList(0, 1).get(1).identifier;
        //ȡ���ϵ�һ���ߴ�������Ĳ������
        for (int i=1;i<=80-bandwave+1;i++){
            searchwaveNum = oneEdgeEnough(a, b, band); //���صĲ������
            if (searchwaveNum == 0) {
                return result=false;//��һ��·���������������Ѱ����һ��·��
            } else {
                //����ط�ȡ������������2�е�һ����·�������·���ζ�·��������
                //�жϵڶ��������������Ƿ����㣬����sanmeNum+1
                sameNum=sameNum+1;
                sameNum=sameNum+subsecondThirdWave2(searchwaveNum, bandwave, x, p, edgedomain2);
                if (sameNum==p.nodes.get(x).getEdgeList().size()){
                    markOccupiedWavelength(searchwaveNum, bandwave, x, p);
                    return result=true;
                }else
                    unedegNum=unedegNum+1;
            }
        }
        if (unedegNum==80-bandwave+1){  //���80-bandware+1�ζ�û���ҵ��Ļ����ͻ��ζ�·
            result=false;
        }
        return result;
    }
    //�Ը÷������и��죬ͬ��
    public int subsecondThirdWave2(int searchNum ,int bandwave,int x,Path p,int edgedomain2){
        int sameNum=0;
        boolean result;        //����istheSame����ֵ���жϺ���ıߺ͵�һ���Ƿ񲨳�һ�£�
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

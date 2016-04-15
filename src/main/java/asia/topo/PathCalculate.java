package asia.topo;

import org.jgrapht.alg.KShortestPaths;
import org.jgrapht.graph.DefaultDirectedWeightedGraph;
import java.util.List;

/**
 * Created by 1 on 2016/4/15.
 * ����ҵ�����󡪡���·���ж�ռ�á����䲨��������ռ�ò�����
 * 1. calculatePath������List<Graph<Node,Node>>�������������·���ζ�·��k�ζ�·
 * 2. checkUsed������boolean�������ж�����������Ƿ�ռ��
 *    (1) oneEdgeEnough������int������ţ������ж����·�Ƿ��в�����Դ�����з������ڲ�����ţ�û��Ѱ�Ҵζ�·
 *    (2) secondThirdWave������int ͳ�����������жϴζ�·��k�ζ�·�����·���صĲ�������Ƿ��в�����Դ���㣬����int
 *                                             ��checkUsed�������жϣ�ͳ����=k����˵�������߶�����ò������
 *                                             ͳ������=k�����·���++���ٴ��жϴζ�·��ѭ����
 *                                             ѭ����֮�󣬻���û���ҵ�����ģ�����ζ�·
 *        (1.1)istheSam���������ж�secondThirdWave�����в����Ƿ�һ��
 *    (6) markOccupiedWavelength�����������Ѿ�ռ�õĲ������б�ǣ���checkUsed������
 * 3.��������edge��ռ�õĲ�������
 */
public class PathCalculate {
    public List<Node> vertex;          //List��Ϊvertex������������Node��ļ�������,ȫ�ֱ���
    public List<Link> edge;
    public PathCalculate(List<Node> Nodes, List<Link> Links){
        vertex = Nodes;
        edge = Links;
    }

    //K��·,Path���ͣ�����class Path��,���Ϊͼg/g1��Դ�ڵ㡢Ŀ�Ľڵ㣻����k�����·������ʽList<GraphPath<Node, Node>>
    public Path calculatePath(DefaultDirectedWeightedGraph ggg, Node Source, Node Dest) {
        Path pa = new Path();
        //K�㷨ȡ3�����·��
        int k = 3;
        KShortestPaths<Node, Node> ksp = new KShortestPaths<Node, Node>(ggg, Source, k);
        pa.nodes = ksp.getPaths(Dest);
        return pa;
    }

    //��������Ĵ����ж��Ƿ�ռ��,����һ����,�����K���·����pa������������edge(Graph.edge)��
    public boolean checkUsed(Path p, int band) {
        int bandwave = (int) Math.ceil(band / 3);  //�ݶ�ÿ����������Ϊ3M
        int searchwaveNum;     //����oneEdgeEnough����ֵ
        int sameNum=0;        //��¼ÿ�����Ƿ����㲨������
        int unedegNum=0;       //��¼���������Ȼû�ҵ��Ĳ����õ���
        int pathNum=0;         //��¼�����õ�·����
        boolean result = false; // ����ֵ
        //��ȡ����·��
        for (int x = 0; x < p.nodes.size(); x++) {
            if (bandwave > 80)
                return false;
            //�Ȼ�ȡ��һ��·���ĵ�һ����
            int a = p.nodes.get(x).getEdgeList().subList(0, 1).get(0).identifier;
            int b = p.nodes.get(x).getEdgeList().subList(0, 1).get(1).identifier;
            //��ȡVֵ����ȡ�õ�һ���ߴ�������Ĳ������
            for (int i=1;i<=80-bandwave+1;i++){
                searchwaveNum = oneEdgeEnough(a, b, band);
                if (searchwaveNum == 0) {
                    break;//��һ��·���������������Ѱ����һ��·��
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
                pathNum=pathNum+1;
                continue;
            }
        }
        if (pathNum==p.nodes.size())      //���k�ζ�û�з��ϵ�·���Ļ����ͷ���false
            result=false;
        return result;
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
            if (edge.get(z).srcSeq == aa && edge.get(z).dstSeq == bb) {
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

    //�Ե�һ��·���ĵڶ��������߽����жϣ��Ƿ�͵�һ���߲���һ��
    public int secondThirdWave(int searchNum ,int bandwave,int x,Path p){
        int sanmeNum=0;
        boolean result;        //����istheSame����ֵ���жϺ���ıߺ͵�һ���Ƿ񲨳�һ�£�
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
            int f =0;   //��¼��ռ�õ�����������
            if (edge.get(z).srcSeq == aa && edge.get(z).dstSeq == bb) {
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

    //�жϲ�������֮�󣬶Բ���isUsed����ռ��
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
                        edge.get(z).wavelengths.get(n).isUsed = true;
                    }
                }
            }
        }
    }

    //��������edge�б�ռ�õĲ�������
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

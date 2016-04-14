package asia.utils;

import asia.topo.Link;
import asia.topo.Node;
import com.alibaba.fastjson.JSONReader;
import com.alibaba.fastjson.JSONWriter;

import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by root on 16-4-14.
 * Read topology information from file named topo.xls in root location of this project.
 *
 * sheet type in topo.xls:
 * 1. nodes
 * 2. domain{n}
 * 3. multidomain
 */
public class ReadTopo {

    private static final String fileName = "topo.json";

    public static void readJsonFile() throws Exception{
        FileReader reader = new FileReader(fileName);
        JSONReader jsonReader = new JSONReader(reader);
        Object object = jsonReader.readObject();
        List<Domain> list = (List<Domain>)object;
        System.out.print(object.toString());
    }

    /**
     * generate a List<DomainOb> instance to topo.json
     * @param domainNum number of domains
     * @param nodeNumPerDomain number of node per domain
     * @param LinkRate the value of linksNum/nodesNum^2
     */
    public static void generateJsonFile(int domainNum, int nodeNumPerDomain, int LinkRate) throws Exception{
//        int addedLinksNum = (nodeNumPerDomain*domainNum)^2*LinkRate/100 - nodeNumPerDomain*(domainNum+1);
        MultiDomain multiDomain = new MultiDomain();
        List<Domain> domainObList = new ArrayList<Domain>(domainNum);
        List<Link> interDomainLinks = new ArrayList<Link>(domainNum);
        multiDomain.domains = domainObList;
        multiDomain.interDomainLinks = interDomainLinks;
        int nodeIdentifier=0;
        // 先构造出互相没有连接的域
        for(int i=0; i<domainNum; i++){
            Domain domain = new Domain();
            domain.domainSeq = i;
            domain.nodes = new ArrayList<Node>(nodeNumPerDomain);
            domain.links = new ArrayList<Link>(nodeNumPerDomain*2);
            // 每次循环都要构造一个环形的域
            for(int j=0; j<nodeNumPerDomain; j++){
                Node node = new Node();
                node.identifier = nodeIdentifier;
                nodeIdentifier++;
                node.domain = i;
                domain.nodes.add(j, node);
                // 新增的点和前一个点相连
                if(j!=0){
                    Link link = new Link();
                    link.src = domain.nodes.get(j-1).clone();
                    link.dst = domain.nodes.get(j).clone();
                    link.occupiedWavelengthNum = 0;
                    link.weight = Math.random()*100;
                    link.wavelengths = null;
                    domain.links.add(link);
                }
            }
            // 首尾相接成环
            Link link = new Link();
            link.src = domain.nodes.get(nodeNumPerDomain-1).clone();
            link.dst = domain.nodes.get(0).clone();
            link.occupiedWavelengthNum = 0;
            link.weight = Math.random()*100;
            link.wavelengths = null;
            domain.links.add(link);

            domainObList.add(i, domain);
        }
        // 再构造域之间的环形连接
        for(int k=0; k<domainNum; k++){
            int pre = k-1;
            if(pre==-1){
                pre = domainNum-1;
            }
            Link interLink = new Link();
            interLink.src = domainObList.get(pre).nodes.get(nodeNumPerDomain-1).clone();
            interLink.dst = domainObList.get(k).nodes.get(0).clone();
            interLink.weight = Math.random()*100;
            interLink.occupiedWavelengthNum = 0;
            interLink.wavelengths = null;
            interDomainLinks.add(interLink);
        }
        // 到这一步为止，multidomain的赋值结束
        // 接下来，开始写入json文件
        FileWriter fileWriter = new FileWriter(fileName);
        JSONWriter jsonWriter = new JSONWriter(fileWriter);
        jsonWriter.writeObject(multiDomain);
        jsonWriter.close();
    }

    public static void main(String[] args) throws  Exception{
//        readJsonFile();
        generateJsonFile(5, 10,-1);
    }
}


class Domain{
    public int domainSeq;
    public List<Link> links;
    public List<Node> nodes;
}

class MultiDomain{
    public List<Domain> domains;
    public List<Link> interDomainLinks;
}
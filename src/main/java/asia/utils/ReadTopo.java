package asia.utils;

import asia.topo.Link;
import asia.topo.Node;
import com.alibaba.fastjson.JSONReader;
import com.alibaba.fastjson.JSONWriter;
import com.alibaba.fastjson.serializer.IntegerCodec;

import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by root on 16-4-14.
 * Read topology information from file named topo.xls in root location of this project.
 *
 */
public class ReadTopo {

    private static final String fileName = "topo.json";
    private static MultiDomain multiDomain;


    /**
     * 读取json文件的内容，转换成multidomain对象
     * @throws Exception
     */
    public static void readJsonFile() throws Exception{
        FileReader reader = new FileReader(fileName);
        JSONReader jsonReader = new JSONReader(reader);
        Object object = jsonReader.readObject();
        multiDomain = (MultiDomain)object;
    }

    /**
     * generate a List<DomainOb> instance and write it to topo.json
     * 默认没有给wavelength赋值
     * @param domainNum number of domains
     * @param nodeNumPerDomain number of node per domain
     * @param LinkRate the value of linksNum/nodesNum^2 这个参数暂时没有用到，因为我发现这种生成拓扑的方式不太合理
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
                    link.srcSeq = domain.nodes.get(j-1).identifier;
                    // 度需要加一
                    domain.nodes.get(j-1).degree++;
                    link.dstSeq = domain.nodes.get(j).identifier;
                    // 度需要加一
                    domain.nodes.get(j).degree++;
                    link.occupiedWavelengthNum = 0;
                    link.weight = Math.random()*100;
                    link.wavelengths = null;
                    domain.links.add(link);
                }
            }
            // 首尾相接成环
            Link link = new Link();
            link.srcSeq = domain.nodes.get(nodeNumPerDomain-1).identifier;
            domain.nodes.get(nodeNumPerDomain-1).degree++;
            link.dstSeq = domain.nodes.get(0).identifier;
            domain.nodes.get(0).degree++;
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
            interLink.srcSeq = domainObList.get(pre).nodes.get(nodeNumPerDomain-1).identifier;
            domainObList.get(pre).nodes.get(nodeNumPerDomain-1).degree++;
            interLink.dstSeq = domainObList.get(k).nodes.get(0).identifier;
            domainObList.get(k).nodes.get(0).degree++;
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

    /**
     * 读取完json文件以后，获取拓扑中的节点列表
     * 节点编号和节点下标完全一致
     * @return List<Node>
     */
    public static List<Node> obtainNode(){
        List<Node> nodes = new ArrayList<Node>();
        for(Domain domain : multiDomain.domains){
            for(Node node : domain.nodes){
                nodes.add(node.identifier, node);
            }
        }
        return nodes;
    }

    /**
     * 读取完json文件以后，获取拓扑中的链路列表，链路列表分为域内链路和域间链路
     * @return List<Link>
     */
    public static List<Link> obtainLink(){
        List<Link> links = new ArrayList<Link>();
        for(Domain domain : multiDomain.domains){
            links.addAll(domain.links);
        }
        links.addAll(multiDomain.interDomainLinks);
        return links;
    }


    public static void main(String[] args) throws Exception{
        generateJsonFile(3, 5,-1);
    }
}


/**
 * 一个单域应该具备的内容，不包括跨域链路. 默认没有给wavelength赋值
 */
class Domain{
    public int domainSeq;
    public List<Link> links;
    public List<Node> nodes;
}

/**
 * 一个多域拓扑应该包含的内容，包括各个单域以及域间链路. 默认没有给wavelength赋值
 */
class MultiDomain{
    public List<Domain> domains;
    public List<Link> interDomainLinks;
}
package asia.topo;

//List Nodes中包含以下方面内容
public class Node implements Cloneable{
    public int identifier;                       //节点命名
    public int domain;                        //节点所在的域
    public int degree;                        //节点的度（一个节点所连边的个数）

    @Override
    public Node clone() throws CloneNotSupportedException {
        Node node = new Node();
        node.identifier = identifier;
        node.domain = domain;
        node.degree = degree;
        return node;
    }
}

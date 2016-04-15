package asia.topo;

//List Nodes�а������·�������
public class Node implements Cloneable{
    public int identifier;                       //�ڵ�����
    public int domain;                        //�ڵ����ڵ���
    public int degree;                        //�ڵ�Ķȣ�һ���ڵ������ߵĸ�����

    @Override
    public Node clone() throws CloneNotSupportedException {
        Node node = new Node();
        node.identifier = identifier;
        node.domain = domain;
        node.degree = degree;
        return node;
    }
}

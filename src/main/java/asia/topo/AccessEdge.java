package asia.topo;

import org.jgrapht.graph.DefaultEdge;

/**
 * Created by 1 on 2016/4/15.
 */
public class AccessEdge extends DefaultEdge{

    public Node getSource(){
        return (Node)super.getSource();
    }

    public Node getDest(){

        return (Node)getTarget();
    }
}

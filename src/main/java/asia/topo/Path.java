package asia.topo;

import org.jgrapht.GraphPath;

import java.util.List;

/**
 * 属性区域：Path、Link、Wavelength、Node
 * 属性可以是基本定义，也可以是方法。只要属于这一类，其中的定义和方法都具备。
 */
//路径的属性特征，每条都要满足
public class Path {
    List<GraphPath<Node, Node>> nodes;     //链表，有下一跳
    int wavelengthNum;          //波长数
    //wl id
}

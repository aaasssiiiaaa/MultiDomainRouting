package asia.topo;

import java.util.List;

/**
 * Created by root on 16-4-14.
 */
//List Links中包含以下方面内容，具体数值上面给
public class Link {
    public Node src;                        //源点
    public Node dst;                        //宿点
    public double weight;                   //权重
    public int occupiedWavelengthNum;
    public List<Wavelength> wavelengths;    //80个波长
}
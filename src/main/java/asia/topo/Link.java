package asia.topo;

import java.util.List;

/**
 * Created by root on 16-4-14.
 */
//List Links�а������·������ݣ�������ֵ�����
public class Link {
    public int srcSeq;                        //Դ��
    public int dstSeq;                        //�޵�
    public double weight;                   //Ȩ��
    public int occupiedWavelengthNum;
    public List<Wavelength> wavelengths;    //80������

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Link link = (Link) o;

        if (srcSeq != link.srcSeq) return false;
        if (dstSeq != link.dstSeq) return false;
        if (Double.compare(link.weight, weight) != 0) return false;
        if (occupiedWavelengthNum != link.occupiedWavelengthNum) return false;
        return wavelengths != null ? wavelengths.equals(link.wavelengths) : link.wavelengths == null;

    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        result = srcSeq;
        result = 31 * result + dstSeq;
        temp = Double.doubleToLongBits(weight);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        result = 31 * result + occupiedWavelengthNum;
        result = 31 * result + (wavelengths != null ? wavelengths.hashCode() : 0);
        return result;
    }
}
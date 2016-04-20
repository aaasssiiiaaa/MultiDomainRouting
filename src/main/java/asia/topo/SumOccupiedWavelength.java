package asia.topo;

import java.util.List;

/**
 * Created by 1 on 2016/4/15.
 * ���󣺼�����ĳ��ʱ������edge�б�ռ�õĲ�������
 *       ���캯��������ʼ��ȫ�ֱ�����������캯�������������ȫ�ֱ���һ������this.xx�������һ��������θ�ֵ��ȫ�ֱ���
 */
public class SumOccupiedWavelength {
    //ȫ�ֱ���
    public List<Link> edge;
    public double num;

    //���캯��
    public SumOccupiedWavelength(int num, List<Link> links){
        this.num = num;
        edge = links;
    }

    //���������㱻ռ���ܲ�����
    public double sumNumOccupied() {
        double occupuy = 0;
        double wavelengthOccupiedRate;
        for (int i = 0; i < edge.size(); i++) {
            for (int j = 1; j <= 80; j++) {
                if (edge.get(i).wavelengths.get(j).isUsed ) {
                    occupuy = occupuy + 1;
                }
            }
        }
        wavelengthOccupiedRate = occupuy/edge.size()/80.0;
        return wavelengthOccupiedRate;
    }
}

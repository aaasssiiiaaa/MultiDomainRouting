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
    public double time;

    //���캯��
    public SumOccupiedWavelength(double time){
        this.time=time;  //TODO ��û����ʱ������
    }

    //���������㱻ռ���ܲ�����
    public int sumNumOccupied() {
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

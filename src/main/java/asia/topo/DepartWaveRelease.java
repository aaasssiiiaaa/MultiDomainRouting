package asia.topo;

import java.util.List;

/**
 * Created by 1 on 2016/4/15.
 * ����ҵ����ȥ���ͷ�ҵ����ռ�ò�����Դ
 */
public class DepartWaveRelease {
    public int serviceID;              //������Դ��ҵ��ID
    public List<Link> edge;

    //���캯��������ʼ��ȫ�ֱ���
    public DepartWaveRelease(int eventID,List<Link> Links){
        serviceID=eventID;
    }

    //�ͷŲ�����Դ
    public void releaseWaveResource(){
        for (int i =0;i<edge.size();i++){
            for (int j =1;j<=80;j++){
                if (serviceID==edge.get(i).wavelengths.get(j).waveserviceID){
                    edge.get(i).wavelengths.get(j).isUsed=false;
                }
            }
        }
    }
}

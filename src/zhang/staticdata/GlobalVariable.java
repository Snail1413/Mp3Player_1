package zhang.staticdata;

import java.util.List;

import zhang.model.Mp3Info;
import android.app.Application;

public class GlobalVariable extends Application{
	
	public static List<Mp3Info> LocalMp3Infos=null;
	public static int currentLocation=0;
	//ָʾ��ǰ�Ĳ���״̬��tag=1��ʾ���ڲ��š���tag=0��ʾ��ͣ
	public static int tag=1;
	public static final String PLAY_COMPLETE_MES="zhang.mp3player.play_complicate.action";
	public static final String UPDATE_PLAYTIME_MES="zhang.mp3player.update_seekbar.action";
	public static final String USER_UPDATETIME_MSG="zhang.mp3player.service.chang_seekbar.action";
	public int currentPlayTime=0;
	
	//����ģʽ��ʾֵ
	public static int constant=0;
	
	
	
	public static int getCurrentLocation() {
		return currentLocation;
	}


	public static void setCurrentLocation(int currentLocation) {
		GlobalVariable.currentLocation = currentLocation;
	}


	public List<Mp3Info> getLocalMp3Infos()
	{
		return LocalMp3Infos;
	}
	
	
	public void setLocalMp3Infos(List<Mp3Info> mp3Infos)
	{
		LocalMp3Infos=mp3Infos;
	}

}

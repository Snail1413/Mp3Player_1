package zhang.mp3player;

import android.app.TabActivity;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.widget.TabHost;

public class MainActivity extends TabActivity{
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stu
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main_activity);
		//�õ�tabHost���󡣡����TabHost����Ĳ���ͨ���������������ɡ�
		TabHost tabHost=getTabHost();
		//����һ��Intent���󡣸ö�������һ��Activity��Ϊת��������Activity
		
		
		
		
			Intent remoteIntent=new Intent();
			remoteIntent.setClass(this, Mp3ListActivity.class);
			
			//����һ��TabSpe���󡣡��˶�������ҳ��ʾ��һҳ
			TabHost.TabSpec remoteSpec=tabHost.newTabSpec("Remote");
			//����ָʾ��������Ϊ��ǩ��ͼ��
			Resources res=getResources();
			remoteSpec.setIndicator("Remote", res.getDrawable(R.drawable.background));
			//res��getDrable�õ�����һ�����͵Ķ���
			//��������
			remoteSpec.setContent(remoteIntent);
			tabHost.addTab(remoteSpec);
		
		
			Intent localIntent=new Intent();
			localIntent.setClass(this, LocalMp3ListActivity.class);
			TabHost.TabSpec localSpec=tabHost.newTabSpec("Local");
		
			localSpec.setIndicator("Local", res.getDrawable(android.R.drawable.stat_sys_data_bluetooth));
			localSpec.setContent(localIntent);
			tabHost.addTab(localSpec);
		
	}

}

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
		//得到tabHost对象。、针对TabHost对象的操作通常都有这个对象完成。
		TabHost tabHost=getTabHost();
		//生成一个Intent对象。该对象生成一个Activity。为转换容器的Activity
		
		
		
		
			Intent remoteIntent=new Intent();
			remoteIntent.setClass(this, Mp3ListActivity.class);
			
			//生成一个TabSpe对象。。此对象代表分页显示的一页
			TabHost.TabSpec remoteSpec=tabHost.newTabSpec("Remote");
			//设置指示器，，，为标签和图标
			Resources res=getResources();
			remoteSpec.setIndicator("Remote", res.getDrawable(R.drawable.background));
			//res。getDrable得到的是一个整型的对象
			//设置内容
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

package zhang.mp3player.service;

import zhang.download.HttpDownloader;
import zhang.model.Mp3Info;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;




public class DownloadService extends Service{

	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}
	//进入srevice即开始执行此函数
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		// TODO Auto-generated method stub
		Mp3Info mp3Info=(Mp3Info)intent.getSerializableExtra("mp3Info");
		
		DownloadThreed downloadThreed=new DownloadThreed(mp3Info);
		Thread thread=new Thread(downloadThreed);
		thread.start();
		
		
		return super.onStartCommand(intent, flags, startId);
	}
	
	
	class DownloadThreed implements Runnable
	{
		
		private Mp3Info mp3Info=null;
		public DownloadThreed(Mp3Info mp3Info)
		{
			this.mp3Info=mp3Info;
		}

		@Override
		public void run() {
			//此处只下载了MP3歌曲而没有下载歌词
			// TODO Auto-generated method stub
			String mp3Url="http://snailmp3didi-main.stor.sinaapp.com/"+mp3Info.getMp3Name();
			HttpDownloader httpDownloader=new HttpDownloader();
			int result=httpDownloader.downFile(mp3Url, "mp3/",mp3Info.getMp3Name() );
			
			String mp3Url2="http://snailmp3didi-main.stor.sinaapp.com/"+mp3Info.getLrcName();			
			HttpDownloader httpDownloader2=new HttpDownloader();			
			int result2=httpDownloader2.downFile(mp3Url2, "mp3/",mp3Info.getLrcName() );
		}
	}
}

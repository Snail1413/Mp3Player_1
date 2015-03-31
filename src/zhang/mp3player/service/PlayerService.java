package zhang.mp3player.service;

import java.io.File;
import java.io.IOException;

import zhang.model.Mp3Info;
import zhang.mp3player.PlayerActivity;

import zhang.staticdata.GlobalVariable;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;

public class PlayerService extends Service{
	
	private boolean isPlaying=false;
	private boolean isPause=false;
	private static Mp3Info Refermp3Info=null;
	private GlobalVariable globalVariable=new GlobalVariable();
	private Handler handler=new Handler();
	
	private userChangSeekbarBroadCastReceiver receiver=null;
	
	private MediaPlayer mediaPlayer=null;
	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	
	
	
	
	
	
	//每次住activity向Service发送一个Intent的时候就会执行此方法
	public int onStartCommand(Intent intent, int flags, int startId) {
		// TODO Auto-generated method stub
		receiver =new userChangSeekbarBroadCastReceiver();
		registerReceiver(receiver, getIntentFilter(GlobalVariable.USER_UPDATETIME_MSG));
		
		
		Mp3Info mp3Info=(Mp3Info) intent.getSerializableExtra("mp3Info");
		if(mp3Info!=null)
		{
			System.out.println("运行到了这里");
			play(mp3Info);
		
		}
		Refermp3Info=mp3Info;
		
		
		mediaPlayer.setOnCompletionListener(new OnCompletionListener() {
			
			@Override
			public void onCompletion(MediaPlayer mp) {
				// TODO Auto-generated method stub
				System.out.println("音乐播放结束亲爱的-----");
				handler.removeCallbacks(r);
				globalVariable.currentPlayTime=0;
				Intent intent=new Intent();
				intent.setAction(GlobalVariable.PLAY_COMPLETE_MES);
				sendBroadcast(intent);  
			}
		});
		handler.post(r);
		
		
		return super.onStartCommand(intent, flags, startId);
	}
	
	
	
	
	private void play(Mp3Info mp3Info) {
		// TODO Auto-generated method stub
		//if(mp3Info!=Refermp3Info)
		
		    // tag=1;
		System.out.println("ReferMp3Info----->"+Refermp3Info);
		System.out.println("........................"+mp3Info);
		System.out.println(mp3Info.equals(Refermp3Info));
		if(mediaPlayer!=null&&globalVariable.tag==-1&&mp3Info.equals(Refermp3Info))
		{
			mediaPlayer.pause();
			
		}
		else if(mp3Info.equals(Refermp3Info)==false)
		{	
			globalVariable.tag=1;
			if(mediaPlayer!=null)
				mediaPlayer.release();
			String path=getMp3Path(mp3Info);
			mediaPlayer=MediaPlayer.create(this,Uri.parse("file://"+path));
			int mMax=mediaPlayer.getDuration();
			System.out.println("播放歌曲的总时间为"+mMax);
			System.out.println(mediaPlayer);
			mediaPlayer.setLooping(false);
			mediaPlayer.start();
			isPlaying = true;
		}
		else if(globalVariable.tag==1&&mediaPlayer!=null)
		{
			mediaPlayer.start();
		}
	}
	
	
	
	private String getMp3Path(Mp3Info mp3Info)
	{
		String SDCardRoot=Environment.getExternalStorageDirectory().getAbsolutePath();
		String path=SDCardRoot+File.separator+"mp3"+File.separator+mp3Info.getMp3Name();
		
		return path;
	}
	
	
	public void onDestroy() {
		// TODO Auto-generated method stubo
		mediaPlayer.release();
		handler.removeCallbacks(r);
		unregisterReceiver(receiver);
		globalVariable.currentPlayTime=0;
		System.out.println("Service onDestory");
		Refermp3Info=null;
		super.onDestroy();
	}
	
	
	
Runnable r=new Runnable() {
        
        @Override
        public void run() {
            // TODO Auto-generated method stub
            globalVariable.currentPlayTime=mediaPlayer.getCurrentPosition();
            //System.out.println("当前播放时长是这么多"+globalVariable.currentPlayTime);
            Intent intent =new Intent();
            intent.setAction(GlobalVariable.UPDATE_PLAYTIME_MES);
            intent.putExtra("currentPlayTime", globalVariable.currentPlayTime);
            sendBroadcast(intent);
            handler.postDelayed(r, 200);
        }
    };
	
    
    
    
    
    
    private IntentFilter getIntentFilter(String msg)
	{
			IntentFilter intentFilter;
			intentFilter=new IntentFilter();
			intentFilter.addAction(msg);
			System.out.println("新注册了一个监听器----》");
		
		return intentFilter;
	}
	
	class userChangSeekbarBroadCastReceiver extends BroadcastReceiver
	{

		@Override
		public void onReceive(Context arg0, Intent intent) {
			// TODO Auto-generated method stub
			//String completeMessage=intent.getStringExtra("Mp3_complete");
			int progress=intent.getIntExtra("User_changeProgress", 0);
			System.out.println("我接收到来自用户的改变播放进度的请求/////////////"+progress);
			try
			{
				mediaPlayer.seekTo(progress);}
			catch(Exception e)
			{
				System.out.println(progress);
				System.out.println("seekTo失败");
			}
			System.out.println("---"+mediaPlayer.getDuration());
			System.out.println("当前播放时间。。。"+mediaPlayer.getCurrentPosition());
			globalVariable.currentPlayTime=mediaPlayer.getCurrentPosition();
			
		}
		
	}
  

}

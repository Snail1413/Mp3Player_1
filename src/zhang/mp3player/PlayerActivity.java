package zhang.mp3player;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.RandomAccess;

import zhang.lrc.LrcProcessor;
import zhang.model.Mp3Info;
import zhang.mp3player.service.PlayerService;
import zhang.staticdata.GlobalVariable;
import android.app.Activity;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.CharArrayBuffer;
import android.database.ContentObserver;
import android.database.Cursor;
import android.database.DataSetObserver;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.DeadObjectException;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.text.style.UpdateAppearance;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemSelectedListener;

public class PlayerActivity extends Activity{
	
	public static final int REFRESH = 0;
	
	public String path= "/mnt/sdcard/mp3/";
	
	private int clickTimes=0;
	private int clickNext=0;

	private Mp3_completeBroadCastReceiver receiver=null;
	private Mp3_updateSeekBarBroadCastReceiver receiver2=null;
	
	ImageButton beginAndpauseButton=null;
	ImageButton nextSongButton=null;
	ImageButton lastSongButton=null;
	Spinner spinner = null;
	
	TextView lrcTextView=null;
	TextView artistIgTextView=null;
	TextView sourceTextView=null;
	TextView songNameTextView=null;
	
	SeekBar seekBar1=null;
	
	MediaPlayer mediaPlayer=null;
	private Mp3Info mp3Info=null;
	Mp3Info lastMp3Info=null;
	Mp3Info nextMp3Info=null;
	Mp3Info ReferInfo=null;
	
	private List<Mp3Info> mp3Infos=null;
	
	private static boolean IS_PLAYING=false;
	
	GlobalVariable globalVariable=null;
	
	
	private ArrayList<Queue> queues=null;
	
	private Handler handler=new Handler();
	private UpdateTimeCallback updateTimeCallback=null;
	private long begin=0;
	private long nextTimeMill=0;
	private long currentTimeMill=0;
	private String message=null;
	private long pauseTimeMills=0;
	private boolean isPlaying=false;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		
		
		receiver =new Mp3_completeBroadCastReceiver();
		receiver2 =new Mp3_updateSeekBarBroadCastReceiver();
		registerReceiver(receiver, getIntentFilter(GlobalVariable.PLAY_COMPLETE_MES));
		registerReceiver(receiver2, getIntentFilter(GlobalVariable.UPDATE_PLAYTIME_MES));
		
		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.mp3_player);
		Intent intent=getIntent();
		mp3Info=(Mp3Info)intent.getSerializableExtra("mp3Info");
		beginAndpauseButton=(ImageButton)findViewById(R.id.play);
		nextSongButton=(ImageButton)findViewById(R.id.next);
		lastSongButton=(ImageButton)findViewById(R.id.previous);
		spinner = (Spinner) findViewById(R.id.spinnerId);
		seekBar1=(SeekBar) findViewById(R.id.seekBar1);
		//通过createFromResource方法创建一个ArrayAdapter对象
		//第一个参数是指上下文对象
		//第二参数引用了在strings.xml文件当中定义的String数组
		//第三个参数是用来指定Spinner的样式，是一个布局文件ID，该布局文件由Android系统提供，也可替换为自己定义的布局文件
		ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
				this, R.array.PlayState,
				android.R.layout.simple_spinner_item);
		//设置Spinner当中每个条目的样式，同样是引用一个Android系统提供的布局文件
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinner.setAdapter(adapter);
		spinner.setPrompt("播放模式");
		//为spinner对象绑定监听器
		spinner.setOnItemSelectedListener(new SpinnerOnSelectedListener());
		
		
		lrcTextView=(TextView)findViewById(R.id.songText);
		artistIgTextView=(TextView)findViewById(R.id.artistText);
		sourceTextView=(TextView)findViewById(R.id.sourceText);
		songNameTextView=(TextView)findViewById(R.id.namText);
		
		
		beginAndpauseButton.setOnClickListener(new beginAndpasueButtonListener());
		nextSongButton.setOnClickListener(new nextSongButtonListener());
		lastSongButton.setOnClickListener(new lastSongButtonListener());
		seekBar1.setOnSeekBarChangeListener(new SeekBar1Listener());
		lrcTextView.setTextSize(15);
		lrcTextView.setText("我们都是泡沫"+"\n"+"轻轻一碰就破"+"\n"+"眼泪是爱的花火");
		artistIgTextView.setText("你猜啊");
		sourceTextView.setText("这都不知道");
		songNameTextView.setText("我不告诉你");
		globalVariable.tag=1;
		play(mp3Info);
		}
	
	//播放模式选择下拉菜单，，，，监听器
	
class SpinnerOnSelectedListener implements OnItemSelectedListener{
		
		//当用户选定了一个条目时，就会调用该方法
		@Override
		public void onItemSelected(AdapterView<?> adapterView, View view, int position,
				long id) {
			String selected = adapterView.getItemAtPosition(position).toString();
			if(position==0)
			{
				globalVariable.constant=0;
			}
			else if(position==1)
			{
				globalVariable.constant=1;
			}
			else if(position==2)
			{
				globalVariable.constant=2;
			}
			System.out.println("consrant------->"+globalVariable.constant);
		}

		@Override
		public void onNothingSelected(AdapterView<?> adapterView) {
			// TODO Auto-generated method stub
			System.out.println("nothingSelected");
		}
		
	}



private class SeekBar1Listener implements SeekBar.OnSeekBarChangeListener{
	//当进度条的进度发生变化时，会调用该方法
	@Override
	public void onProgressChanged(SeekBar seekBar, int progress,
			boolean fromUser) {
		if(fromUser==true)
		{
			Intent intent=new Intent();
			intent.setAction(GlobalVariable.USER_UPDATETIME_MSG);
			intent.putExtra("User_changeProgress", progress);
			System.out.println("seekbar长度"+seekBar.getMax());
			sendBroadcast(intent);
		}
		
		
	}
	//当用户开始滑动滑块时，调用该方法
	@Override
	public void onStartTrackingTouch(SeekBar seekBar) {
		System.out.println("start--->" + seekBar.getProgress());
	}
	//当用户结束对滑块的滑动时，调用该方法
	@Override
	public void onStopTrackingTouch(SeekBar seekBar) {
		System.out.println("stop--->" + seekBar.getProgress());
	}
	
}



	
	class beginAndpasueButtonListener implements OnClickListener
	{
		@Override
		public void onClick(View v) {
			 IS_PLAYING=true;
			// TODO Auto-generated method stub
			 clickTimes++;
			globalVariable.tag = -globalVariable.tag;
			System.out.println("tag2222----->"+globalVariable.tag);
					play(mp3Info);
				
				//System.out.println(globalVariable.tag);
				
				isPlaying=true;
		}
	}
	
	
	class nextSongButtonListener implements OnClickListener
	{
		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			playNextSong();
			
			}
			
		
		
	}
		
	
	/*
	 * 
	 * 上一曲按钮操作
	 * 
	*/
		class lastSongButtonListener implements OnClickListener
		{

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
					System.out.println("我点击了上一曲。。。。。");
					clickNext=1;
					clickTimes=0;
					if(globalVariable.currentLocation>0)
					{
						int i=globalVariable.currentLocation-1;
						System.out.println(globalVariable.currentLocation+"两个值比较"+globalVariable.LocalMp3Infos.size());
						globalVariable.currentLocation--;
						//nextMp3Info=lastMp3Info;
						lastMp3Info=globalVariable.LocalMp3Infos.get(i);
					}
					else
					{
						lastMp3Info=null;
					}
					
					if(lastMp3Info!=null)
					{
						globalVariable.tag=1;
						Intent intent= new Intent();
						intent.setClass(PlayerActivity.this,PlayerService.class);
						stopService(intent);
						mp3Info=lastMp3Info;
						play(lastMp3Info);
						
						isPlaying=true;
					}
					else
						Toast.makeText(PlayerActivity.this,"已经是最前一曲",Toast.LENGTH_SHORT).show();
				}
	}
	

	//根据歌词文件的名字读取歌词文件的信息
	private boolean prepareLrc(String lrcName)
	{
		try
		{
			
			InputStream inputStream=new FileInputStream(Environment.getExternalStorageDirectory().getAbsoluteFile()+File.separator+"mp3"+File.separator+lrcName);
			LrcProcessor lrcProcessor=new LrcProcessor();
			queues=lrcProcessor.process(inputStream);
			begin =0;
			currentTimeMill=0;
			nextTimeMill=0;
			updateTimeCallback=new UpdateTimeCallback(queues);
			return true;
		}
		catch(FileNotFoundException e)
		{
			e.printStackTrace();
			lrcTextView.setText("对不起，歌词木有找到，你来打我啊！！！"+"\n"+"来啊");
			return false;
		}
	}
	
	
	/*
	 * 歌词跟新函数
	 */
	class  UpdateTimeCallback implements Runnable
	{
		ArrayList<Queue> queues=null;
		private Object lockObject=new Object();
		
		
		public UpdateTimeCallback(ArrayList<Queue> queues)
		{
			this.queues=queues;
		}
		@Override
		public void run() {
			// TODO Auto-generated method stub
			//计算偏移量。从开始播放MP3到现在位置消耗时间，，以毫秒为单位
			Queue times=queues.get(0);
			Queue messages=queues.get(1);
			long offset=System.currentTimeMillis()-begin;//begin记录开始播放的时间
			if(currentTimeMill==0)
			{
				nextTimeMill=(Long)times.poll();
				message=(String)messages.poll();
				lrcTextView.setText("");
				String [] arr=message.split("\\.");
			}
			if(globalVariable.tag==1)
			{
				if(offset>=nextTimeMill)
				{
					lrcTextView.setText(message);
					message=(String)messages.poll();
					try
					{
						nextTimeMill=(Long)times.poll();}
					catch(Exception e)
					{
						handler.removeCallbacks(updateTimeCallback);
						lrcTextView.setText("歌词结束了亲爱的");}
				}
				currentTimeMill=currentTimeMill+10;
				handler.postDelayed(updateTimeCallback, 10);//
			}
			else 
			{
				currentTimeMill=currentTimeMill;
				//handler.postDelayed(updateTimeCallback, 10);
			}
			
		}
		
	}
	
	
	//在Service中播放歌曲
	public void play(Mp3Info mp3Info) 
	{
			/*
			 * 按下下一曲或者是上一曲时，设置标志位，，移除updatetimecallback.并把暂停时间置零
			 */
			if(clickNext==1)
			{
				handler.removeCallbacks(updateTimeCallback);
				pauseTimeMills=0;
			}
			Intent intent =new Intent();
			intent.setClass(PlayerActivity.this, PlayerService.class);
			intent.putExtra("mp3Info", mp3Info);
			/*
			 * 获取MP3文件时长
			 */
			getInfomationOfMp3();
			
			String str=mp3Info.getMp3Name();
			/*
			 * 获取lrc文件名
			 */
			String [] arr=str.split("\\.");
			String lrcName = arr[0]+".lrc";
			
			startService(intent);
			/*
			 * 延时delayMillis毫秒 将Runnable插入消息列队，
				Runnable将在handle绑定的线程中运行。
				post 是立即插入消息列队，当消息列队处理到该消息时才运行
			 */
			//刚刚播放时，暂停时间为零
			if(pauseTimeMills==0)
			{
				pauseTimeMills=System.currentTimeMillis();
			}
			//点击一次，click+1.。当click=0时表示第一次，，则要创准备lrc
			if(clickTimes==0)
			{
				prepareLrc(lrcName);
				begin=System.currentTimeMillis();
			}
			
			if(globalVariable.tag==1)
			{
				
				begin=System.currentTimeMillis()-pauseTimeMills+begin;
				handler.postDelayed(updateTimeCallback, 5);
				
			}
			else if(globalVariable.tag==-1)
			{
				handler.removeCallbacks(updateTimeCallback);
				pauseTimeMills=System.currentTimeMillis();
			}
			ReferInfo=mp3Info;
			clickNext=0;
	}
	
	
	
	public Cursor getCursorfromPath(String filePath) {  
	    String path = null;  
	    Cursor c = getApplicationContext().getContentResolver().query(  
	            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, null, null, null,  
	            MediaStore.Audio.Media.DEFAULT_SORT_ORDER);  
	    // System.out.println(c.getString(c.getColumnIndex("_data")));  
	    if (c.moveToFirst()) {  
	        do {  
	            // 通过Cursor 获取路径，如果路径相同则break；  
	            path = c.getString(c  
	                    .getColumnIndexOrThrow(MediaStore.Audio.Media.DATA));  
	            // 查找到相同的路径则返回，此时cursorPosition 便是指向路径所指向的Cursor 便可以返回了  
	            if (path.equals(filePath)) {  
	                // System.out.println("audioPath = " + path);  
	                // System.out.println("filePath = " + filePath);  
	                // cursorPosition = c.getPosition();  
	                break;  
	            }  
	        } while (c.moveToNext());  
	    }  
	    // 这两个没有什么作用，调试的时候用  
	    // String audioPath = c.getString(c  
	    // .getColumnIndexOrThrow(MediaStore.Audio.Media.DATA));  
	    //  
	    // System.out.println("audioPath = " + audioPath);  
	    return c;  
	}               
	
	
	
	private IntentFilter getIntentFilter(String msg)
	{
			IntentFilter intentFilter;
			intentFilter=new IntentFilter();
			intentFilter.addAction(msg);
			System.out.println("新注册了一个监听器----》");
		
		return intentFilter;
	}
	
	class Mp3_completeBroadCastReceiver extends BroadcastReceiver
	{

		@Override
		public void onReceive(Context arg0, Intent intent) {
			// TODO Auto-generated method stub
			//String completeMessage=intent.getStringExtra("Mp3_complete");
			playNextSong();
			System.out.println("音乐播送完了。。。。主MP3Activity已经接收到了");
		}
		
	}
	
	
	class   Mp3_updateSeekBarBroadCastReceiver extends BroadcastReceiver
	{

		@Override
		public void onReceive(Context arg0, Intent intent) {
			// TODO Auto-generated method stub
			int currentTime=intent.getIntExtra("currentPlayTime", 0);
			seekBar1.setProgress(currentTime);
			//System.out.println("。。主程序接收时间。。"+currentTime);
		}
		
	}
	
	
	void playNextSong()
	{
		clickTimes=0;
		clickNext=1;
		System.out.println("我点击了下一曲。。。。。");
		System.out.println("ISPLAYING--->"+IS_PLAYING);
		/*
		 * 根据constant的值来读取播放模式，并获得下一曲
		 */
			if(globalVariable.constant==0)
			{
				if(globalVariable.currentLocation<(globalVariable.LocalMp3Infos.size())-1)
				{
					System.out.println("点击下一曲后全局变量 "+globalVariable.currentLocation+globalVariable.LocalMp3Infos);
					//mp3Infos=globalVariable.getLocalMp3Infos();
					//System.out.println(mp3Infos);
					int i=globalVariable.currentLocation+1;
					System.out.println(globalVariable.currentLocation+"两个值比较"+globalVariable.LocalMp3Infos.size());
					globalVariable.currentLocation++;
					//lastMp3Info=nextMp3Info;
					nextMp3Info=globalVariable.LocalMp3Infos.get(i);
					System.out.println("当前位置为"+globalVariable.currentLocation+"----"+"当前next为"+nextMp3Info);
				}
				else
				{
					nextMp3Info=null;
				}
			}
			else if(globalVariable.constant==1)
			{
				int random=(int)(Math.random()*globalVariable.LocalMp3Infos.size());
				System.out.println("size----->"+globalVariable.LocalMp3Infos.size());
				System.out.println("random------>"+random);
				nextMp3Info=globalVariable.LocalMp3Infos.get(random);
			}
			else if(globalVariable.constant==2)
				if(nextMp3Info==null)
				{
					//nextMp3Info=globalVariable.LocalMp3Infos.get(globalVariable.LocalMp3Infos.size()-1);
					nextMp3Info=mp3Info;
				}
			
			
			if(nextMp3Info!=null)
			{
				globalVariable.tag=1;
				Intent intent= new Intent();
				intent.setClass(PlayerActivity.this,PlayerService.class);
				stopService(intent);
				mp3Info=nextMp3Info;
				play(nextMp3Info);
				
				isPlaying=true;
			}
			else
				Toast.makeText(PlayerActivity.this,"已经是最后一曲",Toast.LENGTH_SHORT).show();
	}
	
	
	void getInfomationOfMp3()
	{
		Cursor currentCusor=null;
		try
			{currentCusor=getCursorfromPath(path+mp3Info.getMp3Name());}
		catch(Exception e)
		{
			System.out.println("MP3信息获取异常");
			songNameTextView.setText("未知歌曲");
			sourceTextView.setText("未知专辑");
			artistIgTextView.setText("未知艺术家");
			return;
		}
		
		String tilte = currentCusor.getString(currentCusor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE));  
		songNameTextView.setText(tilte);
		int duration = currentCusor.getInt(currentCusor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION));  
		System.out.println("吊炸天的歌曲时长------>"+duration);
		seekBar1.setMax(duration);
		String album = currentCusor.getString(currentCusor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM));  
		sourceTextView.setText(album);
		String artist = currentCusor.getString(currentCusor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST));  
		artistIgTextView.setText(artist);
	}
	
	
}



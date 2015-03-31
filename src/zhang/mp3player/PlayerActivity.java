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
		//ͨ��createFromResource��������һ��ArrayAdapter����
		//��һ��������ָ�����Ķ���
		//�ڶ�������������strings.xml�ļ����ж����String����
		//����������������ָ��Spinner����ʽ����һ�������ļ�ID���ò����ļ���Androidϵͳ�ṩ��Ҳ���滻Ϊ�Լ�����Ĳ����ļ�
		ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
				this, R.array.PlayState,
				android.R.layout.simple_spinner_item);
		//����Spinner����ÿ����Ŀ����ʽ��ͬ��������һ��Androidϵͳ�ṩ�Ĳ����ļ�
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinner.setAdapter(adapter);
		spinner.setPrompt("����ģʽ");
		//Ϊspinner����󶨼�����
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
		lrcTextView.setText("���Ƕ�����ĭ"+"\n"+"����һ������"+"\n"+"�����ǰ��Ļ���");
		artistIgTextView.setText("��°�");
		sourceTextView.setText("�ⶼ��֪��");
		songNameTextView.setText("�Ҳ�������");
		globalVariable.tag=1;
		play(mp3Info);
		}
	
	//����ģʽѡ�������˵���������������
	
class SpinnerOnSelectedListener implements OnItemSelectedListener{
		
		//���û�ѡ����һ����Ŀʱ���ͻ���ø÷���
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
	//���������Ľ��ȷ����仯ʱ������ø÷���
	@Override
	public void onProgressChanged(SeekBar seekBar, int progress,
			boolean fromUser) {
		if(fromUser==true)
		{
			Intent intent=new Intent();
			intent.setAction(GlobalVariable.USER_UPDATETIME_MSG);
			intent.putExtra("User_changeProgress", progress);
			System.out.println("seekbar����"+seekBar.getMax());
			sendBroadcast(intent);
		}
		
		
	}
	//���û���ʼ��������ʱ�����ø÷���
	@Override
	public void onStartTrackingTouch(SeekBar seekBar) {
		System.out.println("start--->" + seekBar.getProgress());
	}
	//���û������Ի���Ļ���ʱ�����ø÷���
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
	 * ��һ����ť����
	 * 
	*/
		class lastSongButtonListener implements OnClickListener
		{

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
					System.out.println("�ҵ������һ������������");
					clickNext=1;
					clickTimes=0;
					if(globalVariable.currentLocation>0)
					{
						int i=globalVariable.currentLocation-1;
						System.out.println(globalVariable.currentLocation+"����ֵ�Ƚ�"+globalVariable.LocalMp3Infos.size());
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
						Toast.makeText(PlayerActivity.this,"�Ѿ�����ǰһ��",Toast.LENGTH_SHORT).show();
				}
	}
	

	//���ݸ���ļ������ֶ�ȡ����ļ�����Ϣ
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
			lrcTextView.setText("�Բ��𣬸��ľ���ҵ����������Ұ�������"+"\n"+"����");
			return false;
		}
	}
	
	
	/*
	 * ��ʸ��º���
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
			//����ƫ�������ӿ�ʼ����MP3������λ������ʱ�䣬���Ժ���Ϊ��λ
			Queue times=queues.get(0);
			Queue messages=queues.get(1);
			long offset=System.currentTimeMillis()-begin;//begin��¼��ʼ���ŵ�ʱ��
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
						lrcTextView.setText("��ʽ������װ���");}
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
	
	
	//��Service�в��Ÿ���
	public void play(Mp3Info mp3Info) 
	{
			/*
			 * ������һ����������һ��ʱ�����ñ�־λ�����Ƴ�updatetimecallback.������ͣʱ������
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
			 * ��ȡMP3�ļ�ʱ��
			 */
			getInfomationOfMp3();
			
			String str=mp3Info.getMp3Name();
			/*
			 * ��ȡlrc�ļ���
			 */
			String [] arr=str.split("\\.");
			String lrcName = arr[0]+".lrc";
			
			startService(intent);
			/*
			 * ��ʱdelayMillis���� ��Runnable������Ϣ�жӣ�
				Runnable����handle�󶨵��߳������С�
				post ������������Ϣ�жӣ�����Ϣ�жӴ�������Ϣʱ������
			 */
			//�ող���ʱ����ͣʱ��Ϊ��
			if(pauseTimeMills==0)
			{
				pauseTimeMills=System.currentTimeMillis();
			}
			//���һ�Σ�click+1.����click=0ʱ��ʾ��һ�Σ�����Ҫ��׼��lrc
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
	            // ͨ��Cursor ��ȡ·�������·����ͬ��break��  
	            path = c.getString(c  
	                    .getColumnIndexOrThrow(MediaStore.Audio.Media.DATA));  
	            // ���ҵ���ͬ��·���򷵻أ���ʱcursorPosition ����ָ��·����ָ���Cursor ����Է�����  
	            if (path.equals(filePath)) {  
	                // System.out.println("audioPath = " + path);  
	                // System.out.println("filePath = " + filePath);  
	                // cursorPosition = c.getPosition();  
	                break;  
	            }  
	        } while (c.moveToNext());  
	    }  
	    // ������û��ʲô���ã����Ե�ʱ����  
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
			System.out.println("��ע����һ��������----��");
		
		return intentFilter;
	}
	
	class Mp3_completeBroadCastReceiver extends BroadcastReceiver
	{

		@Override
		public void onReceive(Context arg0, Intent intent) {
			// TODO Auto-generated method stub
			//String completeMessage=intent.getStringExtra("Mp3_complete");
			playNextSong();
			System.out.println("���ֲ������ˡ���������MP3Activity�Ѿ����յ���");
		}
		
	}
	
	
	class   Mp3_updateSeekBarBroadCastReceiver extends BroadcastReceiver
	{

		@Override
		public void onReceive(Context arg0, Intent intent) {
			// TODO Auto-generated method stub
			int currentTime=intent.getIntExtra("currentPlayTime", 0);
			seekBar1.setProgress(currentTime);
			//System.out.println("�������������ʱ�䡣��"+currentTime);
		}
		
	}
	
	
	void playNextSong()
	{
		clickTimes=0;
		clickNext=1;
		System.out.println("�ҵ������һ������������");
		System.out.println("ISPLAYING--->"+IS_PLAYING);
		/*
		 * ����constant��ֵ����ȡ����ģʽ���������һ��
		 */
			if(globalVariable.constant==0)
			{
				if(globalVariable.currentLocation<(globalVariable.LocalMp3Infos.size())-1)
				{
					System.out.println("�����һ����ȫ�ֱ��� "+globalVariable.currentLocation+globalVariable.LocalMp3Infos);
					//mp3Infos=globalVariable.getLocalMp3Infos();
					//System.out.println(mp3Infos);
					int i=globalVariable.currentLocation+1;
					System.out.println(globalVariable.currentLocation+"����ֵ�Ƚ�"+globalVariable.LocalMp3Infos.size());
					globalVariable.currentLocation++;
					//lastMp3Info=nextMp3Info;
					nextMp3Info=globalVariable.LocalMp3Infos.get(i);
					System.out.println("��ǰλ��Ϊ"+globalVariable.currentLocation+"----"+"��ǰnextΪ"+nextMp3Info);
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
				Toast.makeText(PlayerActivity.this,"�Ѿ������һ��",Toast.LENGTH_SHORT).show();
	}
	
	
	void getInfomationOfMp3()
	{
		Cursor currentCusor=null;
		try
			{currentCusor=getCursorfromPath(path+mp3Info.getMp3Name());}
		catch(Exception e)
		{
			System.out.println("MP3��Ϣ��ȡ�쳣");
			songNameTextView.setText("δ֪����");
			sourceTextView.setText("δ֪ר��");
			artistIgTextView.setText("δ֪������");
			return;
		}
		
		String tilte = currentCusor.getString(currentCusor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE));  
		songNameTextView.setText(tilte);
		int duration = currentCusor.getInt(currentCusor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION));  
		System.out.println("��ը��ĸ���ʱ��------>"+duration);
		seekBar1.setMax(duration);
		String album = currentCusor.getString(currentCusor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM));  
		sourceTextView.setText(album);
		String artist = currentCusor.getString(currentCusor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST));  
		artistIgTextView.setText(artist);
	}
	
	
}



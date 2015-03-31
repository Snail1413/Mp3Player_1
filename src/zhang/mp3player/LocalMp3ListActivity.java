package zhang.mp3player;

import java.util.ArrayList;
import zhang.staticdata.*;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import zhang.model.Mp3Info;
import zhang.utils.FileUtils;
import android.app.Application;
import android.app.ListActivity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ListView;
import android.widget.SimpleAdapter;

public class LocalMp3ListActivity extends ListActivity{
	private List<Mp3Info> mp3Infos=null;
	public String path= "/mnt/sdcard/mp3/Christina-Fighter.mp3";
	//����һ��ȫ�ֱ�����ָʾ��ʱMP3Infos��ֵ�Լ���ʱListView���α��ֵ
	GlobalVariable globalVariable=new GlobalVariable();
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		
		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.local_mp3_list);
		
		
	}
	
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		FileUtils fileUtils=new FileUtils();
		System.out.println("ȫ�ֱ�����ֵ������"+globalVariable.getLocalMp3Infos());
		//��ȫ�ֱ�����ֵ
		try {
			globalVariable.LocalMp3Infos=fileUtils.getMp3Files("mp3/");
			mp3Infos=fileUtils.getMp3Files("mp3/");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("ȫ�ֱ����仯�󡣡���"+globalVariable.getLocalMp3Infos());
		List<HashMap<String,String>> list=new ArrayList<HashMap<String, String>>();
		for (Iterator iterator =mp3Infos.iterator(); iterator.hasNext();) {
			Mp3Info mp3Info=(Mp3Info) iterator.next();
			HashMap<String, String> map=new HashMap<String, String>();
			map.put("mp3_name",mp3Info.getMp3Name());
			map.put("mp3_size", mp3Info.getMp3Size());
			list.add(map);
		}
		
		
		SimpleAdapter simpleAdapter = new SimpleAdapter(this, list,
				R.layout.mp3infos, new String[] { "mp3_name", "mp3_size" },
				new int[] { R.id.mp3_name, R.id.mp3_size });
		setListAdapter(simpleAdapter);
		super.onResume();
	}

	
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		// TODO Auto-generated method stub
		
		if(mp3Infos!=null)
		{
			//Cursor currentCusor=getCursorfromPath(path);
			//System.out.println("����ֵ���ԡ������------>"+currentCusor);
			//int album_id = currentCusor.getInt(currentCusor  
	                //.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID))  ;
			//int duration = currentCusor.getInt(currentCusor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION));  
			//System.out.println("��˵�е�������id----->"+duration);
			//String ceshi=getAlbumArt(album_id);
			//System.out.println("��˵�е�����������----->"+ceshi);
			
			
			
			
			globalVariable.currentLocation=position;
			Mp3Info mp3Info=mp3Infos.get(position);
			System.out.println("�����б�mp3Info------->"+mp3Info);
			Intent intent=new Intent();
			intent.putExtra("mp3Info", mp3Info);
			intent.setClass(this, PlayerActivity.class);
			startActivity(intent);
		}
		
		
		
		super.onListItemClick(l, v, position, id);
	}
	

	
	/*public Cursor getCursorfromPath(String filePath) {  
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
	}  */
	
	
	private String getAlbumArt(int album_id) {  
	    String mUriAlbums = "content://media/external/audio/albums";  
	    String[] projection = new String[] { "album_art" };  
	    Cursor cur = this.getContentResolver().query(  
	            Uri.parse(mUriAlbums + "/" + Integer.toString(album_id)),  
	            projection, null, null, null);  
	    String album_art = null;  
	    if (cur.getCount() > 0 && cur.getColumnCount() > 0) {  
	        cur.moveToNext();  
	        album_art = cur.getString(0);  
	    }  
	    cur.close();  
	    cur = null;  
	    return album_art;  
	}  
	
	
	
	
}

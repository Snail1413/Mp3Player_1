package zhang.mp3player;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import javax.xml.parsers.SAXParserFactory;

import zhang.mp3player.R;
import zhang.mp3player.service.DownloadService;

import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

import zhang.download.HttpDownloader;
import zhang.model.Mp3Info;
import zhang.xml.Mp3ListContentHandler;
import android.os.Bundle;
import android.app.ListActivity;
import android.content.Intent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.SimpleAdapter;

public class Mp3ListActivity extends ListActivity {
	private static final int UPDATE=1;
	private static final int ABOUT=2;
	private List<Mp3Info> mp3Infos=null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.mp3_list);
	}
	//���������˵���ѡ��
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// TODO Auto-generated method stub
		menu.add(0,1,1,R.string.mp3list_update);
		menu.add(0,2,2,R.string.mp3list_about);
		return super.onCreateOptionsMenu(menu);
	}
	//ѡ�в˵�ִ�в���
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// TODO Auto-generated method stub
		System.out.println("id...."+item.getItemId());
		if(item.getItemId()==UPDATE)
		{
			updateListView();
		}
		else if(item.getItemId()==ABOUT)
		{
			//�û�����˹��ڰ�ť
		}
		return super.onOptionsItemSelected(item);
	}

	//����������
	private SimpleAdapter buildSimpleAdapter(List<Mp3Info> mp3Infos)
	{
		List<HashMap<String, String>> list=new ArrayList<HashMap<String,String>>();
		for (Iterator iterator = mp3Infos.iterator(); iterator.hasNext();) 
		{
			Mp3Info mp3Info=(Mp3Info) iterator.next();
			HashMap<String,String> map=new HashMap<String, String>();
			map.put("mp3_name",mp3Info.getMp3Name());
			map.put("mp3_size", mp3Info.getMp3Size());
			list.add(map);
		}
		SimpleAdapter simpleAdapter = new SimpleAdapter(this, list,
				R.layout.mp3infos, new String[] { "mp3_name", "mp3_size" },
				new int[] { R.id.mp3_name, R.id.mp3_size });
		return  simpleAdapter;
	}
	
	//��ʾ���º�ĸ����б�
	private void updateListView()
	{
		//������ַ�����ظ�����Ϣ
		String xml = downloadXML("http://snailmp3didi-main.stor.sinaapp.com/resources.xml");
		System.out.println("xml....."+ xml);
		//�������صõ���xml�ļ�
		mp3Infos=parse(xml);
		//���������xml�ļ�����mp3Infos������
		SimpleAdapter simpleAdapter=buildSimpleAdapter(mp3Infos);
		setListAdapter(simpleAdapter);
		
		
	}
	
	
	private String downloadXML(String urlStr)
	{
		//��ʼ��һ��HttpDownloader����
		HttpDownloader httpdDownloader=new HttpDownloader();
		String result = httpdDownloader.download(urlStr);
		return result;
	}
	//����xml�ļ�
	private List<Mp3Info> parse(String xmlStr) {
		SAXParserFactory saxParserFactory = SAXParserFactory.newInstance();
		List<Mp3Info> infos = new ArrayList<Mp3Info>();
		try {
			XMLReader xmlReader = saxParserFactory.newSAXParser()
					.getXMLReader();
			Mp3ListContentHandler mp3ListContentHandler = new Mp3ListContentHandler(
					infos);
			xmlReader.setContentHandler(mp3ListContentHandler);
			xmlReader.parse(new InputSource(new StringReader(xmlStr)));
			for (Iterator iterator = infos.iterator(); iterator.hasNext();) {
				Mp3Info mp3Info = (Mp3Info) iterator.next();
				System.out.println(mp3Info);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return infos;
	}
	
	
	//ѡ��ĳһ����Ŀ֮��ִ�в�������ѡ��֮�󼴿�ʼ����ѡ�еĸ���
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		// TODO Auto-generated method stub
		Mp3Info mp3Info=mp3Infos.get(position);
		Intent intent=new Intent();
		intent.putExtra("mp3Info", mp3Info);
		intent.setClass(this, DownloadService.class);
		startService(intent);
		
		System.out.println("mp3info...."+mp3Info);
		super.onListItemClick(l, v, position, id);
	}
	
	
	
}

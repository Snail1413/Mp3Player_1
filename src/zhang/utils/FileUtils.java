package zhang.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.audio.exceptions.InvalidAudioFrameException;
import org.jaudiotagger.audio.exceptions.ReadOnlyFileException;
import org.jaudiotagger.audio.mp3.MP3AudioHeader;
import org.jaudiotagger.audio.mp3.MP3File;
import org.jaudiotagger.tag.TagException;

import zhang.model.Mp3Info;
import zhang.mp3player.LocalMp3ListActivity;
import zhang.mp3player.PlayerActivity;

import android.content.Context;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;

public class FileUtils {
	private String SDCardRoot;
	
	


	public String getSDPATH() {
		return SDCardRoot;
	}
	public FileUtils() {
		//�õ���ǰ�ⲿ�洢�豸��Ŀ¼
		// /SDCARD
		SDCardRoot = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator;
	}
	/**
	 * ��SD���ϴ����ļ�
	 * 
	 * @throws IOException
	 *///Ŀ¼������dir�ļ�����
	
	public File createFileInSDCard(String fileName,String dir) throws IOException {
		File file = new File(SDCardRoot + dir+File.separator+fileName);
		file.createNewFile();
		return file;
	}
	
	/**
	 * ��SD���ϴ���Ŀ¼
	 * 
	 * @param dirName
	 */
	public File creatSDDir(String dir) {
		File dirFile = new File(SDCardRoot + dir);
		System.out.println(dirFile.mkdir());
		return dirFile;
	}

	/**
	 * �ж�SD���ϵ��ļ����Ƿ����
	 */
	public boolean isFileExist(String fileName){
		File file = new File(SDCardRoot + fileName);
		return file.exists();
	}
	
	/**
	 * ��һ��InputStream���������д�뵽SD����
	 */
	public File write2SDFromInput(String path,String fileName,InputStream input){
		File file = null;
		OutputStream output = null;
		try{
			creatSDDir(path);
			file = createFileInSDCard(fileName, path);
			output = new FileOutputStream(file);
			byte buffer [] = new byte[4 * 1024];
			int temp;
			while((temp=input.read(buffer)) != -1){
				output.write(buffer,0,temp);
			}
			output.flush();
		}
		catch(Exception e){
			e.printStackTrace();
		}
		finally{
			try{
				output.close();
			}
			catch(Exception e){
				e.printStackTrace();
			}
		}
		return file;
	}
	/*
	 * ��ȡĿsd���е�Ŀ¼�е��ļ������ֺʹ�С
	 * 
	 */
	
	public List<Mp3Info> getMp3Files(String path)throws Exception
	{
	    List<Mp3Info> mp3Infos=new ArrayList<Mp3Info>();
	    File file=new File(SDCardRoot+File.separator+path);
	    File [] files= file.listFiles();//���ص�ǰ�ļ����е������ļ�
	    
	    for (int i = 0; i < files.length; i++) {
	    	if(files[i].getName().endsWith("mp3"))
	    	{
	    		String pathllll=SDCardRoot+path+files[i].getName();
	    		Mp3Info mp3Info=new Mp3Info();
	    		mp3Info.setMp3Name(files[i].getName());
	    		mp3Info.setMp3Size(files[i].length()+"");
	    		mp3Infos.add(mp3Info);
	    	}
		}
	    return mp3Infos;
	}
	
	
	
	public static int getMp3TrackLength(File mp3File) {  
	    try {  
	        MP3File f = (MP3File)AudioFileIO.read(mp3File);  
	        MP3AudioHeader audioHeader = (MP3AudioHeader)f.getAudioHeader();  
	        return audioHeader.getTrackLength();      
	    } catch(Exception e) {  
	        return -1;  
	    }  
	}  
	
	
	
		
	
	
	
	
}
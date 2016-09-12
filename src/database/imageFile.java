package database;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class imageFile {
	private String name=null;
	private BufferedImage image = null;
	private String lastSeen;
	public imageFile(String n,BufferedImage i){
		name = n;
		image = i;
		
	}
	public BufferedImage getImage(){
		
		return image;
	}
	public String getName(){
		
		return name;
	}
	public String getLastSeen(){
		
		return lastSeen;
	}
	public int setLastSeen(String l){
		lastSeen = l;
		if(lastSeen.equals(l)){
			return 0;
		}else{
			return 1;
		}
	}
}

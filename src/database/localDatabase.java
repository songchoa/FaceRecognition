package database;
//import imageFile.java;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

import javax.imageio.ImageIO;

public class localDatabase {
	imageFile imf =null;
	BufferedImage img = null;
	ArrayList<imageFile> DB = new ArrayList<imageFile>();
	BufferedReader br = null;
	File file = null;
	public localDatabase(){
		String sCurrentLine;
		try {
			br = new BufferedReader(new FileReader("./DB/index"));
			while ((sCurrentLine = br.readLine()) != null) {
				System.out.println(sCurrentLine.split("-")[0]);
				String path = "./DB/"+sCurrentLine.split("-")[0]+".jpg";
				file = new File(path);
				img = ImageIO.read(file);
				imf = new imageFile(sCurrentLine.split("-")[0],img);
				DB.add(imf);
				}
		
			
		} catch (IOException e) {
		}
		
	}
	public imageFile get(String n){
		 for(int i=0;i<DB.size();i++){
			 if(DB.get(i).getName().equals(n)){
				 return DB.get(i);
			 }
		 }return null;
	}
	public int set(String n,String l,BufferedImage i){
		
		return 0;
	}

}

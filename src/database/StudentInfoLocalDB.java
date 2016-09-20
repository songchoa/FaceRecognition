package database;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

public class StudentInfoLocalDB {

	public static String[] getInfoByName(String name) throws FileNotFoundException {
		String[] result = new String[4];
		File f = new File("StudentInfoDB.txt");
		Scanner scan = new Scanner(f);
		boolean flag = true;
		while(scan.hasNextLine() && flag == true) {
			String s = scan.nextLine();
			if(s.contains(name)){
				result = s.split(":");
				flag = false;
			}
		}
		scan.close();
		return result;
	}
	
	public static void main(String[] args) throws FileNotFoundException {
		String[] str = StudentInfoLocalDB.getInfoByName("Chao Song");
	}
}

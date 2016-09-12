package database;


public class test {
	public  static void main(String[] args){
		localDatabase db = new localDatabase();
		System.out.println(db.get("test").getName());
	}
}

package test;

import db.DBConnection;

public class TestDB {
	public static void main(String[] args){
		DBConnection db=new DBConnection();
		db.getConn();
	}
}

package db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection  
    {  
        private String dbDriver="com.mysql.jdbc.Driver";   
        private String dbUrl="jdbc:mysql://localhost:3306/dataverify";//根据实际情况变化  
        private String dbUser="root";  
        private String dbPass="123456";  
        public Connection getConn()  
        {  
            Connection conn=null;  
            try  
            {  
                Class.forName(dbDriver);  
            }  
            catch (ClassNotFoundException e)  
            {  
                e.printStackTrace();  
            }  
            try  
            {  
                conn = DriverManager.getConnection(dbUrl,dbUser,dbPass);//注意是三个参数  
            }  
            catch (SQLException e)  
            {  
                e.printStackTrace();  
            } 
            if(conn==null)
            	System.out.println("connect database failure!");
            return conn;  
        }  
    }  
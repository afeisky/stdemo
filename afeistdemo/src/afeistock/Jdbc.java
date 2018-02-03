/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package afeistock;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 *
 * @author chaofei.wu
 */
public class Jdbc {
    //add lib : mysql-connector-java-5.1.44-bin.jar
    private boolean hasJdbc = true;
    private Connection conn;

    public Jdbc() {
        try {
            Class.forName("com.mysql.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            Global.loge("找不到驱动程序类 ，加载驱动失败！" + e.getMessage());
            e.printStackTrace();
            hasJdbc = false;
        }
    }

    public boolean getJdbc() {
        return hasJdbc;
    }
    public Connection getConn() {
        return conn;
    }
    public boolean init() {
        String url = "jdbc:mysql://localhost:3306/afeistock";
        String u="?useUnicode=true&characterEncoding=utf-8&useSSL=false";
        String username = "root";
        String password = "areepower";
        try {
            conn= DriverManager.getConnection(url+u, username, password);
        } catch (SQLException se) {
            Global.loge("数据库连接失败！");
            se.printStackTrace();
        }
        return true;
    }
    
    public ResultSet query(String sql){
        ResultSet rs=null;
        try {
         Statement stmt = conn.createStatement();
            rs = stmt.executeQuery(sql);// executeQuery会返回结果的集合，否则返回空值
            return rs;
        }catch (SQLException e) {
            Global.loge("MySQL操作错误");
            e.printStackTrace();
            return rs;
        } catch (Exception e) {
            e.printStackTrace();
            return rs;
        }   
    }
    public int update(String sql){
        int result=-1;
        try {
            Statement stmt = conn.createStatement();
            result = stmt.executeUpdate(sql);// executeUpdate语句会返回一个受影响的行数，如果返回-1就没有成功            
            return result;
        }catch (SQLException e) {
            Global.loge("MySQL操作错误");
            e.printStackTrace();
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            return result;
        }         
    }
    public int getRs(ResultSet rs){
            try {
                while (rs.next()) {
                    System.out.println(rs.getString(1).toString() + "\t" + rs.getString(2).toString());// 入如果返回的是int类型可以用getInt()
                }
            } catch (SQLException e) {
            Global.loge("MySQL操作错误");
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        } 
                return 1;
    }
    public void closeDb(){
        try{
            conn.close();
        }catch (SQLException e) {
            Global.loge("MySQL操作错误");
            e.printStackTrace();
        }  
    }
}

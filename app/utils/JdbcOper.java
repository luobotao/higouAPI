package utils;
import java.sql.CallableStatement;
import java.sql.Connection;  
import java.sql.DriverManager;  
import java.sql.PreparedStatement;  
import java.sql.ResultSet;
import java.sql.SQLException;  
import java.util.List;

import javax.transaction.Transaction;

import play.Logger;
import play.Play;
  
public class JdbcOper {  
//    public static final String url = "jdbc:mysql://192.168.30.233/cloudapireal?useUnicode=true&characterEncoding=UTF-8";  
    public static String url = "jdbc:mysql://127.0.0.1/xdshop?useUnicode=true&characterEncoding=UTF-8";  
    public static final String name = "com.mysql.jdbc.Driver";  
    public static String user = "yangtao";  
    public static String password = "neolix";  
  
    public Connection conn = null;  
    public PreparedStatement pst = null;  
	public CallableStatement cst=null;
	public ResultSet rs         = null;
	Transaction tran ;
	
	/* 持有私有静态实例，防止被引用，此处赋值为null，目的是实现延迟加载 */  
    private static JdbcOper instance = null;  
  
    /* 静态工程方法，创建实例 */  
    public static JdbcOper getInstance() {  
    	instance = new JdbcOper(); 
        return instance;   
    }  
  
    /* 私有构造方法，防止被实例化 */  
    private JdbcOper(){
    	try { 
    		url = Play.application().configuration().getString("druid.url.dev");
    		password = Play.application().configuration().getString("druid.password.dev");
    		if(Play.application().configuration().getBoolean("production", false)){
    			url = Play.application().configuration().getString("druid.url.product");
    			password = Play.application().configuration().getString("druid.password.product");
    		}
    		
			user = Play.application().configuration().getString("druid.username");
			Class.forName(name);//指定连接类型  
			conn = DriverManager.getConnection(url, user, password);//获取连接
    	 } catch (Exception e) {  
             e.printStackTrace();  
             if(instance!=null)
            	 instance.close();
             Logger.info( "JdbcOper init error"+e.toString());
         }  
	}
	
	
	
    public CallableStatement getCalledbleDao(String sql) { 
        try {  
        	if(conn==null){
        		Class.forName(name);//指定连接类型  
        		conn = DriverManager.getConnection(url, user, password);//获取连接
        	}
        	cst = conn.prepareCall(sql);
        } catch (Exception e) {  
            e.printStackTrace();  
            instance.close();
            Logger.info( "getCalledbleDao error"+e.toString()+"  ---!!! "+sql );
        }
        return cst;
    }  
  
    public PreparedStatement getPrepareStateDao(String sql) { 
        try {  
        	if(conn==null){
        		Class.forName(name);//指定连接类型  
        		conn = DriverManager.getConnection(url, user, password);//获取连接
        	}
        	pst = conn.prepareStatement(sql);//准备执行语句  
        } catch (Exception e) {  
            e.printStackTrace();  
            instance.close();
            Logger.info( "getPrepareStateDao error"+e.toString()+"  ---!!! "+sql );
        }  
        return pst;
    } 
    
    
    public void close() {  
    	try{
			if(tran!=null) 
				tran=null;
			if(rs!=null){
				rs.close();
				rs=null;
			} 
			if(cst!=null){
				cst.close();
				cst=null;
			} 
			if(pst!=null){
				pst.close();
				pst=null;
			} 
			if(conn!=null){
				conn.close();
				conn=null;
			} 
		}catch(Exception e){
			
		}
		
    }
    
    /**
	 * 执行关系数据库的update方法
	 * @param sql
	 * @return
	 */
	public static int updateSql(String sql) {
		JdbcOper db1 = JdbcOper.getInstance();// 创建JdbcOper对象
		db1.getPrepareStateDao(sql);
		int result = 0;
		try {
			result = db1.pst.executeUpdate();// 执行语句，得到结果集
		} catch (SQLException e) {
			e.printStackTrace();
			result=-1;
		}finally{
			db1.close();// 关闭连接
		}
		return result;
	}
	
    public static Boolean doTransaction(List<String> sqlList) { 
		JdbcOper db1 = JdbcOper.getInstance();// 创建JdbcOper对象
		int result = 0;
		try {
			if(db1.conn==null){
        		Class.forName(name);//指定连接类型  
        		db1.conn = DriverManager.getConnection(url, user, password);//获取连接
        	}
			db1.conn.setAutoCommit(false); // 将自动提交设置为false
			for (String sql : sqlList) {
				db1.getPrepareStateDao(sql);
				result = db1.pst.executeUpdate();// 执行语句，得到结果集
				Logger.info(sql + "============事务的sqlLIST"+result);
			}
			db1.conn.commit();      //当两个操作成功后手动提交   
		} catch (Exception e) {
			e.printStackTrace();
			try {
				db1.conn.rollback();
				return false;
			} catch (SQLException e1) {
				e1.printStackTrace();
				return false;
			}    //一旦其中一个操作出错都将回滚，使两个操作都不成功  
		} finally {
			db1.close();// 关闭连接
		}
		return true;
    } 
    
    public static void main(String[] args) {
    	 String sql = "select *from test1";//SQL语句  
    	 JdbcOper  db1 = JdbcOper.getInstance();//创建DBHelper对象  
    	 db1.getPrepareStateDao(sql);
         try {  
        	 ResultSet ret = db1.pst.executeQuery();//执行语句，得到结果集  
             while (ret.next()) {  
                 String uid = ret.getString(1);  
                 String ufname = ret.getString(2);  
                 String ulname = ret.getString(3);  
                 String udate = ret.getString(4);  
                 System.out.println(uid + "\t" + ufname + "\t" + ulname + "\t" + udate );  
             }//显示数据  
         } catch (SQLException e) {  
             e.printStackTrace();  
         }  
         
         String sql2 = "CREATE TABLE tableName (id int not null, name varchar(20) not null, age int null, primary key (id));";
    	 db1.getPrepareStateDao(sql2);
         try {  
        	 int a = db1.pst.executeUpdate();//执行语句，得到结果集  
              System.out.println(a);
             db1.close();//关闭连接  
         } catch (SQLException e) {  
             e.printStackTrace();  
         }  
	}
}  

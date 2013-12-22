package myworker;
import java.util.*;
import java.sql.*;
import static myworker.Config.*;

public class JDBC
{
	private static HashMap<String,String> db=getDBConfig();
	public static Connection getDBConnection()
	{
		try
		{
			Class.forName("com.mysql.jdbc.Driver").newInstance();
		}
		catch(Exception e)
		{
			e.printStackTrace(System.out);
			System.exit(0);
		}
		try
		{
			return DriverManager.getConnection("jdbc:mysql://"+db.get("host")+":"+db.get("port")+"/"+db.get("name")+"?user="+db.get("user")+"&password="+db.get("password"));
		}
		catch(SQLException e)
		{
			e.printStackTrace(System.out);
			System.exit(0);
		}
		System.exit(0);
		return null;
	}
}
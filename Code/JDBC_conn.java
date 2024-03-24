import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

public class JDBC_conn {

	public static void main(String[] args) {
		
		try {
			Class.forName("oracle.jdbc.driver.OracleDriver");
			Connection con=DriverManager.getConnection("jdbc:oracle:thin:@localhost:1521:xe","system","1098");
			
			Statement smt=con.createStatement();
			smt.executeUpdate("create table users(username varchar(50) PRIMARY KEY,password varchar(100))");
			
			System.out.print("Table Created successfully");
		}
		catch(Exception e)
		{
			System.out.print(e);
		}
	}

}

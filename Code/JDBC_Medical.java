import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
public class JDBC_Medical {

	public static void main(String[] args) {
				
				try {
					Class.forName("oracle.jdbc.driver.OracleDriver");
					Connection con=DriverManager.getConnection("jdbc:oracle:thin:@localhost:1521:xe","system","1098");
					
					Statement smt=con.createStatement();
					smt.executeUpdate("create table MedicationReminder(MedicationName varchar2(255),Dosage varchar2(50),Frequency varchar2(50),ScheduledDateTime timestamp,username varchar2(50),foreign key (username) references users(username))");
					
					System.out.print("Table Created successfully");
				}
				
				catch(Exception e)
				{
					System.out.print(e);
				}
			}

		}


	

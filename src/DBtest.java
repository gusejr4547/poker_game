import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class DBtest {
	Connection con = null;
	Statement stmt = null;
	ResultSet rs = null;

	String name;

	DBtest(){
		try {
			Class.forName("org.mariadb.jdbc.Driver");
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} // 2. dbms�� ������ �� �ִ� connection ��ü�� ����
		try {
			con = DriverManager.getConnection( "jdbc:mariadb://localhost:3306/poker","root","");
			stmt = con.createStatement();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	void insert(String name) {
		ResultSet check = null;
		boolean exist = false;
		try {
			check = stmt.executeQuery("select id from poker");
			while(check.next()) {
				String id = check.getString(1);
				if(id.equals(name)){
					exist = true;
					break;
				}
			}
			if(!exist) {
				int updatecount = stmt.executeUpdate( "insert into poker values('"+name+"', 0)");
				System.out.println("�Էµ� ���� �� : "+updatecount);
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	void update(String name) {
		try {
			rs = stmt.executeQuery("select �¸� from poker where id='"+name+"'");
			rs.next();
			int num = Integer.parseInt(rs.getString(1));
			num++;
			stmt.executeUpdate("update poker set �¸�="+num+" where id='"+name+"'");
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	void disconnect() {
		try {
			con.close();
			stmt.close();
			rs.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}


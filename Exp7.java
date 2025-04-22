import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class Exp7 {
    public static void main(String[] args) throws ClassNotFoundException, SQLException {
        Class.forName("com.mysql.cj.jdbc.Driver");
        Connection cc = DriverManager.getConnection("jdbc:mysql://localhost:3306/2b", "root", "bavish82");
        System.out.println("Connection established");
        Statement st = cc.createStatement();
        System.out.println("Database selected");
        ResultSet rs1 = st.executeQuery("select * from department");
        while (rs1.next()) {
            System.out.println(rs1.getInt(1) + " " + rs1.getString(2) + " " + rs1.getString(3));
        }
        System.out.println("Data fetched");
        st.executeUpdate("update department set Location='USA' where DepartmentID=1");
        System.out.println("Data updated");
        ResultSet rs3 = st.executeQuery("select * from department");
        while (rs3.next()) {
            System.out.println(rs3.getInt(1) + " " + rs3.getString(2) + " " + rs3.getString(3));
        }
        st.executeUpdate("delete from department where Location='USA'");
        System.out.println("Data deleted");
        cc.close();
    }
}

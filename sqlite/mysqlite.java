
import java.sql.*;
import java.util.*;

public class mysqlite {
    Statement stmt;
    enum OpMode { CREATE, SHOW, UPDATE}
  OpMode mode=OpMode.SHOW;
   //   OpMode mode=OpMode.UPDATE;

    private Connection connect() {

        String url = "jdbc:sqlite:test.db";
        Connection conn = null;  
        try {
            conn = DriverManager.getConnection(url);  
        } catch (SQLException e) {
            System.out.println(e.getMessage());  
        }
        return conn;  
    }  

    public void createTables()throws SQLException
    {
        stmt.executeUpdate("CREATE TABLE movie(title, year, score)");

        stmt.executeUpdate("CREATE TABLE person(name, age, weight, color)");
    }

    public void insertTables()throws SQLException
    {
        stmt.executeUpdate("INSERT INTO movie (title, year, score) VALUES" 
                           +"('Monty Python Live at the Hollywood Bowl', 1982, 7.9),"
                           +"('Monty Pythons The Meaning of Life', 1983, 7.5),"
                           +"('Monty Pythons Life of Brian', 1979, 8.0),"
                           +"('Eat my shorts buckwheat', 2022, 1.0),"
                           +"('I dunno whats up here', 2022, 1.0)");

        stmt.executeUpdate("INSERT INTO person (name, age, weight, color) VALUES" 
                           +"('bruce', 66, 277, 'blue'),"  
                           +"('joe', 22, 200, 'green'),"  
                           +"('mac', 33, 150,'black'),"   
                           +"('joe', 44, 340, 'red'),"    
                           +"('jack', 55, 125, 'yellow')," 
                           +"('Jill', 66, 277, 'purple')," 
                           +"('Mary', 22, 200, 'grey')," 
                           +"('Tim', 33, 150, 'white'),"   
                           +"('Helen', 44, 340, 'orange'),"
                           +"('jackie', 55, 125,'teal'),"  
                           +"('fred', 35, 150,'green')");  

    }
    public void dumpTable(String sql)throws SQLException
    {
        ResultSet rs    = stmt.executeQuery(sql);  

        ResultSetMetaData rsmd = rs.getMetaData();
        int columnsNumber = rsmd.getColumnCount();
        int rowCnt=0;
        while (rs.next()) {
            System.out.println();
            rowCnt++;
            for (int i = 1; i <= columnsNumber; i++) {
                String columnValue = rs.getString(i);
                System.out.print(rsmd.getColumnName(i) + " : " + columnValue+", ");
            }
        }
    }

    public void selectAll()throws SQLException
    {

        dumpTable("SELECT name FROM sqlite_master WHERE type='table'");

        dumpTable("SELECT * FROM person");

        dumpTable("SELECT * FROM movie");                    
    }  

    public void initDB()throws SQLException
    {  
        Connection conn = this.connect();   
        stmt  = conn.createStatement();    
    }

    public void updatePerson(String dbName, String values)throws SQLException
    {
        stmt.executeUpdate("INSERT INTO person (name, age, weight, color) VALUES "+values);
    }

    public static void main(String[] args) {
        try {
            mysqlite app = new mysqlite();  

            app.initDB();

            if (app.mode==OpMode.CREATE) {
                app.createTables();
                app.insertTables();
            } else if (app.mode==OpMode.SHOW) {
                app.selectAll();
            } else if (app.mode==OpMode.UPDATE) {
               app.updatePerson("person", "('Alan', 50, 175, 'black')");
            }



        } catch (Exception e) {
            System.out.println(e.getMessage());  
        }
    }  

}  

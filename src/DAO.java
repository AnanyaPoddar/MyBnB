import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.ParseException;
import java.util.Scanner;

public class DAO {

  private static final String dbClassName = "com.mysql.cj.jdbc.Driver";
  private static final String CONNECTION = "jdbc:mysql://127.0.0.1/mydb";

  public static void addUser(Connection conn, Scanner myObj) {
    System.out.println("Provide your SIN");
    int sin = Integer.parseInt(myObj.nextLine()); // Read user input

    // TODO Only want <= 100 chars
    // TODO Prevent duplicate errors (try catch?)

    System.out.println("Provide your name");
    String name = myObj.nextLine();
    System.out.println("Provide your address");
    String addr = myObj.nextLine();
    System.out.println("Provide your occupation");
    String occupation = myObj.nextLine();

    System.out.println("Provide your date of birth in YYYY-MM-DD format");
    String dob = myObj.nextLine();

    try {
      Statement insert = conn.createStatement();
      String sqlInsert =
          String.format("INSERT INTO USER VALUES (%d, %s, %s, %s, '%s');", sin,
              name, addr, occupation, dob);
      insert.executeUpdate(sqlInsert);
    } catch (SQLException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }

  public static void viewOneUser(Connection conn, Scanner myObj) {
    System.out.println("Provide the SIN of the user");
    int SIN = Integer.parseInt(myObj.nextLine());

    try {
      Statement stmt = conn.createStatement();
      String sql = "SELECT * FROM User WHERE SIN = " + SIN + ";";
      ResultSet rs = stmt.executeQuery(sql);

      // Extract results
      while (rs.next()) {
        // Retrieve by column name
        int sid = rs.getInt("SIN");
        String uname = rs.getString("uname");
        String uaddress = rs.getString("uaddress");
        String uoccupation = rs.getString("uoccupation");
        String udob = rs.getString("uDOB");

        // Display values
        System.out.print("ID: " + sid);
        System.out.print(", Name: " + uname);
        System.out.print(", Address: " + uaddress);
        System.out.print(", Occupation: " + uoccupation);
        System.out.println(", Date of Birth: " + udob);
      }
      rs.close();
    } catch (SQLException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

  }

  public static void viewAllUsers(Connection conn, Scanner myObj) {

    try {
      Statement stmt = conn.createStatement();
      String sql = "SELECT * FROM User;";
      ResultSet rs = stmt.executeQuery(sql);

      // Extract results
      while (rs.next()) {
        // Retrieve by column name
        int sid = rs.getInt("SIN");
        String uname = rs.getString("uname");
        String uaddress = rs.getString("uaddress");
        String uoccupation = rs.getString("uoccupation");
        String udob = rs.getString("uDOB");

        // Display values
        System.out.print("ID: " + sid);
        System.out.print(", Name: " + uname);
        System.out.print(", Address: " + uaddress);
        System.out.print(", Occupation: " + uoccupation);
        System.out.println(", Date of Birth: " + udob);
      }
      rs.close();
    } catch (SQLException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

  }

  public static void addListing(Connection conn, Scanner myObj) {
    System.out.println("Are you renting out the entire place? 0 = No, 1 = Yes");
    // int entire = Integer.parseInt(myObj.nextLine());
    String entire = myObj.nextLine();

    try {
      Statement insert = conn.createStatement();
      String sqlInsert =
          "INSERT INTO Listing (entire) VALUES ( " + entire + ");";
      insert.executeUpdate(sqlInsert);
    } catch (SQLException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }

  public static void viewAllListings(Connection conn, Scanner myObj) {

    try {
      Statement stmt = conn.createStatement();
      String sql = "SELECT * FROM Listing;";
      ResultSet rs = stmt.executeQuery(sql);

      // Extract results
      while (rs.next()) {
        // Retrieve by column name
        int listID = rs.getInt("listID");
        boolean entire = rs.getBoolean("entire");

        // Display values
        System.out.print("ID: " + listID);
        System.out.println(", Entire: " + entire);
      }
      rs.close();
    } catch (SQLException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

  }

  public static void main(String[] args)
      throws ClassNotFoundException, ParseException {
    // Register JDBC driver
    Class.forName(dbClassName);
    // Database credentials
    final String USER = "root";
    final String PASS = ""; // !TODO Don't commit!
    System.out.println("Connecting to database...");

    // TODO Case sensitivity for the queries?
    // TODO there should be a DAO where the methods reside and the other stuff
    // is in a different file
    // TODO These options will be buttons so that the user can't just enter
    // anything willy nilly

    try {
      // Establish connection
      Connection conn = DriverManager.getConnection(CONNECTION, USER, PASS);
      System.out.println("Successfully connected to MySQL!");

      System.out.println("Preparing a statement...");
      Statement stmt = conn.createStatement();

      // Create a table User if it doesn't already exist
      String userTable =
          "CREATE TABLE IF NOT EXISTS USER " + "(SIN INT NOT NULL PRIMARY KEY, "
              + " uname VARCHAR(100) NOT NULL, " + " uaddress VARCHAR(100), "
              + " uoccupation VARCHAR(20), " + " uDOB DATE)";
      // TODO maybe use DATEDIFF so that today's date - uDOB >= 18 years? CHECK
      // (Age>=18)
      // TODO password for a user so that they can log in

      stmt.executeUpdate(userTable);
      System.out.println("Created User table in given database...");


      // Create a table Listing if it doesn't already exist

      String listingTable = "CREATE TABLE IF NOT EXISTS Listing "
          + "(listID INT NOT NULL AUTO_INCREMENT PRIMARY KEY, "
          + " entire BOOLEAN)";

      stmt.executeUpdate(listingTable);
      System.out.println("Created Listing table in given database...");



      Scanner myObj = new Scanner(System.in); // Create a Scanner object

      String exit = "-1";
      while (!exit.equals("0")) {

        // Choices for user
        System.out.println("Enter 0 to exit");
        System.out.println("Enter 1 to add a new User");
        System.out.println("Enter 2 to get a user based on SIN");
        System.out.println("Enter 3 to view all users");
        System.out.println("Enter 4 to add a listing");
        System.out.println("Enter 5 to view all listings");

        exit = myObj.nextLine(); // Read user choice

        if (exit.equals("1")) {
          addUser(conn, myObj);
        }
        if (exit.equals("2")) {
          viewOneUser(conn, myObj);
        }
        if (exit.equals("3")) {
          viewAllUsers(conn, myObj);
        }
        if (exit.equals("4")) {
          addListing(conn, myObj);
        }
        if (exit.equals("5")) {
          viewAllListings(conn, myObj);
        }

      }


      System.out.println("Closing connection...");

      stmt.close();
      conn.close();
      System.out.println("Success!");
    } catch (SQLException e) {
      e.printStackTrace(System.out);
      System.err.println("Connection error occured!");
    }
  }

}

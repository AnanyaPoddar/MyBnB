import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.ParseException;
import java.util.Scanner;
//import io.github.cdimascio.dotenv.Dotenv;

public class DAO {

  private static final String dbClassName = "com.mysql.cj.jdbc.Driver";
  private static final String CONNECTION = "jdbc:mysql://127.0.0.1/mydb";
  static int loggedInUser = -1; // initial + do we need private/static/etc?

  public static void addUser(Connection conn, Scanner myObj) {

    // TODO Prevent errors from database and instead check user input before
    // trying to insert
    // OR is it supposed to be like allll constraints on the

    System.out.println("Provide your SIN");
    int sin = Integer.parseInt(myObj.nextLine()); // Read user input
    System.out.println("Provide a password");
    String password = myObj.nextLine();
    System.out.println("Provide your name");
    String name = myObj.nextLine();
    System.out.println("Provide your address");
    String addr = myObj.nextLine();
    System.out.println("Provide your occupation");
    String occupation = myObj.nextLine();
    System.out.println("Provide your date of birth in YYYY-MM-DD format");
    String dob = myObj.nextLine();

    System.out.println(
        "Are you a renter or a host? R = Renter, any other key = Host");
    String rOrH = myObj.nextLine();

    String rentOrHostInsert;
    if (rOrH.equals("r") || rOrH.equals("R")) {
      // " cardType VARCHAR(12) NOT NULL, " + " cardNum INT NOT NULL, "
      // TODO Should this be open choice or C = Credit, D = Debit
      System.out.println("Provide a payment method (Credit or Debit)");
      String cardType = myObj.nextLine();
      System.out.println("Provide your card Number");
      int cardNum = Integer.parseInt(myObj.nextLine()); // Read user input
      rentOrHostInsert = String.format(
          "INSERT INTO Renter VALUES (%d, '%s', %d);", sin, cardType, cardNum);
    } else {
      rentOrHostInsert = String.format("INSERT INTO Host VALUES (%d);", sin);
    }

    System.out.println(rentOrHostInsert);


    try {
      // TODO Either both are inserted or neither is?
      Statement insert = conn.createStatement();
      String userInsert = String.format(
          "INSERT INTO USER VALUES (%d, '%s', '%s', '%s', '%s', '%s');", sin,
          password, name, addr, occupation, dob);

      System.out.println(userInsert);
      insert.executeUpdate(userInsert);
      insert.executeUpdate(rentOrHostInsert);
    } catch (SQLException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }

  public static void login(Connection conn, Scanner myObj) {

    if (loggedInUser != -1) {
      System.out.println("You're already logged in as: " + loggedInUser);
      return;
    }
    System.out.println("Provide your SIN");
    int SIN = Integer.parseInt(myObj.nextLine());
    System.out.println("Provide you password");
    String password = myObj.nextLine();

    try {
      Statement stmt = conn.createStatement();
      String sql = "SELECT * FROM User WHERE SIN = " + SIN + ";";
      ResultSet rs = stmt.executeQuery(sql);

      if (rs.next()) {
        if (password.equals(rs.getString("upassword"))) {
          loggedInUser = SIN;
          System.out.println("Succesfully logged in as: " + loggedInUser);

        } else {
          System.out.println("Wrong password.");

        }
      } else {
        System.out.println("No user of that SIN exists.");
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

  public static void deleteUser(Connection conn, Scanner myObj) {
    // TODO ! What other tables/relationships should be affected if a Host
    // deletes? If a Renter deletes?s

    if (loggedInUser == -1) {
      System.out.println("You must be logged in to delete your account");
      return;
    }

    System.out.println("Press 1 to indicate you want to delete your account");
    int confirm = Integer.parseInt(myObj.nextLine());
    if (confirm != 1) {
      System.out.println("Not deleting.");
      return;
    }
    System.out.println("Deleting Account of" + loggedInUser);

    try {

      Statement stmt = conn.createStatement();
      String sql = "DELETE FROM user WHERE SIN = " + loggedInUser + ";";
      stmt.executeUpdate(sql);

      loggedInUser = -1;
      System.out.println("Account deleted and logged out." + loggedInUser);
    } catch (SQLException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }

  public static void logout() {
    if (loggedInUser == -1) {
      System.out.println("You're already logged out.");
      return;
    }

    loggedInUser = -1;
    System.out.println("You've been logged out");
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
//    Dotenv dotenv = Dotenv.configure().load();
//    final String PASS = dotenv.get("PASS");
    final String PASS = "root";
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
              + " upassword VARCHAR(12) NOT NULL, "
              + " uname VARCHAR(100) NOT NULL, " + " uaddress VARCHAR(100), "
              + " uoccupation VARCHAR(20), " + " uDOB DATE)";
      // TODO CHECK (DATEDIFF ...) , CHECK (DATEDIFF("
      // + [current date somehow] + ", uDOB) >= "18)
      // SELECT DATEDIFF(u.uDOB, s.uDOB) FROM User u, User s;

      stmt.executeUpdate(userTable);
      System.out.println("Created User table in given database...");

      // Create a table Host if it doesn't already exist
      String hostTable = "CREATE TABLE IF NOT EXISTS HOST "
          + "(HostSIN INT NOT NULL PRIMARY KEY,"
          + "INDEX par_ind (HostSIN), FOREIGN KEY (HostSIN) REFERENCES USER(SIN) ON DELETE CASCADE)";
      // i don't get the index thing
      // On Delete Cascade: if you delete from Host, nothing happens to User. If
      // you delete from user, the row is gone from Host

      stmt.executeUpdate(hostTable);
      System.out.println("Created Host table in given database...");

      // Create a table Renter if it doesn't already exist

      //TODO: Def doesn't matter but can we make everything plural
      String renterTable = "CREATE TABLE IF NOT EXISTS RENTER "
          + "(RenterSIN INT NOT NULL PRIMARY KEY,"
          + " cardType VARCHAR(12) NOT NULL, " + " cardNum INT NOT NULL, "
          + "INDEX par_ind (RenterSIN), FOREIGN KEY (RenterSIN) REFERENCES USER(SIN) ON DELETE CASCADE)";

      stmt.executeUpdate(renterTable);
      System.out.println("Created Renter table in given database...");

      // Create a table Listing if it doesn't already exist

      String listingTable = "CREATE TABLE IF NOT EXISTS Listings "
          + "(listID INT NOT NULL AUTO_INCREMENT PRIMARY KEY, " +
          "price FLOAT NOT NULL, type VARCHAR(10) NOT NULL)";


      String hostsToListingTable = "CREATE TABLE IF NOT EXISTS HostsToListings "
      + "(listID INT NOT NULL, FOREIGN KEY (listID) REFERENCES Listings(listID), " +
      "hostSIN INT NOT NULL, FOREIGN KEY (hostSIN) REFERENCES Host(hostSIN), PRIMARY KEY(listID, hostSIN))";

      stmt.executeUpdate(listingTable);
      System.out.println("Created Listings table in given database...");
      
      stmt.executeUpdate(hostsToListingTable);
      System.out.println("Created HostsTolistings table in given database...");

      String availabilitiesTable = "CREATE TABLE IF NOT EXISTS Availabilities "
      + "(date DATE NOT NULL, listID INT NOT NULL, FOREIGN KEY (listID) REFERENCES Listings(listID), " + 
      "PRIMARY KEY(listID, date) )";

      

      stmt.executeUpdate(availabilitiesTable);
      System.out.println("Created Availabilities table in given database...");

      Scanner myObj = new Scanner(System.in); // Create a Scanner object

      String exit = "-1";
      while (!exit.equals("0")) {

        // Choices for user
        System.out.println("Enter 0 to exit");
        System.out.println("Enter 1 to add a new User");
        System.out.println("Enter 2 to log in based on SIN/password");
        System.out.println("Enter 3 to view all users");
        System.out.println("Enter 4 to add a listing");
        System.out.println("Enter 5 to view all listings");
        System.out.println("Enter 6 to delete your account");
        System.out.println("Enter 7 to log out");
        exit = myObj.nextLine(); // Read user choice

        if (exit.equals("1")) {
          addUser(conn, myObj);
        }
        if (exit.equals("2")) {
          login(conn, myObj);
        }
        if (exit.equals("3")) {
          viewAllUsers(conn, myObj);
        }
        if (exit.equals("4")) {
          ListingDAO.addListing(conn, loggedInUser, myObj);
        }
        if (exit.equals("5")) {
          ListingDAO.viewAllListings(conn, myObj);
        }
        if (exit.equals("6")) {
          deleteUser(conn, myObj);
        }
        if (exit.equals("7")) {
          logout();
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

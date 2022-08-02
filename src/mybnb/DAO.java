package mybnb;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.ParseException;
import java.util.Scanner;
// import io.github.cdimascio.dotenv.Dotenv;

public class DAO {

  private static final String dbClassName = "com.mysql.cj.jdbc.Driver";
  private static final String CONNECTION = "jdbc:mysql://127.0.0.1/mydb";
  static int loggedInUser = -1; // initial + do we need private/static/etc?

  

  public static void main(String[] args)
      throws ClassNotFoundException, ParseException {
    // Register JDBC driver
    Class.forName(dbClassName);
    // Database credentials
    final String USER = "root";
    // Dotenv dotenv = Dotenv.configure().load();
    // final String PASS = dotenv.get("PASS");
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

      // TODO: Def doesn't matter but can we make everything plural
      String renterTable = "CREATE TABLE IF NOT EXISTS RENTER "
          + "(RenterSIN INT NOT NULL PRIMARY KEY,"
          + " cardType VARCHAR(12) NOT NULL, " + " cardNum INT NOT NULL, "
          + "INDEX par_ind (RenterSIN), FOREIGN KEY (RenterSIN) REFERENCES USER(SIN) ON DELETE CASCADE)";

      stmt.executeUpdate(renterTable);
      System.out.println("Created Renter table in given database...");

      // Create a table Listing if it doesn't already exist

      String listingTable = "CREATE TABLE IF NOT EXISTS Listings "
          + "(listID INT NOT NULL AUTO_INCREMENT PRIMARY KEY, "
          + "price FLOAT NOT NULL, type VARCHAR(10) NOT NULL)";

      String hostsToListingTable = "CREATE TABLE IF NOT EXISTS HostsToListings "
          + "(listID INT NOT NULL, FOREIGN KEY (listID) REFERENCES Listings(listID) ON DELETE CASCADE, "
          + "hostSIN INT NOT NULL, FOREIGN KEY (hostSIN) REFERENCES Host(hostSIN) ON DELETE CASCADE, " +
          " PRIMARY KEY(listID, hostSIN))";

      stmt.executeUpdate(listingTable);
      System.out.println("Created Listings table in given database...");
      
      stmt.executeUpdate(hostsToListingTable);
      System.out.println("Created HostsTolistings table in given database...");

      String availabilitiesTable = "CREATE TABLE IF NOT EXISTS Availabilities "
          + "(date DATE NOT NULL, listID INT NOT NULL, " +
          "FOREIGN KEY (listID) REFERENCES Listings(listID) ON DELETE CASCADE, " +
          "PRIMARY KEY(listID, date) )";

      stmt.executeUpdate(availabilitiesTable);
      System.out.println("Created Availabilities table in given database...");

      // TODO Delete when Ananya comes up with actual Booked table
      String bookedTable = "CREATE TABLE IF NOT EXISTS Booked "
          + "(listID INT NOT NULL, FOREIGN KEY (listID) REFERENCES Listings(listID), "
          + "renterSIN INT NOT NULL, FOREIGN KEY (renterSIN) REFERENCES Renter(renterSIN), PRIMARY KEY(listID, renterSIN))";
          stmt.executeUpdate(bookedTable);
          System.out.println("Created Booked table in given database...");

      // Must provide both rating and comment when providing a review
      String rentersReviewHosts = "CREATE TABLE IF NOT EXISTS rentersReviewHosts "
        + "(hostSIN INT NOT NULL, FOREIGN KEY (hostSIN) REFERENCES Host(hostSIN), "
        + "renterSIN INT NOT NULL, FOREIGN KEY (renterSIN) REFERENCES Renter(renterSIN)," + 
        " review VARCHAR(100) NOT NULL, " + " rating INT NOT NULL, " +
        "PRIMARY KEY(hostSIN, renterSIN))";
      stmt.executeUpdate(rentersReviewHosts);
      System.out.println("Created renterReviewsHost table in given database...");

      // Must provide both rating and comment when providing a review
      String hostsReviewRenters = "CREATE TABLE IF NOT EXISTS hostsReviewRenters "
        + "(hostSIN INT NOT NULL, FOREIGN KEY (hostSIN) REFERENCES Host(hostSIN), "
        + "renterSIN INT NOT NULL, FOREIGN KEY (renterSIN) REFERENCES Renter(renterSIN)," + 
        " review VARCHAR(100) NOT NULL, " + " rating INT NOT NULL, " +
        "PRIMARY KEY(hostSIN, renterSIN))";
      stmt.executeUpdate(hostsReviewRenters);
      System.out.println("Created hostsReviewRenters table in given database...");








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
        System.out.println("Enter 8 to delete a listing");
        System.out.println("Enter 9 to see all availabilities for a listing");
        System.out.println("Enter 10 to review a host");
        System.out.println("Enter 11 to review a renter");
        System.out.println("Enter 12 to modify availabilities for a listing");
        exit = myObj.nextLine(); // Read user choice

        if (exit.equals("1")) {
          UserDAO.addUser(conn, myObj);
        }
        if (exit.equals("2")) {
          UserDAO.login(conn, myObj);
        }
        if (exit.equals("3")) {
          UserDAO.viewAllUsers(conn);
        }
        if (exit.equals("4")) {
          ListingDAO.addListing(conn, loggedInUser, myObj);
        }
        if (exit.equals("5")) {
          ListingDAO.viewAllListings(conn, myObj);
        }
        if (exit.equals("6")) {
          UserDAO.deleteUser(conn, myObj);
        }
        if (exit.equals("7")) {
          UserDAO.logout();
        }
        if (exit.equals("8")) {
          ListingDAO.deleteListing(conn, loggedInUser, myObj);
        }
        if (exit.equals("9")) {
          AvailabilityDAO.getAvailabilities(conn, myObj);
        }
        if (exit.equals("10")) {
          UserDAO.renterReviewsHost(conn, myObj);
        }
        if (exit.equals("11")) {
          UserDAO.hostReviewsRenter(conn, myObj);
        }
        if (exit.equals("12")) {
          AvailabilityDAO.modifyAvailabilities(conn, loggedInUser, myObj);
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

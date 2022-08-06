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

    try {
      // Establish connection
      Connection conn = DriverManager.getConnection(CONNECTION, USER, PASS);
      System.out.println("Successfully connected to MySQL!");

      System.out.println("Preparing a statement...");
      Statement stmt = conn.createStatement();


      // Create a table User if it doesn't already exist
      String userTable =
          "CREATE TABLE IF NOT EXISTS USER " + "(SIN INT NOT NULL PRIMARY KEY "
              + " CONSTRAINT CK_SIN_LENGTH check (length(SIN) = 9), upassword VARCHAR(12) NOT NULL, "
              + " uname VARCHAR(100) NOT NULL UNIQUE, " + " uaddress VARCHAR(100), "
              + " uoccupation VARCHAR(20), " + " uDOB DATE)";

      stmt.executeUpdate(userTable);
      System.out.println("Created User table in given database...");

      // Create a table Host if it doesn't already exist
      String hostTable = "CREATE TABLE IF NOT EXISTS HOST "
          + "(HostSIN INT NOT NULL PRIMARY KEY,"
          + "FOREIGN KEY (HostSIN) REFERENCES USER(SIN) ON DELETE CASCADE)";
      // i don't get the index thing
      // On Delete Cascade: if you delete from Host, nothing happens to User. If
      // you delete from user, the row is gone from Host

      stmt.executeUpdate(hostTable);
      System.out.println("Created Host table in given database...");

      // Create a table Renter if it doesn't already exist

      String renterTable = "CREATE TABLE IF NOT EXISTS RENTER "
          + "(RenterSIN INT NOT NULL PRIMARY KEY,"
          + " cardType VARCHAR(12) NOT NULL, " 
          + " cardNum varchar(16) NOT NULL CONSTRAINT CK_cardNum_LENGTH check (length(cardNum) = 16), "
          + "FOREIGN KEY (RenterSIN) REFERENCES USER(SIN) ON DELETE CASCADE)";

      stmt.executeUpdate(renterTable);
      System.out.println("Created Renter table in given database...");

      String listingTable = "CREATE TABLE IF NOT EXISTS Listings "
          + "(listID INT NOT NULL AUTO_INCREMENT PRIMARY KEY, "
          + "type VARCHAR(10) NOT NULL)";

      String hostsToListingTable = "CREATE TABLE IF NOT EXISTS HostsToListings "
          + "(listID INT NOT NULL, FOREIGN KEY (listID) REFERENCES Listings(listID) ON DELETE CASCADE, "
          + "hostSIN INT NOT NULL, FOREIGN KEY (hostSIN) REFERENCES Host(hostSIN) ON DELETE CASCADE, " +
          " PRIMARY KEY(listID, hostSIN))";

      stmt.executeUpdate(listingTable);
      System.out.println("Created Listings table in given database...");
      
      stmt.executeUpdate(hostsToListingTable);
      System.out.println("Created HostsTolistings table in given database...");

      //status options for availabilities: booked, available, past, cancelled
      String availabilitiesTable = "CREATE TABLE IF NOT EXISTS Availabilities "
          + "(date DATE NOT NULL, listID INT NOT NULL, price FLOAT NOT NULL check (price >= 0), " +
          "status varchar(11) NOT NULL default 'available', " +
          "FOREIGN KEY (listID) REFERENCES Listings(listID) ON DELETE CASCADE, " +
          "PRIMARY KEY(listID, date) )";

      stmt.executeUpdate(availabilitiesTable);
      System.out.println("Created Availabilities table in given database...");

      //updated table to add cost
      String bookedTable = "CREATE TABLE IF NOT EXISTS Booked "
          + "(listID INT NOT NULL, FOREIGN KEY (listID) REFERENCES Listings(listID) ON DELETE CASCADE, "
          + "renterSIN INT NOT NULL, FOREIGN KEY (renterSIN) REFERENCES Renter(renterSIN) ON DELETE CASCADE, cost FLOAT NOT NULL check (cost >= 0), " 
          + "startDate DATE NOT NULL, endDate DATE NOT NULL, status varchar(10) NOT NULL DEFAULT 'booked', PRIMARY KEY(listID, startDate, endDate))";
          stmt.executeUpdate(bookedTable);
          System.out.println("Created Booked table in given database...");

      // Must provide both rating and comment when providing a review
      // NOTE: not doing on delete cascade 
      String rentersReviewHosts = "CREATE TABLE IF NOT EXISTS rentersReviewHosts "
        + "(hostSIN INT NOT NULL, FOREIGN KEY (hostSIN) REFERENCES Host(hostSIN) ON DELETE CASCADE, "
        + "renterSIN INT NOT NULL, FOREIGN KEY (renterSIN) REFERENCES Renter(renterSIN) ON DELETE CASCADE," + 
        " review VARCHAR(100) NOT NULL, " + " rating INT NOT NULL CONSTRAINT CK_rating  check (rating >= 1 and rating <= 5), " +
        "PRIMARY KEY(hostSIN, renterSIN))";
      stmt.executeUpdate(rentersReviewHosts);
      System.out.println("Created renterReviewsHost table in given database...");

      // Must provide both rating and comment when providing a review
      String hostsReviewRenters = "CREATE TABLE IF NOT EXISTS hostsReviewRenters "
        + "(hostSIN INT NOT NULL, FOREIGN KEY (hostSIN) REFERENCES Host(hostSIN) ON DELETE CASCADE, "
        + "renterSIN INT NOT NULL, FOREIGN KEY (renterSIN) REFERENCES Renter(renterSIN) ON DELETE CASCADE," + 
        " review VARCHAR(100) NOT NULL, " + " rating INT NOT NULL CONSTRAINT CK_rating2  check (rating >= 1 and rating <= 5), " +
        "PRIMARY KEY(hostSIN, renterSIN))";
      stmt.executeUpdate(hostsReviewRenters);
      System.out.println("Created hostsReviewRenters table in given database...");

      // Must provide both rating and comment when providing a review
      String rentersReviewListings = "CREATE TABLE IF NOT EXISTS rentersReviewListings "
        + "(listID INT NOT NULL, FOREIGN KEY (listID) REFERENCES Listings(listID) ON DELETE CASCADE, "
        + "renterSIN INT NOT NULL, FOREIGN KEY (renterSIN) REFERENCES Renter(renterSIN) ON DELETE CASCADE," + 
        " review VARCHAR(100) NOT NULL, " + " rating INT NOT NULL CONSTRAINT CK_rating3 check (rating >= 1 and rating <= 5), " +
        "PRIMARY KEY(listID, renterSIN))";
      stmt.executeUpdate(rentersReviewListings);
      System.out.println("Created rentersReviewListings table in given database...");

      // TODO latitude and longitude don't have to be keys here?
      String locationsTable = "CREATE TABLE IF NOT EXISTS Locations "
          + "(listID INT NOT NULL, FOREIGN KEY (listID) REFERENCES Listings(listID), " 
          + "latitude FLOAT NOT NULL CONSTRAINT CK_latitude  check (latitude >= -90 and latitude <= 90)," + 
          " longitude FLOAT NOT NULL CONSTRAINT CK_longitude check (longitude >= -180 and longitude <= 180), " +
          "PRIMARY KEY(listID))";
      stmt.executeUpdate(locationsTable);
      System.out.println("Created locationsTable table in given database...");

      // TODO does listID have to be a key or just unique?
      // TODO unit#, street, postal are enough to constitute a key right?
      String addressesTable =
          "CREATE TABLE IF NOT EXISTS ADDRESSES " 
          + "(listID INT NOT NULL UNIQUE, FOREIGN KEY (listID) REFERENCES Listings(listID), " 
          + "unitNum INT NOT NULL, "
          + " street VARCHAR(50) NOT NULL, "
          + " city VARCHAR(25) NOT NULL, " 
          + " country VARCHAR(100) NOT NULL, "
          + " postal VARCHAR(10) NOT NULL, PRIMARY KEY (unitNum, street, postal))";
      stmt.executeUpdate(addressesTable);
      System.out.println("Created addresses table in given database...");

      String amenitiesTable = "CREATE TABLE IF NOT EXISTS AMENITIES "
          + "(name VARCHAR(50) NOT NULL PRIMARY KEY,"
          + "type VARCHAR(50) NOT NULL)";
      stmt.executeUpdate(amenitiesTable);
      System.out.println("Created amenities table in given database...");

      String ListingsHaveAmenities = "CREATE TABLE IF NOT EXISTS ListingsHaveAmenities "
          + "(listID INT NOT NULL, name VARCHAR(50) NOT NULL, " +
          "FOREIGN KEY (name) REFERENCES AMENITIES(name) ON DELETE CASCADE, " + 
          "FOREIGN KEY (listID) REFERENCES Listings(listID) ON DELETE CASCADE, " +
          "PRIMARY KEY(name, listID) )";

      stmt.executeUpdate(ListingsHaveAmenities);
      System.out.println("Created ListingsHaveAmenities table in given database...");

      // used for noun phrase word cloud
      String npReviews = "CREATE TABLE IF NOT EXISTS npReviews "
        + "(nounPhrase VARCHAR(100) NOT NULL)";
      stmt.executeUpdate(npReviews);
      System.out.println("Created npReviews table in given database...");

      // drop tables rentersreviewlistings, rentersreviewhosts, listingshaveamenities, hostsreviewrenters,  hoststolistings, locations, amenities, addresses, availabilities, booked, renter, host, user, listings;
      

      Scanner myObj = new Scanner(System.in); // Create a Scanner object

      String exit = "-1";
      while (!exit.equals("0")) {
        if(loggedInUser == -1){
          System.out.println("------------------------------------------------------");
          System.out.println("Enter 0 to exit.");
          System.out.println("Enter 1 to sign up as a new user.");
          System.out.println("Enter 2 to log in based on SIN/password.");
          System.out.println("Enter 3 to view all listings.");
          System.out.println("Enter 4 to see all availabilities for a listing.");
          System.out.println("Enter 5 to search and filter listings."); // TODO Where in logged in view?
          System.out.println("Enter 6 to see reports."); // TODO Where in logged in view?
          System.out.println("------------------------------------------------------");
          exit = myObj.nextLine();

          if (exit.equals("1")) 
            UserDAO.addUser(conn, myObj);
          if (exit.equals("2")) 
            UserDAO.login(conn, myObj);
          if (exit.equals("3")) 
            ListingDAO.viewAllListings(conn);
          if (exit.equals("4")) 
            AvailabilityDriver.getAvailabilities(conn, myObj);
          if (exit.equals("5")) 
            Search.searchListings(conn, myObj);
          if (exit.equals("6")) 
            ReportsDriver.viewAllReports(conn, myObj);
        }


        else{
          //Logged-in view, both
          System.out.println("------------------------------------------------------");
          System.out.println("Enter 0 to exit.");
          System.out.println("Enter 1 to log out.");
          System.out.println("Enter 2 to delete your account.");

          if(UserDAO.verifyUserInTable(conn, loggedInUser, "hostSIN", "Host")){
            System.out.println("Enter 3 to view all of your listings.");
            System.out.println("Enter 4 to see all availabilities for a listing.");
            System.out.println("Enter 5 to add a listing.");
            System.out.println("Enter 6 to delete a listing.");
            System.out.println("Enter 7 to modify availabilities for a listing.");
            System.out.println("Enter 8 to cancel a booking.");
            System.out.println("Enter 9 to see all your booked listings.");
            System.out.println("Enter 10 to review a renter.");
            //TODO: Move this to workflow when they're creating a listing
            System.out.println("Enter 11 to get suggested amenities.");
            System.out.println("------------------------------------------------------");
            exit = myObj.nextLine();  
            //only show a host's own listings  
            if (exit.equals("3")) ListingDAO.viewAllListingsByHost(conn);
            if (exit.equals("4")) AvailabilityDriver.getAvailabilities(conn, myObj);
            if (exit.equals("5")) 
              ListingDAO.addListing(conn, myObj);
            
            if (exit.equals("6")) 
              ListingDAO.deleteListing(conn, myObj);
            
            if (exit.equals("7")) 
              AvailabilityDriver.modifyAvailabilities(conn, myObj);
            
            if (exit.equals("8")) 
              BookingsDriver.hostCancelsBooking(conn, myObj);
            
            if (exit.equals("9")) 
              BookingsDAO.getAllBookingsForHost(conn);
            
            if (exit.equals("10")) 
              UserDAO.hostReviewsRenter(conn, myObj);
            if (exit.equals("11")) 
              HostToolkit.suggestedAmenities(conn);
          }

          else{
            System.out.println("Enter 3 to view all listings.");
            System.out.println("Enter 4 to see all availabilities for a listing.");
            System.out.println("Enter 5 to book a listing.");
            System.out.println("Enter 6 to cancel a booking.");
            System.out.println("Enter 7 to review a host.");
            System.out.println("Enter 8 to review a listing.");
            System.out.println("Enter 9 to see all your bookings."); 
            System.out.println("Enter 10 to get all listings between two dates");
            System.out.println("------------------------------------------------------");
            exit = myObj.nextLine(); 
            if (exit.equals("3")) ListingDAO.viewAllListings(conn);
            if (exit.equals("4")) AvailabilityDriver.getAvailabilities(conn, myObj);
            if (exit.equals("5")) 
              BookingsDriver.addBooking(conn, myObj);

            if (exit.equals("6")) 
              BookingsDriver.userCancelsBooking(conn, myObj);

            if (exit.equals("7")) 
              UserDAO.renterReviewsHost(conn, myObj);
            
            if (exit.equals("8")) 
              UserDAO.rentersReviewListings(conn, myObj);
            
            if (exit.equals("9")) 
              BookingsDAO.getAllBookingsForRenter(conn);

            if (exit.equals("10")) 
              ListingDAO.getListingsAvailableBetweenDates(conn, myObj);
          }
          if (exit.equals("1")) UserDAO.logout();
          if (exit.equals("2")) UserDAO.deleteUser(conn, myObj);

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

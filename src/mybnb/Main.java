package mybnb;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.ParseException;
import java.util.Scanner;
// import io.github.cdimascio.dotenv.Dotenv;

public class Main {

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

    try {
      // Establish connection
      Connection conn = DriverManager.getConnection(CONNECTION, USER, PASS);
      Statement stmt = conn.createStatement();

      //create all tables
      DDL ddl = new DDL(conn, stmt);
      ddl.createTables();

      // drop tables rentersreviewlistings, rentersreviewhosts, listingshaveamenities, hostsreviewrenters,  hoststolistings, locations, amenities, addresses, availabilities, booked, renter, host, user, listings;
      Scanner myObj = new Scanner(System.in);

      String exit = "-1";
      while (!exit.equals("0")) {
        if(loggedInUser == -1){
          System.out.println("----------------------- MyBnB ------------------------");
          System.out.println("0 - Exit");
          System.out.println("1 - Sign Up ");
          System.out.println("2 - Log In");
          System.out.println("3 - View, Search, and Filter Listings");
          // System.out.println("Enter 4 to see all availabilities for a listing.");
          // System.out.println("Enter 5 to search and filter listings."); // TODO Where in logged in view?
          System.out.println("4 - View reports"); // TODO Where in logged in view?
          System.out.println("------------------------------------------------------");
          exit = myObj.next();

          if (exit.equals("1")) 
            UserDAO.addUser(conn, myObj);
          if (exit.equals("2")) 
            UserDAO.login(conn, myObj);
          if (exit.equals("3")) 
            ListingDAO.viewAllListings(conn);
          if (exit.equals("4")) 
          //   AvailabilityDriver.getAvailabilities(conn, myObj);
          // if (exit.equals("5")) 
          //   Search.searchListings(conn, myObj);
          // if (exit.equals("6")) 
            ReportsDriver.viewAllReports(conn, myObj);
        }


        else{
          //Logged-in view, both
          System.out.println("----------------------- MyBnB -----------------------");
          System.out.println("0 - Exit");
          System.out.println("1 - Log Out");
          System.out.println("2 - Delete Your Account");

          //logged-in host
          if(UserDAO.verifyUserInTable(conn, loggedInUser, "hostSIN", "Host")){
            //Submenu to add, delete, or modify availabilities for a listing
            System.out.println("3 - View and Manage Your Listings"); // this includes modifying availabilities, adding and deleting listings
            System.out.println("4 - View and Manage Your Bookings");
            // System.out.println("Enter 10 to review a renter.");
            // //TODO: Move this to workflow when they're creating a listing
            // System.out.println("Enter 11 to get suggested amenities.");
            System.out.println("------------------------------------------------------");
            exit = myObj.next();  
            //only show a host's own listings  
            if (exit.equals("3")) MenuDriver.hostListingMenu(conn, myObj);
            if (exit.equals("4")) MenuDriver.hostBookingMenu(conn, myObj);
          }

          else{
            System.out.println("3 - View, Search, and Filter Listings");
            System.out.println("Enter 4 to see all availabilities for a listing.");
            System.out.println("Enter 5 to book a listing.");
            System.out.println("Enter 6 to cancel a booking.");
            System.out.println("Enter 7 to review a host.");
            System.out.println("Enter 8 to review a listing.");
            System.out.println("Enter 9 to see all your bookings."); 
            System.out.println("Enter 10 to get all listings between two dates");
            System.out.println("------------------------------------------------------");
            exit = myObj.next(); 
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
            
            // if (exit.equals("9")) 
            //   BookingsDAO.getAllBookingsForRenter(conn);

            // if (exit.equals("10")) 
            //   ListingDAO.getListingsAvailableBetweenDates(conn, myObj);
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

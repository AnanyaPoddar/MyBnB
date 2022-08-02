package mybnb;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Scanner;

public class ListingDAO {    


  //add listing, associated with specific logged-in host
  public static void addListing(Connection conn, Scanner myObj) {

    //TODO: Check that user is logged in and that logged in user is a host, or better just show different options for host/user/logged in at beginning
    System.out.println("Enter the type of your listing. 1 = House, 2 = Guesthouse, 3 = Apartment, 4 = Hotel");
    // System.out.println("Are you renting out the entire place? 0 = No, 1 = Yes");
    int typeInput = Integer.parseInt(myObj.nextLine());
    String type = null;
    if (typeInput == 1) 
      type = "house";
    else if (typeInput == 2) 
      type = "guesthouse";
    else if (typeInput == 3) 
      type = "apartment";
    else if (typeInput == 4) 
      type = "hotel";
    //TODO: Handle, stop reading
    else{
      System.out.println("Invalid type");
    }
    System.out.println("Enter the price of your listing in CAD");
    float price = Float.parseFloat(myObj.nextLine());
    //TODO: Handle if it can't be parsed as an int
    try {
      Statement statement = conn.createStatement();
      String listingInsert =
      String.format(
        "INSERT INTO Listings(type, price) VALUES ('%s', %f);", type, price);
            statement.executeUpdate(listingInsert);
        String getListID = "SELECT LAST_INSERT_ID() as listID;";
        ResultSet rs = statement.executeQuery(getListID);
        if(rs.next()) {
            int listID = rs.getInt("listID");
            String hostsToListingsInsert = String.format(
            "INSERT INTO HostsToListings VALUES (%d, %d);", listID, DAO.loggedInUser);
            statement.executeUpdate(hostsToListingsInsert);
            //After listing is added, prompt user to add availabilities for that listing
            AvailabilityDAO.addAvailabilities(conn, listID, myObj);
        }
    } catch (SQLException e) {
      e.printStackTrace();
    }
  }

  public static void viewAllListings(Connection conn, Scanner myObj) {

    try {
      Statement stmt = conn.createStatement();
      String sql = "SELECT * FROM Listings;";
      ResultSet rs = stmt.executeQuery(sql);

      // Extract results
      while (rs.next()) {
        // Retrieve by column name
        int listID = rs.getInt("listID");
        float price = rs.getFloat("price");
        String type = rs.getString("type");

        // Display values
        System.out.print("ID: " + listID);
        System.out.print(", price: $" + price);
        System.out.println(", type: " + type);
      }
      rs.close();
    } catch (SQLException e) {
      e.printStackTrace();
    }
  }

  public static void deleteListing(Connection conn, Scanner myObj){
    System.out.println("Enter the id of the listing you'd like to delete.");
    int listingID = Integer.parseInt(myObj.nextLine());
    //TODO: Can remove this check once we show different menu options based on logged-in, host, etc
    if(DAO.loggedInUser == -1){
      System.out.println("Must be logged in to delete a listing");
      return;
    }
    try {
      Statement statement = conn.createStatement();
      //check if listing belongs to host
      String checkHost = String.format("SELECT hostSIN from HostsToListings WHERE listID = %d", listingID);
      ResultSet rs = statement.executeQuery(checkHost);
      if(rs.next()){
        int hostSIN = rs.getInt("hostSIN");
        if(hostSIN != DAO.loggedInUser){
          System.out.println("Only the host of the listing can delete it");
          return;
        }
      }
      //TODO: The else clause below could technically be moved up here since there should never be something in hostsToListings that isn't in listing
      
      //Provide feedback if successfully deleted or not, although it should always be the case since the previous shows
      //it's in hostsToListings table
      String deleteListing = String.format("DELETE FROM Listings WHERE listID = %d", listingID);
      int rows = statement.executeUpdate(deleteListing);
      if(rows > 0)
        System.out.println("Successfully deleted listing with listID " + listingID);
      else
        System.out.println("No listing found with listID " + listingID);
    } catch (SQLException e) {
      e.printStackTrace();
    }
  }

}

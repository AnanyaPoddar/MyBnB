package mybnb;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;

public class ListingDAO {    


  //add listing, associated with specific logged-in host
  public static void addListing(Connection conn, Scanner myObj) {

    if(!UserDAO.verifyUserInTable(conn, DAO.loggedInUser, "hostSIN","Host")){
        System.out.println("You must be logged in as a host.");
        return;
    }

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


  //the listing must be available for the entire duration, not just some dates in between
  public static void getListingsAvailableBetweenDates(Connection conn, Scanner myObj){
    //from availabilities table, get all the 

    System.out.println("Start date of range: ");
    String start = myObj.nextLine();
    
    System.out.println("End date of range: ");
    String end = myObj.nextLine();
    //TODO: try-catch here
    LocalDate startDate = LocalDate.parse(start);
    LocalDate endDate = LocalDate.parse(end);

    if(!AvailabilityDAO.checkValidDates(startDate, endDate)) return;

    //get all dates in range
    List<LocalDate> dates = startDate.datesUntil(endDate.plusDays(1)).collect(Collectors.toList());
    String stringDates = "(";
    for(int i = 0; i < dates.size(); i++){
      if(i==dates.size()-1) stringDates += String.format("'%s'", dates.get(i));
      else stringDates += String.format("'%s',", dates.get(i));
    }
    stringDates  += ")";
    
    //get the count of the dates for each listId that appear in the dateRange above;
    //then only return the ones that appear the number of times equivalent to the length of dates (ie includes all the dates), and join with listings
    String getListings = String.format("SELECT listings.listID, listings.price, listings.type " +
    "FROM (SELECT count(date) AS dateCount, listID FROM availabilities WHERE date in %s GROUP BY listID) AS a " +
    "JOIN listings ON listings.listID=a.listID WHERE a.dateCount = %d", stringDates, dates.size());
    System.out.println(getListings);
    try {
      Statement statement = conn.createStatement();
      ResultSet rs = statement.executeQuery(getListings);
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

    } catch (SQLException e) {
      e.printStackTrace();
    } 
  }

}

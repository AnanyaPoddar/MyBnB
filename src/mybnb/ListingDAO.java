package mybnb;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;

public class ListingDAO {    

  //Helper
  public static List<Integer> getAllListingsByHost(Connection conn) throws SQLException {
    List<Integer> listings = new ArrayList<>();
    Statement stmt = conn.createStatement();
    String sql =  String.format("SELECT  listID from HostsToListings WHERE hostSIN=%d;", DAO.loggedInUser);
    ResultSet rs = stmt.executeQuery(sql);

    // Extract results
    while (rs.next()) {
      // Retrieve by column name
      int listID = rs.getInt("listID");
      listings.add(listID);
    }
    rs.close();
    return listings;
  }

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
      return;
    }
    try {
      Statement statement = conn.createStatement();
      String listingInsert =
      String.format(
        "INSERT INTO Listings(type) VALUES ('%s');", type);
            statement.executeUpdate(listingInsert);
        String getListID = "SELECT LAST_INSERT_ID() as listID;";
        ResultSet rs = statement.executeQuery(getListID);
        if(rs.next()) {
            int listID = rs.getInt("listID");
            String hostsToListingsInsert = String.format(
            "INSERT INTO HostsToListings VALUES (%d, %d);", listID, DAO.loggedInUser);
            statement.executeUpdate(hostsToListingsInsert);

            // Ask user for latitude and longitude (should be able to input 10, -10, -10.1, -10.1)
            System.out.println("Enter the latitude of your listing (-90 to 90)");
            float latitude = Float.parseFloat(myObj.nextLine());
            if(latitude > 90 || latitude < -90){
              System.out.println("Latitude must be in a -90 to 90 range.");
              return; // TODO Should it just return when error?
            }

            System.out.println("Enter the longitude of your listing (-180 to 180)");
            float longitude = Float.parseFloat(myObj.nextLine());
            if(longitude > 180 || longitude < -180){
              System.out.println("Longitude must be in a -180 to 180 range.");
              return; // TODO Should it just return when error?
            }

            String locationInsert = String.format(
                "INSERT INTO Locations VALUES (%d, %f, %f);", listID, latitude, longitude);
            statement.executeUpdate(locationInsert);

            // Ask user for address + attributes
            int unitNum = 0;
            if(typeInput == 3 || typeInput == 4){
              System.out.println("Provide the listing's unit number.");
              unitNum = Integer.parseInt(myObj.nextLine()); 
            }

            System.out.println("Provide the listing's street name.");
            String street = myObj.nextLine();
            System.out.println("Provide the listing's city.");
            String city = myObj.nextLine();
            System.out.println("Provide the listing's country.");
            String country = myObj.nextLine();
            System.out.println("Provide the listing's postal code in the following format: L#L #L#.");
            String postal = myObj.nextLine();
            // TODO Verify postal code's properly formatted 0, 2, 4 is alpha, 1, 3, 5 is num
            if(postal.length() != 7 || !Character.isLetter(postal.charAt(0)) || !Character.isLetter(postal.charAt(2)) || !Character.isLetter(postal.charAt(5)) || 
            !Character.isDigit(postal.charAt(1)) || !Character.isDigit(postal.charAt(4)) || !Character.isDigit(postal.charAt(6)) || postal.charAt(3) != ' '){
              System.out.println("Incorrect postal code format");
              return;
            }

            String addressInsert = String.format(
                "INSERT INTO ADDRESSES VALUES (%d, %d, '%s', '%s', '%s', '%s');", listID, unitNum, street, city, country, postal);
            statement.executeUpdate(addressInsert);

            // Choose amenities
            System.out.println("Choose amenities. Enter 0 to exit.");
            System.out.println("Essentials: 1 = Wifi, 2 = Kitchen, 3 = Washer");
            System.out.println("Features: 11 = Pool, 12 = Free Parking");
            System.out.println("Location: 21 = Beachfront, 22 = Waterfront");
            System.out.println("Safety: 23 = Smoke alarm, 24 = CO Alarm");
            int choice = Integer.parseInt(myObj.nextLine());

            Boolean[] amenities = new Boolean[25];
            for (int i = 0; i < 25; i++) {
              amenities[i] = false;
            }
            while(choice != 0){
              if(choice > 25 || choice < 0){
                System.out.println("Must choose between 1 - 24");
              }
              else{
                amenities[choice] = true;
              }
              choice = Integer.parseInt(myObj.nextLine());
            }
            addAmenities(conn, amenities, listID);

            //After listing is added, prompt user to add availabilities for that listing
            AvailabilityDriver.addAvailabilities(conn, listID, myObj);
        }
    } catch (SQLException e) {
      e.printStackTrace();
    }
  }

  public static void viewAllListings(Connection conn) {
    // TODO Does it need to show their corresponding address/location? 

    try {
      Statement stmt = conn.createStatement();
      String sql = "SELECT * FROM Listings;";
      ResultSet rs = stmt.executeQuery(sql);

      // Extract results
      while (rs.next()) {
        // Retrieve by column name
        int listID = rs.getInt("listID");
        String type = rs.getString("type");

        // Display values
        System.out.print("ID: " + listID);
        System.out.println(", type: " + type);
      }
      rs.close();
    } catch (SQLException e) {
      e.printStackTrace();
    }
  }


  public static void viewAllListingsByHost(Connection conn) {
    try {
      Statement stmt = conn.createStatement();
      String sql =  String.format("SELECT listings.listID, type FROM HostsToListings JOIN Listings ON Listings.listID = HostsToListings.listID WHERE hostSIN = %d;", DAO.loggedInUser);
      ResultSet rs = stmt.executeQuery(sql);

      // Extract results
      while (rs.next()) {
        // Retrieve by column name
        int listID = rs.getInt("listID");
        String type = rs.getString("type");

        // Display values
        System.out.print("ID: " + listID);
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

  public static void addAmenities(Connection conn, Boolean[] choices, int listID){
    // TODO Later on, there won't be any more null insertions if 1-24 all have amenities assigned to them
    String[] names = new String[25];
    // for (int i = 0; i < 25; i++) {
    //   names[i] = "";
    // }
    names[1] = "Wifi";
    names[2] = "Kitchen";
    names[3] = "Washer";
    names[11] = "Pool";
    names[12] = "Free Parking";
    names[21] = "Beachfront";
    names[22] = "Waterfront";
    names[23] = "Smoke Alarm";
    names[24] = "Carbon Monoxide Alarm";
    // TODO Are the other indexes "" or something else?
    
    for(int i = 1; i <= 24; i++){
      if(choices[i] == true){
        try {
          // is it in Amenities
          Statement stmt = conn.createStatement();
          String sql = 
          String.format(
            "SELECT * FROM AMENITIES WHERE name = '%s';", names[i]);
          System.out.println(sql);
          ResultSet rs = stmt.executeQuery(sql);
          // if not, add (name, type to amenities)
          if (!rs.next()) {
            String type;
            if(i == 23 || i == 24){
              type = "Safety";
            }
            else if(i == 21 || i == 22){
              type = "Location";
            }
            else if(i > 10){
              type = "Features";
            }
            else{
              type = "Essentials";
            }
            String amenityInsert = String.format(
              "INSERT INTO AMENITIES VALUES ('%s', '%s');", names[i], type);
            stmt.executeUpdate(amenityInsert);
            
          }
          // add (listID, name) to ListingsHaveAmenities
          String listingAmenityInsert = String.format(
            "INSERT INTO ListingsHaveAmenities VALUES (%d, '%s');", listID, names[i]);
          stmt.executeUpdate(listingAmenityInsert);
        } 
        catch (SQLException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
        }
      }

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
    String getListings = String.format("SELECT listings.listID, listings.type " +
    "FROM (SELECT count(date) AS dateCount, listID FROM availabilities WHERE status='available' AND date in %s GROUP BY listID) AS a " +
    "JOIN listings ON listings.listID=a.listID WHERE a.dateCount = %d", stringDates, dates.size());
    try {
      Statement statement = conn.createStatement();
      ResultSet rs = statement.executeQuery(getListings);
      // Extract results
      while (rs.next()) {
        // Retrieve by column name
        int listID = rs.getInt("listID");
        String type = rs.getString("type");

        // Display values
        System.out.print("ID: " + listID);
        System.out.println(", type: " + type);
      }

    } catch (SQLException e) {
      e.printStackTrace();
    } 
  }

}


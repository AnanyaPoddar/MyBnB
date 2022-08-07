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
    String sql =  String.format("SELECT listID from HostsToListings WHERE hostSIN=%d;", Main.loggedInUser);
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
    System.out.println("Enter the type of your listing. 1 = House, 2 = Guesthouse, 3 = Apartment, 4 = Hotel ");
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
            "INSERT INTO HostsToListings VALUES (%d, %d);", listID, Main.loggedInUser);
            // statement.executeUpdate(hostsToListingsInsert);

            // Ask user for latitude and longitude (should be able to input 10, -10, -10.1, -10.1)
            System.out.println("Enter the latitude of your listing (-90 to 90) ");
            float latitude = Float.parseFloat(myObj.nextLine());
            if(latitude > 90 || latitude < -90){
              System.out.println("Latitude must be in a -90 to 90 range.");
              return; // TODO Should it just return when error?
            }

            System.out.println("Enter the longitude of your listing (-180 to 180) ");
            float longitude = Float.parseFloat(myObj.nextLine());
            if(longitude > 180 || longitude < -180){
              System.out.println("Longitude must be in a -180 to 180 range.");
              return; // TODO Should it just return when error?
            }

            String locationInsert = String.format(
                "INSERT INTO Locations VALUES (%d, %f, %f);", listID, latitude, longitude);

            // Ask user for address + attributes
            int unitNum = 0;
            if(typeInput == 3 || typeInput == 4){
              System.out.println("Provide the listing's unit number: ");
              unitNum = Integer.parseInt(myObj.nextLine()); 
            }

            System.out.println("Provide the listing's street name: ");
            String street = myObj.nextLine();
            System.out.println("Provide the listing's city: ");
            String city = myObj.nextLine();
            System.out.println("Provide the listing's country: ");
            String country = myObj.nextLine();
            System.out.println("Provide the listing's postal code: ");
            String postal = myObj.nextLine();
            // Different countries have different postal codes
            if(postal.length() > 10 ) {
              System.out.println("Postal code too long.");
              return;
            }

            String addressInsert = String.format(
                "INSERT INTO ADDRESSES VALUES (%d, %d, '%s', '%s', '%s', '%s');", listID, unitNum, street, city, country, postal);

            System.out.println();
            // Suggest amenities based on popularity
            HostToolkit.suggestAmenities(conn);
            
            // Choose amenities
            System.out.println("\nChoose amenities. Enter 0 to exit.");
            System.out.println("Essentials: 1 = Wifi, 2 = Kitchen");
            System.out.println("Features: 3 = Pool, 4 = Free Parking");
            System.out.println("Safety: 5 = Smoke Alarm, 6 = Carbon Monoxide Alarm");
            int choice = Integer.parseInt(myObj.nextLine());

            Boolean[] amenities = new Boolean[7];
            for (int i = 0; i < 7; i++) {
              amenities[i] = false;
            }
            while(choice != 0){
              if(choice > 6 || choice < 0){
                System.out.println("Must choose between 1 - 6");
              }
              else{
                amenities[choice] = true;
              }
              choice = Integer.parseInt(myObj.nextLine());
            }

            String[] names = new String[7];
            names[1] = "Wifi";
            names[2] = "Kitchen";
            names[3] = "Pool";
            names[4] = "Free Parking";
            names[5] = "Smoke Alarm";
            names[6] = "Carbon Monoxide Alarm";

            List<String> strAmenities = new ArrayList<>();
            List<String> unchosenAmenities = new ArrayList<>();
            for(int i = 0; i < amenities.length; i++){
              if(amenities[i]){
                strAmenities.add(names[i]);
              }
              else unchosenAmenities.add(names[i]);
            }

            //add the amenities to db
            addAmenities(conn, amenities, listID);
            //Suggest price for the listing
            Float suggestedPrice;
            if(strAmenities.size() == 0) suggestedPrice = HostToolkit.suggestPrice(conn, type, country, city, street, postal);
            else suggestedPrice = HostToolkit.suggestPrice(conn, type, country, city, street, postal, strAmenities );

            statement.executeUpdate(hostsToListingsInsert);
            statement.executeUpdate(locationInsert);
            statement.executeUpdate(addressInsert);

            //TODO: provide suggestions about amenities and expected revenue increase\
            HostToolkit.suggestAmenitiesAndPrice(conn, unchosenAmenities, strAmenities, suggestedPrice, type, country, city, street, postal);

            //After listing is added, prompt user to add availabilities for that listing
            AvailabilityDriver.addAvailabilities(conn, listID, myObj);
        }
    } catch (SQLException e) {
      e.printStackTrace();
    }
  }

  public static void viewAllListings(Connection conn) {
    // Shows corresponding address (not location as that isn't necessarily relevant to the user)
    try {
      Statement stmt = conn.createStatement();
      String sql = "SELECT a.listID, type, unitNum, street, city, country, postal FROM listings AS l JOIN addresses AS a ON a.listID=l.listID;";
      ResultSet rs = stmt.executeQuery(sql);

      // Extract results
      while (rs.next()) {
        // Retrieve by column name
        int listID = rs.getInt("listID");
        String type = rs.getString("type");
        String street = rs.getString("street");
        String city = rs.getString("city");
        String country = rs.getString("country");
        String postal = rs.getString("postal");
        int unitNum = rs.getInt("unitNum");
        // Display values
        System.out.print("ID: " + listID);
        System.out.println(", type: " + type);
        System.out.println("Address: " + street + ", " + (unitNum != 0 ? "unit " + unitNum + ", " : "") + city + ", " + country + ", " + postal + "\n");

      }
      rs.close();
    } catch (SQLException e) {
      e.printStackTrace();
    }
  }


  public static void viewAllListingsByHost(Connection conn) {
    try {
      Statement stmt = conn.createStatement();
      String sql =  String.format("SELECT l.listID, type, street, city, country, postal, unitNum FROM Listings as l "+
      "JOIN addresses AS a ON l.listID = a.listID JOIN HostsToListings AS h ON h.listID = a.listID WHERE hostSIN = %d", Main.loggedInUser);
      ResultSet rs = stmt.executeQuery(sql);

      // Extract results
      while (rs.next()) {
        // Retrieve by column name
        int listID = rs.getInt("listID");
        String type = rs.getString("type");
        String street = rs.getString("street");
        String city = rs.getString("city");
        String country = rs.getString("country");
        String postal = rs.getString("postal");
        int unitNum = rs.getInt("unitNum");

        // Display values
        System.out.print("ID: " + listID);
        System.out.println(", type: " + type);
        System.out.println("Address: " + street + ", " + (unitNum != 0 ? "unit " + unitNum + ", " : "") + city + ", " + country + ", " + postal + "\n");
      }
      rs.close();
    } catch (SQLException e) {
      e.printStackTrace();
    }
  }

  public static void deleteListing(Connection conn, Scanner myObj, int listingID){
    try {
      Statement statement = conn.createStatement();
      //check if listing belongs to host
      String checkHost = String.format("SELECT hostSIN from HostsToListings WHERE listID = %d", listingID);
      ResultSet rs = statement.executeQuery(checkHost);
      if(rs.next()){
        int hostSIN = rs.getInt("hostSIN");
        if(hostSIN != Main.loggedInUser){
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
    String[] names = new String[7];
    names[1] = "Wifi";
    names[2] = "Kitchen";
    names[3] = "Pool";
    names[4] = "Free Parking";
    names[5] = "Smoke Alarm";
    names[6] = "Carbon Monoxide Alarm";
    
    for(int i = 1; i <= 6; i++){
      if(choices[i] == true){
        try {
          // is it in Amenities
          Statement stmt = conn.createStatement();
          String sql = 
          String.format(
            "SELECT * FROM AMENITIES WHERE name = '%s';", names[i]);
          ResultSet rs = stmt.executeQuery(sql);
          // if not, add (name, type to amenities)
          if (!rs.next()) {
            String type;
            if(i == 5 || i == 6){
              type = "Safety";
            }
            else if(i == 3 || i == 4){
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
    System.out.println("We only return listings that are available for the whole range.");
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


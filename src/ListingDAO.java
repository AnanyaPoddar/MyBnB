import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Scanner;
public class ListingDAO {
    
  public static void addListing(Connection conn, int loggedInUser, Scanner myObj) {
    //TODO: Check that user is logged in and that logged in user is a host
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
      Statement insert = conn.createStatement();
      String listingInsert =
      String.format(
        "INSERT INTO Listings(type, price) VALUES ('%s', %f);", type, price);
            insert.executeUpdate(listingInsert);
        String getListID = "SELECT LAST_INSERT_ID() as listID;";
        ResultSet rs = insert.executeQuery(getListID);
        if(rs.next()) {
            int listID = rs.getInt("listID");
            String hostsToListingsInsert = String.format(
            "INSERT INTO HostsToListings VALUES (%d, %d);", listID, loggedInUser);
            insert.executeUpdate(hostsToListingsInsert);
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
        String status = rs.getString("status");

        // Display values
        System.out.print("ID: " + listID);
        System.out.println(", price: " + price);
        System.out.println(", status: " + status);
      }
      rs.close();
    } catch (SQLException e) {
      e.printStackTrace();
    }

  }

}

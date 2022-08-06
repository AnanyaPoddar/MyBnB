package mybnb;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;
public class AvailabilityDAO{

  //Helper function
  public static boolean checkValidDates(LocalDate startDate, LocalDate endDate){
    //startDate must be after endDate
    if(startDate.isAfter(endDate)){
      System.out.println("Not a valid date range. Start date must be before end date. Exiting....\n");
      return false;
    }
    //if startDate is before current date
    else if(startDate.isBefore(LocalDate.now())){
      System.out.println("Not a valid date range. Start date must be after the current date\n");
      return false;
    }
    return true;
  }

  //Helper function, returns -1 if listing not available on all the dates mentioned, otherwise returns the total price
  public static float getAvailabilityPriceOnDates(Connection conn, LocalDate startDate, LocalDate endDate, int listingID) throws SQLException{
    //first check that the start and end date are valid
    if(!checkValidDates(startDate, endDate)) return -1;
    
    List<LocalDate> dates = startDate.datesUntil(endDate.plusDays(1))
    .collect(Collectors.toList());
    Statement statement = conn.createStatement();
    String stringDates = "(";
    for(int i = 0; i < dates.size(); i++){
      if(i==dates.size()-1) stringDates += String.format("'%s'", dates.get(i));
      else stringDates += String.format("'%s',", dates.get(i));
    }
    stringDates  += ")";

    String checkDates = String.format("SELECT count(date) = %d  as isBookable, sum(price) as cost " + 
    "FROM Availabilities WHERE listID = %d AND date in %s AND status = 'available';", dates.size(), listingID, stringDates);
    ResultSet rs = statement.executeQuery(checkDates);
    if(rs.next()){
      if(rs.getInt("isBookable") == 0){
        System.out.println("At least one date in the range you have entered is not available for booking. Exiting....");
        return -1;
      }
      else return rs.getFloat("cost");
    }
    //CHECK: It should never get here, checkDates should always return something
    return -1;
  }

  //Helper, checks that a listing belongs to host
  public static boolean hostsListing(Connection conn, int listingID) throws SQLException{
    Statement statement = conn.createStatement();
    String checkHost = String.format("SELECT hostSIN from HostsToListings WHERE listID = %d;", listingID);
    ResultSet rs = statement.executeQuery(checkHost);
    //if not in hostsToListings table
    if(!rs.next()){
      System.out.println("Only the host of the listing has this permission.");
      return false;
    }
    else{
      int hostSIN = rs.getInt("hostSIN");
      if(hostSIN != Main.loggedInUser){
        System.out.println("Only the host of the listing has this permission.");
        return false;
      }
      return true;
    }
    
  }

  //only returns AVAILABLE availabilities
  public static void getAvailabilities(Connection conn, int listingID, Scanner myObj){
    //set availabilities to past first by checking against current date, then return the ones that are available from current date and onward
    setPastAvailabilities(conn, listingID);
    try {
      Statement statement = conn.createStatement();
      String availabilities = String.format("SELECT date, price from Availabilities WHERE listID = %d AND status = 'available' ORDER BY date;", listingID);
      ResultSet rs = statement.executeQuery(availabilities);
      if(!rs.isBeforeFirst()) {
        System.out.println("No availabilities for this listing."); 
        return;
      }
      while(rs.next()){
        System.out.print("Date " + rs.getDate("date"));
        DecimalFormat df = new DecimalFormat("0.00");
        System.out.println(", Price $" + df.format(rs.getFloat("price")));
      }
    }catch (SQLException e) {
      e.printStackTrace();
    }
  }

  public static void addAvailabilities(Connection conn, int listingID, LocalDate startDate, LocalDate endDate, float price){
    //datesUntil enddate is exclusive, so add 1 day to include last date
    List<LocalDate> dates = startDate.datesUntil(endDate.plusDays(1)).collect(Collectors.toList());

    try {
      Statement statement = conn.createStatement();
      String availInsert = "INSERT INTO Availabilities(listID, date, price) VALUES ";
      for(int i = 0; i < dates.size(); i++){
        if(i == dates.size()-1)
          //TODO: Check, this should update the price if the key already exists
          availInsert += String.format("(%d, '%s', %f) ON DUPLICATE KEY UPDATE price = %f;", listingID, dates.get(i), price, price);
        else availInsert +=  String.format("(%d, '%s', %f), ", listingID, dates.get(i), price);
      }
      //updated so that it only executes the query once, rather than every time inside for loop
      statement.executeUpdate(availInsert);
      System.out.println("Success!");
    } catch (SQLException e) {
      //TODO: Because of check, it could fail if price is < 0, so maybe print that error here
        e.printStackTrace();
    }
  }

  //sets availabilities to past based on current date
  public static void setPastAvailabilities(Connection conn, int listingId){
    String past = String.format("UPDATE availabilities SET status = 'past' WHERE listID = %d AND date < '%s'", listingId, LocalDate.now());
    try {
      Statement statement = conn.createStatement();
      statement.executeUpdate(past);
    } catch (SQLException e) {
      e.printStackTrace();
    }
  }


  //set availability status to either 'available', 'booked', 'past', or 'cancelled'
  public static void setAvailability(Connection conn, int listingID, LocalDate startDate, LocalDate endDate, String status){
    List<LocalDate> dates = startDate.datesUntil(endDate.plusDays(1)).collect(Collectors.toList());

    try {
      Statement statement = conn.createStatement();
      String stringDates = "(";
      for(int i = 0; i < dates.size(); i++){
        if(i==dates.size()-1) stringDates += String.format("'%s'", dates.get(i));
        else stringDates += String.format("'%s',", dates.get(i));
      }
      stringDates  += ")";

      String availInsert = String.format("UPDATE Availabilities SET status = '%s' WHERE listID = %d AND date in %s;", status, listingID, stringDates);
      statement.executeUpdate(availInsert);
    } catch (SQLException e) {
      e.printStackTrace();
    }
  }

  //helper
  public static void deleteAvailabilities(int listingID, LocalDate startDate, LocalDate endDate){
    List<LocalDate> dates = startDate.datesUntil(endDate.plusDays(1)).collect(Collectors.toList());
    String stringDates = "(";
    for(int i = 0; i < dates.size(); i++){
      if(i==dates.size()-1) stringDates += String.format("'%s'", dates.get(i));
      else stringDates += String.format("'%s',", dates.get(i));
    }
    stringDates  += ")";

    String.format("DELETE FROM Availabilities WHERE listID = %d AND date IN %s", listingID, stringDates);
  }

}
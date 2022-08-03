package mybnb;
import java.sql.Connection;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;
public class AvailabilityDAO{

  //Helper function
  public static boolean checkValidDates(LocalDate startDate, LocalDate endDate){
    //startDate must be after endDate
    if(startDate.isAfter(endDate)){
      System.out.println("Not a valid date range. Start date must before end date. Exiting....\n");
      return false;
    }
    //if startDate is before current date
    else if(startDate.isBefore(LocalDate.now())){
      System.out.println("Not a valid date range. Start date must before the current date\n");
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
    "FROM Availabilities WHERE listID = %d AND date in %s AND isAvailable = 1;", dates.size(), listingID, stringDates);
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
      if(hostSIN != DAO.loggedInUser){
        System.out.println("Only the host of the listing has this permission.");
        return false;
      }
      return true;
    }
    
  }

  //this gets availabilities with isAvailable = 1
  private static void getAvailabilities(Connection conn, int listingID, Scanner myObj){
    try {
      Statement statement = conn.createStatement();
      String availabilities = String.format("SELECT date, price from Availabilities WHERE listID = %d AND isAvailable = 1;", listingID);
      ResultSet rs = statement.executeQuery(availabilities);
      //TODO: Maybe provide feedback to user if the listingID DNE or if no availabilities for it exist
      while(rs.next()){
        System.out.print("Date " + rs.getDate("date"));
        System.out.println(" Price " + rs.getFloat("price"));
      }
      System.out.println();
    }catch (SQLException e) {
      e.printStackTrace();
    }
  }

  public static void getAvailabilities(Connection conn, Scanner myObj){
    System.out.println("Enter the id of the listing you'd like to see availabilities for.");
    int listingID = Integer.parseInt(myObj.nextLine());
    getAvailabilities(conn, listingID, myObj);
  }


  public static void addAvailabilities(Connection conn, int listingID, LocalDate startDate, LocalDate endDate, float price){
    //datesUntil enddate is exclusive, so add 1 day to include last date
    List<LocalDate> dates = startDate.datesUntil(endDate.plusDays(1))
    .collect(Collectors.toList());

    try {
      Statement statement = conn.createStatement();
      String availInsert = "INSERT INTO Availabilities(listID, date, price) VALUES ";
      for(int i = 0; i < dates.size(); i++){
        if(i == dates.size()-1)
          //TODO: Check, this should update the price if the key already exists
          availInsert += String.format("(%d, '%s', %f) ON DUPLICATE KEY UPDATE price = %f;", listingID, dates.get(i), price, price);
        else availInsert +=  String.format("(%d, '%s', %f)", listingID, dates.get(i), price);
      }
      //updated so that it only executes the query once, rather than every time inside for loop
      statement.executeUpdate(availInsert);
      System.out.println("Success!");
    } catch (SQLException e) {
      //TODO: Because of check, it could fail if price is < 0, so maybe print that error here
        e.printStackTrace();
    }
  }

  public static void addAvailabilities(Connection conn, int listingID, Scanner myObj){
    System.out.println("Enter a range of availabilities for your listing in the YYYY-MM-DD format. Enter 0 to exit\n");
    String end = "-1";
    String start = "-1";
    while(!start.equals("0") && !end.equals("0")){
      System.out.println("Start date of range: ");
      start = myObj.nextLine();
      if(start.equals("0")) break;
      
      System.out.println("End date of range: ");
      end = myObj.nextLine();
      if(end.equals("0")) break;
      //TODO: try-catch here
      LocalDate startDate = LocalDate.parse(start);
      LocalDate endDate = LocalDate.parse(end);

      if(!checkValidDates(startDate, endDate)) return;

      System.out.println("Enter the price of this listing in CAD. This price is applied for each date within the given range.");
      float price = Float.parseFloat(myObj.nextLine());

      addAvailabilities(conn, listingID, startDate, endDate, price);
    }
  }


    //change isAvailable to 0/1 for isAvailable
    public static void setAvailability(Connection conn, int listingID, LocalDate startDate, LocalDate endDate, int isAvailable){
      List<LocalDate> dates = startDate.datesUntil(endDate.plusDays(1)).collect(Collectors.toList());
  
      try {
        Statement statement = conn.createStatement();
        String stringDates = "(";
        for(int i = 0; i < dates.size(); i++){
          if(i==dates.size()-1) stringDates += String.format("'%s'", dates.get(i));
          else stringDates += String.format("'%s',", dates.get(i));
        }
        stringDates  += ")";

          String availInsert = String.format(
            "UPDATE Availabilities SET isAvailable = %d WHERE listID = %d AND date in %s;", isAvailable, listingID, stringDates);
                statement.executeUpdate(availInsert);
      } catch (SQLException e) {
        e.printStackTrace();
      }
    }

  //Helper, called by modifyAvailabilities, which already checks for the correct host, so not required here
  private static void deleteAvailabilities(Connection conn, int listingID, Scanner myObj){
    System.out.println("Enter a range of availabilities to delete for your listing in the YYYY-MM-DD format. Enter 0 to exit\n");
    String end = "-1";
    String start = "-1";
    while(!start.equals("0") && !end.equals("0")){
      System.out.println("Start date of range: ");
      start = myObj.nextLine();
      if(start.equals("0")) break;
      
      System.out.println("End date of range: ");
      end = myObj.nextLine();
      if(end.equals("0")) break;

      //TODO: try-catch here
      LocalDate startDate = LocalDate.parse(start);
      LocalDate endDate = LocalDate.parse(end);

      if(!checkValidDates(startDate, endDate)) return;
      //TODO: Check that none of the dates are booked
      setAvailability(conn, listingID, startDate, endDate, 0);
    }
  }

  //This option is accessible through the menu, hence have to check that the listing belongs to the current logged in user
  public static void modifyAvailabilities(Connection conn, Scanner myObj){
    System.out.println("Enter the id of the listing you'd like to modify availabilities for.\n");
    int listingID = Integer.parseInt(myObj.nextLine());
    System.out.println(listingID);
    try {
      //Check that host of listing is the one attempting to modify availailibility
      if(!hostsListing(conn, listingID)) return;
      
      System.out.println("The current list of availabilities for this listing are as follows:\n");
      getAvailabilities(conn, listingID, myObj);

      System.out.println("Enter 1 to Add a range of availabilities or modify the price of a range of availabilities.");
      System.out.println("Enter 2 to Delete a range of availabilities\n");

      //TODO: Check that none of the dates are booked when deleting or modifying the price


      int choice = Integer.parseInt(myObj.nextLine());
      if(choice == 1){
        addAvailabilities(conn, listingID, myObj);
      }
      else if (choice == 2){
        deleteAvailabilities(conn, listingID, myObj);
      }

    } catch (SQLException e) {
      e.printStackTrace();
    }

  }
}
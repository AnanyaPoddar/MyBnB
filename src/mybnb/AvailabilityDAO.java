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


  //Helper function
  public static boolean checkListingAvailabilityOnDates(Connection conn, LocalDate startDate, LocalDate endDate, int listingID) throws SQLException{
    //first check that the start and end date are valid
    if(!checkValidDates(startDate, endDate)) return false;
    
    List<LocalDate> dates = startDate.datesUntil(endDate.plusDays(1))
    .collect(Collectors.toList());
    Statement statement = conn.createStatement();
    for(LocalDate date: dates){
      String checkDate = String.format("SELECT listID from Availabilities WHERE listID = %d AND date = '%s';", listingID, date);
      ResultSet rs = statement.executeQuery(checkDate);
      if(!rs.next()){
        System.out.println("At least one date in the range you have entered is not available for booking. Exiting....");
        return false;
      }
    }
    return true;
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

  //TODO: ....... FIXX
  private static void getAvailabilities(Connection conn, int listingID, Scanner myObj){
    try {
      Statement statement = conn.createStatement();
      String availabilities = String.format("SELECT date from Availabilities WHERE listID = %d;", listingID);
      ResultSet rs = statement.executeQuery(availabilities);
      //TODO: Maybe provide feedback to user if the listingID DNE or if no availabilities for it exist
      while(rs.next())
        System.out.println(rs.getDate("date"));
      System.out.println();
    }catch (SQLException e) {
      e.printStackTrace();
    }
  }

  //TODO: These aren't reusable by other functions because of the fact that it prompts within the function, that should be moved out....
  public static void getAvailabilities(Connection conn, Scanner myObj){
    System.out.println("Enter the id of the listing you'd like to see availabilities for.");
    int listingID = Integer.parseInt(myObj.nextLine());
    getAvailabilities(conn, listingID, myObj);
  }


  public static void addAvailabilities(Connection conn, int listingID, Scanner myObj, LocalDate startDate, LocalDate endDate){

  }

  // This option only shows up when someone adds a new listing (or modifies existing, which already checks for correct host), hence no need to check the logged-in user?
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

      else{
        //datesUntil enddate is exclusive, so add 1 day to include last date
        List<LocalDate> dates = startDate.datesUntil(endDate.plusDays(1))
        .collect(Collectors.toList());

        try {
          Statement statement = conn.createStatement();
          for(LocalDate date: dates){
            String availInsert = String.format(
              "INSERT INTO Availabilities(listID, date) VALUES (%d, '%s');", listingID, date);
                  statement.executeUpdate(availInsert);
          }
        } catch (SQLException e) {
          e.printStackTrace();
        }
      }
    }
  }

  //TODO: Same issue as before.... not reusable with the scanner stuff inside
  public static void deleteAvailabilities(Connection conn, int listingID, LocalDate startDate, LocalDate endDate){
    List<LocalDate> dates = startDate.datesUntil(endDate.plusDays(1)).collect(Collectors.toList());

    try {
      Statement statement = conn.createStatement();
      for(LocalDate date: dates){
        String availInsert = String.format(
          "DELETE FROM Availabilities WHERE listID = %d AND date = '%s';", listingID, date);
              statement.executeUpdate(availInsert);
      }
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

      else{
        deleteAvailabilities(conn, listingID, startDate, endDate);
      }
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

      System.out.println("Enter 1 to Add a range of availabilities");
      System.out.println("Enter 2 to Delete a range of availabilities\n");

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
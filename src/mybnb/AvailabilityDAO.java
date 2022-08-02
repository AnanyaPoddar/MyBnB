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

  public static void getAvailabilities(Connection conn, Scanner myObj){
    System.out.println("Enter the id of the listing you'd like to see availabilities for.");
    int listingID = Integer.parseInt(myObj.nextLine());
    try {
      Statement statement = conn.createStatement();
      String availabilities = String.format("SELECT date from Availabilities WHERE listID = %d", listingID);
      ResultSet rs = statement.executeQuery(availabilities);
      while(rs.next()){
        System.out.println(rs.getInt("date"));
      }
    }catch (SQLException e) {
      e.printStackTrace();
    }
  }

  // This option only shows up when someone adds a new listing, hence no need to check the logged-in user
  public static void addAvailabilities(Connection conn, int listingId, Scanner myObj){
    
    System.out.println("Enter a range of availabilities for your listing in the YYYY-MM-DD format. Enter 0 to exit");
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

      //startDate must be after endDate
      if(startDate.isAfter(endDate)){
        System.out.println("Not a valid date range. Start date must before end date. Exiting....");
        break;
      }
      //if startDate is before current date
      else if(startDate.isBefore(LocalDate.now())){
        System.out.println("Not a valid date range. Start date must before the current date");
      }

      else{
        //datesUntil enddate is exclusive, so add 1 day to include last date
        List<LocalDate> dates = startDate.datesUntil(endDate.plusDays(1))
        .collect(Collectors.toList());

        try {
          Statement statement = conn.createStatement();
          for(LocalDate date: dates){
            //convert from LocalDate to sql date
            Date.valueOf(date);
            String availInsert = String.format(
              "INSERT INTO Availabilities(listID, date) VALUES (%d, '%s');", listingId, date);
                  statement.executeUpdate(availInsert);
          }
        } catch (SQLException e) {
          e.printStackTrace();
        }
      }
    }
  }

  //This option is accessible through the menu, hence have to check that the listing belongs to the current logged in user
  public static void modifyAvailabilities(Connection conn, int loggedInUser, Scanner myObj){
    System.out.println("Enter the id of the listing you'd like to modify availabilities for.");
    int listingID = Integer.parseInt(myObj.nextLine());
    try {
      Statement statement = conn.createStatement();
      //Check that host of listing is the one attempting to modify availailibility
      //TODO: Move this out to its own function
      String checkHost = String.format("SELECT hostSIN from HostsToListings WHERE listID = %d", listingID);
      ResultSet rs = statement.executeQuery(checkHost);
      if(rs.next()){
        int hostSIN = rs.getInt("hostSIN");
        if(hostSIN != loggedInUser){
          System.out.println("Only the host of the listing can edit its availability");
          return;
        }
      }
      //TODO: Finish, figure out how to allow modification
    } catch (SQLException e) {
      e.printStackTrace();
    }

  }
}
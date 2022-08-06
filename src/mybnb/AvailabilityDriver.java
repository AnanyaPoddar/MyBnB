package mybnb;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;

public class AvailabilityDriver {
    
  public static void getAvailabilities(Connection conn, Scanner myObj){
    System.out.println("Enter the id of the listing you'd like to see availabilities for.");
    int listingID = Integer.parseInt(myObj.nextLine());
    AvailabilityDAO.getAvailabilities(conn, listingID, myObj);
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

      if(!AvailabilityDAO.checkValidDates(startDate, endDate)) return;
      System.out.println("Enter the price of this listing in CAD. This price is applied for each date within the given range: ");
      float price = Float.parseFloat(myObj.nextLine());
      AvailabilityDAO.addAvailabilities(conn, listingID, startDate, endDate, price);
    }
  }


  public static void modifyAvailabilities(Connection conn, Scanner myObj, int listingID){
    try {
      //Check that host of listing is the one attempting to modify availailibility
      if(!AvailabilityDAO.hostsListing(conn, listingID)) return;
      
      AvailabilityDAO.getAvailabilities(conn, listingID, myObj);

      System.out.println("Enter 1 to add/modify the price of a range of availabilities.");
      System.out.println("Enter 2 to delete a range of availabilities");

      int choice = Integer.parseInt(myObj.nextLine());
      //invalid choice 
      if(choice != 1 && choice !=2) return;
      
      System.out.println("Enter the range of dates in the YYYY-MM-DD format.");
      System.out.println("Start date of range: ");
      String start = myObj.nextLine();
      
      System.out.println("End date of range: ");
      String end = myObj.nextLine();
  
      //TODO: try-catch here
      LocalDate startDate = LocalDate.parse(start);
      LocalDate endDate = LocalDate.parse(end);

      if(!AvailabilityDAO.checkValidDates(startDate, endDate)) return;

      List<LocalDate> dates = startDate.datesUntil(endDate.plusDays(1)).collect(Collectors.toList());
      String stringDates = "(";
        for(int i = 0; i < dates.size(); i++){
          if(i==dates.size()-1) stringDates += String.format("'%s'", dates.get(i));
          else stringDates += String.format("'%s',", dates.get(i));
        }
      stringDates  += ")";

      //Check that none of the dates are booked when deleting or modifying the price
      String isBooked = String.format("SELECT count(date) > 0 as isBooked FROM Availabilities WHERE listID = %d AND status='booked' AND date IN %s;", listingID, stringDates);
      Statement statement = conn.createStatement();
      ResultSet rs = statement.executeQuery(isBooked);
      if(rs.next()){
        if(rs.getInt(isBooked) == 1){
          System.out.println("At least one of the dates in the range you specified is already booked.\n" +
          "Please cancel the booking first if you want to make changes to availability");
          return;
        }
        if(choice == 1){
          System.out.println("Enter the price of this listing in CAD. This price is applied for each date within the given range: ");
          float price = Float.parseFloat(myObj.next());
          System.out.println("1 - Modifying Availabilities...");
          AvailabilityDAO.addAvailabilities(conn, listingID, startDate, endDate, price);
        }
      }
      else if (choice == 2){
        System.out.println("2 - Deleting Availabilities...");
        //delete availabilities actually removes them from the table 
        AvailabilityDAO.deleteAvailabilities(listingID, startDate, endDate);
      }

    } catch (SQLException e) {
      e.printStackTrace();
    }

  }

}

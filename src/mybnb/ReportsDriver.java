package mybnb;

import java.sql.Connection;
import java.time.LocalDate;
import java.util.Scanner;

public class ReportsDriver {

    
    public static void viewAllReports(Connection conn, Scanner myObj){
        System.out.println("------------------------------------------------------");
        System.out.println("Enter 0 to exit.");
        System.out.println("1 - Reports About Number of Bookings");
        System.out.println("2 - Reports About Number of Listings");
        System.out.println("------------------------------------------------------");
        String choice = myObj.nextLine();
        if (choice.equals("1")) viewBookingReports(conn, myObj);
        if (choice.equals("2")) viewListingReports(conn, myObj);
    }

    private static void viewBookingReports(Connection conn,Scanner myObj){
        System.out.println("Enter a specific date range to filter bookings reports in the YYYY-MM-DD format.");
        System.out.println("Start date of range: ");
        String start = myObj.nextLine();
        
        System.out.println("End date of range: ");
        String end = myObj.nextLine();
    
        //TODO: try-catch here
        LocalDate startDate = LocalDate.parse(start);
        LocalDate endDate = LocalDate.parse(end);
        System.out.println("------------------------------------------------------");
        System.out.println("Number of Bookings By City:");
        ReportsDAO.numBookingsByDatesAndCity(conn, startDate, endDate);
        System.out.println("------------------------------------------------------");
        System.out.println("Renter(s) with Most Cancelled Bookings:");
        ReportsDAO.maxRenterCancellations(conn);
        System.out.println("------------------------------------------------------");
        System.out.println("Host(s) with Most Cancelled Bookings:");
        ReportsDAO.maxHostCancellations(conn);
        System.out.println("------------------------------------------------------");
    }
    
    private static void viewListingReports(Connection conn, Scanner myObj){
        System.out.println("------------------------------------------------------");
        System.out.println("Number of Listings by Country:");
        ReportsDAO.numListingsByCountry(conn);
        System.out.println("------------------------------------------------------");
    }
}

package mybnb;

import java.sql.Connection;
import java.time.LocalDate;
import java.util.Scanner;

public class ReportsDriver {

    
    public static void viewAllReports(Connection conn, Scanner myObj){
        System.out.println("---------------------- Reports -----------------------");
        System.out.println("Enter 0 to exit.");
        System.out.println("1 - Reports About Number of Bookings");
        System.out.println("2 - Reports About Number of Listings");
        System.out.println("3 - Reports About Number of Noun Phrases in a Listing's Reviews");
        String choice = myObj.nextLine();
        if (choice.equals("1")) viewBookingReports(conn, myObj);
        if (choice.equals("2")) viewListingReports(conn, myObj);
        if (choice.equals("3")) parser(conn, myObj);
    }

    private static void viewBookingReports(Connection conn,Scanner myObj){
        System.out.println("1 - Number of Bookings By City");
        System.out.println("1 - Number of Bookings By City");
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
        System.out.println("Ranking of Renters by booking:");
        ReportsDAO.rankRentersNumBookings(conn, startDate, endDate);
        System.out.println("------------------------------------------------------");
    }
    
    private static void viewListingReports(Connection conn, Scanner myObj){
        System.out.println("------------------------------------------------------");
        System.out.println("Number of Listings by Country:");
        ReportsDAO.numListingsByCountry(conn);
        System.out.println("------------------------------------------------------");
        System.out.println("Number of Listings by Country and City:");
        ReportsDAO.numListingsByCity(conn);
        System.out.println("------------------------------------------------------");
        System.out.println("Number of Listings by Country and City and Postal Code:");
        ReportsDAO.numListingsByPostalCode(conn);
        System.out.println("------------------------------------------------------");
        System.out.println("Rank Hosts By Listings Per Country:");
        ReportsDAO.rankHostsByListingsPerCountry(conn);
        System.out.println("------------------------------------------------------");
        System.out.println("Rank Hosts By Listings Per City:");
        ReportsDAO.rankHostsByListingsPerCity(conn);
        System.out.println("------------------------------------------------------");
        System.out.println("Possible Commercial Hosts Per Country:");
        System.out.println("These are hosts who have more than 10% of listings in a specific country.");
        ReportsDAO.possibleCommercialHostsByCountry(conn);
        System.out.println("------------------------------------------------------");
        System.out.println("Possible Commercial Hosts Per City:");
        System.out.println("These are hosts who have more than 10% of listings in a specific city.");
        ReportsDAO.possibleCommercialHostsByCountry(conn);
        System.out.println("------------------------------------------------------");
    }

    private static void parser(Connection conn, Scanner myObj){
        System.out.println("Start listID of the listing you would like: ");
        int listID = Integer.parseInt(myObj.nextLine());  
        System.out.println("------------------------------------------------------");
        System.out.println("Noun Phrases of listID: " + listID);
        NounParser.parser(conn, listID);
        System.out.println("------------------------------------------------------");


    }
}

package mybnb;

import java.sql.Connection;
import java.time.LocalDate;
import java.util.Scanner;

public class ReportsDriver {

    
    public static void viewAllReports(Connection conn, Scanner myObj){
        System.out.println("---------------------- Reports -----------------------");
        System.out.println("0 - Exit");
        System.out.println("1 - Reports About Number of Bookings");
        System.out.println("2 - Reports About Number of Listings");
        System.out.println("3 - Reports About Number of Noun Phrases in a Listing's Reviews");
        String choice = myObj.nextLine();
        if (choice.equals("1")) viewBookingReports(conn, myObj);
        if (choice.equals("2")) viewListingReports(conn, myObj);
        if (choice.equals("3")) parser(conn, myObj);
    }

    private static void viewBookingReports(Connection conn,Scanner myObj){
        System.out.println("1 - Number of Bookings By City and/or Postal");
        System.out.println("2 - Ranking Renters By Number of Bookings");
        System.out.println("3 - Renter(s) With Most Cancelled Bookings This Year");
        System.out.println("4 - Host(s) with Most Cancelled Bookings This Year");
        String choice = myObj.nextLine();
        if(choice.equals("1") || choice.equals("2")){
            System.out.println("Enter a specific date range to filter bookings reports in the YYYY-MM-DD format.");
            System.out.println("Start date of range: ");
            String start = myObj.nextLine();
            System.out.println("End date of range: ");
            String end = myObj.nextLine();
            LocalDate startDate = LocalDate.parse(start);
            LocalDate endDate = LocalDate.parse(end);
            if (choice.equals("1")){
                ReportsDAO.numBookingsByDatesAndCity(conn, startDate, endDate);
                System.out.println("Would you like to refine by the number of bookings per zip/postal code? (Y/N)");
                String byPostal = myObj.nextLine();
                if(byPostal.toLowerCase().equals("y")) ReportsDAO.numBookingsByDatesAndCityAndPostal(conn, startDate, endDate);
            }
            //TODO: Check how this is formatted
            else if (choice.equals("2")) ReportsDAO.rankRentersNumBookings(conn, startDate, endDate);
        }
        else if (choice.equals("3"))  ReportsDAO.maxRenterCancellations(conn);
        else if (choice.equals("4"))  ReportsDAO.maxHostCancellations(conn);
    }
    
    private static void viewListingReports(Connection conn, Scanner myObj){
        System.out.println("1 - Number of Listings By Country/City/Postal");
        System.out.println("2 - Ranking Hosts By Listings Per Country/City");
        System.out.println("3 - Possible Commercial Hosts Per Country");
        System.out.println("4 - Possible Commercial Hosts Per City");
        String choice = myObj.nextLine();
        if(choice.equals("1") || choice.equals("2")){
            if (choice.equals("1")){
                ReportsDAO.numListingsByCountry(conn);
                System.out.println("Would you like to refine by the number of listings per city? (Y/N)");
                String byCity = myObj.nextLine();
                if(byCity.toLowerCase().equals("y")) ReportsDAO.numListingsByCity(conn);
                System.out.println("Would you like to refine by the number of listings per postal code? (Y/N)");
                String byPostal = myObj.nextLine();
                if(byPostal.toLowerCase().equals("y")) ReportsDAO.numListingsByPostalCode(conn);
            }
            else if (choice.equals("2")){
                ReportsDAO.rankHostsByListingsPerCountry(conn);
                System.out.println("Would you like to refine the host ranking by number of listings per city? (Y/N)");
                String byCity = myObj.nextLine();
                if(byCity.toLowerCase().equals("y")) ReportsDAO.rankHostsByListingsPerCity(conn);
            }
        }
        else if (choice.equals("3")){
            System.out.println("These are hosts who have more than 10% of listings in a specific country.\n" +
            "If you are a MyBnB admin, please look into these hosts.");
            ReportsDAO.possibleCommercialHostsByCountry(conn);
        }
        else if (choice.equals("4")){
            System.out.println("These are hosts who have more than 10% of listings in a specific city.\n" +
            "If you are a MyBnB admin, please look into these hosts.");
            ReportsDAO.possibleCommercialHostsByCity(conn);
        }
    }

    private static void parser(Connection conn, Scanner myObj){
        //TODO: Do it for all instead of specific listId?
        System.out.println("ListID of the listing you'd like a word cloud of: ");
        int listID = Integer.parseInt(myObj.nextLine());
        System.out.println("Loading...");  
        System.out.println("Noun Phrases of listID: " + listID);
        NounParser.parser(conn, listID);
    }
}

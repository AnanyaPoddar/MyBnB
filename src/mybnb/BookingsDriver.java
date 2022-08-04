package mybnb;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Scanner;

public class BookingsDriver {
    public static void addBooking(Connection conn, Scanner myObj){
        System.out.println("Enter the id of the listing you'd like to book.\n");
        int listingID = Integer.parseInt(myObj.nextLine());

        System.out.println("Enter start date of your booking: ");
        String start = myObj.nextLine();
        System.out.println("Enter end date of your booking: ");
        String end = myObj.nextLine();
        LocalDate startDate = LocalDate.parse(start);
        LocalDate endDate = LocalDate.parse(end);

        BookingsDAO.addBooking(conn, listingID, startDate, endDate);
    }   


    public static void hostCancelsBooking(Connection conn, Scanner myObj){
        System.out.println("Enter the id of the listing you'd like to cancel a booking for.\n");
        int listingID = Integer.parseInt(myObj.nextLine());
        
        //check that host is associated with this listing
        try {
            if(!AvailabilityDAO.hostsListing(conn, listingID)) return;
            System.out.println("Enter start date of the booking.");
            String start = myObj.nextLine();
            System.out.println("Enter end date of the booking.");
            String end = myObj.nextLine();
            //TODO: Add all the parsing date stuff to ALL localdates, to give back appropriate feedback of not valid
            LocalDate startDate = LocalDate.parse(start);
            LocalDate endDate = LocalDate.parse(end);
            BookingsDAO.cancelBooking(conn, myObj, listingID, startDate, endDate);
            //do not modify availabilities table, they are not available, just cancelled
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }   

    public static void userCancelsBooking(Connection conn, Scanner myObj){
        System.out.println("Enter the id of the listing you'd like to cancel a booking for.\n");
        int listingID = Integer.parseInt(myObj.nextLine());
        System.out.println("Enter start date of the booking.");
        String start = myObj.nextLine();
        System.out.println("Enter end date of the booking.");
        String end = myObj.nextLine();
        LocalDate startDate = LocalDate.parse(start);
        LocalDate endDate = LocalDate.parse(end);
        BookingsDAO.cancelBooking(conn, myObj, listingID, startDate, endDate);
        //add back to availabilities table
        AvailabilityDAO.setAvailability(conn, listingID, startDate, endDate, "cancelled");

    }
    
}

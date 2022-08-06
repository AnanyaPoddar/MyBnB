package mybnb;

import java.sql.Connection;
import java.util.Scanner;

public class MenuDriver {

    public static void loggedOutOrRenterListingMenu(Connection conn, Scanner myObj){
        int option = -1;
        while (option != 0) {
            System.out.println("---------------------- Listings ----------------------");
            System.out.println("0 - Exit Listings Mode"); 
            System.out.println("1 - View All Listings");
            System.out.println("2 - Check Availabilities for a Listing");
            //TODO: Does the "get listings between 2 dates need to go somewhere separate or here is fine"
            System.out.println("3 - Search and Filter Listings");
            option = Integer.parseInt(myObj.nextLine());
            if(option == 1)
                ListingDAO.viewAllListings(conn);
            else if(option == 2){
                System.out.println("Enter a listID to see availabilities: ");
                int listID = Integer.parseInt(myObj.nextLine());
                AvailabilityDAO.getAvailabilities(conn, listID, myObj);
            }
            else if(option == 3){
                Search.searchListings(conn, myObj);
            }
        }

    }

    public static void hostListingMenu(Connection conn, Scanner myObj){
        int option = -1;
        while (option != 0) {
            System.out.println("---------------------- Listings ----------------------");
            System.out.println("0 - Exit Listings Mode"); 
            System.out.println("1 - View All Your Listings");
            System.out.println("2 - See/Modify Availabilities for a Listing");
            System.out.println("3 - Delete a Listing");
            option = Integer.parseInt(myObj.nextLine());
            if(option == 1)
                ListingDAO.viewAllListingsByHost(conn);
            else if(option == 2){
                System.out.println("Enter a listID to see/modify availabilities: ");
                int listID = Integer.parseInt(myObj.nextLine());
                System.out.println("Would you like to see availabilities for this listing? (Y/N) ");
                if(myObj.nextLine().toLowerCase().equals("y"))
                    AvailabilityDAO.getAvailabilities(conn, listID, myObj);
                System.out.println("Would you like to modify availabilities for this listing? (Y/N) ");
                if(myObj.nextLine().toLowerCase().equals("y"))
                    AvailabilityDriver.modifyAvailabilities(conn, myObj, listID);
            }
            else if(option == 3){
                System.out.println("Enter a listID to delete a listing: ");
                int listID = Integer.parseInt(myObj.nextLine());
                ListingDAO.deleteListing(conn, myObj, listID);
            }
        }
    }

    public static void hostBookingMenu(Connection conn, Scanner myObj){
        int option = -1;
        while (option != 0) {
            System.out.println("---------------------- Bookings ----------------------");
            System.out.println("0 - Exit Bookings Mode"); 
            System.out.println("1 - View All Your Upcoming Bookings");
            System.out.println("2 - Cancel An Upcoming Booking");
            System.out.println("3 - View All Your Past Bookings"); //should review show up here?
            System.out.println("4 - Review a Past Renter");

            option = Integer.parseInt(myObj.nextLine());
            if(option == 1)
                BookingsDAO.getAllBookingsForHost(conn, "booked");
            else if(option == 2){
                System.out.print("Enter a listID for the listing to cancel a booking for: ");
                int listingID = Integer.parseInt(myObj.nextLine());
                BookingsDriver.hostCancelsBooking(conn, myObj, listingID);
            }
            else if(option == 3){
                BookingsDAO.getAllBookingsForHost(conn, "past");
            }
            else if(option == 4){
                UserDAO.hostReviewsRenter(conn, myObj);
            }
        }
    }


    public static void renterBookingMenu(Connection conn, Scanner myObj){
        int option = -1;
        while (option != 0) {
            System.out.println("---------------------- Bookings ----------------------");
            System.out.println("0 - Exit Bookings Mode"); 
            System.out.println("1 - View All Your Upcoming Bookings");
            System.out.println("2 - Cancel An Upcoming Booking");
            System.out.println("3 - View All Your Past Bookings"); //should review show up here?
            System.out.println("4 - Review a Past Host");
            System.out.println("5 - Review a Past Listing");


            option = Integer.parseInt(myObj.nextLine());
            if(option == 1)
                BookingsDAO.getAllBookingsForRenter(conn, "booked");
            else if(option == 2){
                System.out.println("Enter a listID for the listing to cancel a booking for: ");
                int listingID = Integer.parseInt(myObj.nextLine());
                BookingsDriver.renterCancelsBooking(conn, myObj, listingID);
            }
            else if(option == 3){
                BookingsDAO.getAllBookingsForRenter(conn, "past");
            }
            else if(option == 4){
                UserDAO.renterReviewsHost(conn, myObj);
            }
            else if(option == 5){
                UserDAO.rentersReviewListings(conn, myObj);
            }
        }
    }
    
}

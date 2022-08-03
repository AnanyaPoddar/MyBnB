package mybnb;

import java.sql.Connection;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.Scanner;

public class BookingsDAO {

    //Under assumption that they can enter a range of date for the booking, 
    public static void addBooking(Connection conn, Scanner myObj){
        System.out.println("Enter the id of the listing you'd like to book.\n");
        int listingID = Integer.parseInt(myObj.nextLine());
        //Check that user is a logged-in renter
        if(!UserDAO.verifyUserInTable(conn, DAO.loggedInUser, "renterSIN", "Renter")){
            System.out.println("You must be logged in as a renter.");
            return;
        }

        System.out.println("Enter start date of your booking: ");
        String start = myObj.nextLine();
        System.out.println("Enter end date of your booking: ");
        String end = myObj.nextLine();
        LocalDate startDate = LocalDate.parse(start);
        LocalDate endDate = LocalDate.parse(end);

        try {
            //check that the dates are all available for booking; if so, remove them from availabilities, and add to booked
            if(!AvailabilityDAO.checkListingAvailabilityOnDates(conn, startDate, endDate, listingID)) return;
            Statement statement = conn.createStatement();
            String bookingInsert = String.format("INSERT INTO Booked(listID, renterSIN, startDate, endDate) VALUES (%d, %d, '%s', '%s');", listingID, DAO.loggedInUser, Date.valueOf(startDate), Date.valueOf(endDate));
            statement.executeUpdate(bookingInsert);
            //remove from availabilities table only after successful booking;
            AvailabilityDAO.deleteAvailabilities(conn, listingID, startDate, endDate);
            System.out.println("Successfully added booking for listing with id " + listingID + " between dates " + startDate.toString() + " and " + endDate.toString());
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }   

    //TODO: optional field for status so you can get all past bookings, cancelled, etc
    public static void getAllBookingsForRenter(Connection conn){
        //Check that user is a logged-in renter
        if(!UserDAO.verifyUserInTable(conn, DAO.loggedInUser, "renterSIN", "Renter")){
            System.out.println("You must be logged in as a renter.");
            return;
        }
        try {
            Statement statement = conn.createStatement();
            String bookings = String.format("SELECT * from Booked WHERE renterSIN = %d;", DAO.loggedInUser);
            ResultSet rs = statement.executeQuery(bookings);
            while(rs.next()){
                System.out.println("ListId: " + rs.getInt("listID"));
                System.out.println("Status: " + rs.getString("status"));
                System.out.println("Start Date: " + rs.getDate("startDate"));
                System.out.println("End Date: " + rs.getDate("endDate"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    public static void getAllBookingsForHost(Connection conn){
        //Check that user is a logged-in host
        if(!UserDAO.verifyUserInTable(conn, DAO.loggedInUser, "hostSIN", "Host")){
            System.out.println("You must be logged in as a host.");
            return;
        }
        try {
            Statement statement = conn.createStatement();
            //collect listings for host from hostsToListings, join with booked project listID, startDate, endDate
            String bookings = String.format("SELECT Booked.listID, startDate, endDate, status FROM Booked " +
            "JOIN (SELECT listID from HostsToListings WHERE hostSIN = '%s') as h1 " +
            "ON Booked.listID=h1.listID ORDER BY Booked.listID;", DAO.loggedInUser);
            ResultSet rs = statement.executeQuery(bookings);
            while(rs.next()){
                System.out.println("ListID: " + rs.getInt("listID"));
                System.out.println("Start Date: " + rs.getDate("startDate"));
                System.out.println("End Date"  + rs.getDate("endDate"));
                System.out.println("Status " + rs.getString("status"));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    //Helper
    //TODO: Should type be localDate or string?
    private static void cancelBooking(Connection conn, Scanner myObj, int listingID, String startDate, String endDate){
        //TODO: Either parse as localdate or just ensure that valid format
        String deleteListing = String.format("UPDATE Booked SET status = 'cancelled' WHERE listID= %d AND startDate = '%s' AND endDate = '%s' ", listingID, startDate, endDate);
        try {
            Statement statement = conn.createStatement();
            int rows = statement.executeUpdate(deleteListing);
            if(rows > 0)
              System.out.println("Successfully cancelled booking with listID " + listingID);
            else
              System.out.println("No booking found with that listID, start date and end date combination.");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void hostCancelsBooking(Connection conn, Scanner myObj) throws SQLException{
        //TODO: Check logic - if a host cancels a booking, its status is set to cancelled in bookings table, and not readded to availabilities
        //However, if a user cancels a booking, it becomes available again for someone else to book so needs to be readded to availabilities table
        //Check that user is a logged-in host
        if(!UserDAO.verifyUserInTable(conn, DAO.loggedInUser, "hostSIN", "Host")){
            System.out.println("You must be logged in as a host.");
            return;
        }
        System.out.println("Enter the id of the listing you'd like to cancel a booking for.\n");
        int listingID = Integer.parseInt(myObj.nextLine());
        
        //check that host is associated with this listing
        if(!AvailabilityDAO.hostsListing(conn, listingID)) return;
        System.out.println("Enter start date of the booking.");
        String start = myObj.nextLine();
        System.out.println("Enter end date of the booking.");
        String end = myObj.nextLine();
        cancelBooking(conn, myObj, listingID, start, end);
        //do not modify availabilities table, they are not available, just cancelled
    }   

    public static void userCancelsBooking(Connection conn, Scanner myObj){
        if(!UserDAO.verifyUserInTable(conn, DAO.loggedInUser, "renterSIN", "Renter")){
            System.out.println("You must be logged in as a renter.");
            return;
        }
        System.out.println("Enter the id of the listing you'd like to cancel a booking for.\n");
        int listingID = Integer.parseInt(myObj.nextLine());
        System.out.println("Enter start date of the booking.");
        String start = myObj.nextLine();
        System.out.println("Enter end date of the booking.");
        String end = myObj.nextLine();
        cancelBooking(conn, myObj, listingID, start, end);

        //TODO: Again weird discrepancy where availabilityDAO uses localDate but cancelBooking doesnt

        //add back to availabilities table
        LocalDate startDate = LocalDate.parse(start);
        LocalDate endDate = LocalDate.parse(end);
        // AvailabilityDAO.deleteAvailabilities(conn, listingID, startDate, endDate);

    }

    
}

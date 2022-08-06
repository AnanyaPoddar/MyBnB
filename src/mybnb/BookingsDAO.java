package mybnb;

import java.sql.Connection;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.List;
import java.util.Scanner;

public class BookingsDAO {

    public static void addBooking(Connection conn, int listingID, LocalDate startDate, LocalDate endDate){
        try {
            //check that the dates are all available for booking; if so, remove them from availabilities, and add to booked
            Float cost = AvailabilityDAO.getAvailabilityPriceOnDates(conn, startDate, endDate, listingID);
            if(cost == -1) return;
            Statement statement = conn.createStatement();
            String bookingInsert = String.format("INSERT INTO Booked(listID, renterSIN, startDate, endDate, cost) VALUES (%d, %d, '%s', '%s', %f);", listingID, Main.loggedInUser, Date.valueOf(startDate), Date.valueOf(endDate), cost);
            statement.executeUpdate(bookingInsert);
            //remove from availabilities table only after successful booking;
            AvailabilityDAO.setAvailability(conn, listingID, startDate, endDate, "booked");
            System.out.println("Successfully added booking for listing with id " + listingID + " between dates " + startDate.toString() + " and " + endDate.toString());
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    public static void getAllBookingsForRenter(Connection conn, String status){
        //Update bookings to past before displaying here
        setPastBookingsByRenter(conn);
        try {
            Statement statement = conn.createStatement();
            String bookings = String.format("SELECT * from Booked WHERE renterSIN = %d AND status = '%s';", Main.loggedInUser, status);
            ResultSet rs = statement.executeQuery(bookings);
            while(rs.next()){
                System.out.println("ListId: " + rs.getInt("listID"));
                System.out.println("Dates: " + rs.getDate("startDate") + " - " + rs.getDate("endDate"));
                System.out.println("Cost: " + rs.getString("cost") + "\n\n");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void getAllBookingsForHost(Connection conn, String status){
        //Update host's bookings to past before displaying here
        setPastBookingsByHost(conn);
        try {
            Statement statement = conn.createStatement();
            //collect listings for host from hostsToListings, join with booked project listID, startDate, endDate
            String bookings = String.format("SELECT Booked.listID, startDate, endDate, cost, status FROM Booked " +
            "JOIN (SELECT listID from HostsToListings WHERE hostSIN = '%s') as h1 " +
            "ON Booked.listID=h1.listID WHERE status = '%s' ORDER BY Booked.listID;", Main.loggedInUser, status);
            ResultSet rs = statement.executeQuery(bookings);
            while(rs.next()){
                System.out.println("ListID: " + rs.getInt("listID"));
                System.out.println("Dates: " + rs.getDate("startDate") + " - " + rs.getDate("endDate"));
                System.out.println("Cost: " + rs.getString("cost") + "\n\n");
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    //Helper
    public static void cancelBooking(Connection conn, Scanner myObj, int listingID, LocalDate startDate, LocalDate endDate){
        //first check that dates are valid and not past
        if(!AvailabilityDAO.checkValidDates(startDate, endDate)) return;
        String deleteBooking = String.format("UPDATE Booked SET status = 'cancelled' WHERE listID= %d AND startDate = '%s' AND endDate = '%s';", listingID, startDate, endDate);
        try {
            Statement statement = conn.createStatement();
            int rows = statement.executeUpdate(deleteBooking);
            if(rows > 0)
              System.out.println("Successfully cancelled booking with listID " + listingID);
            else
              System.out.println("No booking found with that listID, start date and end date combination.");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    //when a renter wants to see their past bookings, update relevant bookings to past before displaying
    public static void setPastBookingsByRenter(Connection conn){
        String past = String.format("UPDATE Booked SET status = 'past' WHERE renterSIN = %d AND endDate < '%s';", Main.loggedInUser, LocalDate.now());
        try {
          Statement statement = conn.createStatement();
          statement.executeUpdate(past);
        } catch (SQLException e) {
          e.printStackTrace();
        }
    }

    public static void setPastBookingsByHost(Connection conn){
        try {
            //get all listings by current host, set to past as required;
            List<Integer> listingsByHost = ListingDAO.getAllListingsByHost(conn);
            String stringListings = "(";
            for(Integer listing : listingsByHost){
                stringListings += listing.toString();
            }
            stringListings += ")";
            String past = String.format("UPDATE Booked SET status = 'past' WHERE listID in %s AND endDate < '%s';", stringListings, LocalDate.now());
            Statement statement = conn.createStatement();
            statement.executeUpdate(past);
        } catch (SQLException e) {
          e.printStackTrace();
        }
    }



    
}

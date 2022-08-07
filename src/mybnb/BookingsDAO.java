package mybnb;

import java.sql.Connection;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.util.List;
import java.util.Scanner;

public class BookingsDAO {
    static DecimalFormat df = new DecimalFormat("0.00");

    
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

    //Updated to show relevant information such as the host and address
    public static void getAllBookingsForRenter(Connection conn, String status){
        //Update bookings to past before displaying here
        setPastBookingsByRenter(conn);
        try {
            Statement statement = conn.createStatement();
            // String bookings = String.format("SELECT * from Booked WHERE renterSIN = %d AND status = '%s';", Main.loggedInUser, status);

            String bookings = String.format("SELECT startDate, endDate, cost, u.uname AS host, a.* from Booked AS b "+
            "JOIN hostsToListings AS h ON h.listID=b.listID JOIN user AS u ON hostSIN=u.SIN JOIN addresses AS a ON a.listID=b.listID " +
            "WHERE renterSIN = %d AND status = '%s' ;", Main.loggedInUser, status);
            ResultSet rs = statement.executeQuery(bookings);
            if(!rs.isBeforeFirst()) {
                System.out.println("No bookings."); 
                return;
            }
            while(rs.next()){
                System.out.println("ListId: " + rs.getInt("listID") + ", Host: " +rs.getString("host") + " , Cost: $" + df.format(rs.getFloat("cost")));
                System.out.println("Dates: " + rs.getDate("startDate") + " - " + rs.getDate("endDate"));
                int unitNum = rs.getInt("unitNum");
                System.out.println("Address: " + rs.getString("street")+ ", " + (unitNum != 0 ? "unit " + unitNum + ", " : "") + rs.getString("city") + ", " + rs.getString("country") + ", " + rs.getString("postal")+ "\n");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    //Updated to show relevant information such as the renter and address
    public static void getAllBookingsForHost(Connection conn, String status){
        //Update host's bookings to past before displaying here
        setPastBookingsByHost(conn);
        try {
            Statement statement = conn.createStatement();
            //collect listings for host from hostsToListings, join with booked project listID, startDate, endDate
            String bookings = String.format("SELECT startDate, endDate, cost, u.uname AS renter, a.* from Booked AS b " +
            "JOIN (SELECT listID from HostsToListings WHERE hostSIN = %d) as h1 ON b.listID=h1.listID " + 
            "JOIN addresses AS a ON a.listID=b.listID JOIN user as u ON u.SIN=b.renterSIN WHERE status = '%s' ORDER BY b.listID;", Main.loggedInUser, status);
            ResultSet rs = statement.executeQuery(bookings);
            if(!rs.isBeforeFirst()) {
                System.out.println("No bookings."); 
                return;
            }
            while(rs.next()){
                System.out.println("ListId: " + rs.getInt("listID") + ", Renter: " +rs.getString("renter") + " , Cost: $" + df.format(rs.getFloat("cost")));
                System.out.println("Dates: " + rs.getDate("startDate") + " - " + rs.getDate("endDate"));
                int unitNum = rs.getInt("unitNum");
                System.out.println("Address: " + rs.getString("street")+ ", " + (unitNum != 0 ? "unit " + unitNum + ", " : "") + rs.getString("city") + ", " + rs.getString("country") + ", " + rs.getString("postal")+ "\n");
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
            for(int i = 0; i < listingsByHost.size(); i++){
                if(i == listingsByHost.size() - 1) stringListings += listingsByHost.get(i).toString();
                else stringListings += listingsByHost.get(i).toString() + " ,";
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

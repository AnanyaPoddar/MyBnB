package mybnb;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;

public class ReportsDAO {
    //TODO: This returns bookings with any status, including past, booked, cancelled
    //This orders by city, TODO: Does the acc report doc mean I should be able to query by city as well?
    public static void numBookingsByDatesAndCity(Connection conn, LocalDate startDate, LocalDate endDate){
        try {
            Statement stmt = conn.createStatement();
            String sql = String.format("SELECT a.city AS city, count(a.listID) as numBookings FROM Booked as b " +
            "JOIN addresses as a ON a.listID = b.listID WHERE startDate >= '%s' AND endDate <= '%s' GROUP BY a.city ORDER BY a.city;", startDate, endDate);
            ResultSet rs = stmt.executeQuery(sql);
            while(rs.next()){
                System.out.print("City: " + rs.getString("city"));
                System.out.println(", Number of bookings: " + rs.getInt("numBookings"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    
    public static void numBookingsByDatesAndCityAndPostal(Connection conn, LocalDate startDate, LocalDate endDate){
        try {
            Statement stmt = conn.createStatement();
            String sql = String.format("SELECT a.city AS city, count(a.listID) as numBookings FROM Booked as b " +
            "JOIN addresses as a ON a.listID = b.listID WHERE startDate >= '%s' AND endDate <= '%s' GROUP BY a.city ORDER BY a.city;", startDate, endDate);
            ResultSet rs = stmt.executeQuery(sql);
            while(rs.next()){
                System.out.print("City: " + rs.getString("city"));
                System.out.println(", Number of bookings: " + rs.getInt("numBookings"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    //TODO: Get number of bookings by zip codes within a city, does this just group by the postal codes instead or should I provide a specific city

    public static void numListingsByCountry(Connection conn){
        try {
            Statement stmt = conn.createStatement();
            String sql = String.format("SELECT count(listID) AS count, country FROM addresses GROUP BY(country);");
            ResultSet rs = stmt.executeQuery(sql);
            while(rs.next()){
                System.out.print("Country: " + rs.getString("country"));
                System.out.println(", Number of Listings: " + rs.getInt("count"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    //TODO: Reevaluate later if this is supposed to be just cancelled by anyone or specifically by renter
    public static void maxRenterCancellations(Connection conn){
        //subquery gets the number of cancelled bookings by renter; then for each renter in outer query, it checks if the count is greater than the count of all other
        //join with user just to display name instead of SIN
        try {
            Statement stmt = conn.createStatement();
            String sql = "SELECT user.uname AS name, count(*) AS count FROM booked JOIN user ON booked.renterSIN=user.SIN WHERE status='cancelled' "+
            "GROUP BY renterSIN HAVING count(*) >= ALL(SELECT count(*) FROM booked WHERE status = 'cancelled' GROUP BY renterSIN);";
            ResultSet rs = stmt.executeQuery(sql);
            while(rs.next()){
                System.out.print("Renter: " + rs.getString("name"));
                System.out.println(", Number of Cancelled Bookings: " + rs.getInt("count"));
            }
        }catch (SQLException e) {
            e.printStackTrace();
        }
    }

    //TODO: Reevaluate later if this is supposed to be just cancelled by anyone or specifically by host
    public static void maxHostCancellations(Connection conn){
        //Same as for renter but another join required with hostsToListings to get the actual host
        try {
            Statement stmt = conn.createStatement();
            String sql = "SELECT count(*) AS count, user.uname as name FROM booked as b JOIN HostsToListings as h ON h.listID = b.listID JOIN User ON h.hostSIN = user.SIN " +
            "WHERE status='cancelled' GROUP BY hostSIN HAVING count >=  ALL(" +
            "SELECT count(*) FROM booked AS b JOIN HostsToListings AS h ON h.listID = b.listID WHERE status='cancelled' GROUP BY hostSIN);";
            ResultSet rs = stmt.executeQuery(sql);
            while(rs.next()){
                System.out.print("Host: " + rs.getString("name"));
                System.out.println(", Number of Cancelled Bookings: " + rs.getInt("count"));
            }
        }catch (SQLException e) {
            e.printStackTrace();
        }
    }
    


    // public static void numListingsByCountryAndCity(Connection conn, String country, String city){
    //     try {
    //         Statement stmt = conn.createStatement();
    //         String sql = String.format("SELECT count(listID) AS count FROM addresses WHERE country = '%s' AND city = '%s';", country, city);
    //         ResultSet rs = stmt.executeQuery(sql);
    //     } catch (SQLException e) {
    //         e.printStackTrace();
    //     }
    // }

    // public static void numListingsByCountryAndCityAndPostalCode(Connection conn, String country, String city, String postalCode){
    //     try {
    //         Statement stmt = conn.createStatement();
    //         String sql = String.format("SELECT count(listID) AS count FROM addresses WHERE country = '%s' AND city = '%s' AND postal = '%s';", country, city, postalCode);
    //         ResultSet rs = stmt.executeQuery(sql);
    //     } catch (SQLException e) {
    //         e.printStackTrace();
    //     }
    // }

    // //TODO: Does rank mean actually assign a number ? or ordering is good enough?
    // public static void rankHostsByListingsPerCountry(Connection conn, String country){
    //     try {
    //         Statement stmt = conn.createStatement();
    //         String sql = String.format("SELECT hostSIN, count(h.listID) AS numListings FROM HostsToListings AS h JOIN " +
    //         "Addresses AS a ON a.listID = h.listID WHERE country='%s' GROUP BY(hostSIN) ORDER BY (numListings) DESC;", country);
    //         ResultSet rs = stmt.executeQuery(sql);
    //     } catch (SQLException e) {
    //         e.printStackTrace();
    //     }
    // }

    // public static void rankHostsByListingsPerCountryAndCity(Connection conn, String country, String city){
    //     try {
    //         Statement stmt = conn.createStatement();
    //         String sql = String.format("SELECT hostSIN, count(h.listID) AS numListings FROM HostsToListings AS h JOIN " +
    //         "Addresses AS a ON a.listID = h.listID WHERE country='%s' AND city = '%s' GROUP BY(hostSIN) ORDER BY (numListings) DESC;", country, city);
    //         ResultSet rs = stmt.executeQuery(sql);
    //     } catch (SQLException e) {
    //         e.printStackTrace();
    //     }
    // }
    
}

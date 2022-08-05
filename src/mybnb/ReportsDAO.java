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

    public static void rankRentersNumBookings(Connection conn, LocalDate startDate, LocalDate endDate){
        try {
            // TODO Which statuses should be included or not
            // TODO Does this include past bookings even if it's not in the date range? should have brackets?
            Statement stmt = conn.createStatement();
            String sql = String.format("SELECT renterSIN, COUNT(renterSIN) as NUMBOOKINGS from BOOKED WHERE startDate >= '%s' AND endDate <= '%s' AND (status = 'booked' OR status = 'past') GROUP BY renterSIN ORDER BY NUMBOOKINGS DESC;", startDate, endDate);
            ResultSet rs = stmt.executeQuery(sql);
            while(rs.next()){
                System.out.print("renterSIN: " + rs.getInt("renterSIN"));
                System.out.println(", Number of bookings: " + rs.getInt("numBookings"));
            }

            // TODO What if multiple cities of same name?
            System.out.println("------------------------------------------------------");
            System.out.println("Rank of renters by city");
            String cityRank = String.format("SELECT booked.*, country, city, COUNT(booked.renterSIN) as NUMBOOKINGS from BOOKED JOIN ADDRESSES ON addresses.listID = booked.listID JOIN (select renterSIN from booked where extract(year from startDate) = extract(year from '%s') AND (status = 'booked' or status = 'past') GROUP BY renterSIN HAVING count(renterSIN) >= 2) as temp ON temp.renterSIN = booked.renterSIN WHERE startDate >= '%s' AND endDate <= '%s' AND (status = 'booked' OR status = 'past') GROUP BY booked.renterSIN, city ORDER BY city, NUMBOOKINGS DESC;", startDate, startDate, endDate);
            ResultSet rs2 = stmt.executeQuery(cityRank);
            while(rs2.next()){
                System.out.print("renterSIN: " + rs2.getInt("renterSIN"));
                System.out.print(", City: " + rs2.getString("city"));
                System.out.println(", Number of bookings: " + rs2.getInt("numBookings"));
            }

        } catch (SQLException e) {
            // TODO Replace with error?
            e.printStackTrace();
        }

        // todo does the numBookings > 2 count cancelled as well? maybe not since we're not counting them in the whole query and the cancelled booking wouldn't appear in ranking
        // select *, count(renterSIN) from booked where extract(year from startDate) = extract(year from '2022-01-01') AND (status = 'booked' or status = 'past') GROUP BY renterSIN HAVING count(renterSIN) >= 2;

        // select booked.* from booked JOIN (select * from booked where extract(year from startDate) = extract(year from '2022-01-01') AND (status = 'booked' or status = 'past') GROUP BY renterSIN HAVING count(renterSIN) >= 2) as temp ON temp.renterSIN = booked.renterSIN;

        // SELECT booked.*, country, city, COUNT(renterSIN) as NUMBOOKINGS from BOOKED JOIN ADDRESSES ON addresses.listID = booked.listID WHERE startDate >= '%s' AND endDate <= '%s' AND (status = 'booked' OR status = 'past') GROUP BY renterSIN, city ORDER BY city, NUMBOOKINGS DESC;

        // SELECT booked.*, country, city, COUNT(booked.renterSIN) as NUMBOOKINGS from BOOKED JOIN ADDRESSES ON addresses.listID = booked.listID JOIN (select renterSIN from booked where extract(year from startDate) = extract(year from '2022-01-01') AND (status = 'booked' or status = 'past') GROUP BY renterSIN HAVING count(renterSIN) >= 2) as temp ON temp.renterSIN = booked.renterSIN WHERE startDate >= '2022-01-01' AND endDate <= '2022-12-31' AND (status = 'booked' OR status = 'past') GROUP BY booked.renterSIN, city ORDER BY city, NUMBOOKINGS DESC;

        

        // SELECT booked.*, country, city, COUNT(renterSIN) as NUMBOOKINGS from BOOKED JOIN ADDRESSES ON addresses.listID = booked.listID WHERE startDate >= '2022-08-10' AND endDate <= '2022-09-05' AND (status = 'booked' OR status = 'past') AND extract(year from startDate) = extract(year from '2022-01-01') GROUP BY renterSIN, city HAVING count(renterSIN) > 4 ORDER BY city, NUMBOOKINGS DESC;
    }

    

    //  



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

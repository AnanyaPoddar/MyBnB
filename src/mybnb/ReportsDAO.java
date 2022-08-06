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


    //TODO: Get number of bookings by zip codes within a city, does this just group by the postal codes instead or should I provide a specific city

    public static void numListingsByCountry(Connection conn){
        try {
            Statement stmt = conn.createStatement();
            String sql = String.format("SELECT count(listID) AS count, country FROM addresses GROUP BY country ORDER BY country;");
            ResultSet rs = stmt.executeQuery(sql);
            while(rs.next()){
                System.out.print("Country: " + rs.getString("country"));
                System.out.println(", Number of Listings: " + rs.getInt("count"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void numListingsByCity(Connection conn){
        try {
            Statement stmt = conn.createStatement();
            String sql = String.format("SELECT count(listID) AS count, country, city FROM addresses GROUP BY country, city ORDER BY country, city;");
            ResultSet rs = stmt.executeQuery(sql);
            while(rs.next()){
                System.out.print("City: " + rs.getString("city"));
                System.out.print(", Country: " + rs.getString("country"));
                System.out.println(", Number of Listings: " + rs.getInt("count"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void numListingsByPostalCode(Connection conn){
        try {
            Statement stmt = conn.createStatement();
            String sql = String.format("SELECT count(listID) AS count, postal, city, country FROM addresses GROUP BY country, city, postal ORDER BY country, city, postal;");
            ResultSet rs = stmt.executeQuery(sql);
            while(rs.next()){
                System.out.print("Postal: " + rs.getString("postal"));
                System.out.print(", City: " + rs.getString("city"));
                System.out.print(", Country: " + rs.getString("country"));
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
            String sql = "SELECT count(*) AS count, user.uname AS name FROM booked AS b JOIN HostsToListings AS h ON h.listID = b.listID JOIN User ON h.hostSIN = user.SIN " +
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
            String sql = String.format("SELECT user.uname AS name, COUNT(renterSIN) as NUMBOOKINGS from BOOKED JOIN user ON booked.renterSIN = user.SIN WHERE startDate >= '%s' AND endDate <= '%s' AND (status = 'booked' OR status = 'past') GROUP BY name ORDER BY NUMBOOKINGS DESC;", startDate, endDate);
            ResultSet rs = stmt.executeQuery(sql);
            while(rs.next()){
                System.out.print("Renter: " + rs.getString("name"));
                System.out.println(", Number of bookings: " + rs.getInt("numBookings"));
            }

            // TODO What if multiple cities of same name?
            System.out.println("------------------------------------------------------");
            System.out.println("Rank of renters by city");
            System.out.println("Note: only users who have at least 2 bookings during the year of startDate will be displayed.");
            String cityRank = String.format("SELECT user.uname AS name, country, city, COUNT(booked.renterSIN) as NUMBOOKINGS from BOOKED JOIN user ON booked.renterSIN = user.SIN JOIN ADDRESSES ON addresses.listID = booked.listID JOIN (select renterSIN from booked where extract(year from startDate) = extract(year from '%s') AND (status = 'booked' or status = 'past') GROUP BY renterSIN HAVING count(renterSIN) >= 2) as temp ON temp.renterSIN = booked.renterSIN WHERE startDate >= '%s' AND endDate <= '%s' AND (status = 'booked' OR status = 'past') GROUP BY name, country, city ORDER BY city, NUMBOOKINGS DESC;", startDate, startDate, endDate);
            ResultSet rs2 = stmt.executeQuery(cityRank);
            while(rs2.next()){
                System.out.print("Renter: " + rs2.getString("name"));
                System.out.print(", Country: " + rs2.getString("country"));
                System.out.print(", City: " + rs2.getString("city"));
                System.out.println(", Number of bookings: " + rs2.getInt("numBookings"));
            }

        } catch (SQLException e) {
            // TODO Replace with error?
            e.printStackTrace();
        }

        // todo does the numBookings > 2 count cancelled as well? maybe not since we're not counting them in the whole query and the cancelled booking wouldn't appear in ranking
    }

    

    //  



    //TODO: Does rank mean actually assign a number ? or ordering is good enough?
    public static void rankHostsByListingsPerCountry(Connection conn){
        //joined with user to retrieve the username, otherwise would display the hostSIN
        try {
            //group by 2 things means both must be satisfied in the group
            Statement stmt = conn.createStatement();
            String sql = "SELECT count(*) AS count, country, uname as hostName FROM HostsToListings AS h " + 
            "JOIN addresses AS a ON a.listID=h.listID JOIN user ON h.hostSIN = user.SIN " +
            "GROUP BY country, hostSIN ORDER BY country,count(*) DESC;";

            ResultSet rs = stmt.executeQuery(sql);
            while(rs.next()){
                System.out.print("Country: " + rs.getString("country"));
                System.out.print(", Count: " + rs.getInt("count"));
                System.out.println(", Host: " + rs.getString("hostName"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void rankHostsByListingsPerCity(Connection conn){
        //TODO: Should this return the country as well?
        try {
            Statement stmt = conn.createStatement();
            String sql = "SELECT count(*) AS count, city, uname as hostName FROM HostsToListings AS h " + 
            "JOIN addresses AS a ON a.listID=h.listID JOIN user ON h.hostSIN = user.SIN " +
            "GROUP BY country, city, hostSIN ORDER BY city, count(*) DESC;";
            ResultSet rs = stmt.executeQuery(sql);
            while(rs.next()){
                System.out.print("City: " + rs.getString("city"));
                System.out.print(", Count: " + rs.getInt("count"));
                System.out.println(", Host: " + rs.getString("hostName"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    //TODO: Is there anything to do with this info, do we actually need to flag
    public static void possibleCommercialHostsByCountry(Connection conn){
        //add another column for total count per country, then just check if count/totalCount >= 1/10        
        String createView = "CREATE OR REPLACE VIEW hostListingsPerCountry AS " +
        "(SELECT count(*) AS count, country, uname as hostName FROM HostsToListings AS h JOIN addresses AS a ON a.listID=h.listID " +
        "JOIN user ON h.hostSIN = user.SIN GROUP BY country, hostSIN ORDER BY country,count(*) DESC);";

        String query = "SELECT a.country, hostName FROM hostListingsPerCountry as h JOIN " +
        "(SELECT DISTINCT country, count(*) AS totalCount FROM addresses GROUP BY country) AS a " +
        "ON a.country = h.country WHERE count/totalCount > 0.1 ORDER BY country, hostName;";
    
        try {
            Statement stmt = conn.createStatement();
            stmt.execute(createView);
            ResultSet rs = stmt.executeQuery(query);
            while(rs.next()){
                System.out.print("Country: " + rs.getString("country"));
                System.out.println(", Host: " + rs.getString("hostName"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void possibleCommercialHostsByCity(Connection conn){
        //add another column for total count per country, then just check if count/totalCount >= 1/10        
        String createView = "CREATE OR REPLACE VIEW hostListingsPerCity AS " +
        "(SELECT count(*) AS count, city, uname as hostName FROM HostsToListings AS h JOIN addresses AS a ON a.listID=h.listID " +
        "JOIN user ON h.hostSIN = user.SIN GROUP BY city, hostSIN ORDER BY city,count(*) DESC);";

        String query = "SELECT a.city, hostName FROM hostListingsPerCity as h JOIN " +
        "(SELECT DISTINCT city, count(*) AS totalCount FROM addresses GROUP BY city) AS a " +
        "ON a.city = h.city WHERE count/totalCount > 0.1 ORDER BY city, hostName;";
    
        try {
            Statement stmt = conn.createStatement();
            stmt.execute(createView);
            ResultSet rs = stmt.executeQuery(query);
            while(rs.next()){
                System.out.print("City: " + rs.getString("city"));
                System.out.println(", Host: " + rs.getString("hostName"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}

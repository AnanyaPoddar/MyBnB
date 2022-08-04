package mybnb;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;

public class Reports {
    //TODO: This returns bookings with any status, including past, booked, cancelled
    public static void numBookingsByDates(Connection conn, LocalDate startDate, LocalDate endDate){
        try {
            Statement stmt = conn.createStatement();
            String sql = String.format("SELECT a.city AS city, count(a.listID) as numberBookings FROM Booked as b " +
            "JOIN addresses as a ON a.listID = b.listID WHERE startDate >= '%s' AND endDate <= '%s' GROUP BY a.city ORDER BY a.city;", startDate, endDate);
            ResultSet rs = stmt.executeQuery(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void numBookingsByDatesAndCity(Connection conn, LocalDate startDate, LocalDate endDate, String city){
        try {
            Statement stmt = conn.createStatement();
            String sql = String.format("SELECT a.city AS city, count(a.listID) as numberBookings FROM Booked as b " +
            "JOIN addresses as a ON a.listID = b.listID WHERE a.city = '%s' AND startDate >= '%s' AND endDate <= '%s' GROUP BY a.city ORDER BY a.city;", city, startDate, endDate);
            ResultSet rs = stmt.executeQuery(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void numListingsByCountry(Connection conn, String country){
        try {
            Statement stmt = conn.createStatement();
            String sql = String.format("SELECT count(listID) AS count FROM addresses WHERE country = '%s';", country);
            ResultSet rs = stmt.executeQuery(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void numListingsByCountryAndCity(Connection conn, String country, String city){
        try {
            Statement stmt = conn.createStatement();
            String sql = String.format("SELECT count(listID) AS count FROM addresses WHERE country = '%s' AND city = '%s';", country, city);
            ResultSet rs = stmt.executeQuery(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    public static void numListingsByCountryAndCityAndPostalCode(Connection conn, String country, String city, String postalCode){
        try {
            Statement stmt = conn.createStatement();
            String sql = String.format("SELECT count(listID) AS count FROM addresses WHERE country = '%s' AND city = '%s' AND postal = '%s';", country, city, postalCode);
            ResultSet rs = stmt.executeQuery(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
}

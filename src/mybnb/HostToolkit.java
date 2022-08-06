package mybnb;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DecimalFormat;

public class HostToolkit {

    //TODO: Right now this is just an ordered list of the amenities based on percentage of listings that have them
    public static void suggestedAmenities(Connection conn){
        try {
            String orderedAmenities = "SELECT name, count(*)/l.total AS percentage FROM listingsHaveAmenities JOIN (SELECT count(*) as total FROM LISTINGS) as l GROUP BY name, total ORDER BY percentage DESC;";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(orderedAmenities);
            System.out.println("Here are our recommendations for amenities you should include in your listing.");
            while(rs.next()){
                System.out.print(rs.getString("name"));
                DecimalFormat df = new DecimalFormat("0.00");
                System.out.println(" - " + df.format(rs.getFloat("percentage")*100) +"% of MyBnB listings have this amenity");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    // HostToolkit.suggestPrice(conn, type, latitude, longitude, country, city, street, postal, unitNum, amenities);
    //TODO: Include amenities
    public static void suggestPrice(Connection conn, String type, String country, String city, String street, String postal){
        //try suggesting price based on address, including postal
        //fallback to just city, then fallback to country
        try {
            Statement stmt = conn.createStatement();
            String priceByTypeCountryCityStreetPostal = String.format("SELECT avg(price) as avg FROM Availabilities AS av JOIN Addresses AS ad ON av.listID=ad.listID JOIN listings AS l ON l.listID=ad.listID WHERE country='%s' AND city='%s' AND street = '%s' AND postal = '%s' AND type = '%s';", country, city, street, postal, type);
            ResultSet rs = stmt.executeQuery(priceByTypeCountryCityStreetPostal);
            String avg;
            if(rs.next()){
                avg = rs.getString("avg");
                if(avg != null){
                    System.out.print(String.format("Suggested Price (based on avg price of %s listings in %s, %s on street %s, with postal %s): $", type, country, city, street, postal));
                    DecimalFormat df = new DecimalFormat("0.00");
                    System.out.println(df.format(Float.parseFloat(avg)));
                    return;
                }
            }
            String priceByTypeCountryCityStreet = String.format("SELECT avg(price) as avg FROM Availabilities AS av JOIN Addresses AS ad ON av.listID=ad.listID JOIN listings AS l ON l.listID=ad.listID WHERE country='%s' AND city='%s' AND street = '%s' AND type = '%s';", country, city, street, type);
            ResultSet rs2 = stmt.executeQuery(priceByTypeCountryCityStreet);
            if(rs2.next()){
                avg = rs2.getString("avg");
                if(avg != null){
                    System.out.print(String.format("Suggested Price (based on avg price of %s listings in %s, %s on street %s): $", type, country, city, street));
                    DecimalFormat df = new DecimalFormat("0.00");
                    System.out.println(df.format(Float.parseFloat(avg)));
                    return;
                }
            }
            String priceByTypeCountryCity = String.format("SELECT avg(price) as avg FROM Availabilities AS av JOIN Addresses AS ad ON av.listID=ad.listID JOIN listings AS l ON l.listID=ad.listID WHERE country='%s' AND city='%s' AND type = '%s';", country, city, type);
            ResultSet rs3 = stmt.executeQuery(priceByTypeCountryCity);
            if(rs3.next()){
                avg = rs3.getString("avg");
                if(avg != null){
                    System.out.print(String.format("Suggested Price (based on avg price of %s listings in %s, %s): $", type, country, city));
                    DecimalFormat df = new DecimalFormat("0.00");
                    System.out.println(df.format(Float.parseFloat(avg)));
                    return;
                }
            }
            String priceByTypeCountry = String.format("SELECT avg(price) as avg FROM Availabilities AS av JOIN Addresses AS ad ON av.listID=ad.listID JOIN listings AS l ON l.listID=ad.listID WHERE country='%s'  AND type = '%s';", country, type);
            ResultSet rs4 = stmt.executeQuery(priceByTypeCountry);
            if(rs4.next()){
                avg = rs4.getString("avg");
                if(avg != null){
                    System.out.print(String.format("Suggested Price (based on avg price of %s listings in %s): $", type, country));
                    DecimalFormat df = new DecimalFormat("0.00");
                    System.out.println(df.format(Float.parseFloat(avg)));
                    return;
                }
            }
            String priceByType = String.format("SELECT avg(price) as avg FROM Availabilities AS av JOIN Addresses AS ad ON av.listID=ad.listID JOIN listings AS l ON l.listID=ad.listID  AND type = '%s';", type);
            ResultSet rs5 = stmt.executeQuery(priceByType);
            if(rs5.next()){
                avg = rs5.getString("avg");
                if(avg != null){
                    System.out.print(String.format("Suggested Price (based on avg price of %s listings on MyBnB): $", type));
                    DecimalFormat df = new DecimalFormat("0.00");
                    System.out.println(df.format(Float.parseFloat(avg)));
                    return;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}

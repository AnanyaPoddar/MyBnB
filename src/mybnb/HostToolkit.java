package mybnb;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class HostToolkit {

    static DecimalFormat df = new DecimalFormat("0.00");


    //TODO: Right now this is just an ordered list of the amenities based on percentage of listings that have them
    //Should suggest an amenity, then say the increased price based on that amenity
    public static void suggestAmenities(Connection conn){
        try {
            String orderedAmenities = "SELECT name, count(*)/l.total AS percentage FROM listingsHaveAmenities JOIN (SELECT count(*) as total FROM LISTINGS) as l "+
            "GROUP BY name, total ORDER BY percentage DESC LIMIT 3;";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(orderedAmenities);
            System.out.println("Here are our top 3 recommendations for amenities you should include in your listing based on popularity.");
            while(rs.next()){
                System.out.print(rs.getString("name"));
                DecimalFormat df = new DecimalFormat("0.00");
                System.out.println(" - " + df.format(rs.getFloat("percentage")*100) +"% of MyBnB listings have this amenity");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    //This includes amenities
    public static float suggestPrice(Connection conn, String type, String country, String city, String street, String postal, List<String> amenities, boolean print){
        String formattedAmenities = "(";
        for(int i = 0; i < amenities.size(); i++){
            if(i==amenities.size()-1) formattedAmenities += String.format("'%s'", amenities.get(i));
            else formattedAmenities += String.format("'%s',", amenities.get(i));
        }
        formattedAmenities += ")";
        try {
            Statement stmt = conn.createStatement();
            String priceByTypeCountryCityStreetPostalAmenities = String.format("SELECT avg(price) as avg FROM Availabilities AS av " +
            "JOIN Addresses AS ad ON av.listID=ad.listID JOIN listings AS l ON l.listID=ad.listID "+
            "JOIN (SELECT count(*), listID FROM ListingsHaveAmenities WHERE name IN %s GROUP BY listID HAVING count(*) = %d) AS am ON am.listID=av.listID " +
            "WHERE country='%s' AND city = '%s' AND street = '%s' AND postal = '%s' AND type = '%s';", formattedAmenities, amenities.size(), country, city, street, postal, type);
            ResultSet rs = stmt.executeQuery(priceByTypeCountryCityStreetPostalAmenities);
            String avg;
            if(rs.next()){
                avg = rs.getString("avg");
                if(avg != null){
                    if(print) {
                        System.out.println(String.format("Suggested Price: $%s",df.format(Float.parseFloat(avg))));
                        System.out.println(String.format("This is based on avg price per night of %s listings in %s, %s on street %s, with postal %s with the listed amenities", type, city, country, street, postal));
                    }
                    return Float.parseFloat(avg);
                }
            }
            String priceByTypeCountryCityPostalAmenities = String.format("SELECT avg(price) as avg FROM Availabilities AS av " +
            "JOIN Addresses AS ad ON av.listID=ad.listID JOIN listings AS l ON l.listID=ad.listID "+
            "JOIN (SELECT count(*), listID FROM ListingsHaveAmenities WHERE name IN %s GROUP BY listID HAVING count(*) = %d) AS am ON am.listID=av.listID " +
            "WHERE country='%s' AND city = '%s' AND postal = '%s' AND type = '%s';", formattedAmenities, amenities.size(), country, city, postal, type);
            ResultSet rs2 = stmt.executeQuery(priceByTypeCountryCityPostalAmenities);
            if(rs2.next()){
                avg = rs2.getString("avg");
                if(avg != null){
                    if(print) {
                        System.out.println(String.format("Suggested Price: $%s",df.format(Float.parseFloat(avg))));
                        System.out.println(String.format("This is based on avg price per night of %s listings in %s, %s on postal %s with the listed amenities", type, city, country, postal));
                    }
                    return Float.parseFloat(avg);
                }
            }
            String priceByTypeCountryCityAmenities = String.format("SELECT avg(price) as avg FROM Availabilities AS av " +
            "JOIN Addresses AS ad ON av.listID=ad.listID JOIN listings AS l ON l.listID=ad.listID "+
            "JOIN (SELECT count(*), listID FROM ListingsHaveAmenities WHERE name IN %s GROUP BY listID HAVING count(*) = %d) AS am ON am.listID=av.listID " +
            "WHERE country='%s' AND city = '%s' AND type = '%s';", formattedAmenities, amenities.size(), country, city, type);
            ResultSet rs3 = stmt.executeQuery(priceByTypeCountryCityAmenities);
            if(rs3.next()){
                avg = rs3.getString("avg");
                if(avg != null){
                    if(print) {
                        System.out.println(String.format("Suggested Price: $ %s", df.format(Float.parseFloat(avg))));
                        System.out.println(String.format("This is based on avg price per night of %s listings in %s, %s with the listed amenities", type, city, country));
                    }
                    return Float.parseFloat(avg);
                }
            }
            String priceByTypeCountryAmenities = String.format("SELECT avg(price) as avg FROM Availabilities AS av " +
            "JOIN Addresses AS ad ON av.listID=ad.listID JOIN listings AS l ON l.listID=ad.listID "+
            "JOIN (SELECT count(*), listID FROM ListingsHaveAmenities WHERE name IN %s GROUP BY listID HAVING count(*) = %d) AS am ON am.listID=av.listID " +
            "WHERE country='%s' AND type = '%s';", formattedAmenities, amenities.size(),  country, type);
            ResultSet rs4 = stmt.executeQuery(priceByTypeCountryAmenities);
            if(rs4.next()){
                avg = rs4.getString("avg");
                if(avg != null){
                    if(print) {
                        System.out.println(String.format("Suggested Price: $%s",df.format(Float.parseFloat(avg))));
                        System.out.println(String.format("This is based on avg price per night of %s listings in %s with the listed amenities", type, country));
                    }
                    return Float.parseFloat(avg);
                }
            }

            String priceByTypeAmenities = String.format("SELECT avg(price) as avg FROM Availabilities AS av JOIN " +
            "(SELECT count(*), listID FROM ListingsHaveAmenities WHERE name IN %s GROUP BY listID HAVING count(*) = %d) AS am ON am.listID=av.listID "+
            "JOIN listings AS l ON l.listID=am.listID WHERE type = '%s';", formattedAmenities, amenities.size(), type);

            ResultSet rs5 = stmt.executeQuery(priceByTypeAmenities);
            if(rs5.next()){
                avg = rs5.getString("avg");
                if(avg != null){
                    if(print) {
                        System.out.println(String.format("Suggested Price: $%s",df.format(Float.parseFloat(avg))));
                        System.out.println(String.format("This is based on avg price per night of %s listings with the listed amenities", type));
                    }
                    return Float.parseFloat(avg);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    //does not include amenities, in case the host offers none    
    public static float suggestPrice(Connection conn, String type, String country, String city, String street, String postal){
        //try suggesting price based on address, including postal fallback to just city, then fallback to country
        try {
            Statement stmt = conn.createStatement();
            String priceByTypeCountryCityStreetPostal = String.format("SELECT avg(price) as avg FROM Availabilities AS av " +
            "JOIN Addresses AS ad ON av.listID=ad.listID JOIN listings AS l ON l.listID=ad.listID " +
            "WHERE country='%s' AND city='%s' AND street = '%s' AND postal = '%s' AND type = '%s';", country, city, street, postal, type);
            ResultSet rs = stmt.executeQuery(priceByTypeCountryCityStreetPostal);
            String avg;
            if(rs.next()){
                avg = rs.getString("avg");
                if(avg != null){
                    System.out.println(String.format("Suggested Price: $%s",df.format(Float.parseFloat(avg))));
                    System.out.println(String.format("This is based on avg price per night of %s listings in %s, %s on street %s, with postal %s", type, city, country, street, postal));
                    return Float.parseFloat(avg);
                }
            }
            String priceByTypeCountryCityPostal = String.format("SELECT avg(price) as avg FROM Availabilities AS av " +
            "JOIN Addresses AS ad ON av.listID=ad.listID JOIN listings AS l ON l.listID=ad.listID " + 
            "WHERE country='%s' AND city='%s' AND postal = '%s' AND type = '%s';", country, city, postal, type);
            ResultSet rs2 = stmt.executeQuery(priceByTypeCountryCityPostal);
            if(rs2.next()){
                avg = rs2.getString("avg");
                if(avg != null){
                    System.out.println(String.format("Suggested Price: $%s",df.format(Float.parseFloat(avg))));
                    System.out.println(String.format("This is based on avg price per night of %s listings in %s, %s on postal %s", type, city, country, postal));
                    return Float.parseFloat(avg);
                }
            }
            String priceByTypeCountryCity = String.format("SELECT avg(price) as avg FROM Availabilities AS av "+
            "JOIN Addresses AS ad ON av.listID=ad.listID JOIN listings AS l ON l.listID=ad.listID "+
            "WHERE country='%s' AND city='%s' AND type = '%s';", country, city, type);
            ResultSet rs3 = stmt.executeQuery(priceByTypeCountryCity);
            if(rs3.next()){
                avg = rs3.getString("avg");
                if(avg != null){
                    System.out.println(String.format("Suggested Price: $ %s", df.format(Float.parseFloat(avg))));
                    System.out.println(String.format("This is based on avg price per night of %s listings in %s, %s", type, city, country));
                    return Float.parseFloat(avg);
                }
            }
            String priceByTypeCountry = String.format("SELECT avg(price) as avg FROM Availabilities AS av " +
            "JOIN Addresses AS ad ON av.listID=ad.listID JOIN listings AS l ON l.listID=ad.listID " +
            "WHERE country='%s'  AND type = '%s';", country, type);
            ResultSet rs4 = stmt.executeQuery(priceByTypeCountry);
            if(rs4.next()){
                avg = rs4.getString("avg");
                if(avg != null){
                    System.out.println(String.format("Suggested Price: $%s",df.format(Float.parseFloat(avg))));
                    System.out.println(String.format("This is based on avg price per night of %s listings in %s", type, country));
                    return Float.parseFloat(avg);
                }
            }
            String priceByType = String.format("SELECT avg(price) as avg FROM Availabilities AS av JOIN Addresses AS ad " +
            "ON av.listID=ad.listID JOIN listings AS l ON l.listID=ad.listID  AND type = '%s';", type);
            ResultSet rs5 = stmt.executeQuery(priceByType);
            if(rs5.next()){
                avg = rs5.getString("avg");
                if(avg != null){
                    System.out.println(String.format("Suggested Price: $%s",df.format(Float.parseFloat(avg))));
                    System.out.println(String.format("This is based on avg price per night of %s listings on MyBnB", type));
                    return Float.parseFloat(avg);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }


    public static void suggestAmenitiesAndPrice(Connection conn, List<String> unchosenAmenities, List<String> chosenAmenities, float originalSuggested, String type, String country, String city, String street, String postal){
        //these offer an increase of at least $0.01, we want to order them
        List<String[]> possibleAmenities= new ArrayList<String[]>();
        for (String amenity: unchosenAmenities){
            chosenAmenities.add(amenity);
            Float price = suggestPrice(conn, type, country, city, street, postal, chosenAmenities, false);
            chosenAmenities.remove(amenity);
            Float increase = price - originalSuggested;
            if(increase > 0){
                String[] amenities = {amenity, increase.toString()};
                possibleAmenities.add(amenities);
            }
        }
        if(possibleAmenities.size() == 0){
            System.out.println("We have no recommendations for amenities to improve the price of your listing.");
            return;
        }
        possibleAmenities.sort(new Comparator<String[]>() {
            public int compare(String[] first, String[] second) {
              return  Float.compare(Float.parseFloat(second[1]), Float.parseFloat(first[1]));
            }
        });
        System.out.println();
        for(String[] possible : possibleAmenities){
            System.out.println("Adding amenity " + possible[0] + " would increase the suggested price of your listing by $" + df.format(Float.parseFloat(possible[1])));
        }
    }
}

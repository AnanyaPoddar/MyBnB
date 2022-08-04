package mybnb;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Scanner;
import java.sql.Statement;
import java.sql.ResultSet;

public class Search {
    public static void searchListings(Connection conn, Scanner myObj) {
        System.out.println("Find listings by various search/filtering methods");
        String exit = "-1";
        while (!exit.equals("0")) {
            System.out.println("------------------------------------------------------");
            System.out.println("Enter 0 to exit searches."); 
            System.out.println("Enter 1 to search nearby location."); // done
            System.out.println("Enter 2 to search nearby postal codes.");
            // Similarly the search should provide an option to rank the listings by price (ascending or descending). Does this mean every single listing can be ranked by pricing or the nearby locations one only?
            System.out.println("Enter 3 to find a listing by address."); // done
            System.out.println("Enter 4 to find listings by time availabilities.");
            System.out.println("Enter 5 to fully filter.");
            System.out.println("------------------------------------------------------");


            exit = myObj.nextLine(); 

            if(exit.equals("1"))
                locationsDistance (conn, myObj);
            if(exit.equals("2"))
                postalSearch (conn, myObj);
            if(exit.equals("3"))
                addressSearch (conn, myObj);
        }
    }

    public static void locationsDistance (Connection conn, Scanner myObj){

        System.out.println("Enter the latitude of your location (-90 to 90)");
        float latitude = Float.parseFloat(myObj.nextLine());
        if(latitude > 90 || latitude < -90){
            System.out.println("Latitude must be in a -90 to 90 range.");
            return;
        }

        System.out.println("Enter the longitude of your location (-180 to 180)");
        float longitude = Float.parseFloat(myObj.nextLine());
        if(longitude > 180 || longitude < -180){
            System.out.println("Longitude must be in a -180 to 180 range.");
            return;
        }

        int searchDistance = 50; 
        System.out.println("Enter the distance by degree from your location you want to search. Default: 50km");
        searchDistance = Integer.parseInt(myObj.nextLine());        
        // TODO if given nothing, it shouldn't error, just ""

        try {
            Statement statement = conn.createStatement();
            String listings = String.format("SELECT *, ST_Distance_Sphere(point(locations.latitude, locations.longitude), point(%f, %f))/1000 as distance FROM LOCATIONS WHERE ST_Distance_Sphere(point(locations.latitude, locations.longitude), point(%f, %f)) <= %d ORDER BY distance;", latitude, longitude, latitude, longitude, searchDistance*1000); // returns meters!, so converts to km
            ResultSet rs = statement.executeQuery(listings);
        
            // TODO What does "return all listings mean? 
            //is this enough or do i have to join with Listings etc to provide more info
            while(rs.next()){
                System.out.print("ListID: " + rs.getInt("listID"));
                System.out.print(", Latitude: " + rs.getFloat("Latitude"));
                System.out.print(", Longitude: " + rs.getFloat("Longitude"));
                System.out.println(", Distance: " + rs.getFloat("Distance"));
            }

        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public static void postalSearch (Connection conn, Scanner myObj){

    }


    public static void addressSearch (Connection conn, Scanner myObj){
        System.out.println("Provide the unit #, street, and postal code to find a listing.");

        System.out.println("Provide the unit number.");
        int unitNum = Integer.parseInt(myObj.nextLine());   
        System.out.println("Provide the listing's street name.");
        String street = myObj.nextLine();
        System.out.println("Provide the listing's postal code.");
        String postal = myObj.nextLine();

        try {
            Statement statement = conn.createStatement();
            String listing = String.format("SELECT * FROM ADDRESSES "
             + "WHERE unitNum = %d AND street = '%s' AND postal = '%s';", unitNum, street, postal); // returns meters!, so converts to km
            ResultSet rs = statement.executeQuery(listing);

            // TODO What does "return all listings mean? 
            //is this enough or do i have to join with Listings etc to provide more info
        
            while(rs.next()){
                System.out.println("ListID: " + rs.getInt("listID"));
                // System.out.print(", Latitude: " + rs.getFloat("Latitude"));
                // System.out.print(", Longitude: " + rs.getFloat("Longitude"));
                // System.out.println(", Distance: " + rs.getFloat("Distance"));
            }

        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }
    
}

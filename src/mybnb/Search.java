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
            System.out.println("Enter 2 to search nearby postal codes."); // done
            System.out.println("Enter 3 to find a listing by address."); // done
            System.out.println("Enter 4 to find listings by time availabilities.");
            System.out.println("Enter 5 to sort by price."); // done
            System.out.println("Enter 6 to fully filter.");
            System.out.println("------------------------------------------------------");


            exit = myObj.nextLine(); 

            if(exit.equals("1"))
                locationsDistance (conn, myObj);
            if(exit.equals("2"))
                postalSearch (conn, myObj);
            if(exit.equals("3"))
                addressSearch (conn, myObj);
            if(exit.equals("5"))
                sortByPrice(conn, myObj);
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
        System.out.println("Provide the listing's postal code.");
        String postal = myObj.nextLine();

        try {
            Statement statement = conn.createStatement();
            String listing = "SELECT * FROM ADDRESSES "
             + "WHERE postal LIKE '" + postal.substring(0, 6) + "%';"; 
            System.out.println(listing);
            ResultSet rs = statement.executeQuery(listing);

            // TODO What info do I need to return/display?        
            while(rs.next()){
                System.out.print("ListID: " + rs.getInt("listID"));
                System.out.println(", Postal Code: " + rs.getString("postal"));
            }

        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

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

            // TODO What info do I need to return/display?        
            while(rs.next()){
                System.out.println("ListID: " + rs.getInt("listID"));
            }

        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    public static void sortByPrice (Connection conn, Scanner myObj){

        String order = "ASC";
        System.out.println("Do you want the price to be sorted in ascending or descending? Default is Ascending. Press D for Descending");
        String choice = myObj.nextLine();
        if (choice.toLowerCase().equals("d")) {
           order = "DESC";
        }

        // TODO Is what I'm displaying okay? Should it be dates available also??
        try {
            Statement statement = conn.createStatement();
            String listing = "SELECT DISTINCT listID, price FROM availabilities ORDER BY PRICE " + order+ ";"; 
            System.out.println(listing);
            ResultSet rs = statement.executeQuery(listing);

            // TODO What info do I need to return/display? I show multiple listID but no availabilities  
            while(rs.next()){
                System.out.print("ListID: " + rs.getInt("listID"));
                System.out.println(", Price: " + rs.getFloat("price"));
            }

            System.out.println("To see on which dates the listings have the prices available, see all availabilities for a listing from the menu.");

        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }
    
}

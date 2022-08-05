package mybnb;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Scanner;
import java.sql.Statement;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

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
            if(exit.equals("6"))
                fullyFilter(conn, myObj);
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
        if(postal.length() > 10){
            System.out.println("Invalid postal code.");
            return;
        }

        try {
            Statement statement = conn.createStatement();
            String listing = "SELECT * FROM ADDRESSES "
             + "WHERE postal LIKE '" + postal.substring(0, postal.length() - 1) + "_';"; 
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
             + "WHERE unitNum = %d AND street = '%s' AND postal = '%s';", unitNum, street, postal);
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
            String listing = "SELECT DISTINCT listID, avg(price) as price FROM availabilities GROUP BY listID ORDER BY PRICE " + order+ ";"; 
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

    public static void fullyFilter (Connection conn, Scanner myObj){

        // TODO Only when we do by price and availability: all the listings have an availability. other times, doesn't necessariy have availability. that's fine i think?

        // do we need lke views or something?
        // TODO Maybe make some of these (esp availabilities) be different functions bc there's so much similarity w what's happening here and w it happening elsewhere
        // TODO have the string amenities be assigned inside if/else but the try/catch can be outside?

        // filter by postal code
        // TODO Maybe somehow make the postalSearch function be into this
        System.out.println("Would you like to filter by postal code? Y = Yes");
        String postalChoice = myObj.nextLine();
        if (postalChoice.toLowerCase().equals("y")) {
            System.out.println("Provide the listing's postal code.");
            String postal = myObj.nextLine();
            if(postal.length() > 10){
                System.out.println("Invalid postal code.");
                return;
            }

            try {
                Statement statement = conn.createStatement();
                String postalView = "CREATE OR REPLACE VIEW postalView AS SELECT listID, postal FROM ADDRESSES "
                + "WHERE postal LIKE '" + postal.substring(0, postal.length() - 1) + "_';"; 
                statement.executeUpdate(postalView);

            } catch (SQLException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        else {
            try {
                Statement statement = conn.createStatement();
                String postalView = String.format("CREATE OR REPLACE VIEW postalView AS Select listID FROM LISTINGS;"); 
                statement.executeUpdate(postalView);

            } catch (SQLException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        // price
        System.out.println("Would you like to filter by price range? Y = Yes");
        String priceChoice = myObj.nextLine();
        if (priceChoice.toLowerCase().equals("y")) {
            System.out.println("What's the minimum price in your range?"); // todo gotta input smth or it's error
            int minPrice = Integer.parseInt(myObj.nextLine());    
            System.out.println("What's the maximum price in your range?");
            int maxPrice = Integer.parseInt(myObj.nextLine());  
            // TODO We don't have to order by price this time right?
            try {
                Statement statement = conn.createStatement();
                String priceView = String.format("CREATE OR REPLACE VIEW priceView AS SELECT DISTINCT postalView.*, avg(price) as price FROM postalView JOIN availabilities ON postalView.listID = availabilities.listID WHERE price >= %d AND price <= %d GROUP BY listID;", minPrice, maxPrice); 
                System.out.println(priceView);
                statement.executeUpdate(priceView);    
            } catch (SQLException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            
        }
        else {
            try {
                Statement statement = conn.createStatement();
                String priceView = String.format("CREATE OR REPLACE VIEW priceView AS Select * FROM postalView;"); 
                statement.executeUpdate(priceView);

            } catch (SQLException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        // amenities 
        // TODO Is it okay that the same listing is there multiple times for the multiple amenities it has?
        System.out.println("Would you like to filter by amenities? Y = Yes");
        String amenitiesChoice = myObj.nextLine();
        if (amenitiesChoice.toLowerCase().equals("y")) {
            System.out.println("Choose amenities one at a time. Enter 0 to exit.");
            System.out.println("Essentials: Wifi, Kitchen, Washer");
            System.out.println("Features: Pool, Free Parking");
            System.out.println("Safety: Smoke alarm, Carbon Monoxide Alarm");
            String choice = myObj.nextLine();

            String names = "name = '0'"; // this won't bring up anything, just to keep it here
            int count = 0;
            while(!choice.equals("0")){
                names += " OR name = '" + choice + "'";
                choice = myObj.nextLine();
                count++;
            }
            System.out.println(names);
            try {
                Statement statement = conn.createStatement();
                String amenitiesView = "CREATE OR REPLACE VIEW amenitiesView AS SELECT DISTINCT priceView.*, name FROM priceView JOIN listingshaveamenities ON priceView.listID = listingshaveamenities.listID WHERE " + names + " GROUP BY priceView.listID HAVING count(priceView.listID) = " + count + ";"; 
                System.out.println(amenitiesView);
                statement.executeUpdate(amenitiesView);    
            } catch (SQLException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        else {
            try {
                Statement statement = conn.createStatement();
                String amenitiesView = String.format("CREATE OR REPLACE VIEW amenitiesView AS Select * FROM priceView;"); 
                statement.executeUpdate(amenitiesView);
    
            } catch (SQLException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        // availabilities
        System.out.println("Would you like to filter by availabilities? Y = Yes");
        String availabilitiesChoice = myObj.nextLine();
        String getListings = "";
        
        if (availabilitiesChoice.toLowerCase().equals("y")) {
            System.out.println("Start date of range: ");
            String start = myObj.nextLine();
            
            System.out.println("End date of range: ");
            String end = myObj.nextLine();
            //TODO: try-catch here
            LocalDate startDate = LocalDate.parse(start);
            LocalDate endDate = LocalDate.parse(end);

            if(!AvailabilityDAO.checkValidDates(startDate, endDate)) return;

            //get all dates in range
            List<LocalDate> dates = startDate.datesUntil(endDate.plusDays(1)).collect(Collectors.toList());
            String stringDates = "(";
            for(int i = 0; i < dates.size(); i++){
            if(i==dates.size()-1) stringDates += String.format("'%s'", dates.get(i));
            else stringDates += String.format("'%s',", dates.get(i));
            }
            stringDates  += ")";

            getListings = String.format("CREATE OR REPLACE VIEW availabilitiesView AS SELECT amenitiesview.*, listings.type " +
            "FROM (SELECT count(date) AS dateCount, listID FROM availabilities WHERE date in %s GROUP BY listID) AS a JOIN listings ON listings.listID=a.listID JOIN amenitiesview ON listings.listID=amenitiesview.listID WHERE a.dateCount = %d", stringDates, dates.size());
            

        }
        else {
            getListings = "CREATE OR REPLACE VIEW availabilitiesView AS Select * FROM amenitiesView;";
        }
        try {
            Statement statement = conn.createStatement();
            statement.executeUpdate(getListings);
      
          } catch (SQLException e) {
            e.printStackTrace();
        } 
        System.out.println(getListings);



        // TODO others? avg rating in rentersReviewListings? listing type? locations?

        // TODO
        // Print just the DISTINCT listIDs at the end + show them whole table with all repetition of listID for price and amenities

        try {
            Statement statement = conn.createStatement();
            String allListings = "Select * from availabilitiesView;";
            ResultSet rs = statement.executeQuery(allListings);

            // TODO What info do I need to return/display?        
            while(rs.next()){
                System.out.print("ListID: " + rs.getInt("listID"));
                if (postalChoice.toLowerCase().equals("y"))
                    System.out.print(", Postal: " + rs.getString("postal"));
                if (priceChoice.toLowerCase().equals("y")) 
                    System.out.print(", Price: " + rs.getFloat("price"));
                if (amenitiesChoice.toLowerCase().equals("y")) // todo not needed bc only gives 1 amenity 
                    System.out.print(", Amenities: " + rs.getString("name"));
                if (availabilitiesChoice.toLowerCase().equals("y"))    
                    System.out.print(", Type: " + rs.getString("type")); // todo not needed, just type
                System.out.println("");
            }

      
          } catch (SQLException e) {
            e.printStackTrace();
        } 
        
    }
    
}

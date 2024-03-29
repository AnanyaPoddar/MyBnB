package mybnb;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Scanner;
import java.sql.Statement;
import java.text.DecimalFormat;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

public class Search {

    static DecimalFormat df = new DecimalFormat("0.00");
    
    public static void searchListings(Connection conn, Scanner myObj) {
        String exit = "-1";
        while (!exit.equals("0")) {
            System.out.println("----------------------- Search -----------------------");
            System.out.println("0 - Exit Searches."); 
            System.out.println("1 - Search Nearby Location."); 
            System.out.println("2 - Search Nearby Postal Codes."); 
            System.out.println("3 - Find a Listing by Address."); 
            System.out.println("4 - Find Listings by Availabilities.");
            System.out.println("5 - Sort by Price."); 
            System.out.println("6 - Fully filter.");

            exit = myObj.nextLine(); 

            if(exit.equals("1"))
                locationsDistance (conn, myObj);
            if(exit.equals("2"))
                postalSearch (conn, myObj);
            if(exit.equals("3"))
                addressSearch (conn, myObj);
            if(exit.equals("4"))
                ListingDAO.getListingsAvailableBetweenDates(conn, myObj);
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
        System.out.println("Would you like to specify a distance? Y = yes. Any key = Default: 50km");
        if(myObj.nextLine().toLowerCase().equals("y")){
            System.out.println("Enter the distance in kilometers from your location you want to search.");
            searchDistance = Integer.parseInt(myObj.nextLine());
        } 

        String listings = "";
        String order = "ASC";
        System.out.println("Would you like to sort by price? Y = yes, any key = no");
        String priceChoice = myObj.nextLine();
        if(priceChoice.toLowerCase().equals("y")){
            System.out.println("Do you want the price to be sorted in ascending or descending? Default is Ascending. Press D for Descending");
            String choice = myObj.nextLine();
            if (choice.toLowerCase().equals("d")) {
                order = "DESC";
            }
            listings = String.format("SELECT DISTINCT locations.listID, latitude, longitude, ST_Distance_Sphere(point(locations.longitude, locations.latitude), point(%f, %f))/1000 as distance, avg(price) as price, addresses.* FROM locations JOIN addresses ON addresses.listID = locations.listID JOIN availabilities on availabilities.listID = locations.listID WHERE ST_Distance_Sphere(point(locations.longitude, locations.latitude), point(%f, %f)) <= %d GROUP BY locations.listID ORDER BY price %s, distance", longitude,latitude, longitude, latitude, searchDistance*1000, order );
        }
        else {
            listings = String.format("SELECT *, ST_Distance_Sphere(point(locations.longitude, locations.latitude), point(%f, %f))/1000 as distance, addresses.* FROM LOCATIONS JOIN addresses ON addresses.listID = locations.listID WHERE ST_Distance_Sphere(point(locations.longitude, locations.latitude), point(%f, %f)) <= %d ORDER BY distance;", longitude,latitude, longitude, latitude, searchDistance*1000); // returns meters!, so converts to km
        }

        try {
            Statement statement = conn.createStatement();
            ResultSet rs = statement.executeQuery(listings);
        
            System.out.println(""); 
            if(!rs.isBeforeFirst()) {
                System.out.println("No listings in " + searchDistance + "km of this location."); 
                return;
            }
            while(rs.next()){
                System.out.print("ListID: " + rs.getInt("listID"));
                System.out.print(", Latitude: " + rs.getFloat("Latitude"));
                System.out.print(", Longitude: " + rs.getFloat("Longitude"));
                System.out.println(", Distance: " + df.format(rs.getFloat("Distance")) + "km");
                int unitNum = rs.getInt("unitNum");
                System.out.println("Address: " + rs.getString("street")+ ", " + (unitNum != 0 ? "unit " + unitNum + ", " : "") + rs.getString("city") + ", " + rs.getString("country") + ", " + rs.getString("postal"));
                if(priceChoice.toLowerCase().equals("y")){
                    System.out.println("Price $" + df.format(rs.getFloat("price")));
                }
                System.out.println("");
            }

        } catch (SQLException e) {
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
            ResultSet rs = statement.executeQuery(listing);

            System.out.println(""); 
            if(!rs.isBeforeFirst()) {
                System.out.println("No listings at or near this postal code."); 
                return;
              }  
            while(rs.next()){
                int unitNum = rs.getInt("unitNum");
                System.out.println("ListID: " + rs.getInt("listID"));
                System.out.println("Address: " + rs.getString("street")+ ", " + (unitNum != 0 ? "unit " + unitNum + ", " : "") + rs.getString("city") + ", " + rs.getString("country") + ", " + rs.getString("postal") + "\n");
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

    }


    public static void addressSearch (Connection conn, Scanner myObj){
        System.out.println("Provide the unit #, street, and postal code to find a listing.");

        int unitNum = 0;
        System.out.println("Is there a unit number? Y = yes.");
        if(myObj.nextLine().toLowerCase().equals("y")){
            System.out.println("Provide the unit number.");
            unitNum = Integer.parseInt(myObj.nextLine());   
        }
        System.out.println("Provide the listing's street name.");
        String street = myObj.nextLine();
        System.out.println("Provide the listing's postal code.");
        String postal = myObj.nextLine();

        try {
            Statement statement = conn.createStatement();
            String listing = String.format("SELECT * FROM ADDRESSES "
             + "WHERE unitNum = %d AND street = '%s' AND postal = '%s';", unitNum, street, postal);
            ResultSet rs = statement.executeQuery(listing);
  
            System.out.println("");  
            if(rs.next()){
                System.out.println("ListID: " + rs.getInt("listID"));
            }
            else {
                System.out.println("No listing at that address");
            }

        } catch (SQLException e) {
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

        try {
            Statement statement = conn.createStatement();
            String listing = "SELECT ad.*, avg(price) as price FROM availabilities AS av JOIN addresses AS ad ON av.listID=ad.listID GROUP BY listID ORDER BY PRICE " + order+ ";"; 
            ResultSet rs = statement.executeQuery(listing);

            while(rs.next()){
                System.out.print("ListID: " + rs.getInt("listID"));
                System.out.println(", Price $" + df.format(rs.getFloat("price")));
                int unitNum = rs.getInt("unitNum");
                System.out.println("Address: " + rs.getString("street")+ ", " + (unitNum != 0 ? "unit " + unitNum + ", " : "") + rs.getString("city") + ", " + rs.getString("country") + ", " + rs.getString("postal"));
                System.out.println();
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    public static void fullyFilter (Connection conn, Scanner myObj){
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
                e.printStackTrace();
            }
        }
        else {
            try {
                Statement statement = conn.createStatement();
                String postalView = String.format("CREATE OR REPLACE VIEW postalView AS Select listID FROM LISTINGS;"); 
                statement.executeUpdate(postalView);

            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        // price
        System.out.println("Would you like to filter by price range? Y = Yes");
        String priceChoice = myObj.nextLine();
        if (priceChoice.toLowerCase().equals("y")) {
            System.out.println("What's the minimum price in your range?"); 
            int minPrice = Integer.parseInt(myObj.nextLine());    
            System.out.println("What's the maximum price in your range?");
            int maxPrice = Integer.parseInt(myObj.nextLine());  
            try {
                Statement statement = conn.createStatement();
                String priceView = String.format("CREATE OR REPLACE VIEW priceView AS SELECT DISTINCT postalView.*, avg(price) as price FROM postalView JOIN availabilities ON postalView.listID = availabilities.listID GROUP BY listID HAVING avg(price) >= %d AND avg(price) <= %d ;", minPrice, maxPrice); 
                statement.executeUpdate(priceView);    
            } catch (SQLException e) {
                e.printStackTrace();
            }
            
        }
        else {
            try {
                Statement statement = conn.createStatement();
                String priceView = String.format("CREATE OR REPLACE VIEW priceView AS Select * FROM postalView;"); 
                statement.executeUpdate(priceView);

            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        // amenities 
        System.out.println("Would you like to filter by amenities? Y = Yes");
        String amenitiesChoice = myObj.nextLine();
        if (amenitiesChoice.toLowerCase().equals("y")) {
            System.out.println("Choose amenities. Enter 0 to exit.");
            System.out.println("Essentials: Wifi, Kitchen");
            System.out.println("Features: Pool, Free Parking");
            System.out.println("Safety: Smoke Alarm, Carbon Monoxide Alarm");
            String choice = myObj.nextLine();

            String names = "name = '0'"; // this won't bring up anything, just to keep it here
            int count = 0;
            while(!choice.equals("0")){
                names += " OR name = '" + choice + "'";
                choice = myObj.nextLine();
                count++;
            }
            try {
                Statement statement = conn.createStatement();
                String amenitiesView = "CREATE OR REPLACE VIEW amenitiesView AS SELECT DISTINCT priceView.* FROM priceView JOIN listingshaveamenities ON priceView.listID = listingshaveamenities.listID WHERE " + names + " GROUP BY priceView.listID HAVING count(priceView.listID) = " + count + ";"; 
                statement.executeUpdate(amenitiesView);    
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        else {
            try {
                Statement statement = conn.createStatement();
                String amenitiesView = String.format("CREATE OR REPLACE VIEW amenitiesView AS Select * FROM priceView;"); 
                statement.executeUpdate(amenitiesView);
    
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        // availabilities
        System.out.println("Would you like to filter by availabilities? We only return listings that are available for the whole range. Y = Yes");
        String availabilitiesChoice = myObj.nextLine();
        String getListings = "";
        
        if (availabilitiesChoice.toLowerCase().equals("y")) {
            System.out.println("Start date of range: ");
            String start = myObj.nextLine();
            
            System.out.println("End date of range: ");
            String end = myObj.nextLine();
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

        try {
            Statement statement = conn.createStatement();
            String allListings = "Select * from availabilitiesView JOIN addresses on addresses.listID = availabilitiesView.listID;";
            ResultSet rs = statement.executeQuery(allListings);

            System.out.println(""); 
            if(!rs.isBeforeFirst()) {
                System.out.println("No listings available with these filters."); 
                return;
            }
            while(rs.next()){
                System.out.print("ListID: " + rs.getInt("listID"));
                if (postalChoice.toLowerCase().equals("y"))
                    System.out.print(", Postal: " + rs.getString("postal"));
                if (priceChoice.toLowerCase().equals("y")) 
                    System.out.print(", Price $" + df.format(rs.getFloat("price")));
                if (availabilitiesChoice.toLowerCase().equals("y"))    
                    System.out.print(", Type: " + rs.getString("type")); 
                System.out.println("");
                int unitNum = rs.getInt("unitNum");
                System.out.println("Address: " + rs.getString("street")+ ", " + (unitNum != 0 ? "unit " + unitNum + ", " : "") + rs.getString("city") + ", " + rs.getString("country") + ", " + rs.getString("postal")+ "\n");
                
            }

          } catch (SQLException e) {
            e.printStackTrace();
        } 
        
    }
    
}
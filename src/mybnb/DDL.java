package mybnb;

import java.sql.Connection;
import java.sql.Statement;

public class DDL {

    public static void createTables(Connection conn, Statement stmt){

        try {
            String userTable = "CREATE TABLE IF NOT EXISTS USER " + "(SIN INT NOT NULL PRIMARY KEY "
                + " CONSTRAINT CK_SIN_LENGTH check (length(SIN) = 9), upassword VARCHAR(12) NOT NULL, "
                + " uname VARCHAR(100) NOT NULL UNIQUE, " + " uaddress VARCHAR(100), "
                + " uoccupation VARCHAR(20), " + " uDOB DATE)";

            String hostTable = "CREATE TABLE IF NOT EXISTS HOST "
                + "(HostSIN INT NOT NULL PRIMARY KEY,"
                + "FOREIGN KEY (HostSIN) REFERENCES USER(SIN) ON DELETE CASCADE)";

            String renterTable = "CREATE TABLE IF NOT EXISTS RENTER "
                + "(RenterSIN INT NOT NULL PRIMARY KEY,"
                + " cardType VARCHAR(12) NOT NULL, " 
                + " cardNum varchar(16) NOT NULL CONSTRAINT CK_cardNum_LENGTH check (length(cardNum) = 16), "
                + "FOREIGN KEY (RenterSIN) REFERENCES USER(SIN) ON DELETE CASCADE)";

            String listingTable = "CREATE TABLE IF NOT EXISTS Listings "
                + "(listID INT NOT NULL AUTO_INCREMENT PRIMARY KEY, "
                + "type VARCHAR(10) NOT NULL)";

            String hostsToListingTable = "CREATE TABLE IF NOT EXISTS HostsToListings "
                + "(listID INT NOT NULL, FOREIGN KEY (listID) REFERENCES Listings(listID) ON DELETE CASCADE, "
                + "hostSIN INT NOT NULL, FOREIGN KEY (hostSIN) REFERENCES Host(hostSIN) ON DELETE CASCADE, " +
                " PRIMARY KEY(listID))";

            //status options for availabilities: booked, available, past, cancelled
            String availabilitiesTable = "CREATE TABLE IF NOT EXISTS Availabilities "
                + "(date DATE NOT NULL, listID INT NOT NULL, price FLOAT NOT NULL check (price >= 0), " +
                "status varchar(11) NOT NULL default 'available', " +
                "FOREIGN KEY (listID) REFERENCES Listings(listID) ON DELETE CASCADE, " +
                "PRIMARY KEY(listID, date) )";

            String bookedTable = "CREATE TABLE IF NOT EXISTS Booked "
                + "(listID INT NOT NULL, FOREIGN KEY (listID) REFERENCES Listings(listID) ON DELETE CASCADE, "
                + "renterSIN INT NOT NULL, FOREIGN KEY (renterSIN) REFERENCES Renter(renterSIN) ON DELETE CASCADE, cost FLOAT NOT NULL check (cost >= 0), " 
                + "startDate DATE NOT NULL, endDate DATE NOT NULL, status varchar(10) NOT NULL DEFAULT 'booked', PRIMARY KEY(listID, startDate, endDate, status))";

            String rentersReviewHosts = "CREATE TABLE IF NOT EXISTS rentersReviewHosts "
                + "(hostSIN INT NOT NULL, FOREIGN KEY (hostSIN) REFERENCES Host(hostSIN) ON DELETE CASCADE, "
                + "renterSIN INT NOT NULL, FOREIGN KEY (renterSIN) REFERENCES Renter(renterSIN) ON DELETE CASCADE," + 
                " review VARCHAR(100) NOT NULL, " + " rating INT NOT NULL CONSTRAINT CK_rating  check (rating >= 1 and rating <= 5), " +
                "PRIMARY KEY(hostSIN, renterSIN))";

            String hostsReviewRenters = "CREATE TABLE IF NOT EXISTS hostsReviewRenters "
                + "(hostSIN INT NOT NULL, FOREIGN KEY (hostSIN) REFERENCES Host(hostSIN) ON DELETE CASCADE, "
                + "renterSIN INT NOT NULL, FOREIGN KEY (renterSIN) REFERENCES Renter(renterSIN) ON DELETE CASCADE," + 
                " review VARCHAR(100) NOT NULL, " + " rating INT NOT NULL CONSTRAINT CK_rating2  check (rating >= 1 and rating <= 5), " +
                "PRIMARY KEY(hostSIN, renterSIN))";

            // Must provide both rating and comment when providing a review
            String rentersReviewListings = "CREATE TABLE IF NOT EXISTS rentersReviewListings "
                + "(listID INT NOT NULL, FOREIGN KEY (listID) REFERENCES Listings(listID) ON DELETE CASCADE, "
                + "renterSIN INT NOT NULL, FOREIGN KEY (renterSIN) REFERENCES Renter(renterSIN) ON DELETE CASCADE," + 
                " review VARCHAR(100) NOT NULL, " + " rating INT NOT NULL CONSTRAINT CK_rating3 check (rating >= 1 and rating <= 5), " +
                "PRIMARY KEY(listID, renterSIN))";

            String locationsTable = "CREATE TABLE IF NOT EXISTS Locations "
                + "(listID INT NOT NULL, FOREIGN KEY (listID) REFERENCES Listings(listID) ON DELETE CASCADE, " 
                + "latitude FLOAT NOT NULL CONSTRAINT CK_latitude  check (latitude >= -90 and latitude <= 90)," + 
                " longitude FLOAT NOT NULL CONSTRAINT CK_longitude check (longitude >= -180 and longitude <= 180), " +
                "PRIMARY KEY(listID))";

            String addressesTable =
                "CREATE TABLE IF NOT EXISTS ADDRESSES " 
                + "(listID INT NOT NULL UNIQUE, FOREIGN KEY (listID) REFERENCES Listings(listID) ON DELETE CASCADE, " 
                + "unitNum INT NOT NULL, "
                + " street VARCHAR(50) NOT NULL, "
                + " city VARCHAR(25) NOT NULL, " 
                + " country VARCHAR(100) NOT NULL, "
                + " postal VARCHAR(10) NOT NULL, PRIMARY KEY (unitNum, street, postal))";

            String amenitiesTable = "CREATE TABLE IF NOT EXISTS AMENITIES "
                + "(name VARCHAR(50) NOT NULL PRIMARY KEY,"
                + "type VARCHAR(50) NOT NULL)";
            
            String ListingsHaveAmenities = "CREATE TABLE IF NOT EXISTS ListingsHaveAmenities "
                + "(listID INT NOT NULL, name VARCHAR(50) NOT NULL, " +
                "FOREIGN KEY (name) REFERENCES AMENITIES(name) ON DELETE CASCADE, " + 
                "FOREIGN KEY (listID) REFERENCES Listings(listID) ON DELETE CASCADE, " +
                "PRIMARY KEY(name, listID) )";
            
            // used for noun phrase word cloud
            String npReviews = "CREATE TABLE IF NOT EXISTS npReviews "
                + "(nounPhrase VARCHAR(100) NOT NULL)";
                
            stmt.executeUpdate(userTable);
            stmt.executeUpdate(hostTable);
            stmt.executeUpdate(renterTable);
            stmt.executeUpdate(listingTable);
            stmt.executeUpdate(hostsToListingTable);
            stmt.executeUpdate(availabilitiesTable);
            stmt.executeUpdate(bookedTable);
            stmt.executeUpdate(rentersReviewHosts);
            stmt.executeUpdate(hostsReviewRenters);
            stmt.executeUpdate(rentersReviewListings);
            stmt.executeUpdate(locationsTable);    
            stmt.executeUpdate(addressesTable);
            stmt.executeUpdate(amenitiesTable);
            stmt.executeUpdate(ListingsHaveAmenities);
            stmt.executeUpdate(availabilitiesTable);
            stmt.executeUpdate(npReviews);
            
        }      
        catch(Exception e){
            System.err.println("Connection error occured!");
        }
    }
}

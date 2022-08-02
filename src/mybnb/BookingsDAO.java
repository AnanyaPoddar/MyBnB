package mybnb;

import java.sql.Connection;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;

public class BookingsDAO {

    //Under assumption that they can enter a range of date for the booking, 
    public static void addBooking(Connection conn, Scanner myObj){
        System.out.println("Enter the id of the listing you'd like to book.\n");
        int listingID = Integer.parseInt(myObj.nextLine());
        //TODO: - Explain constraint in the ER diagram, only a logged-in renter can book something
        //Check that user is a logged-in renter
        if(!UserDAO.verifyUserInTable(conn, DAO.loggedInUser, "renterSIN", "Renter")){
            System.out.println("You must be logged in as a renter.");
            return;
        }

        System.out.println("Enter start date of your booking: ");
        String start = myObj.nextLine();
        System.out.println("Enter end date of your booking: ");
        String end = myObj.nextLine();
        LocalDate startDate = LocalDate.parse(start);
        LocalDate endDate = LocalDate.parse(end);

        try {
            //check that the dates are all available for booking; if so, remove them from availabilities, and add to booked
            if(!AvailabilityDAO.checkListingAvailabilityOnDates(conn, startDate, endDate, listingID)) return;
            List<LocalDate> dates = startDate.datesUntil(endDate.plusDays(1)).collect(Collectors.toList());
            Statement statement = conn.createStatement();
            for(LocalDate date: dates){
                //Add to bookings table
                Date.valueOf(date);
                String bookingInsert = String.format(
                  "INSERT INTO Booked(listID, renterSIN, date) VALUES (%d, %d, '%s');", listingID, DAO.loggedInUser, date);
                      statement.executeUpdate(bookingInsert);
            }
            //remove from availabilities table only after successful booking;
            AvailabilityDAO.deleteAvailabilities(conn, listingID, startDate, endDate);
            System.out.println("Successfully added booking for listing with id " + listingID + " between dates " + startDate.toString() + " and " + endDate.toString());
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }   



    public static void hostCancelsBooking(){
        //TODO: Check logic - if a host cancels a booking, it is removed from the table, and removed from availabilities as well
        //However, if a user cancels a booking, it becomes available again for someone else to book so needs to be readded to availabilities table
        
    }

    public static void userCancelsBooking(){

    }

    
}

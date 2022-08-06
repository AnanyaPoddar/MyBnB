package mybnb;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.Period;
import java.util.Scanner;

public class UserDAO {
    
    public static boolean verifyUserInTable(Connection conn, int ID, String role,
      String table) {
        try {
        Statement stmt = conn.createStatement();
        String sql = "SELECT * FROM " + table + " WHERE " + role + " = " + ID + ";";
        ResultSet rs = stmt.executeQuery(sql);
        if (rs.next()) {
            return true;
        }
        return false;
        } 
        catch (SQLException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
        }
        
        return false;
    }

  public static void addUser(Connection conn, Scanner myObj) {

    // TODO Prevent errors from database and instead check user input before
    // trying to insert

    // TODO: Verify password and email
    System.out.println("Provide your username: ");
    String name = myObj.nextLine();
    System.out.println("Provide you password (masked for security): ");
    String password = String.valueOf(System.console().readPassword());
    System.out.println("Provide your SIN: ");
    int sin = Integer.parseInt(myObj.nextLine()); // Read user input
    if (sin <= 0){
        System.out.println("The SIN must be positive integer.");
        return;
    }
    System.out.println("Provide your address: ");
    String addr = myObj.nextLine();
    System.out.println("Provide your occupation: ");
    String occupation = myObj.nextLine();
    System.out.println("Provide your date of birth in YYYY-MM-DD format");
    String strBirthdate = myObj.nextLine();
    LocalDate birthdate = LocalDate.parse(strBirthdate);

    // Verify user is >= 18 years old
    LocalDate today = LocalDate.now();
    try {
      Period p = Period.between(birthdate, today);
      if (p.getYears() < 18) {
        System.out.println("You must be 18 years old to sign up");
        return;
      }
    } catch (java.time.DateTimeException e) {
      // TODO Replace with error
      e.printStackTrace();
      return;
    }

    System.out.println("Are you a renter or a host? R = Renter, any other key = Host ");
    String rOrH = myObj.nextLine();

    String rentOrHostInsert;
    if (rOrH.equals("r") || rOrH.equals("R")) {
      
      // TODO Should this be open choice or C = Credit, D = Debit
      System.out.println("Provide a payment method (Credit or Debit): ");
      String cardType = myObj.nextLine();
      System.out.println("Provide your card number: ");
      String cardNum= myObj.nextLine();
      rentOrHostInsert = String.format(
          "INSERT INTO Renter VALUES (%d, '%s', '%s');", sin, cardType, cardNum);
    } else {
      rentOrHostInsert = String.format("INSERT INTO Host VALUES (%d);", sin);
    }
    try {
      // TODO Either both statements are inserted or neither is?
      Statement insert = conn.createStatement();
      String userInsert = String.format(
          "INSERT INTO USER VALUES (%d, '%s', '%s', '%s', '%s', '%s');", sin,
          password, name, addr, occupation, birthdate);
      insert.executeUpdate(userInsert);
      insert.executeUpdate(rentOrHostInsert);
    } catch (SQLException e) {
      // TODO Auto-generated catch block [replace with generic error message]
      e.printStackTrace();
    }
  }


  public static void login(Connection conn, Scanner myObj) {
    if (Main.loggedInUser != -1) {
      System.out.println("You're already logged in as: " + Main.loggedInUser);
      return;
    }
    System.out.println("Provide your username: ");
    String uname = myObj.nextLine();
    System.out.println("Provide you password (masked for security): ");
    String password = String.valueOf(System.console().readPassword());

    try {
      Statement stmt = conn.createStatement();
      String sql = "SELECT * FROM User WHERE uname = '" + uname + "';";
      ResultSet rs = stmt.executeQuery(sql);

      if (rs.next()) {
        if (password.equals(rs.getString("upassword"))) {
          Main.loggedInUser = rs.getInt("SIN");
          //Probably don't show their SIN, just show the name
          System.out.println("Succesfully logged in as: " + rs.getString("uname"));
        } else {
          System.out.println("Wrong password.");

        }
      } else {
        System.out.println("No user of that username exists.");
      }
      rs.close();
    } catch (SQLException e) {
      e.printStackTrace();
    }

  }


  public static void viewAllUsers(Connection conn) {
    try {
      Statement stmt = conn.createStatement();
      String sql = "SELECT * FROM User;";
      ResultSet rs = stmt.executeQuery(sql);
      // Extract results
      while (rs.next()) {
        // Retrieve by column name
        int sid = rs.getInt("SIN");
        String uname = rs.getString("uname");
        String uaddress = rs.getString("uaddress");
        String uoccupation = rs.getString("uoccupation");
        String udob = rs.getString("uDOB");

        // Display values
        System.out.print("ID: " + sid);
        System.out.print(", Name: " + uname);
        System.out.print(", Address: " + uaddress);
        System.out.print(", Occupation: " + uoccupation);
        System.out.println(", Date of Birth: " + udob);
      }
      rs.close();
    } catch (SQLException e) {
      e.printStackTrace();
    }

  }


  public static void deleteUser(Connection conn, Scanner myObj) {
    // TODO ! What other tables/relationships should be affected if a Host
    // deletes? If a Renter deletes?s

    if (Main.loggedInUser == -1) {
      System.out.println("You must be logged in to delete your account");
      return;
    }

    System.out.println("Press 1 to indicate you want to delete your account. Any other number otherwise ");
    int confirm = Integer.parseInt(myObj.nextLine());
    if (confirm != 1) {
      System.out.println("Not deleting.");
      return;
    }
    System.out.println("Deleting Account of" + Main.loggedInUser);

    try {

      Statement stmt = conn.createStatement();
      String sql = "DELETE FROM user WHERE SIN = " + Main.loggedInUser + ";";
      stmt.executeUpdate(sql);

      Main.loggedInUser = -1;
      System.out.println("Account deleted and logged out." + Main.loggedInUser);
    } catch (SQLException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }
 
  public static void logout() {
    if (Main.loggedInUser == -1) {
      System.out.println("You're already logged out.");
      return;
    }
    Main.loggedInUser = -1;
    System.out.println("You've been logged out");
  }

  // TODO For example you cannot comment on a listing if you haven’t rented it recently.
  public static void renterReviewsHost(Connection conn, Scanner myObj){
    // TODO We're assuming one Listing per Host right?

    // Input a Host 
    // TODO How will this be chosen during an actual workflow lol. Maybe we'll 
    // display all hosts' id and then be like which one would you like to review?
    System.out.println("Provide the SIN of the Host you'd like to review: ");
    int hostSIN = Integer.parseInt(myObj.nextLine());
    // Have you already left a review for this host?
    try {
        Statement stmt = conn.createStatement();
        String sql = "SELECT * FROM rentersReviewHosts WHERE renterSIN = " + Main.loggedInUser + " AND hostSIN = " + hostSIN + ";";
        ResultSet rs = stmt.executeQuery(sql);
        if (rs.next()) {
            System.out.println("You've already left a review for this host.");
            return;
        }
    } 
    catch (SQLException e) {
        e.printStackTrace();
    }
    // Have you booked something from the host? (Join Booked & Hosts-Listing)
    try {
        Statement stmt = conn.createStatement();
        String sql = "SELECT * FROM Booked JOIN HostsToListings ON Booked.listID = HostsToListings.listID WHERE renterSIN = " + Main.loggedInUser + " AND hostSIN = " + hostSIN + ";";
        ResultSet rs = stmt.executeQuery(sql);
        if (rs.next()) {
            System.out.println("You've booked from them before.");
        }
        else{
            System.out.println("Cannot leave a review because you've never booked from them before or the hostSIN doesn't exist.");
            return;
        }
      } 
      catch (SQLException e) {
          e.printStackTrace();
      }

    // Leave a review and rating 
    System.out.println("What is your rating for the host out of 5? ");
    int rating = Integer.parseInt(myObj.nextLine());
    if(rating > 5 || rating < 1){
        System.out.println("Rating can only be 1-5.");
        return;
    }
    System.out.println("Provide you comment about the host: ");
    String comment = myObj.nextLine();
    if(comment.length() > 100){
        System.out.println("Comment is too long. Must be 100 characters or less.");
        return;
    }

    // insert into renterReviewsHost
    try {
        Statement insert = conn.createStatement();
        String reviewInsert = String.format(
            "INSERT INTO rentersReviewHosts VALUES (%d, %d, '%s', %d);", hostSIN,
            Main.loggedInUser, comment, rating);
  
        System.out.println(reviewInsert);
        insert.executeUpdate(reviewInsert);
      } catch (SQLException e) {
        e.printStackTrace();
      }


  }

  // TODO For example you cannot comment on a listing if you haven’t rented it recently.
  public static void hostReviewsRenter(Connection conn, Scanner myObj){
    // TODO We're assuming one Listing per Host right?

    // Input a renter 
    // TODO How will this be chosen during an actual workflow lol. Maybe we'll 
    // display all renters' id and then be like which one would you like to review?
    System.out.println("Provide the SIN of the Renter you'd like to review: ");
    int renterSIN = Integer.parseInt(myObj.nextLine());

    // Have you already left a review for this renter?
    try {
        Statement stmt = conn.createStatement();
        String sql = "SELECT * FROM hostsReviewRenters WHERE renterSIN = " + renterSIN + " AND hostSIN = " + Main.loggedInUser + ";";
        ResultSet rs = stmt.executeQuery(sql);
        if (rs.next()) {
            System.out.println("You've already left a review for this renter.");
            return;
        }
    } 
    catch (SQLException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
    }


    // Have you rented something to the renter? (Join Booked & Hosts-Listing)
    try {
      Statement stmt = conn.createStatement();
      String sql = "SELECT * FROM Booked JOIN HostsToListings ON Booked.listID = HostsToListings.listID WHERE renterSIN = " + renterSIN + " AND hostSIN = " + Main.loggedInUser + ";";
      ResultSet rs = stmt.executeQuery(sql);
      if (rs.next()) {
          System.out.println("They've booked from you before.");
      }
      else{
          System.out.println("Cannot leave a review because you've never rented them a listing before or the renterID doesn't exist.");
          return;
      }
    } 
    catch (SQLException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
    }

    // Leave a review and rating 
    System.out.println("What is your rating for the renter out of 5? ");
    int rating = Integer.parseInt(myObj.nextLine());
    if(rating > 5 || rating < 1){
        System.out.println("Rating can only be 1-5.");
        return;
    }
    System.out.println("Provide you comment about the renter: ");
    String comment = myObj.nextLine();
    if(comment.length() > 100){
        System.out.println("Comment is too long. Must be 100 characters or less.");
        return;
    }

    // Insert into
    try {
      Statement insert = conn.createStatement();
      String reviewInsert = String.format(
          "INSERT INTO hostsReviewRenters VALUES (%d, %d, '%s', %d);",
          Main.loggedInUser, renterSIN, comment, rating);

      System.out.println(reviewInsert);
      insert.executeUpdate(reviewInsert);
    } catch (SQLException e) {
      // TODO Auto-generated catch block [replace with generic error message]
      e.printStackTrace();
    }
  }

  // TODO For example you cannot comment on a listing if you haven’t rented it recently.
  public static void rentersReviewListings(Connection conn, Scanner myObj){
    // TODO We're assuming one Listing per Host right?

    // Input a Listing 
    // TODO How will this be chosen during an actual workflow lol. Maybe we'll 
    // display all hosts' id and then be like which one would you like to review?
    System.out.println("Provide the ID of the Listing you'd like to review: ");
    int listID = Integer.parseInt(myObj.nextLine());

    // Have you reviewed this Listing already?
    try {
      Statement stmt = conn.createStatement();
      String sql = "SELECT * FROM rentersReviewListings WHERE renterSIN = " + Main.loggedInUser + " AND listID = " + listID + ";";
      ResultSet rs = stmt.executeQuery(sql);
      if (rs.next()) {
          System.out.println("You've already left a review for this Listing.");
          return;
      }
    } 
    catch (SQLException e) {
        e.printStackTrace();
    }

    // Have you Booked the listing before?
    try {
      Statement stmt = conn.createStatement();
      String sql = "SELECT * FROM Booked WHERE renterSIN = " + Main.loggedInUser + " AND listID = " + listID + ";";
      ResultSet rs = stmt.executeQuery(sql);
      if (rs.next()) {
          System.out.println("You've booked this listing before.");
      }
      else{
          System.out.println("Cannot leave a review because you've never booked this listing before or it doesn't exist.");
          return;
      }
    } 
    catch (SQLException e) {
        e.printStackTrace();
    }


    // Provide review and rating
    System.out.println("What is your rating for the listing out of 5? ");
    int rating = Integer.parseInt(myObj.nextLine());
    if(rating > 5 || rating < 1){
        System.out.println("Rating can only be 1-5.");
        return;
    }
    System.out.println("Provide you comment about the listing: ");
    String comment = myObj.nextLine();
    if(comment.length() > 100){
        System.out.println("Comment is too long. Must be 100 characters or less.");
        return;
    }
    // Insert review and rating
    try {
      Statement insert = conn.createStatement();
      String reviewInsert = String.format(
          "INSERT INTO rentersReviewListings VALUES (%d, %d, '%s', %d);", listID,
          Main.loggedInUser, comment, rating);
      insert.executeUpdate(reviewInsert);
    } catch (SQLException e) {
      e.printStackTrace();
    }



  }


}
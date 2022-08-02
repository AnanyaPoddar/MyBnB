package mybnb;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Scanner;
import java.time.LocalDate;
import java.time.Period;

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

    System.out.println("Provide your SIN");
    int sin = Integer.parseInt(myObj.nextLine()); // Read user input
    // Verify this SIN doesn't already exist
    if (verifyUserInTable(conn, sin, "SIN", "User")) {
      System.out.println("Sorry, there is already an account with this SIN");
      return;
    }
    if (sin <= 0){
        System.out.println("The SIN must be positive integer.");
        return;
    }

    System.out.println("Provide a password");
    String password = myObj.nextLine();
    System.out.println("Provide your name");
    String name = myObj.nextLine();
    System.out.println("Provide your address");
    String addr = myObj.nextLine();
    System.out.println("Provide your occupation");
    String occupation = myObj.nextLine();
    System.out.println("Provide your year of birth");
    int yob = Integer.parseInt(myObj.nextLine());
    System.out.println("Provide your month of birth (in integer; January = 1)");
    int mob = Integer.parseInt(myObj.nextLine());
    System.out.println("Provide your day of birth in YYYY-MM-DD format");
    int dob = Integer.parseInt(myObj.nextLine());

    // Verify user is >= 18 years old
    // TODO Parse the localDate how Ananya did in AVailabilities (won't need seperate year/month/day then)
    LocalDate today = LocalDate.now();
    try {
      LocalDate birthday = LocalDate.of(yob, mob, dob);
      Period p = Period.between(birthday, today);
      System.out.println("You are " + p.getYears() + " years.");
      if (p.getYears() < 18) {
        System.out.println("You must be 18 years old to sign up");
        return;
      }
    } catch (java.time.DateTimeException e) {
      // TODO Replace with error
      e.printStackTrace();
      return;
    }

    System.out.println(
        "Are you a renter or a host? R = Renter, any other key = Host");
    String rOrH = myObj.nextLine();

    String rentOrHostInsert;
    if (rOrH.equals("r") || rOrH.equals("R")) {
      // " cardType VARCHAR(12) NOT NULL, " + " cardNum INT NOT NULL, "
      // TODO Should this be open choice or C = Credit, D = Debit
      System.out.println("Provide a payment method (Credit or Debit)");
      String cardType = myObj.nextLine();
      System.out.println("Provide your card Number");
      int cardNum = Integer.parseInt(myObj.nextLine()); // Read user input
      rentOrHostInsert = String.format(
          "INSERT INTO Renter VALUES (%d, '%s', %d);", sin, cardType, cardNum);
    } else {
      rentOrHostInsert = String.format("INSERT INTO Host VALUES (%d);", sin);
    }

    System.out.println(rentOrHostInsert);


    try {
      // TODO Either both statements are inserted or neither is?
      Statement insert = conn.createStatement();
      String userInsert = String.format(
          "INSERT INTO USER VALUES (%d, '%s', '%s', '%s', '%s', '%s');", sin,
          password, name, addr, occupation, String.valueOf(yob) + "-"
              + String.valueOf(mob) + "-" + String.valueOf(dob));

      System.out.println(userInsert);
      insert.executeUpdate(userInsert);
      insert.executeUpdate(rentOrHostInsert);
    } catch (SQLException e) {
      // TODO Auto-generated catch block [replace with generic error message]
      e.printStackTrace();
    }
  }


  public static void login(Connection conn, Scanner myObj) {

    if (DAO.loggedInUser != -1) {
      System.out.println("You're already logged in as: " + DAO.loggedInUser);
      return;
    }
    System.out.println("Provide your SIN");
    int SIN = Integer.parseInt(myObj.nextLine());
    System.out.println("Provide you password");
    String password = myObj.nextLine();

    try {
      Statement stmt = conn.createStatement();
      String sql = "SELECT * FROM User WHERE SIN = " + SIN + ";";
      ResultSet rs = stmt.executeQuery(sql);

      if (rs.next()) {
        if (password.equals(rs.getString("upassword"))) {
          DAO.loggedInUser = SIN;
          System.out.println("Succesfully logged in as: " + DAO.loggedInUser);

        } else {
          System.out.println("Wrong password.");

        }
      } else {
        System.out.println("No user of that SIN exists.");
      }
      rs.close();
    } catch (SQLException e) {
      // TODO Auto-generated catch block
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
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

  }


  public static void deleteUser(Connection conn, Scanner myObj) {
    // TODO ! What other tables/relationships should be affected if a Host
    // deletes? If a Renter deletes?s

    if (DAO.loggedInUser == -1) {
      System.out.println("You must be logged in to delete your account");
      return;
    }

    System.out.println("Press 1 to indicate you want to delete your account. Any other key otherwise.");
    int confirm = Integer.parseInt(myObj.nextLine());
    if (confirm != 1) {
      System.out.println("Not deleting.");
      return;
    }
    System.out.println("Deleting Account of" + DAO.loggedInUser);

    try {

      Statement stmt = conn.createStatement();
      String sql = "DELETE FROM user WHERE SIN = " + DAO.loggedInUser + ";";
      stmt.executeUpdate(sql);

      DAO.loggedInUser = -1;
      System.out.println("Account deleted and logged out." + DAO.loggedInUser);
    } catch (SQLException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }

 
  public static void logout() {
    if (DAO.loggedInUser == -1) {
      System.out.println("You're already logged out.");
      return;
    }

    DAO.loggedInUser = -1;
    System.out.println("You've been logged out");
  }

  public static void renterReviewsHost(Connection conn, Scanner myObj){
    // TODO We're assuming one Listing per Host right?


    // Are you logged in as a renter?
    if(!verifyUserInTable(conn, DAO.loggedInUser, "renterSIN",
    "Renter")){
        System.out.println("You must be logged in as a renter.");
        return;
    }

    // Input a Host 
    // TODO How will this be chosen during an actual workflow lol. Maybe we'll 
    // display all hosts' id and then be like which one would you like to review?
    System.out.println("Provide the SIN of the Host you'd like to review");
    int hostSIN = Integer.parseInt(myObj.nextLine());

    // Have you already left a review for this host?
    try {
        Statement stmt = conn.createStatement();
        String sql = "SELECT * FROM rentersReviewHosts WHERE renterSIN = " + DAO.loggedInUser + " AND hostSIN = " + hostSIN + ";";
        ResultSet rs = stmt.executeQuery(sql);
        if (rs.next()) {
            System.out.println("You've already left a review for this host.");
            return;
        }
    } 
    catch (SQLException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
    }


    // Have you booked something from the host? (Join Booked & Hosts-Listing)
    try {
        Statement stmt = conn.createStatement();
        String sql = "SELECT * FROM Booked JOIN HostsToListings ON Booked.listID = HostsToListings.listID WHERE renterSIN = " + DAO.loggedInUser + " AND hostSIN = " + hostSIN + ";";
        ResultSet rs = stmt.executeQuery(sql);
        if (rs.next()) {
            System.out.println("You've booked from them before.");
        }
        else{
            System.out.println("Cannot leave a review because you've never booked from them before.");
            return;
        }
    } 
    catch (SQLException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
    }

    // Leave a review and rating 
    System.out.println("What is your rating for the host out of 5?");
    int rating = Integer.parseInt(myObj.nextLine());
    if(rating > 5 || rating < 1){
        System.out.println("Rating can only be 1-5.");
        return;
    }
    System.out.println("Provide you comment about the host.");
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
            DAO.loggedInUser, comment, rating);
  
        System.out.println(reviewInsert);
        insert.executeUpdate(reviewInsert);
      } catch (SQLException e) {
        // TODO Auto-generated catch block [replace with generic error message]
        e.printStackTrace();
      }


  }

  
  public static void hostReviewsRenter(Connection conn, Scanner myObj){
    // TODO We're assuming one Listing per Host right?


    // Are you logged in as a host?
    if(!verifyUserInTable(conn, DAO.loggedInUser, "hostSIN",
    "Host")){
        System.out.println("You must be logged in as a host.");
        return;
    }

    // Input a renter 
    // TODO How will this be chosen during an actual workflow lol. Maybe we'll 
    // display all renters' id and then be like which one would you like to review?
    System.out.println("Provide the SIN of the Renter you'd like to review");
    int renterSIN = Integer.parseInt(myObj.nextLine());

    // Have you already left a review for this renter?
    try {
        Statement stmt = conn.createStatement();
        String sql = "SELECT * FROM hostsReviewRenters WHERE renterSIN = " + renterSIN + " AND hostSIN = " + DAO.loggedInUser + ";";
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
      String sql = "SELECT * FROM Booked JOIN HostsToListings ON Booked.listID = HostsToListings.listID WHERE renterSIN = " + renterSIN + " AND hostSIN = " + DAO.loggedInUser + ";";
      ResultSet rs = stmt.executeQuery(sql);
      if (rs.next()) {
          System.out.println("They've booked from you before.");
      }
      else{
          System.out.println("Cannot leave a review because you've never rented them a listing before.");
          return;
      }
    } 
    catch (SQLException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
    }

    // Leave a review and rating 
    System.out.println("What is your rating for the renter out of 5?");
    int rating = Integer.parseInt(myObj.nextLine());
    if(rating > 5 || rating < 1){
        System.out.println("Rating can only be 1-5.");
        return;
    }
    System.out.println("Provide you comment about the renter.");
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
          DAO.loggedInUser, renterSIN, comment, rating);

      System.out.println(reviewInsert);
      insert.executeUpdate(reviewInsert);
    } catch (SQLException e) {
      // TODO Auto-generated catch block [replace with generic error message]
      e.printStackTrace();
    }
  }
}

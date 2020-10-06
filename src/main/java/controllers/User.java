package controllers;

import server.Main;

import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;
import org.json.simple.JSONObject;

import javax.ws.rs.*;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.MediaType;
import java.io.*;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.UUID;

@Path("user/")
@Consumes(MediaType.MULTIPART_FORM_DATA)
@Produces(MediaType.APPLICATION_JSON)

public class User {
    @POST
    @Path("login")
    public String loginUser(@FormDataParam("Email") String Email, @FormDataParam("Password") String Password) {
        System.out.println("Invoked loginUser() on path user/login");
        try {
            PreparedStatement ps1 = Main.db.prepareStatement("SELECT Password FROM Users WHERE Email = ?");
            ps1.setString(1, Email);
            ResultSet loginResults = ps1.executeQuery();
            if (loginResults.next() == true) {
                String correctPassword = loginResults.getString(1);
                if (Password.equals(correctPassword)) {
                    String Token = UUID.randomUUID().toString();
                    PreparedStatement ps2 = Main.db.prepareStatement("UPDATE Users SET Token = ? WHERE Email = ?");
                    ps2.setString(1, Token);
                    ps2.setString(2, Email);
                    ps2.executeUpdate();
                    JSONObject userDetails = new JSONObject();
                    userDetails.put("Email", Email);
                    userDetails.put("Token", Token);
                    return userDetails.toString();
                } else {
                    return "{\"Error\": \"Incorrect password!\"}";
                }
            } else {
                return "{\"Error\": \"Email and password are incorrect.\"}";
            }
        } catch (Exception exception) {
            System.out.println("Database error during /user/login: " + exception.getMessage());
            return "{\"Error\": \"Server side error!\"}";
        }
    }

    public static boolean validToken(String Token) {        // this method MUST be called before any data is returned to the browser
        // token is taken from the Cookie sent back automatically with every HTTP request
        try {
            PreparedStatement ps = Main.db.prepareStatement("SELECT UserID FROM Users WHERE Token = ?");
            ps.setString(1, Token);
            ResultSet logoutResults = ps.executeQuery();
            return logoutResults.next();   //logoutResults.next() will be true if there is a record in the ResultSet
        } catch (Exception exception) {
            System.out.println("Database error" + exception.getMessage());
            return false;
        }
    }


    /*private static int getUserID(String email, String password) {
        System.out.println("Invoked User.getUserID()");
        try {
            PreparedStatement ps1 = Main.db.prepareStatement("SELECT UserID FROM Users WHERE Email = ? AND Password = ?");
            ps1.setString(1, email);
            ps1.setString(2, password);
            ResultSet resultSet = ps1.executeQuery();

            if (resultSet.next()==false){
                return -1 ;
            } else {
                return resultSet.getInt("UserID");
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return -1;
        }
    }

    private static String updateUUIDinDB(int userID, String uuid) {
        System.out.println("Invoked User.updateUUIDinDB()");

        try {
            PreparedStatement statement = Main.db.prepareStatement(
                    "UPDATE Users SET Token = ? WHERE UserID = ?"
            );
            statement.setString(1, uuid);
            statement.setInt(2, userID);
            statement.executeUpdate();
            return "OK";
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return "Error";
        }
    }

    public static int validateSessionCookie(Cookie sessionCookie){
        String uuid = sessionCookie.getValue();
        System.out.println("Invoked User.validateSessionCookie(), cookie value " + uuid);

        try {
            PreparedStatement statement = Main.db.prepareStatement(
                    "SELECT UserID FROM Users WHERE Token = ?"
            );
            statement.setString(1, uuid);
            ResultSet resultSet = statement.executeQuery();
            System.out.println("userID is " + resultSet.getInt("UserID"));
            return resultSet.getInt("UserID");  //Retrieve by column name  (should really test we only get one result back!)
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return -1;  //rogue value indicating error

        }
    }*/

    @POST
    @Path("add")
    public String userAdd(@FormDataParam("userID") int userID, @FormDataParam("name") String name,
                          @FormDataParam("email") String email, @FormDataParam("password") String password,
                          @FormDataParam("admin") boolean admin) {
        System.out.println("Invoked User.userAdd()");

        //would be better to test if username taken and if username and password already exist and return useful error message to browswer.

        try {
            PreparedStatement statement = Main.db.prepareStatement(
                    "INSERT INTO Users (UserID, Name, Email, Password, Admin) VALUES (?, ?, ?, ?, ?)"
            );
            statement.setInt(1, userID);
            statement.setString(2, name);
            statement.setString(3, email);
            statement.setString(4, password);
            statement.setBoolean(5, admin);
            statement.executeUpdate();
            return "{\"OK\": \"New user has been added successfully. \"}";

        } catch (Exception e) {
            System.out.println(e.getMessage());
            return "{\"Error\": \"Something as gone wrong.  Please contact the administrator with the error code UC-UA. \"}";
        }
    }

    /*@Path("get/{UserID}")
    public String getFood(@PathParam("UserID") Integer UserID) {
        System.out.println("Invoked User.getUser() with UserID " + UserID);
        try {
            PreparedStatement ps = Main.db.prepareStatement("SELECT Name FROM Users WHERE UserID = ?");
            ps.setInt(1, UserID);
            ResultSet results = ps.executeQuery();
            JSONObject response = new JSONObject();
            if (results.next() == true) {
                response.put("Name", results.getString(1));
            }
            return response.toString();
        } catch (Exception exception) {
            System.out.println("Database error: " + exception.getMessage());
            return "{\"Error\": \"Unable to get item, please see server console for more info.\"}";
        }
    }
    @Path("list")
    public String userList() {
        System.out.println("Invoked User.userList()");
        JSONArray response = new JSONArray();
        try {
            PreparedStatement ps = Main.db.prepareStatement("SELECT UserID, Name, Password, Email, Admin FROM Users");
            ResultSet results = ps.executeQuery();
            while (results.next()) {
                JSONObject row = new JSONObject();
                row.put("UserID", results.getInt(1));
                row.put("Name", results.getString(2));
                row.put("Password", results.getString(3));
                row.put("Email", results.getString(4));
                row.put("Admin", results.getBoolean(5));
                response.add(row);
            }
            return response.toString();
        } catch (Exception exception) {
            System.out.println("Database error: " + exception.getMessage());
            return "{\"Error\": \"Unable to list items.\"}";
        }
    }*/
}
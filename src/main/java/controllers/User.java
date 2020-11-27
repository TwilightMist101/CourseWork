package controllers;

import server.Main;

import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;
import org.json.simple.JSONObject;

import javax.xml.bind.DatatypeConverter;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;


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
    public String loginUser(@FormDataParam("email") String email, @FormDataParam("password") String password) {
        System.out.println("Invoked login() with email of " + email + " and password " + password);

        String hashedPassword = generateHash(password);

        try {
            PreparedStatement statement = Main.db.prepareStatement("SELECT UserID FROM Users WHERE Email = ? AND Password = ?");
            statement.setString(1, email);
            statement.setString(2, hashedPassword);
            ResultSet results = statement.executeQuery();
            //if there is not record with this email and password, condition below will be false
            if (results.next() == false) {
                return "{\"Error\": \"Username or password is incorrect.  Are you sure you've registered? \"}";
            } else {
                int userId = results.getInt("UserId");          //take the userId from the record returned in results
                String token = UUID.randomUUID().toString();                 //create a unique ID for session

                if (isTokenSetInDB(userId, token) == true) {   //store token for the user in the database
                    JSONObject cookie = new JSONObject();
                    cookie.put("token", token);
                    return cookie.toString();
                } else {
                    return "{\"Error\": \"Something as gone wrong.  Please contact the administrator with the error code UC-UL. \"}";
                }
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return "{\"Error\": \"Something has gone wrong.  Please contact the administrator with the error code ???? \"}";
        }

    }


    public static int validateSessionCookie(Cookie sessionCookie) {
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
    }



    private static boolean isTokenSetInDB(int userId, String token) {
        System.out.println("Invoked isTokenSetInDB()");

        try {
            PreparedStatement statement = Main.db.prepareStatement("UPDATE Users SET Token = ? WHERE UserID = ?"
            );
            statement.setString(1, token);
            statement.setInt(2, userId);
            statement.executeUpdate();
            return true;
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return false;
        }
    }



    @POST
    @Path("add")
    public String userAdd(@FormDataParam("firstName") String firstName,
                          @FormDataParam("lastName") String lastName,
                          @FormDataParam("password") String password,
                          @FormDataParam("email") String email,
                          @FormDataParam("admin") boolean admin) {
        System.out.println("Invoked User.userAdd()");

        //would be better to test if username taken and if username and password already exist and return useful error message to browser.

        try {
            PreparedStatement statement = Main.db.prepareStatement(
                    "INSERT INTO Users (FirstName, LastName, Password, Email, Admin) VALUES (?, ?, ?, ?, ?)"
            );
           statement.setString(1, firstName);
            statement.setString(2, lastName);
            statement.setString(3, generateHash(password));
            statement.setString(4, email);
            statement.setBoolean(5, false);
            statement.executeUpdate();
            return "{\"OK\": \"New user has been added successfully. \"}";

        } catch (Exception e) {
            System.out.println(e.getMessage());
            return "{\"Error\": \"Something as gone wrong.  Please contact the administrator with the error code UC-UA. \"}";
        }
    }

    public static String generateHash(String password) {
        try {
            MessageDigest hasher = MessageDigest.getInstance("MD5");
            hasher.update(password.getBytes());
            return DatatypeConverter.printHexBinary(hasher.digest()).toUpperCase();
        } catch (NoSuchAlgorithmException nsae) {
            return nsae.getMessage();
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
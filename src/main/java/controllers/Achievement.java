package controllers;

import org.glassfish.jersey.media.multipart.FormDataParam;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import server.Main;

import javax.ws.rs.*;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.MediaType;
import java.sql.PreparedStatement;
import java.sql.ResultSet;


@Path("achievement/")
@Consumes(MediaType.MULTIPART_FORM_DATA)
@Produces(MediaType.APPLICATION_JSON)

public class Achievement {
    @GET
    @Path("list")
    public String getAchievement(@CookieParam("Token") Cookie sessionCookie){
        System.out.println("Invoked Achievement.list()");

        //use session token to work out the userID
        int userId = validateSessionCookie(sessionCookie);
        //if userId is -1 (rogue value), user does not have valid session token
        if (userId==-1){
            return "{\"Error\": \"Please log in.\"}";
        }

        JSONArray response = new JSONArray();
        try{
            PreparedStatement ps = Main.db.prepareStatement("SELECT * FROM Achievements WHERE UserId = ?");
            ps.setInt(1, userId);
            ResultSet resultSet = ps.executeQuery();
            while(resultSet.next()){
                JSONObject rec = new JSONObject();
                rec.put("AchievementDate", resultSet.getString("AchievementDate"));
                rec.put("AchievementName", resultSet.getString("AchievementName"));
                response.add(rec);
            }
            return response.toString();
        } catch (Exception exception) {
            System.out.println("Database error: " + exception.getMessage());
            return "{\"Error\": \"Unable to list items.  Error code xx.\"}";
        }

    }

    @POST
    @Path("add")
    public String addRecord(@FormDataParam("date") String date,
                            @FormDataParam("achievementId") int achievementId,
                            @FormDataParam("achievementName") String achievementName,
                            @CookieParam("sessionToken") Cookie sessionCookie){
        System.out.println("Invoked Achievement.achievementAdd()");

        int userID = User.validateSessionCookie(sessionCookie);
        if (userID == -1) {
            return "{\"Error\": \"Please log in.  Error code EC-EL\"}";
        }

        try {
            PreparedStatement statement = Main.db.prepareStatement(
                    "INSERT INTO Achievements VALUES (?, ?, ?, ?)"          //database sets WeightID when record created so omitted in SQL
            );
            statement.setInt(1, achievementId);
            statement.setString(2, date);
            statement.setString(3, achievementName);
            statement.setInt(4, userID);
            statement.executeUpdate();
            return "{\"OK\": \"Achievement has been added. \"}";
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return "{\"Error\": \"Something as gone wrong.  Please contact the administrator with the error code WC-WA. \"}";
        }
    }

    public static int validateSessionCookie(Cookie sessionCookie) {     //returns the userID that of the record with the cookie value

        String token = sessionCookie.getValue();
        System.out.println("Invoked User.validateSessionCookie(), cookie value " + token);

        try {
            PreparedStatement statement = Main.db.prepareStatement(
                    "SELECT UserId FROM Users WHERE Token = ?"
            );
            statement.setString(1, token);
            ResultSet resultSet = statement.executeQuery();
            System.out.println("userID is " + resultSet.getInt("UserID"));
            return resultSet.getInt("UserID");  //Retrieve by column name  (should really test we only get one result back!)
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return -1;  //rogue value indicating error

        }
    }

    @POST
    @Path("delete/{achievementID}")
    public String recordDelete(@PathParam("achievementID") int achievementID, @CookieParam("sessionToken") Cookie sessionCookie){
        System.out.println("Invoked Achievement.achievementDelete()");
        int userID = User.validateSessionCookie(sessionCookie);

        if (userID == -1) {
            return "{\"Error\": \"Please log in.  Error code EC-EL\"}";
        }
        try {
            PreparedStatement statement = Main.db.prepareStatement(
                    "DELETE FROM Achievements WHERE AchievementID = ?"
            );
            statement.setInt(1, achievementID);
            statement.executeUpdate();
            return "{\"OK\": \"Achievement has been deleted. \"}";
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return "{\"Error\": \"Something as gone wrong.  Please contact the administrator with the error code WC-WD. \"}";
        }
    }
}

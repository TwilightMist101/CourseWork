package controllers;

import org.glassfish.jersey.media.multipart.FormDataParam;
import org.json.simple.JSONObject;
import server.Main;

import org.json.simple.JSONArray;

import javax.ws.rs.*;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.MediaType;
import java.sql.PreparedStatement;
import java.sql.ResultSet;


@Path("record/")
@Consumes(MediaType.MULTIPART_FORM_DATA)
@Produces(MediaType.APPLICATION_JSON)

public class Record {
    @GET
    @Path("list")
    public String getRecord(@CookieParam("Token") Cookie sessionCookie){
        System.out.println("Invoked Record.list()");

        //use session token to work out the userID
        int userId = validateSessionCookie(sessionCookie);
        //if userId is -1 (rogue value), user does not have valid session token
        if (userId==-1){
            return "{\"Error\": \"Please log in.\"}";
        }

        JSONArray response = new JSONArray();
        try{
            PreparedStatement ps = Main.db.prepareStatement("SELECT * FROM Records WHERE UserId = ?");
            ps.setInt(1, userId);
            ResultSet resultSet = ps.executeQuery();
            while(resultSet.next()){
                JSONObject rec = new JSONObject();
                rec.put("RecordDate", resultSet.getString("RecordDate"));
                rec.put("ChoiceId", resultSet.getInt("ChoiceId"));
                rec.put("ChoiceName", resultSet.getString("ChoiceName"));
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
                            @FormDataParam("choiceId") int choiceId,
                            @FormDataParam("choiceName") String choiceName,
                            @CookieParam("sessionToken") Cookie sessionCookie){
        System.out.println("Invoked Weight.weightAdd()");

        int userID = User.validateSessionCookie(sessionCookie);
        if (userID == -1) {
            return "{\"Error\": \"Please log in.  Error code EC-EL\"}";
        }

        try {
            PreparedStatement statement = Main.db.prepareStatement(
                    "INSERT INTO Records (Date, UserID, ChoiceId, ChoiceName) VALUES (?, ?, ?, ?)"          //database sets WeightID when record created so omitted in SQL
            );
            statement.setString(1, date);
            statement.setInt(2, userID);
            statement.setInt(3, choiceId);
            statement.setString(4, choiceName);
            statement.executeUpdate();
            return "{\"OK\": \"Weight has been added. \"}";
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
    @Path("delete/{recordID}")
    public String recordDelete(@PathParam("recordID") int recordID, @CookieParam("sessionToken") Cookie sessionCookie){
        System.out.println("Invoked Record.recordDelete()");
        int userID = User.validateSessionCookie(sessionCookie);

        if (userID == -1) {
            return "{\"Error\": \"Please log in.  Error code EC-EL\"}";
        }
        try {
            PreparedStatement statement = Main.db.prepareStatement(
                    "DELETE FROM Weights WHERE RecordID = ?"
            );
            statement.setInt(1, recordID);
            statement.executeUpdate();
            return "{\"OK\": \"Record has been deleted. \"}";
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return "{\"Error\": \"Something as gone wrong.  Please contact the administrator with the error code WC-WD. \"}";
        }
    }
}

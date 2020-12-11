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

import static server.Convertor.convertToJSONArray;

@Path("record/")
@Consumes(MediaType.MULTIPART_FORM_DATA)
@Produces(MediaType.APPLICATION_JSON)

public class Record {
    @GET
    @Path("list")
    public String getRecord(@CookieParam("sessionToken") Cookie sessionCookie){
        System.out.println("Invoked Record.list()");

        PreparedStatement statement = null;
        JSONObject response = new JSONObject();
        try{
            statement = Main.db.prepareStatement("SELECT * FROM Records");
            ResultSet resultSet = statement.executeQuery();
            while(resultSet.next()){
                int id = resultSet.getInt("RecordId");
                JSONObject rec = new JSONObject();
                rec.put("UserId", resultSet.getInt("UserId"));
                rec.put("RecordDate", resultSet.getInt("RecordDate"));
                rec.put("ChoiceId", resultSet.getInt("ChoiceId"));
                rec.put("ChoiceName", resultSet.getInt("ChoiceName"));
                response.put(id, rec);
            }
        } catch (Exception e){
            response.put("Status", "Failure");
        }
        return response.toString();
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
}

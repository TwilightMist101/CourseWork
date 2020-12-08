package controllers;

import org.glassfish.jersey.media.multipart.FormDataParam;
import server.Main;

import org.json.simple.JSONArray;

import javax.ws.rs.*;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.MediaType;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import static server.Convertor.convertToJSONArray;

@Path("questionnaire/")
@Consumes(MediaType.MULTIPART_FORM_DATA)
@Produces(MediaType.APPLICATION_JSON)

public class Questionnaire {
    @GET
    @Path("list")
    public String choiceList(@CookieParam("sessionCookie") Cookie sessionCookie){
        System.out.println("Invoked Questionnaire.list()");

        int userID = User.validateSessionCookie(sessionCookie);
        if(userID == -1){
            return "{\"Error\": \"Please log in. Error code EC-EL\"}";
        }
        try{
            PreparedStatement statement = Main.db.prepareStatement(
                    "SELECT QuestionId, UserId, Date, ChoiceId FROM Questionnaires WHERE userID = ? order by Date DESC"
            );
            statement.setInt(1, userID);
            ResultSet resultSet = statement.executeQuery();
            JSONArray newJSONArray = convertToJSONArray(resultSet);
            System.out.println(newJSONArray.toString());
            return newJSONArray.toString();
        } catch (Exception e){
            System.out.println(e.getMessage());
            return "{\"Error\": \"Something as gone wrong.  Please contact the administrator with the error code WC-WL. \"}";
        }
    }

    @POST
    @Path("add")
    public String recordAdd(@FormDataParam("questionId") int questionId,
                            @FormDataParam("choiceId") int choiceId,
                            @CookieParam("sessionToken") Cookie sessionCookie){
        System.out.println("Invoked Weight.recordAdd()");

        int userID = User.validateSessionCookie(sessionCookie);
        if(userID == -1){
            return "{\"Error\": \"Please log in. Error code EC-EL\"}";
        }

        try{
            PreparedStatement statement = Main.db.prepareStatement(
                    "INSERT INTO Questionnaires (QuestionId, UserId, Date, ChoiceId) VALUES (?, ?, ?, ?)"
            );
            statement.setInt(1, questionId);
            statement.setInt(2, userID);
            //statement.setString(3, date);
            statement.setInt(4, choiceId);
            statement.executeUpdate();
            return "{\"OK\": \"Weight has been added. \"}";
        } catch (Exception e){
            System.out.println(e.getMessage());
            return "{\"Error\": \"Something as gone wrong.  Please contact the administrator with the error code WC-WA. \"}";
        }
    }
}

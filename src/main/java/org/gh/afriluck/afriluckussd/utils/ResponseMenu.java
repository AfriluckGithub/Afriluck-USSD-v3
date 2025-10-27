package org.gh.afriluck.afriluckussd.utils;

import com.google.gson.JsonObject;
import org.gh.afriluck.afriluckussd.entities.Session;

public class ResponseMenu {

    public static String menuResponse(Session session, int continueFlag, String message) {
        JsonObject json = new JsonObject();
        json.addProperty("msisdn", session.getMsisdn());
        json.addProperty("sequenceID", session.getSequenceID());
        json.addProperty("timestamp", session.getTimeStamp());
        json.addProperty("message", message);
        json.addProperty("continueFlag", continueFlag);
        return json.toString();
    }
}

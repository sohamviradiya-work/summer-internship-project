package com.tool.clients;

import java.io.IOException;
import java.net.URISyntaxException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.items.TeamsNotification;
import com.tool.Config;

import io.github.cdimascio.dotenv.Dotenv;
import io.github.cdimascio.dotenv.DotenvException;

public class TeamsClient extends NetworkServiceClient {

    private String apiUrl;

    private TeamsClient(String apiUrl) {
        this.apiUrl = apiUrl;
    }

    public void createNotification(TeamsNotification teamsNotification)
            throws IOException, URISyntaxException {
        if(teamsNotification.getEmail()=="LAST PHASE") return;
        String endPoint = apiUrl + "/api/notify?email=" + teamsNotification.getEmail(); 
        String payload = getBody(teamsNotification);
        sendPostRequest(endPoint, payload);
    }

    private String getBody(TeamsNotification teamsNotification) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();

        ObjectNode payload = objectMapper.createObjectNode();

        ObjectNode notification = payload.putObject("notification");

        notification.put("description", teamsNotification.getContent());
        return objectMapper.writeValueAsString(payload);
    }

    protected String getAuthHeader() throws IOException, URISyntaxException {
        return "";
    }

    public static TeamsClient create() throws DotenvException, IOException, URISyntaxException {
        Dotenv dotenv = Dotenv.configure().directory(Config.getProjectRoot()).load();
        final String apiUrl = dotenv.get("TEAMS_BOT_API_URL");
        return new TeamsClient(apiUrl);
    }

}

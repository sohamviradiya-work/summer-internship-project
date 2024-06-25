package com.items;

public class JiraTicket {
    private String summary;
    private String description;
    private String email;

    public JiraTicket(String summary, String description, String email) {
        this.summary = summary;
        this.description = description;
        this.email = email;
    }

    public String getSummary() {
        return summary;
    }

    public String getDescription() {
        return description;
    }

    public String getEmail() {
        return email;
    }
}


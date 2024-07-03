package com.tool.writers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;

import com.items.JiraTicket;
import com.items.interfaces.JiraItem;
import com.tool.clients.JiraClient;

public class JiraTicketWriterTest {

    @Mock
    private JiraClient jiraClient;

    private JiraTicketWriter<JiraItem> jiraTicketWriter;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        jiraTicketWriter = new JiraTicketWriter<>(jiraClient);
    }

    @Test
    void testWriteSingleItem() throws IOException, URISyntaxException {
        JiraItem jiraItem = mock(JiraItem.class);
        when(jiraItem.toJiraTicket()).thenReturn(new JiraTicket( "test description", "test@email.com"));

        jiraTicketWriter.write(jiraItem);
        jiraTicketWriter.close();

        ArgumentCaptor<String> emailCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> descriptionCaptor = ArgumentCaptor.forClass(String.class);

        verify(jiraClient, times(1)).createIssue(emailCaptor.capture(),descriptionCaptor.capture());

        assertEquals("test description\n\n", descriptionCaptor.getValue());
        assertEquals("test@email.com", emailCaptor.getValue());
    }

    @Test
    void testWriteMultipleItemsSingleMail() throws IOException, URISyntaxException {
        JiraItem jiraItem1 = mock(JiraItem.class);
        when(jiraItem1.toJiraTicket()).thenReturn(new JiraTicket("description1", "email1@example.com"));

        JiraItem jiraItem2 = mock(JiraItem.class);
        when(jiraItem2.toJiraTicket()).thenReturn(new JiraTicket("description2", "email1@example.com"));

        JiraItem jiraItem3 = mock(JiraItem.class);
        when(jiraItem3.toJiraTicket()).thenReturn(new JiraTicket("description3", "email1@example.com"));

        List<JiraItem> items = List.of(jiraItem1, jiraItem2, jiraItem3);
        jiraTicketWriter.writeAll(items);
        jiraTicketWriter.close();

        ArgumentCaptor<String> emailCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> descriptionCaptor = ArgumentCaptor.forClass(String.class);

        verify(jiraClient, times(1)).createIssue(emailCaptor.capture(),descriptionCaptor.capture());

        List<String> capturedEmails = emailCaptor.getAllValues();
        assertEquals(1, capturedEmails.size());    
    }

    @Test
    void testWriteMultipleItemsMultipleMail() throws IOException, URISyntaxException {
        JiraItem jiraItem1 = mock(JiraItem.class);
        when(jiraItem1.toJiraTicket()).thenReturn(new JiraTicket("description1", "email1@example.com"));

        JiraItem jiraItem2 = mock(JiraItem.class);
        when(jiraItem2.toJiraTicket()).thenReturn(new JiraTicket("description2", "email2@example.com"));

        JiraItem jiraItem3 = mock(JiraItem.class);
        when(jiraItem3.toJiraTicket()).thenReturn(new JiraTicket("description3", "email1@example.com"));

        List<JiraItem> items = List.of(jiraItem1, jiraItem2, jiraItem3);
        jiraTicketWriter.writeAll(items);
        jiraTicketWriter.close();

        ArgumentCaptor<String> emailCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> descriptionCaptor = ArgumentCaptor.forClass(String.class);

        verify(jiraClient, times(2)).createIssue(emailCaptor.capture(),descriptionCaptor.capture());

        List<String> capturedEmails = emailCaptor.getAllValues();
        assertEquals(2, capturedEmails.size());    
    }

}

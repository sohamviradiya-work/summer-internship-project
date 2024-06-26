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

import io.github.cdimascio.dotenv.DotenvException;

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
    void testWriteSingleItem() throws IOException, DotenvException, URISyntaxException {
        JiraItem jiraItem = mock(JiraItem.class);
        when(jiraItem.toJiraTicket()).thenReturn(new JiraTicket("test summary", "test description", "test@email.com"));

        jiraTicketWriter.write(jiraItem);
        jiraTicketWriter.close();

        ArgumentCaptor<JiraTicket> captor = ArgumentCaptor.forClass(JiraTicket.class);

        verify(jiraClient, times(1)).createIssue(captor.capture());

        JiraTicket capturedTicket = captor.getValue();
        assertEquals("test summary", capturedTicket.getSummary());
        assertEquals("test description", capturedTicket.getDescription());
        assertEquals("test@email.com", capturedTicket.getEmail());
    }

    @Test
    void testWriteMultipleItems() throws IOException, DotenvException, URISyntaxException {
        JiraItem jiraItem1 = mock(JiraItem.class);
        when(jiraItem1.toJiraTicket()).thenReturn(new JiraTicket("summary1", "description1", "email1@example.com"));

        JiraItem jiraItem2 = mock(JiraItem.class);
        when(jiraItem2.toJiraTicket()).thenReturn(new JiraTicket("summary2", "description2", "email2@example.com"));

        jiraTicketWriter.write(jiraItem1);
        jiraTicketWriter.write(jiraItem2);
        jiraTicketWriter.close();

        ArgumentCaptor<JiraTicket> captor = ArgumentCaptor.forClass(JiraTicket.class);

        verify(jiraClient, times(2)).createIssue(captor.capture());

        List<JiraTicket> capturedTickets = captor.getAllValues();
        assertEquals(2, capturedTickets.size());
    }

    @Test
    void testWriteAll() throws IOException, DotenvException, URISyntaxException {
        JiraItem jiraItem1 = mock(JiraItem.class);
        when(jiraItem1.toJiraTicket()).thenReturn(new JiraTicket("summary1", "description1", "email1@example.com"));

        JiraItem jiraItem2 = mock(JiraItem.class);
        when(jiraItem2.toJiraTicket()).thenReturn(new JiraTicket("summary2", "description2", "email2@example.com"));

        JiraItem jiraItem3 = mock(JiraItem.class);
        when(jiraItem3.toJiraTicket()).thenReturn(new JiraTicket("summary3", "description3", "email3@example.com"));

        List<JiraItem> items = List.of(jiraItem1, jiraItem2, jiraItem3);
        jiraTicketWriter.writeAll(items);
        jiraTicketWriter.close();

        ArgumentCaptor<JiraTicket> captor = ArgumentCaptor.forClass(JiraTicket.class);

        verify(jiraClient, times(3)).createIssue(captor.capture());

        List<JiraTicket> capturedTickets = captor.getAllValues();
        assertEquals(3, capturedTickets.size());
    }
}

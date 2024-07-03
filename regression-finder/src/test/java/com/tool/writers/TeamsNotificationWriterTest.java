package com.tool.writers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.items.TeamsNotification;
import com.items.interfaces.TeamsItem;
import com.tool.clients.TeamsClient;

public class TeamsNotificationWriterTest {
    
    @Mock
    private TeamsClient teamsClient;

    private TeamsNotificationWriter<TeamsItem> teamsNotificationWriter;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        teamsNotificationWriter = new TeamsNotificationWriter<>(teamsClient);
    }

    @Test
    void testWriteSingleItem() throws IOException, URISyntaxException {
        TeamsItem teamsItem = mock(TeamsItem.class);
        when(teamsItem.toTeamsNotification()).thenReturn(new TeamsNotification("test content", "test@email.com"));

        teamsNotificationWriter.write(teamsItem);
        teamsNotificationWriter.close();

        ArgumentCaptor<TeamsNotification> captor = ArgumentCaptor.forClass(TeamsNotification.class);

        verify(teamsClient, times(1)).createNotification(captor.capture());

        TeamsNotification capturedTicket = captor.getValue();
        assertEquals("test content", capturedTicket.getContent());
        assertEquals("test@email.com", capturedTicket.getEmail());
    }

    @Test
    void testWriteMultipleItems() throws IOException, URISyntaxException {
        TeamsItem teamsItem1 = mock(TeamsItem.class);
        when(teamsItem1.toTeamsNotification()).thenReturn(new TeamsNotification("description1", "email1@example.com"));

        TeamsItem teamsItem2 = mock(TeamsItem.class);
        when(teamsItem2.toTeamsNotification()).thenReturn(new TeamsNotification("description2", "email2@example.com"));

        teamsNotificationWriter.write(teamsItem1);
        teamsNotificationWriter.write(teamsItem2);
        teamsNotificationWriter.close();

        ArgumentCaptor<TeamsNotification> captor = ArgumentCaptor.forClass(TeamsNotification.class);

        verify(teamsClient, times(2)).createNotification(captor.capture());

        List<TeamsNotification> capturedTickets = captor.getAllValues();
        assertEquals(2, capturedTickets.size());
    }

    @Test
    void testWriteAll() throws IOException, URISyntaxException {
        TeamsItem teamsItem1 = mock(TeamsItem.class);
        when(teamsItem1.toTeamsNotification()).thenReturn(new TeamsNotification( "description1", "email1@example.com"));

        TeamsItem teamsItem2 = mock(TeamsItem.class);
        when(teamsItem2.toTeamsNotification()).thenReturn(new TeamsNotification("description2", "email2@example.com"));

        TeamsItem teamsItem3 = mock(TeamsItem.class);
        when(teamsItem3.toTeamsNotification()).thenReturn(new TeamsNotification("description3", "email3@example.com"));

        List<TeamsItem> items = List.of(teamsItem1, teamsItem2, teamsItem3);
        teamsNotificationWriter.writeAll(items);
        teamsNotificationWriter.close();

        ArgumentCaptor<TeamsNotification> captor = ArgumentCaptor.forClass(TeamsNotification.class);

        verify(teamsClient, times(3)).createNotification(captor.capture());

        List<TeamsNotification> capturedTickets = captor.getAllValues();
        assertEquals(3, capturedTickets.size());
    }
}

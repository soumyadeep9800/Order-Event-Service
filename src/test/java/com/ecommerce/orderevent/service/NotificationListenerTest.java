package com.ecommerce.orderevent.service;


import com.ecommerce.orderevent.models.OrderEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.*;

class NotificationListenerTest {

    private NotificationService notificationService;
    private NotificationListener notificationListener;

    @BeforeEach
    void setUp() {
        notificationService = mock(NotificationService.class);
        notificationListener = new NotificationListener(notificationService);
    }

    @Test
    void testConsume_ShouldDelegateToNotificationService() {
        // given
        OrderEvent event = new OrderEvent(10L, 31L, 4L, java.util.List.of(6L), "PLACED");

        // when
        notificationListener.consume(event);

        // then
        verify(notificationService, times(1)).processNotification(event);
    }
}

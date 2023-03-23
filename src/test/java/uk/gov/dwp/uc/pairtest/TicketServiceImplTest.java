package uk.gov.dwp.uc.pairtest;

import org.junit.Test;
import org.junit.jupiter.api.DisplayName;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import thirdparty.paymentgateway.TicketPaymentService;
import thirdparty.seatbooking.SeatReservationService;
import uk.gov.dwp.uc.pairtest.domain.TicketTypeRequest;
import uk.gov.dwp.uc.pairtest.exception.InvalidPurchaseException;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

public class TicketServiceImplTest {

    @InjectMocks
    private TicketServiceImpl ticketService;

    @Mock
    private SeatReservationService seatReservationService;
    @Mock
    private TicketPaymentService ticketPaymentService;

    @Test
    public void purchaseTickets() {
    }


    @Test
    @DisplayName("Invalid account ID test")
    public void testValidAccountId() throws InvalidPurchaseException {

        TicketTypeRequest ticketTypeRequests = new TicketTypeRequest(TicketTypeRequest.Type.ADULT, 20);
        ticketService.purchaseTickets(2L, ticketTypeRequests);
    }



}
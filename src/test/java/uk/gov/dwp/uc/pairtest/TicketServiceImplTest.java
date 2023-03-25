package uk.gov.dwp.uc.pairtest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.api.DisplayName;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import thirdparty.paymentgateway.TicketPaymentService;
import thirdparty.seatbooking.SeatReservationService;
import uk.gov.dwp.uc.pairtest.domain.TicketTypeRequest;
import uk.gov.dwp.uc.pairtest.exception.InvalidPurchaseException;


public class TicketServiceImplTest {

    @Mock
    private TicketPaymentService paymentService;

    @Mock
    private SeatReservationService seatReservationService;

    private TicketServiceImpl ticketService;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        ticketService = new TicketServiceImpl(paymentService, seatReservationService);
    }

    @Test
    @DisplayName("Test account Id is null")
    public void testAccountIdIsNull() throws InvalidPurchaseException {
        Long accountId = null;
        TicketTypeRequest[] ticketTypeRequests = new TicketTypeRequest[] {
                new TicketTypeRequest(TicketTypeRequest.Type.ADULT, 5)
        };

        InvalidPurchaseException exception = assertThrows(
                InvalidPurchaseException.class,
                () -> ticketService.purchaseTickets(accountId, ticketTypeRequests)
        );
        assertEquals("Account Id is not valid", exception.getMessage());
    }

    @Test
    @DisplayName("Test account Id is zero")
    public void testAccountIdIsZero() throws InvalidPurchaseException {
        Long accountId = 0L;
        TicketTypeRequest[] ticketTypeRequests = new TicketTypeRequest[] {
                new TicketTypeRequest(TicketTypeRequest.Type.ADULT, 3)
        };
        InvalidPurchaseException exception = assertThrows(
                InvalidPurchaseException.class,
                () -> ticketService.purchaseTickets(accountId, ticketTypeRequests)
        );
        assertEquals("Account Id is not valid", exception.getMessage());
    }

    @Test
    @DisplayName("Test ticket request is empty")
    public void testTicketRequestIsEmpty() throws InvalidPurchaseException {
        Long accountId = 100001L;
        TicketTypeRequest[] ticketTypeRequests = new TicketTypeRequest[] { };

        InvalidPurchaseException exception = assertThrows(
                InvalidPurchaseException.class,
                () -> ticketService.purchaseTickets(accountId, ticketTypeRequests)
        );
        assertEquals("Ticket requests cannot be null or empty", exception.getMessage());
    }

    @Test
    @DisplayName("Test maximum nof ticket is 20 at a time")
    public void testMaximumNofAllowedTicketsIs20() throws InvalidPurchaseException {
        Long accountId = 100001L;
        TicketTypeRequest[] ticketTypeRequests = new TicketTypeRequest[] {
                new TicketTypeRequest(TicketTypeRequest.Type.ADULT, 10),
                new TicketTypeRequest(TicketTypeRequest.Type.ADULT, 12)
        };
        InvalidPurchaseException exception = assertThrows(
                InvalidPurchaseException.class,
                () -> ticketService.purchaseTickets(accountId, ticketTypeRequests)
        );
        assertEquals("The maximum number of tickets that can be purchased at a time is 20", exception.getMessage());
    }

    @Test
    @DisplayName("Test is nof infant tickets grater that adult tickets")
    public void testNofInfantsGreaterThanNofAdults() throws InvalidPurchaseException {
        Long accountId = 100001L;
        TicketTypeRequest[] ticketTypeRequests = new TicketTypeRequest[] {
                new TicketTypeRequest(TicketTypeRequest.Type.ADULT, 5),
                new TicketTypeRequest(TicketTypeRequest.Type.INFANT, 7)
        };
        InvalidPurchaseException exception = assertThrows(
                InvalidPurchaseException.class,
                () -> ticketService.purchaseTickets(accountId, ticketTypeRequests)
        );
        assertEquals("The number of infant tickets requested cannot exceed the number of adult tickets requested", exception.getMessage());
    }

    @Test
    @DisplayName("Test is at least one adult present")
    public void testIsAdultTicketIsPresent() throws InvalidPurchaseException {
        Long accountId = 100001L;
        TicketTypeRequest[] ticketTypeRequests = new TicketTypeRequest[] {
                new TicketTypeRequest(TicketTypeRequest.Type.CHILD, 5),
                new TicketTypeRequest(TicketTypeRequest.Type.INFANT, 7)
        };
        InvalidPurchaseException exception = assertThrows(
                InvalidPurchaseException.class,
                () -> ticketService.purchaseTickets(accountId, ticketTypeRequests)
        );
        assertEquals("Adult tickets are not present", exception.getMessage());
    }

    @Test
    @DisplayName("Test purchase tickets with make payment and seat reservation ")
    public void testPurchaseTickets() throws InvalidPurchaseException {
        Long accountId = 2L;
        TicketTypeRequest[] ticketTypeRequests = new TicketTypeRequest[] {
                new TicketTypeRequest(TicketTypeRequest.Type.ADULT, 3),
                new TicketTypeRequest(TicketTypeRequest.Type.CHILD, 5),
                new TicketTypeRequest(TicketTypeRequest.Type.INFANT, 1)
        };
        ticketService.purchaseTickets(accountId, ticketTypeRequests);

        Mockito.verify(paymentService).makePayment(accountId, 110);
        Mockito.verify(seatReservationService).reserveSeat(accountId, 8);
    }
}
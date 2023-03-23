package uk.gov.dwp.uc.pairtest;

import thirdparty.paymentgateway.TicketPaymentService;
import thirdparty.seatbooking.SeatReservationService;
import uk.gov.dwp.uc.pairtest.domain.TicketTypeRequest;
import uk.gov.dwp.uc.pairtest.exception.InvalidPurchaseException;

import java.security.PrivilegedAction;

public class TicketServiceImpl implements TicketService {

    /**
     *
     * Should only have private methods other than the one below.
     */
    private static final Integer MAX_NOF_TICKETS_ALLOWED = 20;
    private final Integer INFANT_TICKET_PRICE = 0;
    private final Integer CHILD_TICKET_PRICE = 10;
    private final Integer ADULT_TICKET_PRICE = 20;

    private final TicketPaymentService ticketPaymentService;
    private final SeatReservationService seatReservationService;

    private TicketServiceImpl(TicketPaymentService ticketPaymentService, SeatReservationService seatReservationService) {
        this.ticketPaymentService = ticketPaymentService;
        this.seatReservationService = seatReservationService;
    }

    /**
     * Purchase tickets
     * @param accountId
     * @param ticketTypeRequests
     * @throws InvalidPurchaseException
     */
    @Override
    public void purchaseTickets(Long accountId, TicketTypeRequest... ticketTypeRequests) throws InvalidPurchaseException {
        try {
            validatePurchaseTicketsRequest(accountId, ticketTypeRequests);
            int totalAmountToPay = calculateTotalAmountToPay(ticketTypeRequests);
            int totalSeatsToAllocate = calculateTotalNofSeatsToAllocate(ticketTypeRequests);

            ticketPaymentService.makePayment(accountId, totalAmountToPay);
            seatReservationService.reserveSeat(accountId, totalSeatsToAllocate);

        } catch (Exception e) {
            throw new InvalidPurchaseException("Unknown application error");
        }
    }

    /**
     * Purchase tickets request validations.
     * @param accountId
     * @param ticketTypeRequests
     */
    private void validatePurchaseTicketsRequest(Long accountId, TicketTypeRequest... ticketTypeRequests){

        if (accountId == null || accountId <= 0) {
            throw new InvalidPurchaseException("Account Id is not valid.");
        }

        if (ticketTypeRequests == null || ticketTypeRequests.length == 0) {
            throw new InvalidPurchaseException("Ticket requests cannot be null or empty.");
        }

        int nofInfantTickets = nofInfantTicketsCount(ticketTypeRequests);
        int nofAdultTickets = nofAdultTicketsCount(ticketTypeRequests);
        int nofChildTickets = nofChildTicketsCount(ticketTypeRequests);
        int totalNofSeatsToAllocate = nofAdultTickets + nofChildTickets;

        if(totalNofSeatsToAllocate > MAX_NOF_TICKETS_ALLOWED){
            throw new InvalidPurchaseException("The maximum number of tickets that can be purchased at a time is " + MAX_NOF_TICKETS_ALLOWED + ".");
        }

        if(nofInfantTickets > nofAdultTickets){
            throw new InvalidPurchaseException("The number of infant tickets requested cannot exceed the number of adult tickets requested.");
        }

        if(!isAdultTicketPresent(ticketTypeRequests)){
            throw new InvalidPurchaseException("Adult tickets are not present.");
        }
    }

    /**
     * Determine the presence of adult ticket.
     * @param ticketTypeRequests
     * @return
     */
    private boolean isAdultTicketPresent(TicketTypeRequest[] ticketTypeRequests) {
        for (TicketTypeRequest request : ticketTypeRequests) {
            switch (request.getTicketType()) {
                case ADULT:
                    return true;
                default:
                    break;
            }
        }
        return false;
    }

    /**
     * Calculate the number of INFANT tickets
     * @param ticketTypeRequests
     * @return
     */
    private int nofInfantTicketsCount(TicketTypeRequest[] ticketTypeRequests) {
        int nofTickets = 0;
        for (TicketTypeRequest request : ticketTypeRequests) {
            switch (request.getTicketType()) {
                case INFANT:
                    nofTickets = nofTickets + 1;
                default:
                    break;
            }
        }
        return nofTickets;
    }

    /**
     * Calculate the number of CHILD tickets
     * @param ticketTypeRequests
     * @return
     */
    private int nofChildTicketsCount(TicketTypeRequest[] ticketTypeRequests) {
        int nofTickets = 0;
        for (TicketTypeRequest request : ticketTypeRequests) {
            switch (request.getTicketType()) {
                case CHILD:
                    nofTickets = nofTickets + 1;
                default:
                    break;
            }
        }
        return nofTickets;
    }

    /**
     * Calculate the number of ADULT tickets
     * @param ticketTypeRequests
     * @return
     */
    private int nofAdultTicketsCount(TicketTypeRequest[] ticketTypeRequests) {
        int nofTickets = 0;
        for (TicketTypeRequest request : ticketTypeRequests) {
            switch (request.getTicketType()) {
                case ADULT:
                    nofTickets = nofTickets + 1;
                default:
                    break;
            }
        }
        return nofTickets;
    }

    /**
     * Determine the overall total payment amount for the seats requested.
     * @param ticketTypeRequests
     * @return
     */
    private int calculateTotalAmountToPay(TicketTypeRequest[] ticketTypeRequests){

        int totalAmountToPay = 0;

        for (TicketTypeRequest request : ticketTypeRequests) {

            TicketTypeRequest.Type ticketType = request.getTicketType();
            int nofTicketsRequested = request.getNoOfTickets();

            int ticketPrice = 0;
            switch (ticketType){
                case INFANT: totalAmountToPay = totalAmountToPay + (nofTicketsRequested * INFANT_TICKET_PRICE);
                case CHILD:  totalAmountToPay = totalAmountToPay + (nofTicketsRequested * CHILD_TICKET_PRICE);
                case ADULT:  totalAmountToPay = totalAmountToPay + (nofTicketsRequested * ADULT_TICKET_PRICE);
                default: throw new InvalidPurchaseException("Invalid ticket type");
            }
        }
        return totalAmountToPay;
    }

    /**
     * Determine the overall number of seats that need to be allocated.
     * @param ticketTypeRequests
     * @return
     */
    private int calculateTotalNofSeatsToAllocate(TicketTypeRequest[] ticketTypeRequests) {
        int nofAdultTickets = nofAdultTicketsCount(ticketTypeRequests);
        int nofChildTickets = nofChildTicketsCount(ticketTypeRequests);
        return nofAdultTickets + nofChildTickets;
    }
}
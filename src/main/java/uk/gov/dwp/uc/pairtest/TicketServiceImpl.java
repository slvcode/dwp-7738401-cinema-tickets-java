package uk.gov.dwp.uc.pairtest;

import thirdparty.paymentgateway.TicketPaymentService;
import thirdparty.seatbooking.SeatReservationService;
import uk.gov.dwp.uc.pairtest.domain.TicketTypeRequest;
import uk.gov.dwp.uc.pairtest.exception.InvalidPurchaseException;

import java.util.Arrays;

import static uk.gov.dwp.uc.pairtest.domain.TicketTypeRequest.*;

public class TicketServiceImpl implements TicketService {


    private static final Integer MAX_NOF_TICKETS_ALLOWED = 20;
    private final Integer INFANT_TICKET_PRICE = 0;
    private final Integer CHILD_TICKET_PRICE = 10;
    private final Integer ADULT_TICKET_PRICE = 20;

    private final TicketPaymentService ticketPaymentService;
    private final SeatReservationService seatReservationService;

    TicketServiceImpl(TicketPaymentService ticketPaymentService, SeatReservationService seatReservationService) {
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

        } catch (InvalidPurchaseException e) {
            throw e;
        }
    }

    /**
     * Purchase tickets request validations.
     * @param accountId
     * @param ticketTypeRequests
     */
    private void validatePurchaseTicketsRequest(Long accountId, TicketTypeRequest... ticketTypeRequests)
            throws InvalidPurchaseException {

        try {
            if (accountId == null || accountId <= 0) {
                throw new InvalidPurchaseException("Account Id is not valid");
            }

            if (ticketTypeRequests == null || ticketTypeRequests.length == 0) {
                throw new InvalidPurchaseException("Ticket requests cannot be null or empty");
            }

            int totalNofSeatsToAllocate = calculateTotalNofSeatsToAllocate(ticketTypeRequests);
            if(totalNofSeatsToAllocate > MAX_NOF_TICKETS_ALLOWED){
                throw new InvalidPurchaseException("The maximum number of tickets that can be purchased at a time is " + MAX_NOF_TICKETS_ALLOWED);
            }

            if(isNofInfantGraterThanAdult(ticketTypeRequests)){
                throw new InvalidPurchaseException("The number of infant tickets requested cannot exceed the number of adult tickets requested");
            }

            if(!isAdultTicketPresent(ticketTypeRequests)){
                throw new InvalidPurchaseException("Adult tickets are not present");
            }
        } catch (Exception e) {
            throw e;
        }
    }

    /**
     * Determine the presence of adult ticket.
     * @param ticketTypeRequests
     * @return
     */
    private boolean isAdultTicketPresent(TicketTypeRequest[] ticketTypeRequests) {
        return Arrays.stream(ticketTypeRequests).anyMatch(n -> n.getTicketType() == Type.ADULT);
    }

    /**
     * Check nof infant tickets are grater than nof adult tickets
     * @param ticketTypeRequests
     * @return
     */
    private boolean isNofInfantGraterThanAdult(TicketTypeRequest[] ticketTypeRequests) {

        int nofInfantTickets = 0;
        int nofAdultTickets = 0;

        boolean isAdultPresent = Arrays.stream(ticketTypeRequests).anyMatch(n -> n.getTicketType() == Type.ADULT);

        if(isAdultPresent) {
            for (TicketTypeRequest request : ticketTypeRequests) {
                Type ticketType = request.getTicketType();
                int nofTicketsRequested = request.getNoOfTickets();
                switch (ticketType) {
                    case INFANT:
                        nofInfantTickets = nofTicketsRequested;
                        break;
                    case ADULT:
                        nofAdultTickets = nofTicketsRequested;
                        break;
                }
            }

            if (nofInfantTickets > nofAdultTickets) {
                return true;
            }
        }
        return false;
    }

    /**
     * Calculate the total payment amount for the seats requested.
     * @param ticketTypeRequests
     * @return
     */
    private int calculateTotalAmountToPay(TicketTypeRequest[] ticketTypeRequests){

        int totalAmountToPay = 0;

        for (TicketTypeRequest request : ticketTypeRequests) {

            Type ticketType = request.getTicketType();
            int nofTicketsRequested = request.getNoOfTickets();

            /* Infants do not pay for a ticket and are not allocated a seat.
                They will be sitting on an Adult's lap.*/
            switch (ticketType){
                case CHILD:  totalAmountToPay = totalAmountToPay + (nofTicketsRequested * CHILD_TICKET_PRICE);
                    break;
                case ADULT:  totalAmountToPay = totalAmountToPay + (nofTicketsRequested * ADULT_TICKET_PRICE);
                    break;
            }
        }
        return totalAmountToPay;
    }

    /**
     * Calculate the total number of seats that need to be allocated.
     * @param ticketTypeRequests
     * @return
     */
    private int calculateTotalNofSeatsToAllocate(TicketTypeRequest[] ticketTypeRequests) {

        int totalNofSeatsToAllocate = 0;

        for (TicketTypeRequest request : ticketTypeRequests) {

            Type ticketType = request.getTicketType();
            int nofTicketsRequested = request.getNoOfTickets();

           /* Infants do not pay for a ticket and are not allocated a seat.
                They will be sitting on an Adult's lap.*/
            switch (ticketType){
                case CHILD:  totalNofSeatsToAllocate = totalNofSeatsToAllocate + nofTicketsRequested;
                    break;
                case ADULT:  totalNofSeatsToAllocate = totalNofSeatsToAllocate + nofTicketsRequested;
                    break;
            }
        }
        return totalNofSeatsToAllocate;
    }
}
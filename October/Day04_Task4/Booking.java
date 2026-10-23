public class Booking {
    private final String bookingId;
    private final Flight flight;
    private final Passenger passenger;
    private final int tickets;
    private final double subtotal;
    private final double discount;
    private final double finalAmount;

    public Booking(String bookingId, Flight flight, Passenger passenger, int tickets, double subtotal, double discount, double finalAmount) {
        this.bookingId = bookingId;
        this.flight = flight;
        this.passenger = passenger;
        this.tickets = tickets;
        this.subtotal = subtotal;
        this.discount = discount;
        this.finalAmount = finalAmount;
    }

    public String getBookingId() {
        return bookingId;
    }

    public Flight getFlight() {
        return flight;
    }

    public Passenger getPassenger() {
        return passenger;
    }

    public int getTickets() {
        return tickets;
    }

    public double getSubtotal() {
        return subtotal;
    }

    public double getDiscount() {
        return discount;
    }

    public double getFinalAmount() {
        return finalAmount;
    }
}

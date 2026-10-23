public class Flight {
    private final String flightCode;
    private final String airline;
    private final String source;
    private final String destination;
    private int availableSeats;
    private final double baseFare;

    public Flight(String flightCode, String airline, String source, String destination, int availableSeats, double baseFare) {
        this.flightCode = flightCode;
        this.airline = airline;
        this.source = source;
        this.destination = destination;
        this.availableSeats = availableSeats;
        this.baseFare = baseFare;
    }

    public String getFlightCode() {
        return flightCode;
    }

    public String getAirline() {
        return airline;
    }

    public String getSource() {
        return source;
    }

    public String getDestination() {
        return destination;
    }

    public int getAvailableSeats() {
        return availableSeats;
    }

    public double getBaseFare() {
        return baseFare;
    }

    public boolean hasEnoughSeats(int tickets) {
        return tickets > 0 && tickets <= availableSeats;
    }

    public boolean bookSeats(int tickets) {
        if (!hasEnoughSeats(tickets)) {
            return false;
        }
        availableSeats -= tickets;
        return true;
    }
}

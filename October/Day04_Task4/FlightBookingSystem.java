import java.util.ArrayList;
import java.util.Scanner;

public class FlightBookingSystem {
    private final ArrayList<Flight> flights;
    private final ArrayList<Booking> bookings;
    private final Scanner scanner;
    private int nextBookingNumber = 1001;

    public FlightBookingSystem() {
        flights = new ArrayList<Flight>();
        bookings = new ArrayList<Booking>();
        scanner = new Scanner(System.in);
        flights.add(new Flight("PN101", "Kiran Air", "Pune", "Delhi", 20, 4000.0));
        flights.add(new Flight("MB202", "Sky Wings", "Mumbai", "Bengaluru", 30, 3500.0));
        flights.add(new Flight("PN303", "Cloud Jet", "Pune", "Hyderabad", 25, 3000.0));
    }

    public void run() {
        System.out.println("Welcome to Kiran Air Booking");
        while (true) {
            System.out.println();
            System.out.println("1. View Flights");
            System.out.println("2. Book Flight");
            System.out.println("3. View Booking");
            System.out.println("4. Exit");
            String choice = readLine("Enter choice: ");
            if (choice == null) {
                break;
            }
            choice = choice.trim();
            if (choice.equals("1")) {
                displayFlights();
            } else if (choice.equals("2")) {
                bookFlight();
            } else if (choice.equals("3")) {
                viewBooking();
            } else if (choice.equals("4")) {
                System.out.println("Thank you for using Kiran Air Booking!");
                break;
            } else {
                System.out.println("Invalid choice. Please enter a number from 1 to 4.");
            }
        }
    }

    private void displayFlights() {
        System.out.println("Available Flights:");
        System.out.printf("%-14s %-14s %-12s %-14s %-18s %-12s%n", "Flight Code", "Airline", "Source", "Destination", "Available Seats", "Base Fare");
        for (int i = 0; i < flights.size(); i++) {
            Flight f = flights.get(i);
            System.out.printf("%-14s %-14s %-12s %-14s %-18d Rs. %.2f%n",
                    f.getFlightCode(),
                    f.getAirline(),
                    f.getSource(),
                    f.getDestination(),
                    f.getAvailableSeats(),
                    f.getBaseFare());
        }
    }

    private Flight findFlightByCode(String code) {
        if (code == null) {
            return null;
        }
        String trimmed = code.trim();
        for (int i = 0; i < flights.size(); i++) {
            Flight f = flights.get(i);
            if (f.getFlightCode().equalsIgnoreCase(trimmed)) {
                return f;
            }
        }
        return null;
    }

    private String validateBookingDetails(String flightCode, String name, String ageText, String typeText, String ticketsText) {
        Flight flight = findFlightByCode(flightCode);
        if (flight == null) {
            return "Booking failed: Flight code not found.";
        }
        if (name == null || name.trim().isEmpty()) {
            return "Booking failed: Passenger name cannot be blank.";
        }
        int age;
        try {
            age = Integer.parseInt(ageText.trim());
        } catch (Exception e) {
            return "Booking failed: Invalid age. Please enter a whole number.";
        }
        if (age <= 0) {
            return "Booking failed: Age must be greater than 0.";
        }
        if (typeText == null) {
            return "Booking failed: Passenger type must be Regular or Student.";
        }
        String trimmedType = typeText.trim();
        if (!trimmedType.equalsIgnoreCase("Regular") && !trimmedType.equalsIgnoreCase("Student")) {
            return "Booking failed: Passenger type must be Regular or Student.";
        }
        int tickets;
        try {
            tickets = Integer.parseInt(ticketsText.trim());
        } catch (Exception e) {
            return "Booking failed: Invalid ticket count. Please enter a whole number.";
        }
        if (tickets <= 0) {
            return "Booking failed: Ticket count must be greater than 0.";
        }
        if (tickets > flight.getAvailableSeats()) {
            return "Booking failed: Not enough seats available.";
        }
        return null;
    }

    private double calculateSubtotal(Flight flight, int tickets) {
        return flight.getBaseFare() * tickets;
    }

    private double calculateDiscount(Passenger passenger, double subtotal) {
        return passenger.calculateDiscount(subtotal);
    }

    private double calculateFinalAmount(double subtotal, double discount) {
        return subtotal - discount;
    }

    private Passenger createPassenger(String typeText, String name, int age) {
        if (typeText.trim().equalsIgnoreCase("Student")) {
            return new StudentPassenger(name, age);
        }
        return new RegularPassenger(name, age);
    }

    private Booking confirmBooking(Flight flight, Passenger passenger, int tickets) {
        if (!flight.bookSeats(tickets)) {
            return null;
        }
        double subtotal = calculateSubtotal(flight, tickets);
        double discount = calculateDiscount(passenger, subtotal);
        double finalAmount = calculateFinalAmount(subtotal, discount);
        String bookingId = "BK" + nextBookingNumber;
        Booking booking = new Booking(bookingId, flight, passenger, tickets, subtotal, discount, finalAmount);
        bookings.add(booking);
        nextBookingNumber++;
        return booking;
    }

    private void displayBookingSummary(Booking booking) {
        Flight flight = booking.getFlight();
        Passenger passenger = booking.getPassenger();
        System.out.println("Booking ID: " + booking.getBookingId());
        System.out.println("Flight: " + flight.getFlightCode() + " | " + flight.getSource() + " to " + flight.getDestination());
        System.out.println("Passenger: " + passenger.getName());
        System.out.println("Age: " + passenger.getAge());
        System.out.println("Passenger type: " + passenger.getPassengerType());
        System.out.println("Tickets: " + booking.getTickets());
        System.out.printf("Base fare per ticket: Rs. %.2f%n", flight.getBaseFare());
        System.out.printf("Subtotal: Rs. %.2f%n", booking.getSubtotal());
        System.out.printf("%s discount (%d%%): Rs. %.2f%n", passenger.getPassengerType(), (int) passenger.getDiscountPercent(), booking.getDiscount());
        System.out.printf("Final amount: Rs. %.2f%n", booking.getFinalAmount());
    }

    private void bookFlight() {
        String flightCode = readLine("Enter flight code: ");
        if (flightCode == null) {
            return;
        }
        String name = readLine("Enter passenger name: ");
        if (name == null) {
            return;
        }
        String ageText = readLine("Enter age: ");
        if (ageText == null) {
            return;
        }
        String typeText = readLine("Passenger type (Regular/Student): ");
        if (typeText == null) {
            return;
        }
        String ticketsText = readLine("Enter number of tickets: ");
        if (ticketsText == null) {
            return;
        }
        String error = validateBookingDetails(flightCode, name, ageText, typeText, ticketsText);
        if (error != null) {
            System.out.println(error);
            System.out.println("No booking was created. Available seats remain unchanged.");
            return;
        }
        Flight flight = findFlightByCode(flightCode);
        int age = Integer.parseInt(ageText.trim());
        int tickets = Integer.parseInt(ticketsText.trim());
        String trimmedName = name.trim();
        Passenger passenger = createPassenger(typeText, trimmedName, age);
        Booking booking = confirmBooking(flight, passenger, tickets);
        if (booking != null) {
            System.out.println("Booking confirmed!");
            displayBookingSummary(booking);
        }
    }

    private void viewBooking() {
        if (bookings.isEmpty()) {
            System.out.println("No booking found. Please book a flight first.");
        } else {
            System.out.println("Most Recent Booking:");
            displayBookingSummary(bookings.get(bookings.size() - 1));
        }
    }

    private String readLine(String prompt) {
        System.out.print(prompt);
        if (!scanner.hasNextLine()) {
            return null;
        }
        return scanner.nextLine();
    }
}

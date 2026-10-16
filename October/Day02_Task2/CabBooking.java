import java.util.Scanner;
import java.util.Random;

public class CabBooking {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        Random random = new Random();

        String[] cabTypes = {"Auto", "Sedan", "SUV"};
        int[] rates = {12, 18, 25};

        String[] sources = {"Pune Station", "Swargate", "Hinjawadi"};
        String[] destinations = {"Shivajinagar", "Kothrud", "Wakad"};
        int[] distances = {5, 8, 10};

        while (true) {
            System.out.println("=== Cab Booking Chatbot ===");
            System.out.println("1. Book Ride");
            System.out.println("2. Exit");
            System.out.print("Enter your choice: ");
            String mainChoice = scanner.nextLine().trim();

            if (mainChoice.equals("1")) {
                System.out.println("\nSelect Cab Type:");
                System.out.println("1. Auto - Rs. 12/km");
                System.out.println("2. Sedan - Rs. 18/km");
                System.out.println("3. SUV - Rs. 25/km");
                System.out.print("Enter choice (1-3): ");
                String cabChoice = scanner.nextLine().trim();

                int cabIndex = -1;
                if (cabChoice.equals("1")) {
                    cabIndex = 0;
                } else if (cabChoice.equals("2")) {
                    cabIndex = 1;
                } else if (cabChoice.equals("3")) {
                    cabIndex = 2;
                } else {
                    System.out.println("Invalid cab type selected. Please try again.\n");
                    continue;
                }

                System.out.println("\nSelect Route:");
                System.out.println("1. Pune Station to Shivajinagar (5 km)");
                System.out.println("2. Swargate to Kothrud (8 km)");
                System.out.println("3. Hinjawadi to Wakad (10 km)");
                System.out.print("Enter route number (1-3): ");
                String routeChoice = scanner.nextLine().trim();

                int routeIndex = -1;
                if (routeChoice.equals("1")) {
                    routeIndex = 0;
                } else if (routeChoice.equals("2")) {
                    routeIndex = 1;
                } else if (routeChoice.equals("3")) {
                    routeIndex = 2;
                } else {
                    System.out.println("Invalid route selected. Please try again.\n");
                    continue;
                }

                String cabType = cabTypes[cabIndex];
                int ratePerKm = rates[cabIndex];
                String source = sources[routeIndex];
                String destination = destinations[routeIndex];
                int distance = distances[routeIndex];
                int totalFare = ratePerKm * distance;

                System.out.println("\n--- Ride Details ---");
                System.out.println("Cab Type: " + cabType);
                System.out.println("Source: " + source);
                System.out.println("Destination: " + destination);
                System.out.println("Distance: " + distance + " km");
                System.out.println("Rate: Rs. " + ratePerKm + "/km");
                System.out.println("Total Fare: Rs. " + totalFare);

                System.out.println("\n1. Confirm & Share OTP");
                System.out.println("2. Cancel Ride");
                System.out.print("Enter your choice: ");
                String confirmChoice = scanner.nextLine().trim();

                if (confirmChoice.equals("1")) {
                    int otp = 1000 + random.nextInt(9000);
                    System.out.println("\nYour 4-digit OTP is: " + otp);
                    System.out.println("Once the OTP is shared, the ride cannot be cancelled.");
                    System.out.println("Booking confirmed! Your cab is on the way.\n");
                } else if (confirmChoice.equals("2")) {
                    System.out.println("\nRide cancelled successfully. Returning to main menu.\n");
                } else {
                    System.out.println("\nInvalid choice. Ride not booked. Returning to main menu.\n");
                }
            } else if (mainChoice.equals("2")) {
                System.out.println("Thank you for using Cab Booking Chatbot. Goodbye!");
                break;
            } else {
                System.out.println("Invalid choice. Please enter 1 or 2.\n");
            }
        }

        scanner.close();
    }
}

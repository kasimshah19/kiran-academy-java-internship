public class TravelBill {
    public static double calculateTravelStayTax(double a, double b) {
        double sum = a + b;
        return sum + (sum * 0.12);
    }

    public static double calculateCabFoodTax(double c, double d) {
        double sum = c + d;
        return sum + (sum * 0.05);
    }

    public static double calculateInsuranceTax(double e) {
        return e + (e * 0.18);
    }

    public static double totalBill(double a, double b, double c, double d, double e) {
        return calculateTravelStayTax(a, b) + calculateCabFoodTax(c, d) + calculateInsuranceTax(e);
    }

    public static double calculateDiscountedBill(double a, double b, double c, double d, double e) {
        double total = totalBill(a, b, c, d, e);
        double discount = total * 0.15;
        return total - discount;
    }

    public static void main(String[] args) {
        double a = 100;
        double b = 100;
        double c = 100;
        double d = 100;
        double e = 100;
        double total = totalBill(a, b, c, d, e);
        double finalAmount = calculateDiscountedBill(a, b, c, d, e);
        double discount = total - finalAmount;
        System.out.printf("Total after tax = %.2f | Discount = %.2f | Final payable amount = %.2f%n", total, discount, finalAmount);
    }
}

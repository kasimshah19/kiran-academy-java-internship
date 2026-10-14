public class ShoppingBill {
    public static double calculateElectronicsGST(double a, double b) {
        double sum = a + b;
        return sum + (sum * 0.18);
    }

    public static double calculateFashionGST(double c, double d) {
        double sum = c + d;
        return sum + (sum * 0.13);
    }

    public static double calculateFoodGST(double e) {
        return e + (e * 0.07);
    }

    public static double totalBill(double a, double b, double c, double d, double e) {
        return calculateElectronicsGST(a, b) + calculateFashionGST(c, d) + calculateFoodGST(e);
    }

    public static double calculateDiscountedBill(double a, double b, double c, double d, double e) {
        double total = totalBill(a, b, c, d, e);
        double discount = total * 0.20;
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
        System.out.printf("Total after GST = %.2f | Discount = %.2f | Final payable amount = %.2f%n", total, discount, finalAmount);
    }
}

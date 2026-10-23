public class StudentPassenger extends Passenger {
    public StudentPassenger(String name, int age) {
        super(name, age);
    }

    @Override
    public String getPassengerType() {
        return "Student";
    }

    @Override
    public double getDiscountPercent() {
        return 10.0;
    }

    @Override
    public double calculateDiscount(double subtotal) {
        return subtotal * getDiscountPercent() / 100.0;
    }
}

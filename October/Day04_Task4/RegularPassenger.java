public class RegularPassenger extends Passenger {
    public RegularPassenger(String name, int age) {
        super(name, age);
    }

    @Override
    public String getPassengerType() {
        return "Regular";
    }

    @Override
    public double getDiscountPercent() {
        return 0.0;
    }

    @Override
    public double calculateDiscount(double subtotal) {
        return subtotal * getDiscountPercent() / 100.0;
    }
}

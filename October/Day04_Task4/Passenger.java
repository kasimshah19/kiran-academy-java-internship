public abstract class Passenger {
    private final String name;
    private final int age;

    public Passenger(String name, int age) {
        this.name = name;
        this.age = age;
    }

    public String getName() {
        return name;
    }

    public int getAge() {
        return age;
    }

    public abstract String getPassengerType();
    public abstract double getDiscountPercent();
    public abstract double calculateDiscount(double subtotal);
}

# Internship Progress Report: October 2026

- **Intern Name:** Fakir Mohammad Kasim Salim Shah
- **Organization:** Kiran Academy, Pune
- **Program:** Full Stack Java Internship (3 months)
- **Month Covered:** October 2026
- **Repository:** https://github.com/kasimshah19/kiran-academy-java-internship
- **Tools & Environment:** Java (OpenJDK 17.0.20.1), Antigravity IDE, Git, GitHub

---

## 1. Overview

The focus of October 2026 centered on foundational Java development, local development environment configuration, and version control workflows. Day 01 established core competencies in class structures, primitive arithmetic operations, explicit type casting, and modular method-based program design across foundational programs and two multi-tier billing case studies: **Day 01 Task 1 (Shopping Bill)** and **Day 01 Task 2 (Travel Booking Bill)**. Day 02 expanded into interactive console-based applications, control flow logic, input sanitization, dynamic menu systems, and pseudo-random generation with **Day 02 Task 2 (Cab Booking Chatbot)**. All assignments were tracked, compiled, tested, and maintained under version control using Git and GitHub.

---

## 2. Monthly Summary Table

| Date | Day | Task | Folder | Status |
|:---|:---|:---|:---|:---|
| 13/10/2026 | Tuesday (Day 01) | Day 01: Environment Setup & Core Arithmetic Programs | October/Day01_Task1 | Completed |
| 14/10/2026 | Wednesday (Day 01) | Day 01 — Task 1: Shopping Bill (GST First, Discount After) | October/Day01_Task1 | Completed |
| 14/10/2026 | Wednesday (Day 01) | Day 01 — Task 2: Travel Booking Bill (Tax First, Discount After) | October/Day01_Task1 | Completed |
| 16/10/2026 | Friday (Day 02) | Day 02 — Task 2: Console-Based Cab Booking Chatbot | October/Day02_Task2 | Completed |

---

## 3. Daily Log

### 13 October 2026 (Tuesday) — Day 01: Environment Setup & Core Arithmetic Programs

- **Date and Day:** 13 October 2026, Tuesday (Day 01)
- **Task Assigned:** Install JDK, configure local development workspace, create initial project directory structure, write 6 basic Java programs, commit and push source code to GitHub, and submit repository link via email.
- **Problem Statement:**
  Set up the local Java development environment from scratch, establish structured project directories, and develop fundamental standalone Java programs to verify compiler setup and perform basic mathematical computations (binary addition, subtraction, multiplication, double-precision division, and modulus) without compilation errors.
- **Objective:** Establish a working Java runtime and compiler environment, verify console output functionality, and implement fundamental arithmetic operations using Java primitive types.
- **Requirements:**
  - Verify Java Development Kit (JDK) installation and environment variables.
  - Create separate Java source files for standard output and core arithmetic operations.
  - Implement integer arithmetic operations (addition, subtraction, multiplication, and modulus) using test values `a = 20` and `b = 3`.
  - Implement floating-point division using explicit double type casting to avoid integer truncation.
  - Ensure zero compilation errors and follow standard Java naming conventions.
- **Solution & Implementation Approach:**
  - Configured workspace directory structure under `October/Day01_Task1` using OpenJDK 17.0.20.1 and Antigravity IDE.
  - Formulated six standalone, self-contained classes (`Hello`, `Addition`, `Difference`, `Multiplication`, `Division`, `Modulus`).
  - Resolved integer truncation in division by explicitly type-casting the numerator to `double` (`(double) a / b`), ensuring precise floating-point quotient output.
  - Implemented dynamic string concatenation within `System.out.println()` for clear equation presentation.
  - Initialized local Git version tracking and pushed the initial codebase to the remote GitHub repository.
- **Work Done:**
  - Configured workspace directory structure under `October/Day01_Task1`.
  - Created 6 independent Java source files covering basic output and arithmetic operators.

| File | Purpose |
|:---|:---|
| `Hello.java` | Verifies standard output stream execution by printing `Hello World`. |
| `Addition.java` | Computes binary addition of two integers (`a + b`). |
| `Difference.java` | Computes binary subtraction between two integers (`a - b`). |
| `Multiplication.java` | Computes binary multiplication of two integers (`a * b`). |
| `Division.java` | Computes precise quotient using explicit double casting (`(double) a / b`). |
| `Modulus.java` | Computes the remainder of integer division using the modulo operator (`a % b`). |

- **Sample Output:**

```text
Hello World
Addition of 20 and 3 = 23
Difference of 20 and 3 = 17
Multiplication of 20 and 3 = 60
Division of 20 and 3 = 6.666666666666667
Modulus of 20 and 3 = 2
```

- **Concepts Learned:**
  - Java Development Kit (JDK) architecture and command-line execution (`javac`, `java`).
  - Class declaration and source file naming alignment.
  - Entry-point definition with `public static void main(String[] args)`.
  - Primitive data types (`int`, `double`) and variable declarations.
  - Arithmetic operators (`+`, `-`, `*`, `/`, `%`).
  - Explicit widening type casting to prevent integer division truncation.
  - Console string concatenation with `System.out.println()`.
  - Directory management and remote repository synchronization via Git and GitHub.
- **Status:** Completed and Submitted

---

### 14 October 2026 (Wednesday) — Day 01, Task 1: Shopping Bill (GST First, Discount After)

- **Date and Day:** 14 October 2026, Wednesday (Day 01 — Task 1)
- **Task Assigned:** Day 01 Task 1 Case Study: Construct a modular Shopping Bill calculation program in Java (`ShoppingBill.java`) inside `October/Day01_Task1`, where item amounts are supplied directly via method arguments, calculation methods return numerical values without console printing, and a multi-tier tax and discount model is applied.
- **Problem Statement:**
  As the first practical case study for Day 01, build an automated shopping billing system in Java that computes category-specific Goods and Services Tax (Electronics at 18% GST, Fashion at 13% GST, and Food at 7% GST), aggregates total post-tax billing, and derives the final payable amount after applying a 20% discount. The system must strictly disallow interactive or command-line input, require calculation methods to return numerical values without console printing, and format the final summary line with two decimal places.
- **Objective:** Design reusable Java methods with input parameters and return types, chain methods hierarchically, and format monetary computations cleanly.
- **Requirements:**
  - Avoid user input prompts (`Scanner`, `System.in`, command-line arguments); pass values directly in method calls.
  - Define `calculateElectronicsGST(a, b)` returning sum plus 18% GST.
  - Define `calculateFashionGST(c, d)` returning sum plus 13% GST.
  - Define `calculateFoodGST(e)` returning amount plus 7% GST.
  - Define `totalBill(a, b, c, d, e)` aggregating the three GST method returns.
  - Define `calculateDiscountedBill(a, b, c, d, e)` applying a 20% discount on totalBill.
  - Execute test scenario using `a = 100`, `b = 100`, `c = 100`, `d = 100`, `e = 100`.
  - Print single formatted summary line with values rounded to 2 decimal places using `System.out.printf`.
  - Maintain the file inside `October/Day01_Task1`.
- **Solution & Implementation Approach:**
  - Created `ShoppingBill.java` structured with five single-responsibility static calculation methods and a dedicated `main` driver method.
  - Separated category tax computations into dedicated functions returning `double` precision values: `calculateElectronicsGST`, `calculateFashionGST`, and `calculateFoodGST`.
  - Built a composite `totalBill` method that chains and aggregates returns from all three category functions.
  - Implemented `calculateDiscountedBill` by calling `totalBill()`, computing 20% discount (`total * 0.20`), and subtracting it to yield the final payable sum.
  - Hardcoded test parameters directly in `main` (`a = b = c = d = e = 100`), calculated the discount differential (`total - finalAmount`), and formatted the output using `System.out.printf` with `%.2f` specifiers.
- **Work Done:**
  - Created `ShoppingBill.java` within `October/Day01_Task1` containing 5 static helper methods and a `main` execution method as Day 01 Task 1.
  - Implemented the complete tax and discount pipeline returning `double` precision amounts.

| File | Task | Purpose |
|:---|:---|:---|
| `ShoppingBill.java` | Day 01 — Task 1 | Modular billing engine calculating category GST rates, total bill summation, and discount deductions. |

  **Calculation Flow (`a = b = c = d = e = 100`):**

| Step | Method | Calculation | Result |
|:---|:---|:---|:---|
| 1 | `calculateElectronicsGST(a, b)` | `(100 + 100) + 18% GST = 200.00 + 36.00` | `236.00` |
| 2 | `calculateFashionGST(c, d)` | `(100 + 100) + 13% GST = 200.00 + 26.00` | `226.00` |
| 3 | `calculateFoodGST(e)` | `100 + 7% GST = 100.00 + 7.00` | `107.00` |
| 4 | `totalBill(a, b, c, d, e)` | `236.00 + 226.00 + 107.00` | `569.00` |
| 5 | `calculateDiscountedBill(a, b, c, d, e)` | `569.00 - (20% of 569.00 = 113.80)` | `455.20` |

- **Sample Output:**

```text
Total after GST = 569.00 | Discount = 113.80 | Final payable amount = 455.20
```

- **Concepts Learned:**
  - Method signatures, parameter lists, and return types.
  - Functional decomposition and separation of calculation logic from display logic.
  - Nested method invocation (`totalBill` within `calculateDiscountedBill`).
  - Floating-point mathematical calculations using `double`.
  - String formatting and precision specification (`%.2f`) using `System.out.printf`.
  - Modular code design promoting maintainability and reusability.
- **Status:** Completed and Submitted

---

### 14 October 2026 (Wednesday) — Day 01, Task 2: Travel Booking Bill (Tax First, Discount After)

- **Date and Day:** 14 October 2026, Wednesday (Day 01 — Task 2)
- **Task Assigned:** Day 01 Task 2 Case Study: Construct a modular Travel Booking Bill calculation program in Java (`TravelBill.java`) inside `October/Day01_Task1` as the second practical task of Day 01, where booking amounts are supplied directly via method parameters, calculation methods return numerical values without console printing, and a multi-tier tax and discount model is applied.
- **Problem Statement:**
  As the second case study for Day 01 (`October/Day01_Task1`), develop an automated travel reservation billing system in Java that calculates tiered service taxes (Flight + Hotel at 12% tax, Cab + Food at 5% tax, and Travel Insurance at 18% tax), sums the post-tax bill across all booking categories, and determines the final payable amount after applying a promotional 15% discount. The implementation must strictly forbid interactive inputs (`Scanner`, CLI arguments), require all calculation methods to return numerical values without console printing, and format the final single-line summary with two decimal places.
- **Objective:** Design reusable, single-responsibility Java static methods with parameter passing and return values, execute composite method aggregation, and format monetary computations cleanly.
- **Requirements:**
  - Avoid user input prompts (`Scanner`, `System.in`, command-line arguments); pass values directly in method calls.
  - Define `calculateTravelStayTax(a, b)` returning sum of flight and hotel plus 12% tax.
  - Define `calculateCabFoodTax(c, d)` returning sum of cab and food expenses plus 5% tax.
  - Define `calculateInsuranceTax(e)` returning insurance amount plus 18% tax.
  - Define `totalBill(a, b, c, d, e)` aggregating the returns of the three tax calculation methods.
  - Define `calculateDiscountedBill(a, b, c, d, e)` applying a 15% discount on the total bill and returning the final payable amount.
  - Execute test scenario using `a = 100`, `b = 100`, `c = 100`, `d = 100`, `e = 100`.
  - Print single formatted summary line with values rounded to 2 decimal places using `System.out.printf` and no currency symbol.
  - Maintain clean code with zero comments (no `//`, `/* */`, or Javadoc) and no external packages/dependencies.
  - Maintain the file inside `October/Day01_Task1`.
- **Solution & Implementation Approach:**
  - Created `TravelBill.java` structured with five single-responsibility `public static` calculation methods and a dedicated `main` driver method.
  - Encapsulated service category tax computations into dedicated functions returning `double` precision values:
    - `calculateTravelStayTax(a, b)`: computes `sum = a + b` and returns `sum + (sum * 0.12)`.
    - `calculateCabFoodTax(c, d)`: computes `sum = c + d` and returns `sum + (sum * 0.05)`.
    - `calculateInsuranceTax(e)`: returns `e + (e * 0.18)`.
  - Built an aggregator method `totalBill(a, b, c, d, e)` that invokes and sums the results of all three tax methods.
  - Implemented `calculateDiscountedBill(a, b, c, d, e)` by calling `totalBill()`, computing a 15% discount (`total * 0.15`), and returning `total - discount`.
  - In `main`, supplied fixed test values `a = b = c = d = e = 100`, computed `discount = total - finalAmount`, and printed the exact summary line with `System.out.printf`.
- **Work Done:**
  - Created `TravelBill.java` within `October/Day01_Task1` containing 5 static calculation methods and a `main` execution method as Day 01 Task 2.
  - Successfully completed all 8 Java source files under Day 01 (`October/Day01_Task1`): 6 basic arithmetic operators, ShoppingBill (Task 1), and TravelBill (Task 2).
  - Implemented the complete tax calculation and discount pipeline using `double` precision arithmetic.
  - Verified compilation and runtime output (`javac` and `java`), ensuring zero errors and exact decimal formatting.

| File | Task | Purpose |
|:---|:---|:---|
| `TravelBill.java` | Day 01 — Task 2 | Modular travel booking engine calculating category tax rates (12%, 5%, 18%), total bill summation, and discount deduction. |

  **Calculation Flow (`a = b = c = d = e = 100`):**

| Step | Method | Calculation | Result |
|:---|:---|:---|:---|
| 1 | `calculateTravelStayTax(a, b)` | `(100 + 100) + 12% Tax = 200.00 + 24.00` | `224.00` |
| 2 | `calculateCabFoodTax(c, d)` | `(100 + 100) + 5% Tax = 200.00 + 10.00` | `210.00` |
| 3 | `calculateInsuranceTax(e)` | `100 + 18% Tax = 100.00 + 18.00` | `118.00` |
| 4 | `totalBill(a, b, c, d, e)` | `224.00 + 210.00 + 118.00` | `552.00` |
| 5 | `calculateDiscountedBill(a, b, c, d, e)` | `552.00 - (15% of 552.00 = 82.80)` | `469.20` |

- **Sample Output:**

```text
Total after tax = 552.00 | Discount = 82.80 | Final payable amount = 469.20
```

- **Concepts Learned:**
  - Modular static method design with strong typing (`double` parameters and return values).
  - Clean separation of computational business logic from console display output.
  - Composite method aggregation and multi-level function delegation.
  - Mathematical modeling of tiered tax additions and percentage discount deductions.
  - String formatting precision control using `System.out.printf` (`%.2f`).
  - Writing clean, self-documenting Java code following strict engineering guidelines.
- **Status:** Completed and Submitted

---

### 16 October 2026 (Friday) — Day 02, Task 2: Console-Based Cab Booking Chatbot

- **Date and Day:** 16 October 2026, Friday (Day 02 — Task 2)
- **Task Assigned:** Day 02 Task 2 Case Study: Construct an interactive, crash-proof console-based Cab Booking Chatbot in Java (`CabBooking.java`) inside `October/Day02_Task2`, utilizing predefined routes, per-kilometer rate matrices, robust user input handling with `Scanner`, infinite menu iteration (`while (true)` with `break` on exit), pre-OTP cancellation mechanisms, and secure 4-digit OTP generation with an irreversible ride-locking policy.
- **Problem Statement:**
  Develop a conversational command-line chatbot for on-demand cab reservations. The chatbot must guide the user step-by-step through cab type selection, predefined route selection, transparent fare calculation, booking confirmation or cancellation, and OTP generation. The application must be resilient against invalid inputs (letters, non-numeric strings, empty lines) by reading selections as strings to prevent program crashes. It must provide a cancellation option prior to OTP generation, but once confirmed, issue a random 4-digit OTP and enforce the rule: *"Once the OTP is shared, the ride cannot be cancelled."* Code must adhere to clean engineering guidelines with zero comments and minimal standard library dependencies (`java.util.Scanner`, `java.util.Random`).
- **Objective:** Master interactive console input handling, loop-based state machine architecture (`while (true)` with `break` and `continue`), multi-branch conditional logic, robust String parsing, and pseudo-random number generation for OTP workflows.
- **Requirements:**
  - Create a single Java file: `October/Day02_Task2/CabBooking.java` (`public class CabBooking`).
  - Store predefined routes with fixed distances:
    1. Pune Station to Shivajinagar (5 km)
    2. Swargate to Kothrud (8 km)
    3. Hinjawadi to Wakad (10 km)
  - Store predefined cab categories and per-km pricing:
    1. Auto (Rs. 12/km)
    2. Sedan (Rs. 18/km)
    3. SUV (Rs. 25/km)
  - Use integer arithmetic (`int`) for distances, rates, and total fare (`fare = ratePerKm * distance`).
  - Display monetary values using the "Rs." prefix without unicode currency symbols.
  - Implement a central `while (true)` loop for the main menu offering "1. Book Ride" and "2. Exit".
  - Read all inputs via a single `Scanner` using `scanner.nextLine()` with String comparisons (e.g. `.equals("1")`) to guarantee crash immunity.
  - Gracefully handle invalid selections across all menu levels by displaying descriptive error messages and resetting to the main menu.
  - Provide a cancellation checkpoint before OTP issuance ("1. Confirm & Share OTP", "2. Cancel Ride").
  - On confirmation, generate a random 4-digit OTP (1000–9999) using `java.util.Random` and display the explicit non-cancellation policy: *"Once the OTP is shared, the ride cannot be cancelled."*
  - Ensure clean code standards with zero comments (`//`, `/* */`, or Javadoc).
- **Solution & Implementation Approach:**
  - Structured `CabBooking.java` with parallel arrays to hold cab categories (`{"Auto", "Sedan", "SUV"}`), per-km tariff rates (`{12, 18, 25}`), route source locations, destinations, and corresponding kilometer distances (`{5, 8, 10}`).
  - Structured the program lifecycle around an infinite `while (true)` loop acting as a conversational state machine.
  - Utilized `scanner.nextLine().trim()` on a single `Scanner` instance for all user interactions, avoiding scanner buffer desynchronization issues and handling non-numeric or blank inputs gracefully.
  - Designed the reservation pipeline into sequential phases:
    1. **Main Navigation:** Routes user to ride booking or exits loop via `break`.
    2. **Vehicle Category Selection:** Maps choice `1..3` to array index; routes invalid choices back to the main loop with `continue`.
    3. **Route Selection:** Maps choice `1..3` to source, destination, and distance metrics.
    4. **Fare Computation & Summary:** Multiplies rate by distance, presenting an itemized summary (Cab Type, Source, Destination, Distance, Rate, Total Fare).
    5. **Pre-OTP Confirmation / Cancellation Gate:** Offers explicit confirmation or cancellation. If cancelled, logs a cancellation notice and returns to main menu with zero OTP exposure.
    6. **OTP Generation & Ride Lock:** Uses `1000 + random.nextInt(9000)` to securely issue a 4-digit pin, outputting the mandatory rule *"Once the OTP is shared, the ride cannot be cancelled."* and confirming booking.
    7. **Clean Exit:** Breaks from the loop and prints a friendly departure message.
- **Work Done:**
  - Created `CabBooking.java` within `October/Day02_Task2/`.
  - Removed obsolete `.gitkeep` placeholder file.
  - Verified compilation with `javac` and executed end-to-end regression tests across all 5 mandatory scenarios.
  - Cleaned up all `.class` artifacts to maintain repository cleanliness.

| File | Task | Purpose |
|:---|:---|:---|
| `CabBooking.java` | Day 02 — Task 2 | Interactive console cab booking chatbot featuring crash-safe menu navigation, route-based fare calculation, pre-OTP cancellation, and OTP lock mechanism. |

  **Predefined Data Matrices:**

| Category | Type | Rate / km |
|:---|:---|:---|
| 1 | Auto | Rs. 12 |
| 2 | Sedan | Rs. 18 |
| 3 | SUV | Rs. 25 |

| Route No. | Source | Destination | Distance |
|:---|:---|:---|:---|
| 1 | Pune Station | Shivajinagar | 5 km |
| 2 | Swargate | Kothrud | 8 km |
| 3 | Hinjawadi | Wakad | 10 km |

- **Sample Output (Successful Booking Flow):**

```text
=== Cab Booking Chatbot ===
1. Book Ride
2. Exit
Enter your choice: 1

Select Cab Type:
1. Auto - Rs. 12/km
2. Sedan - Rs. 18/km
3. SUV - Rs. 25/km
Enter choice (1-3): 2

Select Route:
1. Pune Station to Shivajinagar (5 km)
2. Swargate to Kothrud (8 km)
3. Hinjawadi to Wakad (10 km)
Enter route number (1-3): 3

--- Ride Details ---
Cab Type: Sedan
Source: Hinjawadi
Destination: Wakad
Distance: 10 km
Rate: Rs. 18/km
Total Fare: Rs. 180

1. Confirm & Share OTP
2. Cancel Ride
Enter your choice: 1

Your 4-digit OTP is: 3931
Once the OTP is shared, the ride cannot be cancelled.
Booking confirmed! Your cab is on the way.

=== Cab Booking Chatbot ===
1. Book Ride
2. Exit
Enter your choice: 2
Thank you for using Cab Booking Chatbot. Goodbye!
```

- **Sample Output (Pre-OTP Cancellation Flow):**

```text
=== Cab Booking Chatbot ===
1. Book Ride
2. Exit
Enter your choice: 1

Select Cab Type:
1. Auto - Rs. 12/km
2. Sedan - Rs. 18/km
3. SUV - Rs. 25/km
Enter choice (1-3): 1

Select Route:
1. Pune Station to Shivajinagar (5 km)
2. Swargate to Kothrud (8 km)
3. Hinjawadi to Wakad (10 km)
Enter route number (1-3): 1

--- Ride Details ---
Cab Type: Auto
Source: Pune Station
Destination: Shivajinagar
Distance: 5 km
Rate: Rs. 12/km
Total Fare: Rs. 60

1. Confirm & Share OTP
2. Cancel Ride
Enter your choice: 2

Ride cancelled successfully. Returning to main menu.

=== Cab Booking Chatbot ===
1. Book Ride
2. Exit
Enter your choice: 2
Thank you for using Cab Booking Chatbot. Goodbye!
```

- **Sample Output (Invalid Input Handling & Safe Recovery):**

```text
=== Cab Booking Chatbot ===
1. Book Ride
2. Exit
Enter your choice: 5
Invalid choice. Please enter 1 or 2.

=== Cab Booking Chatbot ===
1. Book Ride
2. Exit
Enter your choice: 2
Thank you for using Cab Booking Chatbot. Goodbye!
```

- **Sample Output (Consecutive Sessions — Booking Followed by Cancellation):**

```text
=== Cab Booking Chatbot ===
1. Book Ride
2. Exit
Enter your choice: 1

Select Cab Type:
1. Auto - Rs. 12/km
2. Sedan - Rs. 18/km
3. SUV - Rs. 25/km
Enter choice (1-3): 3

Select Route:
1. Pune Station to Shivajinagar (5 km)
2. Swargate to Kothrud (8 km)
3. Hinjawadi to Wakad (10 km)
Enter route number (1-3): 2

--- Ride Details ---
Cab Type: SUV
Source: Swargate
Destination: Kothrud
Distance: 8 km
Rate: Rs. 25/km
Total Fare: Rs. 200

1. Confirm & Share OTP
2. Cancel Ride
Enter your choice: 1

Your 4-digit OTP is: 2338
Once the OTP is shared, the ride cannot be cancelled.
Booking confirmed! Your cab is on the way.

=== Cab Booking Chatbot ===
1. Book Ride
2. Exit
Enter your choice: 1

Select Cab Type:
1. Auto - Rs. 12/km
2. Sedan - Rs. 18/km
3. SUV - Rs. 25/km
Enter choice (1-3): 2

Select Route:
1. Pune Station to Shivajinagar (5 km)
2. Swargate to Kothrud (8 km)
3. Hinjawadi to Wakad (10 km)
Enter route number (1-3): 1

--- Ride Details ---
Cab Type: Sedan
Source: Pune Station
Destination: Shivajinagar
Distance: 5 km
Rate: Rs. 18/km
Total Fare: Rs. 90

1. Confirm & Share OTP
2. Cancel Ride
Enter your choice: 2

Ride cancelled successfully. Returning to main menu.

=== Cab Booking Chatbot ===
1. Book Ride
2. Exit
Enter your choice: 2
Thank you for using Cab Booking Chatbot. Goodbye!
```

- **Test Scenarios Verified:**
  - **Sample Booking Scenario:** `1 -> 2 -> 3 -> 1 -> 2` (Sedan, Hinjawadi to Wakad, 10 km, fare Rs. 180, OTP generated, lock rule displayed, clean exit).
  - **Cancellation Scenario:** `1 -> 1 -> 1 -> 2 -> 2` (Auto, Pune Station to Shivajinagar, fare Rs. 60, cancelled cleanly with NO OTP generated).
  - **Invalid Main Menu Input:** `abc -> 5 -> 2` (Proper error messages displayed without program termination or exceptions).
  - **Invalid Cab Selection:** `1 -> 9 -> 2` (Error prompt displayed, gracefully returned to main menu).
  - **Multiple Consecutive Bookings:** `1 -> 3 -> 2 -> 1 -> 1 -> 2 -> 1 -> 2 -> 2` (First ride booked with OTP, second ride cancelled, session exited cleanly).
- **Concepts Learned:**
  - Interactive console application architecture using `java.util.Scanner`.
  - Defensive input handling using `nextLine()` and `.equals()` to prevent `InputMismatchException`.
  - Infinite loop control flow patterns using `while (true)`, `break`, and `continue`.
  - Data structure mapping using indexed parallel arrays.
  - Pseudo-random number generation using `java.util.Random` for bounded ranges (`1000..9999`).
  - Business workflow simulation: pre-authorization cancellation versus irrevocable OTP lock states.
  - Zero-comment clean coding practices.
- **Status:** Completed and Verified

---

## 4. Key Learnings of the Month

- Java runtime configuration and development environment setup with JDK 17 and Antigravity IDE.
- Practical understanding of Java syntax, primitive data types, arithmetic operators, and explicit type conversion.
- Designing modular, parameterized methods with return values to cleanly separate computation from output.
- Formatting floating-point numbers to fixed decimal places using `printf` specifiers.
- Interactive console application design with `java.util.Scanner` and defensive String-based input evaluation to prevent crashes.
- State-machine control flow modeling using infinite loops (`while (true)`) paired with `break` and `continue` statements.
- Pseudo-random number generation using `java.util.Random` for security workflows such as OTP generation.
- Implementing structured folder conventions for multi-day internship coursework.
- Utilizing Git for local version tracking, atomic commits, and synchronization with GitHub repositories.

---

## 5. Next Steps

Continue daily practical problem-solving to strengthen core Java programming proficiency. Expand knowledge across intermediate object-oriented principles, control flow structures, and data handling. Maintain consistent documentation and version control discipline across all upcoming assignments.

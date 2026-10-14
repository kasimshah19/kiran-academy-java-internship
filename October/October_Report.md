# Internship Progress Report: October 2026

- **Intern Name:** Fakir Mohammad Kasim Salim Shah
- **Organization:** Kiran Academy, Pune
- **Program:** Full Stack Java Internship (3 months)
- **Month Covered:** October 2026
- **Repository:** https://github.com/kasimshah19/kiran-academy-java-internship
- **Tools & Environment:** Java (OpenJDK 17.0.20.1), Antigravity IDE, Git, GitHub

---

## 1. Overview

The focus of October 2026 centered on foundational Java development, local development environment configuration, and version control workflows. Core competencies established during this period included standard class structures, primitive arithmetic operations, explicit type casting, and modular method-based program design. All assignments were tracked, compiled, tested, and maintained under version control using Git and GitHub.

---

## 2. Monthly Summary Table

| Date | Day | Task | Folder | Status |
|:---|:---|:---|:---|:---|
| 13/10/2026 | Tuesday | Task 1: Environment Setup and Basic Programs | October/Day01_Task1 | Completed |
| 14/10/2026 | Wednesday | Task 01: Shopping Bill (GST First, Discount After) | October/Day01_Task1 | Completed |

---

## 3. Daily Log

### 13 October 2026 (Tuesday)

- **Date and Day:** 13 October 2026, Tuesday
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

### 14 October 2026 (Wednesday)

- **Date and Day:** 14 October 2026, Wednesday
- **Task Assigned:** Construct a modular Shopping Bill calculation program in Java where item amounts are supplied directly via method arguments, calculation methods return numerical values without console printing, and a multi-tier tax and discount model is applied.
- **Problem Statement:**
  Build an automated shopping billing system in Java that computes category-specific Goods and Services Tax (Electronics at 18% GST, Fashion at 13% GST, and Food at 7% GST), aggregates total post-tax billing, and derives the final payable amount after applying a 20% discount. The system must strictly disallow interactive or command-line input, require calculation methods to return numerical values without console printing, and format the final summary line with two decimal places.
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
  - Created `ShoppingBill.java` within `October/Day01_Task1` containing 5 static helper methods and a `main` execution method.
  - Implemented the complete tax and discount pipeline returning `double` precision amounts.

| File | Purpose |
|:---|:---|
| `ShoppingBill.java` | Modular billing engine calculating category GST rates, total bill summation, and discount deductions. |

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

## 4. Key Learnings of the Month

- Java runtime configuration and development environment setup with JDK 17 and Antigravity IDE.
- Practical understanding of Java syntax, data types, arithmetic operators, and explicit type conversion.
- Designing modular, parameterized methods with return values to cleanly separate computation from output.
- Formatting floating-point numbers to fixed decimal places using `printf` specifiers.
- Implementing structured folder conventions for multi-day internship coursework.
- Utilizing Git for local version tracking, atomic commits, and synchronization with GitHub repositories.

---

## 5. Next Steps

Continue daily practical problem-solving to strengthen core Java programming proficiency. Expand knowledge across intermediate object-oriented principles, control flow structures, and data handling. Maintain consistent documentation and version control discipline across all upcoming assignments.

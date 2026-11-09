import java.util.*;
import java.util.stream.Collectors;

/**
 * Menu-driven Student Management System.
 *
 * Structure:
 *   main()            -> only starts the program
 *   run()             -> the menu loop (display menu, read choice, switch)
 *   one method per menu option (Tasks 1-5)
 *   input helpers     -> validated reading from Scanner
 */
public class StudentManagementSystem {

    private static final double PASS_MARK = 65.0;      // Task 2: average >= 65 passes
    private static final double BONUS_THRESHOLD = 50.0; // Task 5: average strictly above 50
    private static final double BONUS_MARKS = 10.0;
    private static final double MAX_MARK = 100.0;

    private static final Scanner scanner = new Scanner(System.in);

    // The main collection required by the assignment
    private static final List<Student> students = new ArrayList<>();

    // Task 5 bookkeeping, so a bonus is applied only once per student.
    // Key = student id, value = the average BEFORE the bonus.
    private static final Map<Integer, Double> originalAverages = new HashMap<>();

    public static void main(String[] args) {
        run();
    }

    // =====================================================================
    // MENU
    // =====================================================================

    private static void run() {
        boolean running = true;
        while (running) {
            printMenu();
            int choice = readInt("");   // validated: must be a whole number

            // switch expression/statement processes the menu choice
            switch (choice) {
                case 1 -> registerStudent();
                case 2 -> displayAllStudents();
                case 3 -> searchStudentById();
                case 4 -> filterPassingStudents();
                case 5 -> filterFailingStudents();
                case 6 -> searchStudentsByName();
                case 7 -> sortStudentsByName();
                case 8 -> sortStudentsByAverage();
                case 9 -> displayAllCourses();
                case 10 -> displayUniqueCourses();
                case 11 -> searchStudentsByCourse();
                case 12 -> displayUniqueSkills();
                case 13 -> groupStudentsByCourse();
                case 14 -> viewPerformanceStatistics();
                case 15 -> viewBestStudent();
                case 16 -> viewTopThree();
                case 17 -> applyBonusMarks();
                case 18 -> viewFinalRanking();
                case 0 -> {
                    System.out.println("Goodbye!");
                    running = false;
                }
                default -> System.out.println("Invalid option. Please choose a number from the menu (0-18).");
            }
        }
    }

    private static void printMenu() {
        System.out.println();
        System.out.println("==========================================");
        System.out.println("STUDENT MANAGEMENT SYSTEM");
        System.out.println("==========================================");
        System.out.println("1. Register New Student");
        System.out.println("2. Display All Students");
        System.out.println("3. Search Student by ID");
        System.out.println("4. Filter Passing Students");
        System.out.println("5. Filter Failing Students");
        System.out.println("6. Search Students by Name");
        System.out.println("7. Sort Students by Name");
        System.out.println("8. Sort Students by Average Marks");
        System.out.println("9. Display All Courses");
        System.out.println("10. Display Unique Courses");
        System.out.println("11. Search Students by Course");
        System.out.println("12. Display Unique Skills");
        System.out.println("13. Group Students by Course");
        System.out.println("14. View Student Performance Statistics");
        System.out.println("15. View Best Performing Student");
        System.out.println("16. View Top 3 Students");
        System.out.println("17. Apply Bonus Marks");
        System.out.println("18. View Final Student Ranking");
        System.out.println();
        System.out.println("0. Exit");
        System.out.println();
        System.out.println("==========================================");
        System.out.print("Enter your choice: ");
    }

    // =====================================================================
    // TASK 1: Registration and management
    // Streams: stream(), forEach(), filter(), findFirst(), anyMatch()
    // =====================================================================

    private static void registerStudent() {
        System.out.println("\n--- Register New Student ---");

        int id = readInt("Enter student ID (positive number): ");
        if (id <= 0) {
            System.out.println("ID must be a positive number.");
            return;
        }
        // anyMatch() -> true if at least one student already has this ID
        if (students.stream().anyMatch(s -> s.getId() == id)) {
            System.out.println("A student with ID " + id + " already exists. Registration cancelled.");
            return;
        }

        String name = readNonEmpty("Enter student name: ");
        int age = readIntInRange("Enter age (1-120): ", 1, 120);

        int numCourses = readIntInRange("Enter number of courses (at least 1): ", 1, 50);
        Map<String, Double> courses = new HashMap<>();
        for (int i = 1; i <= numCourses; i++) {
            String course = readNonEmpty("  Course " + i + " name: ");
            // Reject the same course twice (ignoring upper/lower case)
            boolean duplicate = courses.keySet().stream().anyMatch(c -> c.equalsIgnoreCase(course));
            if (duplicate) {
                System.out.println("  You already entered that course. Try again.");
                i--;            // repeat this course number
                continue;
            }
            double mark = readDoubleInRange("  Marks for " + course + " (0-100): ", 0, 100);
            courses.put(course, mark);
        }

        System.out.print("Enter skills separated by commas (or press Enter for none): ");
        String skillLine = scanner.nextLine().trim();
        List<String> skills = new ArrayList<>();
        if (!skillLine.isEmpty()) {
            skills = Arrays.stream(skillLine.split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .collect(Collectors.toList());
        }

        students.add(new Student(id, name, age, courses, skills));
        System.out.println("Student registered successfully!");
    }

    private static void displayAllStudents() {
        if (!hasStudents()) return;
        System.out.println("\n--- All Registered Students ---");
        students.stream().forEach(System.out::println);
    }

    private static void searchStudentById() {
        if (!hasStudents()) return;
        int id = readInt("Enter student ID to search: ");
        // filter() keeps matches, findFirst() returns an Optional
        students.stream()
                .filter(s -> s.getId() == id)
                .findFirst()
                .ifPresentOrElse(
                        s -> System.out.println("Found: " + s),
                        () -> System.out.println("No student found with ID " + id));
    }

    // =====================================================================
    // TASK 2: Search, filter, sort
    // Streams: filter(), map(), sorted(), collect(), average()
    // =====================================================================

    private static void filterPassingStudents() {
        if (!hasStudents()) return;
        List<Student> passing = students.stream()
                .filter(s -> s.getAverage() >= PASS_MARK)
                .collect(Collectors.toList());
        printList("Students with average >= " + PASS_MARK, passing);
    }

    private static void filterFailingStudents() {
        if (!hasStudents()) return;
        List<Student> failing = students.stream()
                .filter(s -> s.getAverage() < PASS_MARK)
                .collect(Collectors.toList());
        printList("Students with average < " + PASS_MARK, failing);
    }

    private static void searchStudentsByName() {
        if (!hasStudents()) return;
        String keyword = readNonEmpty("Enter name (or part of a name): ").toLowerCase();
        List<Student> found = students.stream()
                .filter(s -> s.getStudentName().toLowerCase().contains(keyword))
                .collect(Collectors.toList());
        printList("Students matching '" + keyword + "'", found);
    }

    private static void sortStudentsByName() {
        if (!hasStudents()) return;
        List<Student> sorted = students.stream()
                .sorted(Comparator.comparing(Student::getStudentName, String.CASE_INSENSITIVE_ORDER))
                .collect(Collectors.toList());
        printList("Students sorted by name (A-Z)", sorted);
    }

    private static void sortStudentsByAverage() {
        if (!hasStudents()) return;
        List<Student> sorted = sortedByAverageDesc();
        System.out.println("\n--- Students sorted by average (highest to lowest) ---");
        // map() converts each Student into a display String
        sorted.stream()
                .map(s -> String.format("%-20s (ID %d)  Average: %.2f", s.getStudentName(), s.getId(), s.getAverage()))
                .forEach(System.out::println);
    }

    // =====================================================================
    // TASK 3: Courses and skills
    // Streams: flatMap(), distinct(), filter(), sorted(), groupingBy()
    // =====================================================================

    private static void displayAllCourses() {
        if (!hasStudents()) return;
        System.out.println("\n--- All courses registered (one line per student's course) ---");
        // flatMap flattens many Streams<String> (one per student) into one stream
        students.stream()
                .flatMap(s -> s.getCourses().keySet().stream())
                .sorted()
                .forEach(c -> System.out.println("  " + c));
    }

    private static void displayUniqueCourses() {
        if (!hasStudents()) return;
        System.out.println("\n--- Unique courses ---");
        students.stream()
                .flatMap(s -> s.getCourses().keySet().stream())
                .distinct()
                .sorted()
                .forEach(c -> System.out.println("  " + c));
    }

    private static void searchStudentsByCourse() {
        if (!hasStudents()) return;
        String course = readNonEmpty("Enter course name: ");
        List<Student> found = students.stream()
                .filter(s -> s.getCourses().keySet().stream().anyMatch(c -> c.equalsIgnoreCase(course)))
                .collect(Collectors.toList());
        printList("Students registered in '" + course + "'", found);
    }

    private static void displayUniqueSkills() {
        if (!hasStudents()) return;
        List<String> skills = students.stream()
                .flatMap(s -> s.getSkills().stream())
                .map(String::trim)
                .distinct()
                .sorted(String.CASE_INSENSITIVE_ORDER)
                .collect(Collectors.toList());
        System.out.println("\n--- Unique skills ---");
        if (skills.isEmpty()) {
            System.out.println("No skills have been recorded.");
        } else {
            skills.forEach(s -> System.out.println("  " + s));
        }
    }

    private static void groupStudentsByCourse() {
        if (!hasStudents()) return;
        /*
         * A student can be in several courses, but groupingBy() puts each element in ONE group.
         * Trick: first flatMap every student into (course, student) pairs, one pair per course,
         * then group those pairs by the course name. That way a student appears in every group.
         */
        Map<String, List<String>> grouped = students.stream()
                .flatMap(s -> s.getCourses().keySet().stream()
                        .map(course -> Map.entry(course, s.getStudentName())))
                .collect(Collectors.groupingBy(
                        Map.Entry::getKey,
                        TreeMap::new,                                   // keeps courses alphabetical
                        Collectors.mapping(Map.Entry::getValue, Collectors.toList())));

        System.out.println("\n--- Students grouped by course ---");
        grouped.forEach((course, names) -> System.out.println("  " + course + " -> " + names));
    }

    // =====================================================================
    // TASK 4: Performance and statistics
    // Streams: mapToDouble(), summaryStatistics(), max(), min(), count(), limit(), sorted()
    // =====================================================================

    private static void viewPerformanceStatistics() {
        if (!hasStudents()) return;
        System.out.println("\n--- Performance Report ---");
        System.out.printf("%-6s %-18s %8s %8s %8s %9s %8s%n",
                "ID", "Name", "Average", "Highest", "Lowest", "Sum", "Courses");

        students.forEach(s -> {
            // One pass over the marks gives count, sum, min, max and average together
            DoubleSummaryStatistics stats = s.getCourses().values().stream()
                    .mapToDouble(Double::doubleValue)
                    .summaryStatistics();
            System.out.printf("%-6d %-18s %8.2f %8.2f %8.2f %9.2f %8d%n",
                    s.getId(), s.getStudentName(), stats.getAverage(),
                    stats.getMax(), stats.getMin(), stats.getSum(), stats.getCount());
        });

        long passed = students.stream().filter(s -> s.getAverage() >= PASS_MARK).count();
        System.out.println("\nStudents who passed (average >= " + PASS_MARK + "): " + passed
                + " out of " + students.size());
    }

    private static void viewBestStudent() {
        if (!hasStudents()) return;
        // max() needs a Comparator and returns an Optional
        students.stream()
                .max(Comparator.comparingDouble(Student::getAverage))
                .ifPresent(s -> System.out.printf("%nBest performing student: %s (ID %d) with average %.2f%n",
                        s.getStudentName(), s.getId(), s.getAverage()));
    }

    private static void viewTopThree() {
        if (!hasStudents()) return;
        System.out.println("\n--- Top 3 Students ---");
        List<Student> top = sortedByAverageDesc().stream().limit(3).collect(Collectors.toList());
        for (int i = 0; i < top.size(); i++) {
            Student s = top.get(i);
            System.out.printf("%d. %s (ID %d) - %.2f%n", i + 1, s.getStudentName(), s.getId(), s.getAverage());
        }
    }

    // =====================================================================
    // TASK 5: Bonus marks and final results
    // Streams: filter(), map(), forEach(), sorted(), reduce()
    // =====================================================================

    private static void applyBonusMarks() {
        if (!hasStudents()) return;

        // Eligible = average strictly above 50 AND bonus not already applied
        List<Student> eligible = students.stream()
                .filter(s -> !originalAverages.containsKey(s.getId()))
                .filter(s -> s.getAverage() > BONUS_THRESHOLD)
                .collect(Collectors.toList());

        if (eligible.isEmpty()) {
            System.out.println("No new students are eligible for bonus marks "
                    + "(either none average above " + BONUS_THRESHOLD + " or bonuses were already applied).");
        } else {
            eligible.forEach(s -> {
                originalAverages.put(s.getId(), s.getAverage());   // remember BEFORE changing marks
                // Add 10 to every course, capped at 100
                s.getCourses().replaceAll((course, mark) -> Math.min(MAX_MARK, mark + BONUS_MARKS));
            });
            System.out.println("Bonus applied to " + eligible.size() + " student(s).");
        }

        System.out.println("\n--- Original vs Final Averages ---");
        System.out.printf("%-6s %-18s %10s %10s %8s%n", "ID", "Name", "Original", "Final", "Bonus?");
        students.stream()
                .map(s -> {
                    boolean got = originalAverages.containsKey(s.getId());
                    double original = got ? originalAverages.get(s.getId()) : s.getAverage();
                    return String.format("%-6d %-18s %10.2f %10.2f %8s",
                            s.getId(), s.getStudentName(), original, s.getAverage(), got ? "Yes" : "No");
                })
                .forEach(System.out::println);

        // reduce(): fold all averages into one number (the sum), then divide for the class average
        double total = students.stream().map(Student::getAverage).reduce(0.0, Double::sum);
        System.out.printf("%nClass average after bonus: %.2f%n", total / students.size());
    }

    private static void viewFinalRanking() {
        if (!hasStudents()) return;
        System.out.println("\n--- Final Student Ranking ---");
        if (originalAverages.isEmpty()) {
            System.out.println("(Note: no bonus has been applied yet - use option 17 first for final results.)");
        }
        List<Student> ranked = sortedByAverageDesc();
        for (int i = 0; i < ranked.size(); i++) {
            Student s = ranked.get(i);
            System.out.printf("%d. %-18s (ID %d)  Final average: %.2f%n",
                    i + 1, s.getStudentName(), s.getId(), s.getAverage());
        }
    }

    // =====================================================================
    // REUSABLE HELPERS
    // =====================================================================

    /** Shared by options 8, 16 and 18. Reversed comparator = highest first. */
    private static List<Student> sortedByAverageDesc() {
        return students.stream()
                .sorted(Comparator.comparingDouble(Student::getAverage).reversed())
                .collect(Collectors.toList());
    }

    /** Prints a titled list, or a friendly message if the list is empty. */
    private static void printList(String title, List<Student> list) {
        System.out.println("\n--- " + title + " ---");
        if (list.isEmpty()) {
            System.out.println("No students found.");
        } else {
            list.forEach(System.out::println);
        }
    }

    /** Returns false (and tells the user) if nobody has registered yet. */
    private static boolean hasStudents() {
        if (students.isEmpty()) {
            System.out.println("\nNo students registered yet. Choose option 1 to register a student.");
            return false;
        }
        return true;
    }

    // ---------- Input validation helpers (loop until the input is valid) ----------

    private static int readInt(String prompt) {
        while (true) {
            if (!prompt.isEmpty()) System.out.print(prompt);
            String line = scanner.nextLine().trim();
            try {
                return Integer.parseInt(line);
            } catch (NumberFormatException e) {
                System.out.print("Invalid input. Please enter a whole number: ");
                prompt = "";
            }
        }
    }

    private static int readIntInRange(String prompt, int min, int max) {
        while (true) {
            int value = readInt(prompt);
            if (value >= min && value <= max) return value;
            System.out.println("Value must be between " + min + " and " + max + ".");
        }
    }

    private static double readDoubleInRange(String prompt, double min, double max) {
        while (true) {
            System.out.print(prompt);
            try {
                double value = Double.parseDouble(scanner.nextLine().trim());
                if (value >= min && value <= max) return value;
                System.out.println("Value must be between " + min + " and " + max + ".");
            } catch (NumberFormatException e) {
                System.out.println("Invalid number. Try again.");
            }
        }
    }

    private static String readNonEmpty(String prompt) {
        while (true) {
            System.out.print(prompt);
            String line = scanner.nextLine().trim();
            if (!line.isEmpty()) return line;
            System.out.println("Input cannot be empty.");
        }
    }
}

package playground;

public class PatternMatching {

    // instanceof 패턴 매칭
    public static String formatObject(Object obj) {
        if (obj instanceof String s) {
            return "String: " + s.toUpperCase();
        } else if (obj instanceof Integer i) {
            return "Number: " + (i * 2);
        } else if (obj instanceof Double d) {
            return "Double: " + String.format("%.2f", d);
        }
        return "Unknown type: " + obj.getClass().getSimpleName();
    }

    // Switch 패턴 매칭 (Java 21)
    public static String processValue(Object obj) {
        return switch (obj) {
            case String s -> "String of length " + s.length() + ": '" + s + "'";
            case Integer i when i > 0 -> "Positive number: " + i;
            case Integer i when i < 0 -> "Negative number: " + i;
            case Integer i -> "Zero";
            case Double d -> "Decimal: " + d;
            case null -> "Null value";
            default -> "Unknown type";
        };
    }

    // Record를 사용한 패턴 매칭
    record Point(int x, int y) {}

    public static String describePoint(Object obj) {
        return switch (obj) {
            case Point(int x, int y) when x == 0 && y == 0 -> "Origin point";
            case Point(int x, int y) when x == y -> "Diagonal point: (" + x + ", " + y + ")";
            case Point(int x, int y) -> "Point at (" + x + ", " + y + ")";
            default -> "Not a point";
        };
    }

    public static void main(String[] args) {
        System.out.println("=== instanceof Pattern Matching ===");
        System.out.println(formatObject("hello"));           // String: HELLO
        System.out.println(formatObject(42));                // Number: 84
        System.out.println(formatObject(3.14159));           // Double: 3.14
        System.out.println(formatObject(new Object()));      // Unknown type: Object

        System.out.println("\n=== Switch Pattern Matching ===");
        System.out.println(processValue("Java"));            // String of length 4: 'Java'
        System.out.println(processValue(100));               // Positive number: 100
        System.out.println(processValue(-50));               // Negative number: -50
        System.out.println(processValue(0));                 // Zero
        System.out.println(processValue(2.718));             // Decimal: 2.718
        System.out.println(processValue(null));              // Null value

        System.out.println("\n=== Record Pattern Matching ===");
        System.out.println(describePoint(new Point(0, 0)));  // Origin point
        System.out.println(describePoint(new Point(5, 5)));  // Diagonal point: (5, 5)
        System.out.println(describePoint(new Point(3, 7)));  // Point at (3, 7)
        System.out.println(describePoint("not a point"));    // Not a point
    }
}
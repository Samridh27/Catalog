import java.math.BigInteger;
import java.util.*;
import java.util.regex.*;

public class catalog {
    
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        StringBuilder jsonInput = new StringBuilder();
        
        System.out.println("Enter JSON input (type 'END' on new line to finish):");
        String line;
        while (!(line = scanner.nextLine()).equals("END")) {
            jsonInput.append(line).append("\n");
        }
        
        parseJsonAndSolve(jsonInput.toString());
        scanner.close();
    }
    
    public static void parseJsonAndSolve(String jsonString) {
        try {
            Pattern nPattern = Pattern.compile("\"n\":\\s*(\\d+)");
            Pattern kPattern = Pattern.compile("\"k\":\\s*(\\d+)");
            
            Matcher nMatcher = nPattern.matcher(jsonString);
            Matcher kMatcher = kPattern.matcher(jsonString);
            
            int n = 0, k = 0;
            if (nMatcher.find()) {
                n = Integer.parseInt(nMatcher.group(1));
            }
            if (kMatcher.find()) {
                k = Integer.parseInt(kMatcher.group(1));
            }
            
            List<Point> points = new ArrayList<>();
            
            Pattern rootPattern = Pattern.compile("\"(\\d+)\":\\s*\\{[^}]*\"base\":\\s*\"(\\d+)\"[^}]*\"value\":\\s*\"([^\"]+)\"[^}]*\\}");
            Matcher rootMatcher = rootPattern.matcher(jsonString);
            
            while (rootMatcher.find()) {
                int x = Integer.parseInt(rootMatcher.group(1));
                int base = Integer.parseInt(rootMatcher.group(2));
                String value = rootMatcher.group(3);
                
                BigInteger y = decodeValue(value, base);
                points.add(new Point(BigInteger.valueOf(x), y));
                
                System.out.println("Point " + x + ": base=" + base + ", value=" + value + " -> decoded=" + y);
            }
            
            points.sort((a, b) -> a.x.compareTo(b.x));
            
            List<Point> selectedPoints = points.subList(0, Math.min(k, points.size()));
            
            System.out.println("\nUsing " + selectedPoints.size() + " points for Lagrange interpolation:");
            for (Point p : selectedPoints) {
                System.out.println("(" + p.x + ", " + p.y + ")");
            }
            
            BigInteger secret = lagrangeInterpolation(selectedPoints, BigInteger.ZERO);
            
            System.out.println("\nSecret (constant term): " + secret);
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public static BigInteger decodeValue(String value, int base) {
        BigInteger result = BigInteger.ZERO;
        BigInteger baseBI = BigInteger.valueOf(base);
        
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            int digit;
            
            if (c >= '0' && c <= '9') {
                digit = c - '0';
            } else if (c >= 'a' && c <= 'z') {
                digit = c - 'a' + 10;
            } else if (c >= 'A' && c <= 'Z') {
                digit = c - 'A' + 10;
            } else {
                throw new IllegalArgumentException("Invalid character in value: " + c);
            }
            
            result = result.multiply(baseBI).add(BigInteger.valueOf(digit));
        }
        
        return result;
    }
    
    public static BigInteger lagrangeInterpolation(List<Point> points, BigInteger x) {
        BigInteger result = BigInteger.ZERO;
        int n = points.size();
        
        for (int i = 0; i < n; i++) {
            BigInteger term = points.get(i).y;
            BigInteger numerator = BigInteger.ONE;
            BigInteger denominator = BigInteger.ONE;
            
            for (int j = 0; j < n; j++) {
                if (i != j) {
                    numerator = numerator.multiply(x.subtract(points.get(j).x));
                    denominator = denominator.multiply(points.get(i).x.subtract(points.get(j).x));
                }
            }
            
            if (denominator.equals(BigInteger.ZERO)) {
                throw new ArithmeticException("Division by zero in Lagrange interpolation");
            }
            
            term = term.multiply(numerator);
            if (denominator.compareTo(BigInteger.ZERO) < 0) {
                term = term.negate();
                denominator = denominator.negate();
            }
            
            result = result.add(term.divide(denominator));
        }
        
        return result;
    }
    
    static class Point {
        BigInteger x;
        BigInteger y;
        
        Point(BigInteger x, BigInteger y) {
            this.x = x;
            this.y = y;
        }
        
        @Override
        public String toString() {
            return "(" + x + ", " + y + ")";
        }
    }
}

class ShamirSecretSharingSimple {
    public static void main(String[] args) {
        testCase1();
        System.out.println();
        testCase2();
    }
    
    public static void testCase1() {
        System.out.println("Test Case 1:");
        
        Map<Integer, String[]> testData1 = new HashMap<>();
        testData1.put(1, new String[]{"10", "4"});
        testData1.put(2, new String[]{"2", "111"});
        testData1.put(3, new String[]{"10", "12"});
        testData1.put(6, new String[]{"4", "213"});
        
        List<Point> points = new ArrayList<>();
        
        for (Map.Entry<Integer, String[]> entry : testData1.entrySet()) {
            int x = entry.getKey();
            int base = Integer.parseInt(entry.getValue()[0]);
            String value = entry.getValue()[1];
            
            BigInteger y = decodeValue(value, base);
            points.add(new Point(BigInteger.valueOf(x), y));
        }
        
        points.sort((a, b) -> a.x.compareTo(b.x));
        
        List<Point> selectedPoints = points.subList(0, 3);
        
        BigInteger secret = lagrangeInterpolation(selectedPoints, BigInteger.ZERO);
        
        System.out.println("Decoded points:");
        for (Point p : selectedPoints) {
            System.out.println(p);
        }
        System.out.println("Secret: " + secret);
    }
    
    public static void testCase2() {
        System.out.println("Test Case 2:");
        
        Map<Integer, String[]> testData2 = new HashMap<>();
        testData2.put(1, new String[]{"6", "13444211440455345511"});
        testData2.put(2, new String[]{"15", "aed7015a346d63"});
        testData2.put(3, new String[]{"15", "6aeeb69631c227c"});
        testData2.put(4, new String[]{"16", "e1b5e05623d881f"});
        testData2.put(5, new String[]{"8", "316034514573652620673"});
        testData2.put(6, new String[]{"3", "2122212201122002221120200210011020220200"});
        testData2.put(7, new String[]{"3", "20120221122211000100210021102001201112121"});
        
        List<Point> points = new ArrayList<>();
        
        for (Map.Entry<Integer, String[]> entry : testData2.entrySet()) {
            int x = entry.getKey();
            int base = Integer.parseInt(entry.getValue()[0]);
            String value = entry.getValue()[1];
            
            BigInteger y = decodeValue(value, base);
            points.add(new Point(BigInteger.valueOf(x), y));
        }
        
        points.sort((a, b) -> a.x.compareTo(b.x));
        
        List<Point> selectedPoints = points.subList(0, 7);
        
        BigInteger secret = lagrangeInterpolation(selectedPoints, BigInteger.ZERO);
        
        System.out.println("Secret: " + secret);
    }
    
    public static BigInteger decodeValue(String value, int base) {
        BigInteger result = BigInteger.ZERO;
        BigInteger baseBI = BigInteger.valueOf(base);
        
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            int digit;
            
            if (c >= '0' && c <= '9') {
                digit = c - '0';
            } else if (c >= 'a' && c <= 'z') {
                digit = c - 'a' + 10;
            } else if (c >= 'A' && c <= 'Z') {
                digit = c - 'A' + 10;
            } else {
                throw new IllegalArgumentException("Invalid character: " + c);
            }
            
            result = result.multiply(baseBI).add(BigInteger.valueOf(digit));
        }
        
        return result;
    }
    
    public static BigInteger lagrangeInterpolation(List<Point> points, BigInteger x) {
        BigInteger result = BigInteger.ZERO;
        int n = points.size();
        
        for (int i = 0; i < n; i++) {
            BigInteger term = points.get(i).y;
            BigInteger numerator = BigInteger.ONE;
            BigInteger denominator = BigInteger.ONE;
            
            for (int j = 0; j < n; j++) {
                if (i != j) {
                    numerator = numerator.multiply(x.subtract(points.get(j).x));
                    denominator = denominator.multiply(points.get(i).x.subtract(points.get(j).x));
                }
            }
            
            term = term.multiply(numerator).divide(denominator);
            result = result.add(term);
        }
        
        return result;
    }
    
    static class Point {
        BigInteger x;
        BigInteger y;
        
        Point(BigInteger x, BigInteger y) {
            this.x = x;
            this.y = y;
        }
        
        @Override
        public String toString() {
            return "(" + x + ", " + y + ")";
        }
    }
}
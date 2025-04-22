import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
public class Exp5 {
    public static Integer parseStringToInteger(String str) {
        return Integer.parseInt(str); 
    }
    public static int calculateSum(List<Integer> list) {
        int sum = 0;
        for (Integer num : list) {
            sum += num; 
        }
        return sum;
    }
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        List<Integer> numbers = new ArrayList<>();
        System.out.println("Enter integers separated by spaces: ");
        String input = scanner.nextLine();
        String[] strNumbers = input.split("\\s+");
        for (String str : strNumbers) {
            Integer num = parseStringToInteger(str);
            numbers.add(num);
        }
        int sum = calculateSum(numbers);
        System.out.println("The sum of the integers is: " + sum);
        scanner.close();
    }
}

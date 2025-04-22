
import java.util.Scanner;

public class Exp1 {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        System.out.println("Enter rows and columns for array 1: ");
        int rows1 = sc.nextInt();
        int cols1 = sc.nextInt();
        int[][] arr1 = new int[rows1][cols1];
        System.out.println("Enter values for array 1: ");
        for (int i = 0; i < rows1; i++) {
            for (int j = 0; j < cols1; j++) {
                arr1[i][j] = sc.nextInt();
            }
        }
        System.out.println("Enter rows and columns for array 2: ");
        int rows2 = sc.nextInt();
        int cols2 = sc.nextInt();
        int[][] arr2 = new int[rows2][cols2];
        System.out.println("Enter values for array 2: ");
        for (int i = 0; i < rows2; i++) {
            for (int j = 0; j < cols2; j++) {
                arr2[i][j] = sc.nextInt();
            }
        }
        System.out.println("Addition of two arrays is: ");
        if (rows1 == rows2 && cols1 == cols2) {
            int[][] arr3 = new int[rows1][cols1];
            for (int i = 0; i < rows1; i++) {
                for (int j = 0; j < cols1; j++) {
                    arr3[i][j] = arr1[i][j] + arr2[i][j];
                    System.out.print(arr3[i][j] + " ");
                }
                System.out.println();
            }
        }
        System.out.println("Subtraction of two arrays is: ");
        if (rows1 == rows2 && cols1 == cols2) {
            int[][] arr4 = new int[rows1][cols1];
            for (int i = 0; i < rows1; i++) {
                for (int j = 0; j < cols1; j++) {
                    arr4[i][j] = arr1[i][j] - arr2[i][j];
                    System.out.print(arr4[i][j] + " ");
                }
                System.out.println();
            }
        }
        sc.close();
    }
}
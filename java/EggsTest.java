import ru.algorithmist.jquant.math.GrangerTest;
import ru.algorithmist.jquant.math.GrangerTestResult;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * @author "Sergey Edunov"
 * @version 1/13/11
 */
public class EggsTest {

    public static void main(String[] args) throws FileNotFoundException {
        Scanner sc = new Scanner(new File("C:\\prj\\git\\JQuant\\modules\\eggs.txt"));
        sc.nextLine();
        List<Double> eggs = new ArrayList<Double>();
        List<Double> chics = new ArrayList<Double>();
        while (sc.hasNextLine()) {
            int year = sc.nextInt();
            if (year < 1980) {
                sc.nextInt();
                sc.nextInt();
                continue;
            }
            double chic = sc.nextInt();
            double egg = sc.nextInt();
            eggs.add(egg);
            chics.add(chic);
        }
        double[] deggs = GrangerCausalitySample.tod(eggs);
        double[] dchics = GrangerCausalitySample.tod(chics);

        printTable(deggs, dchics);
        printTable(dchics, deggs);

    }

    private static void printTable(double[] x, double[] y){
        System.out.println("<table border=\"1\" cellpadding=\"0\" cellspacing=\"0\" style=\"border-collapse: collapse;\">");
        System.out.println("<tr><td> Лаг </td><td>F-statistics</td><td>P-value</td><td>R<sup>2</sup></td></tr>");
        for(int l = 1; l<=4; l++){
            GrangerTestResult result = GrangerTest.granger(x, y, l);
            System.out.println("<tr><td> " + l +
                    " </td><td> " +  result.getFStat() +
                    " </td><td> " + result.getPValue() +
                    " </td><td> " + result.getR2() +  " </td></tr>");
        }
        System.out.println("</table>");
    }
}

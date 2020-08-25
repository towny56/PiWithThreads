package pi;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.Vector;

public class mainPI {
    static BigDecimal result = BigDecimal.ZERO;
    static BigDecimal[] factorial;
    static BigDecimal[] gradation;

    public static BigDecimal calculateFactoriel (int n) {
        if (n < 1) {
            return BigDecimal.valueOf(1);
        }

        if (factorial[n] != null) {
            return factorial[n];
        }

        factorial[n] = BigDecimal.valueOf(n).multiply(calculateFactoriel(n - 1));

        return factorial[n];
    }

    public static BigDecimal calculateGradation (int n)
    {
        if (n == 0)
        {
            return BigDecimal.valueOf(1);
        }

        if (gradation[n] != null)
        {
            return gradation[n];
        }

        gradation[n] = BigDecimal.valueOf(882*882).multiply(calculateGradation(n-1));

        return gradation[n];
    }

    public static void saveToFile (String fileName, BigDecimal pi)
    {
        try
        {
            File file = new File(fileName);

            if (!file.exists())
            {
                file.createNewFile();
            }

            PrintWriter printWriter = new PrintWriter(file);
            printWriter.println(pi.toString());
            printWriter.close();
        }
        catch (IOException exception)
        {
            exception.printStackTrace();
        }
    }

    public static Runnable runPI (final int start, final int end, final int numberThreads, final boolean x)
    {
        Runnable runner = new Runnable() {
            @Override
            public void run()
            {
                long startThread = System.currentTimeMillis();

                if (!x)
                {
                    System.out.println(Thread.currentThread().getName() + " started.");
                }

                BigDecimal pi = BigDecimal.ZERO;

                for (int counter = start; counter < end; counter = counter + numberThreads)
                {
                    BigDecimal top = BigDecimal.valueOf(counter % 2 == 0 ? 1 : -1).multiply(calculateFactoriel(4*counter)).multiply(BigDecimal.valueOf(1123 + 21460*counter));
                    BigDecimal bottom = (BigDecimal.valueOf(4).pow(counter).multiply(calculateFactoriel(counter))).pow(4).multiply(calculateGradation(counter));
                    BigDecimal quotient = top.divide(bottom, new MathContext(10000, RoundingMode.HALF_UP));

                    pi = pi.add(quotient);
                }

                assignResult(pi);

                if(!x)
                {
                    long endThread = System.currentTimeMillis();

                    System.out.println(Thread.currentThread().getName() + " stopped.");

                    System.out.println(Thread.currentThread().getName() + " execution time was " + (endThread - startThread) + " millis.");
                }
            }
        };

        return runner;
    }

    public static synchronized  void assignResult(BigDecimal pi)
    {
        result = result.add(pi);
    }

    public static void main(String[] args) throws InterruptedException
    {
        Vector<String> parameters = new Vector<>(Arrays.asList(args));

        int numberOfMembers = 100;
        int numberThreads = 50;
        boolean quietMode = false;
        String fileName = "pi.txt";

        if (parameters.contains("-p"))
        {
            numberOfMembers = Integer.parseInt(parameters.get(parameters.indexOf("-p") + 1));
        }

        if (parameters.contains("-t"))
        {
            numberThreads = Integer.parseInt(parameters.get(parameters.indexOf("-t") + 1));
        }
        else if (parameters.contains("-task"))
        {
            numberThreads = Integer.parseInt(parameters.get(parameters.indexOf("-task") + 1));
        }

        if (parameters.contains("-q"))
        {
            quietMode = true;
        }

        if (parameters.contains("-o"))
        {
            fileName = parameters.get(parameters.indexOf("-o") + 1);
        }

        factorial = new BigDecimal[4*(numberOfMembers + 1)];
        gradation = new BigDecimal[numberOfMembers + 1];

        Thread[] threads = new Thread[numberThreads];

        long startOne = System.currentTimeMillis();
        final int counterMembers = numberOfMembers;

        new Thread(new Runnable() {
            @Override
            public void run()
            {
                BigDecimal current = BigDecimal.valueOf(1);

                for (int counter = 2; counter < 4 * counterMembers; counter++)
                {
                    current = current.multiply(BigDecimal.valueOf(counter));
                    factorial[counter] = current;
                }
            }
        }).start();

        for (int counter = 0; counter < numberThreads; counter++)
        {
            threads[counter] = new Thread(runPI(counter, numberOfMembers, numberThreads, quietMode));
        }

        for (int counter = 0; counter < threads.length; counter++)
        {
            threads[counter].start();
        }

        for(int counter = 0; counter < threads.length; counter++)
        {
            threads[counter].join();
        }

        result = BigDecimal.valueOf(882*4).divide(result, new MathContext(10000, RoundingMode.HALF_UP));

        System.out.println("pi = " + result);

        long stopOne = System.currentTimeMillis();
        long difference = stopOne - startOne;

        System.out.println("Total work time: " + difference);

        saveToFile(fileName, result);
    }
}

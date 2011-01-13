package ru.algorithmist.jquant;

import org.apache.commons.lang.time.FastDateFormat;
import org.joda.time.format.DateTimeFormatterBuilder;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.DateTime;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.text.ParseException;
import java.util.concurrent.*;
import java.util.Date;

/**
 * Author: Sergey Edunov
 */
public class DateFormatTest {

    public static final int CNT = 10000;
    public static final int THREAD_COUNT = 20;
    public static final String FORMAT = "yyyy.MM.dd HH:mm:ss";
    public static final String TEST_STRING = "2001.07.04 12:08:56";
    public static final Date TEST_DATE = new Date();

    public static void main(String[] args) throws InterruptedException {
        IDateFormat format;
        long t0;

        format = new SynchronizedDateFormat(new SimpleDateFormat(FORMAT));
        t0 = System.currentTimeMillis();
        runParseTest(format);
        System.out.println("Synchronized DateFormat parsing " + (System.currentTimeMillis()-t0));

        format = new NewInstanceDateFormat();
        t0 = System.currentTimeMillis();
        runParseTest(format);
        System.out.println("New instance per call DateFormat parsing " + (System.currentTimeMillis()-t0));

        format = new ThreadLocalDateFormat();
        t0 = System.currentTimeMillis();
        runParseTest(format);
        System.out.println("ThreadLocal DateFormat parsing " + (System.currentTimeMillis()-t0));

        format = new JodaDateFormat();
        t0 = System.currentTimeMillis();
        runParseTest(format);
        System.out.println("JodaTime DateFormat parsing " + (System.currentTimeMillis()-t0));


        format = new SynchronizedDateFormat(new SimpleDateFormat(FORMAT));
        t0 = System.currentTimeMillis();
        runFormatTest(format);
        System.out.println("Synchronized DateFormat format " + (System.currentTimeMillis()-t0));

        format = new NewInstanceDateFormat();
        t0 = System.currentTimeMillis();
        runFormatTest(format);
        System.out.println("New instance per call DateFormat format " + (System.currentTimeMillis()-t0));

        format = new ThreadLocalDateFormat();
        t0 = System.currentTimeMillis();
        runFormatTest(format);
        System.out.println("ThreadLocal DateFormat format " + (System.currentTimeMillis()-t0));

        format = new ApacheDateFormat();
        t0 = System.currentTimeMillis();
        runFormatTest(format);
        System.out.println("Apache FastDateFormat format " + (System.currentTimeMillis()-t0));

        format = new JodaDateFormat();
        t0 = System.currentTimeMillis();
        runFormatTest(format);
        System.out.println("JodaTime DateFormat format " + (System.currentTimeMillis()-t0));

    }

    private static void runFormatTest(final IDateFormat format) throws InterruptedException {
        ThreadPoolExecutor tpe = new ThreadPoolExecutor(THREAD_COUNT, THREAD_COUNT, 0, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>());
        for(int i=0; i<THREAD_COUNT; i++) {
            final int ti = i;
            tpe.execute(new Runnable() {
                @Override
                public void run() {
                    for(int j=0; j<CNT; j++) {
                        format.format(TEST_DATE);
                    }
                }
            });
        }
        tpe.shutdown();
        tpe.awaitTermination(1, TimeUnit.DAYS);
    }

    private static void runParseTest(final IDateFormat format) throws InterruptedException {
        ThreadPoolExecutor tpe = new ThreadPoolExecutor(THREAD_COUNT, THREAD_COUNT, 0, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>());
        for(int i=0; i<THREAD_COUNT; i++) {
            final int ti = i;
            tpe.execute(new Runnable() {
                @Override
                public void run() {
                    for(int j=0; j<CNT; j++) {
                        try {
                            format.parse(TEST_STRING);
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                    }
                }
            });
        }
        tpe.shutdown();
        tpe.awaitTermination(1, TimeUnit.DAYS);
    }

    static interface IDateFormat {
        public Date parse(String str) throws ParseException;

        public String format(Date date);
    }


    private static class SynchronizedDateFormat implements IDateFormat{
        private DateFormat format;

        private SynchronizedDateFormat(DateFormat format) {
            this.format = format;
        }

        public synchronized Date parse(String str) throws ParseException {
            return format.parse(str);
        }

        @Override
        public synchronized String format(Date date) {
            return format.format(date);
        }
    }

    private static class NewInstanceDateFormat implements IDateFormat{

        @Override
        public Date parse(String str) throws ParseException {
            return new SimpleDateFormat(FORMAT).parse(str);
        }

        @Override
        public String format(Date date) {
            return new SimpleDateFormat(FORMAT).format(date);
        }
    }

    private static class ThreadLocalDateFormat implements IDateFormat{
        private ThreadLocal<DateFormat> TL = new ThreadLocal<DateFormat>();

        @Override
        public Date parse(String str) throws ParseException {
            return getDF().parse(str);
        }

        private DateFormat getDF() {
            DateFormat df = TL.get();
            if (df == null){
                df = new SimpleDateFormat(FORMAT);
                TL.set(df);
            }
            return df;
        }

        @Override
        public String format(Date date) {
            return getDF().format(date);
        }
    }

    private static class ApacheDateFormat implements IDateFormat{
        private FastDateFormat format = FastDateFormat.getInstance(FORMAT);

        @Override
        public Date parse(String str) throws ParseException {
            throw new UnsupportedOperationException("Parsing is not supported");
        }

        @Override
        public String format(Date date) {
            return format.format(date);
        }
    }

    private static class JodaDateFormat implements IDateFormat{
        private DateTimeFormatter formater;

        public JodaDateFormat(){
            formater = DateTimeFormat.forPattern(FORMAT);
        }

        @Override
        public Date parse(String str) throws ParseException {
            return formater.parseDateTime(str).toDate();
        }

        @Override
        public String format(Date date) {
            return formater.print(new DateTime(date.getTime()));
        }
    }
}

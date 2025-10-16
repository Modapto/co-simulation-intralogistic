package org.adoxx.utils;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Array;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

public class Utils {
    
	public static String convertoMillisecondsToStringDateTime(long totalMilliseconds) throws Exception{
        //yy:ddd:hh:mm:ss
		String ret = "";
		ret += String.format("%02d", (TimeUnit.MILLISECONDS.toDays(totalMilliseconds)/365));
		ret += ":" + String.format("%03d", (TimeUnit.MILLISECONDS.toDays(totalMilliseconds)%365));
		ret += ":" + String.format("%02d", (TimeUnit.MILLISECONDS.toHours(totalMilliseconds)%24));
		ret += ":" + String.format("%02d", (TimeUnit.MILLISECONDS.toMinutes(totalMilliseconds)%60));
		ret += ":" + String.format("%02d", (TimeUnit.MILLISECONDS.toSeconds(totalMilliseconds)%60));
        return ret;
    }
	
	public static boolean isAdoxxDateTime(String dateTime){
	    if(dateTime!=null && dateTime.length()==15 && dateTime.split(":").length==5)
	        return true;
	    return false;
	}
	
	public static long convertAdoxxDateTimeToMilliseconds(String dateTime) throws Exception{
        //yy:ddd:hh:mm:ss
        if(!isAdoxxDateTime(dateTime))
            throw new Exception("Incorrect data format provided: "+dateTime+" \nThe right data format is yy:ddd:hh:mm:ss");
        
        String[] dateTimeSplittedArray = dateTime.split(":");
        long totalMilliseconds = 0;
        totalMilliseconds += TimeUnit.DAYS.toMillis(Long.parseLong(dateTimeSplittedArray[0])*365);
        totalMilliseconds += TimeUnit.DAYS.toMillis(Long.parseLong(dateTimeSplittedArray[1]));
        totalMilliseconds += TimeUnit.HOURS.toMillis(Long.parseLong(dateTimeSplittedArray[2]));
        totalMilliseconds += TimeUnit.MINUTES.toMillis(Long.parseLong(dateTimeSplittedArray[3]));
        totalMilliseconds += TimeUnit.SECONDS.toMillis(Long.parseLong(dateTimeSplittedArray[4]));
        
        return totalMilliseconds;
    }
    
    public static String getUTCTime(){
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        return sdf.format(new Date());
    }
    
    public static <T> T[] concatenate (T[] a, T[] b) {
        int aLen = a.length;
        int bLen = b.length;

        @SuppressWarnings("unchecked")
        T[] c = (T[]) Array.newInstance(a.getClass().getComponentType(), aLen+bLen);
        System.arraycopy(a, 0, c, 0, aLen);
        System.arraycopy(b, 0, c, aLen, bLen);

        return c;
    }
    
    public enum LogType{
        INFO, ERROR, DEBUG;
    }
    public static void log(Error e){
        StringWriter sw = new StringWriter();
        e.printStackTrace(new PrintWriter(sw));
        log(sw.toString(), LogType.ERROR);
    }
    public static void log(Exception e){
        StringWriter sw = new StringWriter();
        e.printStackTrace(new PrintWriter(sw));
        log(sw.toString(), LogType.ERROR);
    }
    public static void log(String message, LogType logType){
        try{
            String folderPath = Utils.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath();
            if(new File(folderPath).isDirectory())
                folderPath = folderPath.substring(0, folderPath.length()-1);
            String logFile = folderPath.substring(0, folderPath.lastIndexOf("/")+1) + "simulation.log";
            System.err.println("INFO: updated log file " + logFile);
            String callerClassName = new Exception().getStackTrace()[1].getClassName();
            IOUtils.writeFile(("\n"+logType.toString()+" "+getUTCTime()+" "+callerClassName+" -> "+message).getBytes(), logFile, true);
        }catch(Exception ex){ex.printStackTrace();}
    }
    
    public static int[][] generateBinaryMatrix(int num){
        int rows = (int) Math.pow(2, num);
        int[][] ret = new int[rows][num];
        for(int iRow=0;iRow<rows;iRow++)
            for(int iColumn=0;iColumn<num;iColumn++)
                ret[iRow][num-1-iColumn] = (iRow/((int) Math.pow(2, iColumn))) % 2;
        return ret;
    }

    public static Map<String, String> parseQueryString(String query) {
        Map<String, String> params = new HashMap<>();
        if (query == null || query.isEmpty()) return params;

        for (String pair : query.split("&")) {
            int idx = pair.indexOf("=");
            try {
                String key = idx > 0
                        ? java.net.URLDecoder.decode(pair.substring(0, idx), StandardCharsets.UTF_8)
                        : java.net.URLDecoder.decode(pair, StandardCharsets.UTF_8);
                String value = idx > 0 && pair.length() > idx + 1
                        ? java.net.URLDecoder.decode(pair.substring(idx + 1), StandardCharsets.UTF_8)
                        : "";
                params.put(key, value);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return params;
    }

    public static String checkEmpty(String input, String message) throws Exception {
        if(input == null || input == "") throw new Exception(message);
        return input;
    }
    public static String checkDefault(String input, String defaultVal) throws Exception {
        if(input == null || input == "") return defaultVal;
        return input;
    }

	/*
     // Gaussian elimination with partial pivoting
    public static double[] lsolve(double[][] A, double[] b) {
        final double EPSILON = 1e-10;
        int N  = b.length;

        for (int p = 0; p < N; p++) {

            // find pivot row and swap
            int max = p;
            for (int i = p + 1; i < N; i++) {
                if (Math.abs(A[i][p]) > Math.abs(A[max][p])) {
                    max = i;
                }
            }
            double[] temp = A[p]; A[p] = A[max]; A[max] = temp;
            double   t    = b[p]; b[p] = b[max]; b[max] = t;

            // singular or nearly singular
            if (Math.abs(A[p][p]) <= EPSILON) {
                throw new RuntimeException("Matrix is singular or nearly singular");
            }

            // pivot within A and b
            for (int i = p + 1; i < N; i++) {
                double alpha = A[i][p] / A[p][p];
                b[i] -= alpha * b[p];
                for (int j = p; j < N; j++) {
                    A[i][j] -= alpha * A[p][j];
                }
            }
        }

        // back substitution
        double[] x = new double[N];
        for (int i = N - 1; i >= 0; i--) {
            double sum = 0.0;
            for (int j = i + 1; j < N; j++) {
                sum += A[i][j] * x[j];
            }
            x[i] = (b[i] - sum) / A[i][i];
        }
        return x;
    }
    */
}

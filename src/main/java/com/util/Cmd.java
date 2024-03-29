package com.iscas.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class Cmd {

    public static boolean exec(String cmd) {
        boolean success = true;
        Runtime rt = Runtime.getRuntime();
        try {
            Process p = rt.exec(cmd);
            p.waitFor();
//            String std = readStream(p.getInputStream());
            String error = readStream(p.getErrorStream());
            if (error.length() > 0)
                System.out.println(error);
        } catch (InterruptedException | IOException e) {
            e.printStackTrace();
            success = false;
        }
        return success;
    }

    public static boolean execWithPipe(String shell) {
        boolean success = true;
        String[] cmd = new String[]{"sh", "-c", shell};
        Runtime rt = Runtime.getRuntime();
        try {
            Process p = rt.exec(cmd);
            p.waitFor();
//            String std = readStream(p.getInputStream());
            String error = readStream(p.getErrorStream());
            if (error.length() > 0) {
                System.out.println(error);
            }

        } catch (InterruptedException | IOException e) {
            e.printStackTrace();
            success = false;
        }
        return success;
    }

    public static String execForStd(String cmd) {
        Runtime rt = Runtime.getRuntime();
        String std = null;
        try {
            Process p = rt.exec(cmd);
            p.waitFor();
            std = readStream(p.getInputStream());
            String error = readStream(p.getErrorStream());
            if (error.length() > 0)
                System.out.println(error);
        } catch (InterruptedException | IOException e) {
            e.printStackTrace();
        }
        return std;
    }

    public static String execForStd2(String cmd) {
        Runtime rt = Runtime.getRuntime();
        String std = null;
        try {
            Process p = rt.exec(cmd);
            p.waitFor();
            std = readStream2(p.getInputStream());
            String error = readStream2(p.getErrorStream());
            if (error.length() > 0)
                System.out.println(error);
        } catch (InterruptedException | IOException e) {
            e.printStackTrace();
        }
        return std;
    }

    public static String execForErr(String cmd) {
        Runtime rt = Runtime.getRuntime();
        String std = null;
        try {
            Process p = rt.exec(cmd);
            p.waitFor();
            std = readStream(p.getErrorStream());
            String error = readStream(p.getInputStream());
            if (error.length() > 0)
                System.out.println(error);
        } catch (InterruptedException | IOException e) {
            e.printStackTrace();
        }
        return std;
    }

    public static String execForStdWithPipe(String cmd) {
        Runtime rt = Runtime.getRuntime();
        String std = null;
        try {
            Process p = rt.exec(new String[]{"sh", "-c", cmd});
            p.waitFor();
            std = readStream(p.getInputStream());
            String error = readStream(p.getErrorStream());
            if (error.length() > 0) {
                System.out.println(error);
            }
        } catch (InterruptedException | IOException e) {
            e.printStackTrace();
        }
        return std;
    }

    private static String readStream(InputStream in) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(in));
        StringBuffer sb = new StringBuffer();
        String str;
        while ((str = reader.readLine()) != null)
            sb.append(str);
        return sb.toString();
    }

    private static String readStream2(InputStream in) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(in));
        StringBuffer sb = new StringBuffer();
        String str;
        while ((str = reader.readLine()) != null)
            sb.append(str).append("\n");
        return sb.toString();
    }
}

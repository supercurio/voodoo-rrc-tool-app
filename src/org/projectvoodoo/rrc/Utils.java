
package org.projectvoodoo.rrc;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.os.Build;
import android.util.Log;

public class Utils {

    private static final String TAG = "Voodoo RRC Tool Utils";

    public static boolean canGetRootPermission() {
        String check = "SuCheck";
        String command = "echo " + check;
        try {
            ArrayList<String> output = run("su", command);
            if (output.get(0).equals(check))
                return true;
        } catch (Exception e) {
        }

        return false;
    }

    public static ArrayList<String> run(String shell, String command) throws IOException {
        ArrayList<String> output = new ArrayList<String>();

        Log.v(TAG, "Run commands with shell: " + shell + "\n" + command);

        Process process = Runtime.getRuntime().exec(shell);
        BufferedOutputStream shellInput =
                new BufferedOutputStream(process.getOutputStream());
        BufferedReader shellOutput =
                new BufferedReader(new InputStreamReader(process.getInputStream()));

        shellInput.write((command + "\n").getBytes());
        shellInput.write("exit\n".getBytes());
        shellInput.flush();

        String line;
        while ((line = shellOutput.readLine()) != null)
            output.add(line);

        try {
            process.waitFor();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        if (process.exitValue() != 0)
            throw new IOException();

        return output;
    }

    public static boolean requireRebootDontKillRild() {
        for (String deviceNamePattern : App.REQUIRE_REBOOT_DONT_KILL_RILD)
            if (Build.DEVICE.matches(deviceNamePattern))
                return true;

        return false;
    }

    private static int getProcessPid(String cmdline) {
        int pid = -1;
        Pattern p = Pattern.compile("[0-9]*");
        char[] buf = cmdline.toCharArray();

        for (File procFile : new File("/proc/").listFiles())
            if (procFile.isDirectory()) {
                Matcher m = p.matcher(procFile.getName());
                if (m.find())
                    try {
                        File cmdFile = new File(procFile.getAbsolutePath() + "/cmdline");
                        if (!cmdFile.isFile())
                            continue;

                        FileReader reader = new FileReader(cmdFile);
                        int len = reader.read(buf);
                        if (len == buf.length && Arrays.equals(buf, cmdline.toCharArray())) {

                            try {
                                pid = Integer.parseInt(procFile.getName());
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    } catch (Exception e) {
                    }
            }

        return pid;
    }

    public static void killRild() throws IOException {
        int pid = getProcessPid("/system/bin/rild");

        if (pid > 0)
            Utils.run("su", "kill " + pid);
    }

}

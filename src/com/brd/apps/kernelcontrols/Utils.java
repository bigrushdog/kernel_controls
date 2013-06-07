
package com.brd.apps.kernelcontrols;

import android.content.Context;
import android.content.res.AssetManager;
import android.os.Environment;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import com.stericson.RootTools.RootTools;
import com.stericson.RootTools.execution.CommandCapture;

public final class Utils {

    /*
     * kernel constants and their pref flags We only use detected paths and
     * features
     */
    public static final String CPU_AVAIL_FREQ = "/sys/devices/system/cpu/cpu0/cpufreq/scaling_available_frequencies";
    public static final String CPU_AVAIL_GOV = "/sys/devices/system/cpu/cpu0/cpufreq/scaling_available_governors";
    public static final String CPU_MIN_SCALE = "/sys/devices/system/cpu/cpu0/cpufreq/scaling_min_freq";
    public static final String CPU_MAX_SCALE = "/sys/devices/system/cpu/cpu0/cpufreq/scaling_max_freq";
    public static final String CPU_MAX_SCREEN_OFF = "/sys/devices/system/cpu/cpu0/cpufreq/screen_off_max_freq";
    public static final String CPU_GOV = "/sys/devices/system/cpu/cpu0/cpufreq/scaling_governor";
    public static final String IO_SCHED = "/sys/block/mmcblk0/queue/scheduler";
    public static final String ZRAM = "/sys/block/zram0/disksize";
    public static final String S2W_PATH = "/sys/android_touch/sweep2wake";
    public static final String FFC_PATH = "/sys/kernel/fast_charge/force_fast_charge";
    public static final String TCP_CONG_LIST = "/proc/sys/net/ipv4/tcp_available_congestion_control";
    public static final String TCP_CONG_TARGET = "/proc/sys/net/ipv4/tcp_congestion_control";
    public static final String PROC_VERSION = "/proc/version";

    public static final String CLOCKS_ON_BOOT_PREF = "clocks_on_boot";
    public static final String MIN_PREF = "clocks_min";
    public static final String MAX_PREF = "clocks_max";
    public static final String MAX_SCREEN_OFF_PREF = "clocks_max_screen_off";
    public static final String GOV_PREF = "clocks_gov";
    public static final String IOSCHED_PREF = "iosched";
    public static final String ZRAM_PREF = "zram";
    public static final String FFC_PREF = "fast_charge";
    public static final String S2W_PREF = "s2w";
    public static final String TCP_CONG_PREF = "tcp_cong_pref";

    private static final int BOOTLOOP_DELAY = 4;
    public static final String BOOTLOOP_TIMEOUT = "bootloop_timeout";
    public static final String BOOTLOOP_TIMEOUT_TIME = "bootloop_timeout_time";

    private static final String KERNEL_VALS = "kernel_values";

    /*
     * Root operations
     */

    public static boolean getRoot() {
        return RootTools.isAccessGiven();
    }

    public static void setPerms() {
        try {
            CommandCapture command = new CommandCapture(0,
                    "chmod 664 /sys/devices/system/cpu/cpu0/cpufreq/scaling_min_freq",
                    "chmod 664 /sys/devices/system/cpu/cpu0/cpufreq/scaling_max_freq",
                    "chmod 664 /sys/devices/system/cpu/cpu0/cpufreq/screen_off_max_freq",
                    "chmod 664 /sys/devices/system/cpu/cpu0/cpufreq/scaling_governor",
                    "chmod 664 /sys/block/zram0/disksize",
                    "chmod 664 /sys/android_touch/sweep2wake",
                    "chmod 664 /sys/kernel/fast_charge/force_fast_charge",
                    "chmod 664 /sys/block/mmcblk0/queue/scheduler",
                    "chown root /sys/devices/system/cpu/cpu0/cpufreq/scaling_min_freq",
                    "chown root /sys/devices/system/cpu/cpu0/cpufreq/scaling_max_freq",
                    "chown root /sys/devices/system/cpu/cpu0/cpufreq/screen_off_max_freq",
                    "chown root /sys/devices/system/cpu/cpu0/cpufreq/scaling_governor",
                    "chown root /sys/block/zram0/disksize",
                    "chown root /sys/android_touch/sweep2wake",
                    "chown root /sys/kernel/fast_charge/force_fast_charge",
                    "chown root /sys/block/mmcblk0/queue/scheduler",
                    "chgrp system /sys/devices/system/cpu/cpu0/cpufreq/scaling_min_freq",
                    "chgrp system /sys/devices/system/cpu/cpu0/cpufreq/scaling_max_freq",
                    "chgrp system /sys/devices/system/cpu/cpu0/cpufreq/screen_off_max_freq",
                    "chgrp system /sys/devices/system/cpu/cpu0/cpufreq/scaling_governor",
                    "chgrp system /sys/block/zram0/disksize",
                    "chgrp system /sys/android_touch/sweep2wake",
                    "chgrp system /sys/kernel/fast_charge/force_fast_charge",
                    "chgrp system /sys/block/mmcblk0/queue/scheduler");
            RootTools.getShell(true).add(command).waitForFinish();
            lockClocks();
        } catch (Exception e) {

        }
    }

    /*
     * we set these entries as read only when we're done some devices
     * mysteriously write the stock default max clock when screen turns on
     * (grouper)
     */

    private static void lockClocks() {
        try {
            CommandCapture command = new CommandCapture(0,
                    "chmod 444 /sys/devices/system/cpu/cpu0/cpufreq/cpuinfo_max_freq",
                    "chmod 444 /sys/devices/system/cpu/cpu0/cpufreq/scaling_max_freq",
                    "chmod 444 /sys/devices/system/cpu/cpu0/cpufreq/screen_off_max_freq",
                    "chmod 444 /sys/devices/system/cpu/cpu1/cpufreq/cpuinfo_max_freq",
                    "chmod 444 /sys/devices/system/cpu/cpu1/cpufreq/scaling_max_freq");
            RootTools.getShell(true).add(command).waitForFinish();
        } catch (Exception e) {

        }
    }

    private static void unlockClocks() {
        try {
            CommandCapture command = new CommandCapture(0,
                    "chmod 664 /sys/devices/system/cpu/cpu0/cpufreq/cpuinfo_max_freq",
                    "chmod 664 /sys/devices/system/cpu/cpu0/cpufreq/scaling_max_freq",
                    "chmod 664 /sys/devices/system/cpu/cpu0/cpufreq/screen_off_max_freq",
                    "chmod 664 /sys/devices/system/cpu/cpu1/cpufreq/cpuinfo_max_freq",
                    "chmod 664 /sys/devices/system/cpu/cpu1/cpufreq/scaling_max_freq");
            RootTools.getShell(true).add(command).waitForFinish();
        } catch (Exception e) {

        }
    }

    private static void sendCommand(String val) {
        try {
            CommandCapture command = new CommandCapture(0, val);
            RootTools.getShell(true).add(command).waitForFinish();
        } catch (Exception e) {

        }
    }

    private static void setExecutable(File f) {
        String path = f.getAbsolutePath();
        StringBuilder b = new StringBuilder()
                .append("chmod 755 ")
                .append(path);
        sendCommand(b.toString());
        b = new StringBuilder()
                .append("chown root ")
                .append(path);
        sendCommand(b.toString());
        b = new StringBuilder()
                .append("chgrp shell ")
                .append(path);
        sendCommand(b.toString());
    }

    /*
     * Kernel control utils We use two types of methods One for manipulating
     * preference file flags for keeping values and the other is for
     * reading/writing to the sysfs interface
     */

    public static String getKernelInfo(Context context) {
        return readKernelValue(context, PROC_VERSION);
    }

    public static boolean hasKernelFeature(String path) {
        return new File(path).exists();
    }

    public static boolean isKernelFeatureEnabled(String feature) {
        try {
            BufferedReader reader = new BufferedReader(new FileReader(
                    new File(feature).getAbsolutePath()));
            String input = reader.readLine();
            reader.close();
            return input.contains("1");
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean writeKernelValue(String flag, String value) {
        try {
            unlockClocks();
            StringBuilder command = new StringBuilder()
                    .append("echo ")
                    .append(value)
                    .append(" > ")
                    .append(flag);
            sendCommand(command.toString());
            lockClocks();
        } catch (Exception e) {
            Log.w("Settings", e.toString());
            return false;
        }
        return true;
    }

    public static String readKernelValue(Context context, String flag) {
        try {
            BufferedReader reader = new BufferedReader(new FileReader(new File(flag)));
            String input = reader.readLine();
            reader.close();
            return input;
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return null;
        }

    }

    public static String[] readKernelList(String flag) {
        try {
            BufferedReader reader = new BufferedReader(new FileReader(flag));
            String[] governors = reader.readLine().split(" ");
            reader.close();
            return governors;
        } catch (IOException e) {
            if (flag.equals(Utils.CPU_AVAIL_GOV)) {
                return new String[] {
                        "interactive", "ondemand"
                };
            } else {
                return null;
            }
        }
    }

    public static void checkZramScripts(Context context) {
        File root = Environment.getExternalStoragePublicDirectory("KernelControls");
        root.mkdir();
        File zram_on = new File(root, "zram_on");
        File zram_off = new File(root, "zram_off");
        if (!zram_on.exists()) {
            try {
                zram_on.createNewFile();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            copyFileToStorage(context, "zram_on", zram_on);
        }
        setExecutable(zram_on);
        if (!zram_off.exists()) {
            try {
                zram_off.createNewFile();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            copyFileToStorage(context, "zram_off", zram_off);
        }
        setExecutable(zram_off);
    }

    public static void enableZram(boolean enabled) {
        File root = Environment.getExternalStoragePublicDirectory("KernelControls");
        root.mkdir();
        File f = enabled ? new File(root, "zram_on") : new File(root, "zram_off");
        StringBuilder b = new StringBuilder()
                .append("/system/bin/sh ")
                .append(f.getAbsolutePath());
        sendCommand(b.toString());
    }

    private static void copyFileToStorage(Context context, String resName, File outFile) {
        AssetManager assetManager = context.getResources().getAssets();
        InputStream inputStream = null;
        OutputStream outputStream = null;

        try {
            inputStream = assetManager.open(resName);
            if (inputStream != null) {
                outputStream = new FileOutputStream(outFile);
                int read = 0;
                byte[] bytes = new byte[1024];

                while ((read = inputStream.read(bytes)) != -1) {
                    outputStream.write(bytes, 0, read);
                }

            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String[] getSchedulers() {
        try {
            BufferedReader reader = new BufferedReader(new FileReader(
                    new File(IO_SCHED).getAbsolutePath()));
            String[] temp = reader.readLine().split(" ");
            reader.close();
            String[] schedulers = new String[temp.length];
            for (int i = 0; i < temp.length; i++) {
                String test = temp[i];
                if (test.contains("[")) {
                    schedulers[i] = test.substring(1, test.length() - 1);
                    continue;
                }
                schedulers[i] = test;
            }
            return schedulers;
        } catch (Exception e) {
            return new String[] {
                    "cfg", "deadline"
            };
        }
    }

    public static String getCurrentScheduler() {
        String defSched = "cfq";
        try {
            BufferedReader reader = new BufferedReader(new FileReader(
                    new File(IO_SCHED).getAbsolutePath()));
            String[] temp = reader.readLine().split(" ");
            reader.close();
            for (int i = 0; i < temp.length; i++) {
                if (temp[i].contains("[")) {
                    defSched = temp[i].substring(1, temp[i].length() - 1);
                    break;
                }
            }

        } catch (Exception e) {
        }
        return defSched;
    }

    private static void stampBootloopTime(Context context) {
        String current = String.valueOf(System.currentTimeMillis());
        writePrefValue(context, BOOTLOOP_TIMEOUT_TIME, current);
    }

    private static long getBootloopTime(Context context) {
        String temp = readPrefValue(context, BOOTLOOP_TIMEOUT_TIME);
        if (temp.equals("null")) temp = "0";
        return Long.parseLong(temp);
    }

    public static boolean shouldApplyValues(Context context) {
        long current = System.currentTimeMillis();
        long lastStamp = getBootloopTime(context);
        long timeDiff = current - lastStamp;
        boolean safeToApply = false;
        /* stamp no matter what here */
        stampBootloopTime(context);
        if (timeDiff > (BOOTLOOP_DELAY * 1000 * 60)) {
            safeToApply = true;
        } else {
            safeToApply = false;
            // danger Will Robinson! diable clocks on boot
            writePrefBoolValue(context, CLOCKS_ON_BOOT_PREF, false);
            // zram needs to be disabled here as well
            // it's hard to dynamically determine if zram
            // is actually enabled, so make sure it's not checked
            // when user returns to app
            writePrefBoolValue(context, ZRAM_PREF, false);
        }
        return safeToApply;
    }

    public static void writePrefValue(Context context, String key, String val) {
        context.getSharedPreferences(KERNEL_VALS, Context.MODE_PRIVATE).edit()
                .putString(key, val).commit();
    }

    public static String readPrefValue(Context context, String key) {
        return context.getSharedPreferences(KERNEL_VALS, Context.MODE_PRIVATE).getString(key,
                "null");
    }

    public static boolean readPrefBoolValue(Context context, String key) {
        String val = readPrefValue(context, key);
        return val.equals("true");
    }

    public static void writePrefBoolValue(Context context, String key, boolean enabled) {
        String val = enabled ? "true" : "false";
        writePrefValue(context, key, val);
    }

    public static String appendClockSuffix(String val) {
        return "" + (Integer.parseInt(val) / 1000) + " MHz";
    }

    public static String removeClockSuffix(String val) {
        return "" + (Integer.parseInt(val.substring(0,
                val.indexOf(" MHz"))) * 1000);
    }
}


package com.brd.apps.kernelcontrols;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.BitmapFactory;

public class OverclockReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (!Utils.readPrefBoolValue(context, Utils.CLOCKS_ON_BOOT_PREF)) {
            return;
        }
        if (Utils.readPrefBoolValue(context, Utils.BOOTLOOP_TIMEOUT)
                && !Utils.shouldApplyValues(context)) {

            Resources res = context.getResources();
            NotificationManager nm = (NotificationManager) context
                    .getSystemService(Context.NOTIFICATION_SERVICE);

            Intent intent1 = new Intent()
                    .setClassName("com.brd.apps.kernelcontrols",
                            "com.brd.apps.kernelcontrols.Main")
                    .addCategory(Intent.CATEGORY_DEFAULT)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            PendingIntent contentIntent = PendingIntent.getActivity(context, 0, intent1, 0);

            Notification.Builder builder = new Notification.Builder(context);

            builder.setContentIntent(contentIntent)
                    .setSmallIcon(android.R.drawable.ic_dialog_alert)
                    .setLargeIcon(
                            BitmapFactory.decodeResource(res, android.R.drawable.ic_dialog_alert))
                    .setTicker(res.getString(R.string.eos_performance_not_applied))
                    .setWhen(System.currentTimeMillis())
                    .setAutoCancel(true)
                    .setContentTitle(res.getString(R.string.eos_performance_notification_title))
                    .setContentText(res.getString(R.string.eos_performance_notification_text));
            Notification n = builder.build();

            nm.notify(0, n);
        } else {
            if (Utils.readPrefBoolValue(context, Utils.CLOCKS_ON_BOOT_PREF)) {
                String val;
                if (Utils.getRoot()) {
                    Utils.setPerms();
                    val = Utils.readPrefValue(context, Utils.MIN_PREF);
                    Utils.writeKernelValue(Utils.CPU_MIN_SCALE, val);
                    val = Utils.readPrefValue(context, Utils.MAX_PREF);
                    Utils.writeKernelValue(Utils.CPU_MAX_SCALE, val);
                    val = Utils.readPrefValue(context, Utils.GOV_PREF);
                    Utils.writeKernelValue(Utils.CPU_GOV, val);
                    val = Utils.readPrefValue(context, Utils.IOSCHED_PREF);
                    Utils.writeKernelValue(Utils.IO_SCHED, val);
                }
                if (Utils.hasKernelFeature(Utils.CPU_MAX_SCREEN_OFF)) {
                    val = Utils.readPrefValue(context, Utils.MAX_SCREEN_OFF_PREF);
                    Utils.writeKernelValue(Utils.CPU_MAX_SCREEN_OFF, val);
                }
                if (Utils.hasKernelFeature(Utils.S2W_PATH)) {
                    boolean enabled = Utils.readPrefBoolValue(context, Utils.S2W_PREF);
                    Utils.writeKernelValue(Utils.S2W_PATH, enabled ? "1" : "0");
                }
                if (Utils.hasKernelFeature(Utils.ZRAM)) {
                    Utils.checkZramScripts(context);
                    Utils.enableZram(Utils.readPrefBoolValue(context, Utils.ZRAM_PREF));
                }
            }
        }
    }
}

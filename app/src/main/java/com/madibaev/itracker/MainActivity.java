package com.madibaev.itracker;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.*;
import android.content.*;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.*;
import android.provider.Settings;
import android.webkit.*;
import androidx.core.app.NotificationCompat;
import java.text.SimpleDateFormat;
import java.util.*;

public class MainActivity extends Activity {
  WebView w;
  static final String CHANNEL="tasks";
  static final int NOTIFY_PERMISSION=44;

  @SuppressLint({"SetJavaScriptEnabled","JavascriptInterface"})
  public void onCreate(Bundle b){
    super.onCreate(b);
    Window win=getWindow();
    win.setStatusBarColor(Color.rgb(5,5,5)); win.setNavigationBarColor(Color.rgb(5,5,5));
    if(Build.VERSION.SDK_INT>=29){win.setStatusBarContrastEnforced(false);win.setNavigationBarContrastEnforced(false);}
    createChannel();
    w=new WebView(this); w.setBackgroundColor(Color.rgb(5,5,5));
    WebSettings s=w.getSettings(); s.setJavaScriptEnabled(true);s.setDomStorageEnabled(true);s.setDefaultTextEncodingName("utf-8");
    w.setWebViewClient(new WebViewClient());w.setWebChromeClient(new WebChromeClient());
    w.addJavascriptInterface(new Bridge(),"iTrackerNative");
    w.loadUrl("file:///android_asset/www/index.html");setContentView(w);
  }
  void createChannel(){if(Build.VERSION.SDK_INT>=26){NotificationManager n=getSystemService(NotificationManager.class);NotificationChannel c=new NotificationChannel(CHANNEL,"Напоминания о задачах",NotificationManager.IMPORTANCE_HIGH);c.setDescription("Дедлайны и напоминания iTracker");n.createNotificationChannel(c);}}
  class Bridge {
    @JavascriptInterface public boolean notificationsGranted(){return Build.VERSION.SDK_INT<33||checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)==PackageManager.PERMISSION_GRANTED;}
    @JavascriptInterface public void requestNotifications(){runOnUiThread(()->{if(Build.VERSION.SDK_INT>=33&&checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)!=PackageManager.PERMISSION_GRANTED)requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS},NOTIFY_PERMISSION);});}
    @JavascriptInterface public void scheduleReminder(long taskId,String title,long whenMs){
      Intent i=new Intent(MainActivity.this,ReminderReceiver.class);i.putExtra("title",title);i.putExtra("taskId",taskId);
      PendingIntent pi=PendingIntent.getBroadcast(MainActivity.this,(int)(taskId&0x7fffffff),i,PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);
      AlarmManager am=(AlarmManager)getSystemService(ALARM_SERVICE);
      if(Build.VERSION.SDK_INT>=31&&!am.canScheduleExactAlarms()) am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP,whenMs,pi);
      else am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP,whenMs,pi);
    }
    @JavascriptInterface public void cancelReminder(long taskId){
      Intent i=new Intent(MainActivity.this,ReminderReceiver.class);
      PendingIntent pi=PendingIntent.getBroadcast(MainActivity.this,(int)(taskId&0x7fffffff),i,PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);
      ((AlarmManager)getSystemService(ALARM_SERVICE)).cancel(pi);
    }
  }
  @Override public void onBackPressed(){if(w!=null&&w.canGoBack())w.goBack();else super.onBackPressed();}
}
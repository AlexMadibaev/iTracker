package com.madibaev.itracker;
import android.app.*;import android.content.*;import androidx.core.app.NotificationCompat;
public class ReminderReceiver extends BroadcastReceiver {
 public void onReceive(Context c,Intent i){
  String title=i.getStringExtra("title"); long id=i.getLongExtra("taskId",0);
  Intent open=new Intent(c,MainActivity.class);open.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TOP);
  PendingIntent pi=PendingIntent.getActivity(c,(int)(id&0x7fffffff),open,PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);
  NotificationCompat.Builder b=new NotificationCompat.Builder(c,"tasks").setSmallIcon(com.madibaev.itracker.R.drawable.ic_notification).setContentTitle("iTracker").setContentText(title==null?"Пора вернуться к задаче":title).setPriority(NotificationCompat.PRIORITY_HIGH).setAutoCancel(true).setContentIntent(pi);
  ((NotificationManager)c.getSystemService(Context.NOTIFICATION_SERVICE)).notify((int)(id&0x7fffffff),b.build());
 }
}
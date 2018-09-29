package notes.apps.aleonqe.com.activitylaunchflags;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import static notes.apps.aleonqe.com.activitylaunchflags.MainActivity.KEY_ACTIVITY_INTENT;
import static notes.apps.aleonqe.com.activitylaunchflags.util.Logutil.toax;

public class ActivityLauncherService extends Service {

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Intent activityIntent = getActivityLauncherIntent(intent);
        if (activityIntent != null) {
            startActivity(activityIntent);
        } else {
            toax(this, "Could not launch, Intent passed was null");
        }
        return START_NOT_STICKY;
    }

    private Intent getActivityLauncherIntent(Intent intent) {
        return intent.getParcelableExtra(KEY_ACTIVITY_INTENT);
    }
}

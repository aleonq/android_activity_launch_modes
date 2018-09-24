package notes.apps.aleonqe.com.activitylaunchflags.util;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

public class Logutil {

    public static final String FILTER_TAG = "FILTERTAG_";
    private static int depth = 2;

    static String what(String... message) {
        String combinedMessage = "";

        String methodName = Thread.currentThread().getStackTrace()[depth].getMethodName();
        String className = Thread.currentThread().getStackTrace()[depth].getFileName().replace(".java", "");

        String filter = FILTER_TAG;
        if (message != null && message.length > 0) {
            filter += message[0];
        }
        filter = filter + " : " + methodName + "()";
        StringBuilder printableMsg = new StringBuilder();
        if (message != null && message.length > 1) {
            filter += ": ";
            for (int i = 1; i < message.length; i++) {
                printableMsg.append(", ").append(message[i]);
            }
            printableMsg = new StringBuilder(printableMsg.toString().replaceFirst(", ", ""));
        }
        printableMsg.insert(0, filter);
        combinedMessage = className + " :: " + printableMsg;
        return combinedMessage;
    }

    public static String logx(String... data) {
        depth = 4;
        String combinedMessage = what(data);
        String[] split = combinedMessage.split(" :: ");
        Log.d(split[0], split[1]);
        return combinedMessage;
    }

    public static void toax(Context context, String message) {
        Toast.makeText(context.getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }
}

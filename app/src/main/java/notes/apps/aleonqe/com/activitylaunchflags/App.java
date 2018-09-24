package notes.apps.aleonqe.com.activitylaunchflags;

import android.app.Application;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

public class App extends Application {

    private Map<Integer, Set<String>> activityStacksMap = new LinkedHashMap<>();
    private Set<ActivityStackObserver> activityStackObservers = new HashSet<>();

    public String getActivityStack(int id) {
        Set<String> set = activityStacksMap.get(id);
        if (set == null) {
            throwUp(id);
        }
        StringBuilder result = new StringBuilder();
        for (String s : set) {
            result.insert(0, "\n" + s);
        }
        return result.toString().toString();
    }

    public Map<Integer, Set<String>> getActivityStacks() {
        return activityStacksMap;
    }

    public void addToActivityStack(int id, String activityRep, String activityPackage, String appPackage) {
        if (activityPackage.equals(appPackage)) {
            Set<String> set = activityStacksMap.get(id);
            if (set == null) {
                set = new LinkedHashSet<>();
            }
            set.add(activityRep);
            activityStacksMap.put(id, set);
            updateAll();
        }
    }

    public void removeFromStack(int id, String activityRep, int totalActivities, String activityPackage, String appPackage) {
        if (activityPackage.equals(appPackage)) {
            Set<String> set = activityStacksMap.get(id);
            if (set == null) {
                throwUp(id);
            }
            boolean isRemoved = set.remove(activityRep);
            if (set.isEmpty()) {
                activityStacksMap.remove(id);
            }
            if (!isRemoved) {
                // iterate and remove
                for (Integer setId : activityStacksMap.keySet()) {
                    boolean result = activityStacksMap.get(setId).remove(activityRep);
                    if (result) {
                        if (activityStacksMap.get(setId).isEmpty()) {
                            activityStacksMap.remove(setId);
                        }
                        break;
                    }
                }
            }
            updateAll();
        }
    }

    private void throwUp(int id) {
        throw new RuntimeException("Activity Stack: " + id + " does not exist!!");
    }

    public void register(ActivityStackObserver observer) {
        activityStackObservers.add(observer);
    }

    public void unRegister(ActivityStackObserver observer) {
        activityStackObservers.remove(observer);
    }

    private void updateAll() {
        for (ActivityStackObserver observer : activityStackObservers) {
            observer.updateUI();
        }
    }
}

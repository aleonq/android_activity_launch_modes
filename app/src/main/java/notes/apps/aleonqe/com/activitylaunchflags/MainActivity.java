package notes.apps.aleonqe.com.activitylaunchflags;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import notes.apps.aleonqe.com.activitylaunchflags.single_instance.Activity1_SingleInstance;
import notes.apps.aleonqe.com.activitylaunchflags.single_instance.Activity2_SingleInstance;
import notes.apps.aleonqe.com.activitylaunchflags.single_instance.Activity3_SingleInstance;
import notes.apps.aleonqe.com.activitylaunchflags.single_instance.Activity4_SingleInstance;
import notes.apps.aleonqe.com.activitylaunchflags.single_task.Activity1_SingleTask;
import notes.apps.aleonqe.com.activitylaunchflags.single_task.Activity2_SingleTask;
import notes.apps.aleonqe.com.activitylaunchflags.single_task.Activity3_SingleTask;
import notes.apps.aleonqe.com.activitylaunchflags.single_task.Activity4_SingleTask;
import notes.apps.aleonqe.com.activitylaunchflags.single_top.Activity1_SingleTop;
import notes.apps.aleonqe.com.activitylaunchflags.single_top.Activity2_SingleTop;
import notes.apps.aleonqe.com.activitylaunchflags.single_top.Activity3_SingleTop;
import notes.apps.aleonqe.com.activitylaunchflags.single_top.Activity4_SingleTop;
import notes.apps.aleonqe.com.activitylaunchflags.standard.Activity1_Standard;
import notes.apps.aleonqe.com.activitylaunchflags.standard.Activity2_Standard;
import notes.apps.aleonqe.com.activitylaunchflags.standard.Activity3_Standard;
import notes.apps.aleonqe.com.activitylaunchflags.standard.Activity4_Standard;

import static notes.apps.aleonqe.com.activitylaunchflags.util.Logutil.logx;

public abstract class MainActivity extends AppCompatActivity implements ActivityStackObserver {

    private static final int LAUNCHMODE_SINGLE_INSTANCE = R.id.rb_single_instance;
    private static final int LAUNCHMODE_SINGLE_TOP = R.id.rb_single_top;
    private static final int LAUNCHMODE_SINGLE_TASK = R.id.rb_single_task;
    private static final int LAUNCHMODE_STANDARD = R.id.rb_standard;

    private int launchMode = LAUNCHMODE_STANDARD;// default

    private TextView tv_savedInstanceState;
    private TextView tv_intentExtras;
    private LinearLayout ll_taskListContainer;

    private CheckBox cb_addIntentExtras;
    private CheckBox cb_newTask;
    private CheckBox cb_clearTask;
    private CheckBox cb_singleTop;
    private CheckBox cb_clearTop;

    private RadioGroup rg_manifestLaunchModes;

    private App application;

    private int stackId;
    private String activityName;
    private int count_allActivitiesOnStack;
    private int count_runningActivitiesOnStack;
    private String activityPackage;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        application = (App) getApplication();

        application.register(this);
        logx("", savedInstanceState == null ? "bundle is null" : "bundle is not null");
        super.onCreate(savedInstanceState);
        setTitle(getClass().getSimpleName() + " : " + hashCode());
        setContentView(R.layout.activity_main);
        init();

        if (savedInstanceState == null) {
            tv_savedInstanceState.setText("Bundle is null");
        } else {
            String result = "";
            result = "Bundle is not null: " + savedInstanceState.hashCode();
            tv_savedInstanceState.setText(result);
        }
        String intentDescriptor = "Intent: " + getIntent().hashCode();
        if (getIntent().getExtras() == null) {
            tv_intentExtras.setText(intentDescriptor + "\n" + "Intent extras are null");
        } else {
            String result = intentDescriptor + "\n" + getIntent().getStringExtra("test_intent_extra");
            tv_intentExtras.setText(result);
        }
        setTaskList();
        application.addToActivityStack(stackId, activityName, activityPackage, getApplication().getPackageName());
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateUI();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        logx();
        super.onNewIntent(intent);
    }

    @Override
    protected void onDestroy() {
        logx();
        setTaskList();
        application.removeFromStack(stackId, activityName, count_allActivitiesOnStack, activityPackage, getApplication().getPackageName());
        super.onDestroy();
        application.unRegister(this);
    }

    private void init() {
        tv_savedInstanceState = findViewById(R.id.tv_save_instance_state);
        tv_intentExtras = findViewById(R.id.tv_intent_extras);
        ll_taskListContainer = findViewById(R.id.ll_task_container);
        cb_addIntentExtras = findViewById(R.id.cb_add_intent_extras);
        cb_newTask = findViewById(R.id.cb_new_task);
        cb_clearTask = findViewById(R.id.cb_clear_task);
        cb_clearTop = findViewById(R.id.cb_clear_top);
        cb_singleTop = findViewById(R.id.cb_single_top);
        rg_manifestLaunchModes = findViewById(R.id.rg_manifestLaunchModes);
    }

    public void updateUI() {
        Map<Integer, Set<String>> activityStacks = application.getActivityStacks();
        List<LinearLayout> linearLayouts = getChildren(activityStacks.size());

        int index = 0;
        for (Integer id : activityStacks.keySet()) {
            LinearLayout ll_taskViewer = linearLayouts.get(index);
            TextView tv_stackTitle = ll_taskViewer.findViewById(R.id.tv_stack_title);
            tv_stackTitle.setText(getString(R.string.stack_info, id, count_allActivitiesOnStack, count_runningActivitiesOnStack));

            TextView tv_stack = ll_taskViewer.findViewById(R.id.tv_stack);
            tv_stack.setText(application.getActivityStack(id));

            View view = ll_taskViewer.findViewById(R.id.view_activeStackIndicator);
            if (stackId == id) {
                view.setBackgroundColor(getColor(R.color.colorAccent));
            } else {
                view.setBackgroundColor(Color.TRANSPARENT);
            }
            index++;
        }
        addChildren(linearLayouts);
    }

    public void launchActivity(View view) {
        int id = view.getId();
        launchMode = rg_manifestLaunchModes.getCheckedRadioButtonId();
        Class activityClass = getActivtiyClass(id, launchMode);
        Intent intent = prepareIntent(activityClass);
        startActivity(intent);
    }

    private Class getActivtiyClass(int id, int manifestLaunchMode) {
        Class activityClass = null;
        switch (id) {
            case R.id.btn_1:
                switch (manifestLaunchMode) {
                    case LAUNCHMODE_SINGLE_INSTANCE:
                        activityClass = Activity1_SingleInstance.class;
                        break;
                    case LAUNCHMODE_SINGLE_TOP:
                        activityClass = Activity1_SingleTop.class;
                        break;
                    case LAUNCHMODE_SINGLE_TASK:
                        activityClass = Activity1_SingleTask.class;
                        break;
                    case LAUNCHMODE_STANDARD:
                        activityClass = Activity1_Standard.class;
                        break;
                }
                break;
            case R.id.btn_2:
                switch (manifestLaunchMode) {
                    case LAUNCHMODE_SINGLE_INSTANCE:
                        activityClass = Activity2_SingleInstance.class;
                        break;
                    case LAUNCHMODE_SINGLE_TOP:
                        activityClass = Activity2_SingleTop.class;
                        break;
                    case LAUNCHMODE_SINGLE_TASK:
                        activityClass = Activity2_SingleTask.class;
                        break;
                    case LAUNCHMODE_STANDARD:
                        activityClass = Activity2_Standard.class;
                        break;
                }
                break;
            case R.id.btn_3:
                switch (manifestLaunchMode) {
                    case LAUNCHMODE_SINGLE_INSTANCE:
                        activityClass = Activity3_SingleInstance.class;
                        break;
                    case LAUNCHMODE_SINGLE_TOP:
                        activityClass = Activity3_SingleTop.class;
                        break;
                    case LAUNCHMODE_SINGLE_TASK:
                        activityClass = Activity3_SingleTask.class;
                        break;
                    case LAUNCHMODE_STANDARD:
                        activityClass = Activity3_Standard.class;
                        break;
                }
                break;
            case R.id.btn_4:
                switch (manifestLaunchMode) {
                    case LAUNCHMODE_SINGLE_INSTANCE:
                        activityClass = Activity4_SingleInstance.class;
                        break;
                    case LAUNCHMODE_SINGLE_TOP:
                        activityClass = Activity4_SingleTop.class;
                        break;
                    case LAUNCHMODE_SINGLE_TASK:
                        activityClass = Activity4_SingleTask.class;
                        break;
                    case LAUNCHMODE_STANDARD:
                        activityClass = Activity4_Standard.class;
                        break;
                }
                break;
        }
        return activityClass;
    }


    private Intent prepareIntent(Class activityClass) {
        Intent intent = new Intent(this, activityClass);
        if (cb_addIntentExtras.isChecked()) {
            intent.putExtra("test_intent_extra", "Test String Extra passed with Intent");
        }
        if (cb_newTask.isChecked()) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }
        if (cb_clearTask.isChecked()) {
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        }
        if (cb_clearTop.isChecked()) {
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        }
        if (cb_singleTop.isChecked()) {
            intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        }
        return intent;
    }

    private void setTaskList() {
        ActivityManager manager = (ActivityManager) getApplication().getSystemService(Activity.ACTIVITY_SERVICE);

        List<ActivityManager.RunningTaskInfo> taskList = manager.getRunningTasks(1);
        if (taskList != null) {
            for (ActivityManager.RunningTaskInfo taskInfo : taskList) {
                logx("123", taskInfo.baseActivity.toShortString());
                activityName = getActivityRepresentation();
                stackId = taskInfo.id;
                count_allActivitiesOnStack = taskInfo.numActivities;
                count_runningActivitiesOnStack = taskInfo.numRunning;
                activityPackage = taskInfo.topActivity.getPackageName();
            }
        }

        List<ActivityManager.RunningAppProcessInfo> processInfoList = manager.getRunningAppProcesses();
        if (taskList != null) {
            for (ActivityManager.RunningAppProcessInfo info : processInfoList) {
                logx("123", info.pkgList.toString());
            }
        }
    }

    private String getActivityRepresentation() {
        String name[] = getClass().getSimpleName().split("_");
        return "\n" + name[0] + " : " + hashCode() + "\n" + name[1];
    }

    private void addChildren(List<LinearLayout> children) {
        ll_taskListContainer.removeAllViews();
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        int margin = (int) getResources().getDimension(R.dimen.standard_margin);
        layoutParams.setMargins(margin, margin, margin, margin);
        for (LinearLayout view : children) {
            view.setLayoutParams(layoutParams);
            ll_taskListContainer.addView(view);
        }
    }

    private List<LinearLayout> getChildren(int count) {
        List<LinearLayout> linearLayouts = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
            LinearLayout textView = (LinearLayout) inflater.inflate(R.layout.task_viewer, null, false);
            linearLayouts.add(textView);
        }
        return linearLayouts;
    }
}

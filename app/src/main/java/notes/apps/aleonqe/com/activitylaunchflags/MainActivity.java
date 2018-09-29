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
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import notes.apps.aleonqe.com.activitylaunchflags.single_instance.SingleInstance_1;
import notes.apps.aleonqe.com.activitylaunchflags.single_instance.SingleInstance_2;
import notes.apps.aleonqe.com.activitylaunchflags.single_instance.SingleInstance_3;
import notes.apps.aleonqe.com.activitylaunchflags.single_instance.SingleInstance_4;
import notes.apps.aleonqe.com.activitylaunchflags.single_task.SingleTask_1;
import notes.apps.aleonqe.com.activitylaunchflags.single_task.SingleTask_2;
import notes.apps.aleonqe.com.activitylaunchflags.single_task.SingleTask_3;
import notes.apps.aleonqe.com.activitylaunchflags.single_task.SingleTask_4;
import notes.apps.aleonqe.com.activitylaunchflags.single_top.SingleTop_1;
import notes.apps.aleonqe.com.activitylaunchflags.single_top.SingleTop_2;
import notes.apps.aleonqe.com.activitylaunchflags.single_top.SingleTop_3;
import notes.apps.aleonqe.com.activitylaunchflags.single_top.SingleTop_4;
import notes.apps.aleonqe.com.activitylaunchflags.standard.Standard_1;
import notes.apps.aleonqe.com.activitylaunchflags.standard.Standard_2;
import notes.apps.aleonqe.com.activitylaunchflags.standard.Standard_3;
import notes.apps.aleonqe.com.activitylaunchflags.standard.Standard_4;

import static notes.apps.aleonqe.com.activitylaunchflags.util.Logutil.logx;
import static notes.apps.aleonqe.com.activitylaunchflags.util.Logutil.toax;

public abstract class MainActivity extends AppCompatActivity implements ActivityStackObserver {

    public static final String KEY_ACTIVITY_INTENT = "activity_intent";
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

    private RadioGroup rg_finishLaunchingActivity;
    private CheckBox cb_launchViaService;
    private TextView tv_activitiesTypeTitle;
    private Button btn_1;
    private Button btn_2;
    private Button btn_3;
    private Button btn_4;

    private RadioGroup rg_manifestLaunchModes;

    private App application;

    private int stackId;
    private String activityName;
    private int count_allActivitiesOnStack;
    private int count_runningActivitiesOnStack;

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
        application.addToActivityStack(stackId, activityName);
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateActivityStackUI();
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
        application.removeFromStack(stackId, activityName, count_allActivitiesOnStack);
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
        tv_activitiesTypeTitle = findViewById(R.id.tv_activities_type_title);
        rg_finishLaunchingActivity = findViewById(R.id.rg_finish_launching_activity);
        cb_launchViaService = findViewById(R.id.cb_launch_via_service);
        btn_1 = findViewById(R.id.btn_1);
        btn_2 = findViewById(R.id.btn_2);
        btn_3 = findViewById(R.id.btn_3);
        btn_4 = findViewById(R.id.btn_4);
        rg_manifestLaunchModes = findViewById(R.id.rg_manifestLaunchModes);
        rg_manifestLaunchModes.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                updateActivityLaunchButtons(checkedId);
            }
        });
        updateActivityLaunchButtons(launchMode);
    }

    protected void updateActivityLaunchButtons(int checkedId) {
        String activityClass = "";
        switch (checkedId) {
            case LAUNCHMODE_SINGLE_INSTANCE:
                activityClass = "SingleInstance";
                break;
            case LAUNCHMODE_SINGLE_TOP:
                activityClass = "SingleTop";
                break;
            case LAUNCHMODE_SINGLE_TASK:
                activityClass = "SingleTask";
                break;
            case LAUNCHMODE_STANDARD:
                activityClass = "Standard";
                break;
        }

        tv_activitiesTypeTitle.setText(activityClass + "Activities:");
        // as we have four activity classes per manifest launch mode
        btn_1.setText(activityClass + "_1");
        btn_2.setText(activityClass + "_2");
        btn_3.setText(activityClass + "_3");
        btn_4.setText(activityClass + "_4");
    }

    public void updateActivityStackUI() {
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

        if (rg_finishLaunchingActivity.getCheckedRadioButtonId() == R.id.rb_finish_before_launch) {
            finish();
        }
        if (cb_launchViaService.isChecked()) {
            // launch the new activity via service
            toax(this, "Launching activity via service");
            Intent serviceIntent = new Intent(this, ActivityLauncherService.class);
            serviceIntent.putExtra(KEY_ACTIVITY_INTENT, intent);
            startService(serviceIntent);
        } else {
            // launch the new activity via current activity
            toax(this, "Launching activity via current activity");
            startActivity(intent);
        }
        if (rg_finishLaunchingActivity.getCheckedRadioButtonId() == R.id.rb_finish_after_launch) {
            finish();
        }
    }

    private Class getActivtiyClass(int id, int manifestLaunchMode) {
        Class activityClass = null;
        switch (id) {
            case R.id.btn_1:
                switch (manifestLaunchMode) {
                    case LAUNCHMODE_SINGLE_INSTANCE:
                        activityClass = SingleInstance_1.class;
                        break;
                    case LAUNCHMODE_SINGLE_TOP:
                        activityClass = SingleTop_1.class;
                        break;
                    case LAUNCHMODE_SINGLE_TASK:
                        activityClass = SingleTask_1.class;
                        break;
                    case LAUNCHMODE_STANDARD:
                        activityClass = Standard_1.class;
                        break;
                }
                break;
            case R.id.btn_2:
                switch (manifestLaunchMode) {
                    case LAUNCHMODE_SINGLE_INSTANCE:
                        activityClass = SingleInstance_2.class;
                        break;
                    case LAUNCHMODE_SINGLE_TOP:
                        activityClass = SingleTop_2.class;
                        break;
                    case LAUNCHMODE_SINGLE_TASK:
                        activityClass = SingleTask_2.class;
                        break;
                    case LAUNCHMODE_STANDARD:
                        activityClass = Standard_2.class;
                        break;
                }
                break;
            case R.id.btn_3:
                switch (manifestLaunchMode) {
                    case LAUNCHMODE_SINGLE_INSTANCE:
                        activityClass = SingleInstance_3.class;
                        break;
                    case LAUNCHMODE_SINGLE_TOP:
                        activityClass = SingleTop_3.class;
                        break;
                    case LAUNCHMODE_SINGLE_TASK:
                        activityClass = SingleTask_3.class;
                        break;
                    case LAUNCHMODE_STANDARD:
                        activityClass = Standard_3.class;
                        break;
                }
                break;
            case R.id.btn_4:
                switch (manifestLaunchMode) {
                    case LAUNCHMODE_SINGLE_INSTANCE:
                        activityClass = SingleInstance_4.class;
                        break;
                    case LAUNCHMODE_SINGLE_TOP:
                        activityClass = SingleTop_4.class;
                        break;
                    case LAUNCHMODE_SINGLE_TASK:
                        activityClass = SingleTask_4.class;
                        break;
                    case LAUNCHMODE_STANDARD:
                        activityClass = Standard_4.class;
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
        return "\n" + getClass().getSimpleName() + "\n" + hashCode();
    }

    private void addChildren(List<LinearLayout> children) {
        ll_taskListContainer.removeAllViews();
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        int margin = (int) getResources().getDimension(R.dimen.standard_gutter);
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

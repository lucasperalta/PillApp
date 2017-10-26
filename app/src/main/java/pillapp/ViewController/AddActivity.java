package pillapp.ViewController;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Headers;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import pillapp.PrefManager;
import teamqitalach.pillapp.R;

import pillapp.Model.Alarm;
import pillapp.Model.Pill;
import pillapp.Model.PillBox;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.graphics.Typeface;
import android.nfc.Tag;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Utilized the link below as a reference guide:
 * http://wptrafficanalyzer.in/blog/setting-up-alarm-using-alarmmanager-and-waking-up-screen-and-unlocking-keypad-on-alarm-goes-off-in-android/
 * <p>
 * This activity handles the view and controller of the add page, where the user can add an alarm
 */


public class AddActivity extends ActionBarActivity {
    private AlarmManager alarmManager;
    private PendingIntent operation;
    private boolean dayOfWeekList[] = new boolean[7];

    int hour, minute;

    PillBox pillBox = new PillBox();
    private final OkHttpClient client = new OkHttpClient();
    private ProgressBar spinner;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);

        setContentView(R.layout.activity_add);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Actualizando alarmas");
        spinner = (ProgressBar) findViewById(R.id.progressBar);
        spinner.setVisibility(View.VISIBLE);

        PrefManager prefManager = new PrefManager(this);
        String u = prefManager.getEmail();
        Request request = new Request.Builder()
                .url("http://10.0.2.2:8080/usuario/user/"+u)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try {
                    int checkBoxCounter = 0;
                    String jsonData = response.body().string();
                    String pill_name="";
                    Arrays.fill(dayOfWeekList, Boolean.FALSE);

                    JSONObject jobject = new JSONObject(jsonData);

                    Log.i("JSON", String.valueOf(jobject));
                    JSONArray Jarray = jobject.getJSONArray("alarmas");



                    for (int j = 0; j < Jarray.length(); j++) {
                        checkBoxCounter = 0;
                        Arrays.fill(dayOfWeekList, Boolean.FALSE);
                        pill_name="";
                        JSONObject object = Jarray.getJSONObject(j);

                       String[] dias= object.getString("diaDeSemana").split(",");
                        for (int x=0;x<dias.length;x++){
                            int pos=Integer.valueOf(dias[x]);
                            dayOfWeekList[pos-1]=true;
                        }


                         pill_name = object.getString("nombre");
                         hour=Integer.valueOf(object.getString("hora"));
                         minute=Integer.valueOf(object.getString("minutos"));



                        /** Updating model */
                        Alarm alarm = new Alarm();

                        /** If Pill does not already exist */
                        if (!pillBox.pillExist(getApplicationContext(), pill_name)) {
                            Pill pill = new Pill();
                            pill.setPillName(pill_name);
                            alarm.setHour(hour);
                            alarm.setMinute(minute);
                            alarm.setPillName(pill_name);
                            alarm.setDayOfWeek(dayOfWeekList);
                            pill.addAlarm(alarm);
                            long pillId = pillBox.addPill(getApplicationContext(), pill);
                            pill.setPillId(pillId);
                            pillBox.addAlarm(getApplicationContext(), alarm, pill);
                        }else { // If Pill already exists
                            Pill pill = pillBox.getPillByName(getApplicationContext(), pill_name);
                            alarm.setHour(hour);
                            alarm.setMinute(minute);
                            alarm.setPillName(pill_name);
                            alarm.setDayOfWeek(dayOfWeekList);
                            pill.addAlarm(alarm);
                            pillBox.addAlarm(getApplicationContext(), alarm, pill);
                        }
                        List<Long> ids = new LinkedList<Long>();
                        try {
                            List<Alarm> alarms = pillBox.getAlarmByPill(getApplicationContext(), pill_name);
                            for (Alarm tempAlarm : alarms) {
                                if (tempAlarm.getHour() == hour && tempAlarm.getMinute() == minute) {
                                    ids = tempAlarm.getIds();
                                    break;
                                }
                            }
                        } catch (URISyntaxException e) {
                            e.printStackTrace();
                        }

                        for (int i = 0; i < 7; i++) {
                            if (dayOfWeekList[i] && pill_name.length() != 0) {

                                int dayOfWeek = i + 1;

                                long _id = ids.get(checkBoxCounter);
                                int id = (int) _id;
                                checkBoxCounter++;

                                /** This intent invokes the activity AlertActivity, which in turn opens the AlertAlarm window */
                                Intent intent = new Intent(getBaseContext(), AlertActivity.class);
                                intent.putExtra("pill_name", pill_name);

                                operation = PendingIntent.getActivity(getBaseContext(), id, intent, PendingIntent.FLAG_UPDATE_CURRENT);

                                /** Getting a reference to the System Service ALARM_SERVICE */
                                alarmManager = (AlarmManager) getBaseContext().getSystemService(ALARM_SERVICE);

                                /** Creating a calendar object corresponding to the date and time set by the user */
                                Calendar calendar = Calendar.getInstance();

                                calendar.set(Calendar.HOUR_OF_DAY, hour);
                                calendar.set(Calendar.MINUTE, minute);
                                calendar.set(Calendar.SECOND, 0);
                                calendar.set(Calendar.MILLISECOND, 0);
                                calendar.set(Calendar.DAY_OF_WEEK, dayOfWeek);

                                /** Converting the date and time in to milliseconds elapsed since epoch */
                                long alarm_time = calendar.getTimeInMillis();

                                if (calendar.before(Calendar.getInstance()))
                                    alarm_time += AlarmManager.INTERVAL_DAY * 7;

                                alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, alarm_time,
                                        alarmManager.INTERVAL_DAY * 7, operation);
                            }
                        }

                    }


                    if (checkBoxCounter == 0 || pill_name.length() == 0)
                        Toast.makeText(getBaseContext(), "Please input a pill name or check at least one day!", Toast.LENGTH_SHORT).show();
                    else { // Input form is completely filled out

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                spinner.setVisibility(View.GONE);

                                Intent returnHome = new Intent(getBaseContext(), MainActivity.class);
                                startActivity(returnHome);
                                finish();
                            }
                        });


                    }


                } catch (Exception e) {
                    e.printStackTrace();
                }


            }
        });


    }

    @Override
    /** Inflate the menu; this adds items to the action bar if it is present */
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_add, menu);
        return true;
    }

    public void onCheckboxClicked(View view) {
        boolean checked = ((CheckBox) view).isChecked();

        /** Checking which checkbox was clicked */
        switch (view.getId()) {
            case R.id.checkbox_monday:
                if (checked)
                    dayOfWeekList[1] = true;
                else
                    dayOfWeekList[1] = false;
                break;
            case R.id.checkbox_tuesday:
                if (checked)
                    dayOfWeekList[2] = true;
                else
                    dayOfWeekList[2] = false;
                break;
            case R.id.checkbox_wednesday:
                if (checked)
                    dayOfWeekList[3] = true;
                else
                    dayOfWeekList[3] = false;
                break;
            case R.id.checkbox_thursday:
                if (checked)
                    dayOfWeekList[4] = true;
                else
                    dayOfWeekList[4] = false;
                break;
            case R.id.checkbox_friday:
                if (checked)
                    dayOfWeekList[5] = true;
                else
                    dayOfWeekList[5] = false;
                break;
            case R.id.checkbox_saturday:
                if (checked)
                    dayOfWeekList[6] = true;
                else
                    dayOfWeekList[6] = false;
                break;
            case R.id.checkbox_sunday:
                if (checked)
                    dayOfWeekList[0] = true;
                else
                    dayOfWeekList[0] = false;
                break;
            case R.id.every_day:
                LinearLayout ll = (LinearLayout) findViewById(R.id.checkbox_layout);
                for (int i = 0; i < ll.getChildCount(); i++) {
                    View v = ll.getChildAt(i);
                    ((CheckBox) v).setChecked(checked);
                    onCheckboxClicked(v);
                }
                break;
        }
    }


    @Override
    /**
     * Handle action bar item clicks here. The action bar will
     * automatically handle clicks on the Home/Up button, so long
     * as you specify a parent activity in AndroidManifest.xml.
     */
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.home) {
            NavUtils.navigateUpFromSameTask(this);
            return true;
        }
        Intent returnHome = new Intent(getBaseContext(), MainActivity.class);
        startActivity(returnHome);
        finish();
        return super.onOptionsItemSelected(item);
    }

    /**
     * This method takes hours and minute as input and returns
     * a string that is like "12:01pm"
     */
    public String setTime(int hour, int minute) {
        String am_pm = (hour < 12) ? "am" : "pm";
        int nonMilitaryHour = hour % 12;
        if (nonMilitaryHour == 0)
            nonMilitaryHour = 12;
        String minuteWithZero;
        if (minute < 10)
            minuteWithZero = "0" + minute;
        else
            minuteWithZero = "" + minute;
        return nonMilitaryHour + ":" + minuteWithZero + am_pm;
    }

    @Override
    public void onBackPressed() {
        Intent returnHome = new Intent(getBaseContext(), MainActivity.class);
        startActivity(returnHome);
        finish();
    }
}
package sandy.android.assistant.Controller;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;

import sandy.android.assistant.Fragment.DatePickerFragment;
import sandy.android.assistant.Fragment.TimePickerFragment;
import sandy.android.assistant.Model.CalendarSync;
import sandy.android.assistant.Model.DatabaseManagement;
import sandy.android.assistant.Model.Notification;
import sandy.android.assistant.Adapter.NotificationAdapter;
import sandy.android.assistant.R;

public class NotificationEditorActivity extends AppCompatActivity implements DatePickerDialog.OnDateSetListener, TimePickerDialog.OnTimeSetListener {
    public static String CLASS_NOTE_EDITOR_ACTIVITY_NAME = NoteEditorActivity.class.toString();
    public static String CLASS_NOTIFICATION_ADAPTER_NAME = NotificationAdapter.class.toString();

    Notification notification = new Notification();
    DatabaseManagement db;

    String date, time;

    Button cancelNotificationButton;
    Button datePickerButton;
    Button timePickerButton;
    Button saveNotificationButton;

    CalendarSync calendarSync;
    Context context;

    DialogFragment timePicker;
    DialogFragment datePicker;

    TextView datePickerTextView;
    TextView timePickerTextView;

    Notification editNotification;

    String year,month,day,hour,minute;
    String selectedDateString = null;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.notification_editor);

        calendarSync = new CalendarSync();
        context = getApplicationContext();

        cancelNotificationButton = findViewById(R.id.cancelNotificationButton);
        datePickerButton = (Button) findViewById(R.id.datePickerButton);
        timePickerButton = (Button) findViewById(R.id.timePickerButton);
        saveNotificationButton = (Button) findViewById(R.id.saveNotificationButton);

        datePickerTextView = (TextView) findViewById(R.id.datePickertextView);
        timePickerTextView = (TextView) findViewById(R.id.timePickertextView);

        db = new DatabaseManagement(this);


        //getting notification of the note if it has one and updates the screen
        Bundle b = getIntent().getExtras();
        if(b != null){
            if(b.get("NOTIFICATION_ID") != null){
                editNotification = db.getNotificationFromNotificationID(b.getInt("NOTIFICATION_ID"));
                updateNotificationActivity(editNotification);
                //System.out.println(b.getInt("NOTIFICATION_ID"));
            }
        }

        saveNotificationButton.setOnClickListener(new View.OnClickListener() {  //save button
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onClick(View v) {
                //FIXME date-time format is a mess rn ngl
                if(date == null || time == null){
                    return;
                }

                String currentDateString = date + "T" + time + ":00" + "Z";     //I use this format because others did not work

                //System.out.println(notification.getDate()); //debug code

                //this currently passes back only the date not the object, makes the code a bit spaghetti.

                if(getCallingActivity().getClassName().equals(NoteEditorActivity.class.toString().replace("class ",""))){
                    Intent data = new Intent(getApplicationContext(), NoteEditorActivity.class);
                    data.putExtra("NOTIFICATION_DATE", currentDateString);
                    setResult(Activity.RESULT_OK, data);
                }
                else if(getCallingActivity().getClassName().equals(NotificationViewActivity.class.toString().replace("class ",""))){
                    Notification newNotification = new Notification(currentDateString);
                    db.updateNotification(newNotification, editNotification);
                    int numberOfRowsAffected = 0;
                    numberOfRowsAffected = calendarSync.updateCalendarEntry(context, editNotification.getId(), db.getNoteFromNotificationId(editNotification.getId()));
                    if (numberOfRowsAffected > 0) {
                        Toast.makeText(context, context.getResources().getString(R.string.calendar_event_updated), Toast.LENGTH_SHORT).show();
                    }
                }


                finish();

                return;

            }

        });

        timePickerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(editNotification != null){
                    timePicker = new TimePickerFragment(new Integer(hour), new Integer(minute));
                }
                else
                    timePicker = new TimePickerFragment();

                timePicker.show(getSupportFragmentManager(), "time picker");

            }
        });

        datePickerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(editNotification != null){
                    datePicker = new DatePickerFragment(new Integer(year), new Integer(month)-1, new Integer(day));
                }
                else
                    datePicker = new DatePickerFragment();

                datePicker.show(getSupportFragmentManager(), "date picker");
            }
        });

        cancelNotificationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {           //cancel button
                finish();       //notification screen cancelled. go back to note editor.
            }
        });
    }

    @Override
    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) { //choosing date
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, year);
        calendar.set(Calendar.MONTH, month);
        calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
        month = month + 1;

        selectedDateString = DateFormat.getDateInstance(DateFormat.FULL).format(calendar.getTime());
        DateFormat formatter = DateFormat.getDateInstance(DateFormat.FULL);
        DateFormat formatter1 = new SimpleDateFormat("dd-MM-yyyy");
        try {
            datePickerTextView.setText("Date: " + formatter1.format(formatter.parse(selectedDateString)));
            selectedDateString = formatter1.format(formatter.parse(selectedDateString));
        } catch (ParseException e) {
            e.printStackTrace();
        }

        date = year + "-" + month + "-" + dayOfMonth;

    }

    @Override
    public void onTimeSet(TimePicker view, int hourOfDay, int minute) { // choosing time

        Calendar datetime = Calendar.getInstance();
        Calendar current = Calendar.getInstance();
        datetime.set(Calendar.HOUR_OF_DAY, hourOfDay);
        datetime.set(Calendar.MINUTE, minute);

        DateFormat formatter = new SimpleDateFormat("dd-MM-yyyy");
        DateFormat formatter1 = DateFormat.getDateInstance(DateFormat.FULL);
        Date currentDate = Calendar.getInstance().getTime();

        String currentDateString = formatter.format(currentDate);

        System.out.println("current date:" + currentDateString);
        System.out.println("day month year: " + day + "-" + month + "-" + year);
        System.out.println("selected date:" + selectedDateString);

        if (selectedDateString == null) {
            if (currentDateString.equals(day + "-" + month + "-" + year)) {
                if (datetime.getTimeInMillis() >= current.getTimeInMillis()) {
                    timePickerTextView.setText("Hour: " + hourOfDay + " Minute: " + minute);
                    time = hourOfDay + ":" + minute;
                } else {
                    //it's before current'
                    Toast.makeText(getApplicationContext(), getResources().getString(R.string.notification_past_time_error), Toast.LENGTH_LONG).show();
                }
            }
            else {
                timePickerTextView.setText("Hour: " + hourOfDay + " Minute: " + minute);
                time = hourOfDay + ":" + minute;
            }
        }
        else {
            if (currentDateString.equals(selectedDateString)) {
                if (datetime.getTimeInMillis() >= current.getTimeInMillis()) {
                    timePickerTextView.setText("Hour: " + hourOfDay + " Minute: " + minute);
                    time = hourOfDay + ":" + minute;
                } else {
                    //it's before current'
                    Toast.makeText(getApplicationContext(), getResources().getString(R.string.notification_past_time_error), Toast.LENGTH_LONG).show();
                }
            }
            else {
                timePickerTextView.setText("Hour: " + hourOfDay + " Minute: " + minute);
                time = hourOfDay + ":" + minute;
            }
        }


    }

    public void updateNotificationActivity(Notification n){

        parse();
        timePickerTextView.setText(getResources().getString(R.string.hour_text) + ": " + hour + " " + getResources().getString(R.string.minute_text) + ": " + minute);
        datePickerTextView.setText(getResources().getString(R.string.date_text) + ": " + day + "-" + month + "-" + year);

    }

    public void parse(){
        String date = "";
        String time = "";
        String date_time = editNotification.getDate();


        date = date_time.substring(0,date_time.indexOf('T'));
        time = date_time.substring(date_time.indexOf('T') +1, date_time.indexOf('Z'));

        year = date.split("-")[0];
        month = date.split("-")[1];
        day = date.split("-")[2];

        hour = time.split(":")[0];
        minute = time.split(":")[1];

        this.date = year + "-" + month + "-" + day;
        this.time = hour + ":"  + minute;
    }
}

package sandy.android.assistant;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.RecyclerView;

import com.github.irshulx.Editor;

import java.util.ArrayList;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.MyViewHolder> {

    DatabaseManagement db;
    ArrayList<Note> mNoteWithNotificationList;
    LayoutInflater inflater;
    Editor editor;
    Activity activity;
    CalendarSync calendarSync = new CalendarSync();
    MainActivity mainActivity = new MainActivity();

    public NotificationAdapter(Context context, ArrayList<Note> notesWithNotification, DatabaseManagement db, MainActivity ma) {
        inflater = LayoutInflater.from(context);
        this.mNoteWithNotificationList = notesWithNotification;
        this.db = db;
        this.activity = (Activity) context;
        this.mainActivity = ma;
    }

    public NotificationAdapter(Context context, ArrayList<Note> notesWithNotification, DatabaseManagement db) {
        inflater = LayoutInflater.from(context);
        this.mNoteWithNotificationList = notesWithNotification;
        this.db = db;
        this.activity = (Activity) context;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.item_notification_cardview, parent, false);
        MyViewHolder holder = new MyViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        Note selectedNote = mNoteWithNotificationList.get(position);

        if(selectedNote != null)
            holder.setData(selectedNote, position);
    }

    @Override
    public int getItemCount() {
        return mNoteWithNotificationList.size();
    }

    class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        TextView notificationTitle, notificationDescription;
        ImageView deleteNotification;
        LinearLayout notificationSelectionLinearLayout;

        public MyViewHolder(View itemView) {
            super(itemView);

            notificationTitle = (TextView) itemView.findViewById(R.id.notificationTitle);
            notificationDescription = (TextView) itemView.findViewById(R.id.notificationDescription);

            notificationSelectionLinearLayout = (LinearLayout) itemView.findViewById(R.id.notificationSelectionLinearLayout);
            notificationSelectionLinearLayout.setOnClickListener(this);

            deleteNotification = (ImageView) itemView.findViewById(R.id.deleteNotification);
            deleteNotification.setOnClickListener(this);

        }

        public void setData(Note selectedNote, int position) {


                this.notificationTitle.setText(selectedNote.getTitle());
                this.notificationDescription.setText(selectedNote.getNotification().getDate());

        }


        @Override
        public void onClick(View v) {
            if (v == deleteNotification) {
                deleteNotification(getLayoutPosition());
            }
            else if (v == notificationSelectionLinearLayout) {
                openNotification(getLayoutPosition());
            }

        }

        private void deleteNotification(int position) {
            Notification notificationToDelete = mNoteWithNotificationList.get(position).getNotification();

            Boolean returnVal = db.deleteNotification(notificationToDelete);

            ArrayList<Note> notes = db.getAllNotes();

            if (returnVal) {
                notifyRemoved(position);
                int deletedRows = 0;
                deletedRows = calendarSync.deleteCalendarEntry(activity.getApplicationContext(), notificationToDelete.getId());
                if (deletedRows > 0) {
                    Toast.makeText(activity, "Calendar event attached to deleted Notification is also successfully deleted.", Toast.LENGTH_LONG).show();
                }
                /*else {
                    Toast.makeText(activity, "Calendar event attached to deleted Notification couldn't be deleted.", Toast.LENGTH_LONG).show();
                }*/
            }
            else {
                Toast.makeText(activity, "Notification couldn't be deleted.", Toast.LENGTH_LONG);
            }

            refresh();
        }

        private void openNotification(int position) {
            Notification notificationToOpen = mNoteWithNotificationList.get(position).getNotification();

            try {
                db.getNotificationFromNotificationID(notificationToOpen.getId());
                Intent intent = new Intent(activity.getApplicationContext(), NotificationEditorActivity.class);
                intent.putExtra("NOTIFICATION_ID", notificationToOpen.getId());
                activity.startActivityForResult(intent,0);
            }
            catch(Exception e){
                Toast.makeText(activity, "The notification couldn't be opened.", Toast.LENGTH_LONG);
            }

            refresh();
        }

        private void refresh(){
            ArrayList<Note> notes = db.getAllNotes();
            ArrayList<Note> notesWithNotification = new ArrayList<Note>();

            for (int index = 0; index < notes.size(); index++) {
                if (notes.get(index).getNotification() != null) {
                    notesWithNotification.add(notes.get(index));
                }
            }

            mNoteWithNotificationList = notesWithNotification;
        }

        public void notifyRemoved(int position) {
            notifyItemRemoved(position);
            notifyItemRangeChanged(position, mNoteWithNotificationList.size());
        }

        public void notifyInserted(int position) {
            notifyItemInserted(position);
            notifyItemRangeChanged(position, mNoteWithNotificationList.size());
        }

        public void notifyChanged() {
            notifyDataSetChanged();
        }


    }


}

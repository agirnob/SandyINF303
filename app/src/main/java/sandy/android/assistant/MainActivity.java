package sandy.android.assistant;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;


public class MainActivity extends AppCompatActivity {
    Toolbar toolbar;
    RecyclerView recyclerView;
    View view;
    NoteEditorController nec = new NoteEditorController();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button button_open_note_editor = (Button) findViewById(R.id.button_open_note_editor);

        button_open_note_editor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setContentView(R.layout.note_editor);
                Intent intent = new Intent(getApplicationContext(), NoteEditorController.class);
                startActivity(intent);
                //nec.onCreate(savedInstanceState);

            }
        });


    }
}
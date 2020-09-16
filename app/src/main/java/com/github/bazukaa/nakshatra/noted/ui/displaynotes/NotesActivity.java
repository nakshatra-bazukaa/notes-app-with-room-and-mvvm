package com.github.bazukaa.nakshatra.noted.ui.displaynotes;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import com.github.bazukaa.nakshatra.noted.db.entity.TrashNote;
import com.github.bazukaa.nakshatra.noted.adapter.NoteAdapter;
import com.github.bazukaa.nakshatra.noted.ui.displaytrashnotes.TrashNotesActivity;
import com.github.bazukaa.nakshatra.noted.ui.makeeditnote.MakeEditNoteActivity;
import com.github.bazukaa.nakshatra.noted.R;
import com.github.bazukaa.nakshatra.noted.db.entity.Note;
import com.github.bazukaa.nakshatra.noted.viewmodel.NoteViewModel;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.text.SimpleDateFormat;
import java.util.Date;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class NotesActivity extends AppCompatActivity {

    // Shared preference
    public static final String SHARED_PREFERENCE = "sharedPrefs";
    public static final String SET_GRID = "set grid";

    // Variables for Shared Preferences
    private boolean isGrid;

    // to set grid/list mode
    public static final boolean GRID_MODE = true;
    public static final boolean LIST_MODE = false;

    public static final int ADD_NOTE_REQUEST = 1;
    public static final int EDIT_NOTE_REQUEST = 2;

    private NoteViewModel noteViewModel;

    private RecyclerView.LayoutManager layoutManager;

    private MenuItem grid;
    private MenuItem list;
    private MenuItem trash;

    @BindView(R.id.toolbar)
    Toolbar toolbar;

    @BindView(R.id.act_main_rv_notes)
    RecyclerView noteRecyclerView;

    @BindView(R.id.act_main_fab_add)
    FloatingActionButton addButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notes);
        ButterKnife.bind(this);

        // Setting up the toolbar
        setSupportActionBar(toolbar);

        NoteAdapter adapter = new NoteAdapter();
        noteRecyclerView.setAdapter(adapter);

        // Setting grid/list mode
        loadData();
        if(isGrid)
            setGrid();
        else
            setList();

        noteViewModel = ViewModelProviders.of(this).get(NoteViewModel.class);
        noteViewModel.getNotes().observe(this, notes -> adapter.setNotes(notes));

        //To delete a note
        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0,
                ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }
            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                Note note = adapter.getNotePosition(viewHolder.getAdapterPosition());
                TrashNote trashNote = new TrashNote(note.getTitle(), note.getNote(), note.getTimeStamp());
                noteViewModel.insert(trashNote);
                noteViewModel.delete(note);
                Toast.makeText(NotesActivity.this, "Note moved to trash", Toast.LENGTH_SHORT).show();
            }
        }).attachToRecyclerView(noteRecyclerView);

        //To Edit a note
        adapter.setOnItemClickListener(note -> {
            Intent intent = new Intent(NotesActivity.this, MakeEditNoteActivity.class);
            intent.putExtra(MakeEditNoteActivity.EXTRA_ID, note.getId());
            intent.putExtra(MakeEditNoteActivity.EXTRA_TITLE, note.getTitle());
            intent.putExtra(MakeEditNoteActivity.EXTRA_NOTE, note.getNote());

            //Formatting currentTimeMillis in desired form before sending to MakeEditNoteActivity
            long currentTimeMillis = note.getTimeStamp();
            SimpleDateFormat sdf = new SimpleDateFormat("hh:mm aaa MMM dd, yyyy");
            Date resultdate = new Date(currentTimeMillis);
            String timeStamp = "Last changed";
            timeStamp = timeStamp+" "+String.valueOf(sdf.format(resultdate));
            intent.putExtra(MakeEditNoteActivity.EXTRA_TIMESTAMP, timeStamp);

            startActivityForResult(intent, EDIT_NOTE_REQUEST);
        });
    }

    //To create a new note
    @OnClick(R.id.act_main_fab_add)
    public void onFabClicked() {
        Intent intent = new Intent(NotesActivity.this, MakeEditNoteActivity.class);
        startActivityForResult(intent, ADD_NOTE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == ADD_NOTE_REQUEST && resultCode == RESULT_OK) {
            String title = data.getStringExtra(MakeEditNoteActivity.EXTRA_TITLE);
            String note = data.getStringExtra(MakeEditNoteActivity.EXTRA_NOTE);
            long timeStamp = data.getLongExtra(MakeEditNoteActivity.EXTRA_TIMESTAMP, 10000);

            Note notedNote = new Note(title, note, timeStamp);
            noteViewModel.insert(notedNote);

            Toast.makeText(this, "Note Saved successfully", Toast.LENGTH_SHORT).show();
        } else if (requestCode == ADD_NOTE_REQUEST) {
            Toast.makeText(this, "Empty Note not saved", Toast.LENGTH_SHORT).show();
        }

        if (requestCode == EDIT_NOTE_REQUEST && resultCode == RESULT_OK) {
            int id = data.getIntExtra(MakeEditNoteActivity.EXTRA_ID, -1);
            if (id == -1) {
                Toast.makeText(this, "Note can't be updated", Toast.LENGTH_SHORT).show();
                return;
            }

            String title = data.getStringExtra(MakeEditNoteActivity.EXTRA_TITLE);
            String note = data.getStringExtra(MakeEditNoteActivity.EXTRA_NOTE);
            long timeStamp = data.getLongExtra(MakeEditNoteActivity.EXTRA_TIMESTAMP, 10000);

            Note notedNote = new Note(title, note, timeStamp);
            notedNote.setId(id);
            noteViewModel.update(notedNote);

            Toast.makeText(this, "Note Updated", Toast.LENGTH_SHORT).show();

        } else if (requestCode == EDIT_NOTE_REQUEST) {
            Toast.makeText(this, "Note Unchanged", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.menu_act_main, menu);

        grid = menu.findItem(R.id.set_layout_grid);
        list = menu.findItem(R.id.set_layout_list);
        trash = menu.findItem(R.id.trash);

        // To initially set list/grid view
        if(isGrid){
            grid.setVisible(false);
            list.setVisible(true);
        }else{
            grid.setVisible(true);
            list.setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.set_layout_grid:
                setGrid();
                saveData(GRID_MODE);
                grid.setVisible(false);
                list.setVisible(true);
                return true;
            case R.id.set_layout_list:
                setList();
                saveData(LIST_MODE);
                grid.setVisible(true);
                list.setVisible(false);
                return true;
            case R.id.trash:
                Intent intent = new Intent(NotesActivity.this, TrashNotesActivity.class);
                startActivity(intent);

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    // To set to grid mode
    private void setGrid(){
        layoutManager = new StaggeredGridLayoutManager(2,StaggeredGridLayoutManager.VERTICAL);
        noteRecyclerView.setLayoutManager(layoutManager);
    }
    // To set to list mode
    private void setList(){
        layoutManager = new LinearLayoutManager(NotesActivity.this);
        noteRecyclerView.setLayoutManager(layoutManager);
    }
    // Save data to shared preference
    public void saveData(boolean data){
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFERENCE, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(SET_GRID, data);
        editor.apply();
    }
    // Load data from shared preference
    public void loadData(){
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFERENCE, MODE_PRIVATE);
        isGrid = sharedPreferences.getBoolean(SET_GRID, true);
    }

}
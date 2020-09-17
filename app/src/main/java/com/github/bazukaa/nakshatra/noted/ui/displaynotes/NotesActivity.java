package com.github.bazukaa.nakshatra.noted.ui.displaynotes;

import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.github.bazukaa.nakshatra.noted.R;
import com.github.bazukaa.nakshatra.noted.adapter.NoteAdapter;
import com.github.bazukaa.nakshatra.noted.db.entity.Note;
import com.github.bazukaa.nakshatra.noted.db.entity.TrashNote;
import com.github.bazukaa.nakshatra.noted.ui.displaytrashnotes.TrashNotesActivity;
import com.github.bazukaa.nakshatra.noted.ui.makeeditnote.MakeEditNoteActivity;
import com.github.bazukaa.nakshatra.noted.util.Constants;
import com.github.bazukaa.nakshatra.noted.util.PreferenceManager;
import com.github.bazukaa.nakshatra.noted.viewmodel.NoteViewModel;

import java.text.SimpleDateFormat;
import java.util.Date;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class NotesActivity extends AppCompatActivity {

    public static final int ADD_NOTE_REQUEST = 1;
    public static final int EDIT_NOTE_REQUEST = 2;

    private NoteViewModel noteViewModel;
    private PreferenceManager preferenceManager;
    private boolean isNotGrid;

    private RecyclerView.LayoutManager layoutManager;

    private MenuItem grid;
    private MenuItem list;
    private MenuItem trash;

    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.act_main_rv_notes)
    RecyclerView noteRecyclerView;
    @BindView(R.id.act_notes_et_search)
    EditText inputSearch;

    private AlertDialog dialogAddUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notes);
        ButterKnife.bind(this);
        setSupportActionBar(toolbar);
        preferenceManager = new PreferenceManager(this);

        NoteAdapter adapter = new NoteAdapter();
        noteRecyclerView.setAdapter(adapter);

        // Setting grid/list mode
        isNotGrid = preferenceManager.getBoolean(Constants.KEY_GRID_STATE);
        if(isNotGrid) setList();
        else setGrid();

        noteViewModel = ViewModelProviders.of(this).get(NoteViewModel.class);
        noteViewModel.getNotes().observe(this, notes -> adapter.setNotes(notes));

        //To delete a note
        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0,
                ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) { return false; }
            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                Note note = adapter.getNotePosition(viewHolder.getAdapterPosition());
                TrashNote trashNote = new TrashNote(note.getTitle(), note.getNote(), note.getTimeStamp());
                trashNote.setColor(note.getColor());
                trashNote.setWebLink(note.getWebLink());
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
            intent.putExtra(MakeEditNoteActivity.EXTRA_COLOR, note.getColor());
            intent.putExtra(MakeEditNoteActivity.EXTRA_WEB_LINK, note.getWebLink());

            //Formatting currentTimeMillis in desired form before sending to MakeEditNoteActivity
            long currentTimeMillis = note.getTimeStamp();
            SimpleDateFormat sdf = new SimpleDateFormat("hh:mm aaa MMM dd, yyyy");
            Date resultdate = new Date(currentTimeMillis);
            String timeStamp = "Last changed";
            timeStamp = timeStamp+" "+String.valueOf(sdf.format(resultdate));
            intent.putExtra(MakeEditNoteActivity.EXTRA_TIMESTAMP, timeStamp);

            startActivityForResult(intent, EDIT_NOTE_REQUEST);
        });

        inputSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                adapter.cancelTimer();
            }
            @Override
            public void afterTextChanged(Editable s) {
                adapter.searchNotes(s.toString());
            }
        });
    }
    //To create a new note
    @OnClick(R.id.act_notes_add_image)
    public void quickImageNote(){

    }
    @OnClick(R.id.act_main_fab_add)
    public void onFabClicked() {
        Intent intent = new Intent(NotesActivity.this, MakeEditNoteActivity.class);
        startActivityForResult(intent, ADD_NOTE_REQUEST);
    }
    @OnClick(R.id.act_notes_add_links)
    public void quickWebLinkNote(){
        if(dialogAddUrl == null){
            AlertDialog.Builder builder = new AlertDialog.Builder(NotesActivity.this);
            View view = LayoutInflater.from(this).inflate(
                    R.layout.layout_add_url,
                    findViewById(R.id.layout_add_url_cl)
            );
            builder.setView(view);

            dialogAddUrl = builder.create();
            if(dialogAddUrl.getWindow() != null)
                dialogAddUrl.getWindow().setBackgroundDrawable(new ColorDrawable(0));

            final EditText inputUrl = view.findViewById(R.id.layout_add_url_et_url);
            inputUrl.requestFocus();
            view.findViewById(R.id.layout_add_url_tv_add).setOnClickListener(v -> {
                if(inputUrl.getText().toString().trim().isEmpty())
                    Toast.makeText(NotesActivity.this, "Add Url", Toast.LENGTH_SHORT).show();
                else if (!Patterns.WEB_URL.matcher(inputUrl.getText().toString()).matches())
                    Toast.makeText(NotesActivity.this, "Enter valid Url", Toast.LENGTH_SHORT).show();
                else{
                    Intent intent = new Intent(NotesActivity.this, MakeEditNoteActivity.class);
                    intent.putExtra(MakeEditNoteActivity.EXTRA_WEB_LINK, inputUrl.getText().toString());
                    startActivityForResult(intent, ADD_NOTE_REQUEST);

                    inputUrl.setText(null);
                    dialogAddUrl.dismiss();
                }

            });

            view.findViewById(R.id.layout_add_url_tv_cancel).setOnClickListener(v -> {
                dialogAddUrl.dismiss();
            });
        }
        dialogAddUrl.show();
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.menu_act_main, menu);

        grid = menu.findItem(R.id.set_layout_grid);
        list = menu.findItem(R.id.set_layout_list);
        trash = menu.findItem(R.id.trash);

        // To initially set list/grid view
        if(isNotGrid){
            grid.setVisible(true);
            list.setVisible(false);
        }else{
            grid.setVisible(false);
            list.setVisible(true);
        }
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.set_layout_grid:
                setGrid();
                preferenceManager.putBoolean(Constants.KEY_GRID_STATE, Constants.LIST_MODE);
                grid.setVisible(false);
                list.setVisible(true);
                return true;
            case R.id.set_layout_list:
                setList();
                preferenceManager.putBoolean(Constants.KEY_GRID_STATE, Constants.GRID_MODE);
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
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == ADD_NOTE_REQUEST && resultCode == RESULT_OK) {
            String title = data.getStringExtra(MakeEditNoteActivity.EXTRA_TITLE);
            String note = data.getStringExtra(MakeEditNoteActivity.EXTRA_NOTE);
            long timeStamp = data.getLongExtra(MakeEditNoteActivity.EXTRA_TIMESTAMP, 10000);
            String color = data.getStringExtra(MakeEditNoteActivity.EXTRA_COLOR);
            String webLink = data.getStringExtra(MakeEditNoteActivity.EXTRA_WEB_LINK);

            Note notedNote = new Note(title, note, timeStamp);
            notedNote.setColor(color);
            notedNote.setWebLink(webLink);
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
            String color = data.getStringExtra(MakeEditNoteActivity.EXTRA_COLOR);
            String webLink = data.getStringExtra(MakeEditNoteActivity.EXTRA_WEB_LINK);

            Note notedNote = new Note(title, note, timeStamp);
            notedNote.setColor(color);
            notedNote.setWebLink(webLink);
            notedNote.setId(id);
            noteViewModel.update(notedNote);

            Toast.makeText(this, "Note Updated", Toast.LENGTH_SHORT).show();

        } else if (requestCode == EDIT_NOTE_REQUEST) {
            Toast.makeText(this, "Note Unchanged", Toast.LENGTH_SHORT).show();
        }
    }
}

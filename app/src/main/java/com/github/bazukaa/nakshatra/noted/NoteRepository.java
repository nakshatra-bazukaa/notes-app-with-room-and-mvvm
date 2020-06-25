package com.github.bazukaa.nakshatra.noted;

import android.app.Application;
import android.os.AsyncTask;

import androidx.lifecycle.LiveData;

import com.github.bazukaa.nakshatra.noted.db.NoteDatabase;
import com.github.bazukaa.nakshatra.noted.db.TrashNoteDatabase;
import com.github.bazukaa.nakshatra.noted.db.dao.NoteDao;
import com.github.bazukaa.nakshatra.noted.db.dao.TrashNoteDao;
import com.github.bazukaa.nakshatra.noted.db.entity.Note;
import com.github.bazukaa.nakshatra.noted.db.entity.TrashNote;

import java.util.List;

public class NoteRepository {
    private NoteDao noteDao;
    private LiveData<List<Note>> notes;

    private TrashNoteDao trashNoteDao;
    private LiveData<List<TrashNote>> trashNotes;

    public NoteRepository(Application application){
        NoteDatabase noteDatabase = NoteDatabase.getInstance(application);
        noteDao = noteDatabase.noteDao();
        notes = noteDao.getNotes();

        TrashNoteDatabase trashNoteDatabase = TrashNoteDatabase.getInstance(application);
        trashNoteDao = trashNoteDatabase.trashNoteDao();
        trashNotes = trashNoteDao.getTrashNotes();
    }

    public void insert(Note note) {
        new InsertNoteTask(noteDao).execute(note);
    }

    public void update(Note note) {
        new UpdateNoteTask(noteDao).execute(note);
    }

    public void delete(Note note) {
        new DeleteNoteTask(noteDao).execute(note);
    }

    public LiveData<List<Note>> getNotes() {
        return notes;
    }

    public void insert(TrashNote trashNote) {new InsertTrashNoteTask(trashNoteDao).execute(trashNote); }

    public void update(TrashNote trashNote){new UpdateTrashNoteTask(trashNoteDao).execute(trashNote);}

    public void delete(TrashNote trashNote){new DeleteTrashNoteTask(trashNoteDao).execute(trashNote); }

    public LiveData<List<TrashNote>> getTrashNotes() {return trashNotes;}

    private static class InsertNoteTask extends AsyncTask<Note, Void, Void> {
        private NoteDao noteDao;

        private InsertNoteTask(NoteDao noteDao) {
            this.noteDao = noteDao;
        }

        @Override
        protected Void doInBackground(Note... notes) {
            noteDao.insert(notes[0]);
            return null;
        }
    }

    private static class UpdateNoteTask extends AsyncTask<Note, Void, Void> {
        private NoteDao noteDao;

        private UpdateNoteTask(NoteDao noteDao) {
            this.noteDao = noteDao;
        }

        @Override
        protected Void doInBackground(Note... notes) {
            noteDao.update(notes[0]);
            return null;
        }
    }

    private static class DeleteNoteTask extends AsyncTask<Note, Void, Void> {
        private NoteDao noteDao;

        private DeleteNoteTask(NoteDao noteDao) {
            this.noteDao = noteDao;
        }

        @Override
        protected Void doInBackground(Note... notes) {
            noteDao.delete(notes[0]);
            return null;
        }
    }

    private static class InsertTrashNoteTask extends AsyncTask<TrashNote, Void, Void>{
        private TrashNoteDao trashNoteDao;

        private InsertTrashNoteTask(TrashNoteDao trashNoteDao){this.trashNoteDao = trashNoteDao; }

        @Override
        protected Void doInBackground(TrashNote... trashNotes) {
            trashNoteDao.insert(trashNotes[0]);
            return null;
        }
    }

    private static class UpdateTrashNoteTask extends AsyncTask<TrashNote, Void, Void>{
        private TrashNoteDao trashNoteDao;

        private UpdateTrashNoteTask(TrashNoteDao trashNoteDao){this.trashNoteDao = trashNoteDao; }

        @Override
        protected Void doInBackground(TrashNote... trashNotes) {
            trashNoteDao.update(trashNotes[0]);
            return null;
        }
    }

    private static class DeleteTrashNoteTask extends AsyncTask<TrashNote, Void, Void>{
        private TrashNoteDao trashNoteDao;

        private DeleteTrashNoteTask(TrashNoteDao trashNoteDao){this.trashNoteDao = trashNoteDao; }

        @Override
        protected Void doInBackground(TrashNote... trashNotes) {
            trashNoteDao.delete(trashNotes[0]);
            return null;
        }
    }
}

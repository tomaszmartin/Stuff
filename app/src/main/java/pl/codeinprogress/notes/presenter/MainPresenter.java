package pl.codeinprogress.notes.presenter;

import android.content.Intent;
import android.os.Environment;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;

import pl.codeinprogress.notes.R;
import pl.codeinprogress.notes.presenter.firebase.FirebaseActivity;
import pl.codeinprogress.notes.presenter.firebase.FirebaseLink;
import pl.codeinprogress.notes.model.Note;
import pl.codeinprogress.notes.secret.Secrets;
import pl.codeinprogress.notes.view.DetailsActivity;
import pl.codeinprogress.notes.view.views.MainView;
import pl.codeinprogress.notes.view.adapters.NotesAdapter;

/**
 * Created by tomaszmartin on 05.07.2016.
 */

public class MainPresenter {

    private MainView noteView;
    private FirebaseActivity activity;
    private FirebaseDatabase database;
    private StorageReference storage;

    public MainPresenter(MainView noteView, FirebaseActivity activity) {
        this.noteView = noteView;
        this.activity = activity;
        this.database = FirebaseDatabase.getInstance();
        this.storage = FirebaseStorage.getInstance().getReferenceFromUrl(Secrets.FIREBASE_STORAGE);
    }

    public void addNote() {
        DatabaseReference noteReference = database.getReference(FirebaseLink.forNotes()).push();
        String noteId = noteReference.getKey();
        Note note = new Note(noteId);
        noteReference.setValue(note);
        openNote(note);
    }

    public void openNote(Note note) {
        String noteId = note.getId();
        openNote(noteId);
    }

    public void openNote(String noteId) {
        Intent openNote = new Intent(activity, DetailsActivity.class);
        openNote.putExtra(DetailsActivity.NOTE_ID, noteId);
        activity.startActivity(openNote);
    }

    public void loadNotes() {
        NotesAdapter adapter = new NotesAdapter(activity, Note.class, R.layout.main_item, database.getReference(FirebaseLink.forNotes()));
        noteView.showNotes(adapter);
    }

    public void sortByTitle() {
        Query reference = database.getReference(FirebaseLink.forNotes()).orderByChild("title");
        NotesAdapter adapter = new NotesAdapter(activity, Note.class, R.layout.main_item, reference);
        noteView.showNotes(adapter);
    }

    public void sortByDate() {
        Query reference = database.getReference(FirebaseLink.forNotes()).orderByChild("lastModified");
        NotesAdapter adapter = new NotesAdapter(activity, Note.class, R.layout.main_item, reference);
        noteView.showNotes(adapter);
    }

    public void search(String query) {
        Query reference = activity.getDatabase().getReference(FirebaseLink.forNotes()).orderByChild("title").startAt(query);
        NotesAdapter adapter = new NotesAdapter(activity, Note.class, R.layout.main_item, reference);
        noteView.showNotes(adapter);
    }

    public void deleteNote(Note note) {
        deleteFromFile(note);
        deleteFromStorage(note);
        deleteFromDatabase(note);
    }

    private void deleteFromDatabase(Note note) {
        DatabaseReference noteReference = database.getReference(FirebaseLink.forNote(note));
        noteReference.removeValue();
    }

    private void deleteFromFile(final Note note) {
        Runnable task = new Runnable() {
            @Override
            public void run() {
                File file = new File(Environment.getExternalStorageDirectory() + File.separator + note.getFileName());
                file.delete();
            }
        };
        Thread thread = new Thread(task);
        thread.run();
    }

    private void deleteFromStorage(Note note) {
        StorageReference current = storage.child(note.getFileName());
        current.delete();
    }

}

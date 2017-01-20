package pl.codeinprogress.notes.presenter;

import java.util.ArrayList;
import java.util.Date;
import java.util.UUID;

import io.realm.Realm;
import pl.codeinprogress.notes.model.Note;

public class RealmNotesRepository implements NotesRepository {

    private Realm realm;

    public RealmNotesRepository() {
        realm = Realm.getDefaultInstance();
    }

    @Override
    public Note getNote(String noteId) {
        return realm.where(Note.class).equalTo("id", noteId).findFirst();
    }

    @Override
    public ArrayList<Note> getNotes() {
        return null;
    }

    @Override
    public void saveNote(Note note) {

    }

    @Override
    public void deleteNote(Note note) {


    }

    @Override
    public void addNote() {
        realm.executeTransaction(realm -> {
            Note note = realm.createObject(Note.class);
            long timestamp = new Date().getTime();
            note.setCreated(timestamp);
            note.setLastModified(timestamp);
            note.setId(UUID.randomUUID().toString());
        });
    }

    @Override
    public void searchNotes(String query) {

    }
}
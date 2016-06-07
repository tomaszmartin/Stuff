package pl.codeinprogress.notes.ui;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.text.Html;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import com.google.firebase.analytics.FirebaseAnalytics;

import butterknife.Bind;
import butterknife.ButterKnife;
import pl.codeinprogress.notes.firebase.FirebaseFragment;
import pl.codeinprogress.notes.tasks.AddNoteTask;
import pl.codeinprogress.notes.tasks.DeleteNoteTask;
import pl.codeinprogress.notes.adapters.NotesAdapter;
import pl.codeinprogress.notes.R;
import pl.codeinprogress.notes.data.NotesContract;
import pl.codeinprogress.notes.data.NotesContract.NoteEntry;

import java.util.ArrayList;
import java.util.Collections;

/**
 * Created by tomaszmartin on 24.03.15.
 */

public class MainFragment extends FirebaseFragment
        implements android.support.v4.app.LoaderManager.LoaderCallbacks<Cursor>,
        AbsListView.MultiChoiceModeListener, View.OnClickListener {

    static final String ORDER_KEY = "ORDER";
    static final int SORT_NEWEST = 1;
    static final int SORT_TITLE = 2;
    private static final String[] NOTES_COLUMNS = NoteEntry.NOTE_COLUMNS;
    private static final int NOTES_LOADER = 0;
    private NotesAdapter adapter;
    private View rootView;
    private ArrayList<Long> selectedPositions = new ArrayList<>();
    private int numberOfItemsSelected;
    @Bind(R.id.search_results_label) TextView searchResults;
    @Bind(R.id.search) View searchBar;
    @Bind(R.id.listView) ListView listView;
    @Bind(R.id.empty_list) View emptyView;
    @Bind(R.id.clear_button) ImageButton clearSearchResults;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.main_fragment, container, false);
        ButterKnife.bind(this, rootView);

        setupData();

        FloatingActionButton fab = (FloatingActionButton) getActivity().findViewById(R.id.fab);
        fab.setOnClickListener(this);
        clearSearchResults.setOnClickListener(this);

        return rootView;
    }

    private void setupData() {
        adapter = new NotesAdapter(getActivity(), null, 0);

        listView.setAdapter(adapter);
        listView.setEmptyView(emptyView);
        listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
        listView.setMultiChoiceModeListener(this);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                Cursor cursor = adapter.getCursor();
                if (cursor != null && cursor.moveToPosition(position)) {
                    int noteId = cursor.getInt(cursor.getColumnIndex(NoteEntry.COLUMN_ID));
                    ((OnSelectListener) getActivity()).onItemSelected(noteId);
                }
            }
        });
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        getLoaderManager().initLoader(NOTES_LOADER, null, this);
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    void deleteNote(int position) {
        Cursor cursor = adapter.getCursor();
        if (cursor != null && cursor.moveToPosition(position)) {
            int noteId = cursor.getInt(cursor.getColumnIndex(NoteEntry.COLUMN_ID));
            DeleteNoteTask deleteNoteTask = new DeleteNoteTask(getActivity());
            deleteNoteTask.execute(noteId);
        }
    }

    void showSnackbar(String text, String action) {
        Snackbar.make(getActivity().findViewById(R.id.coordinator), text, Snackbar.LENGTH_LONG)
                .setActionTextColor(getResources().getColor(R.color.primary))
                .show();
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {

        String sortOrder = NoteEntry.COLUMN_DATE_CREATED + " ASC";
        if (getArguments() != null && getArguments().getInt(ORDER_KEY, -1) != -1) {
            int sort = getArguments().getInt(ORDER_KEY, -1);
            if (sort == SORT_NEWEST) {
                sortOrder = NoteEntry.COLUMN_DATE_CREATED + " DESC";
            } else if (sort == SORT_TITLE) {
                sortOrder = NoteEntry.COLUMN_TITLE + " ASC";
            }
        }

        Uri notesUri;
        if (getArguments() != null && getArguments().getString(NoteEntry.COLUMN_TITLE) != null) {
            String query = getArguments().getString(NoteEntry.COLUMN_TITLE, "");
            notesUri = NoteEntry.buildQueryUri(query);
            setupSearchResultsView(query);
        } else {
            notesUri = NotesContract.NoteEntry.buildAllNotesUri();
        }

        return new CursorLoader(getActivity(),
                notesUri,
                NOTES_COLUMNS,
                null,
                null,
                sortOrder);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        adapter.swapCursor(cursor);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        adapter.swapCursor(null);
    }

    @Override
    public void onItemCheckedStateChanged(ActionMode mode, int position, long id, boolean checked) {
        if (checked) {
            selectedPositions.add(Long.valueOf(position));
            numberOfItemsSelected++;
        } else {
            selectedPositions.remove(Long.valueOf(position));
            numberOfItemsSelected--;
        }

        setActionModeTitle(mode, numberOfItemsSelected);
    }

    @Override
    public boolean onCreateActionMode(ActionMode mode, Menu menu) {
        getActivity().getMenuInflater().inflate(R.menu.context_menu, menu);
        return true;
    }

    @Override
    public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
        return false;
    }

    @Override
    public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
        if (item.getItemId() == R.id.action_delete) {

            ArrayList<Integer> positionsToDelete = new ArrayList<Integer>();
            for (Long position : selectedPositions) {
                positionsToDelete.add(position.intValue());
            }

            Collections.sort(positionsToDelete);

            for (Integer position : positionsToDelete) {
                deleteNote(position);
            }

            showSnackbar(getString(R.string.deleted_multiple), getString(R.string.undo));
        }

        mode.finish();
        return false;
    }

    @Override
    public void onDestroyActionMode(ActionMode mode) {
        selectedPositions = new ArrayList<>();
        numberOfItemsSelected = 0;
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        if (id == R.id.fab) {
            addNote();
        } else if (id == R.id.clear_button) {
            Intent intent = new Intent(getActivity(), MainActivity.class);
            intent.setAction(Intent.ACTION_REBOOT);
            startActivity(intent);
        }
    }

    void addNote() {
        AddNoteTask task = new AddNoteTask(getActivity());
        task.execute(null, null, null);
    }

    private void setupSearchResultsView(String query) {
        searchBar.setVisibility(View.VISIBLE);
        searchResults.setText(String.format("%s %s", getString(R.string.search_results_label), query));
    }

    void selectAllNotes() {
        for (int i = 0; i < listView.getChildCount(); i++) {
            listView.setItemChecked(i, true);
        }
    }

    private void setActionModeTitle(ActionMode mode, int numberOfItemsSelected) {
        if (numberOfItemsSelected == 0) {
            mode.setTitle("");
        } else if (numberOfItemsSelected == 1) {
            mode.setTitle(Html.fromHtml(String.valueOf("<small>" + numberOfItemsSelected) + " " + getString(R.string.one_note_chosen) + "</small>"));
        } else if (numberOfItemsSelected == 2 || numberOfItemsSelected == 3 || numberOfItemsSelected == 4) {
            mode.setTitle(Html.fromHtml(String.valueOf("<small>" + numberOfItemsSelected) + " " + getString(R.string.two_note_chosen) + "</small>"));
        } else {
            mode.setTitle(Html.fromHtml(String.valueOf("<small>" + numberOfItemsSelected) + " " + getString(R.string.five_note_chosen) + "</small>"));
        }
    }

}
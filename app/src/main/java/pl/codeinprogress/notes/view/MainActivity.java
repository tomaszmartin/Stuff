package pl.codeinprogress.notes.view;import android.content.Intent;import android.databinding.DataBindingUtil;import android.os.Bundle;import android.support.v4.view.GravityCompat;import android.support.v7.widget.SearchView;import android.view.Menu;import android.view.MenuItem;import android.view.View;import android.widget.AbsListView;import pl.codeinprogress.notes.R;import pl.codeinprogress.notes.databinding.ActivityMainBinding;import pl.codeinprogress.notes.model.Note;import pl.codeinprogress.notes.model.data.firebase.FirebaseActivity;import pl.codeinprogress.notes.presenter.MainPresenter;import pl.codeinprogress.notes.view.views.MainView;import pl.codeinprogress.notes.view.adapters.NotesAdapter;import pl.codeinprogress.notes.view.listeners.NavigationListener;import pl.codeinprogress.notes.view.listeners.NotesListener;import pl.codeinprogress.notes.view.listeners.OnAddListener;import pl.codeinprogress.notes.view.listeners.OnSelectListener;public class MainActivity extends FirebaseActivity implements OnSelectListener, OnAddListener, MainView {    private MainPresenter presenter;    private ActivityMainBinding binding;    @Override    protected void onCreate(Bundle savedInstanceState) {        super.onCreate(savedInstanceState);        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);        setupView();    }    @Override    protected void onResume() {        super.onResume();        setupData();    }    @Override    protected void onNewIntent(Intent intent) {        if (intent.getAction() != null && intent.getAction().equals(Intent.ACTION_VIEW)) {            String noteId = intent.getStringExtra(DetailsActivity.NOTE_ID);            presenter.openNote(noteId);        } else if (intent.getAction() != null && intent.getAction().equals(Intent.ACTION_REBOOT)) {        }    }    @Override    public boolean onCreateOptionsMenu(Menu menu) {        getMenuInflater().inflate(R.menu.menu_main, menu);        final MenuItem searchItem = menu.findItem(R.id.action_search);        final SearchView searchView = (SearchView) searchItem.getActionView();        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {            @Override            public boolean onQueryTextSubmit(String query) {                return true;            }            @Override            public boolean onQueryTextChange(String query) {                presenter.search(query);                return true;            }        });        return true;    }    @Override    public boolean onOptionsItemSelected(MenuItem item) {        int id = item.getItemId();        if (id == R.id.action_sort_title) {            presenter.sortByTitle();            return true;        } else if (id == R.id.action_sort_newest) {            presenter.sortByDate();            return true;        } else if (id == android.R.id.home) {            binding.drawerLayout.openDrawer(GravityCompat.START);        } else if (id == R.id.action_select_all) {            for (int i = 0; i < binding.listView.getChildCount(); i++) {                binding.listView.setItemChecked(i, true);            }        } else if (id == R.id.action_night_mode) {            switchNightMode();        }        return super.onOptionsItemSelected(item);    }    @Override    public void onItemSelected(Note note) {        presenter.openNote(note);    }    @Override    public void onItemAdded(Note note) {        presenter.openNote(note);    }    @Override    public void authenticate() {        Intent loginIntent = new Intent(this, LoginActivity.class);        startActivity(loginIntent);    }    @Override    public void notesLoaded(NotesAdapter adapter) {        NotesListener notesListener = new NotesListener(this, adapter);        binding.listView.setMultiChoiceModeListener(notesListener);        binding.listView.setOnItemClickListener(notesListener);        binding.listView.setAdapter(adapter);        binding.listView.setEmptyView(binding.emptyList);        binding.listView.setChoiceMode(AbsListView.CHOICE_MODE_MULTIPLE_MODAL);    }    private void setupView() {        setSupportActionBar(binding.toolbar);        if (getSupportActionBar() != null) {            getSupportActionBar().setDisplayHomeAsUpEnabled(true);            binding.toolbar.setNavigationIcon(R.drawable.ic_menu_white_24dp);        }    }    private void setupData() {        NavigationListener navigationListener = new NavigationListener(this, binding.drawerLayout);        binding.navigationView.setNavigationItemSelectedListener(navigationListener);        binding.clearSearchutton.setOnClickListener(null);        binding.fab.setOnClickListener(new View.OnClickListener() {            @Override            public void onClick(View v) {                presenter.addNote();            }        });        presenter = new MainPresenter(this, this);        presenter.loadNotes();    }}
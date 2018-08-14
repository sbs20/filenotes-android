package sbs20.filenotes;

import android.app.SearchManager;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import sbs20.filenotes.adapters.NoteArrayAdapter;
import sbs20.filenotes.model.Logger;
import sbs20.filenotes.model.Note;
import sbs20.filenotes.model.NoteCollection;
import sbs20.filenotes.model.NotesManager;

public class SearchResultsActivity extends ThemedActivity {

    private NotesManager notesManager;

    private ListView searchListView;
    private NoteArrayAdapter notesAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_search_results);

        setupActionBar();

        // Set up our main objects
        this.notesManager = ServiceManager.getInstance().getNotesManager();
        this.searchListView = this.findViewById(R.id.search_list);

        this.notesAdapter = new NoteArrayAdapter(this);
        this.searchListView.setAdapter(this.notesAdapter);
        this.searchListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Note note = (Note) view.getTag();
                SearchResultsActivity.this.edit(note);
            }
        });

        handleIntent(getIntent());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        handleIntent(intent);
    }

    private void setupActionBar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            // Show the Up button in the action bar.
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setElevation(0);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home: {
                Intent intent = new Intent(this, MainActivity.class);
                this.startActivity(intent);
                return true;
            }
        }

        return super.onOptionsItemSelected(item);
    }

    private void handleIntent(Intent intent) {

        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String query = intent.getStringExtra(SearchManager.QUERY);
            showResults(query);
        }
    }

    private void showResults(String query) {
        Logger.debug(this, query);

        NoteCollection notes = this.notesManager.search(query);

        TextView message = this.findViewById(R.id.search_message);

        message.setText(
                String.format(
                        "Searched: %s (%s results)",
                        query,
                        notes.size()));

        // refresh the ui
        if (this.notesAdapter != null) {
            this.notesAdapter.updateItems(notes);
        }
    }

    public void edit(Note note) {
        this.notesManager.editNote(note);
        Intent intent = new Intent(this, EditActivity.class);
        this.startActivity(intent);
    }
}

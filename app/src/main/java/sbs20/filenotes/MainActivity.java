package sbs20.filenotes;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.SparseBooleanArray;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import sbs20.filenotes.adapters.NoteArrayAdapter;
import sbs20.filenotes.model.Logger;
import sbs20.filenotes.model.Note;
import sbs20.filenotes.model.NoteCollection;
import sbs20.filenotes.model.NotesManager;
import sbs20.filenotes.replication.Action;
import sbs20.filenotes.replication.Replicator;
import sbs20.filenotes.replication.ReplicatorTask;

public class MainActivity extends ThemedActivity {

    private NotesManager notesManager;

    private SwipeRefreshLayout swipeLayout;
	private ListView noteListView;
    private NoteArrayAdapter notesAdapter;

    private void loadNotes() {
		TextView message = (TextView) this.findViewById(R.id.note_list_message);

		try {
            this.notesManager.readAllFromStorage();
		}
		catch (Exception ex) {
			message.setText(R.string.error_storage_does_not_exist);
			message.setVisibility(View.VISIBLE);
		}

        NoteCollection notes = this.notesManager.getNotes();
        if (notes.size() == 0) {
			message.setText(R.string.storage_is_empty);
			message.setVisibility(View.VISIBLE);
		} else {
			message.setText("");
			message.setVisibility(View.GONE);
		}

        // refresh the ui
        if (this.notesAdapter != null) {
            this.notesAdapter.updateItems(notes);
        }
    }

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_main);

        // Set up our main objects
        this.notesManager = ServiceManager.getInstance().getNotesManager();
        this.noteListView = (ListView)this.findViewById(R.id.note_list);
        this.swipeLayout = (SwipeRefreshLayout) findViewById(R.id.note_swiper);

        // Toolbar
		getSupportActionBar().setDisplayHomeAsUpEnabled(false);
		getSupportActionBar().setHomeButtonEnabled(false);

        // Note list
        this.notesAdapter = new NoteArrayAdapter(this);
		this.noteListView.setAdapter(this.notesAdapter);
		this.noteListView.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Note note = (Note) view.getTag();
                MainActivity.this.edit(note);
            }
        });

        this.noteListView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
        this.noteListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                noteListView.setItemChecked(position, true);
                return true;
            }
        });

        this.noteListView.setMultiChoiceModeListener(new ListView.MultiChoiceModeListener() {
            @Override
            public void onItemCheckedStateChanged(ActionMode mode, int position, long id, boolean checked) {
            }

            @Override
            public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                getMenuInflater().inflate(R.menu.select_notes, menu);
                return true;
            }

            @Override
            public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                return true;
            }

            @Override
            public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.select_notes_delete:
                        // delete here.....
                        SparseBooleanArray checked = noteListView.getCheckedItemPositions();
                        List<Note> toBeDeleted = new ArrayList<>();
                        for (int i = 0; i < checked.size(); i++) {
                            if(checked.valueAt(i)) {
                                Note note = (Note) noteListView.getItemAtPosition(checked.keyAt(i));
                                toBeDeleted.add(note);
                            }
                        }

                        for (Note note : toBeDeleted) {
                            notesManager.deleteNote(note);
                        }
                        mode.finish();
                        startReplication();
                        break;
                    default:
                        break;
                }
                return true;
            }

            @Override
            public void onDestroyActionMode(ActionMode mode) {
            }
        });

        // load the notes ... to be honest, this call is sort of pointless because
        // we're about to try and sync and then do it again.... but it gives the
        // user the illusion of progress.
        this.loadNotes();

        // Select a note if applicable
        Note selected = this.notesManager.getSelectedNote();
        if (selected != null) {
            int index = this.notesManager.getNotes().indexOf(selected);
            this.noteListView.setSelection(index);
        }

        // Swipe handling
		swipeLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                noteListView.setEnabled(false);
                startReplication();
            }
        });

		FloatingActionButton createNew = (FloatingActionButton)this.findViewById(R.id.note_create);
		createNew.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity.this.createNew();
            }
        });

        if (Replicator.getInstance().shouldRun()) {
            this.startReplication();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
	public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                Intent intent = new Intent(this, SettingsActivity.class);
                this.startActivity(intent);
                return true;
        }

        return super.onOptionsItemSelected(item);
	}

    public void createNew() {
        Note note = this.notesManager.createNote();
        this.edit(note);
    }

    public void edit(Note note) {
        this.notesManager.editNote(note);
        Intent intent = new Intent(this, EditActivity.class);
        this.startActivity(intent);
    }

    private void startReplication() {

        final ProgressDialog progressDialog = new ProgressDialog(MainActivity.this);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.setCancelable(true);
        progressDialog.setTitle(getString(R.string.replication_replicating));
        progressDialog.setMessage(getString(R.string.replication_analysing));

        // Create a cancel button
        // Put a cancel button in progress dialog
        progressDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel", new DialogInterface.OnClickListener(){
            // Set a click listener for progress dialog cancel button
            @Override
            public void onClick(DialogInterface dialog, int which){
                Logger.info(this, "onClick()");
                Replicator.getInstance().cancel();

                // It would be nice to wait, but it makes android nervous. Comment until solution found
                // Replicator.getInstance().awaitStop();

                // dismiss the progress dialog
                progressDialog.dismiss();
            }
        });

        // Handles the back button - required by Google design guidelines
        progressDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                Logger.info(this, "onCancel()");
                Replicator.getInstance().cancel();
            }
        });

        // Have a guess at the max for now... we'll correct later
        progressDialog.setMax(this.noteListView.getCount());
        progressDialog.show();

        // Fix the size too so it doesn't bounce around
        int width = WindowManager.LayoutParams.MATCH_PARENT;
        int height = WindowManager.LayoutParams.WRAP_CONTENT;
        progressDialog.getWindow().setLayout(width, height);

        new ReplicatorTask() {
            @Override
            protected void onProgressUpdate(Action action) {
                loadNotes();
                progressDialog.setMax(this.replicator.getActionCount());
                progressDialog.setMessage(action.message());
                progressDialog.incrementProgressBy(1);
            }

            @Override
            protected void onPostExecute() {
                progressDialog.setProgress(progressDialog.getMax());
                loadNotes();
                swipeLayout.setRefreshing(false);
                noteListView.setEnabled(true);
                progressDialog.dismiss();
            }
        }.execute();
    }
}

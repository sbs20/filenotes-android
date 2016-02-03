package sbs20.filenotes;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.util.SparseBooleanArray;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import sbs20.filenotes.adapters.NoteArrayAdapter;
import sbs20.filenotes.model.Logger;
import sbs20.filenotes.model.Note;
import sbs20.filenotes.model.NoteCollection;
import sbs20.filenotes.model.NotesManager;
import sbs20.filenotes.storage.Replicator;
import sbs20.filenotes.storage.ReplicatorTask;

public class MainActivity extends ThemedActivity {

    private NotesManager notesManager;

    private ListView drawerList;
	private DrawerLayout drawerLayout;
    private SwipeRefreshLayout swipeLayout;
	private ListView noteListView;

    private ArrayAdapter<String> drawerAdapter;
    private ActionBarDrawerToggle drawerToggle;
    private NoteArrayAdapter notesAdapter;

    private void loadNotes() {
		TextView message = (TextView) this.findViewById(R.id.note_list_message);
        NoteCollection notes = this.notesManager.getNotes();

		try {
            this.notesManager.readAllFromStorage();
		}
		catch (Exception ex) {
			message.setText(R.string.error_storage_does_not_exist);
			message.setVisibility(View.VISIBLE);
		}

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

	private void addDrawerItems() {
		final String[] drawerItems = {
				getString(R.string.title_activity_settings) ,
				getString(R.string.title_activity_about)
		};

		this.drawerAdapter = new ArrayAdapter<String>(this,
				R.layout.listview_drawer,
				drawerItems);

		this.drawerList.setAdapter(this.drawerAdapter);

		final MainActivity activity = this;

		this.drawerList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String selection = drawerItems[position];
                if (selection.compareTo(getString(R.string.title_activity_settings)) == 0) {
                    Intent intent = new Intent(activity, SettingsActivity.class);
                    startActivity(intent);
                } else if (selection.compareTo(getString(R.string.title_activity_about)) == 0) {
                    Intent intent = new Intent(activity, AboutActivity.class);
                    startActivity(intent);
                }
            }
        });
	}

	private void setupDrawer() {
        final String activityTitle = getTitle().toString();

        this.drawerToggle = new ActionBarDrawerToggle(this,
				this.drawerLayout,
				R.string.drawer_open,
				R.string.drawer_close) {

			/** Called when a drawer has settled in a completely open state. */
			public void onDrawerOpened(View drawerView) {
				super.onDrawerOpened(drawerView);
				getSupportActionBar().setTitle(activityTitle);
				invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
			}

			/** Called when a drawer has settled in a completely closed state. */
			public void onDrawerClosed(View view) {
				super.onDrawerClosed(view);
				getSupportActionBar().setTitle(activityTitle);
				invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
			}
		};

		this.drawerToggle.setDrawerIndicatorEnabled(true);
		this.drawerLayout.setDrawerListener(this.drawerToggle);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_main);

        // Set up our main objects
        this.notesManager = ServiceManager.getInstance().getNotesManager();
        this.drawerList = (ListView)findViewById(R.id.drawer_list);
        this.drawerLayout = (DrawerLayout)findViewById(R.id.drawer_layout);
        this.noteListView = (ListView)this.findViewById(R.id.note_list);
        this.swipeLayout = (SwipeRefreshLayout) findViewById(R.id.note_swiper);

        // Setup the drawer
		this.addDrawerItems();
		this.setupDrawer();

        // Toolbar
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setHomeButtonEnabled(true);

        // Note list
		final MainActivity activity = this;
        this.notesAdapter = new NoteArrayAdapter(this);
		this.noteListView.setAdapter(this.notesAdapter);
		this.noteListView.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Note note = (Note) view.getTag();
                activity.edit(note);
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
                            if(checked.valueAt(i) == true) {
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
                new ReplicatorTask() {
                    @Override
                    protected void onProgressUpdate(Replicator.Event... events) {
                        loadNotes();
                        Toast.makeText(getApplicationContext(),
                                events[0].message(),
                                Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    protected void onPostExecute(Replicator replicator) {
                        finishReplication(replicator);
                    }
                }.execute(new Replicator());
            }
        });

		FloatingActionButton createNew = (FloatingActionButton)this.findViewById(R.id.note_create);
		createNew.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                activity.createNew();
            }
        });

        if (this.notesManager.isReplicationRequired()) {
            this.startReplication();
        }
    }

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		// Sync the toggle state after onRestoreInstanceState has occurred.
		this.drawerToggle.syncState();
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		this.drawerToggle.onConfigurationChanged(newConfig);
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

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (this.drawerToggle.onOptionsItemSelected(item)) {
			return true;
		}

		return super.onOptionsItemSelected(item);
	}

    private void finishReplication(Replicator replicator) {
        this.loadNotes();
        this.swipeLayout.setRefreshing(false);
        this.noteListView.setEnabled(true);

        // Post any toasty messages here too
//        int updates = replicator.getUpdateCount();
//        if (updates > 0) {
//            ServiceManager.getInstance().toast(getString(R.string.replication_notes_updated) + ": " + updates);
//        }
    }

    private void startReplication() {

        swipeLayout.post(new Runnable() {
            @Override
            public void run() {

                // We have to put this in a runnable because of this....
                // See: http://stackoverflow.com/a/26910973/1229065
                swipeLayout.setRefreshing(true);

                // And the rest of it has to stay here otherwise onPostExecute
                // might finish before we've started refreshing and leave
                // a busy cursor
                new ReplicatorTask() {
                    @Override
                    protected void onProgressUpdate(Replicator.Event... events) {
                        loadNotes();
                        Toast.makeText(getApplicationContext(),
                                events[0].message(),
                                Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    protected void onPostExecute(Replicator replicator) {
                        finishReplication(replicator);
                    }
                }.execute(new Replicator());
            }
        });
    }
}

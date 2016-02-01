package sbs20.filenotes;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import sbs20.filenotes.adapters.NoteArrayAdapter;
import sbs20.filenotes.model.Note;
import sbs20.filenotes.model.NoteCollection;
import sbs20.filenotes.storage.Syncotron;
import sbs20.filenotes.storage.SyncotronicTask;

public class MainActivity extends ThemedActivity {

    private ListView drawerList;
	private DrawerLayout drawerLayout;
    private SwipeRefreshLayout swipeLayout;
	private ListView noteListView;

    private ArrayAdapter<String> drawerAdapter;
    private ActionBarDrawerToggle drawerToggle;
    private NoteArrayAdapter notesAdapter;

    private void loadNotes() {
		TextView message = (TextView) this.findViewById(R.id.note_list_message);
        NoteCollection notes = ServiceManager.getInstance().getNotesManager().getNotes();

		try {
            ServiceManager.getInstance().getNotesManager().readAllFromStorage();
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
                    Intent intent = new Intent(activity, SettingsPreferenceActivity.class);
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

        // load the notes ... to be honest, this call is sort of pointless because
        // we're about to try and sync and then do it again.... but it gives the
        // user the illusion of performance.
        this.loadNotes();

        // Select a note if applicable
        Note selected = ServiceManager.getInstance().getNotesManager().getSelectedNote();
        if (selected != null) {
            int index = ServiceManager.getInstance().getNotesManager().getNotes().indexOf(selected);
            this.noteListView.setSelection(index);
        }

        // Swipe handling
		swipeLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                noteListView.setEnabled(false);
                new SyncotronicTask() {
                    @Override
                    protected void onPostExecute(Syncotron syncotron) {
                        super.onPostExecute(syncotron);
                        finishSyncWithCloud(syncotron);
                    }
                }.execute(new Syncotron());
            }
        });

		FloatingActionButton createNew = (FloatingActionButton)this.findViewById(R.id.note_create);
		createNew.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				activity.createNew();
			}
		});

        this.startSyncWithCloud();
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
        Note note = ServiceManager.getInstance().getNotesManager().createNote();
		this.edit(note);
	}
	
	public void edit(Note note) {
        ServiceManager.getInstance().getNotesManager().setSelectedNote(note);
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

    private void finishSyncWithCloud(Syncotron syncotron) {
        this.loadNotes();
        this.swipeLayout.setRefreshing(false);
        this.noteListView.setEnabled(true);

        // Post any toasty messages here too
    }

    private void startSyncWithCloud() {
        // See: http://stackoverflow.com/a/26910973/1229065
        swipeLayout.post(new Runnable() {
            @Override
            public void run() {
                swipeLayout.setRefreshing(true);
            }
        });

        this.noteListView.setEnabled(false);

        new SyncotronicTask() {
            @Override
            protected void onPostExecute(Syncotron syncotron) {
                super.onPostExecute(syncotron);
                finishSyncWithCloud(syncotron);
            }
        }.execute(new Syncotron());
    }
}

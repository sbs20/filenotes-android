package sbs20.filenotes;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.AsyncTask;
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
import sbs20.filenotes.cloud.CloudSync;
import sbs20.filenotes.model.Note;
import sbs20.filenotes.model.NoteCollection;

public class MainActivity extends ThemedActivity {

	private ListView drawerList;
	private ArrayAdapter<String> drawerAdapter;
	private ActionBarDrawerToggle drawerToggle;
	private DrawerLayout drawerLayout;

	private ListView filelist;
    private NoteArrayAdapter notesAdapter;

	private void loadNotes() {
		TextView message = (TextView) this.findViewById(R.id.noteListMessage);
        NoteCollection notes = this.getNotesManager().getNotes();

		try {
			this.getNotesManager().readAllFromStorage();
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

        // Setup the drawer
		this.drawerList = (ListView)findViewById(R.id.navList);
		this.drawerLayout = (DrawerLayout)findViewById(R.id.drawer_layout);
		this.addDrawerItems();
		this.setupDrawer();

        // Toolbar
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setHomeButtonEnabled(true);

        // Note list
		final MainActivity activity = this;
        this.notesAdapter = new NoteArrayAdapter(this);
		this.filelist = (ListView)this.findViewById(R.id.noteList);
		this.filelist.setAdapter(this.notesAdapter);
		this.filelist.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				Note note = (Note) view.getTag();
				activity.edit(note);
			}
		});

        // load the notes
        this.loadNotes();

        // Select a note if applicable
        Note selected = this.getNotesManager().getSelectedNote();
        if (selected != null) {
            int index = this.getNotesManager().getNotes().indexOf(selected);
            this.filelist.setSelection(index);
        }

        // Swipe handling
		final SwipeRefreshLayout swipeLayout = (SwipeRefreshLayout) findViewById(R.id.noteListSwipeContainer);
		swipeLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {

                AsyncTask<CloudSync, Void, CloudSync> task = new AsyncTask<CloudSync, Void, CloudSync>() {
                    @Override
                    protected CloudSync doInBackground(CloudSync... params) {
                        params[0].trySync();
                        return params[0];
                    }

                    @Override
                    protected void onPostExecute(CloudSync cloudSync) {
                        super.onPostExecute(cloudSync);
                        activity.loadNotes();
                        swipeLayout.setRefreshing(false);
                    }
                };

                CloudSync cloudSync = activity.getFilenotesApplication().getCloudSync();
                task.execute(cloudSync);
            }
        });

		FloatingActionButton createNew = (FloatingActionButton)this.findViewById(R.id.createNew);
		createNew.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				activity.createNew();
			}
		});
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
        Note note = this.getNotesManager().createNote();
		this.edit(note);
	}
	
	public void edit(Note note) {
        this.getNotesManager().setSelectedNote(note);
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
}

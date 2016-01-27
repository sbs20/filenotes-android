package sbs20.filenotes;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class MainActivity extends ThemedActivity {

	private ListView drawerList;
	private ArrayAdapter<String> drawerAdapter;
	private ActionBarDrawerToggle drawerToggle;
	private DrawerLayout drawerLayout;
	private String activityTitle;

	private ListView filelist;
	private NoteCollection notes;

	private void initNotes() {
		TextView message = (TextView) this.findViewById(R.id.fileListMessage);

		try {
			this.notes = this.getFilenotesApplication()
					.getStorageManager()
					.readAllFromStorage();
		}
		catch (Exception ex) {
			message.setText(R.string.error_storage_does_not_exist);
			message.setVisibility(View.VISIBLE);
		}

		if (this.notes.size() == 0) {
			message.setText(R.string.storage_is_empty);
			message.setVisibility(View.VISIBLE);
		} else {
			message.setText("");
			message.setVisibility(View.GONE);
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

		this.drawerList = (ListView)findViewById(R.id.navList);
		this.drawerLayout = (DrawerLayout)findViewById(R.id.drawer_layout);
		this.activityTitle = getTitle().toString();

		this.addDrawerItems();
		this.setupDrawer();

		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setHomeButtonEnabled(true);

		this.initNotes();

		final MainActivity activity = this;

		NoteArrayAdapter adapter = new NoteArrayAdapter(this);
		adapter.updateItems(this.notes);

		this.filelist = (ListView)this.findViewById(R.id.fileList);
		this.filelist.setAdapter(adapter);
		this.filelist.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				Note note = (Note) view.getTag();
				activity.edit(note);
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
        Note note = this.notes.createNote();
		this.edit(note);
	}
	
	public void edit(Note note) {
		Current.setSelectedNote(note);
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

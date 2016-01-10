package sbs20.filenotes;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.Locale;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends ThemedActivity {

	private static final int MAX_FILE_SIZE = 32 * 1024;

	private ListView drawerList;
	private ArrayAdapter<String> drawerAdapter;
	private ActionBarDrawerToggle drawerToggle;
	private DrawerLayout drawerLayout;
	private String activityTitle;

	private ListView filelist;
	private File noteDirectory;
	private File[] notes;
	
	private void initNotes() {
		
		SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
		String directoryPath = sharedPref.getString(SettingsActivity.KEY_STORAGE_DIRECTORY, "");
		this.noteDirectory = new File(directoryPath);
		
		if (this.noteDirectory.exists()) {
			if (this.noteDirectory.isDirectory()) {
				this.notes = this.noteDirectory.listFiles(new FileFilter() {
					@Override
					public boolean accept(File pathname) {
						if (pathname.canRead() && pathname.isFile()) {
							Locale locale = Locale.getDefault();
							String filename = pathname.getName().toLowerCase(locale);
							if (filename.endsWith(".txt")) {
								return true;
							}
						}

						return false;
					}
				});
				
				Arrays.sort(this.notes, new Comparator<File>() {
					@Override
					public int compare(File lhs, File rhs) {
						Locale locale = Locale.getDefault();
						String l = lhs.getAbsolutePath().toLowerCase(locale);
						String r = rhs.getAbsolutePath().toLowerCase(locale);
						return l.compareTo(r);
					}
				});
			}
		}
		
		TextView message = (TextView) this.findViewById(R.id.fileListMessage);
		if (!this.noteDirectory.exists()) {
			message.setText("Directory " + this.noteDirectory.getAbsolutePath() + " does not exist");
			message.setVisibility(View.VISIBLE);
		} else if (!this.noteDirectory.canRead()) {
			message.setText("Unable to read " + this.noteDirectory.getAbsolutePath());
			message.setVisibility(View.VISIBLE);
		} else if (this.notes.length == 0) {
			message.setText("No matching files in " + this.noteDirectory.getAbsolutePath());
			message.setVisibility(View.VISIBLE);
		} else {
			message.setText("");
			message.setVisibility(View.GONE);
		}
		
		if (this.notes == null) {
			this.notes = new File[0];
		}
	}

	private void addDrawerItems() {
		final String[] drawerItems = { "Create new", "Settings", "About" };
		this.drawerAdapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_list_item_1,
				drawerItems);

		this.drawerList.setAdapter(this.drawerAdapter);

		final MainActivity activity = this;

		this.drawerList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

				switch (drawerItems[position]) {
					case "Create new":
						activity.createNew();
						break;

					case "Settings": {
						Intent intent = new Intent(activity, SettingsActivity.class);
						startActivity(intent);
						break;
					}

					case "About": {
						Intent intent = new Intent(activity, AboutActivity.class);
						startActivity(intent);
						break;
					}
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

		final MainActivity thisActivity = this;

		ArrayAdapter<File> adapter = new ArrayAdapter<File>(this, R.layout.listview_notes, this.notes) {
			private String getSize(File file) {
				long size = file.length();

				if (file.isDirectory())
					return "";

				if (size < 0)
					return "0";
				else if (size == 1)
					return "1 Byte";
				else if (size < 2048)
					return size + " Bytes";
				else if (size < 1024*1024*2)
					return ((int) (size/1024)) + " KB";
				else 
					return Math.round(100.0*size/(1024*1024))/100.0 + " MB";
			}
			
			private String getModificationDate(File file) {
				Date lastModified = new Date(file.lastModified());
				return lastModified.toString();
			}

			@Override
			public View getView(int position, View convertView, ViewGroup parent) {
				File file = this.getItem(position);
				View row = convertView;

				if(row == null)
				{
					LayoutInflater inflater = ((Activity)thisActivity).getLayoutInflater();
					row = inflater.inflate(R.layout.listview_notes, parent, false);
				}

				TextView filename = (TextView) row.findViewById(R.id.fileName);
				filename.setText(file.getName());

				TextView modificationDate = (TextView) row.findViewById(R.id.modificationDate);
				modificationDate.setText(this.getModificationDate(file));
				
				TextView fileSize = (TextView) row.findViewById(R.id.fileSize);
				fileSize.setText(this.getSize(file));
				
				row.setTag(file);

				return row;
			}
		};

		filelist = (ListView)findViewById(R.id.fileList);
		filelist.setAdapter(adapter);
		filelist.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				File file = (File) view.getTag();
				
				if (file.length() > MAX_FILE_SIZE) {
					String message =  file.getName() + " is larger than 32k. Note Monkey is not designed for large files";
					Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();					
				} else {
					thisActivity.edit(file);
				}
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
		
		// Is there a file already there? - delete it
		String filename = this.getString(R.string.newFileName);
		File file = new File(this.noteDirectory, "/" + filename);
		
		// The file may already exist with gash content. Just delete it
		if (file.exists()) {
			file.delete();
		}
		
		// Now recreate the file since we've more or less guaranteed it's not there
		try {
			file.createNewFile();
		} catch (IOException e) {
		}
		
		// Edit the file
		this.edit(file);
	}
	
	public void edit(File file) {
		Current.setFile(file);
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

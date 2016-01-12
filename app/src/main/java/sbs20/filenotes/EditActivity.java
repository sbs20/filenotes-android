package sbs20.filenotes;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

//import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.NavUtils;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

public class EditActivity extends ThemedActivity {

	private boolean isDirty;
	
	private enum Action {
		ACTION_SAVE,
		ACTION_CLOSE,
		ACTION_SAVEANDCLOSE		
	}
	
	private String readFileAsString(File file) {
		StringBuffer string = new StringBuffer();
		try {
			FileReader reader = new FileReader(file);
			char[] buffer = new char[1024];
			int read;
			while ((read = reader.read(buffer)) != -1) {
				string.append(buffer, 0, read);
			}
			reader.close();
		} catch (IOException e) {

		} finally {

		}

		return string.toString();
	}
	
	private Typeface getTypeface() {
		SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
		String fontFace = sharedPref.getString(SettingsActivity.KEY_FONTFACE, "monospace");

		if (fontFace.compareTo("monospace") == 0) {
			return Typeface.MONOSPACE;
		} else if (fontFace.compareTo("sansserif") == 0) {
			return Typeface.SANS_SERIF;
		} else if (fontFace.compareTo("serif") == 0) {
			return Typeface.SERIF;
		}

		return Typeface.MONOSPACE;
	}
	
	private float getTextSize() {
		SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
		int fontSize = Integer.parseInt(sharedPref.getString(SettingsActivity.KEY_FONTSIZE, "16"));
		return fontSize;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_edit);

		// Keep a note of this
		final EditActivity thisActivity = this;
		
		// Initialise isDirty
		this.isDirty = false;
		
		// Show the Up button in the action bar.
		setupActionBar();

		// Load the file
		File file = Current.getFile();
		EditText edit = (EditText) this.findViewById(R.id.note);
		edit.setText(this.readFileAsString(file));

		this.setTitle(file.getName());

		// Now listen for changes
		edit.addTextChangedListener(new TextWatcher() {
			public void onTextChanged(CharSequence one, int a, int b, int c) {
				thisActivity.isDirty = true;
				String temp = thisActivity.getTitle().toString();
				if (!temp.startsWith("* ")) {
					thisActivity.setTitle("* " + temp);
				}
			}

			// complete the interface
			public void afterTextChanged(Editable s) { }
			public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
		});

		edit.setTypeface(this.getTypeface());
		edit.setTextSize(this.getTextSize());
	}

	private void setupActionBar() {
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.edit, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			// This ID represents the Home or Up button. In the case of this
			// activity, the Up button is shown. Use NavUtils to allow users
			// to navigate up one level in the application structure. For
			// more details, see the Navigation pattern on Android Design:
			//
			// http://developer.android.com/design/patterns/navigation.html#up-vs-back
			//
			this.startClose();
			return true;

		case R.id.action_save:
			this.startSave(Action.ACTION_SAVE);
			return true;
			
		case R.id.action_delete:
			this.delete();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
	    if (keyCode == KeyEvent.KEYCODE_BACK) {
	    	this.startClose();
	        return true;
	    }
	    return super.onKeyDown(keyCode, event);
	}

	public void startClose() {

		final EditActivity thisActivity = this;

		if (this.isDirty) {
			// Handler.
			DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					switch (which){
					case DialogInterface.BUTTON_POSITIVE:
						//Yes button clicked
						thisActivity.startSave(Action.ACTION_SAVEANDCLOSE);
						break;

					case DialogInterface.BUTTON_NEGATIVE:
						//No button clicked
						thisActivity.finishClose();
						break;		

					case DialogInterface.BUTTON_NEUTRAL:
						break;
					}
				}
			};

			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setMessage("Do you want to save your changes?")
				.setPositiveButton("Yes", dialogClickListener)
				.setNegativeButton("No", dialogClickListener)
				.setNeutralButton("Cancel", dialogClickListener)
				.show();
		} else {
			// This is not dirty. Nothing to save. Just close
			this.finishClose();
		}
	}

	public void finishClose() {
		Current.setFile(null);
		NavUtils.navigateUpFromSameTask(this);
	}
	
	public boolean isNewFile(File file) {
		String newFilename = this.getResources().getString(R.string.newFileName);
		return file.getName().compareTo(newFilename) == 0;
	}

	public void startSave(final Action action) {
		File file = Current.getFile();
		
		if (this.isNewFile(file)) {
			final EditActivity thisActivity = this;
			final EditText editTextFilename = new EditText(this);
			editTextFilename.setText(file.getName() + ".txt");
			
			DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					switch (which){
					case DialogInterface.BUTTON_POSITIVE:
						// Yes button clicked
						SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(thisActivity);
						String directoryPath = sharedPref.getString(SettingsActivity.KEY_STORAGE_DIRECTORY, "");
						String filepath = directoryPath + "/" + editTextFilename.getText().toString();
						thisActivity.finishSave(filepath, action);
						break;

					case DialogInterface.BUTTON_NEUTRAL:
						// Cancel button clicked - don't do anything further
						break;
					}
				}
			};

			new AlertDialog.Builder(this)
				.setMessage("Do you want to save your changes?")
				.setView(editTextFilename)
				.setPositiveButton("Yes", dialogClickListener)
				.setNeutralButton("Cancel", dialogClickListener)
				.show();

		} else {
			String filepath = file.getAbsolutePath();
			this.finishSave(filepath, action);
		}
	}
	
	public void finishSave(String filepath, Action action) {
		try {
			// First off - let's just get this saved
			FileWriter fstream = new FileWriter(filepath);
			BufferedWriter out = new BufferedWriter(fstream);
			EditText editText = (EditText) this.findViewById(R.id.note);
			String content = editText.getText().toString();
			out.write(content);
			out.close();
			Toast.makeText(getApplicationContext(), "Saved", Toast.LENGTH_LONG).show();
			fstream.close();
			this.isDirty = false;
			Current.setFile(new File(filepath));
		} catch (IOException e) {
			Toast.makeText(getApplicationContext(), e.toString(), Toast.LENGTH_LONG).show();
		}
		
		if (action == Action.ACTION_SAVEANDCLOSE) {
			this.finishClose();
		}
	}
	
	public void delete() {
		File file = Current.getFile();
		if (file.exists()) {
			file.delete();
		}
		
		this.finishClose();
	}
}

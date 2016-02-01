package sbs20.filenotes;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import sbs20.filenotes.model.Logger;
import sbs20.filenotes.model.Note;

public class EditActivity extends ThemedActivity {

    private static final String UNSAVEDNOTE = "SELECTEDNOTE";
    private static final String UNSAVEDTEXT = "UNSAVEDTEXT";

    private Note note;
    private EditText noteText;

    private void updateNote() {
        EditText edit = (EditText) this.findViewById(R.id.note);
        this.note.setText(edit.getText().toString());
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_edit);

        // Keep a note of this
        final EditActivity activity = this;

        // Show the Up button in the action bar.
        setupActionBar();

        // Load the note
        this.note = ServiceManager.getInstance().getNotesManager().getSelectedNote();
        this.noteText = (EditText) this.findViewById(R.id.note);
        this.noteText.setText(this.note.getText());
        this.setTitle(this.note.getName());

        // Listen for changes so we can mark this as dirty
        this.noteText.addTextChangedListener(new TextWatcher() {
            public void onTextChanged(CharSequence one, int a, int b, int c) {
                activity.updateNote();
                if (activity.note.isDirty()) {
                    String title = activity.getTitle().toString();
                    if (!title.startsWith("* ")) {
                        activity.setTitle("* " + title);
                    }
                }
            }

            // complete the interface
            public void afterTextChanged(Editable s) {
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }
        });

        this.noteText.setTypeface(ServiceManager.getInstance().getSettings().getFontFace());
        this.noteText.setTextSize(ServiceManager.getInstance().getSettings().getFontSize());
        this.noteText.clearFocus();
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        // Hide the keyboard if on disk (if it's new you want to type!)
        if (ServiceManager.getInstance().getNotesManager().isStored(this.note)) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(this.noteText.getWindowToken(), 0);
            this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        }
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
                // to navigate up one level in the serviceManager structure. For
                // more details, see the Navigation pattern on Android Design:
                //
                // http://developer.android.com/design/patterns/navigation.html#up-vs-back
                this.startClose();
                return true;

            case R.id.action_save:
                this.save();
                return true;

            case R.id.action_delete:
                this.delete();
                return true;

            case R.id.action_rename:
                this.rename();
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

        this.updateNote();
        final EditActivity activity = this;

        if (this.note.isDirty()) {
            DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int result) {
                    switch (result) {
                        case DialogInterface.BUTTON_POSITIVE:
                            activity.save();
                            activity.finishClose();
                            break;

                        case DialogInterface.BUTTON_NEGATIVE:
                            activity.finishClose();
                            break;

                        case DialogInterface.BUTTON_NEUTRAL:
                            break;
                    }
                }
            };

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("Do you want to save your changes?")
                    .setPositiveButton(R.string.yes, dialogClickListener)
                    .setNegativeButton(R.string.no, dialogClickListener)
                    .setNeutralButton(android.R.string.cancel, dialogClickListener)
                    .show();
        } else {
            // This is not dirty. Nothing to save. Just close
            this.finishClose();
        }
    }

    public void finishClose() {
        // Do not clear the current note yet - we need to know about it on the MainActivity
        NavUtils.navigateUpFromSameTask(this);
    }

    public void rename() {
        final EditActivity activity = this;
        final EditText renameEditText = new EditText(this);

        renameEditText.setText(this.note.getName());

        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int result) {
                switch (result) {
                    case DialogInterface.BUTTON_POSITIVE:
                        // If nothing has changed...
                        if (renameEditText.getText().toString().equals(activity.note.getName())) {
                            Logger.verbose(activity, "File renamed to same name");

                            // don't do anything
                            return;
                        }

                        boolean succeeded = ServiceManager.getInstance().getNotesManager()
                                .renameNote(activity.note, renameEditText.getText().toString());

                        if (succeeded) {
                            activity.setTitle(activity.note.getName());
                        } else {
                            ServiceManager.getInstance().toast(getString(R.string.rename_failed));
                        }
                        break;

                    case DialogInterface.BUTTON_NEUTRAL:
                        // Cancel button clicked - don't do anything further
                        break;
                }
            }
        };

        new AlertDialog.Builder(this)
                .setMessage(R.string.action_rename)
                .setView(renameEditText)
                .setPositiveButton(R.string.yes, dialogClickListener)
                .setNeutralButton(R.string.no, dialogClickListener)
                .show();
    }

    public void save() {
        String content = this.noteText.getText().toString();
        this.note.setText(content);
        ServiceManager.getInstance().getNotesManager().writeToStorage(this.note);
        this.setTitle(this.note.getName());
    }

    public void delete() {
        ServiceManager.getInstance().getNotesManager().deleteNote(this.note);
        this.finishClose();
    }

    @Override
    protected void onSaveInstanceState(Bundle savedInstanceState) {
        this.updateNote();
        Logger.verbose(this, "onSaveInstanceState");
        if (this.note.isDirty()) {
            Logger.verbose(this, "onSaveInstanceState.savingNote");

            savedInstanceState.putString(UNSAVEDNOTE, this.note.getName());
            savedInstanceState.putCharSequence(UNSAVEDTEXT, this.note.getText());
        }

        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        // Always call the superclass so it can restore the view hierarchy
        super.onRestoreInstanceState(savedInstanceState);

        Logger.verbose(this, "onRestoreInstanceState");

        // Restore state members from saved instance
        if (this.note == null) {

            Logger.verbose(this, "onRestoreInstanceState.restoreNote");

            // Get the current note name
            String name = savedInstanceState.getString(UNSAVEDNOTE);

            // Reload all notes
            ServiceManager.getInstance().getNotesManager()
                    .readAllFromStorage();

            // Now get that note
            this.note = ServiceManager.getInstance().getNotesManager()
                    .getNotes()
                    .getByName(name);

            // Finally update the text field
            EditText edit = (EditText) this.findViewById(R.id.note);
            edit.setText(savedInstanceState.getCharSequence(UNSAVEDTEXT));
        }
    }
}

package com.kansus.ksnes.activity;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.kansus.ksnes.KSNESApplication;
import com.kansus.ksnes.R;
import com.kansus.ksnes.abstractemulator.cheats.Cheat;
import com.kansus.ksnes.abstractemulator.cheats.CheatsModule;
import com.kansus.ksnes.abstractemulator.Emulator;

import java.util.List;

import javax.inject.Inject;

/**
 * Activity where the users can add/enable and remove/disable cheats
 * for the currently running ROM.
 * <p>
 * Created by Charles Nascimento on 29/07/2017.
 */
public class CheatsActivity extends ListActivity {

    //region Constants

    private static final int MENU_ITEM_EDIT = Menu.FIRST;
    private static final int MENU_ITEM_DELETE = Menu.FIRST + 1;

    private static final int DIALOG_EDIT_CHEAT = 1;

    //endregion

    //region Variables

    @Inject
    Emulator mEmulator;

    private CheatsModule mCheatsModule;

    private ArrayAdapter<Cheat> mCheatsAdapter;

    private static Cheat mCurrentCheat;

    //endregion

    //region Activity Lifecycle

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(R.string.cheats);

        setContentView(R.layout.cheats);
        getListView().setEmptyView(findViewById(R.id.empty));

        ((KSNESApplication) getApplication()).getEmulatorComponent().inject(this);
        mCheatsModule = mEmulator.getCheatsModule();

        mCheatsAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_list_item_multiple_choice,
                mCheatsModule.getAll()
        );
        setListAdapter(mCheatsAdapter);

        final ListView listView = getListView();
        listView.setOnCreateContextMenuListener(this);
        listView.setItemsCanFocus(false);
        listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);

        syncCheckedStates();
    }

    @Override
    protected void onStop() {
        super.onStop();

        mCheatsModule.save();
    }

    //endregion

    //region Callbacks

    @SuppressLint("InflateParams")
    @Override
    protected Dialog onCreateDialog(int id) {
        switch (id) {
            case DIALOG_EDIT_CHEAT:
                DialogInterface.OnClickListener l = new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        final Dialog d = (Dialog) dialog;
                        String name = ((TextView) d.findViewById(
                                R.id.cheat_name)).getText().toString();
                        if (mCurrentCheat != null) {
                            mCurrentCheat.setName(name);
                            mCurrentCheat = null;
                            mCheatsModule.setModified();
                            mCheatsAdapter.notifyDataSetChanged();
                        } else {
                            String code = ((TextView) d.findViewById(
                                    R.id.cheat_code)).getText().toString();
                            addCheat(code, name);
                        }
                    }
                };

                return new AlertDialog.Builder(this)
                        .setTitle(R.string.new_cheat)
                        .setView(getLayoutInflater().inflate(R.layout.new_cheat, null))
                        .setPositiveButton(android.R.string.ok, l)
                        .setNegativeButton(android.R.string.cancel, null)
                        .create();
        }

        return super.onCreateDialog(id);
    }

    @Override
    protected void onPrepareDialog(int id, Dialog dialog) {
        switch (id) {
            case DIALOG_EDIT_CHEAT:
                TextView codeView = dialog.findViewById(R.id.cheat_code);
                TextView nameView = dialog.findViewById(R.id.cheat_name);
                if (mCurrentCheat == null) {
                    codeView.setEnabled(true);
                    codeView.setText(null);
                    nameView.setText(null);
                } else {
                    codeView.setEnabled(false);
                    codeView.setText(mCurrentCheat.getCode());
                    nameView.setText(mCurrentCheat.getName());
                }
                nameView.requestFocus();
                break;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        getMenuInflater().inflate(R.menu.cheats, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_add_cheat:
                mCurrentCheat = null;
                showDialog(DIALOG_EDIT_CHEAT);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
                                    ContextMenu.ContextMenuInfo menuInfo) {
        menu.add(0, MENU_ITEM_EDIT, 0, R.string.menu_edit);
        menu.add(0, MENU_ITEM_DELETE, 0, R.string.menu_delete);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info =
                (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();

        switch (item.getItemId()) {
            case MENU_ITEM_EDIT:
                mCurrentCheat = mCheatsAdapter.getItem(info.position);
                showDialog(DIALOG_EDIT_CHEAT);
                return true;

            case MENU_ITEM_DELETE:
                mCheatsModule.remove(info.position);
                mCheatsAdapter.notifyDataSetChanged();
                return true;
        }
        return super.onContextItemSelected(item);
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        mCheatsModule.setCheatEnabled(position, l.isItemChecked(position));
    }

    //endregion

    //region Private methods

    /**
     * Adds a new cheat to the CheatsModule.
     *
     * @param code The code the cheat.
     * @param name the name of the cheat.
     */
    private void addCheat(String code, String name) {
        Cheat c = mCheatsModule.add(code, name);

        if (c == null) {
            Toast.makeText(this, R.string.invalid_cheat_code, Toast.LENGTH_SHORT).show();
            return;
        }

        mCheatsAdapter.notifyDataSetChanged();
        syncCheckedStates();
    }

    /**
     * Synchronizes the cheats ListView items' check state according
     * the currently loaded list of cheats.
     */
    private void syncCheckedStates() {
        final List<Cheat> cheats = this.mCheatsModule.getAll();
        final ListView listView = getListView();

        for (int i = 0; i < cheats.size(); i++) {
            listView.setItemChecked(i, cheats.get(i).isEnabled());
        }
    }

    //endregion
}

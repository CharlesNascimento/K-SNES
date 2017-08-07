package com.kansus.ksnes.activity;

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

import com.kansus.ksnes.R;
import com.kansus.ksnes.abstractemulator.Cheat;
import com.kansus.ksnes.abstractemulator.CheatManager;
import com.kansus.ksnes.snes9x.S9xEmulator;

import java.util.List;

public class CheatsActivity extends ListActivity {

    private static final int MENU_ITEM_EDIT = Menu.FIRST;
    private static final int MENU_ITEM_DELETE = Menu.FIRST + 1;

    private static final int DIALOG_EDIT_CHEAT = 1;

    private CheatManager cheats = S9xEmulator.getInstance().getCheats();
    private ArrayAdapter<Cheat> adapter;
    private static Cheat currentCheat;

    private void syncCheckedStates() {
        final List<Cheat> cheats = this.cheats.getAll();
        final ListView listView = getListView();
        for (int i = 0; i < cheats.size(); i++)
            listView.setItemChecked(i, cheats.get(i).enabled);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(R.string.cheats);

        setContentView(R.layout.cheats);
        getListView().setEmptyView(findViewById(R.id.empty));

        adapter = new ArrayAdapter<Cheat>(this,
                android.R.layout.simple_list_item_multiple_choice,
                cheats.getAll());
        setListAdapter(adapter);

        final ListView listView = getListView();
        listView.setOnCreateContextMenuListener(this);
        listView.setItemsCanFocus(false);
        listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);

        syncCheckedStates();
    }

    @Override
    protected void onStop() {
        super.onStop();

        cheats.save();
    }

    private void addCheat(String code, String name) {
        Cheat c = cheats.add(code, name);
        if (c == null) {
            Toast.makeText(this, R.string.invalid_cheat_code,
                    Toast.LENGTH_SHORT).show();
            return;
        }

        adapter.notifyDataSetChanged();
        syncCheckedStates();
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        switch (id) {
            case DIALOG_EDIT_CHEAT:
                DialogInterface.OnClickListener l =
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                final Dialog d = (Dialog) dialog;
                                String name = ((TextView) d.findViewById(
                                        R.id.cheat_name)).getText().toString();
                                if (currentCheat != null) {
                                    currentCheat.name = name;
                                    currentCheat = null;
                                    cheats.setModified();
                                    adapter.notifyDataSetChanged();
                                } else {
                                    String code = ((TextView) d.findViewById(
                                            R.id.cheat_code)).getText().toString();
                                    addCheat(code, name);
                                }
                            }
                        };
                return new AlertDialog.Builder(this).
                        setTitle(R.string.new_cheat).
                        setView(getLayoutInflater().
                                inflate(R.layout.new_cheat, null)).
                        setPositiveButton(android.R.string.ok, l).
                        setNegativeButton(android.R.string.cancel, null).
                        create();
        }
        return super.onCreateDialog(id);
    }

    @Override
    protected void onPrepareDialog(int id, Dialog dialog) {
        switch (id) {
            case DIALOG_EDIT_CHEAT:
                TextView codeView = (TextView) dialog.findViewById(R.id.cheat_code);
                TextView nameView = (TextView) dialog.findViewById(R.id.cheat_name);
                if (currentCheat == null) {
                    codeView.setEnabled(true);
                    codeView.setText(null);
                    nameView.setText(null);
                } else {
                    codeView.setEnabled(false);
                    codeView.setText(currentCheat.code);
                    nameView.setText(currentCheat.name);
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
                currentCheat = null;
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
                currentCheat = (Cheat) adapter.getItem(info.position);
                showDialog(DIALOG_EDIT_CHEAT);
                return true;

            case MENU_ITEM_DELETE:
                cheats.remove(info.position);
                adapter.notifyDataSetChanged();
                return true;
        }
        return super.onContextItemSelected(item);
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        cheats.enable(position, l.isItemChecked(position));
    }
}

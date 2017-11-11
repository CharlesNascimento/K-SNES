package com.kansus.ksnes.activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.media.AudioManager;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.kansus.ksnes.DefaultVideoModule;
import com.kansus.ksnes.InetAddressUtils;
import com.kansus.ksnes.KSNESApplication;
import com.kansus.ksnes.R;
import com.kansus.ksnes.abstractemulator.Emulator;
import com.kansus.ksnes.abstractemulator.input.InputModule;
import com.kansus.ksnes.abstractemulator.multiplayer.MultiPlayerModule;
import com.kansus.ksnes.abstractemulator.video.VideoModule;
import com.kansus.ksnes.dagger.DaggerDgEmulatorComponent;
import com.kansus.ksnes.dagger.DgActivityModule;
import com.kansus.ksnes.dagger.DgEmulatorComponent;
import com.kansus.ksnes.dagger.DgEmulatorModule;
import com.kansus.ksnes.media.MediaScanner;
import com.kansus.ksnes.service.EmulatorService;
import com.kansus.ksnes.service.NetPlayService;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.inject.Inject;

public class EmulatorActivity extends Activity implements
        MultiPlayerModule.FrameUpdateListener,
        SharedPreferences.OnSharedPreferenceChangeListener {

    private static final String LOG_TAG = "K-SNES";

    private static String SCREENSHOTS_FOLDER = Environment
            .getExternalStorageDirectory().getPath() + "K-NES Screenshots";

    private static final int REQUEST_LOAD_STATE = 1;
    private static final int REQUEST_SAVE_STATE = 2;
    private static final int REQUEST_ENABLE_BT_SERVER = 3;
    private static final int REQUEST_ENABLE_BT_CLIENT = 4;
    private static final int REQUEST_BT_DEVICE = 5;

    private static final int DIALOG_QUIT_GAME = 1;
    private static final int DIALOG_REPLACE_GAME = 2;
    private static final int DIALOG_WIFI_CONNECT = 3;

    private static final int NETPLAY_TCP_PORT = 5369;
    private static final int MESSAGE_SYNC_CLIENT = 1000;

    @Inject
    Emulator mEmulator;

    private VideoModule mVideoModule;

    private int quickLoadKey;
    private int quickSaveKey;
    private int fastForwardKey;
    private int screenshotKey;

    private boolean inFastForward;
    private float fastForwardSpeed;

    private SharedPreferences sharedPrefs;
    private Intent newIntent;
    private MediaScanner mediaScanner;

    private NetWaitDialog waitDialog;
    private NetPlayService netPlayService;
    private NetPlayHandler netPlayHandler = new NetPlayHandler(this);
    private int autoSyncClientInterval;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!Intent.ACTION_VIEW.equals(getIntent().getAction())) {
            finish();
            return;
        }
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setVolumeControlStream(AudioManager.STREAM_MUSIC);

        sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        final SharedPreferences prefs = sharedPrefs;
        prefs.registerOnSharedPreferenceChangeListener(this);

        DgEmulatorComponent component = DaggerDgEmulatorComponent.builder()
                .dgApplicationComponent(((KSNESApplication)getApplication()).getApplicationComponent())
                .dgActivityModule(new DgActivityModule(this))
                .dgEmulatorModule(new DgEmulatorModule())
                .build();
        component.inject(this);

        setContentView(R.layout.emulator);

        setupEmulatorView();
        mVideoModule = mEmulator.getVideoModule();

        final String[] prefKeys = {
                "fullScreenMode",
                "flipScreen",
                "fastForwardSpeed",
                "frameSkipMode",
                "maxFrameSkips",
                "refreshRate",
                "enableLightGun",
                "enableGamepad2",
                "soundEnabled",
                "soundVolume",
                "transparencyEnabled",
                "enableHiRes",
                "enableTrackball",
                "trackballSensitivity",
                "useSensor",
                "sensorSensitivity",
                "enableVKeypad",
                "scalingMode",
                "aspectRatio",
                "enableCheats",
                "orientation",
                "useInputMethod",
                "quickLoad",
                "quickSave",
                "fastForward",
                "screenshot",
        };

        for (String key : prefKeys) {
            onSharedPreferenceChanged(prefs, key);
        }

        mEmulator.getInputModule().loadKeyBindings(prefs);

        mEmulator.setOption("enableSRAM", true);
        mEmulator.setOption("apuEnabled", prefs.getBoolean("apuEnabled", true));

        if (!loadROM()) {
            finish();
            return;
        }
        startService(new Intent(this, EmulatorService.class).setAction(EmulatorService.ACTION_FOREGROUND));
    }

    private void setupEmulatorView() {
        if (mEmulator.getVideoModule() instanceof View) {
            LinearLayout myLayout = findViewById(R.id.main);

            View emulatorView = (View) mEmulator.getVideoModule();
            emulatorView.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.MATCH_PARENT));

            myLayout.addView(emulatorView);
            emulatorView.requestFocus();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (mEmulator != null)
            mEmulator.unloadROM();
        onDisconnect();

        stopService(new Intent(this, EmulatorService.class));
    }

    @Override
    protected void onPause() {
        super.onPause();

        pauseEmulator();
        mEmulator.getInputModule().disable();
    }

    @Override
    protected void onResume() {
        super.onResume();

        mEmulator.getInputModule().enable();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        setFlipScreen(sharedPrefs, newConfig);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);

        if (hasFocus) {
            mEmulator.getInputModule().reset();
            mEmulator.setKeyStates(0);

            mEmulator.resume();
        } else {
            mEmulator.pause();
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        if (!Intent.ACTION_VIEW.equals(intent.getAction()))
            return;

        newIntent = intent;

        pauseEmulator();
        showDialog(DIALOG_REPLACE_GAME);
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        switch (id) {
            case DIALOG_QUIT_GAME:
                return createQuitGameDialog();
            case DIALOG_REPLACE_GAME:
                return createReplaceGameDialog();
            case DIALOG_WIFI_CONNECT:
                return createWifiConnectDialog();
        }
        return super.onCreateDialog(id);
    }

    @Override
    protected void onPrepareDialog(int id, Dialog dialog) {
        switch (id) {
            case DIALOG_WIFI_CONNECT:
                TextView v = dialog.findViewById(R.id.port);
                if (v.getText().length() == 0)
                    v.setText(String.valueOf(NETPLAY_TCP_PORT));
                break;
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == quickLoadKey) {
            quickLoad();
            return true;
        }
        if (keyCode == quickSaveKey) {
            quickSave();
            return true;
        }
        if (keyCode == fastForwardKey) {
            onFastForward();
            return true;
        }
        if (keyCode == screenshotKey) {
            onScreenshot();
            return true;
        }
        // ignore keys that would annoy the user
        if (keyCode == KeyEvent.KEYCODE_CAMERA || keyCode == KeyEvent.KEYCODE_SEARCH)
            return true;

        if (keyCode == KeyEvent.KEYCODE_BACK) {
            pauseEmulator();
            showDialog(DIALOG_QUIT_GAME);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        getMenuInflater().inflate(R.menu.emulator, menu);

        if (BluetoothAdapter.getDefaultAdapter() == null) {
            menu.findItem(R.id.menu_bluetooth_server).setVisible(false);
            menu.findItem(R.id.menu_bluetooth_client).setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        pauseEmulator();

        final boolean netplay = (netPlayService != null);
        menu.findItem(R.id.menu_netplay_connect).setVisible(!netplay);
        menu.findItem(R.id.menu_netplay_disconnect).setVisible(netplay);
        menu.findItem(R.id.menu_netplay_sync).setVisible(netplay);
        menu.findItem(R.id.menu_cheats).setVisible(!netplay);
        menu.findItem(R.id.menu_fast_forward).setVisible(!netplay);

        menu.findItem(R.id.menu_cheats).setEnabled(mEmulator.getCheatsModule() != null);
        int fastForwardTitle = inFastForward ? R.string.no_fast_forward : R.string.fast_forward;
        menu.findItem(R.id.menu_fast_forward).setTitle(fastForwardTitle);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_settings:
                startActivity(new Intent(this, EmulatorSettings.class));
                return true;

            case R.id.menu_reset:
                try {
                    if (netPlayService != null)
                        netPlayService.sendResetROM();
                    mEmulator.reset();
                } catch (IOException e) {
                    Log.e(LOG_TAG, e.getMessage());
                }
                return true;

            case R.id.menu_fast_forward:
                onFastForward();
                return true;

            case R.id.menu_screenshot:
                onScreenshot();
                return true;

            case R.id.menu_cheats:
                startActivity(new Intent(this, CheatsActivity.class));
                return true;

            case R.id.menu_save_state:
                onSaveState();
                return true;

            case R.id.menu_load_state:
                onLoadState();
                return true;

            case R.id.menu_wifi_server:
                onWifiServer();
                return true;

            case R.id.menu_wifi_client:
                showDialog(DIALOG_WIFI_CONNECT);
                return true;

            case R.id.menu_bluetooth_server:
                if (checkBluetoothEnabled(REQUEST_ENABLE_BT_SERVER))
                    onBluetoothServer();
                return true;

            case R.id.menu_bluetooth_client:
                if (checkBluetoothEnabled(REQUEST_ENABLE_BT_CLIENT))
                    onBluetoothClient();
                return true;

            case R.id.menu_netplay_disconnect:
                onDisconnect();
                return true;

            case R.id.menu_netplay_sync:
                onNetPlaySync();
                return true;

            case R.id.menu_close:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int request, int result, Intent data) {
        switch (request) {
            case REQUEST_LOAD_STATE:
                if (result == RESULT_OK)
                    loadState(data.getData().getPath());
                break;

            case REQUEST_SAVE_STATE:
                if (result == RESULT_OK)
                    saveState(data.getData().getPath());
                break;

            case REQUEST_ENABLE_BT_SERVER:
                if (result == RESULT_OK)
                    onBluetoothServer();
                break;

            case REQUEST_ENABLE_BT_CLIENT:
                if (result == RESULT_OK)
                    onBluetoothClient();
                break;

            case REQUEST_BT_DEVICE:
                if (result == RESULT_OK) {
                    String address = data.getExtras()
                            .getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS);
                    onBluetoothConnect(address);
                }
                break;
        }
    }

    private static int makeKeyStates(int p1, int p2) {
        return (p2 << 16) | (p1 & 0xffff);
    }

    @Override
    public int onFrameUpdate(int keys) throws IOException, InterruptedException {
        final int remote = netPlayService.sendFrameUpdate(keys);

        if (netPlayService.isServer()) {
            return makeKeyStates(keys, remote);
        } else {
            return makeKeyStates(remote, keys);
        }
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
        if (key.startsWith("gamepad")) {
            mEmulator.getInputModule().loadKeyBindings(prefs);

        } if ("enableLightGun".equals(key)) {
            boolean lightGunEnabled = prefs.getBoolean(key, false);
            mEmulator.getInputModule().setLightGunEnabled(lightGunEnabled);
            mEmulator.setOption(key, lightGunEnabled);

        } else if ("enableTrackball".equals(key)) {
            boolean trackballEnabled = prefs.getBoolean(key, true);
            InputModule inputModule = mEmulator.getInputModule();
            inputModule.setTrackballEnabled(true);

            mVideoModule.setOnTrackballListener(trackballEnabled ? inputModule : null);

        } else if ("trackballSensitivity".equals(key)) {
            int trackballSensitivity = prefs.getInt(key, 2) * 5 + 10;
            mEmulator.getInputModule().setTrackballSensitivity(trackballSensitivity);

        } else if ("useSensor".equals(key)) {
            boolean useSensor = prefs.getString(key, null) != null;
            mEmulator.getInputModule().setSensorInputSourceEnabled(useSensor);

            if (useSensor) {
                mEmulator.getInputModule().getInputSensor().setSensitivity(prefs.getInt(key, 7));
            }

        } else if ("sensorSensitivity".equals(key)) {
            if (mEmulator.getInputModule().getInputSensor() != null) {
                mEmulator.getInputModule().getInputSensor().setSensitivity(prefs.getInt(key, 7));
            }

        } else if ("enableVKeypad".equals(key)) {
            boolean enableVirtualKeypad = prefs.getBoolean(key, true);
            mEmulator.getInputModule().setTouchInputSourceEnabled(enableVirtualKeypad);
        } else if ("fullScreenMode".equals(key)) {
            WindowManager.LayoutParams attrs = getWindow().getAttributes();
            if (prefs.getBoolean("fullScreenMode", true))
                attrs.flags |= WindowManager.LayoutParams.FLAG_FULLSCREEN;
            else
                attrs.flags &= ~WindowManager.LayoutParams.FLAG_FULLSCREEN;
            getWindow().setAttributes(attrs);

        } else if ("flipScreen".equals(key)) {
            setFlipScreen(prefs, getResources().getConfiguration());

        } else if ("fastForwardSpeed".equals(key)) {
            String value = prefs.getString(key, "2x");
            fastForwardSpeed = Float.parseFloat(value.substring(0, value.length() - 1));
            if (inFastForward)
                setGameSpeed(fastForwardSpeed);

        } else if ("frameSkipMode".equals(key)) {
            mEmulator.setOption(key, prefs.getString(key, "auto"));

        } else if ("maxFrameSkips".equals(key)) {
            mEmulator.setOption(key, Integer.toString(prefs.getInt(key, 2)));

        } else if ("maxFramesAhead".equals(key)) {
            if (netPlayService != null)
                netPlayService.setMaxFramesAhead(prefs.getInt(key, 0));

        } else if ("autoSyncClient".equals(key) || "autoSyncClientInterval".equals(key)) {
            if (netPlayService != null && netPlayService.isServer()) {
                stopAutoSyncClient();
                if (sharedPrefs.getBoolean("autoSyncClient", false)) {
                    String prefVal = sharedPrefs.getString("autoSyncClientInterval", "30");
                    autoSyncClientInterval = Integer.valueOf(prefVal);
                    autoSyncClientInterval *= 1000;
                    startAutoSyncClient();
                }
            }

        } else if ("refreshRate".equals(key)) {
            mEmulator.setOption(key, prefs.getString(key, "default"));

        } else if ("enableGamepad2".equals(key)) {
            mEmulator.setOption(key, prefs.getBoolean(key, false));

        } else if ("soundEnabled".equals(key)) {
            mEmulator.setOption(key, prefs.getBoolean(key, true));

        } else if ("soundVolume".equals(key)) {
            mEmulator.setOption(key, prefs.getInt(key, 100));

        } else if ("transparencyEnabled".equals(key)) {
            mEmulator.setOption(key, prefs.getBoolean(key, false));

        } else if ("enableHiRes".equals(key)) {
            mEmulator.setOption(key, prefs.getBoolean(key, true));

        } else if ("scalingMode".equals(key)) {
            mVideoModule.setScalingMode(getScalingMode(prefs.getString(key, "proportional")));

        } else if ("aspectRatio".equals(key)) {
            float ratio = Float.parseFloat(prefs.getString(key, "1.3333"));
            mVideoModule.setAspectRatio(ratio);

        } else if ("enableCheats".equals(key)) {
            boolean enable = prefs.getBoolean(key, true);
            if (enable) {
                mEmulator.getCheatsModule().enable();
            } else {
                mEmulator.getCheatsModule().disable();
            }

        } else if ("orientation".equals(key)) {
            String orientation = prefs.getString(key, "unspecified");
            switch (orientation) {
                case "landscape":
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                    break;
                case "portrait":
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                    break;
                default:
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
                    break;
            }

        } else if ("useInputMethod".equals(key)) {
            getWindow().setFlags(prefs.getBoolean(key, false) ?
                            0 : WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM,
                    WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);

        } else if ("quickLoad".equals(key)) {
            quickLoadKey = prefs.getInt(key, 0);

        } else if ("quickSave".equals(key)) {
            quickSaveKey = prefs.getInt(key, 0);

        } else if ("fastForward".equals(key)) {
            fastForwardKey = prefs.getInt(key, 0);

        } else if ("screenshot".equals(key)) {
            screenshotKey = prefs.getInt(key, 0);
        }
    }

    private void pauseEmulator() {
        mEmulator.pause();
    }

    private void resumeEmulator() {
        if (hasWindowFocus()) {
            mEmulator.resume();
        }
    }

    private boolean checkBluetoothEnabled(int request) {
        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        if (adapter != null && adapter.isEnabled())
            return true;

        Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        startActivityForResult(intent, request);
        return false;
    }

    private void setFlipScreen(SharedPreferences prefs, Configuration config) {
        boolean flipScreen = false;

        if (config.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            flipScreen = prefs.getBoolean("flipScreen", false);
        }

        mEmulator.setScreenFlipped(flipScreen);
        mEmulator.setOption("flipScreen", flipScreen);
    }

    private static int getScalingMode(String mode) {
        if (mode.equals("original"))
            return DefaultVideoModule.SCALING_ORIGINAL;
        if (mode.equals("2x"))
            return DefaultVideoModule.SCALING_2X;
        if (mode.equals("proportional"))
            return DefaultVideoModule.SCALING_PROPORTIONAL;
        return DefaultVideoModule.SCALING_STRETCH;
    }

    private Dialog createQuitGameDialog() {
        DialogInterface.OnClickListener l = new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case 1:
                        quickSave();
                        // fall through
                    case 2:
                        finish();
                        break;
                }
            }
        };

        return new AlertDialog.Builder(this)
                .setTitle(R.string.quit_game_title)
                .setItems(R.array.exit_game_options, l)
                .create();
    }

    private Dialog createReplaceGameDialog() {
        DialogInterface.OnClickListener l = new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                if (which == DialogInterface.BUTTON_POSITIVE) {
                    setIntent(newIntent);
                    loadROM();
                }
                newIntent = null;
            }
        };

        return new AlertDialog.Builder(this)
                .setCancelable(false)
                .setTitle(R.string.replace_game_title)
                .setMessage(R.string.replace_game_message)
                .setPositiveButton(android.R.string.yes, l)
                .setNegativeButton(android.R.string.no, l)
                .create();
    }

    @SuppressLint("InflateParams")
    private Dialog createWifiConnectDialog() {
        DialogInterface.OnClickListener l = new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                final Dialog d = (Dialog) dialog;
                String ip = ((TextView) d.findViewById(R.id.ip_address)).getText().toString();
                String port = ((TextView) d.findViewById(R.id.port)).getText().toString();
                onWifiConnect(ip, port);
            }
        };

        return new AlertDialog.Builder(this)
                .setTitle(R.string.wifi_client)
                .setView(getLayoutInflater().inflate(R.layout.wifi_connect, null))
                .setPositiveButton(android.R.string.ok, l)
                .setNegativeButton(android.R.string.cancel, null)
                .create();
    }

    private String getROMFilePath() {
        return getIntent().getData().getPath();
    }

    private boolean isROMSupported(String file) {
        file = file.toLowerCase();

        String[] filters = getResources().
                getStringArray(R.array.file_chooser_filters);
        for (String f : filters) {
            if (file.endsWith(f))
                return true;
        }
        return false;
    }

    private boolean loadROM() {
        String path = getROMFilePath();

        if (!isROMSupported(path)) {
            Toast.makeText(this, R.string.rom_not_supported, Toast.LENGTH_SHORT).show();
            finish();
            return false;
        }
        if (!mEmulator.loadROM(path)) {
            Toast.makeText(this, R.string.load_rom_failed, Toast.LENGTH_SHORT).show();
            finish();
            return false;
        }
        // reset fast-forward on ROM load
        inFastForward = false;

        mVideoModule.setActualSize(mEmulator.getVideoWidth(), mEmulator.getVideoHeight());

        if (sharedPrefs.getBoolean("quickLoadOnStart", true))
            quickLoad();
        return true;
    }

    private Dialog createNetWaitDialog(CharSequence title, CharSequence message) {
        if (waitDialog != null) {
            waitDialog.dismiss();
            waitDialog = null;
        }
        waitDialog = new NetWaitDialog();
        waitDialog.setTitle(title);
        waitDialog.setMessage(message);
        return waitDialog;
    }

    private void ensureDiscoverable() {
        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();

        if (adapter != null && adapter.getScanMode() !=
                BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
            Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
            startActivity(intent);
        }
    }

    private void onWifiServer() {
        WifiManager wifi = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
        WifiInfo info = (wifi != null ? wifi.getConnectionInfo() : null);
        int ip = (info != null ? info.getIpAddress() : 0);
        if (ip == 0) {
            Toast.makeText(this, R.string.wifi_not_available,
                    Toast.LENGTH_SHORT).show();
            return;
        }

        InetAddress address = null;
        try {
            address = InetAddress.getByAddress(new byte[]{
                    (byte) ip,
                    (byte) (ip >>> 8),
                    (byte) (ip >>> 16),
                    (byte) (ip >>> 24),
            });
        } catch (UnknownHostException e) {
            Log.e(LOG_TAG, e.getMessage());
        }

        int port = NETPLAY_TCP_PORT;
        try {
            final NetPlayService np = new NetPlayService(netPlayHandler);
            port = np.tcpListen(address, port);
            netPlayService = np;
        } catch (IOException e) {
            return;
        }

        assert address != null;
        createNetWaitDialog(getText(R.string.wifi_server),
                getString(R.string.wifi_server_listening, address.getHostAddress(), port)).show();
    }

    private void onWifiConnect(String ip, String portStr) {
        InetAddress addr = null;
        try {
            if (InetAddressUtils.isIPv4Address(ip))
                addr = InetAddress.getByName(ip);
        } catch (UnknownHostException e) {
            Log.e(LOG_TAG, e.getMessage());
        }
        if (addr == null) {
            Toast.makeText(this, R.string.invalid_ip_address, Toast.LENGTH_SHORT).show();
            return;
        }

        int port = 0;
        try {
            port = Integer.parseInt(portStr);
        } catch (NumberFormatException e) {
            Log.e(LOG_TAG, e.getMessage());
        }
        if (port <= 0) {
            Toast.makeText(this, R.string.invalid_port, Toast.LENGTH_SHORT).show();
            return;
        }
        netPlayService = new NetPlayService(netPlayHandler);
        netPlayService.tcpConnect(addr, port);

        createNetWaitDialog(getText(R.string.wifi_client), getString(R.string.client_connecting)).show();
    }

    private void onBluetoothServer() {
        try {
            final NetPlayService np = new NetPlayService(netPlayHandler);
            np.bluetoothListen();
            netPlayService = np;
        } catch (IOException e) {
            Log.e(LOG_TAG, e.getMessage());
            return;
        }

        createNetWaitDialog(getText(R.string.bluetooth_server),
                getString(R.string.bluetooth_server_listening));

        waitDialog.setOnClickListener(new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int button) {
                ensureDiscoverable();
            }
        });
        waitDialog.show();
    }

    private void onBluetoothConnect(String address) {
        try {
            final NetPlayService np = new NetPlayService(netPlayHandler);
            np.bluetoothConnect(address);
            netPlayService = np;
        } catch (IOException e) {
            return;
        }
        createNetWaitDialog(getText(R.string.bluetooth_client),
                getString(R.string.client_connecting)).show();
    }

    private void onBluetoothClient() {
        Intent intent = new Intent(this, DeviceListActivity.class);
        startActivityForResult(intent, REQUEST_BT_DEVICE);
    }

    private void onNetPlaySync() {
        File file = getTempStateFile();
        try {
            mEmulator.saveState(file.getAbsolutePath());
            netPlayService.sendSavedState(readFile(file));
        } catch (IOException e) {
            Log.e(LOG_TAG, e.getMessage());
        }

        if (!file.delete()) {
            Log.w(LOG_TAG, "Failed to delete file");
        }
    }

    private void onDisconnect() {
        if (netPlayService == null)
            return;

        onSharedPreferenceChanged(sharedPrefs, "enableCheats");
        onSharedPreferenceChanged(sharedPrefs, "enableGamepad2");
        stopAutoSyncClient();

        mEmulator.setFrameUpdateListener(null);
        netPlayService.disconnect();
        netPlayService = null;
    }

    private void onLoadState() {
        Intent intent = new Intent(this, StateSlotsActivity.class);
        intent.setData(getIntent().getData());
        startActivityForResult(intent, REQUEST_LOAD_STATE);
    }

    private void onSaveState() {
        Intent intent = new Intent(this, StateSlotsActivity.class);
        intent.setData(getIntent().getData());
        intent.putExtra(StateSlotsActivity.EXTRA_SAVE_MODE, true);
        startActivityForResult(intent, REQUEST_SAVE_STATE);
    }

    private void applyNetplaySettings() {
        mEmulator.setOption("enableGamepad2", true);
        mEmulator.setOption("enableCheats", false);
        onSharedPreferenceChanged(sharedPrefs, "maxFramesAhead");
        onSharedPreferenceChanged(sharedPrefs, "autoSyncClient");

        if (inFastForward) {
            inFastForward = false;
            setGameSpeed(1.0f);
        }
    }

    private void startAutoSyncClient() {
        netPlayHandler.sendMessageDelayed(netPlayHandler.obtainMessage(MESSAGE_SYNC_CLIENT),
                autoSyncClientInterval);
    }

    private void stopAutoSyncClient() {
        netPlayHandler.removeMessages(MESSAGE_SYNC_CLIENT);
    }

    private void setGameSpeed(float speed) {
        pauseEmulator();
        mEmulator.setOption("gameSpeed", Float.toString(speed));
        resumeEmulator();
    }

    private void onFastForward() {
        if (netPlayService != null)
            return;

        inFastForward = !inFastForward;
        setGameSpeed(inFastForward ? fastForwardSpeed : 1.0f);
    }

    private void onScreenshot() {
        File dir = new File(SCREENSHOTS_FOLDER);
        if (!dir.exists() && !dir.mkdir()) {
            Log.w(LOG_TAG, "Could not create directory for screenshots");
            return;
        }
        String name = Long.toString(System.currentTimeMillis()) + ".png";
        File file = new File(dir, name);

        pauseEmulator();

        FileOutputStream out = null;
        try {
            try {
                out = new FileOutputStream(file);
                Bitmap bitmap = getScreenshot();
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
                bitmap.recycle();

                Toast.makeText(this, R.string.screenshot_saved, Toast.LENGTH_SHORT).show();

                if (mediaScanner == null)
                    mediaScanner = new MediaScanner(this);
                mediaScanner.scanFile(file.getAbsolutePath(), "image/png");

            } finally {
                if (out != null)
                    out.close();
            }
        } catch (IOException e) {
            Log.e(LOG_TAG, e.getMessage());
        }

        resumeEmulator();
    }

    private File getTempStateFile() {
        return new File(getCacheDir(), "saved_state");
    }

    @SuppressWarnings("TryFinallyCanBeTryWithResources")
    private static byte[] readFile(File file) throws IOException {
        FileInputStream in = new FileInputStream(file);
        byte[] buffer = new byte[(int) file.length()];
        try {
            if (in.read(buffer) == -1)
                throw new IOException();
        } finally {
            in.close();
        }
        return buffer;
    }

    @SuppressWarnings("TryFinallyCanBeTryWithResources")
    private static void writeFile(File file, byte[] buffer) throws IOException {
        FileOutputStream out = new FileOutputStream(file);
        try {
            out.write(buffer);
        } finally {
            out.close();
        }
    }

    private void saveState(String fileName) {
        pauseEmulator();

        ZipOutputStream out = null;
        try {
            try {
                out = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(fileName)));
                out.putNextEntry(new ZipEntry("screenshot.png"));

                Bitmap bitmap = getScreenshot();
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
                bitmap.recycle();
            } finally {
                if (out != null)
                    out.close();
            }
        } catch (Exception e) {
            Log.e(LOG_TAG, e.getMessage());
        }

        mEmulator.saveState(fileName);
        resumeEmulator();
    }

    private void loadState(String fileName) {
        File file = new File(fileName);
        if (!file.exists())
            return;

        pauseEmulator();
        try {
            if (netPlayService != null)
                netPlayService.sendSavedState(readFile(file));
            mEmulator.loadState(fileName);

        } catch (IOException e) {
            Log.e(LOG_TAG, e.getMessage());
        }
        resumeEmulator();
    }

    private Bitmap getScreenshot() {
        final int w = mEmulator.getVideoWidth();
        final int h = mEmulator.getVideoHeight();

        ByteBuffer buffer = ByteBuffer.allocateDirect(w * h * 2);
        mEmulator.getScreenshot(buffer);

        Bitmap bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.RGB_565);
        bitmap.copyPixelsFromBuffer(buffer);
        return bitmap;
    }

    private String getQuickSlotFileName() {
        return StateSlotsActivity.getSlotFileName(getROMFilePath(), 0);
    }

    private void quickSave() {
        saveState(getQuickSlotFileName());
    }

    private void quickLoad() {
        loadState(getQuickSlotFileName());
    }

    private class NetWaitDialog extends ProgressDialog implements DialogInterface.OnCancelListener {

        private OnClickListener onClickListener;

        NetWaitDialog() {
            super(EmulatorActivity.this);

            setIndeterminate(true);
            setCancelable(true);
            setOnCancelListener(this);
        }

        void setOnClickListener(OnClickListener l) {
            onClickListener = l;
        }

        @Override
        public boolean dispatchTouchEvent(@NonNull MotionEvent event) {
            if (onClickListener != null && event.getAction() == MotionEvent.ACTION_UP) {
                onClickListener.onClick(this, BUTTON_POSITIVE);
                return true;
            }
            return super.dispatchTouchEvent(event);
        }

        public void onCancel(DialogInterface dialog) {
            waitDialog = null;
            netPlayService.disconnect();
            netPlayService = null;
        }
    }

    private static class NetPlayHandler extends Handler {

        private final WeakReference<EmulatorActivity> mTarget;

        NetPlayHandler(EmulatorActivity target) {
            mTarget = new WeakReference<>(target);
        }

        @Override
        public void handleMessage(Message msg) {
            EmulatorActivity activity = mTarget.get();

            if (activity.netPlayService == null)
                return;

            switch (msg.what) {
                case NetPlayService.MESSAGE_CONNECTED:
                    activity.applyNetplaySettings();
                    if (activity.netPlayService.isServer())
                        activity.onNetPlaySync();

                    activity.mEmulator.setFrameUpdateListener(activity);
                    activity.netPlayService.sendMessageReply();

                    if (activity.waitDialog != null) {
                        activity.waitDialog.dismiss();
                        activity.waitDialog = null;
                    }
                    break;

                case NetPlayService.MESSAGE_DISCONNECTED:
                    activity.onDisconnect();

                    if (activity.waitDialog != null) {
                        activity.waitDialog.dismiss();
                        activity.waitDialog = null;
                    }
                    int error = R.string.connection_closed;
                    switch (msg.arg1) {
                        case NetPlayService.E_CONNECT_FAILED:
                            error = R.string.connect_failed;
                            break;
                        case NetPlayService.E_PROTOCOL_INCOMPATIBLE:
                            error = R.string.protocol_incompatible;
                            break;
                    }
                    Toast.makeText(activity, error, Toast.LENGTH_LONG).show();
                    break;

                case NetPlayService.MESSAGE_POWER_ROM:
                    activity.mEmulator.power();
                    activity.netPlayService.sendMessageReply();
                    break;

                case NetPlayService.MESSAGE_RESET_ROM:
                    activity.mEmulator.reset();
                    activity.netPlayService.sendMessageReply();
                    break;

                case NetPlayService.MESSAGE_SAVED_STATE:
                    File file = activity.getTempStateFile();
                    try {
                        writeFile(file, (byte[]) msg.obj);
                        activity.mEmulator.loadState(file.getAbsolutePath());
                    } catch (IOException e) {
                        Log.d(LOG_TAG, e.getMessage());
                    } finally {
                        boolean result = file.delete();

                        if (!result) {
                            Log.w(LOG_TAG, "Failed to delete file");
                        }
                    }
                    activity.netPlayService.sendMessageReply();
                    break;

                case MESSAGE_SYNC_CLIENT:
                    if (activity.hasWindowFocus())
                        activity.onNetPlaySync();
                    activity.startAutoSyncClient();
                    break;
            }
        }
    }
}
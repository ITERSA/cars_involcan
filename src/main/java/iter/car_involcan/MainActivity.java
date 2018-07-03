package iter.car_involcan;

import android.Manifest;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.PagerTabStrip;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;

public class MainActivity extends AppCompatActivity implements EntregaFragment.OnFragmentInteractionListener, RecogidaFragment.OnFragmentInteractionListener{

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    private SectionsPagerAdapter mSectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    private ViewPager mViewPager;
    private RecogidaFragment recogidaFragment;
    private EntregaFragment entregaFragment;
    private Handler writeToFileHandler, uploadFileToFTPHandler;
    private static final String FTP_IP = "1nv0lc4n.dyndns.org";
    private static final int FTP_PORT = 12221;
    private static final String FTP_USER = "cars";
    private static final String FTP_PASS = "c4rsc4rs";
    private static String pathInFTP = "/path";
    private static String pathEntrega = "/otherPath";
    private static final int MY_PERMISSIONS_REQUEST_WRITE_STORAGE = 112;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

         // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);
        PagerTabStrip pagerTabStrip = findViewById(R.id.pager_title_strip);
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showSaveDialog();
                //Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG).setAction("Action", null).show();
            }
        });
        writeToFileHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                final String currentPath = (String)msg.obj;
                if (msg.what == 1){

                    UploadFileToFTPThread uploadFileToFTPThread = new UploadFileToFTPThread(currentPath, msg.arg1);
                    uploadFileToFTPThread.start();
                }else{
                    Toast.makeText(getApplicationContext(),"Error generando el fichero "+ (String)msg.obj, Toast.LENGTH_LONG).show();
                }
            }
        };

        uploadFileToFTPHandler = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                String fileName = (String)msg.obj;
                if (msg.what == 1)
                    Toast.makeText(getApplicationContext(),"Fichero subido con exito "+fileName, Toast.LENGTH_LONG).show();
                else
                    Toast.makeText(getApplicationContext(),"Error subiendo el fichero "+ fileName +" al FTP!", Toast.LENGTH_LONG).show();
            }
        };
    }

    @Override
    protected void onStart() {
        super.onStart();
        //requestJson();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            //requestJson();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == MY_PERMISSIONS_REQUEST_WRITE_STORAGE){
            if (grantResults.length == 0 || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                //Log.i(TAG, "Permission has been denied by user");
            } else {
                sendDataToServerDialog();
            }
        }
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {
        final int PAGE_COUNT = 2;
        // Tab Titles
        private String tabtitles[] = new String[] { "Recogida", "Entrega"};
        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            switch (position){
                case 0:
                    recogidaFragment = RecogidaFragment.newInstance("Nombre", "Apellidos");
                    return recogidaFragment;
                case 1:
                     entregaFragment = EntregaFragment.newInstance("Nombre","Apellidos");
                    return entregaFragment;
            }
            return null;
            // Return a PlaceholderFragment (defined as a static inner class below).
        }

        @Override
        public int getCount() {
            // Show 3 total pages.
            return PAGE_COUNT;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return tabtitles[position];
        }
    }

    private void requestJson(){
        RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());
        String androidId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, getString(R.string.HOST) + "?id=" + androidId,null, new Response.Listener<JSONObject>() {

            @Override
            public void onResponse(JSONObject response) {
                if (recogidaFragment != null && entregaFragment != null){
                    recogidaFragment.setData(response);
                    entregaFragment.setData(response);
                }

            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                String currentError ="Error getting info from server.";
                if (error != null)
                    currentError = error.getMessage().toString();
                Toast.makeText(getApplicationContext(), currentError, Toast.LENGTH_LONG).show();
            }
        });
        requestQueue.add(jsonObjectRequest);
    }

    private class WriteToFileThread extends Thread{

        private String data;
        private File file;
        int type;

        public WriteToFileThread(JSONObject _data, int _type){

            data = _data.toString();
            type = _type;
            //data = campaingName + " - " + data;

            String path = Environment.getExternalStorageDirectory() + File.separator  + "cars";
            // Create the folder.
            File folder = new File(path);
            folder.mkdirs();
            // Create the file.
            //String date = new SimpleDateFormat("HH-mm-ss_dd-MM-yyyy").format(new Date());
            String filename = "filename";   //TODO change name
            file = new File(folder, filename+".json");
        }

        @Override
        public void run(){
            // Save your stream, don't forget to flush() it before closing it.
            int status = 0;
            try{
                FileWriter writer = new FileWriter(file);
                writer.append(data);
                writer.flush();
                writer.close();
                Log.d("MainActivity","File saved to "+file.getAbsolutePath());
                //Log.v("DataActivity", data);
                status = 1;
            }catch (IOException e){
                Log.e("Exception", "File write failed: " + e.toString());
            }
            Message msg = new Message();
            msg.what = status;
            msg.obj = file.getAbsolutePath();
            msg.arg1 = type;
            Log.d("File", file.getAbsolutePath());
            if (writeToFileHandler != null){
                writeToFileHandler.sendMessage(msg);
            }
        }
    }

    /**
     * Thread that upload a file to FTP
     */
    private class UploadFileToFTPThread extends Thread{

        private String path;
        int type;
        public UploadFileToFTPThread(String _path, int _type){
            path = _path;
            type = _type;
        }

        @Override
        public void run() {
            FTPClient con = null;
            boolean result = false;
            File f = new File(path);
            if (f.exists()){
                try {
                    con = new FTPClient();
                    con.connect(FTP_IP,FTP_PORT);
                    if (con.login(FTP_USER,FTP_PASS)){
                        con.enterLocalPassiveMode();
                        con.setFileType(FTP.BINARY_FILE_TYPE);
                        BufferedInputStream buffIn=null;
                        buffIn=new BufferedInputStream(new FileInputStream(f));
                        // FileInputStream in = new FileInputStream(f);
                        String finalPath = pathInFTP;
                        if (type == 0){
                            finalPath = pathEntrega;
                        }
                        con.changeWorkingDirectory(finalPath);
                        result = con.storeFile(f.getName(),buffIn);
                        buffIn.close();
                        if (result)
                            Log.v("ftp", "Upload success");
                        con.logout();
                        con.disconnect();
                    }else
                        Log.e("ftp", "login error");
                }catch (IOException e){
                    //e.getCause().printStackTrace();
                    Log.e("ftp", e.getMessage());
                }
            }else
                Log.d("ftp","File not exists");
            Message msg = new Message();
            if (result)
                msg.what = 1;
            else
                msg.what = 0;
            msg.obj = f.getName();
            if (uploadFileToFTPHandler != null)
                uploadFileToFTPHandler.sendMessage(msg);
        }
    }

    /**
     * Open a dialog asking if user wants to send de data to the server
     */
    private void sendDataToServerDialog(){
        AlertDialog d = new AlertDialog.Builder(MainActivity.this)
                .setTitle(R.string.app_name)
                .setIcon(android.R.drawable.ic_menu_save)
                .setMessage("¿Desea Enviar los datos al FTP?")
                .setPositiveButton("Si", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        JSONObject json = null;
                        switch (mViewPager.getCurrentItem()){
                            case 0:
                                 json = recogidaFragment.getData();
                                break;
                            case 1:
                                json = entregaFragment.getData();
                                break;
                            default:
                        }
                        if (json != null){
                            WriteToFileThread writeToFileThread = new WriteToFileThread(json, mViewPager.getCurrentItem());
                            writeToFileThread.start();
                        }
                        dialog.dismiss();
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }).create();
        d.show();
    }

    private void showSaveDialog(){
        int permission = ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (permission == PackageManager.PERMISSION_GRANTED) {
            sendDataToServerDialog();
        }else{
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

                ActivityCompat.requestPermissions(MainActivity.this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE},
                        MY_PERMISSIONS_REQUEST_WRITE_STORAGE);
            }
        }
    }
}

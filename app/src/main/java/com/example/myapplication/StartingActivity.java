package com.example.myapplication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Array;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StartingActivity extends AppCompatActivity {

    Button newGameButton;
    ListView recordsView;
    ProgressBar pb;

    SharedPreferences sp;
    ArrayList<String> records = new ArrayList<>();

    Intent i;

    String userName = "";
    //var that make the Activity wait for the total execution
    Boolean execute = false;
    ArrayList<String> celebURLs = new ArrayList<String>();
    ArrayList<String> celebNames = new ArrayList<String>();

    int user = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_starting);

        //Start reading webpage
        i= new Intent(StartingActivity.this, MainActivity.class);
        sp = this.getSharedPreferences("com.example.myapplication", Context.MODE_PRIVATE);

        if(!sp.contains("Jennifer Lopez")) {
            DownloadTask task = new DownloadTask();
            task.execute("https://www.boredpanda.com/childhood-celebrities-when-they-were-young-kids/?utm_source=google&utm_medium=organic&utm_campaign=organic");
        } else {
            //no need to connect with internet
            execute = true;
        }

        pb = (ProgressBar) findViewById(R.id.download_progress);
        newGameButton = (Button) findViewById(R.id.new_game_button);
        recordsView = (ListView) findViewById(R.id.records_view);

        initialize();


    }

    private void initialize(){

        while (sp.getString("user " + user, null) != null){
            records.add(sp.getString("user " + user, null));
            user++;

        }
        if(records.size() == 0) {
            records.add("No players have played yet");
        }

        final RecordsAdapterClass adapter = new RecordsAdapterClass(this, R.layout.content_records, records);
        recordsView.setAdapter(adapter);

        //setOnclickListenerList

        recordsView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, final View view,
                                    int position, long id) {
                final String item = (String) parent.getItemAtPosition(position);
                view.animate().setDuration(2000).alpha(0)
                        .withEndAction(new Runnable() {
                            @Override
                            public void run() {
                                records.remove(item);
                                adapter.notifyDataSetChanged();
                                view.setAlpha(1);
                            }
                        });
            }

        });

    }

    //Button Method that initiate new game
    public void newGame(View view){

         // Set up the input
        final EditText input = new EditText(this);
        // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_CLASS_TEXT);

        //AlertDialog to enter the player name
        new AlertDialog.Builder(this)
                .setIcon(R.drawable.ic_person_black_24dp)
                .setTitle("Player Name")
                .setMessage("Please enter the player name")
                .setView(input)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        userName = input.getText().toString();
                        pb.setVisibility(View.VISIBLE);
                        i.putExtra("user", userName);
                        if(execute) {
                            startActivity(i);
                        }
                        else {
                            execute = true;}

                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                }).show();
    }

    public String readWebPage(String urlToRead){
        String result = "";
        try {
            URL oracle = new URL(urlToRead);
            BufferedReader in = null;
            in = new BufferedReader(
                    new InputStreamReader(oracle.openStream()));
            String inputLine = "";
            while ((inputLine = in.readLine()) != null){
                result += inputLine;
            }
            in.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        result = result.split(">You can tell the ones who were child actors")[1];
        //result = result.split("childhood-celebrities-when-they-were-young-kids-125-58b44af7148dc__700.jpg")[0];

        trackNames(result);
        return result;
    }

    public void trackNames(String archive){

        Pattern p = Pattern.compile("data-src=\"(.*?)\" alt");
        Matcher m = p.matcher(archive);

        while (m.find()) {
            celebURLs.add(m.group(1));
        }
        p = Pattern.compile("</span><h2>(.+?)</h2>");
        m = p.matcher(archive);

        while(m.find()) {
            celebNames.add(m.group(1));
        }
        cleanURLs(celebURLs);
    }

    public void cleanURLs (ArrayList<String> URLsToClean){
        for(int i=0; i<URLsToClean.size(); i++) {
            if (URLsToClean.get(i).split("nopin").length > 1 ) {
                URLsToClean.remove(i);
                i--;
            }
        }
        saveURLandNames();
    }

    private void saveURLandNames(){
        //save urls and names in SharedPreferences

        for(int i=0; i<celebNames.size(); i++) {
            sp.edit().putString(celebNames.get(i), celebURLs.get(i)).commit();
        }
    }


    //Adapter Class to inflate ListView via Adapter
    private class RecordsAdapterClass extends ArrayAdapter<String> {

        HashMap<String, Integer> mIdMap = new HashMap<>();

        public RecordsAdapterClass(@NonNull Context context, int resource, List<String> recordsMap) {
            super(context, resource, recordsMap);
            for (int i = 0; i < recordsMap.size(); ++i) {
                mIdMap.put(recordsMap.get(i), i);
            }
        }

        @Override
        public long getItemId(int position){
            String item = getItem(position);
            return mIdMap.get(item);
        }
    }

    public class DownloadTask extends AsyncTask<String, Void, String> {

        String result = "";

        @Override
        protected String doInBackground(String... urls) {

            String result = readWebPage(urls[0]);
            Log.i("HTML", result);
            return result;
        }

        @Override
        protected void onPostExecute(final String success){
            pb.setVisibility(View.INVISIBLE);
            //Creat Intent to MainActivity
            if(execute) {
                startActivity(i);
            }
            else {
                execute = true;}

        }
    }

}

package com.example.myapplication;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.BatchUpdateException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends Activity {
    ArrayList<String> celebURLs = new ArrayList<>();
    ArrayList<String> celebNames = new ArrayList<>();
    int chosenCeleb = 0;
    int locationOfCorrectAnswer = 0;
    String[] answers = new String[4];

    ImageView imageView;
    Button button0;
    Button button1;
    Button button2;
    Button button3;
    TextView punctationText;

    int punctuation = 0;
    String userName = "unknown";
    int numberOfUsers = 0; //from 1 to ...
    int numberOfUser = 0; //id of user

    public void celebChosen(View view){
        Log.i("Tag: ", view.getTag().toString());
        Log.i("LocAnswer: ", Integer.toString(locationOfCorrectAnswer));
        if(view.getTag().toString().equals(Integer.toString(locationOfCorrectAnswer))){
            Toast.makeText(getApplicationContext(), "Correct!", Toast.LENGTH_LONG).show();
            punctuation++;
            punctationText.setText("Score: " + punctuation);
            createNewQuestion();

        } else{
            Toast.makeText(getApplicationContext(), "Wrong! It was " + celebNames.get(chosenCeleb), Toast.LENGTH_LONG).show();

            Intent i = new Intent(MainActivity.this, StartingActivity.class);
            SharedPreferences sp = this.getSharedPreferences("com.example.myapplication", MODE_PRIVATE);
            if(numberOfUser < numberOfUsers) {
                sp.edit().remove("user " + numberOfUser).commit();
            }
                sp.edit().putString("user " + numberOfUser, userName + "--" + punctuation).commit();
            i.putExtra("user " + numberOfUser, userName + "--" + punctuation);
            startActivity(i);
        }

    }

    public class ImageDownloader extends AsyncTask<String, Void, Bitmap>{

        @Override
        protected Bitmap doInBackground(String... urls) {

            try {
                URL url = new URL(urls[0]);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.connect();
                InputStream inputStream = connection.getInputStream();
                Bitmap myBitMap = BitmapFactory.decodeStream(inputStream);
                return myBitMap;

            } catch (MalformedURLException ex) {
                ex.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        imageView = (ImageView) findViewById((R.id.imageView));
        button0 = (Button) findViewById(R.id.button1);
        button1 = (Button) findViewById(R.id.button2);
        button2 = (Button) findViewById(R.id.button3);
        button3 = (Button) findViewById(R.id.button4);
        punctationText = (TextView) findViewById(R.id.punctation_text);

        Bundle extras = getIntent().getExtras();
        userName = extras.getString("user", "unknown");

        SharedPreferences sp = this.getSharedPreferences("com.example.myapplication", MODE_PRIVATE);

        Map<String, ?> allEntries = sp.getAll();
        for (Map.Entry<String, ?> entry : allEntries.entrySet()) {

            if(entry.getKey().substring(0,5).equals("user ")){

                Log.i("user: ", entry.getKey() + " " + entry.getValue());
                numberOfUsers ++;
                if(entry.getValue().toString().split("--")[0].equals(userName)) { //the user is the same as other who has played before
                    numberOfUser = Integer.parseInt(entry.getKey().split(" ")[1]); //gets number of user
                }

            } else {

                celebNames.add(entry.getKey());
                celebURLs.add(entry.getValue().toString());
            }
        }
        numberOfUser = (numberOfUser== 0)? numberOfUsers : numberOfUser; //if it is a new user we put him a new identifier
        //celebURLs.addAll(sp.getStringSet("URL", new HashSet<String>()));
        //celebNames.addAll(sp.getStringSet("name", new HashSet<String>()));

        if(celebURLs.isEmpty()) {
            new AlertDialog.Builder(this)
                    .setTitle("ERROR")
                    .setMessage("Check your connection and restart the app")
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Intent i = new Intent(MainActivity.this, StartingActivity.class);
                            startActivity(i);
                            finish();
                        }
                    })
                    .setNegativeButton("Not OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            finish();
                            System.exit(0);
                        }
                    })
                    .show();
        } else {
            createNewQuestion();
        }

    }

    public void createNewQuestion() {
        Random random = new Random();
        chosenCeleb = random.nextInt(celebURLs.size());

        ImageDownloader imageTask = new ImageDownloader();
        Bitmap celebImage;

        try {
            celebImage = imageTask.execute(celebURLs.get(chosenCeleb)).get();
            imageView.setImageBitmap(celebImage);
            locationOfCorrectAnswer = random.nextInt(4);
            int incorrectAnswerLocation;

            for (int i=0; i<4; i++) {
                if(i == locationOfCorrectAnswer){
                    answers[i] = celebNames.get(chosenCeleb);
                } else {
                    incorrectAnswerLocation = random.nextInt(celebURLs.size());

                    while (incorrectAnswerLocation == chosenCeleb){
                        incorrectAnswerLocation = random.nextInt(celebURLs.size());
                    }

                    answers[i] = celebNames.get(incorrectAnswerLocation);
                }
            }

            button0.setText(answers[0]);
            button1.setText(answers[1]);
            button2.setText(answers[2]);
            button3.setText(answers[3]);

        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }
    /*

    public String readWebPage(String urlToRead){
        int progress = 0;
        String result = "";
        try {
            URL oracle = new URL(urlToRead);
            BufferedReader in = null;
            in = new BufferedReader(
                    new InputStreamReader(oracle.openStream()));
            String inputLine = "";
            while ((inputLine = in.readLine()) != null){
                progress++;
                //mProgressBar.setProgress(progress);
                result += inputLine;
            }
            in.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        //mProgressBar.setProgress(100);
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

            //celebNames.add(m.group(1).split("\\.")[1]);
            //Log.i("Name: ", m.group(1).split("\\.")[1]);
            celebNames.add(m.group(1));
            Log.i("Name: ", m.group(1));

        }

        cleanURLs(celebURLs);

        Log.i("URLS number: ", Integer.toString(celebURLs.size()));
        Log.i("Celebs number: ", Integer.toString(celebNames.size()));

        //pb.setVisibility(View.INVISIBLE);


    }

    public void cleanURLs (ArrayList<String> URLsToClean){
        for(int i=0; i<URLsToClean.size(); i++) {
            if (URLsToClean.get(i).split("nopin").length > 1 ) {
                URLsToClean.remove(i);
                i--;
            }
            else {
                Log.i("URL: ", URLsToClean.get(i));
            }
        }
    }
    */

/*
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
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

 */

}

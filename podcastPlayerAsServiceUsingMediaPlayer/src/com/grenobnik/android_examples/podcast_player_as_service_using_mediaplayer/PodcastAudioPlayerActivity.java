package com.grenobnik.android_examples.podcast_player_as_service_using_mediaplayer;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

/***
 * This example shows how to play an Internet audio stream in MediaPlayer on
 * background.
 * 
 * Since we want to play the audio even when the user leaves the activity, we
 * create a MediaPlayer instance using a Service. See the official Service guide
 * from http://developer.android.com/guide/components/services.html.
 * 
 * @author kvankov
 * 
 */

public class PodcastAudioPlayerActivity extends Activity {

    protected static final String TAG = "PodcastAudioPlayerActivity";
    private static final String podcastAudioSource1 = "http://vipicecast.yacast.net/bfm";
    private static final String podcastAudioSource2 = "http://95.81.147.3/rfimonde/all/rfimonde-64k.mp3";
    private static final String podcastAudioSource3 = "http://telechargement.rfi.fr.edgesuite.net/rfi/francais/audio/modules/languefrancaise/R166/Piaf_icone.mp3";//http://www.hubharp.com/web_sound/BachGavotteShort.mp3";

    // "http://www.tv-radio.com/station/france_inter_mp3/france_inter_mp3-128k.m3u";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);
	Log.d(TAG, "onCreate");
	setContentView(R.layout.activity_main);

	// Intent used for starting the PodcastAudioPlayerService
	final Intent podcastServiceIntent = new Intent(getApplicationContext(), PodcastAudioPlayerService.class);

	final Button startButton1 = (Button) findViewById(R.id.buttonStart1);
	startButton1.setOnClickListener(new OnClickListener() {
	    public void onClick(View src) {
		Log.d(TAG, "Start1 button clicked");
		// Set the audio source
		podcastServiceIntent.putExtra(PodcastAudioPlayerService.PODCAST_AUDIO_SOURCE, podcastAudioSource1);
		// Start the PodcastAudioPlayerService
		startService(podcastServiceIntent);
	    }
	});

	final Button startButton2 = (Button) findViewById(R.id.buttonStart2);
	startButton2.setOnClickListener(new OnClickListener() {
	    public void onClick(View src) {
		Log.d(TAG, "Start2 button clicked");
		// Set the audio source
		podcastServiceIntent.putExtra(PodcastAudioPlayerService.PODCAST_AUDIO_SOURCE, podcastAudioSource2);
		// Start the PodcastAudioPlayerService
		startService(podcastServiceIntent);
	    }
	});

	final Button startButton3 = (Button) findViewById(R.id.buttonStart3);
	startButton3.setOnClickListener(new OnClickListener() {
	    public void onClick(View src) {
		Log.d(TAG, "Start3 button clicked");
		// Set the audio source
		podcastServiceIntent.putExtra(PodcastAudioPlayerService.PODCAST_AUDIO_SOURCE, podcastAudioSource3);
		// Start the PodcastAudioPlayerService
		startService(podcastServiceIntent);
	    }
	});

	final Button stopButton = (Button) findViewById(R.id.buttonStop);
	stopButton.setOnClickListener(new OnClickListener() {
	    public void onClick(View src) {
		Log.d(TAG, "Stop button clicked");
		// Stop the PodcastAudioPlayerService using the Intent
		stopService(podcastServiceIntent);
	    }
	});

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
	// Inflate the menu; this adds items to the action bar if it is present.
	getMenuInflater().inflate(R.menu.main, menu);
	return true;
    }

}

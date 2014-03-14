package com.grenobnik.android_examples.podcast_player_as_service_using_mediaplayer;

import java.io.IOException;

import android.app.Notification;
import android.app.Notification.Builder;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiManager.WifiLock;
import android.os.IBinder;
import android.os.PowerManager;
import android.util.Log;

/***
 * We extend Service class following the official Service guide. The
 * IntentService class is not very suitable for us, because we may want to
 * control our playback,therefore allowing for asynchronous handling of events.
 * 
 * 1. Allow Internet and WakeLock permissions and to declare our service in
 * manifest file.
 * 
 * 2. In onStartCommand we set up a notification area icon to get access to the
 * main activity and declare the service as foreground. Then we start a new
 * thread for our service.
 * 
 * 3. Wrap all media player initiation into a single method play() for code
 * clarity. See the comments there.
 * 
 * Sources: http://developer.android.com/guide/topics/media/mediaplayer.html
 * http://developer.android.com/reference/android/media/MediaPlayer.html
 * http://developer.android.com/guide/components/services.html
 * http://developer.android.com/reference/android/app/Service.html
 * http://developer.android.com/training/managing-audio/audio-focus.html
 * 
 * @author kvankov
 * 
 */
public class PodcastAudioPlayerService extends Service implements //
	MediaPlayer.OnPreparedListener, //
	MediaPlayer.OnErrorListener, //
	MediaPlayer.OnCompletionListener, //
	AudioManager.OnAudioFocusChangeListener {

    protected static final String TAG = "PodcastAudioPlayerService";
    protected static final String PODCAST_AUDIO_SOURCE = "podcastAudioSource";
    private static final int NOTIFICATION_ID = 1;
    private static MediaPlayer mPlayer;
    private String podcastAudioSource;
    private WifiLock wifiLock;
    private AudioManager audioManager;

    @Override
    public IBinder onBind(Intent intent) {
	// We don't provide binding, so return null
	return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
	Log.d(TAG, "onStartCommand : startId = " + startId);
	if (mPlayer != null) {
	    // if mPlayer is not null, it is alive, we do not do anything
	    Log.d(TAG, "onStartCommand : attempt to start service when media player is already alive.");
	    // we may want to check if the new intent has a different audio
	    // stream source and start playing it
	    if (!intent.getStringExtra(PODCAST_AUDIO_SOURCE).equals(podcastAudioSource)) {
		podcastAudioSource = intent.getStringExtra(PODCAST_AUDIO_SOURCE);
		Log.d(TAG, "onStartCommand : starting a new stream.");
		play();
	    }
	} else {
	    // super.onStartCommand(intent, flags, startId);
	    podcastAudioSource = intent.getStringExtra(PODCAST_AUDIO_SOURCE);

	    // Create a notification area notification so the user can get back
	    // to the PodcastAudioPlayerActivity
	    // TODO build a better notification: need to display the name of
	    // currently playing stream, etc...
	    final Intent notificationIntent = new Intent(getApplicationContext(), PodcastAudioPlayerActivity.class);
	    notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
	    final Builder nb = new Notification.Builder(getApplicationContext());
	    nb.setSmallIcon(R.drawable.ic_launcher);
	    nb.setOngoing(true).setContentTitle("Playing podcast");
	    nb.setContentText("Click to access PodcastAudioPlayerActivity.");
	    nb.setContentIntent(PendingIntent.getActivity(this, 0, notificationIntent, 0));
	    final Notification notification = nb.build();

	    // Put this Service in a foreground state, the system will almost
	    // never kill it
	    startForeground(NOTIFICATION_ID, notification);

	    Log.d(TAG, "onStartCommand : initiating a new thread");
	    // Run media player on a separate thread
	    // TODO move thread creating into onCreate method
	    Thread t = new Thread() {
		public void run() {
		    play();
		}
	    };
	    t.start();
	}

	// Don't automatically restart this Service if it is killed.
	return START_NOT_STICKY;
    }

    /***
     * Instantiate the MediaPlayer. Set a WiFi and PowerManager locks to keep
     * the device from sleeping. Register several listeners attached to media
     * player, in particular: 1) onPreparedListener with onPrepared method for
     * asynchronous preparation. 2) onErrorListener with onError method for
     * tracking error states. 3) onCompletionListener with onCompletion method
     * for terminating the service when playback is finished. Next, set the
     * source and catch exceptions, call prepareAsync method. The palyback will
     * be started in onPrepared method.
     * 
     */
    private void play() {
	Log.d(TAG, "play");
	if (mPlayer == null) {
	    mPlayer = new MediaPlayer();
	    wifiLock = ((WifiManager) getSystemService(Context.WIFI_SERVICE)).createWifiLock(WifiManager.WIFI_MODE_FULL, "keep WiFi alive");
	    wifiLock.acquire();
	    mPlayer.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);
	} else {
	    mPlayer.reset();
	}
	mPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
	mPlayer.setOnPreparedListener(this);
	mPlayer.setOnErrorListener(this);
	mPlayer.setOnCompletionListener(this);
	try {
	    mPlayer.setDataSource(podcastAudioSource);
	} catch (IOException e) {
	    Log.e(TAG, "Could not open audio " + podcastAudioSource + " for playback.", e);
	}
	audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
	int result = audioManager.requestAudioFocus(this, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
	if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
	    mPlayer.prepareAsync();
	} else {
	    Log.e(TAG, "Could not get audio focus.");
	    stopSelf();
	}
    }

    /***
     * This method belongs to Service framework. It is called when the service
     * is about to terminate. Here we release the resources associated to the
     * mPlayer.
     */
    @Override
    public void onDestroy() {
	// stopForeground(true); not necessary
	wifiLock.release();
	audioManager.abandonAudioFocus(this);
	if (mPlayer != null) {
	    mPlayer.release();
	    mPlayer = null;
	}
	// Thread.currentThread().interrupt();
	Log.d(TAG, "onDestroy : PodcastAudioPlayerService terminanted");
    }

    // MediaPlayer methods =================================================

    /***
     * This method belongs to the MediaPlayer framework. It is called when the
     * media player is ready to play after prepareAsync() call. The media player
     * has to be registered with an OnPreparedListener.
     */
    @Override
    public void onPrepared(MediaPlayer mp) {
	mp.start();
    }

    /***
     * This method belongs to the MediaPlayer framework. It is called when the
     * media player goes into an error state. The media player has to be
     * registered with an OnErrorListener.
     */
    @Override
    public boolean onError(MediaPlayer arg0, int what, int extra) {
	Log.d(TAG, "onError : mPlayer error " + what + " - " + extra);
	// terminate the service in case of an error
	stopSelf();
	return true;
    }

    /***
     * This method belongs to the MediaPlayer framework. It is called when the
     * media finishes to play the source. The media player has to be registered
     * with an OnCompletionListener.
     */
    @Override
    public void onCompletion(MediaPlayer mp) {
	Log.d(TAG, "onCompletion : finished playing, terminating the service...");
	// terminate the service when finished playing
	stopSelf();
    }

    /***
     * We need to handle audio focus. See
     * http://developer.android.com/guide/topics/media/mediaplayer.html
     */
    @Override
    public void onAudioFocusChange(int focusChange) {
	switch (focusChange) {
	case AudioManager.AUDIOFOCUS_GAIN:
	    Log.d(TAG, "onAudioFocusChange : AUDIOFOCUS_GAIN");
	    // resume playback if possible
	    if (mPlayer == null)
		stopSelf();
	    else if (!mPlayer.isPlaying())
		mPlayer.start();
	    mPlayer.setVolume(1.0f, 1.0f);
	    break;

	case AudioManager.AUDIOFOCUS_LOSS:
	    Log.d(TAG, "onAudioFocusChange : AUDIOFOCUS_LOSS");
	    // Lost focus for an unbounded amount of time: terminate service
	    stopSelf();
	    break;

	case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
	    Log.d(TAG, "onAudioFocusChange : AUDIOFOCUS_LOSS_TRANSIENT");
	    // Lost focus for a short time, but we have to stop playback. We
	    // don't release the media player because playback is likely to
	    // resume
	    if (mPlayer != null && mPlayer.isPlaying())
		mPlayer.pause();
	    break;

	case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
	    Log.d(TAG, "onAudioFocusChange : AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK");
	    // Lost focus for a short time, but it's ok to keep playing at an
	    // attenuated level
	    if (mPlayer != null && mPlayer.isPlaying())
		mPlayer.setVolume(0.1f, 0.1f);
	    break;
	}
    }
}

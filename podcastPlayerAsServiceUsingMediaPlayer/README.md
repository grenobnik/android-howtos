This project shows how to play an Internet audio stream in background not blocking its initialising thread. The audio plays in a non-blocking fashion so a user can continue to use the device of switch the screen off.

The example contains the main activity with a simple layout with four buttons: three choices of audio sources and `Stop` button.

To play the audio the main activity starts a Service containing an instance of MediaPlayer. The Service is declared to be foreground so a user can have an easy access to the main activity through notifications area icon.

Features:
=========

* Choice of an audio stream and possibility to switch from one to another.
* Pausing playback in case of an interruption (such as phone call).
* Locking power manager to keep CPU active.
* Locking power manager to keep WiFi active.
* Release locks and resources after playback completion.

References:
===========

* [API Guide: Services](http://developer.android.com/guide/components/services.html)
* [API Guide: Media Playback](http://developer.android.com/guide/topics/media/mediaplayer.html)
* [Reference: MediaPlayer](http://developer.android.com/reference/android/media/MediaPlayer.html)
* [Reference: Service](http://developer.android.com/reference/android/app/Service.html)
* [Training: Managing Audio Focus](http://developer.android.com/training/managing-audio/audio-focus.html)

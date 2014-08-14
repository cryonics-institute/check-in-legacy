package util;

import java.util.HashMap;

import android.content.Context;
import android.media.AudioManager;
import android.media.SoundPool;
import android.util.Log;

import com.areyouok.R;

public class AlarmSounds {
	private static SoundPool mSoundPool;
	private static HashMap<Integer, Integer> mSoundPoolMap;
	private static Context mContext;
	private static AudioManager mAudioManager;
	private static int mLastStreamID;
	
	public static void init(Context context) {
		mContext = context;
		mSoundPool = new SoundPool(3, AudioManager.STREAM_ALARM, 0);
		mSoundPoolMap = new HashMap<Integer, Integer>();
		mAudioManager = (AudioManager)mContext.getSystemService(Context.AUDIO_SERVICE);
		
		// preload and cache frequently used
		mSoundPoolMap.put(R.raw.alarm, mSoundPool.load(mContext, R.raw.alarm, 1));
	}
	
	/**
	 * Plays a sound once at ringer volume
	 * @param id R.raw.soundID
	 */
	public static void play(int id) {
		play(id, 0);
	}
	
	public static void play(int id, int loop) {
//		if(Prefs.getSoundEffectsOn() == false) return;
		
		final int resID = id;
		
		if(!mSoundPoolMap.containsKey(id)) {
			mSoundPoolMap.put(resID, mSoundPool.load(mContext, resID, 1));
		}
		
		final int max = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_ALARM);
		mAudioManager.setStreamVolume(AudioManager.STREAM_ALARM, max, 0);
		final float streamVolume = max;

		// longer sounds are not READY immediately
		int result = 0;
		int abortCount = 200;	
		do {
			result = mSoundPool.play(mSoundPoolMap.get(resID), streamVolume, streamVolume, 1, loop, 1f);
			if(result == 0) {
				try {
					Thread.sleep(2);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		} while(result == 0 && abortCount-- > 0); // aborts playing sound after X attempts/sleeps (this limit prevents blocking the UI thread too long)
		mLastStreamID = result;
	}
	
	public static void stop() {
		try {
			mSoundPool.stop(mLastStreamID);
		} catch (Exception e) {
			Log.w("AYO", "Can't stop stream");
		}
	}
}


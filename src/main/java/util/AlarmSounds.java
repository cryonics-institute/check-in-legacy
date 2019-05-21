package util;

import java.util.HashMap;

import android.content.Context;
import android.media.AudioManager;
import android.media.SoundPool;
import android.util.Log;

import com.cryonicsinstitute.R;

public class AlarmSounds {
	private static SoundPool mSoundPool;
	private static Context mContext;
	private static AudioManager mAudioManager;
	private static int mLastStreamID;
	
	public static void init(Context context) {
		mContext = context;

		mAudioManager = (AudioManager)mContext.getSystemService(Context.AUDIO_SERVICE);
	}
	
	/**
	 * Plays a sound once at ringer volume
     * NOTE: looping is broken for SoundPool on 4.3+ so we have to manually call this on repeat
	 * @param id R.raw.soundID
	 */
	public static void play(int id) {
		final int resID = id;

		if (mSoundPool != null) {
			mSoundPool.release();
		}
		mSoundPool = new SoundPool(3, AudioManager.STREAM_ALARM, 0);

		int sound = mSoundPool.load(mContext, resID, 1);

		final int max = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_ALARM);
		final float streamVolume = max;
//		final float streamVolume = 0.05f;//max;

		// longer sounds are not READY immediately
		int result = 0;
		int abortCount = 200;	
		do {
			mSoundPool.stop(mLastStreamID);
			result = mSoundPool.play(sound, streamVolume, streamVolume, 1, 0, 1f);
			mAudioManager.setStreamVolume(AudioManager.STREAM_ALARM, max, 0);
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


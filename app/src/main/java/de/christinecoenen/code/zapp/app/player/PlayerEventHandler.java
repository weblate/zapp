package de.christinecoenen.code.zapp.app.player;

import android.view.Surface;

import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.analytics.AnalyticsListener;
import com.google.android.exoplayer2.source.MediaSourceEventListener;
import com.google.android.exoplayer2.upstream.HttpDataSource;

import java.io.IOException;

import androidx.annotation.Nullable;
import de.christinecoenen.code.zapp.R;
import io.reactivex.subjects.BehaviorSubject;
import timber.log.Timber;

/**
 * Transforms player events from exo player into RXJava observables.
 */
class PlayerEventHandler implements AnalyticsListener {

	private final BehaviorSubject<Boolean> isBufferingSource = BehaviorSubject.create();
	private final BehaviorSubject<Integer> errorResourceIdSource = BehaviorSubject.create();
	private final BehaviorSubject<Boolean> shouldHoldWakelockSource = BehaviorSubject.create();

	BehaviorSubject<Boolean> isBuffering() {
		return isBufferingSource;
	}

	BehaviorSubject<Integer> getErrorResourceId() {
		return errorResourceIdSource;
	}

	/**
	 * @return emits true when the player is playing or buffering and false
	 * if it is idle, paused or stopped
	 */
	BehaviorSubject<Boolean> getShouldHoldWakelock() {
		return shouldHoldWakelockSource;
	}

	@Override
	public void onRenderedFirstFrame(EventTime eventTime, @Nullable Surface surface) {
		Timber.d("onRenderedFirstFrame");
		// this is called when the video is really fully loaded
		// TODO: when does the loading begin?
	}

	@Override
	public void onPlayerStateChanged(EventTime eventTime, boolean playWhenReady, int playbackState) {
		// TODO: this reports videos as not buffering when it is
		boolean isBuffering = playbackState == Player.STATE_BUFFERING;
		Timber.d("isBuffering: %s", isBuffering);
		isBufferingSource.onNext(isBuffering);

		boolean shouldHoldWakelock = playWhenReady &&
			(playbackState == Player.STATE_BUFFERING || playbackState == Player.STATE_READY);
		shouldHoldWakelockSource.onNext(shouldHoldWakelock);
	}

	@Override
	public void onPlayerError(EventTime eventTime, ExoPlaybackException error) {
		int errorMessageResourceId = R.string.error_stream_unknown;

		switch (error.type) {
			case ExoPlaybackException.TYPE_SOURCE:
				Timber.e(error, "exo player error TYPE_SOURCE");
				errorMessageResourceId = R.string.error_stream_io;
				break;
			case ExoPlaybackException.TYPE_RENDERER:
				Timber.e(error, "exo player error TYPE_RENDERER");
				errorMessageResourceId = R.string.error_stream_unsupported;
				break;
			case ExoPlaybackException.TYPE_UNEXPECTED:
				Timber.e(error, "exo player error TYPE_UNEXPECTED");
				break;
		}

		errorResourceIdSource.onNext(errorMessageResourceId);
	}

	@Override
	public void onLoadError(EventTime eventTime, MediaSourceEventListener.LoadEventInfo loadEventInfo, MediaSourceEventListener.MediaLoadData mediaLoadData, IOException error, boolean wasCanceled) {
		// TODO: test with invalid stream url
		if (wasCanceled) {
			return;
		}

		Timber.e(error, "exo player onLoadError");

		if (error instanceof HttpDataSource.HttpDataSourceException) {
			errorResourceIdSource.onNext(R.string.error_stream_io);
		} else {
			errorResourceIdSource.onNext(R.string.error_stream_unknown);
		}
	}
}

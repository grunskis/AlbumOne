package com.grunskis.albumone.albums;

import android.os.Bundle;

import com.google.android.gms.analytics.HitBuilders;
import com.grunskis.albumone.BuildConfig;
import com.grunskis.albumone.R;
import com.grunskis.albumone.data.source.remote.UnsplashDataSource;

public class UnsplashAlbumsActivity extends RemoteAlbumsActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setTitle(getString(R.string.backend_unsplash));

        mRemoteDataSource = UnsplashDataSource.getInstance(this);
        if (!mRemoteDataSource.isAuthenticated()) {
            // TODO: 4/4/2018 implement oauth
            mRemoteDataSource.setAuthToken(BuildConfig.UNSPLASH_CLIENT_ID);
        }

        loadAlbums(savedInstanceState == null);
    }

    @Override
    protected void onResume() {
        super.onResume();

        // track unsplash usage
        mAnalyticsTracker.setScreenName(getString(R.string.backend_unsplash));
        mAnalyticsTracker.send(new HitBuilders.ScreenViewBuilder().build());
    }
}

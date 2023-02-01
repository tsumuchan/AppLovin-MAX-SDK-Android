package com.applovin.enterprise.apps.demoapp.ads.max.nativead;

import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;

import com.adjust.sdk.Adjust;
import com.adjust.sdk.AdjustAdRevenue;
import com.adjust.sdk.AdjustConfig;
import com.applovin.enterprise.apps.demoapp.R;
import com.applovin.impl.mediation.model.MediatedNativeAd;
import com.applovin.enterprise.apps.demoapp.ui.BaseAdActivity;
import com.applovin.mediation.MaxAd;
import com.applovin.mediation.MaxError;
import com.applovin.mediation.nativeAds.MaxNativeAd;
import com.applovin.mediation.nativeAds.MaxNativeAdListener;
import com.applovin.mediation.nativeAds.MaxNativeAdLoader;
import com.applovin.mediation.nativeAds.MaxNativeAdView;
import com.applovin.mediation.nativeAds.MaxNativeAdViewBinder;

import androidx.annotation.Nullable;

public class ManualNativeAdActivity
        extends BaseAdActivity
{
    private MaxNativeAdLoader nativeAdLoader;
    private FrameLayout       nativeAdLayout;
    private MaxNativeAdView   nativeAdView;

    private MaxAd nativeAd;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_native_manual );
        setTitle( R.string.activity_manual_native_ad );

        nativeAdLayout = findViewById( R.id.native_ad_layout );
        setupCallbacksRecyclerView();

        MaxNativeAdViewBinder binder = new MaxNativeAdViewBinder.Builder( R.layout.native_custom_ad_view )
                .setTitleTextViewId( R.id.title_text_view )
                .setBodyTextViewId( R.id.body_text_view )
                .setAdvertiserTextViewId( R.id.advertiser_text_view )
                .setIconImageViewId( R.id.icon_image_view )
                .setMediaContentViewGroupId( R.id.media_view_container )
                .setOptionsContentViewGroupId( R.id.options_view )
                .setStarRatingContentViewGroupId( R.id.applovin_native_star_rating_view)    //is this the same as star_rating_view
                .setCallToActionButtonId( R.id.cta_button )
                .build();
        nativeAdView = new MaxNativeAdView( binder, this );

        nativeAdLoader = new MaxNativeAdLoader( "79b382e8609988b1", this );
        nativeAdLoader.setRevenueListener( ad -> {
            logAnonymousCallback();

            AdjustAdRevenue adjustAdRevenue = new AdjustAdRevenue( AdjustConfig.AD_REVENUE_APPLOVIN_MAX );
            adjustAdRevenue.setRevenue( ad.getRevenue(), "USD" );
            adjustAdRevenue.setAdRevenueNetwork( ad.getNetworkName() );
            adjustAdRevenue.setAdRevenueUnit( ad.getAdUnitId() );
            adjustAdRevenue.setAdRevenuePlacement( ad.getPlacement() );

            Adjust.trackAdRevenue( adjustAdRevenue );
        } );
        nativeAdLoader.setNativeAdListener( new MaxNativeAdListener()
        {
            @Override
            public void onNativeAdLoaded(@Nullable final MaxNativeAdView nativeAdView, final MaxAd ad)
            {
                logAnonymousCallback();

                // Cleanup any pre-existing native ad to prevent memory leaks.
                if ( nativeAd != null )
                {
                    nativeAdLoader.destroy( nativeAd );
                }

                // Save ad for cleanup.
                nativeAd = ad;


                MaxNativeAd mediatedNativeAd = ad.getNativeAd();
                if ( mediatedNativeAd != null && mediatedNativeAd.getStarRating() == null )
                {
                    nativeAdView.getStarRatingContentViewGroup().setVisibility( View.GONE );
                }

                // Add ad view to view.
                nativeAdLayout.removeAllViews();
                nativeAdLayout.addView( nativeAdView );
            }

            @Override
            public void onNativeAdLoadFailed(final String adUnitId, final MaxError error)
            {
                logAnonymousCallback();
            }

            @Override
            public void onNativeAdClicked(final MaxAd ad)
            {
                logAnonymousCallback();
            }

            @Override
            public void onNativeAdExpired(final MaxAd ad)
            {
                logAnonymousCallback();
            }
        } );
    }

    @Override
    protected void onDestroy()
    {
        // Must destroy native ad or else there will be memory leaks.
        if ( nativeAd != null )
        {
            // Call destroy on the native ad from any native ad loader.
            nativeAdLoader.destroy( nativeAd );
        }

        // Destroy the actual loader itself
        nativeAdLoader.destroy();

        super.onDestroy();
    }

    public void onShowAdClicked(View view)
    {
        nativeAdLoader.loadAd( nativeAdView );
    }
}

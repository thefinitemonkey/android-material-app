package com.example.xyzreader.ui;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.LoaderManager;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v13.app.FragmentStatePagerAdapter;
import android.support.v4.app.ShareCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.Toolbar;
import android.text.format.DateUtils;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowInsets;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.example.xyzreader.R;
import com.example.xyzreader.data.ArticleLoader;
import com.example.xyzreader.data.ItemsContract;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * An activity representing a single Article detail screen, letting you swipe between articles.
 */
public class ArticleDetailActivity extends ActionBarActivity
        implements LoaderManager.LoaderCallbacks<Cursor> {

    @BindView(R.id.ct_details_toolbar_layout)
    CollapsingToolbarLayout mCTLayout;
    @BindView(R.id.iv_toolbar_backdrop)
    ImageView mPhotoView;
    @BindView(R.id.tv_article_body)
    TextView mArticleBody;
    @BindView(R.id.tv_author_name)
    TextView mAuthorName;
    @BindView(R.id.tv_publish_year)
    TextView mPublishYear;

    private Activity mActivity = this;
    private Cursor mCursor;
    private long mStartId;

    private long mSelectedItemId;

    private SimpleDateFormat outputFormat = new SimpleDateFormat();
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.sss");
    private GregorianCalendar START_OF_EPOCH = new GregorianCalendar(2, 1, 1);


    /*
    private int mTopInset;
    private int mSelectedItemUpButtonFloor = Integer.MAX_VALUE;

    private ViewPager mPager;
    private MyPagerAdapter mPagerAdapter;
    private View mUpButtonContainer;
    private View mUpButton;
    */


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN |
                            View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
        }
        setContentView(R.layout.activity_article_detail);

        ButterKnife.bind(this);

        final Toolbar toolbar = (Toolbar) findViewById(R.id.details_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        /*
        mPagerAdapter = new MyPagerAdapter(getFragmentManager());
        mPager = (ViewPager) findViewById(R.id.pager);
        mPager.setAdapter(mPagerAdapter);
        mPager.setPageMargin((int) TypedValue
                .applyDimension(
                        TypedValue.COMPLEX_UNIT_DIP, 1, getResources().getDisplayMetrics()));
        mPager.setPageMarginDrawable(new ColorDrawable(0x22000000));

        mPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageScrollStateChanged(int state) {
                super.onPageScrollStateChanged(state);
                mUpButton.animate()
                        .alpha((state == ViewPager.SCROLL_STATE_IDLE) ? 1f : 0f)
                        .setDuration(300);
            }

            @Override
            public void onPageSelected(int position) {
                if (mCursor != null) {
                    mCursor.moveToPosition(position);
                }
                mSelectedItemId = mCursor.getLong(ArticleLoader.Query._ID);
                //updateUpButtonPosition();
            }
        });
        */

        findViewById(R.id.share_fab).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(Intent.createChooser(
                        ShareCompat.IntentBuilder.from(mActivity)
                                .setType("text/plain")
                                .setText("Some sample text")
                                .getIntent(), getString(R.string.action_share)));
            }
        });


        /*
        mUpButtonContainer = findViewById(R.id.up_container);

        mUpButton = findViewById(R.id.action_up);
        mUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onSupportNavigateUp();
            }
        });

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mUpButtonContainer.setOnApplyWindowInsetsListener(new View.OnApplyWindowInsetsListener() {
                @Override
                public WindowInsets onApplyWindowInsets(View view, WindowInsets windowInsets) {
                    view.onApplyWindowInsets(windowInsets);
                    mTopInset = windowInsets.getSystemWindowInsetTop();
                    mUpButtonContainer.setTranslationY(mTopInset);
                    updateUpButtonPosition();
                    return windowInsets;
                }
            });
        }
        */

        if (savedInstanceState == null) {
            if (getIntent() != null && getIntent().getData() != null) {
                mStartId = ItemsContract.Items.getItemId(getIntent().getData());
                mSelectedItemId = mStartId;
            }
        }

        getLoaderManager().initLoader(0, null, this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        //return ArticleLoader.newAllArticlesInstance(this);
        return ArticleLoader.newInstanceForItemId(this, mSelectedItemId);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        mCursor = cursor;

        if (mCursor == null || mCursor.getCount() == 0) {
            mArticleBody.setText("N/A");
            mCTLayout.setTitle("N/A");
            return;
        }

        mCursor.moveToFirst();

        // Load up the display for the selected article
        String articleText = mCursor.getString(ArticleLoader.Query.BODY);
        mArticleBody.setText(articleText);

        String articleTitle = mCursor.getString(ArticleLoader.Query.TITLE);
        mCTLayout.setTitle(articleTitle);

        String photoUrl = mCursor.getString(ArticleLoader.Query.PHOTO_URL);

        mAuthorName.setText(mCursor.getString(ArticleLoader.Query.AUTHOR));

        Date publishedDate = parsePublishedDate();
        String dateString;
        if (!publishedDate.before(START_OF_EPOCH.getTime())) {
                        dateString = DateUtils.getRelativeTimeSpanString(
                                publishedDate.getTime(),
                                System.currentTimeMillis(), DateUtils.HOUR_IN_MILLIS,
                                DateUtils.FORMAT_ABBREV_ALL).toString();

        } else {
            // If date is before 1902, just show the string
            dateString = outputFormat.format(publishedDate);
        }
        mPublishYear.setText(dateString);

        ImageLoaderHelper.getInstance(this).getImageLoader()
                .get(
                        photoUrl,
                        new ImageLoader.ImageListener() {
                            @Override
                            public void onResponse(ImageLoader.ImageContainer imageContainer, boolean b) {
                                Bitmap bitmap = imageContainer.getBitmap();
                                if (bitmap != null) {
                                    Log.i("============>>>", "onResponse: loading bitmap");
                                    mPhotoView.setImageBitmap(bitmap);
                                }
                            }

                            @Override
                            public void onErrorResponse(VolleyError volleyError) {

                            }
                        }
                );

        /*
        //mPagerAdapter.notifyDataSetChanged();

        // Select the start ID
        if (mStartId > 0) {
            mCursor.moveToFirst();
            // TODO: optimize
            while (!mCursor.isAfterLast()) {
                if (mCursor.getLong(ArticleLoader.Query._ID) == mStartId) {
                    final int position = mCursor.getPosition();
                    //mPager.setCurrentItem(position, false);
                    break;
                }
                mCursor.moveToNext();
            }
            mStartId = 0;
        }
        */
    }

    /*
    private Date parsePublishedDate() {
        try {
            String date = mCursor.getString(ArticleLoader.Query.PUBLISHED_DATE);
            return dateFormat.parse(date);
        } catch (ParseException ex) {
            Log.e(TAG, ex.getMessage());
            Log.i(TAG, "passing today's date");
            return new Date();
        }
    }
    */

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {
        mCursor = null;
        //mPagerAdapter.notifyDataSetChanged();
    }

    /*
    public void onUpButtonFloorChanged(long itemId, ArticleDetailFragment fragment) {
        if (itemId == mSelectedItemId) {
            //mSelectedItemUpButtonFloor = fragment.getUpButtonFloor();
            //updateUpButtonPosition();
        }
    }

    private void updateUpButtonPosition() {
        //int upButtonNormalBottom = mTopInset + mUpButton.getHeight();
        //mUpButton.setTranslationY(Math.min(mSelectedItemUpButtonFloor - upButtonNormalBottom, 0));
    }

    private class MyPagerAdapter extends FragmentStatePagerAdapter {
        public MyPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public void setPrimaryItem(ViewGroup container, int position, Object object) {
            super.setPrimaryItem(container, position, object);
            // Set the article title
            if (mCursor != null) {

            }

            ArticleDetailFragment fragment = (ArticleDetailFragment) object;
            if (fragment != null) {
                //mSelectedItemUpButtonFloor = fragment.getUpButtonFloor();
                //updateUpButtonPosition();
                Log.i(
                        "============>>>",
                        "setPrimaryItem: opening line = " + fragment.getOpeningLine()
                );
            }
        }

        @Override
        public Fragment getItem(int position) {
            mCursor.moveToPosition(position);
            return ArticleDetailFragment.newInstance(mCursor.getLong(ArticleLoader.Query._ID));
        }

        @Override
        public int getCount() {
            return (mCursor != null) ? mCursor.getCount() : 0;
        }
    }
    */

    private Date parsePublishedDate() {
        try {
            String date = mCursor.getString(ArticleLoader.Query.PUBLISHED_DATE);
            return dateFormat.parse(date);
        } catch (ParseException ex) {
            //Log.e(TAG, ex.getMessage());
            //Log.i(TAG, "passing today's date");
            return new Date();
        }
    }

}

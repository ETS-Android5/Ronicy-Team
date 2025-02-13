package com.adeasy.advertise.ui.home;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;
import androidx.paging.PagedList;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.LinearSnapHelper;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AbsListView;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.adeasy.advertise.R;
import com.adeasy.advertise.adapter.RecyclerAdapterPublicFeed;
import com.adeasy.advertise.adapter.RecyclerAdapterPublicFeedHorizontal;
import com.adeasy.advertise.callback.AdvertisementCallback;
import com.adeasy.advertise.callback.AdvertismentSearchCallback;
import com.adeasy.advertise.callback.PromotionCallback;
import com.adeasy.advertise.helper.EndlessScrollListener;
import com.adeasy.advertise.helper.ViewHolderAdds;
import com.adeasy.advertise.manager.AdvertisementManager;
import com.adeasy.advertise.manager.PromotionManager;
import com.adeasy.advertise.model.Advertisement;
import com.adeasy.advertise.model.ApprovedPromotions;
import com.adeasy.advertise.model.Category;
import com.adeasy.advertise.model.Promotion;
import com.adeasy.advertise.model.TopAds;
import com.adeasy.advertise.search_manager.AdvertismentSearchManager;
import com.adeasy.advertise.ui.advertisement.CategoryPicker;
import com.adeasy.advertise.ui.advertisement.FilterSearchResult;
import com.adeasy.advertise.ui.advertisement.HomeAdSearch;
import com.adeasy.advertise.ui.advertisement.LocationPicker;
import com.adeasy.advertise.util.CustomDialogs;
import com.adeasy.advertise.util.InternetValidation;
import com.firebase.ui.firestore.paging.FirestorePagingAdapter;
import com.firebase.ui.firestore.paging.FirestorePagingOptions;
import com.github.javafaker.App;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Transformation;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import static android.widget.AbsListView.OnScrollListener.SCROLL_STATE_IDLE;

/**
 * Created by Manuka yasas,
 * University Sliit
 * Email manukayasas99@gmail.com
 **/
public class Home extends Fragment implements AdvertisementCallback, AdvertismentSearchCallback, View.OnClickListener {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private static final String TAG = "Home";
    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    Toolbar toolbar;
    TextView adCountText, category_picker, location_picker;
    RecyclerView recyclerView, adMenuRecyclerHorizontalView;
    SwipeRefreshLayout mSwipeRefreshLayout;
    AdvertisementManager advertisementManager;
    String searchKey, location_selected;
    Category category_selected;
    AdvertismentSearchManager advertismentSearchManager;
    Switch aSwitch;
    List<String> search_ids;
    ImageView filters;
    FrameLayout frameLayout;
    CustomDialogs customDialogs;

    private static final String SEARCH_KEY = "search_key";
    private static final String CATEGORY_SELECTED = "category_selected";
    private static final String LOCATION_SELECTED = "location_selected";
    private static final int LOCATION_PICKER = 6512;
    private static final int CATEGORY_PICKER = 4662;
    private static final int FILTER_PICKER = 8825;

    public int ITEM_PER_AD_USED = 0;
    public static final int ITEM_PER_AD_1 = 8;
    public static final int ITEM_PER_AD_2 = 7;
    public static final int ITEM_PER_AD_3 = 11;

    public static final int TOP_AD_COUNT = 2;

    Query query;
    DocumentSnapshot lastDoc, lastDocTop;
    List<Object> objectList = new ArrayList<>();
    RecyclerAdapterPublicFeed recyclerAdapterPublicFeed;
    RecyclerAdapterPublicFeedHorizontal recyclerAdapterPublicFeedHorizontal;
    Query finalNewQuery;
    CardView cardViewHeader;
    boolean isHeaderHidden = false;
    LinearLayoutManager layoutManager;
    int scrollTime = 8000;
    ImageView spotlight;
    PromotionManager promotionManager;
    List<String> topAdIds;
    boolean isAllAdsLoaded = false;
    boolean loadingAdsTask = false;
    ProgressBar progressBar;
    LinearLayout adsFinishedView;
    TextView refreshAdsView;
    Boolean noResults = false;
    StaggeredGridLayoutManager staggeredGridLayoutManager;

    public Home() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment Home.
     */
    // TODO: Rename and change types and number of parameters
    public static Home newInstance(String param1, String param2) {
        Home fragment = new Home();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        mSwipeRefreshLayout = view.findViewById(R.id.swipeRefresh);
        adsFinishedView = view.findViewById(R.id.adsFinishedView);
        refreshAdsView = view.findViewById(R.id.refreshAds);
        cardViewHeader = view.findViewById(R.id.cardViewHeader);
        progressBar = view.findViewById(R.id.progressBar);
        spotlight = view.findViewById(R.id.spotlight);
        recyclerView = view.findViewById(R.id.adMenuRecyclerView);
        adMenuRecyclerHorizontalView = view.findViewById(R.id.adMenuRecyclerHorizontalView);
        aSwitch = view.findViewById(R.id.switchView);

        recyclerView.setNestedScrollingEnabled(false);
        adMenuRecyclerHorizontalView.setNestedScrollingEnabled(false);

        staggeredGridLayoutManager = new StaggeredGridLayoutManager(2, 1) {
            @Override
            public boolean canScrollHorizontally() {
                return false;
            }
        };

        layoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false);

        recyclerView.setLayoutManager(staggeredGridLayoutManager);
        adMenuRecyclerHorizontalView.setLayoutManager(layoutManager);

        //recyclerView.setHasFixedSize(false);

        customDialogs = new CustomDialogs(getActivity());

        advertisementManager = new AdvertisementManager(this);
        promotionManager = new PromotionManager();
        toolbar = (Toolbar) getActivity().findViewById(R.id.toolbar);
        adCountText = toolbar.findViewById(R.id.adResults);
        frameLayout = view.findViewById(R.id.frameLayout);
        category_picker = view.findViewById(R.id.category_picker);
        location_picker = view.findViewById(R.id.location_picker);
        filters = view.findViewById(R.id.filters);
        category_picker.setOnClickListener(this);
        location_picker.setOnClickListener(this);
        filters.setOnClickListener(this);
        refreshAdsView.setOnClickListener(this);

        advertismentSearchManager = new AdvertismentSearchManager(this);

        try {
            searchKey = getArguments().getString(SEARCH_KEY);
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            category_selected = (Category) getArguments().getSerializable(CATEGORY_SELECTED);
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            location_selected = getArguments().getString(LOCATION_SELECTED);
        } catch (Exception e) {
            e.printStackTrace();
        }

        toolbar.setSubtitle(getString(R.string.loading));

        if (category_selected != null) {
            toolbar.setTitle(category_selected.getName());
            category_picker.setText(category_selected.getName());
        }
        if (location_selected != null)
            location_picker.setText(location_selected);

        if (searchKey != null) {
            toolbar.setTitle(searchKey);
            advertismentSearchManager.searchAdsHome(searchKey);
        }

        query = advertisementManager.viewAddsHome(search_ids, category_selected, location_selected, aSwitch.isChecked());

        //new method
        recyclerAdapterPublicFeed = new RecyclerAdapterPublicFeed(getActivity());
        recyclerAdapterPublicFeedHorizontal = new RecyclerAdapterPublicFeedHorizontal(getActivity());

        recyclerView.setAdapter(recyclerAdapterPublicFeed);
        adMenuRecyclerHorizontalView.setAdapter(recyclerAdapterPublicFeedHorizontal);

        resetDataPublicFeed();
        loadDataPublicFeed();

        return view;
    }

    @Override
    public void onViewCreated(@NonNull final View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        aSwitch.setOnClickListener(this);

        NestedScrollView nestedSV = (NestedScrollView) view.findViewById(R.id.nestedScroll);

        if (nestedSV != null) {

            nestedSV.setOnScrollChangeListener(new NestedScrollView.OnScrollChangeListener() {
                @Override
                public void onScrollChange(NestedScrollView v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {

                    if (scrollY > 0) {
                        // Scrolling up
                        if (isHeaderHidden) {
//                            Animation aniSlide = AnimationUtils.loadAnimation(getActivity(), R.anim.moveup);
//                            cardViewHeader.startAnimation(aniSlide);
//                            cardViewHeader.setVisibility(View.GONE);
                        }

                    } else {
                        // User scrolls down
                        if (!isHeaderHidden) {
//                            Animation aniSlide = AnimationUtils.loadAnimation(getActivity(), R.anim.movedown);
//                            cardViewHeader.startAnimation(aniSlide);
//                            cardViewHeader.setVisibility(View.VISIBLE);
                        }
                    }

                    int visibleCount = staggeredGridLayoutManager.getChildCount();
                    int totalCount = staggeredGridLayoutManager.getItemCount();

                    if (scrollY > oldScrollY) {

                        if (!isHeaderHidden) {
                            Log.i(TAG, "Scroll DOWN");
                            Animation aniSlide = AnimationUtils.loadAnimation(getActivity(), R.anim.moveup);
                            cardViewHeader.startAnimation(aniSlide);
                            cardViewHeader.setVisibility(View.INVISIBLE);
                            isHeaderHidden = true;
                        }
                    }
                    if (scrollY < oldScrollY) {
                        if (isHeaderHidden) {
                            Log.i(TAG, "Scroll UP");
                            Animation aniSlide = AnimationUtils.loadAnimation(getActivity(), R.anim.movedown);
                            cardViewHeader.startAnimation(aniSlide);
                            cardViewHeader.setVisibility(View.VISIBLE);
                            isHeaderHidden = false;
                        }
                    }

                    if (scrollY == 0) {
                        //Log.i(TAG, "TOP SCROLL");
                    }

                    if (scrollY == (v.getChildAt(0).getMeasuredHeight() - v.getMeasuredHeight())) {
                        //Log.i(TAG, "BOTTOM SCROLL");
//                        mSwipeRefreshLayout.setRefreshing(true);
//                        loadTopAndRegularAds();
                        if (!loadingAdsTask)
                            loadTopAndRegularAds();
                    }
                }
            });

            mSwipeRefreshLayout.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                        mSwipeRefreshLayout.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    } else {
                        mSwipeRefreshLayout.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                    }

                    Rect rect = new Rect();
                    mSwipeRefreshLayout.getDrawingRect(rect);
                    mSwipeRefreshLayout.setProgressViewOffset(false, 0, (int) view.getResources().getDimension(R.dimen.refresher_offset_end));
                }
            });
        }


        //auto scroll in the top header ads

//The LinearSnapHelper will snap the center of the target child view to the center of the attached RecyclerView , it's optional if you want , you can use it
        final LinearSnapHelper linearSnapHelper = new LinearSnapHelper();
        linearSnapHelper.attachToRecyclerView(adMenuRecyclerHorizontalView);

        final Timer timer = new Timer();
        timer.schedule(new TimerTask() {

            @Override
            public void run() {

                if (layoutManager.findLastCompletelyVisibleItemPosition() < (recyclerAdapterPublicFeedHorizontal.getItemCount() - 1)) {

                    layoutManager.smoothScrollToPosition(adMenuRecyclerHorizontalView, new RecyclerView.State(), layoutManager.findLastCompletelyVisibleItemPosition() + 1);

                } else if (layoutManager.findLastCompletelyVisibleItemPosition() == (recyclerAdapterPublicFeedHorizontal.getItemCount() - 1)) {

                    layoutManager.smoothScrollToPosition(adMenuRecyclerHorizontalView, new RecyclerView.State(), 0);
                }
            }
        }, 0, scrollTime);
    }

    @Override
    public void onClick(View view) {
        if (view == category_picker)
            getActivity().startActivityForResult(new Intent(getActivity(), CategoryPicker.class), CATEGORY_PICKER);
        else if (view == location_picker)
            getActivity().startActivityForResult(new Intent(getActivity(), LocationPicker.class), LOCATION_PICKER);
        else if (view == filters)
            getActivity().startActivityForResult(new Intent(getActivity(), FilterSearchResult.class), FILTER_PICKER);
        else if (view == refreshAdsView)
            loadDataPublicFeed();
    }

    private void loadTopAndRegularAds() {

        loadingAdsTask = true;

        //load the top ads
        Log.d(TAG, "loading top ads");

        //get new top ads ids
        if (topAdIds == null || topAdIds.size() == 0)
            loadTopAds();

        //if there are top ads
        if (topAdIds != null && !isAllAdsLoaded && topAdIds.size() > 0) {

            Query topAdsQuery = advertisementManager.homeAdsByIds(topAdIds).limit(TOP_AD_COUNT);

            if (lastDocTop != null)
                topAdsQuery = topAdsQuery.startAfter(lastDocTop);

            topAdsQuery.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                    if (task.isSuccessful()) {
                        QuerySnapshot documentSnapshots = task.getResult();
                        if (documentSnapshots.size() > 0) {
                            lastDocTop = documentSnapshots.getDocuments().get(documentSnapshots.size() - 1);
                            objectList = new ArrayList<>();
                            objectList.addAll(task.getResult().toObjects(TopAds.class));
                            recyclerAdapterPublicFeed.setObjects(objectList);
                        } else {
                            lastDocTop = null;
                        }
                    } else
                        Log.i(TAG, task.getException().getMessage());

                    loadRegularAds();
                }
            });
        } else
            loadRegularAds();
    }

    private void loadRegularAds() {
        final int queryLimit = getQueryLimitVlueForRegularAds();
        finalNewQuery = query.limit(queryLimit);

        if (lastDoc == null)
            advertisementManager.getCount(query);
        else
            finalNewQuery = finalNewQuery.startAfter(lastDoc);

        finalNewQuery.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {

                if (task.isSuccessful() && finalNewQuery != null && task.getResult().getQuery().equals(finalNewQuery)) {
                    QuerySnapshot documentSnapshots = task.getResult();
                    if (documentSnapshots.size() > 0) {
                        lastDoc = documentSnapshots.getDocuments().get(documentSnapshots.size() - 1);
                        objectList = new ArrayList<>();
                        objectList.addAll(task.getResult().toObjects(Advertisement.class));
                        objectList.add(new AdRequest.Builder().build());
                        recyclerAdapterPublicFeed.setObjects(objectList);
                        isAllAdsLoaded = false;
                    } else {
                        isAllAdsLoaded = true;
                        progressBar.setVisibility(View.GONE);
                        if (!noResults)
                            adsFinishedView.setVisibility(View.VISIBLE);
                    }
                }
                ITEM_PER_AD_USED = queryLimit;
                loadingAdsTask = false;
            }
        });
    }

    private void loadSpotLightAds() {
        //first get the ad ids of spot light ads
        Query query = promotionManager.getAdIDsByPromotionTypeQuery(Promotion.SPOTLIGHT_AD);
        if (query != null) {
            query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                    Log.i(TAG, "task sent");
                    final List<String> ids = new ArrayList<>();
                    if (task.isSuccessful()) {

                        for (ApprovedPromotions promotion : task.getResult().toObjects(ApprovedPromotions.class)) {
                            Log.i(TAG, "task sent");
                            ids.add(promotion.getAdvertismentID());
                        }

                        if (ids != null && ids.size() > 0) {
                            //load the ads from the ad ids
                            advertisementManager.homeAdsByIds(ids).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                    if (task.isSuccessful()) {
                                        QuerySnapshot documentSnapshots = task.getResult();
                                        if (documentSnapshots.size() > 0) {
                                            spotlight.setVisibility(View.VISIBLE);
                                            recyclerAdapterPublicFeedHorizontal.resetObjects();
                                            recyclerAdapterPublicFeedHorizontal.setObjects(task.getResult().toObjects(Advertisement.class));
                                        } else
                                            spotlight.setVisibility(View.GONE);
                                    } else
                                        Log.i(TAG, task.getException().getMessage());
                                }
                            });
                        } else {
                            recyclerAdapterPublicFeedHorizontal.resetObjects();
                            spotlight.setVisibility(View.GONE);
                        }

                    } else
                        Log.i(TAG, task.getException().getMessage());

                }
            });
        }
    }

    private void loadTopAds() {
        Log.d(TAG, "loading top ");
        Query query = promotionManager.getAdIDsByPromotionTypeQuery(Promotion.TOP_AD);
        if (query != null) {
            query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                    Log.i(TAG, "task sent");
                    final List<String> ids = new ArrayList<>();
                    if (task.isSuccessful()) {
                        for (ApprovedPromotions promotion : task.getResult().toObjects(ApprovedPromotions.class)) {
                            ids.add(promotion.getAdvertismentID());
                        }
                        topAdIds = ids;
                    } else
                        Log.i(TAG, task.getException().getMessage());
                }
            });
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        if (!new InternetValidation().validateInternet(getActivity()))
            customDialogs.showNoInternetDialog();
        else {
            mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                @Override
                public void onRefresh() {
                    mSwipeRefreshLayout.setRefreshing(false);
                    loadDataPublicFeed();
                }
            });
        }

        aSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                query = advertisementManager.viewAddsHome(search_ids, category_selected, location_selected, b);
                loadDataPublicFeed();
            }
        });
    }

    private void resetDataPublicFeed() {
        if (recyclerAdapterPublicFeed != null) {
            try {
                adCountText.setText(" loading..");
            } catch (NullPointerException e) {
                Log.i(TAG, "fragments changed");
            }
            try {
                toolbar.setSubtitle("loading..");
            } catch (NullPointerException e) {
                Log.i(TAG, "fragments changed");
            }

            //reset last documents
            lastDoc = null;
            lastDocTop = null;

            //reset top ad list
            topAdIds = null;
            isAllAdsLoaded = false;

            //reset adapters
            recyclerAdapterPublicFeed.resetObjects();

            //remove spotlight image
            spotlight.setVisibility(View.GONE);

            //load data
            loadingAdsTask = false;

            //progress bar and ads finished view
            progressBar.setVisibility(View.VISIBLE);
            adsFinishedView.setVisibility(View.GONE);
        }

        if (recyclerAdapterPublicFeedHorizontal != null)
            recyclerAdapterPublicFeedHorizontal.resetObjects();
    }

    private void loadDataPublicFeed() {
        resetDataPublicFeed();
        loadSpotLightAds();
        loadTopAndRegularAds();
    }

    //Stop Listening Adapter
    @Override
    public void onStop() {
        super.onStop();
    }


    @Override
    public void onUploadImage(@NonNull Task<Uri> task) {

    }

    @Override
    public void onTaskFull(boolean result) {

    }

    @Override
    public void onCompleteInsertAd(Task<Void> task) {

    }

    @Override
    public void onCompleteDeleteAd(Task<Void> task) {

    }

    @Override
    public void onAdCount(Task<QuerySnapshot> task) {
        if (task.isSuccessful() && query != null && task.getResult().getQuery().equals(query)) {
            try {
                adCountText.setText(task.getResult().size() + " results");
            } catch (NullPointerException e) {
                Log.i(TAG, "fragments changed");
            }
            try {
                toolbar.setSubtitle(task.getResult().size() + " results");
            } catch (NullPointerException e) {
                Log.i(TAG, "fragments changed");
            }

            if (task.getResult().size() == 0) {
                noResults = true;
                getParentFragmentManager().beginTransaction().replace(frameLayout.getId(), new NoData()).commit();
                frameLayout.setVisibility(View.VISIBLE);
                adsFinishedView.setVisibility(View.GONE);

            } else
                frameLayout.setVisibility(View.GONE);
        } else {
            Log.d(TAG, "Error getting documents: ", task.getException());
        }
    }

    @Override
    public void getAdbyID(@NonNull Task<DocumentSnapshot> task) {

    }

    @Override
    public void onSuccessGetAllAdsByYear(Task<QuerySnapshot> task) {

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        advertisementManager.destroy();
    }

    @Override
    public void onSearchComplete(List<String> ids, List<Advertisement> advertisementList) {
        search_ids = ids;
        Log.i(TAG, "ids: " + ids);

        query = advertisementManager.viewAddsHome(ids, category_selected, location_selected, aSwitch.isChecked());
        loadDataPublicFeed();
    }

    private int getQueryLimitVlueForRegularAds() {
        if (ITEM_PER_AD_USED == ITEM_PER_AD_1)
            return ITEM_PER_AD_2;
        else if (ITEM_PER_AD_USED == ITEM_PER_AD_2)
            return ITEM_PER_AD_3;
        else
            return ITEM_PER_AD_1;
    }

}
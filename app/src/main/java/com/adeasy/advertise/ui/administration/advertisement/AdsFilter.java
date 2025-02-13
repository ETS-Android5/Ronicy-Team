package com.adeasy.advertise.ui.administration.advertisement;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.core.widget.NestedScrollView;
import androidx.paging.PagedList;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.adeasy.advertise.R;
import com.adeasy.advertise.callback.AdvertisementCallback;
import com.adeasy.advertise.helper.ViewHolderListAdds;
import com.adeasy.advertise.manager.AdvertisementManager;
import com.adeasy.advertise.model.Advertisement;
import com.adeasy.advertise.ui.editAd.EditAd;
import com.adeasy.advertise.util.CustomDialogs;
import com.firebase.ui.firestore.paging.FirestorePagingAdapter;
import com.firebase.ui.firestore.paging.FirestorePagingOptions;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.squareup.picasso.Picasso;

import static com.facebook.FacebookSdk.getApplicationContext;
import static com.google.firebase.firestore.FirebaseFirestoreException.Code.PERMISSION_DENIED;

/**
 * Created by Manuka yasas,
 * University Sliit
 * Email manukayasas99@gmail.com
 **/
public class AdsFilter extends AppCompatActivity implements AdvertisementCallback {

    String filterType, category, categoryName;
    AdvertisementManager advertisementManager;
    Query query;
    Context context;

    Toolbar toolbar;
    TextView filter_ads_text;
    SwipeRefreshLayout swipeRefreshLayout;
    RecyclerView recyclerView;
    FirestorePagingAdapter<Advertisement, ViewHolderListAdds> firestorePagingAdapter;
    FirestorePagingOptions<Advertisement> options;

    CardView noDataFragment;

    boolean isTrashDelete = false;

    private static final String TAG = "AdsFilter";
    CustomDialogs customDialogs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.manuka_activity_filter_ads);

        context = this;

        noDataFragment = findViewById(R.id.noDataFragment);
        filter_ads_text = findViewById(R.id.filter_ads_text);
        recyclerView = findViewById(R.id.myaddsRecycle);
        swipeRefreshLayout = findViewById(R.id.swipeRefreshMyadds);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        customDialogs = new CustomDialogs(this);

        try {
            filterType = getIntent().getStringExtra("filterType");
            category = getIntent().getStringExtra("category");
            categoryName = getIntent().getStringExtra("categoryName");
        } catch (Exception e) {
            e.printStackTrace();
        }
        advertisementManager = new AdvertisementManager(this);

        if (filterType != null && category != null)
            getQueryForFilters();

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Filter ads - " + filterType + " Ads");
        getSupportActionBar().setSubtitle(categoryName);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        loadData();

    }

    private void getQueryForFilters() {
        Log.i(TAG, "category =" + category);
        switch (category) {
            case "All":
                setQueryForAllCategories();
                break;
            default:
                setQueryForSelectedCategories();
                break;
        }
    }

    private void setQueryForAllCategories() {
        Log.i(TAG, "filtertype =" + filterType);
        switch (filterType) {
            case "live":
                query = advertisementManager.viewAdsFilterByAllCategory("availability", true);
                break;

            case "hidden":
                query = advertisementManager.viewAdsFilterByAllCategory("availability", false);
                break;

            case "buynow":
                query = advertisementManager.viewAdsFilterByAllCategory("buynow", true);
                break;

            case "trash":
                query = advertisementManager.viewAllAdsInTrash();
                filter_ads_text.setText(R.string.trash_ads);
                isTrashDelete = true;
                break;

            default:
                Log.i(TAG, "No filterTypeSelected");
                break;
        }
    }

    private void setQueryForSelectedCategories() {
        switch (filterType) {
            case "live":
                query = advertisementManager.viewAdsFilterByCategory("availability", true, category);
                break;

            case "hidden":
                query = advertisementManager.viewAdsFilterByCategory("availability", false, category);
                break;

            case "buynow":
                query = advertisementManager.viewAdsFilterByCategory("buynow", true, category);
                break;

            case "trash":
                query = advertisementManager.viewAdsInTrashByCategory(category);
                filter_ads_text.setText(R.string.trash_ads);
                isTrashDelete = true;
                break;

            default:
                Log.i(TAG, "No filterTypeSelected");
                break;
        }
    }

    public void loadData() {
        PagedList.Config config = new PagedList.Config.Builder()
                .setEnablePlaceholders(false)
                .setInitialLoadSizeHint(4)
                .setPageSize(3)
                .build();

        options = new FirestorePagingOptions.Builder<Advertisement>()
                .setLifecycleOwner(this)
                .setQuery(query, config, Advertisement.class)
                .build();

        firestorePagingAdapter =
                new FirestorePagingAdapter<Advertisement, ViewHolderListAdds>(
                        options
                ) {
                    @Override
                    protected void onBindViewHolder(@NonNull ViewHolderListAdds holder, final int position, @NonNull Advertisement advertisement) {
                        try {
                            holder.getMyadsTitle().setText(advertisement.getTitle());
                            holder.getMyadsPrice().setText("Rs " + advertisement.getPrice());
                            holder.getMyaddsDate().setText(advertisement.getPreetyTime());
                            Picasso.get().load(advertisement.getImageUrls().get(0)).fit().into(holder.getImageView());

                            if (!advertisement.isAvailability()) {
                                holder.getMyadsAprooved().setText("Not Available");
                                holder.getMyadsAprooved().setBackgroundResource(R.color.colorPrimary);
                                holder.getMyadsAprooved().setTextColor(ContextCompat.getColor(context, R.color.colorWhite));
                                holder.getMyaddsUaprovedReason().setVisibility(View.GONE);
                            } else if (advertisement.isApproved()) {
                                holder.getMyadsAprooved().setText("Live");
                                holder.getMyadsAprooved().setBackgroundResource(R.color.colorSucess);
                                holder.getMyadsAprooved().setTextColor(ContextCompat.getColor(context, R.color.colorBlack));
                                holder.getMyaddsUaprovedReason().setVisibility(View.GONE);
                            } else {
                                holder.getMyadsAprooved().setText("Not Approved");
                                holder.getMyadsAprooved().setBackgroundResource(R.color.colorDanger);
                                holder.getMyadsAprooved().setTextColor(ContextCompat.getColor(context, R.color.colorWhite));
                                if (advertisement.getUnapprovedReason() != null) {
                                    holder.getMyaddsUaprovedReason().setText(advertisement.getUnapprovedReason());
                                    holder.getMyaddsUaprovedReason().setVisibility(View.VISIBLE);
                                } else
                                    holder.getMyaddsUaprovedReason().setVisibility(View.GONE);
                            }


                            holder.itemView.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    AlertDialog.Builder alertDialog = new AlertDialog.Builder(context)
                                            .setIcon(R.drawable.ic_baseline_info_24_red)
                                            .setTitle("Ad - " + getItem(position).get("title"))
                                            .setMessage("Note any changes made cannot be revert. Select below and proceed.")
                                            .setNeutralButton("More", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialogInterface, int i) {
                                                    Intent intent = new Intent(context, MoreActionsOnAd.class);
                                                    intent.putExtra("isTrashDelete", isTrashDelete);
                                                    intent.putExtra("adID", getItem(position).get("id").toString());
                                                    intent.putExtra("adCID", getItem(position).get("categoryID").toString());
                                                    startActivity(intent);
                                                }
                                            })
                                            .setNegativeButton("Edit ad", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialogInterface, int i) {
                                                    Intent intent = new Intent(context, EditAd.class);
                                                    intent.putExtra("adID", getItem(position).get("id").toString());
                                                    intent.putExtra("adCID", getItem(position).get("categoryID").toString());
                                                    startActivity(intent);
                                                }
                                            });

                                    alertDialog.setPositiveButton("View ad", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            Intent intent = new Intent(context, com.adeasy.advertise.ui.advertisement.Advertisement.class);
                                            intent.putExtra("adID", getItem(position).getId());
                                            intent.putExtra("adCID", (String) getItem(position).get("categoryID"));
                                            startActivity(intent);
                                        }
                                    });
                                    alertDialog.show();
                                }
                            });
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                    @NonNull
                    @Override
                    public ViewHolderListAdds onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.manuka_ads_row_card, parent, false);
                        return new ViewHolderListAdds(view);
                    }

                    @Override
                    protected void onLoadingStateChanged(@NonNull com.firebase.ui.firestore.paging.LoadingState state) {
                        super.onLoadingStateChanged(state);
                        switch (state) {
                            case LOADING_INITIAL:
                                advertisementManager.getCount(query);
                            case LOADING_MORE:
                                // Do your loading animation
                                swipeRefreshLayout.setRefreshing(true);
                                break;

                            case LOADED:
                                // Stop Animation
                                swipeRefreshLayout.setRefreshing(false);
                                break;

                            case FINISHED:
                                swipeRefreshLayout.setRefreshing(false);
                                //Toast.makeText(getApplicationContext(), "last..fg", Toast.LENGTH_SHORT).show();
                                break;

                            case ERROR:
                                //retry();
                                break;
                        }
                    }

                    @Override
                    protected void onError(@NonNull Exception e) {
                        super.onError(e);
                        swipeRefreshLayout.setRefreshing(false);
                        e.printStackTrace();
                        //Handle Error
                        //refresh();
                        if (e instanceof FirebaseFirestoreException) {
                            ((FirebaseFirestoreException) e).getCode().equals(PERMISSION_DENIED);
                            customDialogs.showPermissionDeniedStorage();
                        }
                    }
                };

        firestorePagingAdapter.startListening();
        recyclerView.setAdapter(firestorePagingAdapter);

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                firestorePagingAdapter.refresh();
            }
        });
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
        if (task != null && task.isSuccessful() && task.getResult().size() > 0) {
            noDataFragment.setVisibility(View.GONE);
            getSupportActionBar().setSubtitle(categoryName + " - " + task.getResult().size() + " results");
        } else {
            noDataFragment.setVisibility(View.VISIBLE);
            getSupportActionBar().setSubtitle(categoryName + " - " + " no results");
        }
    }

    @Override
    public void getAdbyID(@NonNull Task<DocumentSnapshot> task) {

    }

    @Override
    public void onSuccessGetAllAdsByYear(Task<QuerySnapshot> task) {

    }
}
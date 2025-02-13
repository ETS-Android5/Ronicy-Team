package com.adeasy.advertise.manager;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;

import com.adeasy.advertise.callback.AdvertisementCallback;
import com.adeasy.advertise.model.Advertisement;
import com.adeasy.advertise.model.Category;
import com.adeasy.advertise.model.Promotion;
import com.adeasy.advertise.util.ImageQualityReducer;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Created by Manuka yasas,
 * University Sliit
 * Email manukayasas99@gmail.com
 **/
public class AdvertisementManager {

    private static final String TAG = "AdvertisementManager";
    private static final String childName = "Advertisement";
    private static final String childNameTrash = "TrashAdvertisement";
    private StorageTask storageTask;
    private FirebaseStorage firebaseStorage;
    private FirebaseFirestore firebaseFirestore;
    private DocumentReference documentReference;
    private StorageReference storageReference;
    private AdvertisementCallback advertisementCallback;
    private FirebaseAuth firebaseAuth;
    private static final String FIREBASE_HOST = "firebasestorage.googleapis.com";

    private List<String> newUploadedImagesUrls;
    private List<String> oldfirebaseUnusedImages; //after updating

    public AdvertisementManager() {
        firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseStorage = FirebaseStorage.getInstance();
        documentReference = firebaseFirestore.collection(childName).document();
        storageReference = firebaseStorage.getReference().child(childName).child("Images");
        firebaseAuth = FirebaseAuth.getInstance();
        newUploadedImagesUrls = new ArrayList<>();
    }

    public AdvertisementManager(AdvertisementCallback callBacks) {
        firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseStorage = FirebaseStorage.getInstance();
        documentReference = firebaseFirestore.collection(childName).document();
        storageReference = firebaseStorage.getReference().child(childName).child("Images");
        this.advertisementCallback = callBacks;
        firebaseAuth = FirebaseAuth.getInstance();
    }

    //insert an advertisement
    //second parametwer is to delete or nnot advertisement.getImageUrls()
    public void insertUpdateAdvertisement(final Advertisement advertisement) {
        DocumentReference refStore;
        if (advertisement.getId() == null) {
            String myId = documentReference.getId();
            advertisement.setId(myId);
            refStore = documentReference;
        } else
            refStore = firebaseFirestore.collection(childName).document(advertisement.getId());
        refStore.set(advertisement).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (advertisementCallback != null)
                    advertisementCallback.onCompleteInsertAd(task);

                //if it was failed to insert the ad
                if (!task.isSuccessful())
                    deleteMultipleImages(newUploadedImagesUrls);

                //if success fiull and delete images not null then it will delete (this may be an update)
                if (task.isSuccessful()) {
                    deleteMultipleImages(oldfirebaseUnusedImages);
                }
            }
        });
    }

    public void uploadImageMultiple(final Advertisement advertisement, final List<String> deletedImageUrls, Context context) {
        try {
            if (storageTask != null && storageTask.isInProgress()) {
                if (advertisementCallback != null)
                    advertisementCallback.onTaskFull(true);
            } else {
                newUploadedImagesUrls = new ArrayList<>();
                if (advertisementCallback != null)
                    advertisementCallback.onTaskFull(false);

                final List<String> imageUriList = new ArrayList<>();
                final Advertisement ad = advertisement;

                for (int i = 0; i < advertisement.getImageUrls().size(); ++i) {

                    final int counter = i;

                    Uri imageUri = Uri.parse(advertisement.getImageUrls().get(i));

                    //validate if uri is from firebase
                    if (imageUri.getHost().equals(FIREBASE_HOST)) {
                        //if from firebase dont upload them instead add them dierectly tio the list
                        imageUriList.add(advertisement.getImageUrls().get(i));
                        try {
                            advertisement.getImageUrls().get(counter + 1);
                        } catch (Exception e) {
                            ad.setImageUrls(imageUriList);
                            oldfirebaseUnusedImages = deletedImageUrls;
                            insertUpdateAdvertisement(advertisement);
                            e.printStackTrace();
                        }
                    } else {
                        //upload image
                        byte[] data = ImageQualityReducer.reduceQualityFromBitmap(imageUri, context);

                        String imageID = UUID.randomUUID().toString().replace("-", "");

                        Log.i(TAG, imageID);

                        final StorageReference ref = storageReference.child(imageID);
                        storageTask = ref.putBytes(data);

                        Task<Uri> urlTask = storageTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                            @Override
                            public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                                if (!task.isSuccessful()) {
                                    throw task.getException();
                                }
                                // Continue with the task to get the download URL
                                return ref.getDownloadUrl();
                            }
                        }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                            @Override
                            public void onComplete(@NonNull Task<Uri> task) {
                                if (task.isSuccessful()) {
                                    Uri downloadUri = task.getResult();
                                    imageUriList.add(downloadUri.toString());
                                    newUploadedImagesUrls.add(downloadUri.toString());
                                    //advertisement.getImageUrls().add(downloadUri.toString());
                                } else {
                                    // Handle failures
                                    // ...
                                }

                                try {
                                    advertisement.getImageUrls().get(counter + 1);
                                } catch (Exception e) {
                                    ad.setImageUrls(imageUriList);
                                    oldfirebaseUnusedImages = deletedImageUrls;
                                    insertUpdateAdvertisement(advertisement);
                                    e.printStackTrace();
                                }
                            }
                        });
                    }

                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //delete ad from advertisment collection
    private void deleteAddFromAdCollection(final Advertisement advertisement) {
        try {
            firebaseFirestore.collection(childName).document(advertisement.getId())
                    .delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (advertisementCallback != null)
                        advertisementCallback.onCompleteDeleteAd(task);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //delete ad from advertisment Trash collection
    public void deleteAddFromTrash(final Advertisement advertisement) {
        try {
            firebaseFirestore.collection(childNameTrash).document(advertisement.getId())
                    .delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (advertisementCallback != null)
                        advertisementCallback.onCompleteDeleteAd(task);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // insert an ad to trash collection
    public void moveAdToTrash(final Advertisement advertisement) {
        try {
            DocumentReference refStore = firebaseFirestore.collection(childNameTrash).document(advertisement.getId());
            refStore.set(advertisement).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    //if the ad was moved to trash colloection then delete the ad from the ad collection
                    if (task.isSuccessful())
                        deleteAddFromAdCollection(advertisement);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void hideAdd(String id, boolean visibility) {
        try {
            // Update one field
            Map<String, Object> data = new HashMap<>();
            data.put("availability", visibility);
            firebaseFirestore.collection(childName).document(id)
                    .set(data, SetOptions.merge()).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (advertisementCallback != null)
                        advertisementCallback.onCompleteInsertAd(task);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Query viewAddsHome(List<String> adIdsFromSearch, Category category, String location, boolean buyNow) {
        Query query = FirebaseFirestore.getInstance().collection(childName);

        if (category != null)
            query = query.whereEqualTo("categoryID", category.getId());

        if (location != null)
            query = query.whereEqualTo("location", location);

        if (adIdsFromSearch != null && adIdsFromSearch.size() > 0)
            query = query.whereIn("id", adIdsFromSearch);

        if (buyNow)
            query = query.whereEqualTo("buynow", buyNow);

        return query.whereEqualTo("availability", true).whereEqualTo("approved", true).orderBy("placedDate", Query.Direction.DESCENDING);
    }

    public Query homeAdsByIds(List<String> ids) {
        return FirebaseFirestore.getInstance().collection(childName).whereIn("id", ids).whereEqualTo("availability", true).whereEqualTo("approved", true).orderBy("placedDate", Query.Direction.DESCENDING);
    }

    public Query homeSimilarAds(String cid, List<String> promo_AdIDs, int limitValue) {
        if (promo_AdIDs != null && promo_AdIDs.size() > 0)
            return FirebaseFirestore.getInstance().collection(childName).whereIn("id", promo_AdIDs).whereEqualTo("categoryID", cid).whereEqualTo("availability", true).whereEqualTo("approved", true).limit(limitValue);

        else
            return FirebaseFirestore.getInstance().collection(childName).whereEqualTo("categoryID", cid).whereEqualTo("availability", true).whereEqualTo("approved", true).limit(limitValue);
    }

    public Query viewNotReviewedAdds() {
        return firebaseFirestore.collection(childName).whereEqualTo("reviewed", false).orderBy("placedDate", Query.Direction.DESCENDING);
    }

    public Query viewApprovedAdds() {
        return firebaseFirestore.collection(childName).whereEqualTo("approved", true).orderBy("placedDate", Query.Direction.DESCENDING);
    }

    public Query viewRejectedAdds() {
        return firebaseFirestore.collection(childName).whereEqualTo("approved", false).orderBy("placedDate", Query.Direction.DESCENDING);
    }

    public Query viewAdsFilterByCategory(String filterAttribute, boolean status, String categoryID) {
        return firebaseFirestore.collection(childName).whereEqualTo(filterAttribute, status).whereEqualTo("categoryID", categoryID).orderBy("placedDate", Query.Direction.DESCENDING);
    }

    public Query viewAdsFilterByAllCategory(String filterAttribute, boolean status) {
        return firebaseFirestore.collection(childName).whereEqualTo(filterAttribute, status).orderBy("placedDate", Query.Direction.DESCENDING);
    }

    public Query viewAllAdsInTrash() {
        return firebaseFirestore.collection(childNameTrash).orderBy("placedDate", Query.Direction.DESCENDING);
    }

    public Query viewAdsInTrashByCategory(String categoryID) {
        return firebaseFirestore.collection(childNameTrash).whereEqualTo("categoryID", categoryID).orderBy("placedDate", Query.Direction.DESCENDING);
    }

    //my ads
    public Query viewMyAllAds(String uid) {
        return firebaseFirestore.collection(childName).whereEqualTo("userID", uid).orderBy("placedDate", Query.Direction.DESCENDING);
    }

    public Query viewMyPublishedAddsAll(String uid) {
        return firebaseFirestore.collection(childName).whereEqualTo("userID", uid).whereEqualTo("reviewed", true).whereEqualTo("approved", true).orderBy("placedDate", Query.Direction.DESCENDING);
    }

    public Query getMyReviewingAds(String uid) {
        return firebaseFirestore.collection(childName).whereEqualTo("userID", uid).whereEqualTo("reviewed", false).orderBy("placedDate", Query.Direction.DESCENDING);
    }

    public Query getMyRejectedAds(String uid) {
        return firebaseFirestore.collection(childName).whereEqualTo("userID", uid).whereEqualTo("reviewed", true).whereEqualTo("approved", false).orderBy("placedDate", Query.Direction.DESCENDING);
    }

    //get an ad from the advertisement collection
    public void getAddbyID(String id) {
        try {
            firebaseFirestore.collection(childName).document(id).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (advertisementCallback != null)
                        advertisementCallback.getAdbyID(task);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            advertisementCallback.getAdbyID(null);
        }
    }

    //get ad from the advertisement Trash collection
    public void getAddFromRashbyID(String id) {
        try {
            firebaseFirestore.collection(childNameTrash).document(id).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (advertisementCallback != null)
                        advertisementCallback.getAdbyID(task);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //delete image from aa url of firebase store
    public void deleteImage(String url) {
        final StorageReference storageref = firebaseStorage.getReferenceFromUrl(url);
        storageref.delete().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {

            }
        });
    }

    //delete multiple images
    public void deleteMultipleImages(List<String> imageUrls) {
        if (imageUrls != null) {
            for (String url : imageUrls) {
                final StorageReference storageref = firebaseStorage.getReferenceFromUrl(url);
                storageref.delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {

                    }
                });
            }
        }
    }

    //get the total ad count in home
    public void getCount(Query query) {
        try {
            query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                    if (advertisementCallback != null)
                        advertisementCallback.onAdCount(task);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void getAllAdsByYear(int year) {
        //Date dateFrom = new Date(year, 1, 0); //  date from in string
        // Date dateTo = new Date(year + 1, 1, 0);  //  date to in string

        Date dateFrom = new Date("Tue Jan 00 " + year + " 00:00:00 GMT+0530 (IST)");// Any date in string
        Date dateTo = new Date("Tue Jan 00 " + (year + 1) + " 00:00:00 GMT+0530 (IST)");// Any date in string

        Log.i(TAG, "Date from: " + dateFrom + " , Date to: " + dateTo);

        try {
            firebaseFirestore.collection(childName).whereGreaterThanOrEqualTo("placedDate", dateFrom).whereLessThan("placedDate", dateTo).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                    if (advertisementCallback != null)
                        advertisementCallback.onSuccessGetAllAdsByYear(task);
                }
            });
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }

    public void destroy() {
        firebaseFirestore = null;
        firebaseStorage = null;
        documentReference = null;
        storageReference = null;
        advertisementCallback = null;
    }

}
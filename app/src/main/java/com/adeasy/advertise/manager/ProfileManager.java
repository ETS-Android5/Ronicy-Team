package com.adeasy.advertise.manager;

import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;

import com.adeasy.advertise.callback.ProfileManagerCallback;
import com.adeasy.advertise.model.Advertisement;
import com.adeasy.advertise.model.User;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Created by Manuka yasas,
 * University Sliit
 * Email manukayasas99@gmail.com
 **/
public class ProfileManager {

    private static final String TAG = "ProfileManager";
    private static final String childName = "Users";
    private ProfileManagerCallback profileManagerCallback;
    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firebaseFirestore;
    private StorageTask storageTask;
    private FirebaseStorage firebaseStorage;
    private StorageReference storageReference;

    //when user update his profile
    //1. updateFirebaseEmailAndUserAndImage will be called and email will be updated if failed process will stop
    //2. updateProfileWithImage this will update the profile photo if user has selected a new one, if failed entire process fails
    //3. updateFirebaseProfile here the firebase auth user will be updated with new details
    //4. in the above step if user had a old profile pic that will be deleted
    //5. updateProfile this will update the data in the local firestore

    public ProfileManager(ProfileManagerCallback profileManagerCallback) {
        this.profileManagerCallback = profileManagerCallback;
        firebaseAuth = FirebaseAuth.getInstance();
        this.firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseStorage = FirebaseStorage.getInstance();
        storageReference = firebaseStorage.getReference().child(childName).child("Images");
    }

    public ProfileManager() {
        firebaseAuth = FirebaseAuth.getInstance();
        this.firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseStorage = FirebaseStorage.getInstance();
        storageReference = firebaseStorage.getReference().child(childName).child("Images");
    }

    public void updateProfile(User user) {
        final DocumentReference refStore;
        refStore = firebaseFirestore.collection(childName).document(firebaseAuth.getCurrentUser().getUid());
        refStore.set(user, SetOptions.merge())
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        if (profileManagerCallback != null)
                            profileManagerCallback.onSuccessUpdateProfile(aVoid);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        if (profileManagerCallback != null)
                            profileManagerCallback.onFailureUpdateProfile(e);
                    }
                });
    }

    public void updatePassword(String password) {
        firebaseAuth.getCurrentUser().updatePassword(password).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (profileManagerCallback != null)
                    profileManagerCallback.onCompleteUpdatePassword(task);
            }
        });
    }


    public void updateFirebaseProfile(final User user, String url) {
        UserProfileChangeRequest profileUpdates;
        profileUpdates = new UserProfileChangeRequest.Builder()
                .setDisplayName(user.getName()).build();

        if (url != null) {
            if (firebaseAuth.getCurrentUser().getPhotoUrl() != null) {
                String oldProfilePic = firebaseAuth.getCurrentUser().getPhotoUrl().toString();
                deleteImage(oldProfilePic);
            }
            profileUpdates = new UserProfileChangeRequest.Builder()
                    .setDisplayName(user.getName()).setPhotoUri(Uri.parse(url)).build();
        }

        firebaseAuth.getCurrentUser().updateProfile(profileUpdates)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "User profile updated.");
                            updateProfile(user);
                        } else {
                            if (profileManagerCallback != null)
                                profileManagerCallback.onFailureUpdateProfile(task.getException());
                        }
                    }
                });
    }

    public void updateFirebaseEmailAndUserAndImage(String email, final User user, final byte[] data) {
        firebaseAuth.getCurrentUser().updateEmail(email).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (profileManagerCallback != null)
                    profileManagerCallback.onCompleteUpdateEmail(task);
                if (task.isSuccessful()) {
                    updateProfileWithImage(user, data);
                }
            }
        });
    }

    public void deleteImage(String url) {
        final StorageReference storageref = firebaseStorage.getReferenceFromUrl(url);
        storageref.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                // File deleted successfully
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Uh-oh, an error occurred!
            }
        });

    }

    public void updateProfileWithImage(final User user, byte[] data) {
        try {
            if (storageTask != null && storageTask.isInProgress()) {
                if (profileManagerCallback != null)
                    profileManagerCallback.onTaskFull(true);
            } else if (data != null) {
                if (profileManagerCallback != null)
                    profileManagerCallback.onTaskFull(false);

                String imageID = UUID.randomUUID().toString().replace("-", "");
                final StorageReference ref = storageReference.child(imageID);
                storageTask = ref.putBytes(data);

                Task<Uri> urlTask = storageTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                    @Override
                    public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                        if (!task.isSuccessful()) {
                            if (profileManagerCallback != null)
                                profileManagerCallback.onFailureUpdateProfile(task.getException());
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
                            updateFirebaseProfile(user, downloadUri.toString());
                        } else if (profileManagerCallback != null) {
                            profileManagerCallback.onFailureUpdateProfile(task.getException());
                        }
                    }
                });
            } else
                updateFirebaseProfile(user, null);
        } catch (Exception e) {
            e.printStackTrace();
            if (profileManagerCallback != null)
                profileManagerCallback.onFailureUpdateProfile(e);
        }
    }

    public void getUser() {
        try {
            firebaseFirestore.collection(childName).document(firebaseAuth.getCurrentUser().getUid()).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (profileManagerCallback != null)
                        profileManagerCallback.onCompleteGetUser(task);
                }
            });
        } catch (NullPointerException e) {
            profileManagerCallback.onCompleteGetUser(null);
        }
    }

    public void getUser(String uid) {
        try {
            firebaseFirestore.collection(childName).document(uid).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (profileManagerCallback != null)
                        profileManagerCallback.onCompleteGetUser(task);
                }
            });
        } catch (NullPointerException e) {
            profileManagerCallback.onCompleteGetUser(null);
        }
    }

    public void reathenticateWithCredentials(AuthCredential credential, final String newPass) {
        firebaseAuth.getCurrentUser().reauthenticate(credential)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            updatePassword(newPass);
                        } else if (profileManagerCallback != null)
                            profileManagerCallback.onCompleteUpdatePassword(task);

                    }
                });
    }


    public void destroy() {
        profileManagerCallback = null;
    }

}

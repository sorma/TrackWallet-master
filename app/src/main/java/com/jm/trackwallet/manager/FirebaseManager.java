package com.jm.trackwallet.manager;

import android.content.Context;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.jm.trackwallet.R;

public class FirebaseManager {
    private static FirebaseManager instance;
    private FirebaseAuth auth;
    private FirebaseFirestore firestore;

    private FirebaseManager() {
        //TODO cambio deprecato
        auth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
        FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(true)
                .build();
        firestore.setFirestoreSettings(settings);
    }

    public static FirebaseManager getInstance() {
        if (instance == null) {
            instance = new FirebaseManager();
        }
        return instance;
    }

    public void logout() {
        FirebaseFirestore.getInstance().clearPersistence();
        FirebaseAuth.getInstance().signOut();
    }

    public FirebaseAuth getAuth() {
        return auth;
    }

    public FirebaseFirestore getFirestore() {
        return firestore;
    }

    public FirebaseUser getCurrentUser() {
        return auth.getCurrentUser();
    }

    public void resetPassword(String emailAddress, Context c){

        auth.sendPasswordResetEmail(emailAddress)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(c, R.string.mail_inviata_per_reset, Toast.LENGTH_SHORT).show();
                            logout();
                        }
                    }
                });
    }
}

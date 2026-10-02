package com.jm.trackwallet.ui.welcome.fragment;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import android.text.InputType;
import android.util.Log;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.jm.trackwallet.R;
import com.jm.trackwallet.manager.FirebaseManager;
import com.jm.trackwallet.ui.home.MainActivity;

import java.util.HashMap;
import java.util.Map;

public class LoginFragment extends Fragment {

    private static final int RC_SIGN_IN = 9001; // Codice di richiesta per il login con Google
    private TextInputEditText editTextUsername, editTextPassword;

    private ActivityResultLauncher<Intent> googleSignInLauncher;

    public LoginFragment() {
        // Required empty public constructor
    }

    public static LoginFragment newInstance() {
        return new LoginFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Impostazione per il launcher di Google Sign-In
        googleSignInLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Intent data = result.getData();
                        handleSignInResult(GoogleSignIn.getSignedInAccountFromIntent(data));
                    }
                });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_login, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        editTextUsername = view.findViewById(R.id.username);
        editTextPassword = view.findViewById(R.id.password);

        Button loginButton = view.findViewById(R.id.loginButton);
        Button loginGoogle = view.findViewById(R.id.loginGoogle);
        Button registratiButton = view.findViewById(R.id.registratiButton);
        Button forgotButton = view.findViewById(R.id.forgotButton);

        loginButton.setOnClickListener(v -> {
            loginUser(editTextUsername.getText().toString(), editTextPassword.getText().toString());
        });

        // 1. Imposta il click listener per il pulsante di accesso con Google
        loginGoogle.setOnClickListener(v -> {
            // Avvia il flusso di login con Google
            if (MainActivity.isOnline(getContext()))
                signInWithGoogle();
            else
                Toast.makeText(getActivity(), R.string.controlla_la_connessione_ad_internet, Toast.LENGTH_SHORT).show();
        });

        forgotButton.setOnClickListener(v -> {
            // Crea un EditText per l'input dell'email
            EditText emailInput = new EditText(getContext());
            emailInput.setHint(R.string.inserisci_la_tua_email);
            emailInput.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);

            // Crea l'AlertDialog di Material Design
            MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(getContext());
            builder.setTitle(R.string.recupero_password)
                    .setMessage(R.string.inserisci_la_tua_email_per_resettare_la_password)
                    .setView(emailInput) // Imposta l'EditText nel dialog
                    .setPositiveButton(R.string.invia, (dialog, which) -> {
                        String email = emailInput.getText().toString();

                        // Verifica se l'email non è vuota e ha un formato valido
                        if (!email.isEmpty() && Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                            // Chiamata per resettare la password (qui andrebbe il tuo codice)
                            FirebaseManager.getInstance().resetPassword(email, getContext());
                        } else {
                            // Mostra un messaggio di errore se l'email non è valida
                            Toast.makeText(getContext(), R.string.inserisci_una_email_valida, Toast.LENGTH_SHORT).show();
                        }
                    })
                    .setNegativeButton(R.string.annulla, (dialog, which) -> {
                        dialog.dismiss(); // Chiude il dialog se l'utente preme "Annulla"
                    });

            // Mostra il dialog
            builder.show();
        });

        registratiButton.setOnClickListener(v -> {
            Navigation.findNavController(v).navigate(R.id.action_loginFragment_to_registratiFragment);
        });
    }

    private void loginUser(String email, String password) {

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(getActivity(), R.string.compila_tutti_i_campi, Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseManager.getInstance().getAuth().signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(getActivity(), task -> {
                    if (task.isSuccessful()) {
                        // Login successful
                        if (FirebaseManager.getInstance().getCurrentUser() != null) {
                            // Redirect to main activity
                            Intent intent = new Intent(getActivity(), MainActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                            getActivity().finish();
                        }
                    } else {
                        // If sign in fails
                        Toast.makeText(getActivity(), R.string.autenticazione_fallita,
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void signInWithGoogle() {
        // Configura Google Sign-In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))  // Aggiungi il tuo ID client qui
                .requestEmail()
                .build();

        GoogleSignInClient googleSignInClient = GoogleSignIn.getClient(requireContext(), gso);
        googleSignInLauncher.launch(googleSignInClient.getSignInIntent());
    }

    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            // Ottieni il GoogleSignInAccount
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);

            // Il login è riuscito
            String idToken = account.getIdToken();
            if (idToken != null) {
                Log.d("GoogleSignIn", "ID Token: " + idToken);

                firebaseAuthWithGoogle(idToken);
                // L'accesso è riuscito
                String email = account.getEmail();
                Toast.makeText(requireContext(), getString(R.string.accesso_riuscito) + email, Toast.LENGTH_SHORT).show();
            }

        } catch (ApiException e) {
            // Gestisci l'errore
            Log.e("GoogleSignIn", "Errore durante l'accesso con Google", e);
            Toast.makeText(requireContext(), R.string.errore_durante_l_accesso_con_google, Toast.LENGTH_SHORT).show();
        }
    }

    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);

        FirebaseManager.getInstance().getAuth().signInWithCredential(credential)
                .addOnCompleteListener(requireActivity(), task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = FirebaseManager.getInstance().getAuth().getCurrentUser();
                        if (user != null) {
                            Log.d("FirebaseAuth", "Accesso riuscito: " + user.getEmail());

                            // Creiamo la mappa con i dati dell'utente
                            Map<String, Object> userData = new HashMap<>();
                            userData.put("idUtente", user.getUid()); // Salviamo l'UID di Firebase come identificativo
                            userData.put("email", user.getEmail());

                            // Se ha un nome nel profilo Google, lo usiamo come username, altrimenti ne impostiamo uno di default
                            String username = (user.getDisplayName() != null) ? user.getDisplayName() : "Utente" + System.currentTimeMillis();
                            userData.put("username", username);

                            // Salviamo l'utente nella collezione "utenti"
                            FirebaseManager.getInstance().getFirestore().collection("utenti").document(user.getUid()).set(userData, SetOptions.merge())
                                    .addOnSuccessListener(aVoid -> Log.d("Firestore", "Utente salvato con successo"))
                                    .addOnFailureListener(e -> Log.e("Firestore", "Errore nel salvataggio dell'utente", e));

                            // Avvia l'activity principale
                            Toast.makeText(requireContext(), getString(R.string.benvenuto) + username, Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(getActivity(), MainActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                            getActivity().finish();
                        }
                    } else {
                        Log.e("FirebaseAuth", "Autenticazione fallita", task.getException());
                        Toast.makeText(requireContext(), R.string.autenticazione_fallita, Toast.LENGTH_SHORT).show();
                    }
                });
    }
}

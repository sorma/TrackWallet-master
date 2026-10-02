package com.jm.trackwallet.ui.welcome.fragment;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputEditText;
import com.jm.trackwallet.R;
import com.jm.trackwallet.manager.FirebaseManager;
import com.jm.trackwallet.ui.home.MainActivity;

import org.apache.commons.validator.routines.EmailValidator;

import java.util.HashMap;
import java.util.Map;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link RegistratiFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class RegistratiFragment extends Fragment {

    private TextInputEditText username, password, confermaPassword, mail;

    public RegistratiFragment() {
        // Required empty public constructor
    }

    public static RegistratiFragment newInstance() {
        RegistratiFragment fragment = new RegistratiFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_registrati, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        username = view.findViewById(R.id.username);
        password=view.findViewById(R.id.password);
        confermaPassword=view.findViewById(R.id.confermaPassword);
        mail=view.findViewById(R.id.mail);

        Button loginButton= view.findViewById(R.id.loginButtonBack);
        Button registratiButton=view.findViewById(R.id.confermaRegistratiButton);

        loginButton.setOnClickListener(v -> {
            Navigation.findNavController(v).navigate(R.id.action_registratiFragment_to_loginFragment);
        });


        registratiButton.setOnClickListener(v -> {

            if(isUserOk(username.getText().toString())){
                if(isEmailOk(mail.getText().toString())){
                    if(isPasswordOk(password.getText().toString())) {
                        if(password.getText().toString().equals(confermaPassword.getText().toString())){
                            //passa alla home e aggiungi utente a database
                            registerUser(mail.getText().toString(),password.getText().toString(),username.getText().toString());
                        }else{
                            confermaPassword.setError(getString(R.string.le_password_non_coincidono));
                        }
                    }else{
                        password.setError(getString(R.string.inserire_una_password_di_lunghezza_corretta));
                    }
                }else{
                    mail.setError(getString(R.string.inserire_una_mail_valida));
                }
            }else{
                username.setError(getString(R.string.inserire_uno_username_di_lunghezza_corretta));
            }
        });

    }
    private boolean isEmailOk(String mail) {
        return EmailValidator.getInstance().isValid(mail);
    }
    private boolean isUserOk(String user){
        return user.length() > 5;
    }
    private boolean isPasswordOk(String password){
        return password.length() > 7;
    }
    private void registerUser(String email,String password,String username) {


        if (email.isEmpty() || password.isEmpty() || username.isEmpty()) {
            Toast.makeText(getActivity(), R.string.compila_tutti_i_campi, Toast.LENGTH_SHORT).show();
            return;
        }

        // Creazione dell'utente su Firebase Auth
        FirebaseManager.getInstance().getAuth().createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(getActivity(), task -> {
                    if (task.isSuccessful()) {
                        if (FirebaseManager.getInstance().getCurrentUser() != null) {
                            // Salvataggio dei dati dell'utente su Firestore
                            saveUserToFirestore(FirebaseManager.getInstance().getCurrentUser().getUid(), username, email);
                        }
                    } else {
                        // Errore durante la registrazione in Firebase Auth
                        Toast.makeText(getActivity(), getString(R.string.registrazione_fallita) + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void saveUserToFirestore(String userId, String username, String email) {
        // Crea un oggetto con i dati da salvare su Firestore
        Map<String, Object> userMap = new HashMap<>();
        userMap.put("idUtente", userId);
        userMap.put("username", username);
        userMap.put("email", email);

        // Salva i dati su Firestore
        FirebaseManager.getInstance().getFirestore().collection("utenti").document(userId)
                .set(userMap)
                .addOnSuccessListener(aVoid -> {
                    // Se il salvataggio su Firestore ha successo, continua
                    Toast.makeText(getActivity(), R.string.utente_creato_correttamente, Toast.LENGTH_SHORT).show();
                    // Vai alla MainActivity (o qualsiasi altra schermata)
                    Intent intent = new Intent(getActivity(), MainActivity.class);
                    startActivity(intent);
                    getActivity().finish();
                })
                .addOnFailureListener(e -> {
                    // Se il salvataggio su Firestore fallisce, esegui il logout
                    Toast.makeText(getActivity(), getString(R.string.errore_salvataggio_dati_utente) + e.getMessage(), Toast.LENGTH_SHORT).show();
                    // Annulla l'autenticazione di Firebase, così non rimane un utente non completo
                    FirebaseManager.getInstance().getCurrentUser().delete();
                });
    }
}

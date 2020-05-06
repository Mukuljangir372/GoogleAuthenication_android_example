package com.mu.jan.app.googleauthenication;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthCredential;
import com.google.firebase.auth.GoogleAuthProvider;

public class MainActivity extends AppCompatActivity {

    private static final int SIGN_IN = 1212;
    private FirebaseAuth firebaseAuth;

    private GoogleSignInClient googleSignInClient;
    private Button signIn_btn,sign_out;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        signIn_btn = (Button)findViewById(R.id.signButton_in);
        sign_out = (Button)findViewById(R.id.signButton_out);
        progressBar = (ProgressBar)findViewById(R.id.pro_bar);
        progressBar.setVisibility(View.GONE);

        //init
        firebaseAuth = FirebaseAuth.getInstance();

        //prepare
        //config google sign in
        GoogleSignInOptions googleSignInOptions = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        //end config google sign in
        //make a client
        googleSignInClient = GoogleSignIn.getClient(MainActivity.this,googleSignInOptions);

        signIn_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //start auth
                Intent i = googleSignInClient.getSignInIntent();
                startActivityForResult(i,SIGN_IN);
            }
        });
        sign_out.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                signOutGoogle();
            }
        });


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == SIGN_IN){
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);

            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account);
            } catch (ApiException e) {
                e.printStackTrace();
            }

        }
    }
    private void firebaseAuthWithGoogle(GoogleSignInAccount account){
        //show progress bar
        progressBar.setVisibility(View.VISIBLE);

        AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(),null);

        firebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        //sign in successful
                        FirebaseUser user = firebaseAuth.getCurrentUser();
                        updateUI(user);
                        progressBar.setVisibility(View.GONE);
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                  updateUI(null);
                  progressBar.setVisibility(View.GONE);

            }
        });

    }
    private void updateUI(FirebaseUser user){
        if(user!=null){
            Toast.makeText(this,user.getEmail()+"",Toast.LENGTH_SHORT).show();
        }
    }
    private void signOutGoogle(){
        //firebase signout
        firebaseAuth.signOut();
        //google sign out
        googleSignInClient.signOut().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                updateUI(null);

                Toast.makeText(MainActivity.this,"signOut",Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
              FirebaseUser user = firebaseAuth.getCurrentUser();
              updateUI(user);
            }
        });
    }
}

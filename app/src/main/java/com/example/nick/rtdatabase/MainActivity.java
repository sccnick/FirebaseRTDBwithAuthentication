package com.example.nick.rtdatabase;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    private DatabaseReference mRootRef; /*= FirebaseDatabase.getInstance().getReference();*/
    private DatabaseReference mRecordsRef;
    private ArrayList recordsUsername;
    private ArrayList recordsMessage;


    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;

    private GoogleSignInClient mGoogleSignInClient;
    private GoogleApiClient mGoogleApiClient;
    private static final int RC_SIGN_IN = 590;
    private static final String TAG = "GoogleSignIn";

    private LinearLayout Prof_Section;
    private Button SignOut;
    private SignInButton SignIn;
    private TextView Name, Email;
    private ImageView Pic;

    private EditText etMessage;
    private EditText etUsername;
    private Button btnSaveRecord;
    private Button btnDisplayRecord;
    private LinearLayout llDisplay;
    private TextView tvDisplay;

    private void onViewBind() {
        etMessage = (EditText) findViewById(R.id.et_message);
        etUsername = (EditText) findViewById(R.id.et_message2);
        btnSaveRecord = (Button) findViewById(R.id.btn_save);
        btnDisplayRecord = (Button) findViewById(R.id.btn_display);
        llDisplay = (LinearLayout) findViewById(R.id.ll_display);
        tvDisplay = (TextView) findViewById(R.id.tv_display);

        Prof_Section = (LinearLayout) findViewById(R.id.prof_section);
        SignOut = (Button) findViewById(R.id.btn_logout_google_account);
        SignIn = (SignInButton) findViewById(R.id.sign_in_button);
        Name = (TextView) findViewById(R.id.tv_display_name);
        Email = (TextView) findViewById(R.id.tv_display_email);
        Pic = (ImageView) findViewById(R.id.iv_display_image);
    }

    private void initInstance() {
        SignIn.setSize(SignIn.SIZE_STANDARD);
        Prof_Section.setVisibility(View.GONE);
        tvDisplay.setVisibility(View.VISIBLE);

        btnSaveRecord.setOnClickListener(this);
        btnDisplayRecord.setOnClickListener(this);

        SignIn.setOnClickListener(this);
        SignOut.setOnClickListener(this);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        onViewBind();
        initInstance();

        mAuth = FirebaseAuth.getInstance();
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                if (firebaseAuth.getCurrentUser() != null) {
                    Toast.makeText(MainActivity.this, "Auth currentUser not null", Toast.LENGTH_SHORT).show();
                } else {

                }
            }
        };


        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, new GoogleApiClient.OnConnectionFailedListener() {
                    @Override
                    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
                        Toast.makeText(MainActivity.this, "on connection failed", Toast.LENGTH_SHORT).show();
                    }
                })
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        FirebaseDatabase.getInstance().setPersistenceEnabled(true); //set Offline mode
        mRootRef = FirebaseDatabase.getInstance().getReference();
    }

    private void signIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    private void signOut() {
        mAuth.signOut();
        mGoogleSignInClient.signOut();
        mGoogleSignInClient.revokeAccess();

        tvDisplay.setVisibility(View.GONE);
        updateUI(null);

    }

    public void saveRecord() {
        String stringMessage = etMessage.getText().toString();
        String stringUsername = etUsername.getText().toString();

        if (stringMessage.isEmpty() || stringUsername.isEmpty()) {
            return;
        }



        if (mAuth.getCurrentUser() != null) {
            DatabaseReference mUsernameRef = mRootRef
                    .child("records").child(mAuth.getUid()).child("username");


            DatabaseReference mMessageRef = mRootRef
                    .child("records").child(mAuth.getUid()).child("message");

            mUsernameRef.setValue(stringUsername);
            mMessageRef.setValue(stringMessage);
        } else {
            Toast.makeText(MainActivity.this, "Please Sign In with Google account", Toast.LENGTH_SHORT).show();
        }

    }

    public void displayRecord() {



        mRecordsRef = mRootRef.child("records");

        mRecordsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                String show = "";

                if (mAuth.getCurrentUser() != null) {

                    recordsUsername = new ArrayList();
                    recordsMessage = new ArrayList();


                    for (DataSnapshot dsp : dataSnapshot.getChildren()) {

                        recordsMessage.add(dsp.child("message").getValue());
                        recordsUsername.add(dsp.child("username").getValue());

                    }

                    for (int i = 0; i < recordsUsername.size(); i++) {
                        if (recordsUsername.get(i) != null && recordsMessage.get(i) != null) {
                            show += recordsMessage.get(i).toString() + ", " + recordsUsername.get(i).toString() + " ไม่ได้กล่าวไว้. \n";
                        }
                    }

                } else {

                    recordsUsername = new ArrayList();

                    for (DataSnapshot dsp : dataSnapshot.getChildren()) {
                        recordsUsername.add(dsp.child("username").getValue());
                    }

                    for (int i = 0; i < recordsUsername.size(); i++) {
                        if (recordsUsername.get(i) != null) {
                            show += recordsUsername.get(i).toString() + " ไม่ได้กล่าวไว้. \n";
                        }
                    }

                }

                tvDisplay.setText(show);

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            if (result.isSuccess()) {
                GoogleSignInAccount account = result.getSignInAccount();
                firebaseAuthWithGoogle(account);
            } else {
                Toast.makeText(MainActivity.this, "Google Sign In was failed", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        updateUI(currentUser);
    }

    private void updateUI(FirebaseUser currentUser) {
        if (currentUser != null) {

            String userId = currentUser.getUid();
            String name = currentUser.getDisplayName();
            String email = currentUser.getEmail();
            String img_url = currentUser.getPhotoUrl().toString();
            Name.setText(name);
            Email.setText(email);
            Glide.with(this).load(img_url).into(Pic);

            Prof_Section.setVisibility(View.VISIBLE);
            SignIn.setVisibility(View.GONE);

        } else {
            Prof_Section.setVisibility(View.GONE);
            SignIn.setVisibility(View.VISIBLE);

        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.sign_in_button:
                signIn();
                break;
            case R.id.btn_logout_google_account:
                signOut();
                break;
            case R.id.btn_save:
                saveRecord();
                break;
            case R.id.btn_display:
                tvDisplay.setVisibility(View.VISIBLE);
                displayRecord();
                break;
        }

    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        Log.d(TAG, "firebaseAuthWithGoogle:" + acct.getId());

        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "signInWithCredential:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            updateUI(user);
                        } else {
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                            Toast.makeText(MainActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                            updateUI(null);
                        }


                    }
                });
    }

}

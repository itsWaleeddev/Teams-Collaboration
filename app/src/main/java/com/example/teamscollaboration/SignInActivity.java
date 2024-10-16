package com.example.teamscollaboration;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.service.autofill.UserData;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.teamscollaboration.Models.UserModel;
import com.example.teamscollaboration.databinding.ActivitySignInBinding;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
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

import java.util.Objects;

public class SignInActivity extends AppCompatActivity {
    ActivitySignInBinding binding;
    GoogleSignInClient googleSignInClient;
    DatabaseReference databaseReference;
    FirebaseAuth auth;
    String name = null;
    String role = null;
    String uId = null;
    String email = null;


    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser != null) {
            updateUi();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivitySignInBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        auth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference();

        //setting google sign in options
        GoogleSignInOptions googleSignInOptions = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id)).requestEmail().build();

        //initialize google sign in client
        googleSignInClient = GoogleSignIn.getClient(this, googleSignInOptions);

        binding.googleSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                name = null;
                role = null;
                name = binding.name.getText().toString().trim();
                role = binding.role.getSelectedItem().toString().trim();
                Log.d("nameCheck", "onClick: " + name);
                if (!name.isEmpty() && !role.isEmpty()) {
                    Intent signInIntent = googleSignInClient.getSignInIntent();
                    launcher.launch(signInIntent);
                } else {
                    Toast.makeText(SignInActivity.this, "Please Fill all the details", Toast.LENGTH_SHORT).show();
                }
            }
        });

        binding.alreadyAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent signInIntent = googleSignInClient.getSignInIntent();
                launcher.launch(signInIntent);
            }
        });
        ArrayAdapter<CharSequence> adapter = new ArrayAdapter<CharSequence>(this, R.layout.spinner_item,
                getResources().getTextArray(R.array.role_options)) {
            @Override
            public View getDropDownView(int position, View convertView, ViewGroup parent) {
                View view = super.getDropDownView(position, convertView, parent);
                view.setBackgroundColor(Color.WHITE); // Set the background color of the dropdown
                return view;
            }
        };
        binding.role.setAdapter(adapter);
    }
    ActivityResultLauncher<Intent> launcher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(result.getData());
                        if (task.isSuccessful()) {
                            GoogleSignInAccount account = task.getResult();
                            AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);
                            auth.signInWithCredential(credential).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if (task.isSuccessful()) {
                                        checkUserExistence(new UserExistenceCallback() {
                                            @Override
                                            public void onUserExistenceChecked(boolean exists) {
                                                if (exists) {
                                                    Toast.makeText(SignInActivity.this, "Logged-in Successfully", Toast.LENGTH_SHORT).show();
                                                    updateUi();
                                                } else {
                                                    saveUserData();
                                                    Toast.makeText(SignInActivity.this, "Successfully signed-in with Google", Toast.LENGTH_SHORT).show();
                                                    updateUi();
                                                }
                                            }
                                        });
                                    } else {
                                        Toast.makeText(SignInActivity.this, "Signed-in Failed", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                        } else {
                            Toast.makeText(SignInActivity.this, "Signed-in Failed", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            });

    public void updateUi() {
        Intent intent = new Intent(SignInActivity.this, MainActivity.class);
        intent.putExtra("role",role);
        startActivity(intent);
        finish();
    }

    private void saveUserData() {
        name = binding.name.getText().toString().trim();
        role = binding.role.getSelectedItem().toString().trim();
        uId = auth.getCurrentUser().getUid();
        email = auth.getCurrentUser().getEmail();
        UserModel usermodel = new UserModel(uId, name, role, email);
        if (auth.getCurrentUser() != null) {
            databaseReference.child("Users").child(uId).setValue(usermodel);
        }
    }

    private void checkUserExistence(UserExistenceCallback callback) {
        databaseReference.child("Users").child(Objects.requireNonNull(auth.getCurrentUser()).getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    Log.d("userExist", "User exists in the database");
                    callback.onUserExistenceChecked(true);
                } else {
                    Log.d("userExist", "User does not exist in the database");
                    callback.onUserExistenceChecked(false);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.d("databaseError", "onCancelled: " + error.getMessage());
            }
        });
    }

    public interface UserExistenceCallback {
        void onUserExistenceChecked(boolean exists);
    }
}
package app.m26.wikidriver.activities;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import app.m26.wikidriver.R;;
import app.m26.wikidriver.models.User;
import app.m26.wikidriver.utils.Config;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.rengwuxian.materialedittext.MaterialEditText;

import dmax.dialog.SpotsDialog;

public class LoginActivity extends AppCompatActivity {

    private MaterialEditText edtEmail, edtPassword;
    private Button btnLogIn;
    private TextView txtForgotPassword;
    private FirebaseDatabase database;
    private DatabaseReference users;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        edtEmail = findViewById(R.id.edtEmail);
        edtPassword = findViewById(R.id.edtPassword);
        btnLogIn = findViewById(R.id.btnLogIn);
        txtForgotPassword = findViewById(R.id.txtForgotPassword);

        database  = FirebaseDatabase.getInstance();
        users = database.getReference(Config.FIREBASE_USERS_REFERENCE);


        txtForgotPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = edtEmail.getText().toString();
                if(isEmailValid(email))
                    FirebaseAuth.getInstance().sendPasswordResetEmail(edtEmail.getText().toString()).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Toast.makeText(LoginActivity.this, getResources().getString(R.string.reset), Toast.LENGTH_SHORT).show();
                        }
                    });
                else
                    Toast.makeText(LoginActivity.this, getResources().getString(R.string.email_not_valid), Toast.LENGTH_SHORT).show();
            }
        });

        /*txtTerms.setText(Html.fromHtml(terms));

        txtTerms.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        txtSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent signUpIntent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(signUpIntent);
            }
        });
*/
        btnLogIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String email = edtEmail.getText().toString();
                final String password = edtPassword.getText().toString();
                final SpotsDialog spotsDialog = new SpotsDialog(LoginActivity.this, R.style.CustomRegister);

                //if(FirebaseAuth.getInstance().getCurrentUser().isEmailVerified()) {
                if(FirebaseAuth.getInstance().getCurrentUser() != null)
                    FirebaseAuth.getInstance().getCurrentUser().reload();
                    spotsDialog.show();
                    FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful())
                                users.addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        User user = null;
                                        boolean exists = false;
                                        for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                                            user = postSnapshot.getValue(User.class);
                                            if (user.getEmail().equals(email)) {
                                                exists = true;
                                                break;
                                            }
                                        }
                                        if (!email.isEmpty() && exists) {
                                            user.setStatus("online");
                                            Config.setCurrentUser(getApplicationContext(), user);
                                            Config.updateOnlineUser(getApplicationContext());

                                            spotsDialog.dismiss();
                                            Intent mainActivity = new Intent(LoginActivity.this, MainActivity.class);
                                            startActivity(mainActivity);
                                            finish();

                                        } else {
                                            spotsDialog.dismiss();
                                            Toast.makeText(LoginActivity.this, getResources().getString(R.string.email_no_exist), Toast.LENGTH_SHORT).show();
                                        }
                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {

                                    }
                                });
                            else {
                                Toast.makeText(LoginActivity.this, task.getException().getMessage() + " ", Toast.LENGTH_SHORT).show();
                                spotsDialog.dismiss();
                            }
                        }
                    });
                /*} else {
                    Snackbar.make(v, "Email is not verified", Snackbar.LENGTH_LONG).setAction("VERIFY", new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            FirebaseAuth.getInstance().getCurrentUser().sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if(task.isSuccessful())
                                        Toast.makeText(LoginActivity.this, "Verification email is sent", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    }).show();
                }*/
            }
        });
    }

    boolean isEmailValid(CharSequence email) {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == android.R.id.home) {
            startActivity(new Intent(LoginActivity.this, LandingActivity.class));
            finish();
        }
        return super.onOptionsItemSelected(item);
    }
}

package com.app.wikidriver.activities;

import android.content.Intent;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.app.wikidriver.R;
import com.app.wikidriver.models.User;
import com.app.wikidriver.utils.Config;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.rengwuxian.materialedittext.MaterialAutoCompleteTextView;

import net.igenius.customcheckbox.CustomCheckBox;

import dmax.dialog.SpotsDialog;

public class RegisterActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private EditText edtEmail, edtPassword, edtFirstName, edtLastName, edtPhoneNumber;
    private MaterialAutoCompleteTextView edtCountry, edtCity;
    private Button btnCreate;
    private FirebaseDatabase database;
    private CustomCheckBox chbxTerms;
    private DatabaseReference users;
    private TextView txtTerms;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        auth = FirebaseAuth.getInstance();

        edtCity = findViewById(R.id.edtCity);
        edtCountry = findViewById(R.id.edtCountry);
        edtEmail = findViewById(R.id.edtEmail);
        chbxTerms = findViewById(R.id.chbxTerms);
        edtPhoneNumber = findViewById(R.id.edtPhoneNumber);
        edtPassword = findViewById(R.id.edtPassword);
        edtFirstName = findViewById(R.id.edtFirstName);
        edtLastName = findViewById(R.id.edtLastName);
        btnCreate = findViewById(R.id.btnCreate);
        txtTerms = findViewById(R.id.txtTerms);

        setCountryCityLists();

        database = FirebaseDatabase.getInstance();
        users = database.getReference(Config.FIREBASE_USERS_REFERENCE);

        txtTerms.setText(Html.fromHtml(getResources().getString(R.string.terms)));

        txtTerms.setOnClickListener(v -> {
            showTermsDialog();
        });

        btnCreate.setOnClickListener(v -> {

            final String firstName = edtFirstName.getText().toString();
            final String lastName = edtLastName.getText().toString();
            final String email = edtEmail.getText().toString();
            final String password = edtPassword.getText().toString();
            final String phoneNumber = edtPhoneNumber.getText().toString();
            final String city = edtCity.getText().toString();
            final String country = edtCountry.getText().toString();

            if (chbxTerms.isChecked()) {
                if (Config.isConnectedToInternet(getApplicationContext())) {
                    auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if(task.isSuccessful())
                                users.addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        boolean exists = false;
                                        for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                                            User user = postSnapshot.getValue(User.class);
                                            if (user.getEmail().toLowerCase().equals(email.toLowerCase()))
                                                exists = true;
                                        }
                                        if (!exists) {
                                            if (!firstName.isEmpty() && !lastName.isEmpty() && !email.isEmpty() && !password.isEmpty() && !phoneNumber.isEmpty()
                                                    && !edtCity.getText().toString().isEmpty() && !edtCountry.getText().toString().isEmpty()) {
                                                String userId = users.push().getKey();
                                                final User user = new User(
                                                        userId,
                                                        firstName,
                                                        lastName,
                                                        email,
                                                        phoneNumber,
                                                        "default",
                                                        "null",
                                                        "null",
                                                        country,
                                                        city, FirebaseInstanceId.getInstance().getToken(),
                                                        "online");

                                                //Config.setClosestAirport(getApplicationContext());

                                                users.child(userId).setValue(user).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void aVoid) {
                                                        final SpotsDialog spotsDialog = new SpotsDialog(RegisterActivity.this, R.style.CustomRegister);
                                                        spotsDialog.show();
                                                        new Handler().postDelayed(new Runnable() {
                                                            @Override
                                                            public void run() {
                                                                spotsDialog.dismiss();
                                                                FirebaseUser firebaseUser = auth.getCurrentUser();
                                                                firebaseUser.sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                    @Override
                                                                    public void onComplete(@NonNull Task<Void> task) {
                                                                        if(task.isSuccessful()) {
                                                                            Intent mainActivity = new Intent(RegisterActivity.this, VerificationEmailActivity.class);
                                                                            startActivity(mainActivity);
                                                                            finish();
                                                                        }

                                                                    }
                                                                });
                                                            }
                                                        }, 1800);
                                                    }
                                                });
                                            }
                                        } else {
                                            Toast.makeText(RegisterActivity.this, getResources().getString(R.string.email_exist), Toast.LENGTH_SHORT).show();
                                        }
                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {

                                    }
                                });
                            else
                                Toast.makeText(getApplicationContext(), task.getException().getMessage() + " ", Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    Config.NetworkAlert(RegisterActivity.this);
                }
            } else {
                Snackbar.make(v, getResources().getString(R.string.must_accept_terms), Snackbar.LENGTH_SHORT).show();
            }
        });
    }

    private void showTermsDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getResources().getString(R.string.accept_terms));
        builder.setCancelable(true);
        builder.setMessage(getResources().getString(R.string.terms_content));
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void setCountryCityLists() {
        final ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(getApplicationContext(),
                android.R.layout.simple_spinner_item, Config.getAllCountries(getApplicationContext()));
        dataAdapter.sort((o1, o2) -> o1.compareTo(o2));
        dataAdapter.setDropDownViewResource(android.R.layout.simple_list_item_1);
        edtCountry.setAdapter(dataAdapter);

        edtCountry.setOnItemClickListener((parent, view, position, id) -> {
            String country = dataAdapter.getItem(position);
            ArrayAdapter<String> dataAdapter2 = new ArrayAdapter<String>(getApplicationContext(),
                    android.R.layout.simple_spinner_item, Config.getAllCities(getApplicationContext(), country));
            dataAdapter2.sort((o1, o2) -> o1.compareTo(o2));
            dataAdapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            //spCity.setAdapter(dataAdapter2);
            edtCity.setAdapter(dataAdapter2);
        });

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == android.R.id.home)
            onBackPressed();
        return super.onOptionsItemSelected(item);
    }
}

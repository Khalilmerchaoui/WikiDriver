package app.m26.wikidriver.activities;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import app.m26.wikidriver.R;;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class VerificationEmailActivity extends AppCompatActivity {

    private TextView txtResend, txtLogin, txtVerifCnt;
    private FirebaseAuth auth;
    private FirebaseUser firebaseUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verification_email);

        txtResend = findViewById(R.id.txtResend);
        txtLogin = findViewById(R.id.txtLogin);
        txtVerifCnt = findViewById(R.id.txtVerifCnt);

        auth = FirebaseAuth.getInstance();
        firebaseUser = auth.getCurrentUser();

        txtResend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                firebaseUser.sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()) {
                            Toast.makeText(VerificationEmailActivity.this, getResources().getString(R.string.email_sent), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });

        txtLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(VerificationEmailActivity.this, LoginActivity.class));
                finish();
            }
        });

        final FirebaseAuth.AuthStateListener firebaseAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(FirebaseAuth auth) {
                FirebaseUser user = auth.getCurrentUser();
                user.reload();
                if (user != null && user.isEmailVerified()) {
                    FirebaseAuth.getInstance().removeAuthStateListener(this);
                    startActivity(new Intent(VerificationEmailActivity.this, MainActivity.class));
                }
            }
        };
        FirebaseAuth.getInstance().addAuthStateListener(firebaseAuthListener);

        String verifCont = getResources().getString(R.string.verif_content).toString();
        txtVerifCnt.setText(String.format(verifCont, firebaseUser.getEmail()));
    }
}

package com.app.wikidriver.activities;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.app.wikidriver.R;
import com.app.wikidriver.models.User;
import com.app.wikidriver.utils.Config;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.FileNotFoundException;

import de.hdodenhof.circleimageview.CircleImageView;

import static com.app.wikidriver.utils.Config.PICK_IMAGE;
import static com.app.wikidriver.utils.Config.STORAGE_PERMISSION_CODE;

public class ProfileActivity extends AppCompatActivity {

    private TextView txtEdit;
    private Button btnLogout;
    private EditText edtEmail, edtPhone, edtCarBrand, edtCarModel;
    private CircleImageView imgPic;

    private StorageReference mStorageRef = FirebaseStorage.getInstance().getReference(Config.FIREBASE_PROFILE_PICS_REFERENCE);

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.profile_fragment);

        Toolbar mToolbar = findViewById(R.id.toolbar);
        mToolbar.setTitleTextColor(getResources().getColor(android.R.color.white));
        mToolbar.setTitle(getResources().getString(R.string.edit_profile_title));
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        txtEdit = findViewById(R.id.txtEdit);
        btnLogout = findViewById(R.id.btnLogout);
        edtEmail = findViewById(R.id.edtEmail);
        edtPhone = findViewById(R.id.edtPhone);
        edtCarBrand = findViewById(R.id.edtCarBrand);
        edtCarModel = findViewById(R.id.edtCarModel);
        imgPic = findViewById(R.id.imgPic);

        Picasso.with(getApplicationContext())
                .load(Config.getCurrentUser(getApplicationContext()).getThumbnail())
                .error(R.drawable.profile_icon)
                .into(imgPic);

        User user = Config.getCurrentUser(getApplicationContext());

        edtEmail.setText(user.getEmail());
        edtPhone.setText(user.getPhoneNumber());
        if(!user.getCarBrand().equals("null"))
            edtCarBrand.setText(user.getCarBrand());
        if(!user.getCarModel().equals("null"))
            edtCarModel.setText(user.getCarModel());


        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logOut();
            }
        });

        txtEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(ProfileActivity.this,
                            new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, STORAGE_PERMISSION_CODE);
                } else {
                    Intent intent = new Intent();
                    intent.setType("image/*");
                    intent.setAction(Intent.ACTION_GET_CONTENT);
                    startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE);
                }
            }
        });
    }


    private void updateUserInfo(String email, String phone, String carBrand, String carModel) {
        User updateUser = Config.getCurrentUser(getApplicationContext());
        if(!email.isEmpty())
            updateUser.setEmail(email);
        else
            edtEmail.setError(getResources().getString(R.string.empty_field));
        if(!phone.isEmpty())
            updateUser.setPhoneNumber(phone);
        else
            edtPhone.setError(getResources().getString(R.string.empty_field));
        if(!carBrand.isEmpty())
            updateUser.setCarBrand(carBrand);
        else
            edtCarBrand.setError(getResources().getString(R.string.empty_field));
        if(!carModel.isEmpty())
            updateUser.setCarModel(carModel);
        else
            edtCarModel.setError(getResources().getString(R.string.empty_field));

        Config.setCurrentUser(getApplicationContext(), updateUser);
        Config.updateLocalUser(getApplicationContext());
        Config.updateOnlineUser(getApplicationContext());
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == STORAGE_PERMISSION_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE);
            }
        }
    }

    private Uri file;
    private boolean picChanged = false;
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE && resultCode == Activity.RESULT_OK && data != null) {
            try {
                file = data.getData();
                final Bitmap bitmap = BitmapFactory.decodeStream(getApplicationContext().getContentResolver().openInputStream(file));
                picChanged = true;
                if(bitmap != null)
                    imgPic.setImageBitmap(bitmap);


            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }

            //TOD0 add to firebase storage and update user thumbnail string.
        }
    }

    private void uploadProfilePic(final String email, final String phone, final String carBrand, final String carModel) {

        final User updateUser = Config.getCurrentUser(getApplicationContext());

        if(!email.isEmpty())
            updateUser.setEmail(email);
        else
            edtEmail.setError(getResources().getString(R.string.empty_field));
        if(!phone.isEmpty())
            updateUser.setPhoneNumber(phone);
        else
            edtPhone.setError(getResources().getString(R.string.empty_field));
        if(!carBrand.isEmpty())
            updateUser.setCarBrand(carBrand);
        else
            edtCarBrand.setError(getResources().getString(R.string.empty_field));
        if(!carModel.isEmpty())
            updateUser.setCarModel(carModel);
        else
            edtCarModel.setError(getResources().getString(R.string.empty_field));

        if(picChanged) {
            final StorageReference imgRef = mStorageRef.child(Config.getCurrentUser(getApplicationContext()).getUserId() + ".jpg");
            imgRef.putFile(file).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    imgRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {

                            updateUser.setThumbnail(uri.toString());
                            Toast.makeText(getApplicationContext(), getResources().getString(R.string.info_updated), Toast.LENGTH_SHORT).show();
                            Config.setCurrentUser(getApplicationContext(), updateUser);
                            Config.updateOnlineUser(getApplicationContext());
                            Config.updateLocalUser(getApplicationContext());

                        }
                    });
                }
            })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(getApplicationContext(), getResources().getString(R.string.failed_uploading), Toast.LENGTH_SHORT).show();
                        }
                    });
        } else {
            Config.setCurrentUser(getApplicationContext(), updateUser);
            Config.updateOnlineUser(getApplicationContext());
            Config.updateLocalUser(getApplicationContext());
            Toast.makeText(getApplicationContext(), getResources().getString(R.string.info_updated), Toast.LENGTH_SHORT).show();
        }

    }

    private void logOut() {
        FirebaseAuth.getInstance().signOut();
        User currentUser = Config.getCurrentUser(getApplicationContext());
        Config.setCurrentUser(getApplicationContext(), currentUser);
        Config.updateOnlineUser(getApplicationContext());
        Config.setCurrentUser(getApplicationContext(), null);

        Intent logoutIntent = new Intent(getApplicationContext(), LandingActivity.class);
        startActivity(logoutIntent);
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_profile, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == android.R.id.home)
            onBackPressed();
        else if(item.getItemId() == R.id.menu_check) {
            String email = edtEmail.getText().toString();
            String phone = edtPhone.getText().toString();
            String carBrand = edtCarBrand.getText().toString();
            String carModel = edtCarModel.getText().toString();

            uploadProfilePic(email, phone, carBrand, carModel);
            finish();
        }

        return super.onOptionsItemSelected(item);
    }
}

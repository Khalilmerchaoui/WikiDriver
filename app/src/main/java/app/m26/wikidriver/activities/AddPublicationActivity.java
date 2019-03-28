package app.m26.wikidriver.activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import app.m26.wikidriver.R;;
import app.m26.wikidriver.models.Publication;
import app.m26.wikidriver.models.User;
import app.m26.wikidriver.utils.Config;
import app.m26.wikidriver.utils.ImgHelper;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import de.hdodenhof.circleimageview.CircleImageView;
import dmax.dialog.SpotsDialog;

public class AddPublicationActivity extends AppCompatActivity {

    private Bundle extras;
    private List<String> imgUriList, imgUrlList = new ArrayList<>();
    private User currentUser;
    private int type;
    private ImageView imgPhoto;
    private ImageView imgAdded, imgAdded2, imgAdded3;
    private Button btnPost;
    private CircleImageView profilePic;
    private TextView txtFullName, txtType;
    private EditText edtPublication;
    private int SELECT_PHOTO = 1;
    private StorageReference mStorageRef = FirebaseStorage.getInstance().getReference(Config.FIREBASE_PUBLICATION_IMAGES_REFERENCE);
    private DatabaseReference publications, users;
    private SpotsDialog spotsDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_publication);

        imgUriList = new ArrayList<>();

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle(getResources().getString(R.string.create_post));
        toolbar.setTitleTextColor(getResources().getColor(android.R.color.white));
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        spotsDialog = new SpotsDialog(AddPublicationActivity.this, R.style.CustomRegister);

        btnPost = findViewById(R.id.btnPost);
        imgAdded = findViewById(R.id.imgAddedPhoto);
        imgAdded2 = findViewById(R.id.imgAddedPhoto2);
        imgAdded3 = findViewById(R.id.imgAddedPhoto3);
        imgPhoto = findViewById(R.id.imgPhoto);
        profilePic = findViewById(R.id.profilePic);
        txtFullName = findViewById(R.id.txtFullName);
        txtType = findViewById(R.id.txtType);
        edtPublication = findViewById(R.id.edtPublication);

        currentUser = Config.getCurrentUser(getApplicationContext());

        extras = getIntent().getExtras();

        if(extras != null) {
            type = extras.getInt("type");
            switch(type) {
                case Config.PUBLICATION_TYPE_SALE:
                    txtType.setText(getResources().getString(R.string.sale_rent));
                    break;
                case Config.PUBLICATION_TYPE_MECHANICAL:
                    txtType.setText(getResources().getString(R.string.mechanical_bodywork));
                    break;
                case Config.PUBLICATION_TYPE_COURSE:
                    txtType.setText(getResources().getString(R.string.course_driver));
                    break;
                case Config.PUBLICATION_TYPE_OTHER:
                    txtType.setText(getResources().getString(R.string.other));
                    break;
            }
        }

        publications = FirebaseDatabase.getInstance().getReference(Config.FIREBASE_PUBLICATION_REFERENCE);
        users = FirebaseDatabase.getInstance().getReference(Config.FIREBASE_USERS_REFERENCE);

        Picasso.with(getApplicationContext())
                .load(currentUser.getThumbnail())
                .error(R.drawable.profile_icon)
                .into(profilePic);

        txtFullName.setText(currentUser.getFirstName() + " " + currentUser.getLastName());

        btnPost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                long timeStamp = System.currentTimeMillis();
                String content = edtPublication.getText().toString();
                if(!content.isEmpty()) {
                    String publicationId = publications.push().getKey();
                    spotsDialog.show();
                    publishPublication(content, publicationId, timeStamp);
                }
            }
        });

        imgPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(counter < 3) {
                    Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
                    photoPickerIntent.setType("image/*");
                    startActivityForResult(photoPickerIntent, SELECT_PHOTO);
                } else
                    Toast.makeText(AddPublicationActivity.this, getResources().getString(R.string.max_images), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void publishPublication(String content, String publicationId, long timeStamp) {

        if (imgUriList.size() != 0) {
                StorageReference imgRef = mStorageRef.child(UUID.randomUUID().toString());
                imgRef.putFile(Uri.parse(imgUriList.get(0))).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        imgRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                String imgUrl = uri.toString();
                                imgUrlList.add(imgUrl);
                                imgUriList.remove(0);
                                Log.i("tagging", imgUrlList.size() +" " + imgUrl);
                                if(imgUriList.size() > 0) {
                                    StorageReference imgRef = mStorageRef.child(UUID.randomUUID().toString());
                                    imgRef.putFile(Uri.parse(imgUriList.get(0))).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                        @Override
                                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                            imgRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                                @Override
                                                public void onSuccess(Uri uri) {
                                                    String imgUrl = uri.toString();
                                                    imgUrlList.add(imgUrl);
                                                    imgUriList.remove(0);
                                                    Log.i("tagging", imgUrlList.size() +" " + imgUrl);
                                                    if(imgUriList.size() > 0) {
                                                        StorageReference imgRef = mStorageRef.child(UUID.randomUUID().toString());
                                                        imgRef.putFile(Uri.parse(imgUriList.get(0))).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                                            @Override
                                                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                                                imgRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                                                    @Override
                                                                    public void onSuccess(Uri uri) {
                                                                        String imgUrl = uri.toString();
                                                                        imgUrlList.add(imgUrl);
                                                                        imgUriList.remove(0);
                                                                        Log.i("tagging", imgUrlList.size() +" " + imgUrl);
                                                                        Publication publication = new Publication(
                                                                                publicationId,
                                                                                currentUser.getUserId(),
                                                                                timeStamp,
                                                                                0,
                                                                                0,
                                                                                0,
                                                                                null,
                                                                                null,
                                                                                null,
                                                                                content,
                                                                                type,
                                                                                currentUser.getCity(),
                                                                                currentUser.getCountry(),
                                                                                imgUrlList, "null");

                                                                        publications.child(publicationId).setValue(publication).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                            @Override
                                                                            public void onSuccess(Void aVoid) {
                                                                                AddPublicationActivity.this.onBackPressed();
                                                                            }
                                                                        });
                                                                    }
                                                                });
                                                            }
                                                        });
                                                    } else {
                                                        Publication publication = new Publication(
                                                                publicationId,
                                                                currentUser.getUserId(),
                                                                timeStamp,
                                                                0,
                                                                0,
                                                                0,
                                                                null,
                                                                null,
                                                                null,
                                                                content,
                                                                type,
                                                                currentUser.getCity(),
                                                                currentUser.getCountry(),
                                                                imgUrlList, "null");

                                                        publications.child(publicationId).setValue(publication).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                            @Override
                                                            public void onSuccess(Void aVoid) {
                                                                spotsDialog.dismiss();
                                                                AddPublicationActivity.this.onBackPressed();
                                                            }
                                                        });
                                                    }
                                                }
                                            });
                                        }
                                    });
                                } else {
                                    Publication publication = new Publication(
                                            publicationId,
                                            currentUser.getUserId(),
                                            timeStamp,
                                            0,
                                            0,
                                            0,
                                            null,
                                            null,
                                            null,
                                            content,
                                            type,
                                            currentUser.getCity(),
                                            currentUser.getCountry(),
                                            imgUrlList, "null");
                                    publications.child(publicationId).setValue(publication).addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            spotsDialog.dismiss();
                                            AddPublicationActivity.this.onBackPressed();
                                        }
                                    });
                                }
                            }
                        });

                    }
                });

        }
        else {
            Publication publication = new Publication(
                    publicationId,
                    currentUser.getUserId(),
                    timeStamp,
                    0,
                    0,
                    0,
                    null,
                    null,
                    null,
                    content,
                    type,
                    currentUser.getCity(),
                    currentUser.getCountry(),
                     null, "null");

            publications.child(publicationId).setValue(publication).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    spotsDialog.dismiss();
                    onBackPressed();
                }
            });
        }
    }

    private Bitmap finalBitmap;
    private int counter = 0;
    private Uri imageUri = null;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == SELECT_PHOTO) {
                if(resultCode == RESULT_OK){
                    try {
                        imageUri = data.getData();
                        imgUriList.add(imageUri.toString());
                        final InputStream imageStream = getContentResolver().openInputStream(imageUri);
                        final Bitmap selectedImage = BitmapFactory.decodeStream(imageStream);
                        finalBitmap = ImgHelper.getRoundedCornerBitmap(selectedImage, 100);
                        counter++;
                        if(counter == 1)
                            imgAdded.setImageBitmap(finalBitmap);
                        else if (counter == 2)
                            imgAdded2.setImageBitmap(finalBitmap);
                        else if (counter == 3)
                            imgAdded3.setImageBitmap(finalBitmap);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }

                }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == android.R.id.home)
            onBackPressed();
        return super.onOptionsItemSelected(item);
    }

}

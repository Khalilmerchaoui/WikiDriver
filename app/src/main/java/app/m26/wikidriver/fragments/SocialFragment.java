package app.m26.wikidriver.fragments;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.util.Linkify;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.URLUtil;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import app.m26.wikidriver.LinkView;
import app.m26.wikidriver.R;;
import app.m26.wikidriver.adapters.SocialPublicationsAdapter;
import app.m26.wikidriver.models.Link;
import app.m26.wikidriver.models.Publication;
import app.m26.wikidriver.models.User;
import app.m26.wikidriver.utils.Config;
import app.m26.wikidriver.utils.ImgHelper;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import de.hdodenhof.circleimageview.CircleImageView;
import dmax.dialog.SpotsDialog;
import fm.jiecao.jcvideoplayer_lib.JCVideoPlayer;

import static android.app.Activity.RESULT_OK;

public class SocialFragment extends Fragment implements TextWatcher {

    private ImageView imgPhoto, imgVideo;
    private User currentUser;
    private List<String> imgUriList, imgUrlList = new ArrayList<>();
    private ImageView imgAdded, imgAdded2, imgAdded3;
    private Button btnPost;
    private DatabaseReference publicationsReference, usersReference;
    private CircleImageView profilePic;
    private TextView txtNoPublications;
    private EditText edtPublication;
    private int SELECT_PHOTO = 1, SELECT_VIDEO = 2;
    private StorageReference mStorageRef = FirebaseStorage.getInstance().getReference(Config.FIREBASE_SOCIAL_IMAGES_REFERENCE);
    private StorageReference mStorageVideosRef = FirebaseStorage.getInstance().getReference(Config.FIREBASE_SOCIAL_VIDEOS_REFERENCE);
    private DatabaseReference publications, users;
    private SpotsDialog spotsDialog;
    private RecyclerView recyclerView;
    private VideoView videoView;
    private String videoUrl;
    private LinkView linkView;
    private LinearLayout postLayout, videoLayout, imgLayout;
    private LinearLayoutManager layoutManager;

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        imgUriList = new ArrayList<>();

        mStorageRef.delete();
        mStorageVideosRef.delete();

        spotsDialog = new SpotsDialog(getActivity(), R.style.CustomRegister);
        currentUser = Config.getCurrentUser(getActivity());

        publicationsReference = FirebaseDatabase.getInstance().getReference(Config.FIREBASE_SOCIAL_REFERENCE);
        usersReference = FirebaseDatabase.getInstance().getReference(Config.FIREBASE_USERS_REFERENCE);


        linkView = getActivity().findViewById(R.id.linkView);
        postLayout = getActivity().findViewById(R.id.postLayout);
        videoLayout = getActivity().findViewById(R.id.videoLayout);
        imgLayout = getActivity().findViewById(R.id.imgLayout);
        txtNoPublications = getActivity().findViewById(R.id.txtNoPublications);
        recyclerView = getActivity().findViewById(R.id.recyclerView);
        edtPublication = getActivity().findViewById(R.id.edtPublication);
        btnPost = getActivity().findViewById(R.id.btnPost);
        imgAdded = getActivity().findViewById(R.id.imgAddedPhoto);
        imgAdded2 = getActivity().findViewById(R.id.imgAddedPhoto2);
        imgAdded3 = getActivity().findViewById(R.id.imgAddedPhoto3);
        imgPhoto = getActivity().findViewById(R.id.imgPhoto);
        imgVideo = getActivity().findViewById(R.id.imgVideo);
        profilePic = getActivity().findViewById(R.id.profilePic);

        edtPublication.addTextChangedListener(this);
        edtPublication.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(hasFocus)
                    postLayout.setVisibility(View.VISIBLE);
            }
        });

        layoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemViewCacheSize(30);
        recyclerView.setDrawingCacheEnabled(true);
        recyclerView.setHasFixedSize(true);
        recyclerView.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);

        publications = FirebaseDatabase.getInstance().getReference(Config.FIREBASE_SOCIAL_REFERENCE);
        users = FirebaseDatabase.getInstance().getReference(Config.FIREBASE_USERS_REFERENCE);

        Picasso.with(getActivity())
                .load(currentUser.getThumbnail())
                .error(R.drawable.profile_icon)
                .into(profilePic);

        loadPublicationsFromFirebase();

        btnPost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                long timeStamp = System.currentTimeMillis();
                String content = edtPublication.getText().toString();
                String publicationId = publications.push().getKey();

                if(video) {
                    if(selectedVideo != null)
                        if (content.isEmpty())
                            uploadVideo(publicationId, " ", timeStamp, selectedVideo);
                        else
                            uploadVideo(publicationId, content, timeStamp, selectedVideo);
                        selectedVideo = null;
                } else {
                    spotsDialog.show();
                    if (content.isEmpty())
                        publishPublication(" ", publicationId, timeStamp);
                    else
                        publishPublication(content, publicationId, timeStamp);
                }
                edtPublication.setText("");
                loadPublicationsFromFirebase();
            }
        });

        imgLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(ContextCompat.checkSelfPermission(getContext(), Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(getActivity(),
                            new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                            0);
                    type = false;
                } else {
                    if (counter < 3) {
                        Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
                        photoPickerIntent.setType("image/*");
                        startActivityForResult(photoPickerIntent, SELECT_PHOTO);
                    } else
                        Toast.makeText(getActivity(), getResources().getString(R.string.max_images), Toast.LENGTH_SHORT).show();
                }
            }
        });

        videoLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(ContextCompat.checkSelfPermission(getContext(), Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(getActivity(),
                            new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                            1);

                } else {
                    Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
                    photoPickerIntent.setType("video/*");
                    startActivityForResult(photoPickerIntent, SELECT_VIDEO);
                }
            }
        });
    }

    private Bitmap finalBitmap;
    private int counter = 0;
    private Uri imageUri = null;
    private boolean type;

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
                                                                            0,
                                                                            currentUser.getCity(),
                                                                            currentUser.getCountry(),
                                                                            imgUrlList,
                                                                            "null");

                                                                    publications.child(publicationId).setValue(publication).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                        @Override
                                                                        public void onSuccess(Void aVoid) {
                                                                            spotsDialog.dismiss();
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
                                                            0,
                                                            currentUser.getCity(),
                                                            currentUser.getCountry(),
                                                            imgUrlList, "null");

                                                    publications.child(publicationId).setValue(publication).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                        @Override
                                                        public void onSuccess(Void aVoid) {
                                                            spotsDialog.dismiss();
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
                                        0,
                                        currentUser.getCity(),
                                        currentUser.getCountry(),
                                        imgUrlList, "null");
                                publications.child(publicationId).setValue(publication).addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        spotsDialog.dismiss();

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
                        0,
                        currentUser.getCity(),
                        currentUser.getCountry(),
                        null,
                        "null");

            publications.child(publicationId).setValue(publication).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    spotsDialog.dismiss();
                }
            });
        }

        imgAdded.setImageResource(0);
        imgAdded2.setImageResource(0);
        imgAdded3.setImageResource(0);
    }

    private void loadPublicationsFromFirebase() {

        publicationsReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                List<Publication> publications = new ArrayList<>();
                User currentUser = Config.getCurrentUser(getActivity());
                for(DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    Publication publication = postSnapshot.getValue(Publication.class);
                    Log.i("publicationsTest", publication.getPublicationId());

                    if(currentUser != null)
                        if(publication.getCity().equals(currentUser.getCity()) && publication.getCountry().equals(currentUser.getCountry()) && !publication.getPublicationId().equals("-LcTnhMhqiRFr9n3ycYb")) {
                            publications.add(publication);
                        }
                }
                Publication video = dataSnapshot.child("-LcTnhMhqiRFr9n3ycYb").getValue(Publication.class);
                publications.add(video);
                if(publications.size() > 0){
                    txtNoPublications.setVisibility(View.INVISIBLE);
                    SocialPublicationsAdapter adapter = new SocialPublicationsAdapter(getActivity(), publications, Config.FIREBASE_SOCIAL_REFERENCE);
                    adapter.setHasStableIds(true);
                    recyclerView.setAdapter(adapter);
                    adapter.notifyDataSetChanged();
                } else {
                    txtNoPublications.setVisibility(View.VISIBLE);
                }
                spotsDialog.show();
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        spotsDialog.dismiss();
                    }
                }, 2000);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        publicationsReference.keepSynced(true);
    }

    boolean video = false;
    Uri selectedVideo = null;

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {
            Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
            photoPickerIntent.setType("video/*");
            startActivityForResult(photoPickerIntent, SELECT_VIDEO);
        } else if (requestCode == 0) {
            if (counter < 3) {
                Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
                photoPickerIntent.setType("image/*");
                startActivityForResult(photoPickerIntent, SELECT_PHOTO);
            } else
                Toast.makeText(getActivity(), getResources().getString(R.string.max_images), Toast.LENGTH_SHORT).show();
        }

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == SELECT_PHOTO) {
            if(resultCode == RESULT_OK){
                try {
                    imageUri = data.getData();
                    imgUriList.add(imageUri.toString());
                    final InputStream imageStream = getActivity().getContentResolver().openInputStream(imageUri);
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
        } else if(requestCode == SELECT_VIDEO) {
                counter = 4;
                video = true;
                selectedVideo = data.getData();
                //videoRef = storageRef.child("/videos/" + userUid );
                //uploadData(selectedVideoUri);
                String path = getThumbnailPathForLocalFile(getActivity(), selectedVideo);
                File image = new File(path);
                BitmapFactory.Options bmOptions = new BitmapFactory.Options();
                Bitmap bitmap = BitmapFactory.decodeFile(image.getAbsolutePath(), bmOptions);

                imgAdded.setImageBitmap(bitmap);
        }
    }

    public static String[] thumbColumns = { MediaStore.Video.Thumbnails.DATA };
    public static String[] mediaColumns = { MediaStore.Video.Media._ID };

    public static String getThumbnailPathForLocalFile(Activity context, Uri fileUri) {

        long fileId = getFileId(context, fileUri);

        MediaStore.Video.Thumbnails.getThumbnail(context.getContentResolver(),
                fileId, MediaStore.Video.Thumbnails.MICRO_KIND, null);

        Cursor thumbCursor = null;
        try {

            thumbCursor = context.managedQuery(
                    MediaStore.Video.Thumbnails.EXTERNAL_CONTENT_URI,
                    thumbColumns, MediaStore.Video.Thumbnails.VIDEO_ID + " = "
                            + fileId, null, null);

            if (thumbCursor.moveToFirst()) {
                String thumbPath = thumbCursor.getString(thumbCursor
                        .getColumnIndex(MediaStore.Video.Thumbnails.DATA));

                return thumbPath;
            }

        } finally {
        }

        return null;
    }

    public static long getFileId(Activity context, Uri fileUri) {

        Cursor cursor = context.managedQuery(fileUri, mediaColumns, null, null,
                null);

        if (cursor.moveToFirst()) {
            int columnIndex = cursor
                    .getColumnIndexOrThrow(MediaStore.Video.Media._ID);
            int id = cursor.getInt(columnIndex);

            return id;
        }

        return 0;
    }

    private void uploadVideo(String publicationId, String content, long timeStamp, final Uri videoUri) {
        StorageReference videoRef = mStorageVideosRef.child(UUID.randomUUID().toString());
        counter = 0;
        if(videoUri != null){
            UploadTask uploadTask = videoRef.putFile(videoUri);
            ProgressDialog progressDialog = new ProgressDialog(getActivity());
            progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            progressDialog.setMessage(getResources().getString(R.string.uploading));
            progressDialog.setCancelable(false);
            progressDialog.show();
            uploadTask.addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                    progressDialog.dismiss();
                    imgAdded.setImageResource(0);

                    videoRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            videoUrl = uri.toString();
                            if(videoUrl != null) {
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
                                        0,
                                        currentUser.getCity(),
                                        currentUser.getCountry(),
                                        null,
                                        videoUrl);
                                publications.child(publicationId).setValue(publication).addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        loadPublicationsFromFirebase();
                                        videoUrl = null;
                                        video = false;
                                    }
                                });
                            }
                        }
                    });
                }
            }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                    updateProgress(progressDialog, taskSnapshot);
                }
            });
        }else {
            Toast.makeText(getContext(), "Nothing to upload", Toast.LENGTH_SHORT).show();
        }

    }

    private void updateProgress(ProgressDialog progressDialog, UploadTask.TaskSnapshot taskSnapshot) {
        double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
        progressDialog.setProgress((int)progress);
        Log.i("progressupdate", progress +  "");
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.social_fragment, container, false);
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        if(Config.getUrlFromString(s.toString()) != null) {
            Linkify.addLinks(edtPublication, Linkify.WEB_URLS);
            edtPublication.setLinkTextColor(ContextCompat.getColor(getActivity(),
                    R.color.colorAccent));

            //new HttpGet().execute(Config.getUrlFromString(s.toString()));

        } else {
            linkView.setVisibility(View.GONE);
        }
    }

    @Override
    public void afterTextChanged(Editable s) {
    }


    class HttpGet extends AsyncTask<String, Void, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            /*if(!spotsDialog.isShowing())
                spotsDialog.show();*/
        }
        String urlString;
        @Override
        protected String doInBackground(String... params) {
            // These two need to be declared outside the try/catch
            // so that they can be closed in the finally block.

            urlString = params[0];
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            // Will contain the raw JSON response as a string.
            String forecastJsonStr = null;

            try {
                // Construct the URL for the OpenWeatherMap query
                // Possible parameters are avaiable at OWM's forecast API page, at
                // http://openweathermap.org/API#forecast
                URL url = new URL(urlString);

                // Create the request to OpenWeatherMap, and open the connection
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                // Read the input stream into a String
                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();
                if (inputStream == null) {
                    // Nothing to do.
                    return null;
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = reader.readLine()) != null) {
                    // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                    // But it does make debugging a *lot* easier if you print out the completed
                    // buffer for debugging.
                    buffer.append(line + "\n");
                }

                if (buffer.length() == 0) {
                    // Stream was empty.  No point in parsing.
                    return null;
                }
                forecastJsonStr = buffer.toString();

                return forecastJsonStr;
            } catch (IOException e) {
                Log.e("PlaceholderFragment", "Error ", e);
                // If the code didn't successfully get the weather data, there's no point in attemping
                // to parse it.
                return null;
            } finally{
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) {
                        Log.e("PlaceholderFragment", "Error closing stream", e);
                    }
                }
            }
        }

        @Override
        protected void onPostExecute(String response) {
            super.onPostExecute(response);
            //spotsDialog.dismiss();

            if(response != null)
                if(!response.equals("null")) {
                    Document doc = Jsoup.parse(response);

                    if(response.contains("<meta property=\"og:image\" content=\"")) {
                        String s = response.substring(response.indexOf("<meta property=\"og:image\" content=\""));
                        s = s.replace("<meta property=\"og:image\" content=\"", "");
                        String imgUrl = s.substring(0, s.indexOf("\""));
                        Link link = new Link(doc.title(),urlString, imgUrl);
                        linkView.setLinkInfo(link);
                        Log.i("ragging", imgUrl);
                    }
                    else if(response.contains("<img")) {
                        String img = response.substring(response.indexOf("<img"));
                        String sub = img.substring(img.indexOf("src=\"") + 5);
                        String imgUrl = sub.substring(0, sub.indexOf("\""));
                        linkView.setVisibility(View.VISIBLE);
                        Log.i("ragging", imgUrl);
                        if (imgUrl.isEmpty() || !URLUtil.isValidUrl(imgUrl) || !(imgUrl.contains("png") || imgUrl.contains("jpg") || imgUrl.contains("jpeg")))
                            imgUrl = "https://firebasestorage.googleapis.com/v0/b/difpridriver-6dc47.appspot.com/o/img_not_found.png?alt=media&token=dfc6b1f3-2027-40fb-96dc-f67b74529fd5";


                        Link link = new Link(doc.title(), urlString, imgUrl);
                        linkView.setLinkInfo(link);
                    } else {
                        String imgUrl = "https://firebasestorage.googleapis.com/v0/b/difpridriver-6dc47.appspot.com/o/img_not_found.png?alt=media&token=dfc6b1f3-2027-40fb-96dc-f67b74529fd5";
                        Link link = new Link(doc.title(), urlString , imgUrl);
                        linkView.setLinkInfo(link);
                    }
                }

        }
    }

    @Override
    public void onPause() {
        super.onPause();
        JCVideoPlayer.releaseAllVideos();
    }

}

package app.m26.wikidriver.activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.webkit.URLUtil;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import app.m26.wikidriver.LinkView;
import app.m26.wikidriver.R;
import app.m26.wikidriver.adapters.CommentsAdapter;
import app.m26.wikidriver.adapters.ImagesAdapter;
import app.m26.wikidriver.models.Comment;
import app.m26.wikidriver.models.Link;
import app.m26.wikidriver.models.Publication;
import app.m26.wikidriver.models.User;
import app.m26.wikidriver.utils.Config;
import app.m26.wikidriver.utils.ImgHelper;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubeThumbnailLoader;
import com.google.android.youtube.player.YouTubeThumbnailView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.BufferedReader;
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
import fm.jiecao.jcvideoplayer_lib.JCVideoPlayerStandard;

import static android.view.View.GONE;

public class AddCommentActivity extends AppCompatActivity {

    private Bundle extras;
    private RecyclerView imgRecyclerView;
    private ImageView imgAddPhoto, imgPic, imgImportedPhoto;
    private int commentsNumber;
    private Publication publication;
    private String publicationId, toolbarTitle;
    private List<Comment> commentList = new ArrayList<>();
    private EditText edtComment;
    private Button btnPost;
    private TextView txtName, txtTimeStamp, txtPublication;
    public static TextView txtComments;
    private CircleImageView profileIcon, commentProfileIcon;
    private RecyclerView recyclerView;
    private String reference;
    private JCVideoPlayerStandard jcVideoPlayerStandard;
    private LinkView linkView;
    private YouTubeThumbnailView youTubePlayerView;
    private RelativeLayout youtubeLayout;
    private ImageView ytErrorImage, imgYoutube;
    private RecyclerView.LayoutManager layoutManager, gridLayoutManager;
    private DatabaseReference publicationsReference, usersReference;
    private StorageReference mStorageRef = FirebaseStorage.getInstance().getReference(Config.FIREBASE_PUBLICATION_IMAGES_REFERENCE);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_comment);

        imgYoutube = findViewById(R.id.imgYoutubePlay);
        jcVideoPlayerStandard = findViewById(R.id.videoplayer);
        ytErrorImage = findViewById(R.id.ytErrorImage);
        youTubePlayerView = findViewById(R.id.ytView);
        linkView = findViewById(R.id.linkView);
        youtubeLayout = findViewById(R.id.youtubeLayout);
        imgRecyclerView = findViewById(R.id.imgRecyclerView);
        imgImportedPhoto = findViewById(R.id.imgImportedPhoto);
        imgAddPhoto = findViewById(R.id.imgPhoto);
        btnPost = findViewById(R.id.btnPost);
        txtName = findViewById(R.id.txtFullName);
        txtTimeStamp = findViewById(R.id.txtTimeStamp);
        txtPublication = findViewById(R.id.txtPublication);
        txtComments = findViewById(R.id.txtComments);
        profileIcon = findViewById(R.id.profilePic);
        commentProfileIcon = findViewById(R.id.commentProfilePic);
        edtComment = findViewById(R.id.edtComment);
        recyclerView = findViewById(R.id.recyclerView);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        usersReference = FirebaseDatabase.getInstance().getReference(Config.FIREBASE_USERS_REFERENCE);

         //TODO commentsAdapter, comment_item_layout, publicationsFirebase into views.
        extras = getIntent().getExtras();
        if(extras != null) {
            reference = extras.getString("reference");
            publicationId = extras.getString("publicationId");
            publicationsReference = FirebaseDatabase.getInstance().getReference(reference);
        }

        loadCommentsList();

        btnPost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String content = edtComment.getText().toString();
                if (!content.isEmpty() || imageUri != null) {
                    String commentId = publicationsReference.child(publicationId).push().getKey();
                    long timeStamp = System.currentTimeMillis();
                    edtComment.setText("");
                    imgImportedPhoto.setImageBitmap(null);
                    if (imageUri != null) {
                        StorageReference imgRef = mStorageRef.child(UUID.randomUUID().toString());
                        imgRef.putFile(imageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                imgRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                    @Override
                                    public void onSuccess(Uri uri) {
                                        Comment comment = new Comment(
                                                commentId,
                                                Config.getCurrentUser(getApplicationContext()).getUserId(),
                                                content,
                                                timeStamp,
                                                uri.toString());
                                        if (commentList != null)
                                            commentList.add(comment);
                                        else {
                                            commentList = new ArrayList<>();
                                            commentList.add(comment);
                                        }
                                        publication.setCommentList(commentList);
                                        commentsNumber += 1;
                                        publication.setNumberOfComments(commentsNumber);

                                        publicationsReference.child(publicationId).setValue(publication).addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                edtComment.setText("");
                                                loadCommentsList();

                                            }
                                        });
                                    }
                                });
                            }
                        });
                        return;
                    }
                    if (imageUri == null) {
                        Comment comment = new Comment(
                                commentId,
                                Config.getCurrentUser(getApplicationContext()).getUserId(),
                                content,
                                timeStamp,
                                "none");
                        if (commentList != null)
                            commentList.add(comment);
                        else {
                            commentList = new ArrayList<>();
                            commentList.add(comment);
                        }
                        publication.setCommentList(commentList);
                        commentsNumber += 1;
                        publication.setNumberOfComments(commentsNumber);

                        publicationsReference.child(publicationId).setValue(publication).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                loadCommentsList();
                            }
                        });
                    }
                }
            }
        });

        imgAddPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
                photoPickerIntent.setType("image/*");
                startActivityForResult(photoPickerIntent, 1);
            }
        });

    }

    private Bitmap finalBitmap;
    private Uri imageUri;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == 1) {
            if(resultCode == RESULT_OK){
                try {
                    imageUri = data.getData();
                    final InputStream imageStream = getContentResolver().openInputStream(imageUri);
                    final Bitmap selectedImage = BitmapFactory.decodeStream(imageStream);
                    finalBitmap = ImgHelper.getRoundedCornerBitmap(selectedImage, 100);
                    imgImportedPhoto.setImageBitmap(finalBitmap);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }

            }
        }
    }

    private void setupToolbar(String toolbarTitle) {
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle(toolbarTitle);
        toolbar.setTitleTextColor(getResources().getColor(android.R.color.white));
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void loadCommentsList() {
        publicationsReference.child(publicationId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                publication = dataSnapshot.getValue(Publication.class);
                commentsNumber = publication.getNumberOfComments();
                setupToolbar(publication.getContent());
                txtPublication.setText(publication.getContent());
                txtTimeStamp.setText(Config.epochToDate(publication.getTimeStamp(), "MMMM d  h:mm a"));
                txtComments.setText(String.format("%d " + getResources().getString(R.string.comments), publication.getNumberOfComments()));

                if(publication.getImgUrlList() != null) {
                    Log.i("tagging", publication.getImgUrlList() + "");
                    gridLayoutManager = new StaggeredGridLayoutManager(publication.getImgUrlList().size(), StaggeredGridLayoutManager.VERTICAL);
                    imgRecyclerView.setLayoutManager(gridLayoutManager);
                    ImagesAdapter imagesAdapter = new ImagesAdapter(getApplicationContext(), publication.getImgUrlList());
                    imgRecyclerView.setAdapter(imagesAdapter);
                }

                commentList = publication.getCommentList();
                if(commentList != null) {
                    CommentsAdapter adapter = new CommentsAdapter(getApplicationContext(), commentList, reference, publicationId);
                    recyclerView.setAdapter(adapter);
                }

                new HttpGet().execute(Config.getUrlFromString(publication.getContent()), publication.getContent());


                if(reference.equals(Config.FIREBASE_SOCIAL_REFERENCE))
                    if(!publication.getVideoUrl().equals("null")) {
                        if(publication.getContent().isEmpty() || publication.getContent().equals(" "))
                            txtPublication.setVisibility(GONE);

                        jcVideoPlayerStandard.setVisibility(View.VISIBLE);
                        jcVideoPlayerStandard.setUp(publication.getVideoUrl()
                                , JCVideoPlayerStandard.SCREEN_LAYOUT_NORMAL, "");

                    } else {
                        jcVideoPlayerStandard.setVisibility(GONE);
                    }

                usersReference.child(publication.getUserId()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        User user = dataSnapshot.getValue(User.class);

                        txtName.setText(String.format("%s %s", user.getFirstName(), user.getLastName()));

                        Picasso.with(getApplicationContext())
                                .load(user.getThumbnail())
                                .error(R.drawable.profile_icon)
                                .into(profileIcon);

                        Picasso.with(getApplicationContext())
                                .load(Config.getCurrentUser(getApplicationContext()).getThumbnail())
                                .error(R.drawable.profile_icon)
                                .into(commentProfileIcon);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == android.R.id.home)
            onBackPressed();
        return super.onOptionsItemSelected(item);
    }

    class HttpGet extends AsyncTask<String, Void, String> {
        

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }
        String urlString;
        String content;
        @Override
        protected String doInBackground(String... params) {
            // These two need to be declared outside the try/catch
            // so that they can be closed in the finally block.

            urlString = params[0];
            content = params[1];

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
            Log.i("url", content);
            String url = Config.getUrlFromString(content);
            linkView.setVisibility(GONE);
            youtubeLayout.setVisibility(GONE);
            if(response != null && url != null)
                if(!response.equals("null")) {
                    Document doc = Jsoup.parse(response);
                    if(url.contains("youtube") || url.contains("youtu.be")) {
                        youtubeLayout.setVisibility(View.VISIBLE);
                        String VIDEO_ID = Config.getVideoIdFromUrl(response);

                        response = content.replace(Config.getUrlFromString(content), "");
                        if(response.isEmpty()|| response.equals(""))
                            txtPublication.setVisibility(GONE);
                        YouTubeThumbnailLoader.OnThumbnailLoadedListener thumbnailLoadedListener = new YouTubeThumbnailLoader.OnThumbnailLoadedListener() {
                            @Override
                            public void onThumbnailLoaded(YouTubeThumbnailView youTubeThumbnailView, String s) {
                                youTubeThumbnailView.setVisibility(View.VISIBLE);
                                ytErrorImage.setVisibility(GONE);

                            }

                            @Override
                            public void onThumbnailError(YouTubeThumbnailView youTubeThumbnailView, YouTubeThumbnailLoader.ErrorReason errorReason) {
                                ytErrorImage.setBackgroundResource(R.drawable.ic_youtube_error);
                                imgYoutube.setVisibility(GONE);
                                youTubeThumbnailView.setVisibility(GONE);
                                //PublicationsAdapter.this.notifyItemChanged(Integer.parseInt(position));
                            }
                        };
                        youTubePlayerView.initialize(Config.API_KEY, new YouTubeThumbnailView.OnInitializedListener() {
                            @Override
                            public void onInitializationSuccess(YouTubeThumbnailView youTubeThumbnailView, YouTubeThumbnailLoader youTubeThumbnailLoader) {
                                youTubeThumbnailLoader.setVideo(VIDEO_ID);
                                youTubeThumbnailLoader.setOnThumbnailLoadedListener(thumbnailLoadedListener);
                            }

                            @Override
                            public void onInitializationFailure(YouTubeThumbnailView youTubeThumbnailView, YouTubeInitializationResult youTubeInitializationResult) {

                            }
                        });

                        youtubeLayout.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("vnd.youtube:"+VIDEO_ID));
                                intent.putExtra("VIDEO_ID", VIDEO_ID);
                                getApplicationContext().startActivity(intent);
                            }
                        });
                    } else if(url.contains("facebook.com")){
                        String imgUrl = "";
                        Link link = new Link(doc.title(), Config.getUrlFromString(content), imgUrl);
                        linkView.setVisibility(View.VISIBLE);
                        linkView.setLinkLocalImage(getApplicationContext(), link);
                    } else {
                        if (response.contains("<meta property=\"og:image\" content=\"")) {
                            String s = response.substring(response.indexOf("<meta property=\"og:image\" content=\""));
                            s = s.replace("<meta property=\"og:image\" content=\"", "");
                            String imgUrl = s.substring(0, s.indexOf("\""));
                            Link link = new Link(doc.title(), Config.getUrlFromString(content), imgUrl);
                            linkView.setVisibility(View.VISIBLE);
                            linkView.setLinkInfo(getApplicationContext(), link);
                            Log.i("ragging", imgUrl);
                        } else if (response.contains("<img")) {
                            String img = response.substring(response.indexOf("<img"));
                            String sub = img.substring(img.indexOf("src=\"") + 5);
                            String imgUrl = sub.substring(0, sub.indexOf("\""));
                            linkView.setVisibility(View.VISIBLE);
                            Log.i("ragging", imgUrl);
                            if (imgUrl.isEmpty() || !URLUtil.isValidUrl(imgUrl) || !(imgUrl.contains("png") || imgUrl.contains("jpg") || imgUrl.contains("jpeg")))
                                imgUrl = "https://firebasestorage.googleapis.com/v0/b/difpridriver-6dc47.appspot.com/o/img_not_found.png?alt=media&token=dfc6b1f3-2027-40fb-96dc-f67b74529fd5";


                            Link link = new Link(doc.title(), Config.getUrlFromString(content), imgUrl);
                            linkView.setLinkInfo(getApplicationContext(), link);
                        } else {
                            String imgUrl = "https://firebasestorage.googleapis.com/v0/b/difpridriver-6dc47.appspot.com/o/img_not_found.png?alt=media&token=dfc6b1f3-2027-40fb-96dc-f67b74529fd5";
                            Link link = new Link(doc.title(), Config.getUrlFromString(content), imgUrl);
                            linkView.setVisibility(View.VISIBLE);
                            linkView.setLinkInfo(getApplicationContext(), link);
                        }
                    }

                    txtPublication.setVisibility(GONE);
                }

        }
    }

}

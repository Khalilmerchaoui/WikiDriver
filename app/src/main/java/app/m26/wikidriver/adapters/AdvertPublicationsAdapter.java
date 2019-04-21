package app.m26.wikidriver.adapters;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.text.util.Linkify;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.URLUtil;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.VideoView;

import app.m26.wikidriver.LinkView;
import app.m26.wikidriver.R;;
import app.m26.wikidriver.activities.AddCommentActivity;
import app.m26.wikidriver.activities.ChatActivity;
import app.m26.wikidriver.activities.WebActivity;
import app.m26.wikidriver.models.Link;
import app.m26.wikidriver.models.Publication;
import app.m26.wikidriver.models.User;
import app.m26.wikidriver.utils.Config;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubeThumbnailLoader;
import com.google.android.youtube.player.YouTubeThumbnailView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Collections;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import dmax.dialog.SpotsDialog;
import fm.jiecao.jcvideoplayer_lib.JCVideoPlayerStandard;

public class AdvertPublicationsAdapter extends RecyclerView.Adapter<AdvertPublicationsAdapter.ViewHolder> implements YouTubePlayer.OnInitializedListener{

    private Context context;
    private List<Publication> publicationList;
    private DatabaseReference usersReference = FirebaseDatabase.getInstance().getReference(Config.FIREBASE_USERS_REFERENCE);
    private DatabaseReference publicationsReference;
    private RecyclerView.LayoutManager layoutManager;
    private String reference;
    private SpotsDialog spotsDialog;
    private AdvertPublicationsAdapter.ViewHolder viewHolder;
    private Publication currentPublication;

    public AdvertPublicationsAdapter(Context context, List<Publication> publicationList, String reference) {
        this.context = context;
        this.publicationList = publicationList;
        Collections.reverse(this.publicationList);
        this.publicationsReference = FirebaseDatabase.getInstance().getReference(reference);
        this.reference = reference;
        spotsDialog = new SpotsDialog(context, R.style.CustomRegister);
    }

    @NonNull
    @Override
    public AdvertPublicationsAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View itemView = inflater.inflate(R.layout.advert_publication_item_layout, parent, false);
        return new AdvertPublicationsAdapter.ViewHolder(itemView);
    }

    YouTubePlayer youtubePlayer;

    @Override
    public void onBindViewHolder(final @NonNull AdvertPublicationsAdapter.ViewHolder holder, int position) {

        final Publication publication = publicationList.get(position);
        this.currentPublication = publication;

        User currentUser = Config.getCurrentUser(context);

        usersReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                User user;
                for(DataSnapshot postSnapshot : dataSnapshot.getChildren())
                {
                    user = postSnapshot.getValue(User.class);
                    if(user.getUserId().equals(publication.getUserId())) {
                        holder.txtName.setText(user.getFirstName() + " " + user.getLastName());
                        Picasso.with(context)
                                .load(user.getThumbnail())
                                .error(R.drawable.profile_icon)
                                .fit()
                                .into(holder.profileIcon);

                        break;
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        /*Picasso.with(context)
                .load(publication.getImgUrl())
                .into(holder.imgPic);
*/

        if(reference.equals(Config.FIREBASE_SOCIAL_REFERENCE))
            if(!publication.getVideoUrl().equals("null")) {
                if(publication.getContent().isEmpty() || publication.getContent().equals(" "))
                    holder.txtPublication.setVisibility(View.GONE);

               /*holder.videoView.setVisibility(View.VISIBLE);
               MediaController mediaController = new MediaController(context);
               mediaController.setAnchorView(holder.videoView);
               holder.videoView.setMediaController(mediaController);
               holder.videoView.setVideoPath("https://firebasestorage.googleapis.com/v0/b/foodorderingapp-1a5a2.appspot.com/o/video-1546368630.mp4?alt=media&token=54ba0ddb-39a8-41b9-9ecb-3db5face4c80");
               holder.videoView.seekTo(10);
*/
              // holder.videoView.start();
                holder.jcVideoPlayerStandard.setVisibility(View.VISIBLE);
                holder.jcVideoPlayerStandard.setUp(publication.getVideoUrl()
                        , JCVideoPlayerStandard.SCREEN_LAYOUT_NORMAL, "");
                /*Picasso.with(context)
                        .load("http://p.qpic.cn/videoyun/0/2449_43b6f696980311e59ed467f22794e792_1/640")
                        .into(holder.jcVideoPlayerStandard.thumbImageView);*/
        } else {
                holder.jcVideoPlayerStandard.setVisibility(View.GONE);
            }

        try {
            holder.txtPublication.setText(publication.getContent().replace(Config.getUrlFromString(publication.getContent()), ""));
            if(holder.txtPublication.getText().toString().isEmpty())
                holder.txtPublication.setVisibility(View.GONE);
        } catch (NullPointerException e) {
            holder.txtPublication.setText(publication.getContent());
        }
        if(Config.getUrlFromString(publication.getContent()) != null) {
            holder.linkView.setVisibility(View.VISIBLE);
            Linkify.addLinks(holder.txtPublication, Linkify.WEB_URLS);
            holder.txtPublication.setLinkTextColor(ContextCompat.getColor(context,
                    R.color.colorAccent));
            holder.txtPublication.setLinksClickable(true);

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    new HttpGet(holder).execute(Config.getUrlFromString(publication.getContent()), String.valueOf(position), publication.getContent());
                }
            }, 50);

        } else {
            holder.linkView.setVisibility(View.GONE);
        }

        holder.txtComments.setText(String.format("%d " + context.getResources().getString(R.string.comments), publication.getNumberOfComments()));
        holder.txtTimeStamp.setText(Config.epochToDate(publication.getTimeStamp(), "MMMM d  h:mm a"));

        holder.commentLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent commentIntent = new Intent(context, AddCommentActivity.class);
                commentIntent.putExtra("reference", reference);
                commentIntent.putExtra("publicationId", publication.getPublicationId());
                context.startActivity(commentIntent);
            }
        });

        if (publication.getUserId().equals(Config.getCurrentUser(context).getUserId())) {
            final PopupMenu popup = new PopupMenu(context,  holder.imgBtnMore);
            popup.getMenuInflater()
                    .inflate(R.menu.popup_menu, popup.getMenu());

            holder.imgBtnMore.setVisibility(View.VISIBLE);
            holder.imgBtnMore.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    popup.show();
                }
            });

            popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    if(item.getItemId() == R.id.menu_delete) {
                        publicationsReference.child(publication.getPublicationId()).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                popup.dismiss();
                                publicationList.remove(position);
                                AdvertPublicationsAdapter.this.notifyItemRemoved(position);
                                //AdvertPublicationsAdapter.this.notifyItemRangeChanged(position, getItemCount());

                            }
                        });
                    }
                    return false;
                }
            });

            holder.msgLayout.setVisibility(View.GONE);
            holder.menusLayout.setWeightSum(1);
        }

        holder.linkView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /*Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(holder.linkView.getLinkInfo().getUrl()));
                context.startActivity(intent);*/

                Intent webIntent = new Intent(context, WebActivity.class);
                webIntent.putExtra("title", holder.linkView.getLinkInfo().getTitle());
                webIntent.putExtra("url", holder.linkView.getLinkInfo().getUrl());
                context.startActivity(webIntent);
            }
        });

        holder.commentsLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent commentIntent = new Intent(context, AddCommentActivity.class);
                commentIntent.putExtra("publicationId", publication.getPublicationId());
                commentIntent.putExtra("reference", reference);
                context.startActivity(commentIntent);
            }
        });

        if(publication.getImgUrlList() != null) {
            layoutManager = new StaggeredGridLayoutManager(publication.getImgUrlList().size(), StaggeredGridLayoutManager.VERTICAL);
            holder.imgRecyclerView.setLayoutManager(layoutManager);
            ImagesAdapter imagesAdapter = new ImagesAdapter(context, publication.getImgUrlList());
            holder.imgRecyclerView.setAdapter(imagesAdapter);
        }

        holder.msgLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(context, ChatActivity.class);
                i.putExtra("user_id", publication.getUserId());
                context.startActivity(i);
            }
        });
    }

    @Override
    public void onInitializationSuccess(YouTubePlayer.Provider provider, YouTubePlayer player, boolean wasRestored) {
        youtubePlayer = player;
    }

    @Override
    public void onInitializationFailure(YouTubePlayer.Provider provider, YouTubeInitializationResult youTubeInitializationResult) {

    }

    private class HttpGet extends AsyncTask<String, Void, String> {

        AdvertPublicationsAdapter.ViewHolder viewHolder;

        public HttpGet(ViewHolder viewHolder) {
            this.viewHolder = viewHolder;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }
        String urlString;
        String position, content;
        @Override
        protected String doInBackground(String... params) {
            // These two need to be declared outside the try/catch
            // so that they can be closed in the finally block.

            urlString = params[0];
            position = params[1];
            content = params[2];

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
            currentPublication.setContent(content);
            Log.i("url", currentPublication.getContent());
            String url = Config.getUrlFromString(currentPublication.getContent());
            viewHolder.linkView.setVisibility(View.GONE);
            viewHolder.youtubeLayout.setVisibility(View.GONE);
            if(response != null && url != null)
                if(!response.equals("null")) {
                    Document doc = Jsoup.parse(response);
                    if(url.contains("youtube") || url.contains("youtu.be")) {
                        viewHolder.youtubeLayout.setVisibility(View.VISIBLE);
                        String VIDEO_ID = Config.getVideoIdFromUrl(response);

                        response = currentPublication.getContent().replace(Config.getUrlFromString(currentPublication.getContent()), "");
                        if(response.isEmpty()|| response.equals(""))
                            viewHolder.txtPublication.setVisibility(View.GONE);
                        YouTubeThumbnailLoader.OnThumbnailLoadedListener thumbnailLoadedListener = new YouTubeThumbnailLoader.OnThumbnailLoadedListener() {
                            @Override
                            public void onThumbnailLoaded(YouTubeThumbnailView youTubeThumbnailView, String s) {
                                youTubeThumbnailView.setVisibility(View.VISIBLE);
                                viewHolder.ytErrorImage.setVisibility(View.GONE);

                            }

                            @Override
                            public void onThumbnailError(YouTubeThumbnailView youTubeThumbnailView, YouTubeThumbnailLoader.ErrorReason errorReason) {
                                viewHolder.ytErrorImage.setBackgroundResource(R.drawable.ic_youtube_error);
                                viewHolder.imgYoutube.setVisibility(View.GONE);
                                youTubeThumbnailView.setVisibility(View.GONE);
                                //PublicationsAdapter.this.notifyItemChanged(Integer.parseInt(position));
                            }
                        };
                        viewHolder.youTubePlayerView.initialize(Config.API_KEY, new YouTubeThumbnailView.OnInitializedListener() {
                            @Override
                            public void onInitializationSuccess(YouTubeThumbnailView youTubeThumbnailView, YouTubeThumbnailLoader youTubeThumbnailLoader) {
                                youTubeThumbnailLoader.setVideo(VIDEO_ID);
                                youTubeThumbnailLoader.setOnThumbnailLoadedListener(thumbnailLoadedListener);
                            }

                            @Override
                            public void onInitializationFailure(YouTubeThumbnailView youTubeThumbnailView, YouTubeInitializationResult youTubeInitializationResult) {

                            }
                        });

                        viewHolder.youtubeLayout.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("vnd.youtube:"+VIDEO_ID));
                                intent.putExtra("VIDEO_ID", VIDEO_ID);
                                context.startActivity(intent);
                            }
                        });
                    } else if(url.contains("facebook.com")){
                        String imgUrl = "";
                        Link link = new Link(doc.title(), Config.getUrlFromString(currentPublication.getContent()), imgUrl);
                        viewHolder.linkView.setVisibility(View.VISIBLE);
                        viewHolder.linkView.setLinkLocalImage(context, link);
                    } else {
                        if (response.contains("<meta property=\"og:image\" content=\"")) {
                            String s = response.substring(response.indexOf("<meta property=\"og:image\" content=\""));
                            s = s.replace("<meta property=\"og:image\" content=\"", "");
                            String imgUrl = s.substring(0, s.indexOf("\""));
                            Link link = new Link(doc.title(), Config.getUrlFromString(currentPublication.getContent()), imgUrl);
                            viewHolder.linkView.setVisibility(View.VISIBLE);
                            viewHolder.linkView.setLinkInfo(context, link);
                            Log.i("ragging", imgUrl);
                        } else if (response.contains("<img")) {
                            String img = response.substring(response.indexOf("<img"));
                            String sub = img.substring(img.indexOf("src=\"") + 5);
                            String imgUrl = sub.substring(0, sub.indexOf("\""));
                            viewHolder.linkView.setVisibility(View.VISIBLE);
                            Log.i("ragging", imgUrl);
                            if (imgUrl.isEmpty() || !URLUtil.isValidUrl(imgUrl) || !(imgUrl.contains("png") || imgUrl.contains("jpg") || imgUrl.contains("jpeg")))
                                imgUrl = "https://firebasestorage.googleapis.com/v0/b/difpridriver-6dc47.appspot.com/o/img_not_found.png?alt=media&token=dfc6b1f3-2027-40fb-96dc-f67b74529fd5";


                            Link link = new Link(doc.title(), Config.getUrlFromString(currentPublication.getContent()), imgUrl);
                            viewHolder.linkView.setLinkInfo(context, link);
                        } else {
                            String imgUrl = "https://firebasestorage.googleapis.com/v0/b/difpridriver-6dc47.appspot.com/o/img_not_found.png?alt=media&token=dfc6b1f3-2027-40fb-96dc-f67b74529fd5";
                            Link link = new Link(doc.title(), Config.getUrlFromString(currentPublication.getContent()), imgUrl);
                            viewHolder.linkView.setVisibility(View.VISIBLE);
                            viewHolder.linkView.setLinkInfo(context, link);
                        }
                    }
                }

        }
    }


    @Override
    public int getItemCount() {
        return publicationList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        private TextView txtName, txtTimeStamp, txtPublication, txtComments, txtLikes, txtDislikes;
        private ImageView imgYoutube ;
        private RecyclerView imgRecyclerView;
        private CircleImageView profileIcon;
        private LinearLayout commentLayout, commentsLayout, msgLayout ,menusLayout;
        private ImageView imgBtnMore;
        private VideoView videoView;
        private JCVideoPlayerStandard jcVideoPlayerStandard;
        private LinkView linkView;
        private YouTubeThumbnailView youTubePlayerView;
        private RelativeLayout youtubeLayout;
        private ImageView ytErrorImage;


        public ViewHolder(View itemView) {
            super(itemView);

            menusLayout = itemView.findViewById(R.id.likesLayout);
            msgLayout = itemView.findViewById(R.id.msgLayout);
            jcVideoPlayerStandard = itemView.findViewById(R.id.videoplayer);
            ytErrorImage = itemView.findViewById(R.id.ytErrorImage);
            youTubePlayerView = itemView.findViewById(R.id.ytView);
            linkView = itemView.findViewById(R.id.linkView);
            youtubeLayout = itemView.findViewById(R.id.youtubeLayout);
            //videoView = itemView.findViewById(R.id.VidView);
            imgRecyclerView = itemView.findViewById(R.id.imgRecyclerView);
            imgYoutube = itemView.findViewById(R.id.imgYoutubePlay);
            imgBtnMore = itemView.findViewById(R.id.imgBtnMore);
            txtLikes = itemView.findViewById(R.id.txtLikes);
            txtDislikes = itemView.findViewById(R.id.txtDisLikes);
            txtName = itemView.findViewById(R.id.txtFullName);
            txtTimeStamp = itemView.findViewById(R.id.txtTimeStamp);
            txtPublication = itemView.findViewById(R.id.txtPublication);
            txtComments = itemView.findViewById(R.id.txtComments);
            profileIcon = itemView.findViewById(R.id.profilePic);
            commentLayout = itemView.findViewById(R.id.commentLayout);
            commentsLayout = itemView.findViewById(R.id.commentsLayout);

        }
    }

}

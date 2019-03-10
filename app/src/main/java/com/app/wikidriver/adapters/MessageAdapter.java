package com.app.wikidriver.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.app.wikidriver.R;
import com.app.wikidriver.models.Messages;
import com.app.wikidriver.models.User;
import com.app.wikidriver.utils.Config;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by AkshayeJH on 24/07/17.
 */

public class MessageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{

    public static final int VIEW_TYPE_USER_MESSAGE = 0;
    public static final int VIEW_TYPE_CHATTER_MESSAGE = 1;
    private List<Messages> mMessageList;
    private DatabaseReference mUserDatabase;
    private Context context;

    public MessageAdapter(Context context, List<Messages> mMessageList) {
        this.context = context;
        this.mMessageList = mMessageList;

    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        if(viewType == VIEW_TYPE_CHATTER_MESSAGE) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.message_single_layout_incoming, parent, false);
            return new ChatterViewHolder(v);
        }
        else if(viewType == VIEW_TYPE_USER_MESSAGE) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.message_single_layout_outgoing, parent, false);
            return new UserViewHolder(v);
        }
        return null;
    }

    public class ChatterViewHolder extends RecyclerView.ViewHolder {

        public TextView messageText, messageTime;
        public CircleImageView profileImage;
       // public TextView displayName;
        //public ImageView messageImage;

        public ChatterViewHolder(View view) {
            super(view);
            messageText =  view.findViewById(R.id.message_text_layout);
            messageTime = view.findViewById(R.id.time_text_layout);
            profileImage =  view.findViewById(R.id.message_profile_layout);
            //displayName = (TextView) view.findViewById(R.id.name_text_layout);
            //messageImage = (ImageView) view.findViewById(R.id.message_image_layout);

        }
    }

    public class UserViewHolder extends RecyclerView.ViewHolder {
        public TextView messageText, messageTime;
        public UserViewHolder(View view) {
            super(view);
            messageText = view.findViewById(R.id.user_message_text_layout);
            messageTime = view.findViewById(R.id.user_time_text_layout);

        }
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder viewHolder, int i) {

        Messages c = mMessageList.get(i);

        String from_user = c.getFrom();
        String message_type = c.getType();


        mUserDatabase = FirebaseDatabase.getInstance().getReference().child(Config.FIREBASE_USERS_REFERENCE).child(from_user);

        mUserDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if(viewHolder instanceof ChatterViewHolder) {
                    User user = dataSnapshot.getValue(User.class);
                    if (user != null) {
                        String name = user.getFirstName();
                        String image = user.getThumbnail();
                        // viewHolder.displayName.setText(name);
                        Picasso.with(((ChatterViewHolder) viewHolder).profileImage.getContext()).load(image)
                                .placeholder(R.drawable.profile_icon).into(((ChatterViewHolder) viewHolder).profileImage);
                    }
                }
                else if (viewHolder instanceof UserViewHolder) {

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        if(viewHolder instanceof ChatterViewHolder) {
            if (message_type.equals("text")) {
                ((ChatterViewHolder) viewHolder).messageText.setText(c.getMessage());
                ((ChatterViewHolder)viewHolder).messageTime.setText(EpochToDate(c.getTime(), "HH:mm"));
                // viewHolder.messageImage.setVisibility(View.INVISIBLE);
            } else {
                ((ChatterViewHolder) viewHolder).messageText.setVisibility(View.INVISIBLE);
            /*Picasso.with(viewHolder.profileImage.getContext()).load(c.getMessage())
                    .placeholder(R.mipmap.default_avatar).into(viewHolder.messageImage);
*/
            }
        }
        else if(viewHolder instanceof UserViewHolder) {
            if (message_type.equals("text")) {
                ((UserViewHolder) viewHolder).messageText.setText(c.getMessage());
                ((UserViewHolder)viewHolder).messageTime.setText(EpochToDate(c.getTime(), "HH:mm"));
                // viewHolder.messageImage.setVisibility(View.INVISIBLE);
            } else {
                ((UserViewHolder) viewHolder).messageText.setVisibility(View.INVISIBLE);
            /*Picasso.with(viewHolder.profileImage.getContext()).load(c.getMessage())
                    .placeholder(R.mipmap.default_avatar).into(viewHolder.messageImage);
*/
            }
        }
    }
    public static String EpochToDate(long time, String formatString) {
        SimpleDateFormat format = new SimpleDateFormat(formatString);
        return format.format(new Date(time));

    }
    @Override
    public int getItemViewType(int position) {
        return mMessageList.get(position).getFrom().equals(Config.getCurrentUser(context).getUserId()) ? VIEW_TYPE_USER_MESSAGE : VIEW_TYPE_CHATTER_MESSAGE;
    }
    @Override
    public int getItemCount() {
        return mMessageList.size();
    }

}

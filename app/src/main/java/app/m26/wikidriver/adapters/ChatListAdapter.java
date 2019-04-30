package app.m26.wikidriver.adapters;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import app.m26.wikidriver.R;;
import app.m26.wikidriver.activities.ChatActivity;
import app.m26.wikidriver.models.Messages;
import app.m26.wikidriver.models.User;
import app.m26.wikidriver.utils.Config;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;
import de.hdodenhof.circleimageview.CircleImageView;
import java.util.ArrayList;
import java.util.List;

public class ChatListAdapter extends RecyclerView.Adapter<ChatListAdapter.ViewHolder> {
    private Context context;
    private FirebaseDatabase database;
    private DatabaseReference msgReference;
    private List<String> usersChatIdList;
    private DatabaseReference usersReference;

    public class ViewHolder extends android.support.v7.widget.RecyclerView.ViewHolder {
        private CircleImageView imgProfile;
        private ImageView imgStatus;
        private TextView txtFullName;
        private TextView txtMsg;
        private TextView txtTimeStamp;
        private RelativeLayout userLayout;

        public ViewHolder(View view) {
            super(view);
            this.txtTimeStamp = view.findViewById(R.id.txtTimeStamp);
            this.txtFullName = view.findViewById(R.id.txtFullName);
            this.txtMsg = view.findViewById(R.id.txtMsg);
            this.imgProfile = view.findViewById(R.id.imgProfile);
            this.imgStatus = view.findViewById(R.id.imgStatus);
            this.userLayout = view.findViewById(R.id.userLayout);
        }
    }

    public ChatListAdapter(Context context, List<String> list) {
        this.context = context;
        this.usersChatIdList = list;
        this.database = FirebaseDatabase.getInstance();
        this.usersReference = this.database.getReference(Config.FIREBASE_USERS_REFERENCE);
        this.msgReference = this.database.getReference(Config.FIREBASE_MESSAGES_REFERENCE);
    }

    @NonNull
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        return new ViewHolder(LayoutInflater.from(this.context).inflate(R.layout.chat_item_layout, viewGroup, false));
    }

    public void onBindViewHolder(@NonNull final ViewHolder viewHolder, int i) {
        final String str = this.usersChatIdList.get(i);
        Log.i("usersId", str);
        this.usersReference.child(str).addListenerForSingleValueEvent(new ValueEventListener() {

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                if (user != null) {
                    StringBuilder stringBuilder = new StringBuilder();
                    stringBuilder.append(user.getFirstName());
                    stringBuilder.append(" ");
                    stringBuilder.append(user.getLastName());
                    CharSequence stringBuilder2 = stringBuilder.toString();
                    String thumbnail = user.getThumbnail();
                    if (user.getStatus().contains("online")) {
                        viewHolder.imgStatus.setVisibility(View.VISIBLE);
                        viewHolder.txtTimeStamp.setVisibility(View.GONE);
                    } else {
                        viewHolder.imgStatus.setVisibility(View.GONE);
                        viewHolder.txtTimeStamp.setVisibility(View.VISIBLE);
                    }
                    Picasso.with(context).load(thumbnail).error(R.drawable.profile_icon).into(viewHolder.imgProfile);
                    viewHolder.txtFullName.setText(stringBuilder2);
                    msgReference.child(Config.getCurrentUser(ChatListAdapter.this.context).getUserId()).child(str).orderByKey().limitToLast(1).addChildEventListener(new ChildEventListener() {
                        @Override
                        public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                            Messages messages = dataSnapshot.getValue(Messages.class);
                            if (messages != null) {
                                viewHolder.txtTimeStamp.setText(Config.epochToDate(messages.getTime(), "HH:mm"));
                                viewHolder.txtMsg.setText(messages.getMessage());
                            }
                        }

                        @Override
                        public void onChildChanged(DataSnapshot dataSnapshot, String s) {

                        }

                        @Override
                        public void onChildRemoved(DataSnapshot dataSnapshot) {

                        }

                        @Override
                        public void onChildMoved(DataSnapshot dataSnapshot, String s) {

                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                }
            }
        });
        viewHolder.userLayout.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(context, ChatActivity.class);
                i.putExtra("user_id", str);
                context.startActivity(i);
            }
        });
    }
    @Override
    public int getItemCount() {
        return this.usersChatIdList.size();
    }
}
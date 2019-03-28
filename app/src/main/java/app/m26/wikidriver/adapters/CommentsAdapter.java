package app.m26.wikidriver.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import app.m26.wikidriver.R;;
import app.m26.wikidriver.activities.AddCommentActivity;
import app.m26.wikidriver.models.Comment;
import app.m26.wikidriver.models.Publication;
import app.m26.wikidriver.models.User;
import app.m26.wikidriver.utils.Config;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class CommentsAdapter extends RecyclerView.Adapter<CommentsAdapter.ViewHolder> {

    private Context context;
    private String publicationId;
    private List<Comment> commentList;
    private DatabaseReference publicationsReference;
    private DatabaseReference usersReference = FirebaseDatabase.getInstance().getReference(Config.FIREBASE_USERS_REFERENCE);

    public CommentsAdapter(Context context, List<Comment> commentList, String reference, String publicationId) {
        this.publicationId = publicationId;
        this.context = context;
        this.commentList = commentList;
        this.publicationsReference = FirebaseDatabase.getInstance().getReference(reference);
    }

    @NonNull
    @Override
    public CommentsAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View itemView = inflater.inflate(R.layout.comment_item_layout, parent, false);
        return new CommentsAdapter.ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final @NonNull CommentsAdapter.ViewHolder holder, int position) {
        final Comment comment = commentList.get(position);
        holder.txtComment.setText(comment.getContent());

        Picasso.with(context)
                .load(comment.getImgUrl())
                .into(holder.imgPic);

        usersReference.child(comment.getUserId()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                Picasso.with(context)
                        .load(user.getThumbnail())
                        .error(R.drawable.profile_icon)
                        .into(holder.profileIcon);
                holder.txtName.setText(String.format("%s %s", user.getFirstName(), user.getLastName()));
                holder.txtTimeStamp.setText(String.format("on %s", Config.epochToDate(comment.getTimeStamp(), "EEE")));
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        if (comment.getUserId().equals(Config.getCurrentUser(context).getUserId())) {
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
                        commentList.remove(position);

                        publicationsReference.child(publicationId).child("commentList").setValue(commentList).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                popup.dismiss();
                                CommentsAdapter.this.notifyItemRemoved(position);
                                CommentsAdapter.this.notifyItemRangeChanged(position, getItemCount());
                                publicationsReference.child(publicationId).addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        Publication publication = dataSnapshot.getValue(Publication.class);
                                        int numberOfComments = publication.getNumberOfComments();
                                        numberOfComments--;
                                        AddCommentActivity.txtComments.setText(String.format("%d " + context.getResources().getString(R.string.comments), numberOfComments));
                                        publicationsReference.child(publicationId).child("numberOfComments").setValue(numberOfComments);
                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {

                                    }
                                });

                            }
                        });
                    }
                    return false;
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return commentList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        private TextView txtName, txtComment, txtTimeStamp;
        private CircleImageView profileIcon;
        private ImageView imgPic, imgBtnMore;

        public ViewHolder(View itemView) {
            super(itemView);
            imgBtnMore = itemView.findViewById(R.id.imgBtnMore);
            imgPic = itemView.findViewById(R.id.imgPic);
            txtName = itemView.findViewById(R.id.txtName);
            txtComment = itemView.findViewById(R.id.txtComment);
            txtTimeStamp = itemView.findViewById(R.id.txtTimeStamp);
            profileIcon = itemView.findViewById(R.id.profilePic);
        }
    }
}

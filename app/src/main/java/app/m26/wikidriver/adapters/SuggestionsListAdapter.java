package app.m26.wikidriver.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import app.m26.wikidriver.R;;
import app.m26.wikidriver.fragments.AdvertFragment;
import app.m26.wikidriver.models.Publication;
import app.m26.wikidriver.models.User;
import app.m26.wikidriver.utils.Config;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class SuggestionsListAdapter extends ArrayAdapter implements Filterable{

    private Context mContext;
    private List<User> userList;
    private int layout;
    private DatabaseReference publicationsReference = FirebaseDatabase.getInstance().getReference(Config.FIREBASE_PUBLICATION_REFERENCE);

    public SuggestionsListAdapter(Context mContext, int resource, List<User> userList) {
        super(mContext, resource, userList);
        this.mContext = mContext;
        this.userList = userList;
        this.layout = resource;
    }

    @Override
    public int getCount() {
        return userList.size();
    }

    @Override
    public Object getItem(int position) {
        return userList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    private TextView txtFullName, txtLocation;
    private CircleImageView profileIcon;

    @Override
    public View getView(final int position, View view, ViewGroup parent) {
        if (view == null) {
            view = LayoutInflater.from(mContext).
                    inflate(layout, parent, false);
        }
        final User user = (User)getItem(position);

        txtFullName = view.findViewById(R.id.txtFullName);
        txtLocation = view.findViewById(R.id.txtLocation);
        profileIcon = view.findViewById(R.id.profilePic);

        txtFullName.setText(String.format("%s %s", user.getFirstName(), user.getLastName()));
        txtLocation.setText(String.format("%s, %s", user.getCity(), user.getCountry()));
        Picasso.with(mContext).load(user.getThumbnail()).error(R.drawable.profile_icon).into(profileIcon);

        final View finalView = view;
        view.setOnClickListener(v -> {
            updateList(user);
            finalView.setVisibility(View.INVISIBLE);
        });

        return view;
    }

    private void updateList(final User user) {
       final String userId = user.getUserId();
       publicationsReference.addListenerForSingleValueEvent(new ValueEventListener() {
           @Override
           public void onDataChange(DataSnapshot dataSnapshot) {
               List<Publication> publicationList = new ArrayList<>();
               for(DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                   Publication publication = postSnapshot.getValue(Publication.class);
                   if(publication.getUserId().equals(userId)) {
                       publicationList.add(publication);
                   }
               }
               SocialPublicationsAdapter socialPublicationsAdapter = new SocialPublicationsAdapter(mContext, publicationList, Config.FIREBASE_PUBLICATION_REFERENCE);
               AdvertFragment.getRecyclerView().setAdapter(socialPublicationsAdapter);
           }

           @Override
           public void onCancelled(DatabaseError databaseError) {

           }
       });
    }
    @NonNull
    @Override
    public Filter getFilter() {
        return exampleFilter;
    }

    private Filter exampleFilter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            List<User> filteredList = new ArrayList<>();
            if(constraint == null || constraint.length() == 0) {
            } else {
                String filterPatter = constraint.toString().toLowerCase().trim();
                for(User user : userList) {
                    if((user.getFirstName() + " " + user.getLastName()).toLowerCase().contains(filterPatter)) {
                        filteredList.add(user);
                    }
                }
            }
            FilterResults results = new FilterResults();
            results.values = filteredList;
            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            userList.clear();
            try {
                userList.addAll((List) results.values);
            } catch (NullPointerException  e) {

            }
            notifyDataSetChanged();
        }
    };
}

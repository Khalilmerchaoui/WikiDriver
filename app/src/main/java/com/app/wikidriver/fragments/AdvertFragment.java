package com.app.wikidriver.fragments;

import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.app.wikidriver.R;
import com.app.wikidriver.activities.AddPublicationActivity;
import com.app.wikidriver.adapters.AdvertPublicationsAdapter;
import com.app.wikidriver.adapters.SocialPublicationsAdapter;
import com.app.wikidriver.adapters.SuggestionsListAdapter;
import com.app.wikidriver.models.Publication;
import com.app.wikidriver.models.User;
import com.app.wikidriver.utils.Config;
import com.github.clans.fab.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.jaredrummler.materialspinner.MaterialSpinner;

import java.util.ArrayList;
import java.util.List;

public class AdvertFragment extends Fragment implements SearchView.OnQueryTextListener{

    private FloatingActionButton fbSale, fbMechanical, fbCourse, fbOther;
    private static RecyclerView recyclerView;
    private LinearLayoutManager layoutManager;
    private DatabaseReference publicationsReference, usersReference;
    private Toolbar mToolbar;
    private TextView txtNoPublications;
    private MaterialSpinner spSort;

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        User user = Config.getCurrentUser(getActivity());

        mToolbar = getActivity().findViewById(R.id.toolbar);
        mToolbar.setTitleTextColor(getResources().getColor(android.R.color.white));
        ((AppCompatActivity)getActivity()).setSupportActionBar(mToolbar);
        ((AppCompatActivity)getActivity()).getSupportActionBar().setTitle(String.format("%s, %s", user.getCity(), user.getCountry()));

        recyclerView = getActivity().findViewById(R.id.recyclerView);
        txtNoPublications = getActivity().findViewById(R.id.txtNoPublications);
        fbSale = getActivity().findViewById(R.id.menu_sale);
        spSort = getActivity().findViewById(R.id.spSort);
        fbMechanical = getActivity().findViewById(R.id.menu_mechanical);
        fbCourse = getActivity().findViewById(R.id.menu_course);
        fbOther = getActivity().findViewById(R.id.menu_other);

        List<String> listOfCategories = new ArrayList<>();
        listOfCategories.add(getResources().getString(R.string.all));
        listOfCategories.add(getResources().getString(R.string.sale_rent));
        listOfCategories.add(getResources().getString(R.string.mechanical_bodywork));
        listOfCategories.add(getResources().getString(R.string.course_driver));
        listOfCategories.add(getResources().getString(R.string.other));

        final ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(getActivity(),
                android.R.layout.simple_spinner_item, listOfCategories);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_list_item_1);
        spSort.setAdapter(dataAdapter);

        layoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(layoutManager);
        publicationsReference = FirebaseDatabase.getInstance().getReference(Config.FIREBASE_PUBLICATION_REFERENCE);
        usersReference = FirebaseDatabase.getInstance().getReference(Config.FIREBASE_USERS_REFERENCE);


        fbSale.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                launchAddPublication(Config.PUBLICATION_TYPE_SALE);
            }
        });

        fbMechanical.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                launchAddPublication(Config.PUBLICATION_TYPE_MECHANICAL);
            }
        });

        fbCourse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                launchAddPublication(Config.PUBLICATION_TYPE_COURSE);
            }
        });

        fbOther.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                launchAddPublication(Config.PUBLICATION_TYPE_OTHER);
            }
        });
        spSort.setOnItemSelectedListener(new MaterialSpinner.OnItemSelectedListener() {
            @Override
            public void onItemSelected(MaterialSpinner view, int position, long id, Object item) {
                if(position == 0)
                    loadPublicationsFromFirebase();
                else
                    loadSortedPublicationsFromFirebase(position);
            }
        });
        loadPublicationsFromFirebase();
    }

    private void loadPublicationsFromFirebase() {
        publicationsReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                List<Publication> publications = new ArrayList<>();
                User currentUser = Config.getCurrentUser(getActivity());
                for(DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    Publication publication = postSnapshot.getValue(Publication.class);
                    if(currentUser != null)
                        if(publication.getCity().equals(currentUser.getCity()) && publication.getCountry().equals(currentUser.getCountry()))
                           publications.add(publication);
                }
                if(publications.size() > 0){
                    txtNoPublications.setVisibility(View.INVISIBLE);
                    AdvertPublicationsAdapter adapter = new AdvertPublicationsAdapter(getActivity(), publications, Config.FIREBASE_PUBLICATION_REFERENCE);
                    recyclerView.setAdapter(adapter);
                    adapter.notifyDataSetChanged();
                } else {
                    txtNoPublications.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void loadSortedPublicationsFromFirebase(final int position) {
        publicationsReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                User currentUser = Config.getCurrentUser(getActivity());
                List<Publication> publications = new ArrayList<>();
                for(DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    Publication publication = postSnapshot.getValue(Publication.class);
                    if(publication.getType() == position && publication.getCity().equals(currentUser.getCity())
                            && publication.getCountry().equals(currentUser.getCountry()))
                        publications.add(publication);
                }
                SocialPublicationsAdapter adapter = new SocialPublicationsAdapter(getActivity(), publications, Config.FIREBASE_PUBLICATION_REFERENCE);
                recyclerView.setAdapter(adapter);
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void launchAddPublication(int type) {
        Intent intent = new Intent(getActivity(), AddPublicationActivity.class);
        intent.putExtra("type", type);
        startActivity(intent);

    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        final ProgressDialog progressDialog = new ProgressDialog(getActivity());
        progressDialog.setMessage(getResources().getString(R.string.please_wait));
        progressDialog.show();

        /*publicationsReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                List<Publication> publications = new ArrayList<>();
                for(DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    Publication publication = postSnapshot.getValue(Publication.class);
                    if(publication.get)
                    publications.add(publication);
                }
                PublicationsAdapter adapter = new PublicationsAdapter(getApplicationContext(), publications);
                recyclerView.setAdapter(adapter);
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });*/
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                progressDialog.dismiss();
                progressDialog.cancel();
            }
        }, 2000);
        return false;
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    private SearchView searchView;
    private SearchView.SearchAutoComplete mSearchAutoComplete;

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        inflater.inflate(R.menu.main_categories_toolbar, menu);

        SearchManager searchManager = (SearchManager)
                getActivity().getSystemService(Context.SEARCH_SERVICE);
        MenuItem searchMenuItem = menu.findItem(R.id.action_search);
        searchView = (SearchView) searchMenuItem.getActionView();

        mSearchAutoComplete = searchView.findViewById(android.support.v7.appcompat.R.id.search_src_text);
        mSearchAutoComplete.setDropDownBackgroundResource(android.R.color.white);
        mSearchAutoComplete.setDropDownAnchor(R.id.action_search);

        searchView.setSearchableInfo(searchManager.
                getSearchableInfo(getActivity().getComponentName()));
        searchView.setOnQueryTextListener(this);
        searchView.setQueryHint(getResources().getString(R.string.search_drivers));
        mSearchAutoComplete.setHintTextColor(getResources().getColor(android.R.color.white));
        mSearchAutoComplete.setTextColor(getResources().getColor(android.R.color.white));

    }


    private SuggestionsListAdapter suggestionsListAdapter;

    @Override
    public boolean onQueryTextChange(final String newText) {

        final List<User> userList = new ArrayList<>();
        usersReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    User user = postSnapshot.getValue(User.class);
                    userList.add(user);
                    suggestionsListAdapter = new SuggestionsListAdapter(getActivity(), R.layout.suggestion_item_layout, userList);
                    suggestionsListAdapter.getFilter().filter(newText);
                    mSearchAutoComplete.setAdapter(suggestionsListAdapter);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        return false;
    }

    public static RecyclerView getRecyclerView() {
        return recyclerView;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
         return inflater.inflate(R.layout.advert_fragment, container, false);
    }

    @Override
    public void onResume() {
        super.onResume();
        loadPublicationsFromFirebase();
    }
}

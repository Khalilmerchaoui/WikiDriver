package app.m26.wikidriver.activities;

import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import app.m26.wikidriver.R;;
import app.m26.wikidriver.adapters.SuggestionsListAdapter;
import app.m26.wikidriver.models.Publication;
import app.m26.wikidriver.models.User;
import app.m26.wikidriver.utils.Config;
import com.github.clans.fab.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.jaredrummler.materialspinner.MaterialSpinner;


import java.util.ArrayList;
import java.util.List;

public class CategoriesActivity extends AppCompatActivity implements SearchView.OnQueryTextListener {

    private FloatingActionButton fbSale, fbMechanical, fbCourse, fbOther;
    private static RecyclerView recyclerView;
    private LinearLayoutManager layoutManager;
    private DatabaseReference publicationsReference, usersReference;
    private Toolbar mToolbar;
    private TextView txtCityCountry;
    private MaterialSpinner spSort;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.advert_fragment);

        User user = Config.getCurrentUser(getApplicationContext());

        mToolbar = findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);

        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(CategoriesActivity.this, "Clicked", Toast.LENGTH_SHORT).show();

            }
        });

        recyclerView = findViewById(R.id.recyclerView);
        fbSale = findViewById(R.id.menu_sale);
        spSort = findViewById(R.id.spSort);
        fbMechanical = findViewById(R.id.menu_mechanical);
        fbCourse = findViewById(R.id.menu_course);
        fbOther = findViewById(R.id.menu_other);

        txtCityCountry.setText(String.format("%s, %s", user.getCity(), user.getCountry()));

        List<String> listOfCategories = new ArrayList<>();
        listOfCategories.add(getResources().getString(R.string.sale_rent));
        listOfCategories.add(getResources().getString(R.string.mechanical_bodywork));
        listOfCategories.add(getResources().getString(R.string.course_driver));
        listOfCategories.add(getResources().getString(R.string.other));

        final ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, listOfCategories);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_list_item_1);
        spSort.setAdapter(dataAdapter);

        layoutManager = new LinearLayoutManager(getApplicationContext());
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

        loadPublicationsFromFirebase();
    }

    private void loadPublicationsFromFirebase() {
        Toast.makeText(this, "all", Toast.LENGTH_SHORT).show();
        publicationsReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                List<Publication> publications = new ArrayList<>();
                User currentUser = Config.getCurrentUser(getApplicationContext());
                for(DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    Publication publication = postSnapshot.getValue(Publication.class);
                    if(publication.getCity().equals(currentUser.getCity()) && publication.getCountry().equals(currentUser.getCountry()))
                        publications.add(publication);
                }
                /*PublicationsAdapter adapter = new PublicationsAdapter(getApplicationContext(), publications);
                recyclerView.setAdapter(adapter);
                adapter.notifyDataSetChanged();*/
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void loadSortedPublicationsFromFirebase(final int position) {
        publicationsReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                User currentUser = Config.getCurrentUser(getApplicationContext());
                List<Publication> publications = new ArrayList<>();
                for(DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    Publication publication = postSnapshot.getValue(Publication.class);
                    if(publication.getType() == position && publication.getCity().equals(currentUser.getCity())
                            && publication.getCountry().equals(currentUser.getCountry()))
                        publications.add(publication);
                }
                /*PublicationsAdapter adapter = new PublicationsAdapter(getApplicationContext(), publications);
                recyclerView.setAdapter(adapter);
                adapter.notifyDataSetChanged();*/
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadPublicationsFromFirebase();
    }

    private void launchAddPublication(int type) {
        Intent intent = new Intent(CategoriesActivity.this, AddPublicationActivity.class);
        intent.putExtra("type", type);
        startActivity(intent);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == R.id.action_search) {
            layoutAll.setVisibility(View.INVISIBLE);
        }
        return super.onOptionsItemSelected(item);
    }

    private SearchView searchView;
    private SearchView.SearchAutoComplete mSearchAutoComplete;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_categories_toolbar, menu);

        SearchManager searchManager = (SearchManager)
                getSystemService(Context.SEARCH_SERVICE);
        MenuItem searchMenuItem = menu.findItem(R.id.action_search);
        searchView = (SearchView) searchMenuItem.getActionView();

        mSearchAutoComplete = searchView.findViewById(android.support.v7.appcompat.R.id.search_src_text);
        mSearchAutoComplete.setDropDownBackgroundResource(android.R.color.white);
        mSearchAutoComplete.setDropDownAnchor(R.id.action_search);

        searchView.setSearchableInfo(searchManager.
                getSearchableInfo(getComponentName()));
        searchView.setOnQueryTextListener(this);
        searchView.setQueryHint(getResources().getString(R.string.search_drivers));
        mSearchAutoComplete.setHintTextColor(getResources().getColor(android.R.color.white));
        mSearchAutoComplete.setTextColor(getResources().getColor(android.R.color.white));
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent mainIntent = new Intent(CategoriesActivity.this, MainActivity.class);
        startActivity(mainIntent);
        finish();
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage(getResources().getString(R.string.please_wait));
        progressDialog.show();

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                progressDialog.dismiss();
                progressDialog.cancel();
            }
        }, 2000);
        return false;
    }

    private LinearLayout layoutAll;
    private SuggestionsListAdapter suggestionsListAdapter;

    @Override
    public boolean onQueryTextChange(final String newText) {
        layoutAll.setVisibility(View.INVISIBLE);
        final List<User> userList = new ArrayList<>();
        usersReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    User user = postSnapshot.getValue(User.class);
                    userList.add(user);
                    suggestionsListAdapter = new SuggestionsListAdapter(getApplicationContext(), R.layout.suggestion_item_layout, userList);
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
}

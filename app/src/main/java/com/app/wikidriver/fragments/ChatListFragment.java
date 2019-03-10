package com.app.wikidriver.fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.app.wikidriver.R;
import com.app.wikidriver.adapters.ChatListAdapter;
import com.app.wikidriver.utils.Config;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.List;

public class ChatListFragment extends Fragment {
    private DatabaseReference chatReference;
    private LinearLayoutManager layoutManager;
    private RecyclerView recyclerView;

    @Nullable
    public View onCreateView(@NonNull LayoutInflater layoutInflater, @Nullable ViewGroup viewGroup, @Nullable Bundle bundle) {
        return layoutInflater.inflate(R.layout.chat_list_fragment, viewGroup, false);
    }

    public void onActivityCreated(@Nullable Bundle bundle) {
        super.onActivityCreated(bundle);
        this.chatReference = FirebaseDatabase.getInstance().getReference(Config.FIREBASE_CHAT_REFERENCE);
       String userId = Config.getCurrentUser(getActivity()).getUserId();
        this.recyclerView = (RecyclerView) getActivity().findViewById(R.id.recyclerView);
        this.layoutManager = new LinearLayoutManager(getActivity());
        this.recyclerView.setLayoutManager(this.layoutManager);
        final List arrayList = new ArrayList();
        this.chatReference.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            public void onCancelled(DatabaseError databaseError) {
            }

            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot key : dataSnapshot.getChildren()) {
                    arrayList.add(key.getKey());
                }
                ChatListFragment.this.recyclerView.setAdapter(new ChatListAdapter(ChatListFragment.this.getActivity(), arrayList));
            }
        });
    }
}
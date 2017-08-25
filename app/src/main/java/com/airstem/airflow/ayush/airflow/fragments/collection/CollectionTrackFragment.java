package com.airstem.airflow.ayush.airflow.fragments.collection;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.airstem.airflow.ayush.airflow.CollectionActivity;
import com.airstem.airflow.ayush.airflow.R;
import com.airstem.airflow.ayush.airflow.adapters.collection.TrackAdapter;
import com.airstem.airflow.ayush.airflow.events.collection.CollectionTrackListener;
import com.airstem.airflow.ayush.airflow.model.collection.CollectionTrack;

import java.util.ArrayList;

import io.realm.Realm;

/**
 * Created by mcd-50 on 9/7/17.
 */

public class CollectionTrackFragment  extends Fragment implements CollectionTrackListener {

    Realm realm;

    boolean isLoading;
    ProgressDialog progressDialog;


    ArrayList<CollectionTrack> mItems;
    TrackAdapter mAdapter;
    RecyclerView listView;
    TextView empty;
    LinearLayoutManager linearLayoutManager;



    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.collection_track_fragment, container, false);

        progressDialog = new ProgressDialog(getContext());


        empty = (TextView) rootView.findViewById(R.id.collection_track_fragment_empty);
        listView = (RecyclerView) rootView.findViewById(R.id.collection_track_fragment_list);
        listView.setHasFixedSize(true);
        linearLayoutManager = new LinearLayoutManager(getContext());
        listView.setLayoutManager(linearLayoutManager);

        return rootView;
    }


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        realm = ((CollectionActivity)getActivity()).getRealm();
        setAdapter();
    }


    private void setAdapter() {
        mItems = new ArrayList<CollectionTrack>(realm.where(CollectionTrack.class).findAll());
        mAdapter = new TrackAdapter(getContext(), mItems, this);
        listView.setAdapter(mAdapter);
    }
    
    
    @Override
    public void onTrackClick(CollectionTrack collectionTrack) {

    }

    @Override
    public void onTrackRemove(CollectionTrack collectionTrack) {
        
    }

    @Override
    public void onTrackFav(CollectionTrack collectionTrack, boolean addToFav) {

    }
}

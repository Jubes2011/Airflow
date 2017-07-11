package com.airstem.airflow.ayush.airflow.fragments.collection;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.airstem.airflow.ayush.airflow.R;
import com.airstem.airflow.ayush.airflow.adapters.collection.PlaylistAdapter;
import com.airstem.airflow.ayush.airflow.events.CollectionPlaylistClickListener;
import com.airstem.airflow.ayush.airflow.model.collection.CollectionPlaylist;

import java.util.ArrayList;

/**
 * Created by mcd-50 on 10/7/17.
 */

public class CollectionPlaylistFragment  extends Fragment implements CollectionPlaylistClickListener {

    TextView textView;
    RecyclerView listView;

    ArrayList<CollectionPlaylist> mItems;
    PlaylistAdapter mAdapter;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.collection_playlist_fragment, container, false);

        textView = (TextView) rootView.findViewById(R.id.collection_playlist_fragment_empty);
        listView = (RecyclerView) rootView.findViewById(R.id.collection_playlist_fragment_list);

        return rootView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onItemClick(CollectionPlaylist collectionPlaylist) {

    }
}
package com.airstem.airflow.ayush.airflow.events;

import com.airstem.airflow.ayush.airflow.model.search.SearchAlbumInfoTrack;
import com.airstem.airflow.ayush.airflow.model.search.SearchArtistInfoTrack;
import com.airstem.airflow.ayush.airflow.model.search.SearchRadio;

/**
 * Created by mcd-50 on 10/7/17.
 */

public interface SearchArtistInfoTrackClickListener {
    void onItemClick(SearchArtistInfoTrack searchArtistInfoTrack);
}
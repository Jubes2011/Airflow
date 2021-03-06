package com.airstem.airflow.ayush.airflow.events.collection;

import com.airstem.airflow.ayush.airflow.enums.collection.Action;
import com.airstem.airflow.ayush.airflow.model.collection.CollectionVideo;

/**
 * Created by mcd-50 on 10/7/17.
 */

public interface CollectionVideoListener {
    void onVideoClick(CollectionVideo collectionVideo);
    void onVideoOptions(CollectionVideo collectionVideo, Action action);
}

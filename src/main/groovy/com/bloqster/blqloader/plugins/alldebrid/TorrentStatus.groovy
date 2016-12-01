package com.bloqster.blqloader.plugins.alldebrid

/**
 * Created by micha on 30.11.2016.
 */
enum TorrentStatus {
    FINISHED,
    DOWNLOADING


    @Override
    public String toString() {
        return this.name()
    }
}
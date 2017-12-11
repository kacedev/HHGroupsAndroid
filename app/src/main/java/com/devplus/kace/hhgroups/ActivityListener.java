package com.devplus.kace.hhgroups;

interface ActivityListener {
    void requestCatalog(int page, int section, int category, String search);
    void requestAlbum(Previo albumPrevio);
    void requestDownload(Previo previo);
    void requestPlay(Previo previo);
}

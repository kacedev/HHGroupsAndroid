package com.devplus.kace.hhgroups;

import android.content.Intent;

interface ServiceSender {
    void send(Intent intent, boolean finish);
    void sendCatalogAndCache(Intent intent);
    void sendAlbumAndCache(Intent intent);
}

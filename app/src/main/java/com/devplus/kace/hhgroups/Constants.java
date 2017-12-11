package com.devplus.kace.hhgroups;

final class Constants {

    final static String BROADCAST_FILTER = "com.devplus.kace.hhgroups.ACTION_HHGROUPS";

    interface ACTIONS {
        //THREADS:
        String GET_CATALOG = "com.devplus.kace.hhgroups.ACTION_CATALOGO";
        String GET_ALBUM = "com.devplus.kace.hhgroups.ACTION_ALBUM";
        String DOWNLOAD = "com.devplus.kace.hhgroups.ACTION_DOWNLOAD";
        String PLAY = "com.devplus.kace.hhgroups.ACTION_PLAY";
        String GET_FAVS = "com.devplus.kace.hhgroups.GET_FAVS";
        String SAVE_TO_FAVS = "com.devplus.kace.hhgroups.SAVE_TO_FAVS";
    }

    interface EXTRAS {
        //EXTRAS:
        String URL = "com.devplus.kace.hhgroups.EXTRA_URL";
        String DOWNLOAD_PREVIO = "com.devplus.kace.hhgroups.EXTRA_DOWNLOAD_PREVIO";
        String PLAY_PREVIO = "com.devplus.kace.hhgroups.EXTRA_PLAY_PREVIO";
        String ALBUM = "com.devplus.kace.hhgroups.EXTRA_ALBUM";
        String PREVIO = "com.devplus.kace.hhgroups.EXTRA_PREVIO";
        String CATALOG = "com.devplus.kace.hhgroups.EXTRA_CATALOG";
        String MAX_PAGE = "com.devplus.kace.hhgroups.EXTRA_MAX_PAGE";

        String ALBUM_NAME = "com.devplus.kace.hhgroups.ALBUM_NAME";
        String FAVS = "com.devplus.kace.hhgroups.FAVS";
        String SUCCESS = "com.devplus.kace.hhgroups.SUCCESS";

        //CATALOG:
        String PAGE = "com.devplus.kace.hhgroups.EXTRA_PAGE";
        String SECTION = "com.devplus.kace.hhgroups.EXTRA_SECTION";
        String CATEGORY = "com.devplus.kace.hhgroups.EXTRA_CATEGORY";
        String SEARCH = "com.devplus.kace.hhgroups.EXTRA_SEARCH";
    }
}

package com.devplus.kace.hhgroups;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.util.Log;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;

class CatalogLoader extends Thread implements InterruptedThread {

    private boolean busqueda;
    private String textoBuscar;
    private int seccion, categoria;
    private int pagina, maxPagina;

    private Document doc;
    private ServiceSender mSender;
    private Boolean mStop = false;

    //SEARCH CONSTRUCTOR:
    CatalogLoader (int seccion, int pagina, String textoBuscar) {
        this.busqueda = true;
        this.seccion = seccion;
        this.categoria = -1;
        this.textoBuscar = textoBuscar.replace(" ", "-");
        this.pagina = pagina;
    }

    //NON SEARCH CONSTRUCTOR:
    CatalogLoader (int seccion, int categoria, int pagina) {
        this.busqueda = false;
        this.seccion = seccion;
        this.categoria = categoria;
        this.textoBuscar = "";
        this.pagina = pagina;
    }

    //Obligatorio llamar a este método: new CatalogLoader(...).setSender(this);
    CatalogLoader setSender(ServiceSender sender) {
        mSender = sender;
        return this;
    }

    @Override
    public void run() {

        //TODO: Gestionar errores de conexión:
        if(!establecerConexion()) {
            mSender.send(new Intent().setAction(Constants.BROADCAST_FILTER).putExtra(Constants.EXTRAS.SUCCESS, false), true);
            return;
        }

        Previo[] catalog;

        if(categoria == 0) {
            catalog = getDestacados();
        } else {
            maxPagina = getMaxPagina();
            if (seccion == 0) {
                catalog = getAlbumes();
            } else if (seccion == 1) {
                catalog = getBases();
            } else if (seccion == 2) {
                catalog = getTemas();
            } else
                return;
        }

        if(!mStop) {
            Log.v("SERVICIO-CAT_LOADER", "No ha sido interrumpido");
            mSender.sendCatalogAndCache(new Intent().setAction(Constants.BROADCAST_FILTER)
                    .putExtra(Constants.EXTRAS.SECTION, seccion)
                    .putExtra(Constants.EXTRAS.CATEGORY, categoria)
                    .putExtra(Constants.EXTRAS.PAGE, pagina)
                    .putExtra(Constants.EXTRAS.SUCCESS, true)
                    .putExtra(Constants.EXTRAS.CATALOG, catalog)
                    .putExtra(Constants.EXTRAS.MAX_PAGE, maxPagina));
        } else {
            Log.v("SERVICIO-CAT_LOADER", "Ha sido interrumpido");
        }
    }

    @SuppressLint("DefaultLocale")
    private String establecerUrl() {

        final String basesPuntos = "http://www.hhgroups.com/bases/o3d2p%df0/";
        final String basesBuscar = "http://www.hhgroups.com/bases/%s/p%d/";
        final String basesLibres = "http://www.hhgroups.com/bases/o5d2p%df1/";

        final String temasPuntos = "http://www.hhgroups.com/temas/o3d2p%d/";
        final String temasBuscar = "http://www.hhgroups.com/temas/%s/p%d/";

        final String albumesBases = "http://www.hhgroups.com/albumes/o5d2p%df1/";
        final String albumesPuntos = "http://www.hhgroups.com/albumes/o3d2p%df0/";
        final String albumesBuscar = "http://www.hhgroups.com/albumes/%s/p%d/";

        String url = "";

        switch(seccion) {
            case 0:
                if(busqueda || categoria == 0)
                    url = String.format(albumesBuscar, textoBuscar, pagina);
                else {
                    if (categoria == 1)
                        url = String.format(albumesBuscar, "", pagina);
                    else if (categoria == 2)
                        url = String.format(albumesPuntos, pagina);
                    else
                        url = String.format(albumesBases, pagina);
                }
                break;
            case 2:
                if(busqueda || categoria == 0)
                    url = String.format(temasBuscar, textoBuscar, pagina);
                else {
                    if (categoria == 1)
                        url = String.format(temasBuscar, "", pagina);
                    else if (categoria == 2)
                        url = String.format(temasPuntos, pagina);
                }
                break;
            case 1:
                if(busqueda || categoria == 0)
                    url = String.format(basesBuscar, textoBuscar, pagina);
                else {
                    if (categoria == 1)
                        url = String.format(basesBuscar, "", pagina);
                    else if (categoria == 2)
                        url = String.format(basesPuntos, pagina);
                    else
                        url = String.format(basesLibres, pagina);
                }
                break;
        }
        Log.v("CARGADOR-CATALOGO", "URL : " + url);
        return url;
    }

    private boolean establecerConexion () {
        String url = establecerUrl();
        if(url.isEmpty())
            return false;

        try {
            doc = Jsoup.connect(url).timeout(5000).get();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return !(doc == null);
    }

    private int getMaxPagina() {
        try{
            return Integer.parseInt(
                    doc.select("ul.list_pag").get(0).
                            select("li > a").last().
                            attr("title").substring(7));
        } catch (IndexOutOfBoundsException e) {
            Log.v("ERROR", "IndexOutOfBounds");
            return 1;
        }
    }

    private Previo[] getTemas() {
        Elements temas = doc.select(".list_table tr td.tbl_double");
        if(temas.size() == 0) {
            return null;
        }
        Previo[] previos = new Previo[temas.size()];
        Previo p;
        for(int i = 0; i < temas.size(); i++) {
            Elements e = temas.get(i).select("meta");
            String nombre = e.get(0).attr("content");
            String artista = e.get(2).attr("content");
            String temaUrl =  e.get(1).attr("content");
            Log.v("CARGADOR-TEMAS", " -> " + artista + " - " + nombre + " - " + temaUrl);
            {
                p = new Previo();
                p.setNombre(nombre);
                p.setArtista(artista);
                p.setUrl(temaUrl);
                p.setImagenUrl(null);
                previos[i] = p;
            }
        }
        return previos;
    }

    private Previo[] getAlbumes() {
        Elements albumes = doc.select(".list_table tr td.tbl_double");
        if(albumes.size() == 0) {
            return null;
        }
        Previo[] previos = new Previo[albumes.size()];
        Previo p;
        for(int i = 0; i < albumes.size(); i++) {
            Elements e = albumes.get(i).select("div");
            String albumImagen = e.get(0).select("img").attr("src");
            String albumUrl = "http://www.hhgroups.com/" + e.get(1).select("a").attr("href");
            String artista = e.get(1).select("span").first().text();
            String nombre = e.get(1).select("span").last().text();
            Log.v("CARGADOR-ALBUMES", " -> " + artista + " - " + nombre + " - " + albumUrl + " - " + albumImagen);
            {
                p = new Previo();
                p.setNombre(nombre);
                p.setArtista(artista);
                p.setUrl(albumUrl);
                p.setImagenUrl(albumImagen);
                previos[i] = p;
            }
        }
        return previos;

    }

    private Previo[] getBases() {
        Elements bases = doc.select(".list_table tr td.tbl_double");
        if(bases.size() == 0) {
            return null;
        }
        Previo[] previos = new Previo[bases.size()];
        Previo p;
        for(int i = 0; i < bases.size(); i++) {
            Elements e = bases.get(i).select("meta");
            String nombre = e.get(0).attr("content");
            String artista = e.get(2).attr("content");
            String baseUrl = e.get(1).attr("content");
            //String uso = bases.get(i).select("div.tbl_name img").attr("src");
            Log.v("CARGADOR-BASES   ", " -> " + artista + " - " + nombre + " - " + baseUrl);
            {
                p = new Previo();
                p.setNombre(nombre);
                p.setArtista(artista);
                p.setUrl(baseUrl);
                p.setImagenUrl(null);
                previos[i] = p;
            }
        }
        return previos;
    }

    private Previo[] getDestacados () {
        Elements albumes = doc.select(".related_big");
        if(albumes.size() == 0) {
            return null;
        }
        Previo[] previos = new Previo[albumes.size()];
        Previo p;
        for(int i = 0; i < albumes.size(); i++) {
            Element a = albumes.get(i);
            p = new Previo();
            Elements info = a.select("img, a, a span, a p");
            String imagenURL = info.get(0).attr("src");
            if (imagenURL.length() > 200)
                p.setImagenUrl(null);
            else
                p.setImagenUrl(imagenURL);
            p.setUrl("http://www.hhgroups.com/" + info.get(1).attr("href"));
            p.setArtista(info.get(2).text());
            p.setNombre(info.get(3).text());
            p.setNumero(i + 1);
            previos[i] = p;
        }
        return previos;
    }

    @Override
    public void shouldStop() {
        mStop = true;
    }

    @Override
    public String getTaskName() {
        return "CatalogLoader [Seccion "+ seccion + " Categoria " + categoria;
    }
}

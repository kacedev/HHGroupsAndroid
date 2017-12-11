package com.devplus.kace.hhgroups;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

import java.io.FileNotFoundException;

import static android.view.View.GONE;

public class CatalogFragment extends Fragment implements View.OnClickListener,
                                                         AdapterView.OnItemSelectedListener,
                                                         OnErrorListener {

    private static final String CAT_SECTION = "com.devplus.kace.hhgroups.CAT_SECTION";
    private static final String CAT_SECTION_NAME = "com.devplus.kace.hhgroups.CAT_SECTION_NAME";
    //private static final String CATALOG = "com.devplus.kace.hhgroups.CATALOG";

    /* CAT_SECTION : CAT_SECTION_NAME
        - 0 : Álbumes.
        - 1 : Instrumentales.
        - 2 : Temas.
     */

    private int mSection, mPage = 1, mMaxPage;
    //private String mSectionName;

    private ActivityListener mActivity;

    private LinearLayout mPageLay;
    private RecyclerView mRecycler;
    private CatalogAdapter mAdapter;
    private ProgressBar mProgress;
    private TextView mPageText;
    private Spinner mCatSpinner;

    public CatalogFragment() {}

    public static CatalogFragment newInstance(int section, String sectionName) {
        CatalogFragment fragment = new CatalogFragment();
        Bundle args = new Bundle();
        args.putInt(CAT_SECTION, section);
        args.putString(CAT_SECTION_NAME, sectionName);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if(context instanceof ActivityListener) {
            mActivity = (ActivityListener) context;
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            //Obtenemos la sección del fragmento y el catálogo:
            mSection = getArguments().getInt(CAT_SECTION);
            //mCatalog = (Previo[]) getArguments().getSerializable(CATALOG);
            //Adapter:
            mAdapter = new CatalogAdapter(getContext(), null);
            mAdapter.setClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int position = mRecycler.getChildAdapterPosition(view);
                    if(mSection != 0)
                        mActivity.requestPlay(mAdapter.getPrevio(position));
                    else
                        mActivity.requestAlbum(mAdapter.getPrevio(position));
                }
            });
            if(mSection != 0) {
                mAdapter.setLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View view) {
                        final int position = mRecycler.getChildAdapterPosition(view);
                        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                        builder.setItems(new String[]{"Descargar"}, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                mActivity.requestDownload(mAdapter.getPrevio(position));
                            }
                        });
                        builder.show();
                        return true;
                    }
                });
            }
        }
    }

    @Override
    @SuppressLint("InflateParams")
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_catalog, null);
        mPageLay = root.findViewById(R.id.catalogPageLayout);
        //Por defecto estará seleccionada la categoría "Destacado", con lo que escondemos el layout de la página:
        mPageLay.setVisibility(GONE);
        mRecycler = root.findViewById(R.id.catalogRecycler);
        mRecycler.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
        mRecycler.addItemDecoration(new RecyclerSeparator().setSeparation(15));
        mRecycler.setAdapter(mAdapter);
        root.findViewById(R.id.catalogAntBtn).setOnClickListener(this);
        root.findViewById(R.id.catalogSigBtn).setOnClickListener(this);
        mPageText = root.findViewById(R.id.catalogPageText);
        //Actualizamos el texto de la sección:
        ((TextView) root.findViewById(R.id.catalogSectionText)).setText(getArguments().getString(CAT_SECTION_NAME));
        //Establecemos el adaptador para el Spinner:
        mCatSpinner = root.findViewById(R.id.catalogCategorySpinner);
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(
                getContext(), android.R.layout.simple_dropdown_item_1line, Utilities.getCategories(mSection));
        mCatSpinner.setAdapter(spinnerAdapter);
        mCatSpinner.setOnItemSelectedListener(this);

        mProgress = root.findViewById(R.id.catalogProgress);
        return root;
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        switch(id) {
            case R.id.catalogSigBtn:
                if(mPage < mMaxPage) {
                    mPage++;
                }
                break;
            case R.id.catalogAntBtn:
                if(mPage > 1) {
                    mPage--;
                }
                break;
        }
        mActivity.requestCatalog(mPage, mSection, mCatSpinner.getSelectedItemPosition(), "");
        mProgress.setVisibility(View.VISIBLE);
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        mActivity.requestCatalog(mPage, mSection, i, "");
        mProgress.setVisibility(View.VISIBLE);
        if(i == 0) // Categoría: [Destacados]
            mPageLay.setVisibility(GONE);
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {}

    public void setCatalog(Previo[] previos, int maxPage) {
        mMaxPage = maxPage;
        if(mCatSpinner.getSelectedItemPosition() != 0)
            updatePageLayout();
        mAdapter.updateElements(previos);
        mProgress.setVisibility(GONE);
    }

    @SuppressLint("DefaultLocale")
    void updatePageLayout() {
        mPageLay.setVisibility(View.VISIBLE);
        mPageText.setText(String.format("%d - %d", mPage, mMaxPage));
    }

    @Override
    public void onError(String error) {
        Utilities.getErrorDialog(getContext(), error, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                mActivity.requestCatalog(mPage, mSection, mCatSpinner.getSelectedItemPosition(), "");
            }
        }, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                mProgress.setVisibility(View.INVISIBLE);
            }
        }).show();
    }
}

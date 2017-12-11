package com.devplus.kace.hhgroups;


import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

public class SearchFragment extends Fragment implements OnErrorListener {

    private EditText mSearch;
    private Spinner mSpinner;
    private RecyclerView mRecycler;
    private CatalogAdapter mAdapter;
    private ActivityListener mActivity;
    private ProgressBar mProgress;

    public SearchFragment() {}

    public static SearchFragment newInstance() {
        return new SearchFragment();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if(context instanceof ActivityListener) {
            mActivity = (ActivityListener) context;
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_search, null);
        mSearch = root.findViewById(R.id.searchText);
        mSearch.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                boolean handled = false;
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    InputMethodManager inputManager = (InputMethodManager)getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                    inputManager.hideSoftInputFromWindow(getActivity().getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                    mSearch.clearFocus();
                    Log.v("SERVICIO", "Buscando " + mSearch.getText().toString());
                    requestSearch();
                    handled = true;
                }
                return handled;
            }
        });
        mProgress = root.findViewById(R.id.searchProgress);
        mProgress.setVisibility(View.INVISIBLE);
        mSpinner = root.findViewById(R.id.searchSpinner);
        mAdapter = new CatalogAdapter(getContext(), null);
        mAdapter.setClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int position = mRecycler.getChildAdapterPosition(view);
                if(mSpinner.getSelectedItemPosition() != 0)
                    mActivity.requestPlay(mAdapter.getPrevio(position));
                else
                    mActivity.requestAlbum(mAdapter.getPrevio(position));
            }
        });

        mAdapter.setLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                final int position = mRecycler.getChildAdapterPosition(view);
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                builder.setItems(Utilities.getContextOptions(mSpinner.getSelectedItemPosition()), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        mActivity.requestDownload(mAdapter.getPrevio(position));
                    }
                });
                builder.show();
                return true;
            }
        });
        mRecycler = root.findViewById(R.id.searchRecycler);
        mRecycler.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
        mRecycler.addItemDecoration(new RecyclerSeparator().setSeparation(15));
        mRecycler.setAdapter(mAdapter);
        ArrayAdapter<String> spinnerAdapter =
                new ArrayAdapter<>(getContext(), android.R.layout.simple_dropdown_item_1line, Utilities.searchSections);
        mSpinner.setAdapter(spinnerAdapter);
        mSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
                if(!mSearch.getText().toString().isEmpty()) {
                    requestSearch();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {}
        });

        return root;
    }

    void requestSearch() {
        mActivity.requestCatalog(1, mSpinner.getSelectedItemPosition(), 0, mSearch.getText().toString());
        mProgress.setVisibility(View.VISIBLE);
    }

    public void setCatalog(Previo[] previos, int maxPage) {

        //mMaxPage = maxPage;
        //if(mSpinner.getSelectedItemPosition() != 0)
            //updatePageLayout();
        mAdapter.updateElements(previos);
        mProgress.setVisibility(View.INVISIBLE);
    }

    @Override
    public void onError(String error) {
        Utilities.getErrorDialog(getContext(), error, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                requestSearch();
            }
        }, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                mProgress.setVisibility(View.INVISIBLE);
            }
        }).show();
    }


}

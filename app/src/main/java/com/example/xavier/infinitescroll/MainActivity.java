package com.example.xavier.infinitescroll;

import android.os.Handler;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.example.xavier.infinitescroll.model.Record;
import com.example.xavier.infinitescroll.util.DataRequestHelper;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private SwipeRefreshLayout refreshLayout;
    private RecyclerView recyclerView;
    private LinearLayoutManager linearLayoutManager;
    private RecordRecycleViewAdapter recordRecycleViewAdapter;

    private List<Record> recordList;

    int pastVisibleItems, visibleItemCount, totalItemCount;

    private int REQUEST_DATA_THRESHOLD = 10;
    private int DATA_CACHE_SIZE = 3 * DataRequestHelper.REQUEST_SIZE;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        refreshLayout = (SwipeRefreshLayout) findViewById(R.id.refresh_layout);
        recyclerView = (RecyclerView) findViewById(R.id.list);

        recordRecycleViewAdapter = new RecordRecycleViewAdapter();
        recyclerView.setAdapter(recordRecycleViewAdapter);

        linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(linearLayoutManager);

        recordList = new ArrayList<>();

        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {

                refreshDataFromScratch();
            }
        });

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                if (dy < 0 && recordRecycleViewAdapter.currentFirstIndex == 0)
                    return;
                visibleItemCount = linearLayoutManager.getChildCount();
                totalItemCount = linearLayoutManager.getItemCount();
                pastVisibleItems = linearLayoutManager.findFirstVisibleItemPosition();

                if (dy > 0 && totalItemCount - pastVisibleItems <= REQUEST_DATA_THRESHOLD) {
                    if (!recordRecycleViewAdapter.isLoadingMore()) {
                        requestNewData(true);
                    }
                }
                else if (dy < 0 && pastVisibleItems - recordRecycleViewAdapter.currentFirstIndex <= REQUEST_DATA_THRESHOLD)
                {
                    if (!recordRecycleViewAdapter.isLoadingMore()) {
                        requestNewData(false);
                    }
                }
            }
        });

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                refreshLayout.setRefreshing(true);
                refreshDataFromScratch();
            }
        }, 500);
    }

    private void requestNewData(final boolean scrollToBottom) {

        final int index = scrollToBottom ? recordRecycleViewAdapter.getItemCount() : recordRecycleViewAdapter.currentFirstIndex - DataRequestHelper.REQUEST_SIZE;
        Log.d("MainActivity", "requestNewData for index = " + index);

        if (index < 0)
            return;

        recordRecycleViewAdapter.setLoadingMore(true);
        DataRequestHelper.retrieveData(MainActivity.this, index, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {

                int newFirstIndex = recordRecycleViewAdapter.currentFirstIndex;

                if (recordList.size() == DATA_CACHE_SIZE) {

                    int start, end;
                    if (scrollToBottom) {
                        start = DataRequestHelper.REQUEST_SIZE;
                        end = recordList.size();
                        newFirstIndex = newFirstIndex + DataRequestHelper.REQUEST_SIZE;
                    } else {
                        start = 0;
                        end = recordList.size() - DataRequestHelper.REQUEST_SIZE;
                        newFirstIndex = newFirstIndex - DataRequestHelper.REQUEST_SIZE;
                    }
                    List<Record> newList = new ArrayList<>();
                    for (int i = start; i < end; i++) {
                        newList.add(recordList.get(i));
                    }
                    recordList.clear();
                    recordList = newList;
                }

                for (int i = 0; i < response.length(); i++) {
                    try {
                        if (scrollToBottom)
                            recordList.add(new Record(response.getJSONObject(i)));
                        else
                            recordList.add(i, new Record(response.getJSONObject(i)));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                recordRecycleViewAdapter.setLoadingMore(false);
                recordRecycleViewAdapter.setData(recordList, newFirstIndex);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("MainActivity", "onErrorResponse " + error);
                recordRecycleViewAdapter.setLoadingMore(false);
            }
        });
    }


    private void refreshDataFromScratch() {
        DataRequestHelper.retrieveData(MainActivity.this, 0, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                recordList.clear();
                for (int i = 0; i < response.length(); i++) {
                    try {
                        recordList.add(new Record(response.getJSONObject(i)));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                refreshLayout.setRefreshing(false);
                recordRecycleViewAdapter.setData(recordList, 0);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("MainActivity", "onErrorResponse " + error);
                refreshLayout.setRefreshing(false);
            }
        });
    }

    public static class RecordRecycleViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
        private List<Record> data;
        private boolean isLoadingMore = false;

        private int currentFirstIndex = 0;

        public RecordRecycleViewAdapter() {
            data = new ArrayList<>();
        }

        public void setData(List<Record> _data, int firstIndex)
        {
            currentFirstIndex = firstIndex;
            data.clear();
            data.addAll(_data);
            notifyDataSetChanged();
        }

        @Override
        public int getItemCount() {
            return currentFirstIndex + data.size();
        }

        public Record getItem(int position){
            int realPosition = position - currentFirstIndex;

            if (realPosition >= data.size() || realPosition < 0)
                return null;
            return data.get(realPosition);
        }

        public void setLoadingMore(boolean isLoadingMore)
        {
            this.isLoadingMore = isLoadingMore;
        }

        public boolean isLoadingMore(){
            return isLoadingMore;
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, final int viewType) {

            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.layout_record_item, parent, false);

            RecordViewHolder vh = new RecordViewHolder(v);
            return vh;
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

            if (holder instanceof RecordViewHolder)
            {
                ((RecordViewHolder) holder).setData(getItem(position));
            }

        }
    }

    public static class RecordViewHolder extends RecyclerView.ViewHolder {

        public TextView sender, created, note, recipient, amount, currency, id;

        public RecordViewHolder(View v) {
            super(v);

            id = (TextView) v.findViewById(R.id.id);
            sender = (TextView) v.findViewById(R.id.sender);
            created = (TextView) v.findViewById(R.id.created);
            note = (TextView) v.findViewById(R.id.note);
            recipient = (TextView) v.findViewById(R.id.recipient);
            amount = (TextView) v.findViewById(R.id.amount);
            currency = (TextView) v.findViewById(R.id.currency);
        }

        public void setData(Record data)
        {
            if (data == null)
            {
                id.setText("id");
                sender.setText("sender");
                created.setText("created");
                note.setText("note");
                recipient.setText("recipient");
                amount.setText("amount");
                currency.setText("currency");
            }
            else {
                id.setText("" + data.id);
                sender.setText(data.source.sender);
                created.setText(data.created);
                note.setText(data.source.note);
                recipient.setText(data.destination.recipient);
                amount.setText("" + data.destination.amount);
                currency.setText(data.destination.currency);
            }
        }
    }
}

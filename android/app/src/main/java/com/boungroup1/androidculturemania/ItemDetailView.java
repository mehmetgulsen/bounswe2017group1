package com.boungroup1.androidculturemania;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

/**
 * Detail activity of CultureMania
 * Created by mehmetsefa on 22/11/2017.
 */

public class ItemDetailView extends AppCompatActivity {
    TextView voteCount;
    ImageButton upVote;
    ImageButton downVote;
    ImageButton deleteVote;
    int heritageId;

    /**
     * @param savedInstanceState The savedInstanceState is a reference to a Bundle object that is passed into the onCreate method
     */
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.item_detail_view);
        final NestedScrollView layout = (NestedScrollView) findViewById(R.id.detail_view_relayout);
        layout.setVisibility(View.INVISIBLE);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();

        Intent intent = getIntent();
        heritageId = intent.getIntExtra("heritageId", -1);

        final TextView title = (TextView) findViewById(R.id.detailtitle);
        final TextView date = (TextView) findViewById(R.id.detaildate);
        final TextView location = (TextView) findViewById(R.id.detaillocation);
        final TextView name = (TextView) findViewById(R.id.detailname);
        final TextView description = (TextView) findViewById(R.id.detaildescription);
        voteCount = (TextView) findViewById(R.id.vote_count);
        upVote = (ImageButton) findViewById(R.id.up_vote_button);
        downVote = (ImageButton) findViewById(R.id.down_vote_button);
        deleteVote = (ImageButton) findViewById(R.id.delete_vote_button);
        final EditText comment_entry = (EditText) findViewById(R.id.comment_entry);
        final Button send_button = (Button) findViewById(R.id.comment_send);
        final Button videobutton = (Button) findViewById(R.id.videobutton);
        final Button editbutton = (Button) findViewById(R.id.item_edit);
        final Button deletebutton = (Button) findViewById(R.id.item_delete);

        editbutton.setVisibility(View.INVISIBLE);
        deletebutton.setVisibility(View.INVISIBLE);

        videobutton.setVisibility(View.INVISIBLE);
        getCommentList();

        final TextView tag = (TextView) findViewById(R.id.tag);
        final ImageView image = (ImageView) findViewById(R.id.detailimage);

        editbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), ItemCreateActivity.class);
                intent.putExtra("heritageId",heritageId);
                intent.putExtra("description", ""+description.getText() );
                intent.putExtra("title", title.getText() );
                intent.putExtra("location", ""+location.getText());
                intent.putExtra("tags", ""+tag.getText());
                finish();
                startActivity(intent);
            }
        });

        deletebutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deletePost(heritageId);
            }
        });

        send_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (!TextUtils.isEmpty(comment_entry.getText()))
                {
                    Retrofit retrofit = ApiClient.getApiClient();
                    ApiInterface apiInterface = retrofit.create(ApiInterface.class);

                    final SharedPreferences sharedPref = getSharedPreferences("TOKENSHARED", Context.MODE_PRIVATE);
                    final String token = sharedPref.getString("TOKEN", null);

                    Call<JsonResponseComment> call = apiInterface.commentCreate(new CommentBody(comment_entry.getText().toString(),heritageId), "Token " + token);
                    call.enqueue(new Callback<JsonResponseComment>() {
                        @Override
                        public void onResponse(Call<JsonResponseComment> call, Response<JsonResponseComment> response) {
                            if(response.isSuccessful())
                            {
                                Toast.makeText(getApplicationContext(),"Comment Posted", Toast.LENGTH_SHORT).show();
                                getCommentList();
                                comment_entry.setText("");
                            }
                        }

                        @Override
                        public void onFailure(Call<JsonResponseComment> call, Throwable t) {

                        }
                    });
                }
            }
        });


        Retrofit retrofit = ApiClient.getApiClient();
        ApiInterface apiInterface = retrofit.create(ApiInterface.class);

        final SharedPreferences sharedPref = getSharedPreferences("TOKENSHARED", Context.MODE_PRIVATE);
        final String token = sharedPref.getString("TOKEN", null);

        Call<JsonResponseItemDetail> call = apiInterface.getItem(heritageId, "Token " + token);
        call.enqueue(new Callback<JsonResponseItemDetail>() {
            @Override
            public void onResponse(Call<JsonResponseItemDetail> call, final Response<JsonResponseItemDetail> response) {
                if(response.isSuccessful())
                {
                    if(response.body().isIs_upvoted())
                        upVote.setEnabled(false);
                    if(response.body().isIs_downvoted())
                        downVote.setEnabled(false);
                    if(response.body().isIs_owner()){
                        editbutton.setVisibility(View.VISIBLE);
                        deletebutton.setVisibility(View.VISIBLE);
                    }
                    voteCount.setText(Integer.toString(response.body().getUpvote_count()-response.body().getDownvote_count()));
                    //Log.d("RESPONSE", Integer.toString(response.body().getUpvote_count()));
                    String[] datestr = response.body().getEvent_date().toString().split("\\s+");
                    title.setText(response.body().getTitle());
                    date.setText(datestr[0] + "-"+ datestr[1] + "-" + datestr[2]);
                    location.setText(response.body().getLocation());
                    name.setText("By "+response.body().getCreator_username());
                    description.setText(response.body().getDescription());
                    for(TagResponse tags : response.body().getTags())
                    {
                        tag.append(tags.getName().toString());
                        tag.append(",");
                    }
                    /*int counter = 0;
                    Integer ind_img = null;
                    for(Media m:response.body().getMedia()){
                        if(m.getType().equals("image"))
                            ind_img = counter;
                        counter += 1;
                    }


                    if(ind_img!=null)*/
                    if(response.body().getMedia().size()>0)
                        Picasso.with(getApplicationContext()).load(ApiClient.BASE_URL+response.body().getMedia().get(0).getImage()).into(image);

                    if(response.body().getVideo()!=null) {
                        videobutton.setVisibility(View.VISIBLE);
                        videobutton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(response.body().getVideo().video_url)));
                                Log.i("Video", "Video Playing....");
                            }
                        });
                    }

                    layout.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onFailure(Call<JsonResponseItemDetail> call, Throwable t) {

            }
        });

        description.setMovementMethod(new ScrollingMovementMethod());
        description.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                v.getParent().requestDisallowInterceptTouchEvent(true);
                return false;
            }
        });
        //upvote and downvote functions
        upVote.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendPostUpVote();
            }
        });

        downVote.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendPostDownVote();

            }
        });
        deleteVote.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendPostDeleteVote();
            }
        });
    }

    /**
     * Delete the heritage item with heritage item's id
     * @param id Heritage item's id number
     */
    public void deletePost(int id){
        Retrofit retrofit = ApiClient.getApiClient();
        final SharedPreferences sharedPref = getSharedPreferences("TOKENSHARED", Context.MODE_PRIVATE);
        final String  token = sharedPref.getString("TOKEN", null);
        ApiInterface apiInterface = retrofit.create(ApiInterface.class);
        Call<JsonResponseDeletePost> call = apiInterface.deletePost(id,"Token " + token);
        call.enqueue(new Callback<JsonResponseDeletePost>() {
            @Override
            public void onResponse(Call<JsonResponseDeletePost> call, Response<JsonResponseDeletePost> response) {
                    finish();
                    startActivity(new Intent(getApplicationContext(), MainActivity.class));

            }

            @Override
            public void onFailure(Call<JsonResponseDeletePost> call, Throwable t) {
                finish();
                startActivity(new Intent(getApplicationContext(), MainActivity.class));
            }
        });
    }

    /**
     * Delete the upvote or downvote for the heritage item
     */
    public void sendPostDeleteVote(){
        Intent intent = getIntent();
        final int heritageId = intent.getIntExtra("heritageId", -1);
        Retrofit retrofit = ApiClient.getApiClient();
        final SharedPreferences sharedPref = getSharedPreferences("TOKENSHARED", Context.MODE_PRIVATE);
        final String  token = sharedPref.getString("TOKEN", null);
        ApiInterface apiInterface = retrofit.create(ApiInterface.class);
        Call<JsonResponseDeleteVote> call = apiInterface.deleteVote(new DeleteVoteBody(heritageId),"Token " + token);
        call.enqueue(new Callback<JsonResponseDeleteVote>() {
            @Override
            public void onResponse(Call<JsonResponseDeleteVote> call, Response<JsonResponseDeleteVote> response) {

                if (response.isSuccessful()) {
                    Toast.makeText(getApplicationContext(), "SUCCESSFUL DELETE VOTE", Toast.LENGTH_SHORT).show();
                    voteCount.setText(Integer.toString(response.body().getUpvote_count()-response.body().getDownvote_count()));
                    upVote.setEnabled(true);
                    downVote.setEnabled(true);

                }else if(response.code() == 404){
                    Toast.makeText(getApplicationContext(), "Firstly, please vote !!" , Toast.LENGTH_SHORT).show();
                }
                else {
                    Toast.makeText(getApplicationContext(), "Sorry for inconvince server is down" + response.code(), Toast.LENGTH_SHORT).show();
                    Log.d("response", response.raw().body().toString());
                }

            }

            @Override
            public void onFailure(Call<JsonResponseDeleteVote> call, Throwable t) {
                Toast.makeText(getApplicationContext(), "ERROR while posting", Toast.LENGTH_SHORT).show();
            }
        });

    }

    /**
     * Send a upvote request to api and upvote the heritage item
     */
    public void sendPostUpVote(){
        Intent intent = getIntent();
        final int heritageId = intent.getIntExtra("heritageId", -1);
        Retrofit retrofit = ApiClient.getApiClient();
        final SharedPreferences sharedPref = getSharedPreferences("TOKENSHARED", Context.MODE_PRIVATE);
        final String  token = sharedPref.getString("TOKEN", null);
        ApiInterface apiInterface = retrofit.create(ApiInterface.class);
        Call<JsonResponseVote> call = apiInterface.vote(new VoteBody(true, heritageId),"Token " + token);
        call.enqueue(new Callback<JsonResponseVote>() {
            @Override
            public void onResponse(Call<JsonResponseVote> call, Response<JsonResponseVote> response) {

                if (response.isSuccessful()) {
                    Toast.makeText(getApplicationContext(), "SUCCESSFUL UPVOTE", Toast.LENGTH_SHORT).show();
                    voteCount.setText(Integer.toString(response.body().getUpvote_count()-response.body().getDownvote_count()));
                    deleteVote.setEnabled(true);
                    upVote.setEnabled(false);
                    downVote.setEnabled(true);

                } else {
                    Toast.makeText(getApplicationContext(), "Sorry for inconvince server is down" + response.code(), Toast.LENGTH_SHORT).show();
                    Log.d("response", response.raw().body().toString());
                }

            }

            @Override
            public void onFailure(Call<JsonResponseVote> call, Throwable t) {
                Toast.makeText(getApplicationContext(), "ERROR while posting", Toast.LENGTH_SHORT).show();
            }
        });

    }

    /**
     * Get comment list from api
     */
    public void getCommentList(){
        Retrofit retrofit = ApiClient.getApiClient();
        ApiInterface apiInterface = retrofit.create(ApiInterface.class);

        final SharedPreferences sharedPref = getSharedPreferences("TOKENSHARED", Context.MODE_PRIVATE);
        final String token = sharedPref.getString("TOKEN", null);

        Call<List<JsonResponseComment>> call = apiInterface.getComments(heritageId, "Token " + token);
        call.enqueue(new Callback<List<JsonResponseComment>>() {
            @Override
            public void onResponse(Call<List<JsonResponseComment>> call, Response<List<JsonResponseComment>> response) {
                if (response.isSuccessful()) {
                    final ArrayList<JsonResponseComment> heritageList = (ArrayList<JsonResponseComment>) response.body();
                    //Log.d("RESPONSE", response.body());
                    setCommentRecyclerView(heritageList);
                } else {
                    Toast.makeText(getApplicationContext(), "Sorry for inconvince server is down" + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<JsonResponseComment>> call, Throwable t) {
                Toast.makeText(getApplicationContext(), "Sorry for inconvince server is down", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Set comment list in recycler view with heritage list
     * @param heritageList Heritage List that is json response from api
     */
    private void setCommentRecyclerView(final ArrayList<JsonResponseComment> heritageList){
        final RecyclerView heritageRecyclerView = (RecyclerView) findViewById(R.id.comment_recycler_view);
        final CommentAdapter heritageAdapter = new CommentAdapter(getApplicationContext(),heritageList);

        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        heritageRecyclerView.setLayoutManager(mLayoutManager);
        heritageRecyclerView.setItemAnimator(new DefaultItemAnimator());
        heritageRecyclerView.setAdapter(heritageAdapter);
        heritageAdapter.notifyDataSetChanged();
    }

    /**
     * Send a downvote request to api and downvote the heritage item
     */
    public void sendPostDownVote(){
        Intent intent = getIntent();
        final int heritageId = intent.getIntExtra("heritageId", -1);
        Retrofit retrofit = ApiClient.getApiClient();
        final SharedPreferences sharedPref = getSharedPreferences("TOKENSHARED", Context.MODE_PRIVATE);
        final String  token = sharedPref.getString("TOKEN", null);
        ApiInterface apiInterface = retrofit.create(ApiInterface.class);
        Call<JsonResponseVote> call = apiInterface.vote(new VoteBody(false, heritageId),"Token " + token);
        call.enqueue(new Callback<JsonResponseVote>() {
            @Override
            public void onResponse(Call<JsonResponseVote> call, Response<JsonResponseVote> response) {

                if (response.isSuccessful()) {
                    Toast.makeText(getApplicationContext(), "SUCCESSFUL DOWNVOTE", Toast.LENGTH_SHORT).show();
                    voteCount.setText(Integer.toString(response.body().getUpvote_count()-response.body().getDownvote_count()));
                    deleteVote.setEnabled(true);
                    downVote.setEnabled(false);
                    upVote.setEnabled(true);

                } else {
                    Toast.makeText(getApplicationContext(), "Sorry for inconvince server is down" + response.code(), Toast.LENGTH_SHORT).show();
                    Log.d("response", response.raw().body().toString());
                }

            }

            @Override
            public void onFailure(Call<JsonResponseVote> call, Throwable t) {
                Toast.makeText(getApplicationContext(), "ERROR while posting", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Back button for backing to parent
     */
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
        startActivity(new Intent(getApplicationContext(), MainActivity.class));
    }

    /**
     * @param item items in the action bar
     * @return selected item in the action bar
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                finish();
                startActivity(new Intent(getApplicationContext(), MainActivity.class));
        }
        return super.onOptionsItemSelected(item);
    }
}

package khamphai.org.ceitnews;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class MainActivity extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener {

    private RecyclerView mRecyclerView;
    private DatabaseReference mDatabaseReference;
    private SwipeRefreshLayout swipeRefreshLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initializes();

       FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, PostActivity.class));
            }
        });

    }

    private void initializes() {
        mDatabaseReference = FirebaseDatabase.getInstance().getReference().child("ceit-news");
        mDatabaseReference.keepSynced(true);
        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swiperefresh);
        swipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary, R.color.colorAccent);
        mRecyclerView = (RecyclerView) findViewById(R.id.list_news);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setReverseLayout(true);
        layoutManager.setStackFromEnd(true);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(layoutManager);
        swipeRefreshLayout.setOnRefreshListener(this);
        swipeRefreshLayout.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        swipeRefreshLayout.setRefreshing(true);
                                        LoadData();
                                    }
                                }
        );
    }

    @Override
    public void onRefresh() {
        LoadData();
    }

    @Override
    protected void onStart() {
        super.onStart();
        LoadData();
    }

    private void LoadData() {
        if (mDatabaseReference != null) {
            FirebaseRecyclerAdapter<ObjectDAO, ObjectDAOViewHolder> firebaseRecyclerAdapter =
                    new FirebaseRecyclerAdapter<ObjectDAO, ObjectDAOViewHolder>(
                            ObjectDAO.class,
                            R.layout.item_news,
                            ObjectDAOViewHolder.class,
                            mDatabaseReference
                    ) {
                        @Override
                        protected void populateViewHolder(ObjectDAOViewHolder viewHolder, ObjectDAO model, final int position) {
                            final String post_key = getRef(position).getKey();
                            viewHolder.setTitle(model.getTitle());
                            viewHolder.setDes(model.getDes());
                            viewHolder.setImage(getApplicationContext(), model.getImage());
                            viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    Intent detailIntent = new Intent(MainActivity.this, DetailActivity.class);
                                    detailIntent.putExtra("news_id", post_key);
                                    startActivity(detailIntent);
                                }
                            });
                            viewHolder.button.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {

                                    AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                                    builder.setTitle("ລົບຂ່າວນີ້");
                                    builder.setMessage("ເຈົ້າຕ້ອງການລົບຂ່າວນີ້ແທ້ບໍ່ ?");
                                    builder.setNegativeButton("ຍົກເລີກ", new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface paramAnonymousDialogInterface, int paramAnonymousInt) {
                                            Toast.makeText(MainActivity.this, "ຍົກເລີກການລົບຂ່າວ", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                    builder.setPositiveButton("ຕົກລົງ", new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface paramAnonymousDialogInterface, int paramAnonymousInt) {
                                            mDatabaseReference.child(post_key).removeValue();
                                            //LoadData();
                                            Toast.makeText(MainActivity.this, "ຂ່າວຖືລົບແລ້ວ", Toast.LENGTH_SHORT).show();

                                        }
                                    });
                                    builder.show();

                                }
                            });
                        }
                    };
            mRecyclerView.setAdapter(firebaseRecyclerAdapter);
            swipeRefreshLayout.setRefreshing(false);
        }else {
            swipeRefreshLayout.setRefreshing(false);
        }

    }

    public static class ObjectDAOViewHolder extends RecyclerView.ViewHolder{

        View mView;
        private Button button;
        public ObjectDAOViewHolder(View itemView) {
            super(itemView);
            mView = itemView;
            button = (Button) mView.findViewById(R.id.btnRemove);
        }

        public void setTitle(String title) {
            TextView tvTitle = (TextView) mView.findViewById(R.id.tvTitle);
            tvTitle.setText(title);
        }

        public void setDes(String des) {
            TextView tvDes = (TextView) mView.findViewById(R.id.tvDes);
            tvDes.setText(des);
        }

        public void setImage(Context context, String image) {
            ImageView imvShow = (ImageView) mView.findViewById(R.id.imvShow);
            Picasso.with(context).load(image).into(imvShow);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_camera) {
            startActivity(new Intent(MainActivity.this, PostActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private long backPressedTime = 0;

    @Override
    public void onBackPressed() {
        long t = System.currentTimeMillis();
        if (t - backPressedTime > 2000) {
            backPressedTime = t;
            Toast.makeText(this, "ກົດ BACK ອີກຄັ້ງເພື່ອອອກຈາກແອັປ",
                    Toast.LENGTH_SHORT).show();
        } else {
            super.onBackPressed();
        }
    } // end of onBackPressed

    @Override
    protected void attachBaseContext(Context context) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(context));
    }

}

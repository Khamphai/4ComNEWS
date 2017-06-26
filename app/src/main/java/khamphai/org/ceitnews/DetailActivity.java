package khamphai.org.ceitnews;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

public class DetailActivity extends AppCompatActivity {

    private ImageView imageView;
    private TextView tvTitle, tvDes;
    private String post_key = null;
    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        post_key = getIntent().getExtras().getString("news_id");
        databaseReference = FirebaseDatabase.getInstance().getReference().child("ceit-news");
        imageView = (ImageView) findViewById(R.id.imvShow);
        tvTitle = (TextView) findViewById(R.id.tvTitle);
        tvDes = (TextView) findViewById(R.id.tvDes);

        databaseReference.child(post_key).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String title = (String) dataSnapshot.child("title").getValue();
                String des = (String) dataSnapshot.child("des").getValue();
                String image = (String) dataSnapshot.child("image").getValue();
                tvTitle.setText(title);
                tvDes.setText(des);
                Picasso.with(getApplicationContext()).load(image).into(imageView);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
}

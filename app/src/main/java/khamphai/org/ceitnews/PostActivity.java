package khamphai.org.ceitnews;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class PostActivity extends AppCompatActivity {

    private ImageButton imbPic;
    private EditText edtTitle, edtDes;
    private Button btnPost;

    private Uri imgUri = null;
    private static final int GALLERY_REQUEST = 1;

    private StorageReference mStorageReference;
    private DatabaseReference mDatabaseReference;
    private ProgressDialog mProgressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);
        initializes();
    }

    private void initializes() {
        mStorageReference = FirebaseStorage.getInstance().getReference();
        mDatabaseReference = FirebaseDatabase.getInstance().getReference().child("ceit-news");

        imbPic = (ImageButton) findViewById(R.id.imb_pic);
        edtTitle = (EditText) findViewById(R.id.edt_title);
        edtDes = (EditText) findViewById(R.id.edt_des);
        btnPost = (Button) findViewById(R.id.btn_post);
        imbPic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent galleryIntent = new Intent(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/*");
                startActivityForResult(galleryIntent, GALLERY_REQUEST);
            }
        });

        btnPost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startPosting();
            }
        });
    }

    private void startPosting() {
        final String title = edtTitle.getText().toString().trim();
        final String des = edtDes.getText().toString().trim();

        if (!TextUtils.isEmpty(title) && !TextUtils.isEmpty(des) && imgUri != null) {
            mProgressDialog = ProgressDialog.show(this, "", "ກຳລັງເພິ່ມຂ່າວ...");
            hideSoftKeyboard();
            StorageReference filePath = mStorageReference.child("CEIT_NEWS_Images").child(imgUri.getLastPathSegment());
            filePath.putFile(imgUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    Uri downloadUrl = taskSnapshot.getDownloadUrl();
                    DatabaseReference mPost = mDatabaseReference.push();
                    mPost.child("title").setValue(title);
                    mPost.child("des").setValue(des);
                    mPost.child("image").setValue(downloadUrl.toString());
                    mProgressDialog.dismiss();
                    //startActivity(new Intent(PostActivity.this, MainActivity.class));
                    finish();
                }
            });
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == GALLERY_REQUEST && resultCode == RESULT_OK) {
            Uri imageUri = data.getData();
            CropImage.activity(imageUri)
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .start(this);

        }

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                imgUri = result.getUri();
                imbPic.setImageURI(imgUri);
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }

    }

    /**
     * Hides the soft keyboard
     */
    public void hideSoftKeyboard() {
        if (getCurrentFocus() != null) {
            InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
        }
    }

    @Override
    protected void attachBaseContext(Context context) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(context));
    }

}//end Main Activity

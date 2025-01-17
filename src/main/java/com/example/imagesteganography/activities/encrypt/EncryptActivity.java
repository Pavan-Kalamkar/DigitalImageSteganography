package com.example.imagesteganography.activities.encrypt;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import com.squareup.picasso.Picasso;
import java.io.File;
import com.example.imagesteganography.R;
import com.example.imagesteganography.activities.stego.StegoActivity;
import com.example.imagesteganography.utils.Constants;
import com.example.imagesteganography.utils.StandardMethods;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class EncryptActivity extends AppCompatActivity implements EncryptView {

  @BindView(R.id.etSecretMessage)
  EditText etSecretMessage;
  @BindView(R.id.ivCoverImage)
  ImageView ivCoverImage;
  @BindView(R.id.ivSecretImage)
  ImageView ivSecretImage;

  private ProgressDialog progressDialog;
  private EncryptPresenter mPresenter;
  private int whichImage = -1;
  private int secretMessageType = Constants.TYPE_TEXT;

  @OnClick({R.id.ivCoverImage, R.id.ivSecretImage})
  public void onCoverSecretImageClick(View view) {

    final CharSequence[] items = {
            getString(R.string.select_image_dialog)  // Only show "Select Image" now
    };

    AlertDialog.Builder builder = new AlertDialog.Builder(EncryptActivity.this);
    builder.setTitle(getString(R.string.select_image_title));
    builder.setCancelable(false);
    builder.setItems(items, new DialogInterface.OnClickListener() {
      @Override
      public void onClick(DialogInterface dialogInterface, int item) {
        if (items[item].equals(getString(R.string.select_image_dialog))) {
          // Check for permission to read storage
          if (ContextCompat.checkSelfPermission(getApplicationContext(),
                  Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            // If permission not granted, request permission
            ActivityCompat.requestPermissions(EncryptActivity.this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    Constants.PERMISSIONS_EXTERNAL_STORAGE);
          } else {
            // If permission granted, open gallery
            chooseImage();
          }
        }
      }
    });

    builder.setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
      @Override
      public void onClick(DialogInterface dialogInterface, int i) {
        dialogInterface.dismiss();
      }
    });

    // Determine which image view was clicked
    if (view.getId() == R.id.ivCoverImage) {
      whichImage = Constants.COVER_IMAGE;
    } else if (view.getId() == R.id.ivSecretImage) {
      whichImage = Constants.SECRET_IMAGE;
    }

    builder.show();
  }

  @OnClick(R.id.bEncrypt)
  public void onButtonClick() {
    if (secretMessageType == Constants.TYPE_IMAGE) {
      mPresenter.encryptImage();
    } else if (secretMessageType == Constants.TYPE_TEXT) {
      String text = getSecretMessage();

      if (!text.isEmpty()) {
        mPresenter.encryptText();
      } else {
        showToast(R.string.secret_text_empty);
      }
    }
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_encrypt_text);

    ActionBar actionBar = getSupportActionBar();
    actionBar.setTitle("Encrypt Text into Image");

    ButterKnife.bind(this);

    progressDialog = new ProgressDialog(EncryptActivity.this);
    progressDialog.setMessage("Please wait...");

    mPresenter = new EncryptPresenterImpl(this);

    SharedPreferences sp = getSharedPrefs();
    String filePath = sp.getString(Constants.PREF_COVER_PATH, "");
    boolean isCoverSet = sp.getBoolean(Constants.PREF_COVER_IS_SET, false);

    if (isCoverSet) {
      setCoverImage(new File(filePath));
    }
  }

  @Override
  public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
    super.onRequestPermissionsResult(requestCode, permissions, grantResults);

    if (requestCode == Constants.PERMISSIONS_EXTERNAL_STORAGE) {
      if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
        chooseImage();  // Open gallery if permission is granted
      } else {
        showToast(R.string.permission_denied_storage);  // Show a message if permission is denied
      }
    }
  }

  @Override
  public void chooseImage() {
    Intent intent = new Intent(
            Intent.ACTION_PICK,
            android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
    intent.setType("image/*");
    startActivityForResult(
            Intent.createChooser(intent, getString(R.string.choose_image)),
            Constants.SELECT_FILE);
  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);

    if (resultCode == RESULT_OK) {
      if (requestCode == Constants.SELECT_FILE) {
        Uri selectedImageUri = data.getData();
        if (selectedImageUri != null) {
          // Handle the selected image URI
          String tempPath = getPath(selectedImageUri, EncryptActivity.this);
          if (tempPath != null && !tempPath.isEmpty()) {
            // Display the image on the ImageView
            ivCoverImage.setImageURI(selectedImageUri);

            // Use the image path in your presenter to process
            mPresenter.selectImage(whichImage, tempPath);
          } else {
            showToast(R.string.image_selection_failed);
          }
        } else {
          showToast(R.string.image_selection_failed);
        }
      }
    } else {
      // Handle result not OK
      showToast(R.string.image_selection_failed);
    }
  }

  public String getPath(Uri uri, AppCompatActivity activity) {
    String filePath = null;

    if (uri.getScheme().equals("content")) {
      // For content URI (gallery images)
      Cursor cursor = activity.getContentResolver().query(uri, null, null, null, null);
      if (cursor != null) {
        int columnIndex = cursor.getColumnIndex(MediaStore.Images.Media.DATA);
        if (columnIndex != -1) {
          cursor.moveToFirst();
          filePath = cursor.getString(columnIndex);
        }
        cursor.close();
      }
    } else if (uri.getScheme().equals("file")) {
      // For file URI (camera images)
      filePath = uri.getPath();
    }

    return filePath;
  }

  @Override
  public void startStegoActivity(String filePath) {
    Intent intent = new Intent(EncryptActivity.this, StegoActivity.class);
    intent.putExtra(Constants.EXTRA_STEGO_IMAGE_PATH, filePath);
    startActivity(intent);
  }

  @Override
  public Bitmap getCoverImage() {
    return ((BitmapDrawable) ivCoverImage.getDrawable()).getBitmap();
  }

  @Override
  public void setCoverImage(File file) {
    showProgressDialog();
    Picasso.get()
            .load(file)
            .fit()
            .placeholder(R.drawable.ic_upload)
            .into(ivCoverImage);
    stopProgressDialog();
    whichImage = -1;

    SharedPreferences.Editor editor = getSharedPrefs().edit();
    editor.putString(Constants.PREF_COVER_PATH, file.getAbsolutePath());
    editor.putBoolean(Constants.PREF_COVER_IS_SET, true);
    editor.apply();
  }

  @Override
  public Bitmap getSecretImage() {
    return ((BitmapDrawable) ivSecretImage.getDrawable()).getBitmap();
  }

  @Override
  public void setSecretImage(File file) {
    showProgressDialog();
    Picasso.get()
            .load(file)
            .fit()
            .placeholder(R.drawable.ic_upload)
            .into(ivSecretImage);
    stopProgressDialog();
    whichImage = -1;
  }

  @Override
  public String getSecretMessage() {
    return etSecretMessage.getText().toString().trim();
  }

  @Override
  public void setSecretMessage(String secretMessage) {
    etSecretMessage.setText(secretMessage);
  }

  @Override
  public void showToast(int message) {
    StandardMethods.showToast(this, message);
  }

  @Override
  public void showProgressDialog() {
    if (progressDialog != null && !progressDialog.isShowing()) {
      progressDialog.show();
    }
  }

  @Override
  public void stopProgressDialog() {
    if (progressDialog != null && progressDialog.isShowing()) {
      progressDialog.dismiss();
    }
  }

  @Override
  public void openCamera() {

  }

  @Override
  public SharedPreferences getSharedPrefs() {
    return getSharedPreferences(Constants.SHARED_PREF_NAME, Context.MODE_PRIVATE);
  }
}
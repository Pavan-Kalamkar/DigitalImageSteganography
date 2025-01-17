package com.example.imagesteganography.activities.stego;

import android.content.Intent;
import android.net.Uri;

import java.io.File;

import com.example.imagesteganography.R;


class StegoPresenterImpl implements StegoPresenter {

  private StegoView mView;

  StegoPresenterImpl(StegoView mView) {
    this.mView = mView;
  }

  @Override
  public boolean saveStegoImage(String stegoPath) {
    mView.showProgressDialog();
    Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);

    File stegoFile = new File(stegoPath);
    Uri contentUri = Uri.fromFile(stegoFile);
    mediaScanIntent.setData(contentUri);

    mView.saveToMedia(mediaScanIntent);

    mView.showToast(R.string.save_image_success);
    mView.stopProgressDialog();
    return true;
  }

}

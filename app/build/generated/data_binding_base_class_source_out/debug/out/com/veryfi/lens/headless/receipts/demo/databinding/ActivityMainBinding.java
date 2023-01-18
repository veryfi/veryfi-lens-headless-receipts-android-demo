// Generated by view binder compiler. Do not edit!
package com.veryfi.lens.headless.receipts.demo.databinding;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.viewbinding.ViewBinding;
import androidx.viewbinding.ViewBindings;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.veryfi.lens.headless.receipts.demo.R;
import java.lang.NullPointerException;
import java.lang.Override;
import java.lang.String;

public final class ActivityMainBinding implements ViewBinding {
  @NonNull
  private final CoordinatorLayout rootView;

  @NonNull
  public final FloatingActionButton btnScan;

  @NonNull
  public final Switch switchAutoCropGallery;

  @NonNull
  public final Switch switchAutoDocDetection;

  @NonNull
  public final Switch switchAutoRotate;

  @NonNull
  public final Switch switchBlur;

  @NonNull
  public final Switch switchSkew;

  @NonNull
  public final TextView txtPrivacyPolice;

  @NonNull
  public final ImageView veryfiLogo;

  private ActivityMainBinding(@NonNull CoordinatorLayout rootView,
      @NonNull FloatingActionButton btnScan, @NonNull Switch switchAutoCropGallery,
      @NonNull Switch switchAutoDocDetection, @NonNull Switch switchAutoRotate,
      @NonNull Switch switchBlur, @NonNull Switch switchSkew, @NonNull TextView txtPrivacyPolice,
      @NonNull ImageView veryfiLogo) {
    this.rootView = rootView;
    this.btnScan = btnScan;
    this.switchAutoCropGallery = switchAutoCropGallery;
    this.switchAutoDocDetection = switchAutoDocDetection;
    this.switchAutoRotate = switchAutoRotate;
    this.switchBlur = switchBlur;
    this.switchSkew = switchSkew;
    this.txtPrivacyPolice = txtPrivacyPolice;
    this.veryfiLogo = veryfiLogo;
  }

  @Override
  @NonNull
  public CoordinatorLayout getRoot() {
    return rootView;
  }

  @NonNull
  public static ActivityMainBinding inflate(@NonNull LayoutInflater inflater) {
    return inflate(inflater, null, false);
  }

  @NonNull
  public static ActivityMainBinding inflate(@NonNull LayoutInflater inflater,
      @Nullable ViewGroup parent, boolean attachToParent) {
    View root = inflater.inflate(R.layout.activity_main, parent, false);
    if (attachToParent) {
      parent.addView(root);
    }
    return bind(root);
  }

  @NonNull
  public static ActivityMainBinding bind(@NonNull View rootView) {
    // The body of this method is generated in a way you would not otherwise write.
    // This is done to optimize the compiled bytecode for size and performance.
    int id;
    missingId: {
      id = R.id.btn_scan;
      FloatingActionButton btnScan = ViewBindings.findChildViewById(rootView, id);
      if (btnScan == null) {
        break missingId;
      }

      id = R.id.switch_auto_crop_gallery;
      Switch switchAutoCropGallery = ViewBindings.findChildViewById(rootView, id);
      if (switchAutoCropGallery == null) {
        break missingId;
      }

      id = R.id.switch_auto_doc_detection;
      Switch switchAutoDocDetection = ViewBindings.findChildViewById(rootView, id);
      if (switchAutoDocDetection == null) {
        break missingId;
      }

      id = R.id.switch_auto_rotate;
      Switch switchAutoRotate = ViewBindings.findChildViewById(rootView, id);
      if (switchAutoRotate == null) {
        break missingId;
      }

      id = R.id.switch_blur;
      Switch switchBlur = ViewBindings.findChildViewById(rootView, id);
      if (switchBlur == null) {
        break missingId;
      }

      id = R.id.switch_skew;
      Switch switchSkew = ViewBindings.findChildViewById(rootView, id);
      if (switchSkew == null) {
        break missingId;
      }

      id = R.id.txt_privacy_police;
      TextView txtPrivacyPolice = ViewBindings.findChildViewById(rootView, id);
      if (txtPrivacyPolice == null) {
        break missingId;
      }

      id = R.id.veryfi_logo;
      ImageView veryfiLogo = ViewBindings.findChildViewById(rootView, id);
      if (veryfiLogo == null) {
        break missingId;
      }

      return new ActivityMainBinding((CoordinatorLayout) rootView, btnScan, switchAutoCropGallery,
          switchAutoDocDetection, switchAutoRotate, switchBlur, switchSkew, txtPrivacyPolice,
          veryfiLogo);
    }
    String missingId = rootView.getResources().getResourceName(id);
    throw new NullPointerException("Missing required view with ID: ".concat(missingId));
  }
}

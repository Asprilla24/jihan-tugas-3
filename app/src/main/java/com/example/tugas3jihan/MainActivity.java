package com.example.tugas3jihan;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    private static final int CAMERA_PIC_REQUEST = 1337;
    private static final int MY_CAMERA_PERMISSION_CODE = 2456;
    private static final int EXTERNAL_STORAGE_PERMISSION_CODE = 2480;

    private Button cameraButton;
    private ImageView imageView;
    private TextView pathText;

    private Bitmap bitmap;
    private Uri gbrCaptureUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        cameraButton = (Button)findViewById(R.id.cameraButton);
        imageView = (ImageView) findViewById(R.id.imageView);
        pathText = (TextView) findViewById(R.id.pathText);

        if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
        {
            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, EXTERNAL_STORAGE_PERMISSION_CODE);
        }

        cameraButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED)
                {
                    requestPermissions(new String[]{Manifest.permission.CAMERA}, MY_CAMERA_PERMISSION_CODE);
                }
                else {
                    openCamera();
                }
            }
        });
    }

    private void openCamera(){
        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());
        File file = null;
        try {
            file = new File(Environment.getExternalStorageDirectory(),
                    String.valueOf(System.currentTimeMillis())+".jpg");
        } catch (Exception e) {
            e.printStackTrace();
        }
        gbrCaptureUri = Uri.fromFile(file);
        Intent i = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        i.putExtra(MediaStore.EXTRA_OUTPUT, gbrCaptureUri);
        startActivityForResult(i, CAMERA_PIC_REQUEST);
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CAMERA_PIC_REQUEST) {
            getBitmapImage();
        }
    }

    private void getBitmapImage() {
        Uri selectedImage = null;
        selectedImage = gbrCaptureUri;
        ContentResolver cr = getContentResolver();
        try {
            bitmap = android.provider.MediaStore.Images.Media.getBitmap(cr,
                    selectedImage);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

        if(bitmap == null){
            Toast.makeText(MainActivity.this, "Image error, please try again",
                    Toast.LENGTH_LONG).show();
        }

        showDialog();
    }

    private void showDialog() {
        LayoutInflater factory = LayoutInflater.from(this);
        final View deleteDialogView = factory.inflate(R.layout.custom_dialog, null);
        final AlertDialog deleteDialog = new AlertDialog.Builder(this).create();
        deleteDialog.setView(deleteDialogView);

        ImageView dialogImageView = (ImageView)deleteDialogView.findViewById(R.id.imageViewDialog);
        dialogImageView.setVisibility(ImageView.VISIBLE);
        dialogImageView.setImageBitmap(bitmap);

        deleteDialogView.findViewById(R.id.btn_yes).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveImage();
                deleteDialog.dismiss();
            }
        });
        deleteDialogView.findViewById(R.id.btn_no).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pathText.setText("");
                imageView.setVisibility(ImageView.INVISIBLE);
                deleteDialog.dismiss();
            }
        });

        deleteDialog.show();
    }

    private void saveImage() {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG,80, bytes);

        String nuFile = System.currentTimeMillis()+".jpg";

        File files = new File(Environment.getExternalStorageDirectory()+ File.separator+nuFile);

        try {
            files.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (files.exists()) {
            FileOutputStream fo = null;
            try {
                fo = new FileOutputStream(files);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            try {
                fo.write(bytes.toByteArray());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        String fileFoto = files.toString();
        pathText.setText(fileFoto);

        Drawable picture = new BitmapDrawable(fileFoto);
        imageView.setVisibility(ImageView.VISIBLE);
        imageView.setImageDrawable(picture);
        bitmap.recycle();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults)
    {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == MY_CAMERA_PERMISSION_CODE)
        {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED)
            {
                Toast.makeText(this, "camera permission granted", Toast.LENGTH_LONG).show();
                Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(cameraIntent, CAMERA_PIC_REQUEST);
            }
            else
            {
                Toast.makeText(this, "camera permission denied", Toast.LENGTH_LONG).show();
            }
        }
        if (requestCode == EXTERNAL_STORAGE_PERMISSION_CODE)
        {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED)
            {
                Toast.makeText(this, "external storage permission granted", Toast.LENGTH_LONG).show();
            }
            else
            {
                Toast.makeText(this, "external storage permission denied", Toast.LENGTH_LONG).show();
            }
        }
    }
}

package dev.chdu.picker.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import dev.chdu.picker.R;

public class MainActivity extends AppCompatActivity {

    private ImageView mImageView;
    private Button pickImageBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mImageView = (ImageView) findViewById(R.id.image_view);
        pickImageBtn = (Button) findViewById(R.id.button_pick_image);
        pickImageBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, PickImageActivity.class);
                startActivity(intent);
            }
        });
    }
}

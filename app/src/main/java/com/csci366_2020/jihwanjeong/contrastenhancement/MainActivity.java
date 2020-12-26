package com.csci366_2020.jihwanjeong.contrastenhancement;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.IOException;


public class MainActivity extends AppCompatActivity {

    // DECLARE REQUEST VARIABLE
    final int PICK_IMAGE_REQUEST = 111;

    // DECLARE VARIABLES
    boolean imageLoadStatus = false;
    TextView textViewOriginalImage;
    ImageView imageViewPart1, imageViewPart2;
    FloatingActionButton loadButton;
    Bitmap inputBM, outputBM;
    Button backButton, processButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // TO CONNECT USING ID
        imageViewPart1 = findViewById(R.id.imageViewPart1);
        imageViewPart2 = findViewById(R.id.imageViewPart2);
        loadButton = findViewById(R.id.floatingActionButtonLoad);
        backButton = findViewById(R.id.backButton);
        processButton = findViewById(R.id.processButton);
        textViewOriginalImage = findViewById(R.id.textView1);
    }

    public void updateImage(View v) {
        // IF THE IMAGE IS SUCCESSFULLY LOADED
        if (imageLoadStatus) {
            // TO GET THE WIDTH AND HEIGHT FROM THE IMAGE
            int width = inputBM.getWidth();
            int height = inputBM.getHeight();

            // TO DECLARE TWO RGB ARRAYS TO BE STORED
            int[] rgb = new int[3];
            int[] rgb1 = new int[3];

            // TO DECLARE THE COLOR VALUE FOR YUV
            float Y, U, V;

            // TO CREATE OUTPUT BITMAP IMAGE USING THE LOADED CONFIGURATION
            outputBM = Bitmap.createBitmap(width, height, inputBM.getConfig());

            // TO DECLARE ARRAYS FOR THE NUMBER OF GRAY VALUE BASED ON RGB COLORS
            double count0[] = new double[256];
            double count1[] = new double[256];
            double count2[] = new double[256];

            for (int j = 0; j < height; j++) {
                for(int i = 0; i < width; i++) {
                    // TO GET THE COLOR FROM EACH PIXEL
                    int color = inputBM.getPixel(i, j);
                    rgb[0] = Color.red(color);
                    rgb[1] = Color.green(color);
                    rgb[2] = Color.blue(color);

                    // TO COUNT THE NUMBER OF GRAY VALUES THAT APPEAR IN THE ENTIRE PICTURE
                    if (count0[rgb[0]]==0){
                        count0[rgb[0]]=1;
                    }
                    else {
                        count0[rgb[0]]+=1;
                    }
                    if (count1[rgb[1]]==0){
                        count1[rgb[1]]=1;
                    }
                    else {
                        count1[rgb[1]]+=1;
                    }
                    if (count2[rgb[2]]==0){
                        count2[rgb[2]]=1;
                    }
                    else {
                        count2[rgb[2]]+=1;
                    }
                }
            }

            // TO COUNT THE PROPORTION OF THE GRAY VALUE TO THE WHOLE
            double gl0[] = new double[256];
            for (int i=0;i<256;i++){
                gl0[i]=count0[i]/(width*height);
            }
            double gl1[] = new double[256];
            for (int i=0;i<256;i++){
                gl1[i]=count1[i]/(width*height);
            }
            double gl2[] = new double[256];
            for (int i=0;i<256;i++){
                gl2[i]=count2[i]/(width*height);
            }

            // TO SET UP STATISTICS CUMULATIVE HISTOGRAM (CUMULATIVE PERCENTAGE)
            double sk0[]=new double[256];
            for (int i=0;i<256;i++){
                for (int j=0;j<=i;j++){
                    sk0[i]+=gl0[j];
                }
            }
            double sk1[]=new double[256];
            for (int i=0;i<256;i++){
                for (int j=0;j<=i;j++){
                    sk1[i]+=gl1[j];
                }
            }
            double sk2[]=new double[256];
            for (int i=0;i<256;i++){
                for (int j=0;j<=i;j++){
                    sk2[i]+=gl2[j];
                }
            }

            // TO MULTIPLY THE CALCULATED ACCUMULATION RATIO BY THE MAXIMUM PIXEL VALUE (255)
            // TO RECORD THE NEW PIXEL VALUE
            double Sk0[]=new double[256];
            for (int i=0;i<256;i++){
                Sk0[i]=((255)*sk0[i]+0.5);
            }
            double Sk1[]=new double[256];
            for (int i=0;i<256;i++){
                Sk1[i]=((255)*sk1[i]+0.5);
            }
            double Sk2[]=new double[256];
            for (int i=0;i<256;i++){
                Sk2[i]=((255)*sk2[i]+0.5);
            }


            for (int j=0;j<height;j++){
                for(int i=0;i<width;i++){
                    int color = inputBM.getPixel(i, j);
                    int alpha = Color.alpha(color);
                    rgb[0] = Color.red(color);
                    rgb[1] = Color.green(color);
                    rgb[2] = Color.blue(color);

                    // TO ASSIGN THE NEW PIXEL VALUE TO THE ORIGINAL PIXEL VALUE
                    rgb1[0]=(int)Sk0[rgb[0]];
                    rgb1[1]=(int)Sk1[rgb[1]];
                    rgb1[2]=(int)Sk2[rgb[2]];

                    // TO CONVERT INTO YUV COLOR VALUE
                    Y = (float) (0.299*rgb1[0] + 0.587*rgb1[1] + 0.114*rgb1[2]);
                    U = (float) (-0.147*rgb1[0] - 0.289*rgb1[1] + 0.436*rgb1[2]);
                    V = (float) (0.615*rgb1[0] - 0.515*rgb1[1] - 0.100*rgb1[2]);

                    // TO CONVERT INTO RGB VALUE AGAIN
                    rgb1[0] = (int) (Y + 1.140*V);
                    rgb1[1] = (int) (Y - 0.395*U - 0.581*V);
                    rgb1[2] = (int) (Y + 2.032*U);

                    // IF THE COLOR VALUE IS OUT OF BOUNDS
                    if (rgb1[0] > 255)
                        rgb1[0] = 255;
                    else if (rgb1[0] < 0)
                        rgb1[0] = 0;

                    if (rgb1[1] > 255)
                        rgb1[1] = 255;
                    else if (rgb1[1] < 0)
                        rgb1[1] = 0;

                    if (rgb1[2] > 255)
                        rgb1[2] = 255;
                    else if (rgb1[2] < 0)
                        rgb1[2] = 0;

                    // TO SET EACH PIXEL WITH COLOR
                    outputBM.setPixel(i, j, Color.argb(alpha, rgb1[0], rgb1[1], rgb1[2]));
                }
            }
            imageViewPart2.setImageBitmap(outputBM);
        }
    }

    // WHEN THE IMAGE LOAD BUTTON IS CLICKED, IT RUNS
    public void loadImage(View view) {
        Intent i = new Intent();
        i.setType("image/*");
        i.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(i, "Select Picture"), PICK_IMAGE_REQUEST);
    }

    // IF THE USER CLICKS THE BACK BUTTON
    public void backButtonClick(View view) {
        imageViewPart1.setImageBitmap(null);
        imageViewPart2.setImageBitmap(null);
        backButton.setVisibility(View.INVISIBLE);
        loadButton.setVisibility(View.VISIBLE);
        textViewOriginalImage.setVisibility(View.INVISIBLE);
        processButton.setVisibility(View.INVISIBLE);
    }

    // WHEN RETURNING, IT RUNS FROM CHOOSER ACTIVITY
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri uri = data.getData();

            try {
                inputBM = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
                imageViewPart1.setImageBitmap(inputBM);
                imageLoadStatus = true;
                textViewOriginalImage.setVisibility(View.VISIBLE);
                backButton.setVisibility(View.VISIBLE);
                processButton.setVisibility(View.VISIBLE);
                loadButton.setVisibility(View.GONE);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
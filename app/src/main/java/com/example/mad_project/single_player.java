package com.example.mad_project;

import android.content.Intent;
import android.media.audiofx.PresetReverb;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class single_player extends AppCompatActivity{

    private Button[][] buttons = new Button[3][3];

    private boolean p1Turn = true;
    private int roundCount;

    private int p1Points;
    private int p2Points;

    private TextView text_view_p1;
    private TextView text_view_p2;
    private Button reset_btn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single_player);

        text_view_p1 = findViewById(R.id.tv_p1);
        text_view_p2 = findViewById(R.id.tv_p2);

        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                String buttonID = "btn_" + i + j;
                int resID = getResources().getIdentifier(buttonID, "id", getPackageName());

                buttons[i][j] = findViewById(resID);
                buttons[i][j].setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (!((Button) v).getText().toString().equalsIgnoreCase("")) {
                            return;
                        }

                        if(p1Turn) {
                            ((Button) v).setText("X");
                        }
                        else
                        {
                            ((Button) v).setText("O");
                        }
                        roundCount++;

                        if(checkWin()){
                            if(p1Turn){
                                p1Wins();
                            }
                            else {
                                p2Wins();
                            }
                        } else if(roundCount == 9) {
                            draw();
                        }else {
                            p1Turn = !p1Turn;
                        }
                    }
                });
            }
        }

        reset_btn = findViewById(R.id.btn_reset_board);
        reset_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resetBoard();
                text_view_p1.setText("Player 1: 0");
                text_view_p2.setText("Player 2: 0");
            }
        });
    }

    private boolean checkWin() {
        String[][] board = new String[3][3];

        for(int i=0;i<3;i++)
        {
            for(int j=0;j<3;j++)
            {
                board[i][j] = buttons[i][j].getText().toString();
            }
        }

        for(int i=0;i<3;i++) {
            if(board[i][0].equals(board[i][1]) &&  board[i][0].equals(board[i][2]) && !board[i][0].equals("")) {
                return true;
            }
        }

        for(int i=0;i<3;i++) {
            if(board[0][i].equals(board[1][i]) &&  board[0][i].equals(board[2][i]) && !board[0][i].equals("")) {
                return true;
            }
        }

        if(board[0][0].equals(board[1][1]) &&  board[0][0].equals(board[2][2]) && !board[0][0].equals("")) {
            return true;
        }
        if(board[0][2].equals(board[1][1]) &&  board[0][2].equals(board[2][0]) && !board[0][2].equals("")) {
            return true;
        }
        return false;
    }

    private void p1Wins() {
        p1Points++;
        Toast.makeText(getApplicationContext(), "Player 1 wins!", Toast.LENGTH_SHORT).show();
        updatePointsText();
        resetBoard();
    }

    private void p2Wins() {
        p2Points++;
        Toast.makeText(getApplicationContext(), "Player 2 wins!", Toast.LENGTH_SHORT).show();
        updatePointsText();
        resetBoard();
    }

    private void draw() {
        Toast.makeText(getApplicationContext(), "Draw!", Toast.LENGTH_SHORT).show();
        resetBoard();
    }

    private void resetBoard() {
        for(int i=0;i<3;i++)
        {
            for(int j=0;j<3;j++){
                buttons[i][j].setText("");
            }
        }
        roundCount=0;
        p1Turn=true;
    }
    private void updatePointsText() {
        text_view_p1.setText("Player 1: "+p1Points);
        text_view_p2.setText("Player 2: "+p2Points);
    }

    @Override
    public void onBackPressed() {
        Intent main_menu_intent = new Intent(single_player.this, main_menu.class);
        startActivity(main_menu_intent);
        finish();
    }
}





package com.example.mad_project;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.HashMap;

public class online_game extends AppCompatActivity {

    TextView tv_playerTurn;

    String player_session = "";
    String user_name = "";
    String enemy_name = "";
    String login_uid = "";
    String typeOfRequest = "";
    String myToken = "X";

    int gameState = 0;
    int activePlayer = 1;
    ArrayList<Integer> Player1 = new ArrayList<Integer>();
    ArrayList<Integer> Player2 = new ArrayList<Integer>();

    FirebaseDatabase database;
    DatabaseReference myRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_online_game);

        database = FirebaseDatabase.getInstance();
        myRef = database.getReference();

        user_name = getIntent().getExtras().get("user_name").toString();
        login_uid = getIntent().getExtras().get("login_uid").toString();
        enemy_name = getIntent().getExtras().get("enemy_name").toString();
        typeOfRequest = getIntent().getExtras().get("typeOfRequest").toString();
        player_session = getIntent().getExtras().get("player_session").toString();

        tv_playerTurn = (TextView) findViewById(R.id.tv_playerTurn);
        gameState = 1;

        if(typeOfRequest.equalsIgnoreCase("From")) {
            myToken = "O";
            tv_playerTurn.setText("Your Turn");
            myRef.child("playing").child(player_session).child("turn").setValue(user_name);

        }else {
            myToken = "X";
            tv_playerTurn.setText(enemy_name + "'s Turn");
            myRef.child("playing").child(player_session).child("turn").setValue(enemy_name);
        }

        myRef.child("playing").child(player_session).child("turn").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                try {
                    String value = (String) dataSnapshot.getValue();
                    if(value.equals(user_name)) {
                        tv_playerTurn.setText(enemy_name+"'s Turn");
                        setEnableClick(false);
                        activePlayer = 1;

                    }else if(value.equals(enemy_name)) {
                        tv_playerTurn.setText(enemy_name+"'s Turn");
                        setEnableClick(false);
                        activePlayer = 2;
                    }
                }catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        myRef.child("playing").child(player_session).child("game")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        try {
                            Player1.clear();
                            Player2.clear();

                            HashMap<String, Object> map = (HashMap<String, Object>) dataSnapshot.getValue();
                            if(map!=null) {
                                String value = "";
                                String firstPlayer = user_name;
                                for(String key: map.keySet()) {
                                    value = (String) map.get(key);
                                    if(value.equals(user_name)) {
                                        activePlayer = 2;
                                    }else {
                                        activePlayer=1;
                                    }
                                    firstPlayer = value;
                                    String[] splitId = key.split(":");
                                    enemy_name(Integer.parseInt(splitId[1]));
                                }
                            }

                        }catch (Exception e){
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

    } // End of onCreate

    void setEnableClick(boolean flag){
        ImageView iv;
        iv = (ImageView) findViewById(R.id.iv_00); iv.setClickable(flag);
        iv = (ImageView) findViewById(R.id.iv_01); iv.setClickable(flag);
        iv = (ImageView) findViewById(R.id.iv_02); iv.setClickable(flag);

        iv = (ImageView) findViewById(R.id.iv_10); iv.setClickable(flag);
        iv = (ImageView) findViewById(R.id.iv_11); iv.setClickable(flag);
        iv = (ImageView) findViewById(R.id.iv_12); iv.setClickable(flag);

        iv = (ImageView) findViewById(R.id.iv_20); iv.setClickable(flag);
        iv = (ImageView) findViewById(R.id.iv_21); iv.setClickable(flag);
        iv = (ImageView) findViewById(R.id.iv_22); iv.setClickable(flag);
    }


    public void GameBoardClick(View view) {
        ImageView selectedImage = (ImageView) view;

        if(player_session.length() <= 0 ) {
            Intent network_hub_intent = new Intent(online_game.this, network_hub.class);
            startActivity(network_hub_intent);
            finish();
        }else {
            int selectedBlock = 0;
            switch(selectedImage.getId()) {
                case R.id.iv_00: selectedBlock = 1; break;
                case R.id.iv_01: selectedBlock = 2; break;
                case R.id.iv_02: selectedBlock = 3; break;

                case R.id.iv_10: selectedBlock = 4; break;
                case R.id.iv_11: selectedBlock = 5; break;
                case R.id.iv_12: selectedBlock = 6; break;

                case R.id.iv_20: selectedBlock = 7; break;
                case R.id.iv_21: selectedBlock = 8; break;
                case R.id.iv_22: selectedBlock = 9; break;
            }
            myRef.child("playing").child(player_session).child("game").child("block:"+selectedBlock).setValue(user_name);
            myRef.child("playing").child(player_session).child("turn").setValue(enemy_name);
            setEnableClick(false);
            activePlayer=2;
            PlayGame(selectedBlock, selectedImage);
        }

    } // End of GameBoardClick

    void PlayGame(int selectedBlock, ImageView selectedImage) {
        if(gameState == 1){
            if(activePlayer==1) {
                selectedImage.setImageResource(R.drawable.logo_x);
                Player1.add(selectedBlock);
            }else if(activePlayer ==2 ){
                selectedImage.setImageResource(R.drawable.logo_o);
                Player2.add(selectedBlock);
            }
        }
        selectedImage.setEnabled(false);
        CheckWinner();
    } //End Of PlayGame

    void CheckWinner() {
        int winner = 0;

        // For Player1
        //Rows
        if(Player1.contains(1) && Player1.contains(2) && Player1.contains(3)){ winner = 1; }
        if(Player1.contains(4) && Player1.contains(5) && Player1.contains(6)){ winner = 1; }
        if(Player1.contains(7) && Player1.contains(8) && Player1.contains(9)){ winner = 1; }
        //Columns
        if(Player1.contains(1) && Player1.contains(4) && Player1.contains(7)){ winner = 1; }
        if(Player1.contains(2) && Player1.contains(5) && Player1.contains(8)){ winner = 1; }
        if(Player1.contains(3) && Player1.contains(6) && Player1.contains(9)){ winner = 1; }
        //Diagonals
        if(Player1.contains(1) && Player1.contains(5) && Player1.contains(9)){ winner = 1; }
        if(Player1.contains(3) && Player1.contains(5) && Player1.contains(7)){ winner = 1; }


        // For Player2
        //Rows
        if(Player2.contains(1) && Player2.contains(2) && Player2.contains(3)){ winner = 2; }
        if(Player2.contains(4) && Player2.contains(5) && Player2.contains(6)){ winner = 2; }
        if(Player2.contains(7) && Player2.contains(8) && Player2.contains(9)){ winner = 2; }
        //Columns
        if(Player2.contains(1) && Player2.contains(4) && Player2.contains(7)){ winner = 2; }
        if(Player2.contains(2) && Player2.contains(5) && Player2.contains(8)){ winner = 2; }
        if(Player2.contains(3) && Player2.contains(6) && Player2.contains(9)){ winner = 2; }
        //Diagonals
        if(Player2.contains(1) && Player2.contains(5) && Player2.contains(9)){ winner = 2; }
        if(Player2.contains(3) && Player2.contains(5) && Player2.contains(7)){ winner = 2; }

        if(winner != 0 && gameState == 1){
            if(winner == 1){
                ShowAlert(enemy_name +" is winner");
            }else if(winner == 2){
                ShowAlert("You won the game");
            }
            gameState = 2; //Game has a winner.
        }

        ArrayList<Integer> emptyBlocks = new ArrayList<Integer>();
        for(int i=1; i<=9; i++){
            if(!(Player1.contains(i) || Player2.contains(i))){
                emptyBlocks.add(i);
            }
        }
        if(emptyBlocks.size() == 0) {
            if(gameState == 1) {
                AlertDialog.Builder b = new AlertDialog.Builder(this, android.R.style.Theme_Material_Dialog_Alert);
                ShowAlert("Draw");
            }
            gameState = 3; // Game ended in a draw.
        }


    } // End oF CheckWinner

    void ShowAlert(String Title){
        AlertDialog.Builder b = new AlertDialog.Builder(this, R.style.TransparentDialog);
        b.setTitle(Title)
                .setMessage("Start a new game?")
                .setNegativeButton("Menu", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent(getApplicationContext(), main_menu.class);
                        startActivity(intent);
                        finish();
                    }
                })
                .setIcon(android.R.drawable.ic_dialog_info)
                .show();
    } //End of ShowAlert

    void enemy_name(int selectedBlock) {
        ImageView selectedImage = (ImageView) findViewById(R.id.iv_00);
        switch (selectedBlock) {
            case 1:
                selectedImage = (ImageView) findViewById(R.id.iv_00);
                break;
            case 2:
                selectedImage = (ImageView) findViewById(R.id.iv_01);
                break;
            case 3:
                selectedImage = (ImageView) findViewById(R.id.iv_02);
                break;

            case 4:
                selectedImage = (ImageView) findViewById(R.id.iv_10);
                break;
            case 5:
                selectedImage = (ImageView) findViewById(R.id.iv_11);
                break;
            case 6:
                selectedImage = (ImageView) findViewById(R.id.iv_12);
                break;

            case 7:
                selectedImage = (ImageView) findViewById(R.id.iv_20);
                break;
            case 8:
                selectedImage = (ImageView) findViewById(R.id.iv_21);
                break;
            case 9:
                selectedImage = (ImageView) findViewById(R.id.iv_22);
                break;
        }

        PlayGame(selectedBlock, selectedImage);
    } //End of enemy_name

    void ResetGame(){
        gameState = 1;
        activePlayer = 1;
        Player1.clear();
        Player2.clear();

        myRef.child("playing").child(player_session).removeValue();

        ImageView iv;
        iv = (ImageView) findViewById(R.id.iv_00); iv.setImageResource(0); iv.setEnabled(true);
        iv = (ImageView) findViewById(R.id.iv_01); iv.setImageResource(0); iv.setEnabled(true);
        iv = (ImageView) findViewById(R.id.iv_02); iv.setImageResource(0); iv.setEnabled(true);

        iv = (ImageView) findViewById(R.id.iv_10); iv.setImageResource(0); iv.setEnabled(true);
        iv = (ImageView) findViewById(R.id.iv_11); iv.setImageResource(0); iv.setEnabled(true);
        iv = (ImageView) findViewById(R.id.iv_12); iv.setImageResource(0); iv.setEnabled(true);

        iv = (ImageView) findViewById(R.id.iv_20); iv.setImageResource(0); iv.setEnabled(true);
        iv = (ImageView) findViewById(R.id.iv_21); iv.setImageResource(0); iv.setEnabled(true);
        iv = (ImageView) findViewById(R.id.iv_22); iv.setImageResource(0); iv.setEnabled(true);

    }


}

package com.example.mad_project;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.renderscript.Sampler;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.w3c.dom.Text;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class network_hub extends AppCompatActivity {

    private FirebaseAuth mAuth;
    FirebaseDatabase database;
    DatabaseReference myRef;
    private FirebaseAuth.AuthStateListener mAuthListener;

    ListView lv_sendInvite;
    ArrayList<String> list_sendInvite = new ArrayList<String>();
    ArrayAdapter adpt;

    ListView lv_acceptInvite;
    ArrayList<String> list_acceptInvite = new ArrayList<>();
    ArrayAdapter adpt2;

    TextView tv_sendInvite, tv_acceptInvite,tv_playerName;
    String userName, userEmail, userId;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_network_hub);

        mAuth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        myRef = database.getReference();

        tv_sendInvite = (TextView) findViewById(R.id.tv_sendInvite);
        tv_acceptInvite = (TextView) findViewById(R.id.tv_acceptInvite);
        tv_playerName = (TextView) findViewById(R.id.tv_playerName);
        tv_sendInvite.setText("Please Wait..");
        tv_acceptInvite.setText("Please Wait..");


        lv_sendInvite = (ListView) findViewById(R.id.lv_sendInvite);
        adpt = new ArrayAdapter(this, android.R.layout.simple_list_item_1, list_sendInvite);
        lv_sendInvite.setAdapter(adpt);

        lv_acceptInvite = (ListView) findViewById(R.id.lv_acceptInvite);
        adpt2 = new ArrayAdapter(this, android.R.layout.simple_expandable_list_item_1, list_acceptInvite);
        lv_acceptInvite.setAdapter(adpt2);


        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if(user!=null) {
                    //User is signed in
                    userId = user.getUid();
                    userEmail = user.getEmail();
                    userName = convertEmailToString(userEmail);
                    tv_playerName.setText(userName);

                    myRef.child("users").child(userName).child("request").setValue(userId);
                    adpt2.clear();
                    AcceptIncomingRequests();
                }else {
                    //User isn't signed in
                    CompleteRegistration();

                }
            }
        };

        myRef.getRoot().child("users").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                updateLoggedInUsers(dataSnapshot);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        lv_sendInvite.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                final String sendInviteTo = ((TextView) view).getText().toString();
                confirmDialog(sendInviteTo, "To");

            }
        });

        lv_acceptInvite.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                final String acceptInviteFrom = ((TextView) view).getText().toString();
                confirmDialog(acceptInviteFrom, "From");
            }
        });
    } // End of onCreate

    void confirmDialog(final String player, final String typeOfRequest) {
        AlertDialog.Builder b = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.connect_dialog, null);
        b.setView(dialogView);

        b.setTitle("Start Game?");
        b.setMessage("Connect with "+player);
        b.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                myRef.child("users")
                        .child(player).child("request").push().setValue(userEmail);

                if(typeOfRequest.equalsIgnoreCase("From")) {
                    startGame(player + ":"+ userName, player, "From");

                }else {
                    startGame(userName + ":" + player, player, "To");
                }
            }
        });

        b.setNegativeButton("Back", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        b.show();
    } // End of confirmDialog

    void startGame(String gameId, String player, String typeOfRequest) {
        myRef.child("playing").child(gameId).removeValue();
        Intent gameIntent = new Intent(network_hub.this, online_game.class);
        gameIntent.putExtra("player_session", gameId);
        gameIntent.putExtra("user_name", userName);
        gameIntent.putExtra("enemy_name", player);
        gameIntent.putExtra("login_uid", userId);
        gameIntent.putExtra("typeOfRequest", typeOfRequest);
        startActivity(gameIntent);
    } //End of startGame

    public void updateLoggedInUsers(DataSnapshot dataSnapshot) {
        String key = "";
        Set<String> set = new HashSet<String>();
        Iterator i = dataSnapshot.getChildren().iterator();

        while(i.hasNext()) {
            key = ((DataSnapshot) i.next()).getKey();
            if(!key.equalsIgnoreCase(userName)) {
                set.add(key);
            }
        }

        adpt.clear();
        adpt.addAll(set);
        adpt.notifyDataSetChanged();
        tv_sendInvite.setText("Send Invite To");
        tv_acceptInvite.setText("Accept Invite From");

    }// End of updateLoggedInUsers

    void AcceptIncomingRequests() {
        myRef.child("users").child(userName).child("request")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        try {
                            HashMap<String, Object> map = (HashMap<String, Object>) dataSnapshot.getValue();
                            if(map!=null) {
                                String value = "";
                                for( String key: map.keySet()) {
                                    value = (String) map.get(key);

                                    adpt2.add(convertEmailToString(value));
                                    adpt2.notifyDataSetChanged();
                                    myRef.child("users").child(userName).child("request").setValue(userId);
                                }

                            }
                        }catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

    } // End of AcceptIncomingRequests

    void CompleteRegistration() {
        AlertDialog.Builder b = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.login_dialog, null);
        b.setView(dialogView);

        final EditText etEmail = (EditText) dialogView.findViewById(R.id.etEmail);
        final EditText etPassword = (EditText) dialogView.findViewById(R.id.etPassword);

        b.setTitle("Authentication Window");
        b.setMessage("Enter you email and password for registration");
        b.setPositiveButton("Register", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                    createAccount(etEmail.getText().toString(), etPassword.getText().toString());
            }
        });
        b.setNeutralButton("Login", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                    loginAccount(etEmail.getText().toString(), etPassword.getText().toString());
            }
        });
        b.setNegativeButton("Back", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(getApplicationContext(), main_menu.class);
                startActivity(intent);
                finish();
            }
        });
        b.show();
    } // End of CompleteRegistration

    private String convertEmailToString(String Email) {
        String value = Email.substring(0, Email.indexOf('@'));
        return value;
    } // End of convertEmailToString

    public void createAccount(String email, String password) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // If sign in succeeds
                            // the auth state listener will be notified and logic to handle the
                            // signed in user can be handled in the listener.
                            Toast.makeText(network_hub.this, "Registration Complete, Account created.",
                                    Toast.LENGTH_SHORT).show();

                        } else {
                            // If sign in fails, display a message to the user.

                            Toast.makeText(network_hub.this, "Registration failed.",
                                    Toast.LENGTH_SHORT).show();

                        }
                    }
                });
    } // End of createAccount


    public void loginAccount(String email, String password) {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Toast.makeText(network_hub.this, "Authentication Complete, Login Successful.",
                                    Toast.LENGTH_SHORT).show();

                        } else {
                            // If sign in fails, display a message to the user.
                            Toast.makeText(network_hub.this, "Login failed.",
                                    Toast.LENGTH_SHORT).show();

                        }
                    }
                });
    } // End of loginAccount

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
//        FirebaseUser currentUser = mAuth.getCurrentUser();
        mAuth.addAuthStateListener(mAuthListener);
    } // End of onStart

    @Override
    public void onBackPressed() {
        //mAuth.signOut();
        Intent main_menu_intent = new Intent(network_hub.this, main_menu.class);
        startActivity(main_menu_intent);
        finish();
    } // End of onBackPressed
}

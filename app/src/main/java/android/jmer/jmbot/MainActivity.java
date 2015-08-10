package android.jmer.jmbot;

import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.ImageView;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.app.AlertDialog;


public class MainActivity extends ActionBarActivity implements SensorEventListener{
    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {
    }

    //region Variables globales
    SeekBar sbSeuil;
    TextView tvBat;

    ImageView imgv1;
    RadioButton rbTouch;
    RadioButton rbAccelero;
    RadioGroup btRadio;
    RadioButton rbLigne;
    RadioButton rbProxim;

	TextView tvMsg;
    TextView tvMode;
    //ScrollView scrollView;
    Toolbar myToolbar;

    private final int off = 0;
	boolean accelPresent;
	boolean accelActif;
    private int choixAccelero = off;
	int centrePulse;
	double gainAccelero=50;
	
	SensorManager sm;
	private double gain;

    // identification des modes de fonctionnement
    public static final int ModeTouch= 0;
    public static final int ModeAcelleros = 1;
    public static final int ModeSuiveur= 2;
    public static final int ModeProxim = 3;
    public static final int ModeSeuils = 4;
    public static final int ModeSuicide = 5;
    public static final int ModeContour = 6;

	// Identificateurs des messages recus du "Handler" de "BluetoothService"
	public static final int MESSAGE_STATE_CHANGE = 1;
	public static final int MESSAGE_READ = 2;
	public static final int MESSAGE_WRITE = 3;
	public static final int MESSAGE_DEVICE_NAME = 4;
	public static final int MESSAGE_TOAST = 5;
	
	// Noms clé reçus du "Handler" de "BluetoothService"
	public static final String DEVICE_NAME = "device_name";
	public static final String TOAST = "toast";
	
	// Codes de demandé de l'Intent
	private static final int REQUEST_CONNECT_DEVICE = 1;
	private static final int REQUEST_ENABLE_BT = 2;
	private static final int REQUEST_Settings= 3;
	
	// Nom du périphérique Bluetooth connecté
	private String mConnectedDeviceName = null;
	// Classe de l'adaptateur Bluetooth local
	private BluetoothAdapter mBluetoothAdapter = null;
	// Classe du service "BluetoothService"
	private BluetoothService mBluetoothService = null;

	int longueur = 0;
    //endregion


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);


		setContentView(R.layout.activity_main);

        rbAccelero=(RadioButton)findViewById(R.id.rbAccelero);
        rbTouch = (RadioButton)findViewById(R.id.rbTouch);
        btRadio=(RadioGroup)findViewById(R.id.btRadio);
		tvMsg=(TextView)findViewById(R.id.tvMsg);
        tvMode=(TextView)findViewById(R.id.tvMode);
		//tvMsg.setText("jmDoidBot\n");
		//tv1=(TextView)findViewById(R.id.tv1);
		//scrollView=(ScrollView)findViewById(R.id.scrollView);
        imgv1=(ImageView)findViewById(R.id.imgv1);
        sbSeuil = (SeekBar)findViewById(R.id.sbSeuil);
        tvBat = (TextView)findViewById(R.id.tvBat);

        myToolbar = (Toolbar) findViewById(R.id.app_bar);
        if(myToolbar != null)
        {
            setSupportActionBar(myToolbar);
            getSupportActionBar().setDisplayShowTitleEnabled(true);
            //getSupportActionBar().setDisplayShowHomeEnabled(true);
            //getSupportActionBar().setDisplayUseLogoEnabled(true);

       }

        sbSeuil.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                String seuil ="setBotTrig "+ (1800+progress) +"\n";
                tvMsg.setText(seuil);

                String msgBT = seuil+"\n";
                if(mBluetoothService.getState() == mBluetoothService.STATE_CONNECTED) {
                    byte[] mBytes=seuil.getBytes();
                    mBluetoothService.write(mBytes);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        btRadio.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                RadioButton rb1 = (RadioButton)findViewById(R.id.rbTouch);
                RadioButton rb2 = (RadioButton)findViewById(R.id.rbAccelero);
                RadioButton rb3 =(RadioButton)findViewById(R.id.rbLigne);
                RadioButton rb4 = (RadioButton)findViewById(R.id.rbProxim);
                String mode ="";

                if(rb1.isChecked())
                {
                    accelActif=false;
                    tvMode.setText("Mode Touch");
                    imgv1.setVisibility(View.VISIBLE);
                    sbSeuil.setVisibility(View.INVISIBLE);
                    mode = "setBotMode "+ModeTouch;
                }
                if(rb2.isChecked())
                {
                    accelActif = true;
                    tvMode.setText("Mode Accéléromètres");
                    imgv1.setVisibility(View.INVISIBLE);
                    sbSeuil.setVisibility(View.INVISIBLE);
                    mode = "setBotMode "+ModeAcelleros;
                }
                if(rb3.isChecked())
                {
                    tvMode.setText("Mode Suiveur de Ligne\n");
                    accelActif = false;
                    imgv1.setVisibility(View.INVISIBLE);
                    sbSeuil.setVisibility(View.VISIBLE);
                    mode = "setBotMode "+ModeSuiveur;
                }
                if(rb4.isChecked())
                {
                    tvMode.setText("Mode Détection de Proximité\n");
                    accelActif = false;
                    imgv1.setVisibility(View.INVISIBLE);
                    sbSeuil.setVisibility(View.INVISIBLE);
                    mode = "setBotMode "+ModeProxim;
                }

                if(mBluetoothService.getState() == mBluetoothService.STATE_CONNECTED) {
                    String msgBT =mode+ "\n";
                    byte[] mBytes=msgBT.getBytes();
                    mBluetoothService.write(mBytes);
                }
            }
        });

        imgv1.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {

                double X;
                double Y;
                double m1;
                double m2;
                String dir1 = "1 ";
                String dir2=" 1 ";

                // la zone touch est de 256 px
                // 0,0 coin supérieur gauche
                // 256, 256 coin inférieur droit
                // translation du 0,0 au centre de la zone
                // alors x et Y varient maintenant de -255 0 255
                // valeur pour le PWM 8 bits
                X= motionEvent.getX()*2-256;
                Y=motionEvent.getY()*2-256;

                // tank drive
                m1 = X+Y;
                m2 = Y-X;

                // pour obtenir plus facilement pleine vitesse
                if(m1>220) m1=255;

                // pour arrèter plus facilement
                if(m1>-50 && m1<50)m1=0;
                if(m1<-220) m1=-255;


                // pour pivoter moins rapidement
                if(m1<0 && m2>0 || m1>0 && m2<0){
                    m1 = m1/4;
                    m2 = m2/4;
                }

                // pour changement de direction
                if(m1<0){
                    dir1="0 ";
                    m1=0-m1;
                }

                // voir les commentaires du moteur m1
                if(m2>220)m2=255;
                if(m2>-50 && m2<50)m2=0;
                if(m2<-220)m2=-255;
                if(m2<0){
                    dir2=" 0 ";
                    m2=0-m2;
                }


                String msg = "jmBot "+dir1 +(int)m1 +dir2 +(int)m2;
                //String msg = (int)X +"  " +(int)Y;
                tvMsg.setText(msg);


                if(mBluetoothService.getState() == mBluetoothService.STATE_CONNECTED) {
                    String msgBT =msg+ "\n";
                    byte[] mBytes=msgBT.getBytes();
                    mBluetoothService.write(mBytes);
                }

                return true;
            }
        });


        // Recherche de l'adaptateur Bluetooth
		message("\nVérifie si Android a un BT ...\n");
		mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		// Test de présence de l'adaptateur
		if (mBluetoothAdapter == null) 
		{// Absent : affichage d'un message spécifique et arrêt de l'application
			Toast.makeText(this, "Bluetooth non disponible", Toast.LENGTH_LONG).show();
			finish();
		}
		
		message("Android BT présent\n");
		message("Activation de "+mBluetoothAdapter.getName()+"\n");
		mBluetoothAdapter.enable();
		//ensureDiscoverable() ;
		message("Découverte des modules BT...\n");


        message("Recherche d'un accéléromètre\nAccéléromètre ");
    	accelPresent = false;
		sm = (SensorManager)getSystemService(Context.SENSOR_SERVICE);
		if(sm.getSensorList(Sensor.TYPE_ACCELEROMETER).size() != 0){
			Sensor s =sm.getSensorList(Sensor.TYPE_ACCELEROMETER).get(0);
			accelPresent = true;
			sm.registerListener(this, s, SensorManager.SENSOR_DELAY_NORMAL);
		}
		
		if(!accelPresent)message("non ");
		message("présent\n");

	}

    //region Messages scrollview
    private void message(String s)
	{
		tvMsg.setText(s);
		
	//	scrollView.post(new Runnable(){
	//	 public void run(){
	//		 scrollView.fullScroll(View.FOCUS_DOWN);
	//	 }
	 //});
	}
    //endregion
	
	protected void connect() {
	     // Choix : "Voir les périphériques Bluetooth enregistrés"
	     Intent serverIntent = new Intent(this, BtListActivity.class);
	     // Démarrer l'activité "BtListActivity" avec l'identifiant `
	     // de réponse REQUEST_CONNECT_DEVICE
	     startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE);	
	}

	@Override
	public void onSensorChanged(SensorEvent arg0) {
		if(accelActif){

            String dir1 = "1 ";
            int X=(int)(arg0.values[0]*gainAccelero) ;
            String dir2=" 1 ";
            int Y=(int) (arg0.values[1]*gainAccelero) ;

            int m1 = X+Y;
            int m2 = Y-X;

            if(m1>220) m1=255;
            if(m1>-20 && m1<20)m1=0;

            if(m1<-220) m1=-255;
            if(m1<0){
                dir1="0 ";
                m1=0-m1;
            }


            if(m2>220)m2=255;
            if(m2>-20 && m2<20)m2=0;
            if(m2<-220)m2=-255;
            if(m2<0){
                dir2=" 0 ";
                m2=0-m2;
            }

            String msg = "jmBot "+dir1 +(int)m1 +dir2 +(int)m2;
            tvMsg.setText(msg);

			
			if(mBluetoothService.getState() == mBluetoothService.STATE_CONNECTED) {
				String msgBT =msg+ "\n";
				byte[] mBytes=msgBT.getBytes();
				mBluetoothService.write(mBytes);
			}

		}
		
	}
	
	 @Override
	 protected void onStart() 
	 {
	  super.onStart();
	  // L'adaptateur Bluetooth est-il déjà activé ?
	  if (!mBluetoothAdapter.isEnabled()) 
	  {// Non : demande d'activation de l'adaptateur via un Intent
	   Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
	   // Une fenêtre de choix est demandée à l'utilisateur
	   startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
	   // L'activité système répond par un appel de la méthode "onActivityResult"
	  }
	  else 
	   // Oui : initialisation de "BluetoothService" pour gérer la connexion
	   mBluetoothService = new BluetoothService(this, mHandler); // mHandler ci-dessous
	 }

	 @Override
	 public void onDestroy() 
	 {
	  super.onDestroy();
	  // Arrêt des services de "BluetoothService"
	  if (mBluetoothService!=null) mBluetoothService.stop();
	 }

    //region Menu
    /*
    * Méthodes de gestion du menu
    * @see android.app.Activity#onCreateOptionsMenu(android.view.Menu)
     */
    // Constructeur
    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        return true;
    }

    // Méthodes de traitement du choix dans le menu
    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        String mode =null;
        switch (item.getItemId())
        {

            case R.id.accel:
                accelActif = true;
                tvMode.setText("Mode Accéléromètre");
                imgv1.setVisibility(View.INVISIBLE);
                sbSeuil.setVisibility(View.INVISIBLE);
                mode = "setBotMode "+ModeAcelleros;
                TxBT(mode);
                return(true);

            case R.id.line:
                tvMode.setText("Mode Suiveur de Ligne\n");
                accelActif = false;
                imgv1.setVisibility(View.INVISIBLE);
                sbSeuil.setVisibility(View.INVISIBLE);
                mode = "setBotMode "+ModeSuiveur;
                TxBT(mode);
                return true;

            case R.id.seuils:
                tvMode.setText("Mode Ajuster Seuils\n");
                accelActif = false;
                imgv1.setVisibility(View.INVISIBLE);
                sbSeuil.setVisibility(View.VISIBLE);
                mode = "setBotMode "+ModeSeuils;
                TxBT(mode);
                return true;

            case R.id.touch:
                accelActif=false;
                tvMode.setText("Mode Touch");
                imgv1.setVisibility(View.VISIBLE);
                sbSeuil.setVisibility(View.INVISIBLE);
                mode = "setBotMode "+ModeTouch;
                TxBT(mode);
                return true;

            case R.id.suicide:
                tvMode.setText("Mode pas fou\n");
                accelActif = false;
                imgv1.setVisibility(View.INVISIBLE);
                sbSeuil.setVisibility(View.INVISIBLE);
                mode = "setBotMode "+ModeSuicide;
                TxBT(mode);
                return true;

            case R.id.contour:
                tvMode.setText("Mode Contour\n");
                accelActif = false;
                imgv1.setVisibility(View.INVISIBLE);
                sbSeuil.setVisibility(View.INVISIBLE);
                mode = "setBotMode "+ModeContour;
                TxBT(mode);
                return true;

            case R.id.proxim:
                tvMode.setText("Mode Proximité\n");
                accelActif = false;
                imgv1.setVisibility(View.INVISIBLE);
                sbSeuil.setVisibility(View.INVISIBLE);
                mode = "setBotMode "+ModeProxim;
                TxBT(mode);
                return true;


            case R.id.list_pairred:
                // Choix : "Voir les périphériques Bluetooth enregistrés"
                Intent serverIntent = new Intent(this, BtListActivity.class);
                // Démarrer l'activité "BtListActivity" avec l'identifiant `
                // de réponse REQUEST_CONNECT_DEVICE
                startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE);
                message("");
                return true;

            case R.id.about:
                //Toast.makeText(getApplicationContext(),"ABot de jmer 2015",Toast.LENGTH_SHORT).show();
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("jmABot 1.0 par AsTuVuComment@gmail.com");
                builder.setMessage("Contrôle de robot par Android").setCancelable(false).setPositiveButton("OK",new DialogInterface.OnClickListener()
                        {
                            public void onClick(DialogInterface dialog, int id){
                                // do things if you want to
                            }
                        }
                );
                AlertDialog alert = builder.create();
                alert.show();
                tvMode.setText("À Propos");
                return true;


            case R.id.quit: // Choix : "Quitter"
                finish();     // Fermeture de l'application
                return true;
        }

        return false;
    }

    private void TxBT(String mode){
        // transmet le mode via BlueTooth si présent
        if(mode !=null) {
            message(mode);
            if (mBluetoothService.getState() == mBluetoothService.STATE_CONNECTED) {
                String msgBT = mode + "\n";
                tvMode.setText(msgBT);
                byte[] mBytes = msgBT.getBytes();
                mBluetoothService.write(mBytes);
            }
            else  message(mode + " Bluetooth non connecté");
        }

    }
    //endregion

    // Méthode appelée à la fermeture d'une activitè
    // Elle identifie la rèponse pour rèaliser le traitement correspondant
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        switch (requestCode)
        {
            case REQUEST_ENABLE_BT:
                // L'activitè système de demande d'activation de l'adaptateur Bluetooth a été
                // lancée avec cet identifiant
                if (resultCode == Activity.RESULT_OK)
                {
                    // Le module Bluetooth est maintenant validé : démarrer "BluetoothService"
                    // pour gérer la connexion et son maintien
                    mBluetoothService = new BluetoothService(this, mHandler); // mHandler ci-dessous
                    // Le Handler gère les messages de "BluetoothService"
                }
                else
                {
                    // L'utilisateur a choisi de ne pas valider le module Bluetooth (ou erreur)
                    Toast.makeText(this, "Le module Bluetooth n'est pas activé", Toast.LENGTH_SHORT).show();
                    finish();
                }
                break;
            case REQUEST_CONNECT_DEVICE:
                // L'activité "BtListActivity" a été lancée avec cet identifiant
                if (resultCode == Activity.RESULT_OK) // "BtListActivity" s'est terminée normalement
                {// Connexion au périphérique choisi
                    // Lecture de l'adresse MAC du périphérique sélectionné
                    String address = data.getExtras().getString(BtListActivity.EXTRA_DEVICE_ADDRESS);
                    // Instanciation de l'objet "BLuetoothDevice"
                    BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
                    // Connexion au périphérique
                    tvMsg.append("RequÃªte de connexion Ã  l'adresse:\n");
                    tvMsg.append(address+"\n");
                    mBluetoothService.connect(device); // Démarrer le "ConnectThread" pour établir la connexion
                }
                break;
        }
    }
    //endregion

    //region Bluetooth
    // Méthode non utilisée dans cette application
	 private void ensureDiscoverable() 
	 {
	  if (mBluetoothAdapter.getScanMode()!=BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) 
	  {
	   Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
	   discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
	   startActivity(discoverableIntent);
	  }
	 }

	 /*
	  *  Handler utilisé pour obtenir les messages du "BluetoothService"
	  */
	 final Handler mHandler = new Handler() 
	 {
	  @Override
	  public void handleMessage(Message msg) 
	  {
	   switch (msg.what) 
	   {// Identifier le message
	    case MESSAGE_STATE_CHANGE:  // L'état du service a changé
	         switch (msg.arg1) 
	         {
	         case BluetoothService.STATE_CONNECTING: // Connexion en cours
	             setTitle("Connection ...");
	             break;
	          case BluetoothService.STATE_CONNECTED: // Connexion établie
	            setTitle("Connecté Ã  "+mConnectedDeviceName);
	            break;
	          case BluetoothService.STATE_LISTEN:
	          case BluetoothService.STATE_NONE:      // Pas de connexion
	            setTitle("Non connecté.");
	            break;
	         }
	         break;
	    case MESSAGE_WRITE: // Une écriture vers le périphérique a été déclenchée
	         byte[] writeBuf = (byte[]) msg.obj; // Tableau d'octets transmis
	         // On n'en fait rien ici !
	         break;
	    case MESSAGE_READ: // Des octets ont été reçus du périphérique Bluetooth
	         byte[] readBuf = (byte[]) msg.obj; // Tableau contenant les octets reçus
	         longueur=readBuf.length;
	         // Pour les tests : retransmission du tableau complet
	         //mBluetoothService.write(readBuf);
	         tvMsg.append(readBuf.toString());
	         //setTitle((int)longueur);
	         break;
	    case MESSAGE_DEVICE_NAME: // Réception du message de la demande du nom du périphérique
	         // Sauvegarde du nom du périphérique Bluetooth
	         mConnectedDeviceName = msg.getData().getString(DEVICE_NAME);
	         // Affichage temporaire de ce nom
	         Toast.makeText(getApplicationContext(), "Connecté à  "
	                        + mConnectedDeviceName, Toast.LENGTH_SHORT).show();
	        // byte[] who = "jmer\n".getBytes();
	 		// mBluetoothService.write(who);
	         break;
	    case MESSAGE_TOAST: // Afficher un message "toast"
	         Toast.makeText(getApplicationContext(), msg.getData().getString(TOAST), Toast.LENGTH_SHORT).show();
	         break;
	   }
	  }
	 };
    //endregion





}

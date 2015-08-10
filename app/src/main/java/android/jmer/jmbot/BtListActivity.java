package android.jmer.jmbot;

import java.util.Set;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

import android.jmer.jmbot.R;

public class BtListActivity extends Activity implements OnClickListener
{
 // Return Intent extra
 public static String EXTRA_DEVICE_ADDRESS = "device_address";
 
 // Classe de l'adaptateur Bluetooth
 public static BluetoothAdapter monBluetooth;
 
 //D�claration des instances des widgets de l'�cran "listes_bt"
 Button btnScanBT;
 
 // Adaptateurs de tableaux pour manipuler les 2 "ListView"
 public ArrayAdapter mesPairedDevicesArrayAdapter;
 public ArrayAdapter mesNewDevicesArrayAdapter;
 
 @Override
 public void onCreate(Bundle savedInstanceState) 
 {
  super.onCreate(savedInstanceState);
  // Affichage de l'�cran des ListView
  setContentView(R.layout.listes_bt);
  
  // Cr�ation de l'instance du bouton de l'�cran "listes_bt"
  btnScanBT=(Button)findViewById(R.id.scanBluetooth);
  
  // Affecter par prudence le r�sultat CANCELED en cas d'arr�t pr�matur�
  setResult(Activity.RESULT_CANCELED);
  
  // "Ecouteur" du bouton "Scan Bluetooth"
  btnScanBT.setOnClickListener(this);
  
  // Initialisation des "ArrayAdapter". Un pour chaque type de liste de p�riph�riques
  mesPairedDevicesArrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1);
  mesNewDevicesArrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1);
  
  // Cr�ation de l'instance "ListView" des p�riph�riques appair�s
  ListView pairedListView = (ListView) findViewById(R.id.paired_devices);
  pairedListView.setAdapter(mesPairedDevicesArrayAdapter);         // Association du "ArrayAdapter"
  pairedListView.setOnItemClickListener(mDeviceClickListener);     // Association du "Listener"
  
  // Cr�ation de l'instance "ListView" des p�riph�riques d�couverts
  ListView newDevicesListView = (ListView) findViewById(R.id.new_devices);
  newDevicesListView.setAdapter(mesNewDevicesArrayAdapter);        // Association du "ArrayAdapter"
  newDevicesListView.setOnItemClickListener(mDeviceClickListener); // Association du "Listener"
  
  // Enregistrement du filtre "BroadcastReceiver" qd un p�riph�rique est d�couvert
  IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
  this.registerReceiver(mReceiver, filter);
  
  // Enregistrement du filtre "BroadcastReceiver" qd la d�couverte est termin�e
  filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
  this.registerReceiver(mReceiver, filter);
  
  // Affectation de la classe de l'adaptateur "Bluetooth"
  monBluetooth=BluetoothAdapter.getDefaultAdapter();
  
  // Lecture des p�riph�riques Bluetooth d�ja associ�s
  Set<BluetoothDevice> pairedDevices = monBluetooth.getBondedDevices();
  
  // Ajouter les noms et adresses des p�riph�riques Bluetooth associ�s ds le "ArrayAdapter"
  if (pairedDevices.size() > 0) 
  {// L'affichage est mis � jour de par l'association du "ArrayAdapter" avec "pairedListView"
   for (BluetoothDevice device : pairedDevices) 
   {
    mesPairedDevicesArrayAdapter.add(device.getName() + "\n" + device.getAddress());
   }
  } 
  else 
  {// Aucun p�riph�rique associ� : "ArrayAdapter" affect� avec "Aucun peripherique associ�"
   mesPairedDevicesArrayAdapter.add("Aucun peripherique associ�");
  }
 }

  /*
   * Clic sur le bouton "Scan Bluetooth"
   */
 @Override
 public void onClick(View v) 
 {
  doDiscovery(); // Changer le titre et d�marrer la d�couverte des p�riph�riques BT
 };
 
 // M�thode "on-click" appel�e par un clic sur un item de toutes les "ListView"
 public OnItemClickListener mDeviceClickListener = new OnItemClickListener() 
 {
  public void onItemClick(AdapterView<?> av, View v, int arg2, long arg3) 
  {
   // Arr�t de la d�couverte car cette fonction est gourmande en �nergie
   if (monBluetooth.isDiscovering()) monBluetooth.cancelDiscovery();
   // Lecture du nom et de l'adresse MAC dans les 17 derniers caract�res du TextView v
   String info = ((TextView) v).getText().toString();   // Nom et adresse MAC
   String address = info.substring(info.length() - 17); // Adresse MAC dans les 17 
                                                        // derniers caract�res du TextView v

   // Cr�ation de l'Intent de r�sultat et inclusion de l'adresse MAC
   Intent intent = new Intent();
   intent.putExtra(EXTRA_DEVICE_ADDRESS, address); // Avec l'identifiant utilis� ds 
                                                   // l'activit� principale ("onActivityResult")
   // Affectation du r�sultat (OK) et arr�t de l'activit�
   setResult(Activity.RESULT_OK, intent); // Utilis� ds "onActivityResult" de l'activit� principale
   finish();
  }
 };

 /**
  * D�marrer la d�couverte des p�riph�riques Bluetooth
  */
 private void doDiscovery() 
 {
  // Modifier le titre pour indiquer la phase "scanning"
  setTitle("Scan Bluetooth ...");
  // Stopper la d�couverte si elle est d�j� en cours 
  if (monBluetooth.isDiscovering()) monBluetooth.cancelDiscovery();
  // D�marrer la d�couverte des p�riph�riques Bluetooth
  monBluetooth.startDiscovery();
 }

 // M�thode "onReceive" du "BroadcastReceiver" 
 // Appel�e en cas de message correspondant aux filtres "BluetoothDevice.ACTION_FOUND"
 // ou "BluetoothAdapter.ACTION_DISCOVERY_FINISHED"
 private final BroadcastReceiver mReceiver = new BroadcastReceiver() 
 {
  @Override
  public void onReceive(Context context, Intent intent) 
  {
   String action = intent.getAction(); // Lecture du message re�u depuis l'adaptateur BT
   // Identification du message
   if (BluetoothDevice.ACTION_FOUND.equals(action)) 
   {// Quand un nouveau p�riph�rique est d�couvert :
    // Affectation du "BluetoothDevice" depuis l'Intent
    BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
    // Non pris en compte s'il est d�ja associ� car il est d�ja ds la liste
    if (device.getBondState() != BluetoothDevice.BOND_BONDED) 
    {
     mesNewDevicesArrayAdapter.add(device.getName() + "\n" + device.getAddress());
    }
   }
   // Nouveau titre � la fin de la d�couverte
   else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) 
   {
    setTitle("Sélectionner le péripherique");
    if (mesNewDevicesArrayAdapter.getCount() == 0) 
    {// Aucun p�riph�rique d�couvert
     mesNewDevicesArrayAdapter.add("Pas de péripherique trouvé");
    }
   }
  }
 };

 @Override
 protected void onDestroy() 
 {
  super.onDestroy();
  // Arr�t de la d�couverte
  if (monBluetooth!=null) monBluetooth.cancelDiscovery();
  // Elimination du "BroadcastReceiver"
  this.unregisterReceiver(mReceiver);
 }
}


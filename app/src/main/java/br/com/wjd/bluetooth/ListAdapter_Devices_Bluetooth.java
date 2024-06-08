package br.com.wjd.bluetooth;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

import br.com.wjd.R;

public class ListAdapter_Devices_Bluetooth extends ArrayAdapter<Device_Bluetooth> {

    Activity activity;
    int layoutResourceID;
    ArrayList<Device_Bluetooth> devices;

    private TextView tv_nome, tv_rssi, tv_mac;

    public ListAdapter_Devices_Bluetooth(Activity activity, int resource, ArrayList<Device_Bluetooth> objects) {
        super(activity.getApplicationContext(), resource, objects);

        this.activity = activity;
        layoutResourceID = resource;
        devices = objects;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) activity.getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(layoutResourceID, parent, false);
        }

        tv_nome = (TextView) convertView.findViewById(R.id.tv_name);
        tv_rssi = (TextView) convertView.findViewById(R.id.tv_rssi);
        tv_mac = (TextView) convertView.findViewById(R.id.tv_macaddr);

        Device_Bluetooth device = devices.get(position);
        String name = device.getName();
        String address = device.getAddress();

        //Configura o nome do dispositivo
        if (name != null && name.length() > 0)
            tv_nome.setText(device.getName());
        else
            tv_nome.setText("No Name");

        //Configura a potência do dispositivo
        tv_rssi.setText("RSSI: " + Integer.toString(device.getRSSI()));

        //Configura o MAC address do dispositivo
        if (address != null && address.length() > 0)
            tv_mac.setText(device.getAddress());
        else
            tv_mac.setText("No Address");

        return convertView;
    }
}

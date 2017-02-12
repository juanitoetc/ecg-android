package com.utn.juanignacio.utnecgv10;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.widget.ArrayAdapter;



/*
 * (cc) 2013 Daniel Garcia <contacto {at} danigarcia.org>
 *
 * This file is part of bluetooth.
 *
 * Flashlight is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * bluetooth is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with bluetooth.  If not, see <http://www.gnu.org/licenses/>.
 *
 */


import java.util.List;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class BluetoothDeviceArrayAdapter extends ArrayAdapter{

    private List<BluetoothDevice> deviceList;	// Contendra el listado de dispositivos
    private Context context;					// Contexto activo

    public BluetoothDeviceArrayAdapter(Context context, int textViewResourceId,
                                       List<BluetoothDevice> objects) {
        super(context, textViewResourceId, objects);
        this.deviceList = objects;
        this.context = context;
    }

    @Override
    public int getCount()
    {
        if(deviceList != null)
            return deviceList.size();
        else
            return -1;
    }

    @Override
    public Object getItem(int position)
    {
        return (deviceList == null ? null : deviceList.get(position));
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
        if((deviceList == null) || (context == null))
            return null;

        // Usamos un LayoutInflater para crear las vistas
        LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        // Creamos una vista a partir de simple_list_item_2, que contiene dos TextView.
        // El primero (text1) lo usaremos para el nombre, mientras que el segundo (text2)
        // lo utilizaremos para la direccion del dispositivo.
        View elemento = inflater.inflate(android.R.layout.simple_list_item_2, parent, false);

        // Referenciamos los TextView
        TextView txtNombre = (TextView)elemento.findViewById(android.R.id.text1);
        TextView txtDireccion = (TextView)elemento.findViewById(android.R.id.text2);

        // Obtenemos el dispositivo del array y obtenemos su nombre y direccion, asociandosela
        // a los dos TextView del elemento
        BluetoothDevice dispositivo = (BluetoothDevice)getItem(position);
        if(dispositivo != null)
        {
            txtNombre.setText(dispositivo.getName());
            txtDireccion.setText(dispositivo.getAddress());
        }
        else
        {
            txtNombre.setText("ERROR");
        }

        // Devolvemos el elemento con los dos TextView cumplimentados
        return elemento;
    }
}

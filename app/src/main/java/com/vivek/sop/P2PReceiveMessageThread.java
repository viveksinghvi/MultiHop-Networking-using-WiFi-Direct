package com.vivek.sop;

import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class P2PReceiveMessageThread implements Runnable{
	
	Context cntxt;
	
	Socket mysock;
	InputStream ins;
	
	String peerMAC,rMAC;
	
	byte[] buffer;
	int len;
	Date date;
    SimpleDateFormat dateFormat;
	
	public P2PReceiveMessageThread(Context c, Socket s, String m1, String m2){
		
		cntxt = c;
		mysock = s;
		
		try {
			ins = s.getInputStream();
		} catch (IOException e) {
			e.printStackTrace();
		}

		peerMAC = m1;
		rMAC = m2;
		dateFormat = new SimpleDateFormat("HH:mm:ss");
    	date = new Date();
	}

	public void run() {
		
		String[] msgarray = new String[P2PMessage.msgarraysize];
		try{
			
		for(int i=0;i<P2PMessage.msgarraysize;i++){
			
			if(P2PMessage.lenarray[i] == -1){ // -1 means we should use the previous value as the length
				if(i==0  || P2PMessage.lenarray[i-1] == -1){
					Log.w("rmessage","P2P Message len array not instantiated properly, exiting receive message task.");
					return;
				}
				
				len = Integer.parseInt(msgarray[i-1]);
				buffer = ByteBuffer.allocate(len).array();
				ins.read(buffer,0,len);
				msgarray[i] = new String(buffer);
				Log.w("rmessage", "Received string " + msgarray[i]);

				
			}else{ //this is a length that we should read in as an int
				
				len = P2PMessage.lenarray[i];
				buffer = ByteBuffer.allocate(len).array();
				ins.read(buffer,0,len);
				msgarray[i] = ByteBuffer.wrap(buffer).getInt() + "";
				Log.w("rmessage", "Received int " + msgarray[i]);
				
			}
		}
		
		}catch(Exception e){
			Log.w("rmessage",e.toString());
			e.printStackTrace();
		}
		
		Log.w("rmservice",dateFormat.format(new Date()) + " Service ended. ");
		
		Intent i = new Intent("MessageReceived");
		P2PMessage msg = new P2PMessage(msgarray);
		msg.setLastHop(peerMAC);
		i.putExtra("msg", msg);
		cntxt.sendBroadcast(i);
		
		Intent i2 = new Intent("RestartServer");
		i2.putExtra("rMAC", rMAC);
		cntxt.sendBroadcast(i2);
		
	}

}

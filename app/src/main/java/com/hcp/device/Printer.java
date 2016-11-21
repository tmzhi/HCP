package com.hcp.device;

public class Printer {
	
	public static void printOnBluetooth(String printData){
//		BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
//		Set<BluetoothDevice> devices = bluetoothAdapter.getBondedDevices();
//		BluetoothDevice device1 = null;
//		for(BluetoothDevice device : devices){
//			if(device.getName().startsWith("LPK")){
//				device1 = device;
//				break;
//			}
//		}
//		BluetoothSocket bluetoothSocket = null;
//		OutputStream outputStream = null;
//		
//		try {
//			bluetoothSocket = device1.createRfcommSocketToServiceRecord(UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"));
//	
//	        bluetoothSocket.connect();    
//	        outputStream = bluetoothSocket.getOutputStream();
//	        
//	        byte[] data = printData.toString().getBytes("gb2312");    
//	        outputStream.write(data, 0, data.length);    
//	        outputStream.flush();
//	        outputStream.close();
//	        bluetoothSocket.close();
//		} catch (Exception e) {
//			
//		} finally{
//			if(outputStream != null){
//				try {
//					outputStream.close();
//				} catch (IOException e) {
//					
//				}
//			}
//			
//			if(bluetoothSocket != null){
//				try {
//					bluetoothSocket.close();
//				} catch (IOException e) {
//					e.printStackTrace();
//				}
//			}
//		}
	}
}

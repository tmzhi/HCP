package com.hcp.device;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;

public class LPK130WifiPrinter {
	
	private Socket mSocket;
	
	private String address;
	private int port;
	
	public LPK130WifiPrinter(String address, int port){
		this.address = address;
		this.port = port;
	}
	
	public void print(byte[] content) throws IOException{
		
		OutputStream dataStream = null;
		try {
	        
			dataStream = mSocket.getOutputStream();
			
			dataStream.write(content);
	        dataStream.flush();
		} catch (IOException e) {
			throw e;
		}
	}
	

	public void printOneDimenBarcode(String content) throws IOException{

        OutputStream dataStream = mSocket.getOutputStream();
        
        byte[] bufferCreatePage = new byte[]{0x1c, 0x4c, 0x70, 0x40, 0x02, 0x60, 0, 0};
        byte[] bufferDrawPage = new byte[]{0x1c, 0x4c, 0x6f, 0, 0, 0, 0};
        
        byte[] bufferBarcodeCommand = new byte[]{0x1c, 0x4c, 0x62, 0, 0, 0, 0, 0, 2, 70, (byte)content.length()};
        byte[] bufferSendCommand = new byte[bufferBarcodeCommand.length + content.length()];
        
        byte[] bufferBarcodeTextCommand = new byte[]{0x1c, 0x4c, 0x74, 0, 0, (byte) (bufferBarcodeCommand[9] + 1), 0, 0, 0};
        byte[] bufferBarcodeTextSendCommand = new byte[bufferBarcodeTextCommand.length + content.length() + 1];
         
        for(int i=0, length=bufferBarcodeCommand.length; i<length; i++){
        	bufferSendCommand[i] = bufferBarcodeCommand[i];
        }
        
        for(int i=0, length=bufferBarcodeTextCommand.length; i<length; i++){
        	bufferBarcodeTextSendCommand[i] = bufferBarcodeTextCommand[i];
        }
        
        byte[] bufferBarcode = content.getBytes();
        for(int i=0, length=bufferBarcode.length; i<length; i++){
        	bufferSendCommand[i + bufferBarcodeCommand.length] = bufferBarcode[i];
        	bufferBarcodeTextSendCommand[i + bufferBarcodeTextCommand.length] = bufferBarcode[i];
        }
        
        bufferBarcodeTextSendCommand[bufferBarcodeTextSendCommand.length - 1] = 0;
        
        dataStream.write(bufferCreatePage);
        dataStream.write(bufferSendCommand);
        dataStream.write(bufferBarcodeTextSendCommand);
        dataStream.write(bufferDrawPage);
        dataStream.flush();
	}
	
	public void connect() throws IOException{
		if(mSocket == null || !mSocket.isConnected() || mSocket.isClosed()){
			mSocket = new Socket();
			mSocket.connect(new InetSocketAddress(this.address, this.port), 5000);
		}
	}
	
	public void close() throws IOException{
		if(mSocket != null && !mSocket.isClosed()){
			mSocket.close();
		}
	}
}

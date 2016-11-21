package com.hcp.http;

import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.MarshalBase64;
import org.ksoap2.serialization.PropertyInfo;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;

public class RequestUtil {

	public static final String RESPONSE_MSG_SUCCESS = "SUCCESS";
	public static final String RESPONSE_MSG_FAILED = "Failed";
	public static final String RESPONSE_MSG_ERROR_NOT_FOUND = "Data not found";
	public static final String RESPONSE_MSG_ERROR_AUTHORITY = "The security token could not be authenticated or authorized";



	/**
	 * webservice访问请求
	 *
	 * @param propertyInfos
	 *            请求内容
	 * @param methodName
	 *            方法名
	 * @return 请求返回内容
	 * @throws Exception
	 */
	public static String doRequest(String url, PropertyInfo[] propertyInfos,
								   String methodName) throws Exception {
		return doRequest(new HttpTransportSE(url, 10 * 1000), propertyInfos, methodName);
	}

	/**
	 * webservice访问请求
	 *
	 * @param propertyInfos
	 *            请求内容
	 * @param methodName
	 *            方法名
	 * @return 请求返回内容
	 * @throws Exception
	 */
	public static String doRequest(String url, PropertyInfo[] propertyInfos,
								   String methodName, int timeout) throws Exception {
		return doRequest(new HttpTransportSE(url, timeout), propertyInfos, methodName);
	}

	/**
	 * webservice访问请求
	 *
	 * @param htSe
	 *            请求工具
	 * @param propertyInfos
	 *            请求内容
	 * @param methodName
	 *            方法名
	 * @return 请求返回内容
	 * @throws Exception
	 */
	public static String doRequest(HttpTransportSE htSe,
								   PropertyInfo[] propertyInfos, String methodName) throws Exception {

		String requestResult = null;

		SoapSerializationEnvelope soapSerializationEnvelope;
		soapSerializationEnvelope = new SoapSerializationEnvelope(
				SoapEnvelope.VER11);
		SoapObject soapObject = new SoapObject("http://tempuri.org/",
				methodName);
		if (propertyInfos != null) {
			for (int i = 0; i < propertyInfos.length; i++) {
				soapObject.addProperty(propertyInfos[i]);
			}
		}
		MarshalBase64 md = new MarshalBase64();
		md.register(soapSerializationEnvelope);
		soapSerializationEnvelope.bodyOut = soapObject;
		soapSerializationEnvelope.dotNet = true;
		soapSerializationEnvelope.setOutputSoapObject(soapObject);
		try {
			htSe.call("http://tempuri.org/IHCPService/" + methodName,
					soapSerializationEnvelope);
			if (soapSerializationEnvelope.getResponse() != null) {
				SoapObject result = (SoapObject) soapSerializationEnvelope.bodyIn;
				if (result != null)
					requestResult = result.getProperty(0).toString();
			}
		} catch (Exception e) {
			throw e;
		}

		return requestResult;
	}
}

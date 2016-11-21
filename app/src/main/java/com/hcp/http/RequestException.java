package com.hcp.http;

public class RequestException extends Exception {

	/**
	 * Generated serial id;
	 */
	private static final long serialVersionUID = -3025863005492444419L;

	public RequestException(RequestErrorType error) {

	}

	public enum RequestErrorType {
		DATA_NOT_FOUND, REQUEST_FAILED;
	}
}

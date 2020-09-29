
package com.sun.squawk.runtime.loader;

public interface InputContext {
    public VerificationException verificationException() throws VerificationException;
    public VerificationException verificationException(int code) throws VerificationException;
    public VerificationException verificationException(String msg) throws VerificationException;
}

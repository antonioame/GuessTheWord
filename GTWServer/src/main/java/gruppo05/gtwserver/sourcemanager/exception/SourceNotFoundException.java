/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gruppo05.gtwserver.sourcemanager.exception;

/**
 *
 * @author Hermann
 */
public class SourceNotFoundException extends RuntimeException {

    /**
     * Creates a new instance of <code>SourceNotFoundException</code> without
     * detail message.
     */
    public SourceNotFoundException() {
    }

    /**
     * Constructs an instance of <code>SourceNotFoundException</code> with the
     * specified detail message.
     *
     * @param msg the detail message.
     */
    public SourceNotFoundException(String msg) {
        super(msg);
    }
}

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
public class StopWordsLoadException extends RuntimeException {

    /**
     * Creates a new instance of <code>StopWordsLoadException</code> without
     * detail message.
     */
    public StopWordsLoadException() {
    }

    /**
     * Constructs an instance of <code>StopWordsLoadException</code> with the
     * specified detail message.
     *
     * @param msg the detail message.
     */
    public StopWordsLoadException(String msg) {
        super(msg);
    }
}

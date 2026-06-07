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
public class FrequencyMapNotFoundException extends RuntimeException {

    /**
     * Creates a new instance of <code>FrequencyMapNotFoundException</code>
     * without detail message.
     */
    public FrequencyMapNotFoundException() {
    }

    /**
     * Constructs an instance of <code>FrequencyMapNotFoundException</code> with
     * the specified detail message.
     *
     * @param msg the detail message.
     */
    public FrequencyMapNotFoundException(String msg) {
        super(msg);
    }
}

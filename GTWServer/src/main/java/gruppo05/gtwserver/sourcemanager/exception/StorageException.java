/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gruppo05.gtwserver.sourcemanager.exception;

public class StorageException extends RuntimeException {

    /**
     * Creates a new instance of <code>StorageException</code> without detail
     * message.
     */
    public StorageException() {
    }

    /**
     * Constructs an instance of <code>StorageException</code> with the
     * specified detail message.
     *
     * @param msg the detail message.
     */
    public StorageException(String msg) {
        super(msg);
    }
}

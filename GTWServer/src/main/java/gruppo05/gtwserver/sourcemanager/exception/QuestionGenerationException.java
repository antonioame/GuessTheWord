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
public class QuestionGenerationException extends RuntimeException {

    /**
     * Creates a new instance of <code>QuestionGenerationException</code>
     * without detail message.
     */
    public QuestionGenerationException() {
    }

    /**
     * Constructs an instance of <code>QuestionGenerationException</code> with
     * the specified detail message.
     *
     * @param msg the detail message.
     */
    public QuestionGenerationException(String msg) {
        super(msg);
    }
}

package gruppo05.gtwserver.model;

import java.util.Objects;

/**
 * @brief Rappresenta una domanda generata a partire da una fonte.
 * @invariant
 * Il testo della domanda non è nullo.
 * @invariant
 * La risposta alla domanda non è nulla.
 */
public class Question {

    /**
     * @brief Il testo della domanda.
     */
    private final String text;

    /**
     * @brief La risposta corretta alla domanda.
     */
    private final String answer;

    /**
     * @brief Crea una nuova domanda con il testo e la risposta specificati.
     * @param[in] text   Il testo della domanda.
     * @param[in] answer La risposta associata alla domanda.
     * @pre
     * Il parametro text non è nullo.
     * @pre
     * Il parametro answer non è nullo.
     * @post
     * L'istanza è stata correttamente creata e i valori sono stati assegnati.
     */
    public Question(String text, String answer) {
        Objects.requireNonNull(text, "Il testo della domanda non puo' essere nullo");
        Objects.requireNonNull(answer, "La risposta alla domanda non puo' essere nulla");
        this.text = text;
        this.answer = answer;
    }

    /**
     * @brief Restituisce il testo della domanda.
     * @return Il testo della domanda corrente.
     */
    public String getText() {
        return text;
    }

    /**
     * @brief Restituisce la risposta alla domanda.
     * @return La stringa che rappresenta la risposta corretta.
     */
    public String getAnswer() {
        return answer;
    }
}
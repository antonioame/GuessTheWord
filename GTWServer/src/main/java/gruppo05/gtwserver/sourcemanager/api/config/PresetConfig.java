/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gruppo05.gtwserver.sourcemanager.api.config;

/**
 *
 */

/**
 * @brief Configurazione dei parametri per la generazione delle domande.
 * @invariant
 * Il numero di periodi deve essere maggiore di zero.
 * @invariant
 * La massima frequenza delle parole deve essere maggiore di zero.
 * @invariant
 * Il numero massimo di parole simili nel testo della domanda deve essere maggiore o uguale a zero.
 * @invariant
 * L'offset di shifting deve essere maggiore o uguale a zero.
 */
public class PresetConfig {

	/**
	 * @brief Numero di periodi da estrarre dalla sorgente.
	 */
	private final int numberOfPeriods;

	/**
	 * @brief Massima frequenza consentita per una parola affinché venga estratta.
	 */
	private final int maximumWordFrequency;

	/**
	 * @brief Numero massimo di parole simili consentite nel testo della domanda.
	 */
	private final int maximumSimilarWordInQuestionText;

	/**
	 * @brief Offset di shifting utilizzato per la cifratura del testo della domanda.
	 */
	private final int shiftingOffset;

	/**
	 * @brief Costruttore privato che inizializza la configurazione tramite il Builder.
	 * @param[in] builder Il builder contenente i parametri di configurazione.
	 * @pre
	 * Il parametro builder non deve essere nullo.
	 * @post
	 * L'oggetto PresetConfig viene istanziato con i valori validati dal builder.
	 */
	private PresetConfig(Builder builder) {
		this.numberOfPeriods = builder.numberOfPeriods;
		this.maximumWordFrequency = builder.maximumWordFrequency;
		this.maximumSimilarWordInQuestionText = builder.maximumSimilarWordInQuestionText;
		this.shiftingOffset = builder.shiftingOffset;
	}

	/**
	 * @brief Restituisce il numero di periodi.
	 * @return Il numero di periodi.
	 */
	public int getNumberOfPeriods() {
		return numberOfPeriods;
	}

	/**
	 * @brief Restituisce la massima frequenza delle parole consentita.
	 * @return La massima frequenza delle parole.
	 */
	public int getMaximumWordFrequency() {
		return maximumWordFrequency;
	}

	/**
	 * @brief Restituisce il numero massimo di parole simili nel testo della domanda.
	 * @return Il numero massimo di parole simili.
	 */
	public int getMaximumSimilarWordInQuestionText() {
		return maximumSimilarWordInQuestionText;
	}

	/**
	 * @brief Restituisce l'offset di shifting.
	 * @return L'offset di shifting.
	 */
	public int getShiftingOffset() {
		return shiftingOffset;
	}

	/**
	 * @brief Builder per la creazione guidata e validata di istanze di PresetConfig.
	 * @invariant
	 * I valori impostati devono rispettare i vincoli di integrità prima della chiamata a build().
	 */
	public static class Builder {
		private int numberOfPeriods = 1;
		private int maximumWordFrequency = 100;
		private int maximumSimilarWordInQuestionText = 5;
		private int shiftingOffset = 3;

		/**
		 * @brief Costruttore del Builder con valori di default validi.
		 * @post
		 * Il Builder viene inizializzato con valori predefiniti coerenti con le invarianti.
		 */
		public Builder() {}

		/**
		 * @brief Imposta il numero di periodi.
		 * @param[in] periods Il numero di periodi da configurare.
		 * @return Il builder stesso per consentire il chaining dei metodi.
		 * @pre
		 * Il valore di periods deve essere maggiore di zero.
		 * @post
		 * Il campo numberOfPeriods viene aggiornato con il valore fornito.
		 */
		public Builder withNumberOfPeriods(int periods) {
			if (periods <= 0) {
				throw new IllegalArgumentException("numberOfPeriods deve essere > 0");
			}
			this.numberOfPeriods = periods;
			return this;
		}

		/**
		 * @brief Imposta la massima frequenza delle parole.
		 * @param[in] frequency La massima frequenza consentita.
		 * @return Il builder stesso per consentire il chaining dei metodi.
		 * @pre
		 * Il valore di frequency deve essere maggiore di zero.
		 * @post
		 * Il campo maximumWordFrequency viene aggiornato con il valore fornito.
		 */
		public Builder withMaximumWordFrequency(int frequency) {
			if (frequency <= 0) {
				throw new IllegalArgumentException("maximumWordFrequency deve essere > 0");
			}
			this.maximumWordFrequency = frequency;
			return this;
		}

		/**
		 * @brief Imposta il numero massimo di parole simili nel testo della domanda.
		 * @param[in] similarWords Il numero massimo di parole simili.
		 * @return Il builder stesso per consentire il chaining dei metodi.
		 * @pre
		 * Il valore di similarWords deve essere maggiore o uguale a zero.
		 * @post
		 * Il campo maximumSimilarWordInQuestionText viene aggiornato con il valore fornito.
		 */
		public Builder withMaximumSimilarWordInQuestionText(int similarWords) {
			if (similarWords < 0) {
				throw new IllegalArgumentException("maximumSimilarWordInQuestionText deve essere >= 0");
			}
			this.maximumSimilarWordInQuestionText = similarWords;
			return this;
		}

		/**
		 * @brief Imposta l'offset di shifting per la cifratura.
		 * @param[in] offset L'offset di shifting.
		 * @return Il builder stesso per consentire il chaining dei metodi.
		 * @pre
		 * Il valore di offset deve essere maggiore o uguale a zero.
		 * @post
		 * Il campo shiftingOffset viene aggiornato con il valore fornito.
		 */
		public Builder withShiftingOffset(int offset) {
			if (offset < 0) {
				throw new IllegalArgumentException("shiftingOffset deve essere >= 0");
			}
			this.shiftingOffset = offset;
			return this;
		}

		/**
		 * @brief Costruisce un'istanza immutabile di PresetConfig.
		 * @return Un oggetto PresetConfig propriamente configurato.
		 * @pre
		 * Lo stato corrente del builder deve soddisfare tutte le invarianti della classe PresetConfig.
		 * @post
		 * Viene restituita una nuova istanza di PresetConfig immutabile.
		 */
		public PresetConfig build() {
			return new PresetConfig(this);
		}
	}
}
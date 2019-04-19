package com.ieee19.bc.interop.pf.core;

import com.ieee19.bc.interop.pf.core.exception.DataReadingFailedException;
import com.ieee19.bc.interop.pf.core.exception.DataWritingFailedException;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.function.BiFunction;


/**
 * This class provides an interface for accessing (reading and writing) data on a blockchain and is used by
 * the {@link BlockchainManager}. For each supported blockchain, an implementation has to be provided.
 */
public abstract class AbstractDataAccessService {

    private BiFunction<String, Integer, List<String>> singleDataStringFormatter;
    private BiFunction<List<String>, Integer, List<String>> dataStringListFormatter;

    /**
     *
     * @param singleDataStringFormatter a function with two args (<i>str</i> and <i>maxLength</i>) that splits a string
     *                                  into multiple substrings such that each substring's length is lower than
     *                                  or equal to <i>maxLength</i>.
     * @param dataStringListFormatter   a function with two args (<i>strList</i> and <i>maxLength</i>) that splits or
     *                                  merges the strings into one or multiple strings such that each of the string
     *                                  has a length lower than or equal to <i>maxLength</i>.
     */
    public AbstractDataAccessService(BiFunction<String, Integer, List<String>> singleDataStringFormatter,
                                     BiFunction<List<String>, Integer, List<String>> dataStringListFormatter) {
        this.singleDataStringFormatter = singleDataStringFormatter;
        this.dataStringListFormatter = dataStringListFormatter;
    }

    /**
     * Returns data entries that have been written to the blockchain between <i>from</i> and <i>to</i>.
     * @param from start date
     * @param to end date
     * @return a list of data strings (e.g. one string for every transaction data)
     * @throws DataReadingFailedException
     */
    public abstract List<String> getData(ZonedDateTime from, ZonedDateTime to) throws DataReadingFailedException;

    /**
     * Writes a string to the blockchain.
     * @param dataStr the string to store
     * @throws DataWritingFailedException
     */
    public abstract void writeData(String dataStr) throws DataWritingFailedException;

    /**
     * Writes a string to the blockchain.
     * @param dataStr the string to store
     * @throws DataWritingFailedException
     */
    public abstract void writeData(List<String> dataStrings) throws DataWritingFailedException;

    /**
     * @return a formatter that splits a given string into multiple strings with a maximum length specified
     * in the second argument of the formatter function. The formatter is used if an input string's length exceeds
     * the maximum size of the data a blockchain transaction can store.
     */
    public BiFunction<String, Integer, List<String>> getSingleDataStringFormatter() {
        return singleDataStringFormatter;
    }

    /**
     * @return a formatter that splits each given string into multiple strings with a maximum length specified
     * in the second argument of the formatter function.
     */
    public BiFunction<List<String>, Integer, List<String>> getDataStringListFormatter() {
        return dataStringListFormatter;
    }

}

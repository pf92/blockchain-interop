package com.ieee19.bc.interop.pf.proxy.ethereum.interfaces;

import com.ieee19.bc.interop.pf.core.exception.DataWritingFailedException;
import com.ieee19.bc.interop.pf.core.model.Block;
import com.ieee19.bc.interop.pf.proxy.ethereum.exception.EthereumException;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

public interface IEthereumService {

    /**
     * @return the current block height (block number)
     */
    long getCurrentBlockNumber() throws EthereumException;

    /**
     * @param blockNumber the block number of the desired block
     * @param numberOfRetries
     * @return the block with the given block number and its uncle blocks
     * @throws EthereumException
     */
    Optional<Block> getBlockByNumberWithUncles(long blockNumber) throws EthereumException;

    /**
     * @return the median gas price in wei.
     * @throws EthereumException
     */
    Long getGasPrice() throws EthereumException;

    /**
     * Returns all data that have been mined during <i>from</i> and <i>to</i>.
     * @param from start date
     * @param to end date
     * @return the data
     */
    List<String> getData(ZonedDateTime from, ZonedDateTime to) throws EthereumException;

    /**
     * Writes data to the Ethereum blockchain
     * @param dataStr the data to write
     * @param gasPriceInWei gas price in wei
     * @throws DataWritingFailedException
     */
    void storeData(String dataStr, long gasPriceInWei) throws EthereumException;

    /**
     * @return max. length of the data field in an Ethereum transaction in bytes
     * @throws EthereumException
     */
    int getMaxTransactionDataSizeInBytes() throws EthereumException;

}

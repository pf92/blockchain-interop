package com.ieee19.bc.interop.pf.proxy.bitcoin.interfaces;

import com.ieee19.bc.interop.pf.core.model.Block;
import com.ieee19.bc.interop.pf.core.exception.DataWritingFailedException;
import com.ieee19.bc.interop.pf.proxy.bitcoin.dto.blockcypher.FeePerKbInfo;
import com.ieee19.bc.interop.pf.proxy.bitcoin.exception.BitcoinException;

import java.io.IOException;
import java.time.ZonedDateTime;
import java.util.List;

public interface IBitcoinService {

    /**
     * @return the current block height (block number)
     */
    long getCurrentBlockHeight() throws BitcoinException;

    /**
     * @param blockNumber the number of the block to fetch
     * @return the block with the given <i>blockNumber</i>
     * @throws IOException
     */
    Block getBlockByBlockNumber(long blockNumber) throws BitcoinException;

    /**
     * @return transaction fee info per KB.
     */
    FeePerKbInfo getFeePerKbInfo() throws BitcoinException;

    /**
     * Returns all data that have been mined during <i>from</i> and <i>to</i>.
     * @param from start date
     * @param to end date
     * @return the data
     */
    List<String> getData(ZonedDateTime from, ZonedDateTime to) throws BitcoinException;

    /**
     * Writes data to the Bitcoin blockchain
     * @param dataString the data to write
     * @param txFeesInSatoshi transaction fees in satoshis
     * @throws DataWritingFailedException
     */
    void storeData(String dataString, long txFeesInSatoshi) throws BitcoinException;

}

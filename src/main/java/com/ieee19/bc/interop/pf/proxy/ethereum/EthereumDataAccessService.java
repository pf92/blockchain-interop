package com.ieee19.bc.interop.pf.proxy.ethereum;

import com.ieee19.bc.interop.pf.core.AbstractDataAccessService;
import com.ieee19.bc.interop.pf.core.exception.DataReadingFailedException;
import com.ieee19.bc.interop.pf.core.exception.DataWritingFailedException;
import com.ieee19.bc.interop.pf.proxy.ethereum.exception.EthereumException;
import com.ieee19.bc.interop.pf.proxy.ethereum.interfaces.IEthereumService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.BiFunction;

/**
 * This class provides methods for accessing blockchain data (read and write operations).
 */
public class EthereumDataAccessService extends AbstractDataAccessService {

    private static final Logger LOG = LoggerFactory.getLogger(EthereumDataAccessService.class);
    private static long NO_GASPRICE_SET_INDICATOR = -1;
    private AtomicLong gasPrice = new AtomicLong(NO_GASPRICE_SET_INDICATOR);  // in wei; if null get gas price from EthereumService
    private IEthereumService ethereumService;

    public EthereumDataAccessService(BiFunction<String, Integer, List<String>> singleDataStringFormatter,
                                     BiFunction<List<String>, Integer, List<String>> dataStringListFormatter, IEthereumService ethereumService) {
        super(singleDataStringFormatter, dataStringListFormatter);
        this.ethereumService = ethereumService;
    }

    public EthereumDataAccessService(BiFunction<String, Integer, List<String>> singleDataStringFormatter,
                                     BiFunction<List<String>, Integer, List<String>> dataStringListFormatter,
                                     IEthereumService ethereumService, long gasPrice) {
        super(singleDataStringFormatter, dataStringListFormatter);
        this.ethereumService = ethereumService;
        this.gasPrice.set(gasPrice);
    }

    public long getGasPriceInWei() throws EthereumException {
        if (gasPrice.get() != NO_GASPRICE_SET_INDICATOR) {
            return gasPrice.get();
        }
        return ethereumService.getGasPrice();
    }

    public void setGasPriceInWei(long gasPriceInWei) {
        gasPrice.set(gasPriceInWei);
    }

    @Override
    public List<String> getData(ZonedDateTime from, ZonedDateTime to) throws DataReadingFailedException {
        try {
            return ethereumService.getData(from, to);
        } catch (EthereumException e) {
            throw new DataReadingFailedException(e.getMessage(), e);
        }
    }

    @Override
    public void writeData(String dataStr) throws DataWritingFailedException {
        try {
            List<String> strPerTx = getSingleDataStringFormatter().apply(dataStr, ethereumService.getMaxTransactionDataSizeInBytes());
            for (String str : strPerTx) {
                ethereumService.storeData(str, getGasPriceInWei());
            }
        } catch (EthereumException e) {
            throw new DataWritingFailedException(e.getMessage(), e);
        }
    }

    @Override
    public void writeData(List<String> dataStrings) throws DataWritingFailedException {
        try {
            List<String> strPerTx = getDataStringListFormatter().apply(dataStrings, ethereumService.getMaxTransactionDataSizeInBytes());
            for (String str : strPerTx) {
                ethereumService.storeData(str, getGasPriceInWei());
            }
        } catch (EthereumException e) {
            throw new DataWritingFailedException(e.getMessage(), e);
        }
    }

}

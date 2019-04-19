package com.ieee19.bc.interop.pf.proxy.bitcoin;

import com.ieee19.bc.interop.pf.core.AbstractDataAccessService;
import com.ieee19.bc.interop.pf.core.exception.DataReadingFailedException;
import com.ieee19.bc.interop.pf.core.exception.DataWritingFailedException;
import com.ieee19.bc.interop.pf.proxy.bitcoin.exception.BitcoinException;
import com.ieee19.bc.interop.pf.proxy.bitcoin.interfaces.IBitcoinService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.BiFunction;

/**
 * Implements the {@link AbstractDataAccessService} class.
 *
 */
public class BitcoinDataAccessService extends AbstractDataAccessService {

    private static final Logger LOG = LoggerFactory.getLogger(BitcoinDataAccessService.class);
    private static long NO_FEES_SET_INDICATOR = -1;

    private AtomicLong txFeesInSatoshisPerKb = new AtomicLong(NO_FEES_SET_INDICATOR);  // in satoshis; if null get fees from BitcoinService
    private IBitcoinService bitcoinService;

    public BitcoinDataAccessService(BiFunction<String, Integer, List<String>> singleDataStringFormatter,
                                    BiFunction<List<String>, Integer, List<String>> dataStringListFormatter, IBitcoinService bitcoinService) {
        super(singleDataStringFormatter, dataStringListFormatter);
        this.bitcoinService = bitcoinService;
    }

    public BitcoinDataAccessService(BiFunction<String, Integer, List<String>> singleDataStringFormatter,
                                    BiFunction<List<String>, Integer, List<String>> dataStringListFormatter, IBitcoinService bitcoinService,
                                    Long txFeesInSatoshisPerKb) {
        super(singleDataStringFormatter, dataStringListFormatter);
        this.bitcoinService = bitcoinService;
        this.txFeesInSatoshisPerKb.set(txFeesInSatoshisPerKb);
    }

    public long getFeesInSatoshis() throws BitcoinException {
        if (txFeesInSatoshisPerKb.get() != NO_FEES_SET_INDICATOR) {
            return txFeesInSatoshisPerKb.get();
        }
        return bitcoinService.getFeePerKbInfo().getMediumFeePerKb();
    }

    public void setFeesInSatoshis(long feesInSatoshis) {
        txFeesInSatoshisPerKb.set(feesInSatoshis);
    }

    @Override
    public List<String> getData(ZonedDateTime from, ZonedDateTime to) throws DataReadingFailedException {
        try {
            return bitcoinService.getData(from, to);
        } catch (BitcoinException e) {
            throw new DataReadingFailedException(e.getMessage(), e);
        }
    }

    @Override
    public void writeData(String dataString) throws DataWritingFailedException {
        List<String> dataStrings = getSingleDataStringFormatter().apply(dataString, Constants.MAX_TX_DATA_SIZE_IN_BYTES);
        try {
            long fees = getFeesInSatoshis();

            for (String str : dataStrings) {

                bitcoinService.storeData(str, fees);

            }
        } catch (BitcoinException e) {
            throw new DataWritingFailedException(e.getMessage(), e);
        }
    }

    @Override
    public void writeData(List<String> dataStrings) throws DataWritingFailedException {
        List<String> strings = getDataStringListFormatter().apply(dataStrings, Constants.MAX_TX_DATA_SIZE_IN_BYTES);

        for (String str : strings) {
            try {
                bitcoinService.storeData(str, getFeesInSatoshis());
            } catch (BitcoinException e) {
                throw new DataWritingFailedException(e.getMessage(), e);
            }
        }
    }

}

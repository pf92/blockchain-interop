package com.ieee19.bc.interop.pf.core.model;

import com.ieee19.bc.interop.pf.core.AbstractDataAccessService;
import com.ieee19.bc.interop.pf.core.IMetricCollector;

import java.util.Objects;

/**
 * This class represents a blockchain and contains some metadata such as an identifier, a {@link IMetricCollector} and an
 * {@link AbstractDataAccessService}.
 */
public class BlockchainMetaData {

    private String identifier;
    private IMetricCollector metricCollector;
    private AbstractDataAccessService dataAccessService;
    private int reputation = 0;
    private int numberOfRequiredConfirmations = 0;

    public BlockchainMetaData() {
    }

    public BlockchainMetaData(String identifier, IMetricCollector metricCollector,
                              AbstractDataAccessService dataAccessService, int numberOfRequiredConfirmations,
                              int reputation) {
        this.identifier = identifier;
        this.metricCollector = metricCollector;
        this.dataAccessService = dataAccessService;
        this.numberOfRequiredConfirmations = numberOfRequiredConfirmations;
        this.reputation = reputation;
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public IMetricCollector getMetricCollector() {
        return metricCollector;
    }

    public void setMetricCollector(IMetricCollector metricCollector) {
        this.metricCollector = metricCollector;
    }

    public AbstractDataAccessService getDataAccessService() {
        return dataAccessService;
    }

    public void setDataAccessService(AbstractDataAccessService dataAccessService) {
        this.dataAccessService = dataAccessService;
    }

    /**
     * @param reputation indicates trust in a blockchain. 0 <= <i>reputation</i> <= 10
     */
    public void setReputation(int reputation) {
        this.reputation = reputation;
    }

    public int getReputation() {
        return reputation;
    }

    public int getNumberOfRequiredConfirmations() {
        return numberOfRequiredConfirmations;
    }

    /**
     * @param numberOfRequiredConfirmations specifies the number of blocks that should confirm a
     *                                      newly inserted block in the blockchain and must be >= 0
     */
    public void setNumberOfRequiredConfirmations(int numberOfRequiredConfirmations) {
        this.numberOfRequiredConfirmations = numberOfRequiredConfirmations;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof BlockchainMetaData)) return false;
        BlockchainMetaData that = (BlockchainMetaData) o;
        return Objects.equals(identifier, that.identifier);
    }

    @Override
    public int hashCode() {
        return Objects.hash(identifier);
    }

    @Override
    public String toString() {
        return "BlockchainMetaData{" +
                "identifier='" + identifier + '\'' +
                '}';
    }

}

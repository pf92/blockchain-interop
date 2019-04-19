package com.ieee19.bc.interop.pf.proxy.ethereum;

import com.ieee19.bc.interop.pf.core.model.Block;
import com.ieee19.bc.interop.pf.proxy.ethereum.dto.rpc.JsonRpcTransaction;
import com.ieee19.bc.interop.pf.proxy.ethereum.model.Transaction;
import org.web3j.protocol.core.methods.response.EthBlock;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

public class DtoConverter {

    public static Block convert(EthBlock.Block ethBlock) {
        Block block = new Block();
        Instant instant = Instant.ofEpochSecond(ethBlock.getTimestamp().longValue());
        ZonedDateTime timestamp = ZonedDateTime.ofInstant(instant, ZoneOffset.UTC);

        block.setHash(ethBlock.getHash());
        block.setHeight(ethBlock.getNumber().longValue());
        block.setPreviousBlockHash(ethBlock.getParentHash());
        block.setTimestamp(timestamp);

        if (ethBlock.getTransactions() != null) {
            block.setNumberOfTransactions(ethBlock.getTransactions().size());
        }

        block.setMinerAddress(ethBlock.getMiner());
        block.setDifficulty(ethBlock.getDifficulty().longValue());

        return block;
    }

    public static Transaction convert(org.web3j.protocol.core.methods.response.Transaction tx) {
        Transaction transaction = new Transaction();
        transaction.setHash(tx.getHash());
        transaction.setFromAddress(tx.getFrom());
        transaction.setToAddress(tx.getTo());
        transaction.setData(tx.getInput());
        return transaction;
    }

    public static Transaction convert(JsonRpcTransaction jsonRpcTransaction) {
        Transaction transaction = new Transaction();
        transaction.setHash(jsonRpcTransaction.getHash());
        transaction.setFromAddress(jsonRpcTransaction.getFromAddress());
        transaction.setToAddress(jsonRpcTransaction.getToAddress());
        transaction.setData(jsonRpcTransaction.getData());
        return transaction;
    }

}

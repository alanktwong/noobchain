package com.pb.noobchain.service.impl

import com.pb.noobchain.domain.Block
import com.pb.noobchain.domain.Transaction
import com.pb.noobchain.domain.Wallet
import com.pb.noobchain.repository.TransactionRepository
import com.pb.noobchain.repository.impl.TransactionRepositoryImpl
import com.pb.noobchain.service.HashUtil
import com.pb.noobchain.service.BlockChainService
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import spock.lang.Specification

class TransactionServiceImplSpec extends Specification {

    private static final Logger LOG = LoggerFactory.getLogger(TransactionServiceImplSpec)

    TransactionServiceImpl service

    TransactionRepository repo

    BlockChainService blockChainService

    def setup() {
        repo = new TransactionRepositoryImpl()
        blockChainService = new BlockChainServiceImpl()

        service = new TransactionServiceImpl(repo)
        service.minimumTransaction = 0.01
    }

    def "should create a verified transaction"() {

        given: "Create 2 new wallets"
          Wallet walletA = new Wallet("A")
          LOG.info("Keys - Private {} and public {}",
              HashUtil.getStringFromKey(walletA.getPrivateKey()),
              HashUtil.getStringFromKey(walletA.getPublicKey()))
          Wallet walletB = new Wallet("B")
          LOG.info("Keys - Private {} and public {}",
              HashUtil.getStringFromKey(walletB.getPrivateKey()),
              HashUtil.getStringFromKey(walletB.getPublicKey()))

        and: "Create a test transaction from walletA to walletB"
          def inputs = null
          Transaction transaction = new Transaction(walletA.getPublicKey(), walletB.getPublicKey(),
              5, inputs)
          transaction.generateSignature(walletA.getPrivateKey())

        when: "Verify the signature works and verify it from the public key"
          def verified = transaction.verifySignature()

        then:
          verified
    }

    def "should NOT process a transaction w/o inputs"() {

        given: "Create 2 new wallets"
          Wallet walletA = new Wallet("A")
          Wallet walletB = new Wallet("B")

        and: "Create a test transaction from walletA to walletB"
          def inputs = []
          Transaction transaction = new Transaction(walletA.getPublicKey(), walletB.getPublicKey(),
              5, inputs)
          transaction.generateSignature(walletA.getPrivateKey())

        when: "Try to process the transaction"
          def verified = service.processTransaction(transaction)

        then:
          !verified
    }

    def "should send try to funds from walletA which does not have enough funds"() {

        given: "Create 2 new wallets"
          Wallet walletA = new Wallet("A")
          Wallet walletB = new Wallet("B")

        when: "Verify the transaction has been processed"
          def txn = service.sendFundsFromWallet(walletA, walletB.getPublicKey(), 10.0)

        then:
          txn == null
    }

    def "should try to add null transaction to a block"() {

        given:
          def chain = blockChainService.myFirstChain()
          Block lastBlock = chain.last()
          Transaction txn = null

        when: "Verify the transaction has been processed"
          def success = service.addTransactionToBlock(txn, lastBlock)

        then:
          !success
    }
}

package com.pb.noobchain.service.impl

import com.pb.noobchain.domain.Block
import com.pb.noobchain.domain.Transaction
import com.pb.noobchain.domain.Wallet
import com.pb.noobchain.repository.TransactionRepository
import com.pb.noobchain.repository.impl.TransactionRepositoryImpl
import com.pb.noobchain.service.BlockChainTestFactory
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
        blockChainService.setDifficulty(3)

        service = new TransactionServiceImpl(repo)
        service.minimumTransaction = -0.01
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

    def "should process a transaction w/empty inputs"() {
        given: "Create 2 new wallets"
          Wallet walletA = new Wallet("A")
          Wallet walletB = new Wallet("B")

        and: "Create a test transaction from walletA to walletB"
          def inputs = []
          Transaction transaction = new Transaction(walletA.getPublicKey(), walletB.getPublicKey(),
              5.0, inputs)
          transaction.generateSignature(walletA.getPrivateKey())

        when: "Try to process the transaction"
          def verified = service.processTransaction(transaction)

        then:
          verified
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
          Wallet coinBase = new Wallet("coinBase")
          Wallet walletA = new Wallet("A")

        and:
          def blockChain = []
          def factory = new BlockChainTestFactory(transactionService: service,
              blockChainService: blockChainService,
              transactionRepository: repo)
          def genesisTxn = factory.createGenesisTxn(coinBase, walletA, 100f)
          def genesisBlock = factory.createGenesisBlock(genesisTxn, blockChain)
          Transaction txn = null

        when: "Verify the transaction has been processed"
          def success = service.addTransactionToBlock(txn, genesisBlock)

        then:
          !success
    }

    def "should create a valid blockChain of a transaction history"() {
        given:
          def factory = new BlockChainTestFactory(transactionService: service,
              blockChainService: blockChainService,
              transactionRepository: repo)
          List<Block> blockChain = factory.create()

        when:
          def valid = blockChainService.validateChain(blockChain)

        then:
          valid
    }
}

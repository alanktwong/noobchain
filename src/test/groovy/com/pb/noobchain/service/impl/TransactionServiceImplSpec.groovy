package com.pb.noobchain.service.impl

import com.pb.noobchain.domain.Block
import com.pb.noobchain.domain.Transaction
import com.pb.noobchain.domain.TransactionOutput
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
          def chain = blockChainService.myFirstChain()
          Block lastBlock = chain.last()
          Transaction txn = null

        when: "Verify the transaction has been processed"
          def success = service.addTransactionToBlock(txn, lastBlock)

        then:
          !success
    }

    def "should create a valid blockChain of a transaction history"() {
        given:
          List<Block> blockChain = []

        and: "Create wallets"
          Wallet coinBase = new Wallet("coinBase")
          Wallet walletA = new Wallet("A")
          Wallet walletB = new Wallet("B")

        and:
          LOG.info("Create genesis transaction, which sends 100 NoobCoin to wallet A")
          Transaction genesisTransaction = createGenesis(coinBase, walletA, 100f)

        and:
          LOG.info("Creating and mining Genesis block... ")
          Block genesis = new Block("genesis", HashUtil.PREVIOUS_HASH_OF_GENESIS)
          service.addTransactionToBlock(genesisTransaction, genesis)
          blockChainService.mineBlockAndAddToChain(genesis, blockChain)

        and:
          LOG.info("Wallet A is trying to send funds (40) to Wallet B ...")
          Block block1 = new Block("1", genesis.getHash())
          tryToSend(walletA, walletB, block1, blockChain, 40f)

        and:
          LOG.info("Wallet A is trying to send more funds (1000) than it has to Wallet B ...")
          def block2 = new Block("2", block1.getHash())
          tryToSend(walletA, walletB, block2, blockChain, 1000f)

        and:
          LOG.info("Wallet B is trying to send more funds (1000) than it has to Wallet A ...")
          def block3 = new Block("3", block2.getHash())
          tryToSend(walletB, walletA, block3, blockChain, 20f)

        when:
          def valid = blockChainService.validateChain(blockChain)

        then:
          valid
    }

    def tryToSend(Wallet sender, Wallet recipient, Block block, List<Block> blockChain, float amount) {
        LOG.info("\nSender [{}] initial balance: {}", sender.getId(), service.getBalance(sender))
        def txn = service.sendFundsFromWallet(sender, recipient.getPublicKey(), amount)
        service.addTransactionToBlock(txn, block)
        blockChainService.mineBlockAndAddToChain(block, blockChain)
        LOG.info("Sender [{}] final balance: {}", sender.getId(), service.getBalance(sender))
        LOG.info("Recipient [{}] final balance: {}", recipient.getId(), service.getBalance(recipient))
    }


    Transaction createGenesis(Wallet coinbase, Wallet sender, float value = 100f) {
        Transaction genesisTransaction = new Transaction(coinbase.publicKey, sender.getPublicKey(), value, null)
        //manually sign the genesis transaction
        genesisTransaction.generateSignature(coinbase.getPrivateKey())
        //manually set the transaction id
        genesisTransaction.transactionId = HashUtil.PREVIOUS_HASH_OF_GENESIS
        //manually add the Transactions Output
        def output = new TransactionOutput(genesisTransaction.getRecipient(), genesisTransaction.getValue(), genesisTransaction.getTransactionId())
        genesisTransaction.outputs.add(output)

        //its important to store our first transaction in the UTXOs list.
        repo.addTransactionOutput(output)
        genesisTransaction
    }
}

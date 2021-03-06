package com.pb.noobchain.service

import com.pb.noobchain.domain.Block
import com.pb.noobchain.domain.Transaction
import com.pb.noobchain.domain.TransactionOutput
import com.pb.noobchain.domain.Wallet
import com.pb.noobchain.repository.TransactionRepository
import com.pb.noobchain.service.util.HashUtil
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class BlockChainTestFactory {

    private static final Logger LOG = LoggerFactory.getLogger(BlockChainTestFactory)

    TransactionService transactionService
    BlockChainService blockChainService
    TransactionRepository transactionRepository

    List<Block> create() {
        List<Block> blockChain = []

        Wallet coinBase = new Wallet("coinBase")
        Wallet walletA = new Wallet("A")
        Wallet walletB = new Wallet("B")

        Transaction genesisTransaction = createGenesisTxn(coinBase, walletA, 100f)

        Block genesis = createGenesisBlock(genesisTransaction, blockChain)

        LOG.info("Wallet A is trying to send funds (40) to Wallet B ...")
        Block block1 = new Block("1", genesis.getHash())
        tryToSend(walletA, walletB, block1, blockChain, 40f)

        LOG.info("Wallet A is trying to send more funds (1000) than it has to Wallet B ...")
        def block2 = new Block("2", block1.getHash())
        tryToSend(walletA, walletB, block2, blockChain, 1000f)

        LOG.info("Wallet B is trying to send more funds (1000) than it has to Wallet A ...")
        def block3 = new Block("3", block2.getHash())
        tryToSend(walletB, walletA, block3, blockChain, 20f)
        blockChain
    }

    def tryToSend(Wallet sender, Wallet recipient, Block block, List<Block> blockChain, float amount) {
        LOG.info("Sender [{}] initial balance: {}", sender.getId(), transactionService.getBalance(sender))
        def txn = transactionService.sendFundsFromWallet(sender, recipient.getPublicKey(), amount)
        transactionService.addTransactionToBlock(txn, block)
        blockChainService.mineBlockAndAddToChain(block, blockChain)
        LOG.info("Sender [{}] final balance: {}", sender.getId(), transactionService.getBalance(sender))
        LOG.info("Recipient [{}] final balance: {}", recipient.getId(), transactionService.getBalance(recipient))
    }

    Block createGenesisBlock(Transaction genesisTransaction, List<Block> blockChain) {
        LOG.info("Creating and mining Genesis block... ")
        Block genesis = new Block("genesis", HashUtil.PREVIOUS_HASH_OF_GENESIS)
        transactionService.addTransactionToBlock(genesisTransaction, genesis)
        blockChainService.mineBlockAndAddToChain(genesis, blockChain)
        genesis
    }

    Transaction createGenesisTxn(Wallet sender, Wallet recipient, float value = 100f) {
        LOG.info("Create genesis transaction, which sends {} NoobCoin from wallet {} to wallet {}",
            value,
            sender.getId(),
            recipient.getId())

        Transaction genesisTransaction = new Transaction(sender.getPublicKey(), recipient.getPublicKey(), value, null)
        //manually sign the genesis transaction
        genesisTransaction.generateSignature(sender.getPrivateKey())
        //manually set the transaction id
        genesisTransaction.transactionId = HashUtil.PREVIOUS_HASH_OF_GENESIS
        //manually add the Transactions Output
        def output = new TransactionOutput(genesisTransaction.getRecipient(), genesisTransaction.getValue(), genesisTransaction.getTransactionId())
        genesisTransaction.outputs.add(output)

        //its important to store our 1st transaction in the UTXOs list.
        transactionRepository.addTransactionOutput(output)
        genesisTransaction
    }
}

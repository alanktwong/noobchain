package com.pb.noobchain.service.impl

import com.pb.noobchain.domain.Block
import com.pb.noobchain.domain.Transaction
import com.pb.noobchain.domain.Wallet
import com.pb.noobchain.exceptions.BrokenChainException
import com.pb.noobchain.exceptions.UnequalCurrentHashException
import com.pb.noobchain.exceptions.UnminedChainException
import com.pb.noobchain.service.HashUtil
import org.bouncycastle.jce.provider.BouncyCastleProvider
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import spock.lang.Specification

import java.security.Security

class BlockChainServiceImplSpec extends Specification {

    private static final Logger LOG = LoggerFactory.getLogger(BlockChainServiceImplSpec)

    BlockChainServiceImpl service
    def difficulty = 5

    def setup() {
        service = new BlockChainServiceImpl(minimumTransaction: 0.01)
    }

    def "create my 1st chain"() {
        when:
          def chain = service.myFirstChain(difficulty)

        then:
          chain != null && !chain.isEmpty()
    }

    def "serialize my 1st chain"() {
        given:
            def chain = service.myFirstChain(difficulty)

        when:
            def json = service.serialize(chain)

        then:
            json != null && !json.isEmpty()
    }

    def "should validate my 1st chain"() {
        given:
            def chain = service.myFirstChain(difficulty)

        when:
            def valid = service.validateChain(chain, difficulty)

        then:
            valid
    }

    def "should tamper with my 1st chain by changing previous hash"() {
        given:
          def chain = service.myFirstChain(difficulty)
          def aBlock = chain.get(1)
          aBlock.setPreviousHash("foo")

        when:
          service.validateChain(chain, difficulty)

        then:
          thrown UnequalCurrentHashException
    }

    def "should tamper with my 1st chain by changing hash"() {
        given:
          def chain = service.myFirstChain(difficulty)
          def aBlock = chain.get(1)
          aBlock.setHash("foo")

        when:
          service.validateChain(chain, difficulty)

        then:
          thrown UnequalCurrentHashException
    }

    def "should tamper with my 1st chain by changing nonce"() {
        given:
          def chain = service.myFirstChain(difficulty)
          def aBlock = chain.get(1)
          aBlock.setNonce(20)

        when:
          service.validateChain(chain, difficulty)

        then:
          thrown UnequalCurrentHashException
    }

    def "should tamper with my 1st chain by changing timestamp"() {
        given:
          def chain = service.myFirstChain(difficulty)
          def aBlock = chain.get(1)
          aBlock.setTimeStamp(new Date().getTime())

        when:
          service.validateChain(chain, difficulty)

        then:
          thrown UnequalCurrentHashException
    }

    def "should tamper with my 1st chain by changing merkleRoot"() {
        given:
          def chain = service.myFirstChain(difficulty)
          def aBlock = chain.get(1)
          aBlock.setMerkleRoot("tampering")

        when:
          service.validateChain(chain, difficulty)

        then:
          thrown UnequalCurrentHashException
    }

    def "should tamper with my 1st chain by trying to append a block"() {
        given:
          def chain = service.myFirstChain(difficulty)
          def newBlock = new Block("adding evil block")
          chain.add(newBlock)

        when:
          service.validateChain(chain, difficulty)

        then:
          thrown BrokenChainException
    }

    def "should tamper with my 1st chain by trying to remove a block"() {
        given:
          def chain = service.myFirstChain(difficulty)
          chain.remove(1)

        when:
          service.validateChain(chain, difficulty)

        then:
          thrown BrokenChainException
    }

    def "should tamper with my 1st chain by trying to add an unmined block"() {
        given:
          def chain = service.myFirstChain(difficulty)
          def block = new Block(chain.get(2).getHash())
          chain.add(block)

        when:
          service.validateChain(chain, difficulty)

        then:
          thrown UnminedChainException
    }

    def "should add to my 1st chain by trying to add an mined block"() {
        given:
          def chain = service.myFirstChain(difficulty)
          def block = new Block(chain.get(2).getHash())
          block.mine(difficulty)
          chain.add(block)

        when:
          def valid = service.validateChain(chain, difficulty)

        then:
          valid
    }

    def "should try mining my 1st chain"() {
        given:
            def chain = service.myFirstChain(difficulty)

        when:
            def mined = service.tryMining(chain, difficulty)
            def valid = service.validateChain(mined, difficulty)

        then:
            mined != null && !mined.isEmpty()
            valid
    }


    def "should create a verified transaction"() {

        given: "set up Bouncy Castle as security provider"
          Security.addProvider(new BouncyCastleProvider())

        and: "Create 2 new wallets"
          Wallet walletA = new Wallet()
          LOG.info("Keys - Private {} and public {}",
              HashUtil.getStringFromKey(walletA.getPrivateKey()),
              HashUtil.getStringFromKey(walletA.getPublicKey()))
          Wallet walletB = new Wallet()
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

}

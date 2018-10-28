package com.pb.noobchain.service.impl

import com.pb.noobchain.domain.Block
import com.pb.noobchain.exceptions.BrokenChainException
import com.pb.noobchain.exceptions.UnequalCurrentHashException
import com.pb.noobchain.exceptions.UnminedChainException
import com.pb.noobchain.repository.TransactionRepository
import com.pb.noobchain.repository.impl.TransactionRepositoryImpl
import com.pb.noobchain.service.BlockChainTestFactory
import com.pb.noobchain.service.TransactionService
import spock.lang.Specification

class BlockChainServiceImplSpec extends Specification {

    BlockChainServiceImpl service

    BlockChainTestFactory factory

    TransactionService transactionService

    TransactionRepository transactionRepository

    def difficulty = 5

    List<Block> chain

    def setup() {
        service = new BlockChainServiceImpl(difficulty: difficulty)

        transactionRepository = new TransactionRepositoryImpl()

        transactionService = new TransactionServiceImpl(transactionRepository)
        transactionService.minimumTransaction = -0.01

        factory = new BlockChainTestFactory(transactionService: transactionService,
            transactionRepository: transactionRepository,
            blockChainService: service)
        chain = factory.create()
    }

    def "serialize my 1st chain"() {
        when:
            def json = service.serialize(chain)

        then:
            json != null && !json.isEmpty()
    }

    def "should validate my 1st chain"() {
        when:
            def valid = service.validateChain(chain)

        then:
            valid
    }

    def "should tamper with my 1st chain by changing previous hash"() {
        given:
          def aBlock = chain.find{ it.id == "2"}
          aBlock.setPreviousHash("foo")

        when:
          service.validateChain(chain)

        then:
          thrown UnequalCurrentHashException
    }

    def "should tamper with my 1st chain by changing hash"() {
        given:
          def aBlock = chain.find{ it.id == "2"}
          aBlock.setHash("foo")

        when:
          service.validateChain(chain)

        then:
          thrown UnequalCurrentHashException
    }

    def "should tamper with my 1st chain by changing nonce"() {
        given:
          def aBlock = chain.find{ it.id == "2"}
          aBlock.setNonce(20)

        when:
          service.validateChain(chain)

        then:
          thrown UnequalCurrentHashException
    }

    def "should tamper with my 1st chain by changing timestamp"() {
        given:
          def aBlock = chain.find{ it.id == "2"}
          aBlock.setTimeStamp(new Date().getTime())

        when:
          service.validateChain(chain)

        then:
          thrown UnequalCurrentHashException
    }

    def "should tamper with my 1st chain by changing merkleRoot"() {
        given:
          def aBlock = chain.find{ it.id == "2"}
          aBlock.setMerkleRoot("tampering")

        when:
          service.validateChain(chain)

        then:
          thrown UnequalCurrentHashException
    }

    def "should tamper with my 1st chain by trying to append a block"() {
        given:
          def newBlock = new Block("4","adding evil block")
          chain.add(newBlock)

        when:
          service.validateChain(chain)

        then:
          thrown BrokenChainException
    }

    def "should tamper with my 1st chain by trying to remove a block"() {
        given:
          chain.remove(1)

        when:
          service.validateChain(chain)

        then:
          thrown BrokenChainException
    }

    def "should tamper with my 1st chain by trying to add an unmined block"() {
        given:
          def aBlock = chain.last()
          def block = new Block("4", aBlock.getHash())
          chain.add(block)

        when:
          service.validateChain(chain)

        then:
          thrown UnminedChainException
    }

    def "should add to my 1st chain by trying to add an mined block"() {
        given:
          def aBlock = chain.last()
          def block = new Block("4", aBlock.getHash())
          block.mine(this.difficulty)
          chain.add(block)

        when:
          def valid = service.validateChain(chain)

        then:
          valid
    }

    def "should try mining my 1st chain"() {
        when:
            def mined = service.tryMining(chain)
            def valid = service.validateChain(mined)

        then:
            mined != null && !mined.isEmpty()
            valid
    }
}

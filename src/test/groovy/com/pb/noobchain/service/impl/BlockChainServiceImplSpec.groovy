package com.pb.noobchain.service.impl

import com.pb.noobchain.domain.Block
import com.pb.noobchain.exceptions.BrokenChainException
import com.pb.noobchain.exceptions.UnequalCurrentHashException
import com.pb.noobchain.exceptions.UnminedChainException
import spock.lang.Specification

class BlockChainServiceImplSpec extends Specification {

    BlockChainServiceImpl service
    def difficulty = 5

    def setup() {
        service = new BlockChainServiceImpl(difficulty: difficulty)
    }

    def "create my 1st chain"() {
        when:
          def chain = service.myFirstChain()

        then:
          chain != null && !chain.isEmpty()
    }

    def "serialize my 1st chain"() {
        given:
            def chain = service.myFirstChain()

        when:
            def json = service.serialize(chain)

        then:
            json != null && !json.isEmpty()
    }

    def "should validate my 1st chain"() {
        given:
            def chain = service.myFirstChain()

        when:
            def valid = service.validateChain(chain)

        then:
            valid
    }

    def "should tamper with my 1st chain by changing previous hash"() {
        given:
          def chain = service.myFirstChain()
          def aBlock = chain.find{ it.id == "2"}
          aBlock.setPreviousHash("foo")

        when:
          service.validateChain(chain)

        then:
          thrown UnequalCurrentHashException
    }

    def "should tamper with my 1st chain by changing hash"() {
        given:
          def chain = service.myFirstChain()
          def aBlock = chain.find{ it.id == "2"}
          aBlock.setHash("foo")

        when:
          service.validateChain(chain)

        then:
          thrown UnequalCurrentHashException
    }

    def "should tamper with my 1st chain by changing nonce"() {
        given:
          def chain = service.myFirstChain()
          def aBlock = chain.find{ it.id == "2"}
          aBlock.setNonce(20)

        when:
          service.validateChain(chain)

        then:
          thrown UnequalCurrentHashException
    }

    def "should tamper with my 1st chain by changing timestamp"() {
        given:
          def chain = service.myFirstChain()
          def aBlock = chain.find{ it.id == "2"}
          aBlock.setTimeStamp(new Date().getTime())

        when:
          service.validateChain(chain)

        then:
          thrown UnequalCurrentHashException
    }

    def "should tamper with my 1st chain by changing merkleRoot"() {
        given:
          def chain = service.myFirstChain()
          def aBlock = chain.find{ it.id == "2"}
          aBlock.setMerkleRoot("tampering")

        when:
          service.validateChain(chain)

        then:
          thrown UnequalCurrentHashException
    }

    def "should tamper with my 1st chain by trying to append a block"() {
        given:
          def chain = service.myFirstChain()
          def newBlock = new Block("4","adding evil block")
          chain.add(newBlock)

        when:
          service.validateChain(chain)

        then:
          thrown BrokenChainException
    }

    def "should tamper with my 1st chain by trying to remove a block"() {
        given:
          def chain = service.myFirstChain()
          chain.remove(1)

        when:
          service.validateChain(chain)

        then:
          thrown BrokenChainException
    }

    def "should tamper with my 1st chain by trying to add an unmined block"() {
        given:
          def chain = service.myFirstChain()
          def aBlock = chain.last()
          def block = new Block("4",aBlock.getHash())
          chain.add(block)

        when:
          service.validateChain(chain)

        then:
          thrown UnminedChainException
    }

    def "should add to my 1st chain by trying to add an mined block"() {
        given:
          def chain = service.myFirstChain()
          def aBlock = chain.last()
          def block = new Block("4",aBlock.getHash())
          block.mine(this.difficulty)
          chain.add(block)

        when:
          def valid = service.validateChain(chain)

        then:
          valid
    }

    def "should try mining my 1st chain"() {
        given:
            def chain = service.myFirstChain()

        when:
            def mined = service.tryMining(chain)
            def valid = service.validateChain(mined)

        then:
            mined != null && !mined.isEmpty()
            valid
    }
}

package com.pb.noobchain.service.impl

import com.pb.noobchain.exceptions.BrokenChainException
import com.pb.noobchain.exceptions.UnequalCurrentHashException
import spock.lang.Specification

class BlockchainServiceImplSpec extends Specification {

    BlockchainServiceImpl service
    def difficulty = 5

    def setup() {
        service = new BlockchainServiceImpl()
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
            def valid = service.isChainValid(chain, difficulty)

        then:
            valid
    }

    def "should tamper with my 1st chain by changing previous hash"() {
        given:
          def chain = service.myFirstChain(difficulty)
          def aBlock = chain.get(1)
          aBlock.setPreviousHash("foo")

        when:
          service.isChainValid(chain, difficulty)

        then:
          thrown UnequalCurrentHashException
    }

    def "should tamper with my 1st chain by changing hash"() {
        given:
          def chain = service.myFirstChain(difficulty)
          def aBlock = chain.get(1)
          aBlock.setHash("foo")

        when:
          service.isChainValid(chain, difficulty)

        then:
          thrown UnequalCurrentHashException
    }

    def "should tamper with my 1st chain by changing nonce"() {
        given:
          def chain = service.myFirstChain(difficulty)
          def aBlock = chain.get(1)
          aBlock.setNonce(20)

        when:
          service.isChainValid(chain, difficulty)

        then:
          thrown UnequalCurrentHashException
    }

    def "should tamper with my 1st chain by changing timestamp"() {
        given:
          def chain = service.myFirstChain(difficulty)
          def aBlock = chain.get(1)
          aBlock.setTimeStamp(new Date().getTime())

        when:
          service.isChainValid(chain, difficulty)

        then:
          thrown UnequalCurrentHashException
    }

    def "should tamper with my 1st chain by changing data"() {
        given:
          def chain = service.myFirstChain(difficulty)
          def aBlock = chain.get(1)
          aBlock.setData("tampering")

        when:
          service.isChainValid(chain, difficulty)

        then:
          thrown UnequalCurrentHashException
    }

    def "should tamper with my 1st chain by changing links"() {
        given:
          def chain = service.myFirstChain(difficulty)
          def first = chain.get(0)
          def _2nd = chain.get(1)

        when:
          def valid = service.isChainValid(chain, difficulty)

        then:
          thrown BrokenChainException
    }

    def "should try mining my 1st chain"() {
        given:
            def chain = service.myFirstChain(difficulty)

        when:
            def mined = service.tryMining(chain, difficulty)
            def valid = service.isChainValid(mined, difficulty)

        then:
            mined != null && !mined.isEmpty()
            valid
    }
}

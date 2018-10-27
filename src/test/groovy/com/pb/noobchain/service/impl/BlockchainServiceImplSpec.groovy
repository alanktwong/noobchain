package com.pb.noobchain.service.impl

import spock.lang.Specification

class BlockchainServiceImplSpec extends Specification {

    BlockchainServiceImpl service
    def difficulty = 5

    def setup() {
        service = new BlockchainServiceImpl()
    }

    def "create my first chain"() {
        when:
          def chain = service.myFirstChain(difficulty)

        then:
          chain != null && !chain.isEmpty()
    }

    def "serialize my first chain"() {
        given:
            def chain = service.myFirstChain(difficulty)

        when:
            def json = service.serialize(chain)

        then:
            json != null && !json.isEmpty()
    }

    def "test my first chain"() {
        given:
            def chain = service.myFirstChain(difficulty)

        when:
            def valid = service.isChainValid(chain, difficulty)

        then:
            valid
    }

    def "test mining my first chain"() {
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

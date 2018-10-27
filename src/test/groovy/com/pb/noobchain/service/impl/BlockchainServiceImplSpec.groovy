package com.pb.noobchain.service.impl

import spock.lang.Specification

class BlockchainServiceImplSpec extends Specification {
    BlockchainServiceImpl service


    def setup() {
        service = new BlockchainServiceImpl()
    }

    def "create my first chain"() {
        when:
          def chain = service.myFirstChain()

        then:
          chain != null && !chain.isEmpty()
    }

    def "test my first chain"() {
        given:
            def chain = service.myFirstChain()

        when:
            def valid = service.isChainValid(chain)

        then:
            valid
    }

}

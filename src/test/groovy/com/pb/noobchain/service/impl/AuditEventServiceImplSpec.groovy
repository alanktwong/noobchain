package com.pb.noobchain.service.impl

import com.pb.noobchain.config.audit.AuditEventConverter
import com.pb.noobchain.domain.PersistentAuditEvent
import com.pb.noobchain.repository.PersistenceAuditEventRepository
import org.springframework.boot.actuate.audit.AuditEvent
import spock.lang.Specification

class AuditEventServiceImplSpec extends Specification {

    AuditEventServiceImpl service

    private PersistenceAuditEventRepository persistenceAuditEventRepository = Mock()

    private AuditEventConverter auditEventConverter = Mock()

    def setup() {
        service = new AuditEventServiceImpl(persistenceAuditEventRepository, auditEventConverter)
    }

    def "should find 1 audit event"() {
        given:
            def id = 1
            PersistentAuditEvent persistentAE = new PersistentAuditEvent()

        and:
            Map<String, Object> data = [:]
            def auditEvent = new AuditEvent("principal", "type", data)

        when:
            def result = service.find(id)

        then:
            result.isPresent()
            1 * persistenceAuditEventRepository.findOne(id) >> persistentAE
            1 * auditEventConverter.convertToAuditEvent(persistentAE) >> auditEvent
    }
}

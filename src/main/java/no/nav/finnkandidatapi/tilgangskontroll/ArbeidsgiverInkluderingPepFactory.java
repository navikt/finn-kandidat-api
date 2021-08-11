package no.nav.finnkandidatapi.tilgangskontroll;

import no.nav.common.abac.AbacCachedClient;
import no.nav.common.abac.AbacHttpClient;
import no.nav.common.abac.audit.*;

public class ArbeidsgiverInkluderingPepFactory {
    public ArbeidsgiverInkluderingPepFactory() {
    }

    private static ArbeidsgiverInkluderingPep get(String abacUrl, String srvUsername, String srvPassword, AuditConfig auditConfig) {
        AbacCachedClient abacClient = new AbacCachedClient(new AbacHttpClient(abacUrl, srvUsername, srvPassword));
        return new ArbeidsgiverInkluderingPep(srvUsername, abacClient, new NimbusSubjectProvider(), auditConfig);
    }

    public static ArbeidsgiverInkluderingPep get(String abacUrl, String srvUsername, String srvPassword, AuditRequestInfoSupplier auditRequestInfoSupplier, AuditLogFilter auditLogFilter) {
        AuditConfig auditConfig = new AuditConfig(new AuditLogger(), auditRequestInfoSupplier, auditLogFilter);
        return get(abacUrl, srvUsername, srvPassword, auditConfig);
    }

    public static ArbeidsgiverInkluderingPep get(String abacUrl, String srvUsername, String srvPassword, AuditRequestInfoSupplier auditRequestInfoSupplier) {
        AuditConfig auditConfig = new AuditConfig(new AuditLogger(), auditRequestInfoSupplier, (AuditLogFilter) null);
        return get(abacUrl, srvUsername, srvPassword, auditConfig);
    }

    public static ArbeidsgiverInkluderingPep get(String abacUrl, String srvUsername, String srvPassword) {
        return get(abacUrl, srvUsername, srvPassword, new AuditConfig((AuditLogger) null, (AuditRequestInfoSupplier) null, (AuditLogFilter) null));
    }
}

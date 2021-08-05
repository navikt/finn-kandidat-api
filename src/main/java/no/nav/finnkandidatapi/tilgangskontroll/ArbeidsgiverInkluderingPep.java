package no.nav.finnkandidatapi.tilgangskontroll;

import lombok.extern.slf4j.Slf4j;
import no.nav.common.abac.*;
import no.nav.common.abac.audit.*;
import no.nav.common.abac.cef.CefAbacEventContext;
import no.nav.common.abac.cef.CefAbacResponseMapper;
import no.nav.common.abac.constants.NavAttributter;
import no.nav.common.abac.domain.Attribute;
import no.nav.common.abac.domain.request.*;
import no.nav.common.abac.domain.response.Response;
import no.nav.common.abac.domain.response.XacmlResponse;
import no.nav.common.types.identer.EksternBrukerId;
import no.nav.common.types.identer.EnhetId;
import no.nav.common.types.identer.NavIdent;
import no.nav.common.utils.EnvironmentUtils;

import java.util.Optional;
import java.util.function.Supplier;

@Slf4j
public class ArbeidsgiverInkluderingPep implements Pep {

    private final AbacClient abacClient;
    private final String srvUsername;
    private final SubjectProvider subjectProvider;
    private final AuditConfig auditConfig;

    private static final String PEP_ID = "finn-kandidat-api";

    public ArbeidsgiverInkluderingPep(String srvUsername, AbacClient abacClient, SubjectProvider subjectProvider, AuditConfig auditConfig) {
        this.srvUsername = srvUsername;
        this.abacClient = abacClient;
        this.subjectProvider = subjectProvider;
        this.auditConfig = auditConfig;
    }

    public boolean harVeilederTilgangTilEnhet(NavIdent veilederIdent, EnhetId enhetId) {
        ActionId actionId = ActionId.READ;
        Resource resource = XacmlRequestBuilder.lagEnhetResource(enhetId, "arbeidsgiver-inkludering");
        addPepid(resource);
        XacmlRequest xacmlRequest = XacmlRequestBuilder.buildRequest(XacmlRequestBuilder.lagEnvironment(this.srvUsername), XacmlRequestBuilder.lagAction(actionId), XacmlRequestBuilder.lagVeilederAccessSubject(veilederIdent), resource);
        Supplier<CefAbacEventContext> cefEventContext = () -> {
            CefAbacResponseMapper mapper = CefAbacResponseMapper.enhetIdMapper(enhetId, actionId, resource);
            return this.lagCefEventContext(mapper, veilederIdent.get());
        };
        return this.harTilgang(xacmlRequest, cefEventContext);
    }



    public boolean harTilgangTilEnhet(String innloggetBrukerIdToken, EnhetId enhetId) {
        String oidcTokenBody = AbacUtils.extractOidcTokenBody(innloggetBrukerIdToken);
        Resource resource = XacmlRequestBuilder.lagEnhetResource(enhetId, "arbeidsgiver-inkludering");
        addPepid(resource);

        ActionId actionId = ActionId.READ;
        XacmlRequest xacmlRequest = XacmlRequestBuilder.buildRequest(XacmlRequestBuilder.lagEnvironmentMedOidcTokenBody(this.srvUsername, oidcTokenBody), XacmlRequestBuilder.lagAction(actionId), (AccessSubject) null, resource);
        Supplier<CefAbacEventContext> cefEventContext = () -> {
            CefAbacResponseMapper mapper = CefAbacResponseMapper.enhetIdMapper(enhetId, actionId, resource);
            return this.lagCefEventContext(mapper, this.subjectProvider.getSubjectFromToken(innloggetBrukerIdToken));
        };
        return this.harTilgang(xacmlRequest, cefEventContext);
    }

    public boolean harTilgangTilEnhetMedSperre(String innloggetBrukerIdToken, EnhetId enhetId) {
        String oidcTokenBody = AbacUtils.extractOidcTokenBody(innloggetBrukerIdToken);
        Resource resource = XacmlRequestBuilder.lagEnhetMedSperreResource(enhetId, "arbeidsgiver-inkludering");
        addPepid(resource);
        ActionId actionId = ActionId.READ;
        XacmlRequest xacmlRequest = XacmlRequestBuilder.buildRequest(XacmlRequestBuilder.lagEnvironmentMedOidcTokenBody(this.srvUsername, oidcTokenBody), XacmlRequestBuilder.lagAction(actionId), (AccessSubject) null, resource);
        Supplier<CefAbacEventContext> cefEventContext = () -> {
            CefAbacResponseMapper mapper = CefAbacResponseMapper.enhetIdMapper(enhetId, actionId, resource);
            return this.lagCefEventContext(mapper, this.subjectProvider.getSubjectFromToken(innloggetBrukerIdToken));
        };
        return this.harTilgang(xacmlRequest, cefEventContext);
    }

    public boolean harVeilederTilgangTilPerson(NavIdent veilederIdent, ActionId actionId, EksternBrukerId eksternBrukerId) {

        log.info("Forsøker å spørre ABAC om veileder har tilgang til person");

        Resource resource = XacmlRequestBuilder.lagPersonResource(eksternBrukerId, "arbeidsgiver-inkludering");
        addPepid(resource);
        XacmlRequest xacmlRequest = XacmlRequestBuilder.buildRequest(XacmlRequestBuilder.lagEnvironment(this.srvUsername), XacmlRequestBuilder.lagAction(actionId), XacmlRequestBuilder.lagVeilederAccessSubject(veilederIdent), resource);
        Supplier<CefAbacEventContext> cefEventContext = () -> {
            CefAbacResponseMapper mapper = CefAbacResponseMapper.personIdMapper(eksternBrukerId, actionId, resource);
            return this.lagCefEventContext(mapper, veilederIdent.get());
        };
        boolean harTilgang = this.harTilgang(xacmlRequest, cefEventContext);

        log.info("Fikk svar fra ABAC om veileder har tilgang til person: {}", harTilgang);

        return harTilgang;
    }

    public boolean harTilgangTilPerson(String innloggetBrukerIdToken, ActionId actionId, EksternBrukerId eksternBrukerId) {
        String oidcTokenBody = AbacUtils.extractOidcTokenBody(innloggetBrukerIdToken);
        Resource resource = XacmlRequestBuilder.lagPersonResource(eksternBrukerId, "arbeidsgiver-inkludering");
        addPepid(resource);
        XacmlRequest xacmlRequest = XacmlRequestBuilder.buildRequest(XacmlRequestBuilder.lagEnvironmentMedOidcTokenBody(this.srvUsername, oidcTokenBody), XacmlRequestBuilder.lagAction(actionId), (AccessSubject) null, resource);
        Supplier<CefAbacEventContext> cefEventContext = () -> {
            CefAbacResponseMapper mapper = CefAbacResponseMapper.personIdMapper(eksternBrukerId, actionId, resource);
            return this.lagCefEventContext(mapper, this.subjectProvider.getSubjectFromToken(innloggetBrukerIdToken));
        };
        return this.harTilgang(xacmlRequest, cefEventContext);
    }

    public boolean harTilgangTilOppfolging(String innloggetBrukerIdToken) {
        String oidcTokenBody = AbacUtils.extractOidcTokenBody(innloggetBrukerIdToken);
        Resource resource = XacmlRequestBuilder.lagOppfolgingDomeneResource();
        addPepid(resource);
        String tokenSubject = this.subjectProvider.getSubjectFromToken(innloggetBrukerIdToken);
        XacmlRequest xacmlRequest = XacmlRequestBuilder.buildRequest(XacmlRequestBuilder.lagEnvironmentMedOidcTokenBody(this.srvUsername, oidcTokenBody), (Action) null, (AccessSubject) null, resource);
        Supplier<CefAbacEventContext> cefEventContext = () -> {
            CefAbacResponseMapper mapper = CefAbacResponseMapper.resourceMapper(resource);
            return this.lagCefEventContext(mapper, tokenSubject);
        };
        return this.harTilgang(xacmlRequest, cefEventContext);
    }

    public boolean harVeilederTilgangTilModia(String innloggetVeilederIdToken) {
        String oidcTokenBody = AbacUtils.extractOidcTokenBody(innloggetVeilederIdToken);
        Resource resource = XacmlRequestBuilder.lagModiaDomeneResource();
        addPepid(resource);
        String veilederIdent = this.subjectProvider.getSubjectFromToken(innloggetVeilederIdToken);
        XacmlRequest xacmlRequest = XacmlRequestBuilder.buildRequest(XacmlRequestBuilder.lagEnvironmentMedOidcTokenBody(this.srvUsername, oidcTokenBody), (Action) null, (AccessSubject) null, resource);
        Supplier<CefAbacEventContext> cefEventContext = () -> {
            CefAbacResponseMapper mapper = CefAbacResponseMapper.resourceMapper(resource);
            return this.lagCefEventContext(mapper, veilederIdent);
        };
        return this.harTilgang(xacmlRequest, cefEventContext);
    }

    public boolean harVeilederTilgangTilKode6(NavIdent veilederIdent) {
        Resource resource = XacmlRequestBuilder.lagKode6Resource("arbeidsgiver-inkludering");
        addPepid(resource);
        XacmlRequest xacmlRequest = XacmlRequestBuilder.buildRequest(XacmlRequestBuilder.lagEnvironment(this.srvUsername), (Action) null, XacmlRequestBuilder.lagVeilederAccessSubject(veilederIdent), resource);
        Supplier<CefAbacEventContext> cefEventContext = () -> {
            CefAbacResponseMapper mapper = CefAbacResponseMapper.resourceMapper(resource);
            return this.lagCefEventContext(mapper, veilederIdent.get());
        };
        return this.harTilgang(xacmlRequest, cefEventContext);
    }

    public boolean harVeilederTilgangTilKode7(NavIdent veilederIdent) {
        Resource resource = XacmlRequestBuilder.lagKode7Resource("arbeidsgiver-inkludering");
        addPepid(resource);
        XacmlRequest xacmlRequest = XacmlRequestBuilder.buildRequest(XacmlRequestBuilder.lagEnvironment(this.srvUsername), (Action) null, XacmlRequestBuilder.lagVeilederAccessSubject(veilederIdent), resource);
        Supplier<CefAbacEventContext> cefEventContext = () -> {
            CefAbacResponseMapper mapper = CefAbacResponseMapper.resourceMapper(resource);
            return this.lagCefEventContext(mapper, veilederIdent.get());
        };
        return this.harTilgang(xacmlRequest, cefEventContext);
    }

    public boolean harVeilederTilgangTilEgenAnsatt(NavIdent veilederIdent) {
        Resource resource = XacmlRequestBuilder.lagEgenAnsattResource("arbeidsgiver-inkludering");
        addPepid(resource);
        XacmlRequest xacmlRequest = XacmlRequestBuilder.buildRequest(XacmlRequestBuilder.lagEnvironment(this.srvUsername), (Action) null, XacmlRequestBuilder.lagVeilederAccessSubject(veilederIdent), resource);
        Supplier<CefAbacEventContext> cefEventContext = () -> {
            CefAbacResponseMapper mapper = CefAbacResponseMapper.resourceMapper(resource);
            return this.lagCefEventContext(mapper, veilederIdent.get());
        };
        return this.harTilgang(xacmlRequest, cefEventContext);
    }

    public AbacClient getAbacClient() {
        return this.abacClient;
    }

    private boolean harTilgang(XacmlRequest xacmlRequest, Supplier<CefAbacEventContext> cefEventContext) {
        XacmlResponse xacmlResponse = this.abacClient.sendRequest(xacmlRequest);

        printRespons(xacmlResponse);

        if (cefEventContext != null && this.skalLogges(xacmlRequest, xacmlResponse)) {
            this.getAuditLogger().ifPresent((auditLogger) -> {
                auditLogger.logCef(xacmlRequest, xacmlResponse, (CefAbacEventContext) cefEventContext.get());
            });
        }

        return XacmlResponseParser.harTilgang(xacmlResponse);
    }

    private void printRespons(XacmlResponse response) {
        response.getResponse().stream().forEach((Response res) -> {
            log.info("{}", res);
        });
    }

    private boolean skalLogges(XacmlRequest xacmlRequest, XacmlResponse xacmlResponse) {
        return (Boolean) this.getAuditRequestInfoSupplier().map(AuditRequestInfoSupplier::get).map((auditRequestInfo) -> {
            return (Boolean) this.getAuditLogFilter().map((filter) -> {
                return filter.isEnabled(auditRequestInfo, xacmlRequest, xacmlResponse);
            }).orElse(true);
        }).orElse(false);
    }

    private Optional<AuditLogger> getAuditLogger() {
        return this.auditConfig != null ? Optional.ofNullable(this.auditConfig.getAuditLogger()) : Optional.empty();
    }

    private Optional<AuditRequestInfoSupplier> getAuditRequestInfoSupplier() {
        return this.auditConfig != null ? Optional.ofNullable(this.auditConfig.getAuditRequestInfoSupplier()) : Optional.empty();
    }

    private Optional<AuditLogFilter> getAuditLogFilter() {
        return this.auditConfig != null ? Optional.ofNullable(this.auditConfig.getAuditLogFilter()) : Optional.empty();
    }

    private CefAbacEventContext lagCefEventContext(CefAbacResponseMapper mapper, String subjectId) {
        Optional<AuditRequestInfo> requestInfo = this.getAuditRequestInfoSupplier().map(AuditRequestInfoSupplier::get);
        return CefAbacEventContext.builder()
                .applicationName(EnvironmentUtils.requireApplicationName())
                .callId(requestInfo.map(AuditRequestInfo::getCallId)
                        .orElse(null))
                .consumerId(requestInfo.map(AuditRequestInfo::getConsumerId)
                        .orElse(null))
                .requestMethod((String) requestInfo.map(AuditRequestInfo::getRequestMethod)
                        .orElse(null)).requestPath((String) requestInfo
                        .map(AuditRequestInfo::getRequestPath).orElse(null))
                .subjectId(subjectId).mapper(mapper)
                .build();
    }

    private boolean addPepid(Resource resource) {
        return resource.getAttribute().add(new Attribute(NavAttributter.ENVIRONMENT_FELLES_PEP_ID, PEP_ID));
    }
}

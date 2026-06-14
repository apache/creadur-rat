# Security

TBD

## Known Non-Findings

- Static code analysis may report `XXE_DOCUMENT` vulnerabilities because RAT reads XML and XSLT files provided as user input.

  - Configuration files and XSLT documents passed to RAT are operator-controlled configuration, not request input. Reports claiming SSRF or path traversal via these resolvers, based on the assumption that the resource name is attacker-controlled, are out of scope under the documented threat model. XML and XSLT authorship, as well as resource configuration, are privileged operations.

  - Applications that thread untrusted input into XML configuration or XSLT documents should validate that input before passing it to RAT. Responsibility for such validation rests with the application, not with RAT.

## Threat Model

The full Apache Creadur RAT threat model — scope and intended use, trust
boundaries, the security properties RAT provides and disclaims, the adversary
model, and known non-findings — is documented in
[THREAT_MODEL.md](./THREAT_MODEL.md). The scope notes above are a summary;
THREAT_MODEL.md is the detailed companion.

<!-- SPDX-License-Identifier: Apache-2.0 -->
# Security Policy

## Reporting a Vulnerability

Apache Creadur follows the Apache Software Foundation security process. Report
suspected vulnerabilities **privately** to [security@apache.org](mailto:security@apache.org)
(the PMC is reachable via `private@creadur.apache.org`). Do **not** open public
issues/PRs for security reports. See <https://www.apache.org/security/>.

## Threat Model

See **[THREAT_MODEL.md](THREAT_MODEL.md)**. RAT is a build-time/CLI license-audit
tool, not a network service and not a security scanner. Its security-relevant
surface (XML config parsing, archive descent) matters when RAT audits
**untrusted** input; findings requiring trusted input are out of model.

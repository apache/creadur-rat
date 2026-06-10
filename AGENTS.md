<!-- SPDX-License-Identifier: Apache-2.0 -->
# AGENTS.md

## Security

You are helping a security researcher evaluate findings against Apache Creadur
RAT (Release Audit Tool). Before drafting any report:

1. Read the threat model: **[THREAT_MODEL.md](THREAT_MODEL.md)** — RAT is an
   in-process build/CLI license-audit tool; its security-relevant case is
   auditing **untrusted** input (XML config -> XXE; archives -> decompression
   bombs). Findings that require RAT to process input the operator already
   trusts are out of model.
2. Read **[SECURITY.md](SECURITY.md)** for how to report.

RAT is **not** a security/vulnerability scanner. Route any finding through
`THREAT_MODEL.md` section 13, citing the section.

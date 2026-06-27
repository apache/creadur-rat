<!--
  Licensed to the Apache Software Foundation (ASF) under one or more
  contributor license agreements.  See the NOTICE file distributed with
  this work for additional information regarding copyright ownership.
  The ASF licenses this file to You under the Apache License, Version 2.0
  (the "License"); you may not use this file except in compliance with
  the License.  You may obtain a copy of the License at

      https://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
-->

# Security

Apache Creadur RAT (Release Audit Tool) runs as a CLI, an Ant task, or a Maven
plugin in the developer's or CI's own process — it is not a network service. It
audits a source tree against operator-controlled license and header definitions.

## Reporting a Vulnerability

Please report suspected security vulnerabilities privately to the Apache Security
Team at security@apache.org, following the
[ASF vulnerability handling process](https://www.apache.org/security/). Please do
not report security issues on public issue trackers or mailing lists.

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

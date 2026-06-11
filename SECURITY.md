<!--
  Licensed to the Apache Software Foundation (ASF) under one or more
  contributor license agreements.  See the NOTICE file distributed with
  this work for additional information regarding copyright ownership.
  The ASF licenses this file to You under the Apache License, Version 2.0
  (the "License"); you may not use this file except in compliance with
  the License.  You may obtain a copy of the License at

      http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
-->
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

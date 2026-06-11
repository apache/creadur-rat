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

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
# Agent guidance

This file is read by automated agents (security scanners, code
analyzers, AI assistants) operating on this repository. It
points them at the human-authored references they should
consult before producing output.

# SECURITY
- Before reporting any security finding, consult [`SECURITY.md`](SECURITY.md)
  and the full threat model in [`THREAT_MODEL.md`](THREAT_MODEL.md).
- `THREAT_MODEL.md` defines RAT's scope, trust boundaries, the security
  properties RAT provides and disclaims, the adversary model, and the
  known non-findings used to triage scanner output.
- Report suspected vulnerabilities privately to security@apache.org; do not
  open public issues for them.

# CODE STYLE
- Use consistent naming conventions
- Follow language-specific style guides
- Keep functions small and focused
- Use meaningful variable and function names
- Add comments for complex logic
- Add package-info.java to each new package in the source tree

# BEST PRACTICES
- **DRY (Don't Repeat Yourself)**: Avoid code duplication
- **SOLID Principles**: Follow object-oriented design principles
- **Error Handling**: Always handle potential errors gracefully
- **Security**: Consider security implications in your code
- **Version Control**: Write clear commit messages, referencing Apache Creadur Jira tickets if possible

# COMMUNICATION
- Explain your approach before implementing
- Break down complex solutions into steps
- Provide examples when helpful
- Ask clarifying questions when requirements are unclear

# RESTRICTIONS
- Always ask before making breaking changes
- Don't add unnecessary dependencies
- Follow the existing codebase patterns and conventions
- Test your solutions when possible with unit or integration tests

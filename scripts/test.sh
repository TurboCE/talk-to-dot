#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/.."
if [[ -n "${JAVA_HOME:-}" ]]; then export PATH="$JAVA_HOME/bin:$PATH"; fi
mkdir -p build/tests
javac -d build/tests app/src/local/zipshortcut/CallPolicy.java tests/local/zipshortcut/CallPolicyTest.java
java -cp build/tests local.zipshortcut.CallPolicyTest
python3 - <<'PY'
import xml.etree.ElementTree as ET
from pathlib import Path
ns='{http://schemas.android.com/apk/res/android}'
manifest=ET.parse('app/AndroidManifest.xml').getroot()
assert not manifest.findall('uses-permission'), 'No Internet, microphone or phone permissions expected'
assert manifest.find('application').get(ns+'allowBackup')=='false'
service=ET.parse('app/res/xml/zip_service.xml').getroot()
assert service.get(ns+'packageNames')=='com.openai.chatgpt'
assert service.get(ns+'canPerformGestures')!='true'
a=ET.parse('app/res/values/strings.xml').getroot()
b=ET.parse('app/res/values-ko/strings.xml').getroot()
assert {x.get('name') for x in a}=={x.get('name') for x in b}
assert Path('LICENSE').read_text().startswith('MIT License')
print('PASS manifest scope, privacy, MIT notice and locale parity')
PY

# Compile the actual service against small deterministic Android fakes. No device actions.
python3 - <<'PYGEN'
from pathlib import Path
import xml.etree.ElementTree as ET
names=[n.get('name') for n in ET.parse('app/res/values/strings.xml').getroot() if n.tag=='string']
p=Path('build/test-generated/local/zipshortcut');p.mkdir(parents=True,exist_ok=True)
(p/'R.java').write_text('package local.zipshortcut; public final class R {public static class string {'+''.join('public static final int '+n+'='+str(i+1)+';' for i,n in enumerate(names))+'}}')
PYGEN
javac -d build/tests $(find tests/stubs -name '*.java') build/test-generated/local/zipshortcut/R.java app/src/local/zipshortcut/{ZipService,CallPolicy,CallSettings,DotSettings}.java tests/local/zipshortcut/ZipServiceTest.java
java -cp build/tests local.zipshortcut.ZipServiceTest

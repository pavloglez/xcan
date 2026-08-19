import os
for root, dirs, files in os.walk('.'):
    for f in files:
        if f == 'build.gradle.kts' and root != '.':
            path = os.path.join(root, f)
            with open(path, 'r') as file:
                content = file.read()
            if 'alias(libs.plugins.kotlin.android)' in content:
                content = content.replace('    alias(libs.plugins.kotlin.android)\n', '')
                with open(path, 'w') as file:
                    file.write(content)
